/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.search.impl;

import static net.hillsdon.reviki.text.WikiWordUtils.lastComponentOfPath;
import static net.hillsdon.reviki.text.WikiWordUtils.pathToTitle;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.search.QuerySyntaxException;
import net.hillsdon.reviki.search.SearchEngine;
import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.RenderedPage;
import net.hillsdon.reviki.wiki.RenderedPageFactory;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.LockObtainFailedException;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Uses lucene to provide search capabilities.
 *
 * @author mth
 */
public class LuceneSearcher implements SearchEngine {

  public static class NoQueryPerformedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public NoQueryPerformedException(final QuerySyntaxException cause) {
      super("No query was performed yet got query error", cause);
    }
  }

  private static final String FIELD_UID = "uid";
  private static final String FIELD_WIKI = "wiki";
  private static final String FIELD_PATH = "path";
  private static final String FIELD_PATH_LOWER = "path-lower";
  private static final String FIELD_CONTENT = "content";
  /**
   * We tokenize the wiki word to allow e.g. 'another' to find 'AnotherNewPage'.
   */
  private static final String FIELD_TITLE_TOKENIZED = "title";
  private static final String FIELD_OUTGOING_LINKS = "outgoing-links";

  private static final String FIELD_PROPERTY_KEY = "property";
  private static final String FIELD_PROPERTY_VALUE = "property-value";
  private static final String PROPERTY_LAST_INDEXED_REVISION = "last-indexed-revision";

  private static final String[] ALL_SEARCH_FIELDS = new String[] {FIELD_PATH, FIELD_PATH_LOWER, FIELD_TITLE_TOKENIZED, FIELD_CONTENT};

  private final String _wikiName;
  private final File _dir;
  private final List<File> _otherDirs;
  private final RenderedPageFactory _renderedPageFactory;

  public static String uidFor(final String wiki, final String path) {
    return (wiki==null ? "" : wiki) + "::" + (path==null ? "" : path);
  }

  /**
   * @param dir The search index lives here.
   *            If null is passed the search will behave as a null implementation.
   */
  public LuceneSearcher(final String wikiName, final File dir, final List<File> otherSearchDirs, final RenderedPageFactory renderedPageFactory) {
    _wikiName = wikiName;
    _dir = dir;
    _otherDirs = otherSearchDirs;
    _renderedPageFactory = renderedPageFactory;
  }

  private void createIndexIfNecessary() throws IOException {
    if (_dir != null && !IndexReader.indexExists(_dir)) {
      new IndexWriter(_dir, createAnalyzer(), true).close();
    }
  }

  private Analyzer createAnalyzer() {
    final Analyzer text = new StandardAnalyzer() {
      @Override
      public TokenStream tokenStream(final String fieldName, final Reader reader) {
        return new PorterStemFilter(super.tokenStream(fieldName, reader));
      }
    };
    final Analyzer id = new KeywordAnalyzer();
    final PerFieldAnalyzerWrapper perField = new PerFieldAnalyzerWrapper(new Analyzer() {
      @Override
      public TokenStream tokenStream(final String fieldName, final Reader reader) {
        throw new UnsupportedOperationException("Need to define analyser for: " + fieldName);
      }
    });
    perField.addAnalyzer(FIELD_UID, id);
    perField.addAnalyzer(FIELD_WIKI, id);
    perField.addAnalyzer(FIELD_PATH, id);
    perField.addAnalyzer(FIELD_PATH_LOWER, id);
    perField.addAnalyzer(FIELD_TITLE_TOKENIZED, text);
    perField.addAnalyzer(FIELD_PROPERTY_KEY, id);
    perField.addAnalyzer(FIELD_PROPERTY_VALUE, id);
    perField.addAnalyzer(FIELD_OUTGOING_LINKS, text);
    perField.addAnalyzer(FIELD_CONTENT, text);
    return perField;
  }

  private Document createWikiPageDocument(final String wiki, final String path, final String content) throws IOException, PageStoreException {
    RenderedPage renderedPage = _renderedPageFactory.create(path, content, URLOutputFilter.NULL);
    Document document = new Document();
    final String title = pathToTitle(path);
    final String pathLower = lastComponentOfPath(path).toLowerCase();
    document.add(new Field(FIELD_UID, uidFor(wiki, path), Field.Store.YES, Field.Index.UN_TOKENIZED));
    document.add(new Field(FIELD_WIKI, wiki, Field.Store.YES, Field.Index.UN_TOKENIZED));
    document.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));
    document.add(new Field(FIELD_PATH_LOWER, pathLower, Field.Store.YES, Field.Index.UN_TOKENIZED));
    document.add(new Field(FIELD_TITLE_TOKENIZED, title, Field.Store.YES, Field.Index.TOKENIZED));
    document.add(new Field(FIELD_OUTGOING_LINKS, Joiner.on(" ").join(renderedPage.findOutgoingWikiLinks()), Field.Store.YES, Field.Index.TOKENIZED));
    // We store the content in order to show matching extracts.
    document.add(new Field(FIELD_CONTENT, content, Field.Store.YES, Field.Index.TOKENIZED));
    return document;
  }

  private Document createPropertyDocument(final String property, final String value) {
    Document document = new Document();
    document.add(new Field(FIELD_PROPERTY_KEY, property, Field.Store.YES, Field.Index.UN_TOKENIZED));
    document.add(new Field(FIELD_PROPERTY_VALUE, value, Field.Store.YES, Field.Index.UN_TOKENIZED));
    return document;
  }

  private void deleteWikiDocument(final String wiki, final String path) throws IOException {
    deleteDocument(FIELD_UID, uidFor(wiki, path));
  }

  private void deleteDocument(final String keyField, final String value) throws IOException {
    IndexWriter writer = new IndexWriter(_dir, createAnalyzer());
    try {
      writer.deleteDocuments(new Term(keyField, value));
    }
    finally {
      writer.close();
    }
  }

  private void replaceProperty(final Document propertyDocument) throws CorruptIndexException, LockObtainFailedException, IOException {
    replaceDocument(FIELD_PROPERTY_KEY, propertyDocument);
  }

  private void replaceWikiDocument(final Document wikiDocument) throws CorruptIndexException, LockObtainFailedException, IOException {
    replaceDocument(FIELD_UID, wikiDocument);
  }

  private void replaceDocument(final String keyField, final Document document) throws CorruptIndexException, LockObtainFailedException, IOException {
    IndexWriter writer = new IndexWriter(_dir, createAnalyzer());
    try {
      writer.deleteDocuments(new Term(keyField, document.get(keyField)));
      writer.addDocument(document);
      writer.optimize();
    }
    finally {
      writer.close();
    }
  }

  // Lucene allows multiple non-deleting readers and at most one writer at a time.
  // It maintains a lock file but we never want it to fail to take the lock, so serialize writes.
  public synchronized void index(final String wiki, final String path, final long revision, final String content) throws IOException, PageStoreException {
    if (_dir == null) {
      return;
    }
    createIndexIfNecessary();
    replaceWikiDocument(createWikiPageDocument(wiki, path, content));
    rememberLastIndexedRevision(revision);
  }

  // See comment on index.
  public synchronized void delete(final String wiki, final String path, final long revision) throws IOException {
    createIndexIfNecessary();
    deleteWikiDocument(wiki, path);
    rememberLastIndexedRevision(revision);
  }

  public Set<String> incomingLinks(final String page) throws IOException, PageStoreException {
    if (_dir == null) {
      return Collections.emptySet();
    }
    try {
      return doReadOperation(new ReadOperation<Set<String>>() {
        public Set<String> execute(final IndexReader reader, final Searcher searcher, final Analyzer analyzer) throws IOException, ParseException {
          final String pageEscaped = escape(URIUtil.encodeWithinPath(page));
          Set<String> results = Sets.newLinkedHashSet(Iterables.transform(query(reader, createAnalyzer(), searcher, FIELD_OUTGOING_LINKS, pageEscaped, false), SearchMatch.TO_PAGE_NAME));
          results.remove(page);
          return results;
        }
      }, false);
    }
    catch (QuerySyntaxException ex) {
      throw new NoQueryPerformedException(ex);
    }
  }

  public Set<String> outgoingLinks(final String page) throws IOException, PageStoreException {
    if (_dir == null) {
      return Collections.emptySet();
    }
    try {
      return doReadOperation(new ReadOperation<Set<String>>() {
        public Set<String> execute(final IndexReader reader, final Searcher searcher, final Analyzer analyzer) throws IOException, ParseException {
          Hits hits = searcher.search(new TermQuery(new Term(FIELD_PATH, page)));
          Iterator<?> iterator = hits.iterator();
          if (iterator.hasNext()) {
            Hit hit = (Hit) iterator.next();
            String outgoingLinks = hit.getDocument().get(FIELD_OUTGOING_LINKS);
            Set<String> results = ImmutableSet.copyOf(outgoingLinks.split("\\s"));
            results.remove(page);
            return results;
          }
          return Collections.emptySet();
        }
      }, false);
    }
    catch (QuerySyntaxException ex) {
      throw new NoQueryPerformedException(ex);
    }
  }

  /**
   * Reusable template that cleans up properly.
   * @param <T> Result type.
   * @param operation Operation to perform.
   * @param allIndices If true, search all indices (other wikis) not just our own.
   * @return Result from operation.
   * @throws IOException On index read error,
   * @throws QuerySyntaxException If we can't parse a query.
   */
  private <T> T doReadOperation(final ReadOperation<T> operation, final boolean allIndices) throws IOException, QuerySyntaxException {
    createIndexIfNecessary();

    List<Searcher> searchers = new ArrayList<Searcher>();
    List<IndexReader> readers = new ArrayList<IndexReader>();

    /* First add our reader/searcher. If this fails, it's an error but clean up. */
    IndexReader reader = IndexReader.open(_dir);
    Searcher searcher = null;
    try {
      searcher = new IndexSearcher(reader);
      searchers.add(searcher);
      readers.add(reader);
    }
    finally {
      if (searcher==null) {
        reader.close();
      }
    }

    if (allIndices) {
      for (File dir: _otherDirs) {
        searcher = null;
        reader = null;
        try {
          reader = IndexReader.open(dir);
          searcher = new IndexSearcher(reader);
          searchers.add(searcher);
          readers.add(reader);
        }
        catch (Exception e) {
          // The index may not exist, but other wikis' indices aren't that important anyway, so
          // just don't search them.
          if (searcher!=null) {
            searcher.close();
          }
          if (reader!=null) {
            reader.close();
          }
        }
      }
    }

    try {
      /* Don't bother using a multi searcher if we only have one */
      if (searchers.size()>1) {
        searcher = new MultiSearcher(searchers.toArray(new Searcher[]{}));
        /* Add to list of searchers so it gets closed */
        searchers.add(searcher);
      }
      else {
        searcher = searchers.get(0);
      }

      try {
        Analyzer analyzer = createAnalyzer();
        return operation.execute(readers.get(0), searcher, analyzer);
      }
      catch (ParseException ex) {
        throw new QuerySyntaxException(ex.getMessage(), ex);
      }
    }
    finally {
      for (Searcher s: searchers) {
        try {
          s.close();
        } catch (Exception e) {}
      }
      for (IndexReader r: readers) {
        try {
          r.close();
        } catch (Exception e) {}
      }
    }
  }

  public Set<SearchMatch> search(final String queryString, final boolean provideExtracts, final boolean singleWiki) throws IOException, QuerySyntaxException {
    if (_dir == null || queryString == null || queryString.trim().length() == 0) {
      return Collections.emptySet();
    }
    return doReadOperation(new ReadOperation<Set<SearchMatch>>() {
      public Set<SearchMatch> execute(final IndexReader reader, final Searcher searcher, final Analyzer analyzer) throws IOException, ParseException {
        LinkedHashSet<SearchMatch> results = new LinkedHashSet<SearchMatch>();
        // Prefer path, then title then content matches (match equality is on page name)
        for (String field : ALL_SEARCH_FIELDS) {
          if (field.equals(FIELD_PATH_LOWER)) {
            final Query query = new WildcardQuery(new Term(FIELD_PATH_LOWER, "*" + queryString.toLowerCase() + "*"));
            results.addAll(doQuery(reader, analyzer, searcher, field, provideExtracts, query));
          }
          else {
            results.addAll(query(reader, analyzer, searcher, field, queryString, provideExtracts));
          }
        }
        return orderResults(results);
      }
    }, !singleWiki);
  }

  private Set<SearchMatch> orderResults(final Set<SearchMatch> results) {
    /* Split single set of results into per-wiki sets, maintaining order (within each set) */
    Map<String, Set<SearchMatch>> byWiki = new LinkedHashMap<String, Set<SearchMatch>>();
    for(SearchMatch match: results) {
      Set<SearchMatch> matchesForWiki = byWiki.get(match.getWiki());
      if (matchesForWiki==null) {
        byWiki.put(match.getWiki(), matchesForWiki = new LinkedHashSet<SearchMatch>());
      }
      matchesForWiki.add(match);
    }

    Set<SearchMatch> sortedSet = new LinkedHashSet<SearchMatch>();

    /* Find the set for this wiki, and force it to be first */
    Set<SearchMatch> sameWikiMatches = byWiki.get(_wikiName);
    if (sameWikiMatches!=null) {
      sortedSet.addAll(sameWikiMatches);
      byWiki.remove(_wikiName);
    }

    /* Flatten remaining per-wiki sets into single set, maintaining order */
    for (Set<SearchMatch> matches: byWiki.values()) {
      sortedSet.addAll(matches);
    }
    return sortedSet;
  }

  private LinkedHashSet<SearchMatch> query(final IndexReader reader, final Analyzer analyzer, final Searcher searcher, final String field, final String queryString, final boolean provideExtracts) throws IOException, ParseException {
    QueryParser parser = new QueryParser(field, analyzer);
    parser.setLowercaseExpandedTerms(!FIELD_PATH.equals(field));
    parser.setDefaultOperator(Operator.AND);
    Query query = parser.parse(queryString);
    return doQuery(reader, analyzer, searcher, field, provideExtracts, query);
  }

  private LinkedHashSet<SearchMatch> doQuery(final IndexReader reader, final Analyzer analyzer, final Searcher searcher, final String field, final boolean provideExtracts, final Query query) throws IOException, CorruptIndexException {
    Highlighter highlighter = null;
    if (provideExtracts) {
      query.rewrite(reader);
      highlighter = new Highlighter(new SimpleHTMLFormatter("<strong>", "</strong>"), new SimpleHTMLEncoder(), new QueryScorer(query));
    }
    Hits hits = searcher.search(query);
    LinkedHashSet<SearchMatch> results = new LinkedHashSet<SearchMatch>();
    @SuppressWarnings("unchecked") Iterator<Hit> iter = hits.iterator();
    while (iter.hasNext()) {
      Hit hit = iter.next();
      String text = hit.get(field);
      String extract = null;
      // The text is not stored for all fields, just provide a null extract.
      if (highlighter != null && text != null) {
        TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(text));
        // Get 3 best fragments and separate with a "..."
        extract = highlighter.getBestFragments(tokenStream, text, 3, "...");
      }
      results.add(new SearchMatch(_wikiName.equals(hit.get(FIELD_WIKI)), hit.get(FIELD_WIKI), hit.get(FIELD_PATH), extract));
    }
    return results;
  }

  public long getHighestIndexedRevision() throws IOException {
    createIndexIfNecessary();
    String property = getProperty(PROPERTY_LAST_INDEXED_REVISION);
    try {
      if (property != null) {
        return Long.valueOf(property);
      }
    }
    catch (NumberFormatException ex) {
      // Fallthrough to default.
    }
    return 0;
  }

  private String getProperty(final String propertyName) throws IOException {
    if (_dir == null) {
      return null;
    }
    try {
      return doReadOperation(new ReadOperation<String>() {
        public String execute(final IndexReader reader, final Searcher searcher, final Analyzer analyzer) throws IOException, ParseException {
          Hits hits = searcher.search(new TermQuery(new Term(FIELD_PROPERTY_KEY, propertyName)));
          Iterator<?> iterator = hits.iterator();
          if (iterator.hasNext()) {
            return ((Hit) iterator.next()).get(FIELD_PROPERTY_VALUE);
          }
          return null;
        }
      }, false);
    }
    catch (QuerySyntaxException ex) {
      throw new NoQueryPerformedException(ex);
    }
  }

  private void rememberLastIndexedRevision(final long revision) throws CorruptIndexException, LockObtainFailedException, IOException {
    replaceProperty(createPropertyDocument(PROPERTY_LAST_INDEXED_REVISION, String.valueOf(revision)));
  }

  public String escape(final String in) {
    return QueryParser.escape(in);
  }

}
