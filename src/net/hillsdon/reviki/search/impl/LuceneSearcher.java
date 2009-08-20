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

import static net.hillsdon.fij.core.Functional.map;
import static net.hillsdon.fij.core.Functional.set;
import static net.hillsdon.fij.text.Strings.join;
import static net.hillsdon.reviki.text.WikiWordUtils.pathToTitle;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import net.hillsdon.reviki.search.QuerySyntaxException;
import net.hillsdon.reviki.search.SearchEngine;
import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.RenderedPage;
import net.hillsdon.reviki.wiki.RenderedPageFactory;

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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.LockObtainFailedException;

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

  private static final String FIELD_PATH = "path";
  private static final String FIELD_CONTENT = "content";
  /**
   * We tokenize the wiki word to allow e.g. 'another' to find 'AnotherNewPage'.
   */
  private static final String FIELD_TITLE = "title";
  private static final String FIELD_OUTGOING_LINKS = "outgoing-links";
  
  private static final String FIELD_PROPERTY_KEY = "property";
  private static final String FIELD_PROPERTY_VALUE = "property-value";
  private static final String PROPERTY_LAST_INDEXED_REVISION = "last-indexed-revision";
  
  private final File _dir;
  private final RenderedPageFactory _renderedPageFactory;

  /**
   * @param dir The search index lives here.  
   *            If null is passed the search will behave as a null implemenation.
   */
  public LuceneSearcher(final File dir, final RenderedPageFactory renderedPageFactory) {
    _dir = dir;
    _renderedPageFactory = renderedPageFactory;
  }

  private void createIndexIfNecessary() throws IOException {
    if (_dir != null && !IndexReader.indexExists(_dir)) {
      new IndexWriter(_dir, createAnalyzer(), true).close();
    }
  }

  private Analyzer createAnalyzer() {
    Analyzer text = new StandardAnalyzer() {
      public TokenStream tokenStream(final String fieldName, final Reader reader) {
        return new PorterStemFilter(super.tokenStream(fieldName, reader));
      }
    };
    KeywordAnalyzer id = new KeywordAnalyzer();
    PerFieldAnalyzerWrapper perField = new PerFieldAnalyzerWrapper(text);
    perField.addAnalyzer(FIELD_PATH, id);
    perField.addAnalyzer(FIELD_PROPERTY_KEY, id);
    perField.addAnalyzer(FIELD_PROPERTY_VALUE, id);
    return perField;
  }
  
  private Document createWikiPageDocument(final String path, final String content) throws IOException, PageStoreException {
    RenderedPage renderedPage = _renderedPageFactory.create(path, content, URLOutputFilter.NULL);
    Document document = new Document();
    document.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));
    document.add(new Field(FIELD_TITLE, pathToTitle(path), Field.Store.YES, Field.Index.TOKENIZED));
    document.add(new Field(FIELD_OUTGOING_LINKS, join(renderedPage.findOutgoingWikiLinks().iterator(), " "), Field.Store.YES, Field.Index.TOKENIZED));
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

  private void deleteDocument(final String keyField, final String value) throws IOException {
    IndexWriter writer = new IndexWriter(_dir, createAnalyzer());
    try {
      writer.deleteDocuments(new Term(keyField, value));
    }
    finally {
      writer.close();
    }
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
  public synchronized void index(final String path, final long revision, final String content) throws IOException, PageStoreException {
    if (_dir == null) {
      return;
    }
    createIndexIfNecessary();
    replaceDocument(FIELD_PATH, createWikiPageDocument(path, content));
    rememberLastIndexedRevision(revision);
  }

  // See comment on index.
  public synchronized void delete(final String path, final long revision) throws IOException {
    createIndexIfNecessary();
    deleteDocument(FIELD_PATH, path);
    rememberLastIndexedRevision(revision);
  }
  
  public Set<String> incomingLinks(final String page) throws IOException, PageStoreException {
    if (_dir == null) {
      return Collections.emptySet();
    }
    try {
      return doReadOperation(new ReadOperation<Set<String>>() {
        public Set<String> execute(final IndexReader reader, final Searcher searcher, final Analyzer analyzer) throws IOException, ParseException {
          Set<String> results = set(map(query(reader, createAnalyzer(), searcher, FIELD_OUTGOING_LINKS, escape(page), false), SearchMatch.TO_PAGE_NAME));
          results.remove(page);
          return results;
        }
      });
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
            Set<String> results = set(outgoingLinks.split("\\s"));
            results.remove(page);
            return results;
          }
          return Collections.emptySet();
        }
      });
    }
    catch (QuerySyntaxException ex) {
      throw new NoQueryPerformedException(ex);
    }
  }

  /**
   * Reusable template that cleans up properly.
   * 
   * @param <T> Result type.
   * @param operation Operation to perform.
   * @return Result from operation.
   * @throws IOException On index read error,
   * @throws QuerySyntaxException If we can't parse a query.
   */
  private <T> T doReadOperation(final ReadOperation<T> operation) throws IOException, QuerySyntaxException {
    createIndexIfNecessary();
    IndexReader reader = IndexReader.open(_dir);
    try {
      Searcher searcher = new IndexSearcher(reader);
      try {
        Analyzer analyzer = createAnalyzer();
        return operation.execute(reader, searcher, analyzer);
      }
      catch (ParseException ex) {
        throw new QuerySyntaxException(ex.getMessage(), ex);
      }
      finally {
        searcher.close();
      }
    }
    finally {
      reader.close();
    }
  }
  
  public Set<SearchMatch> search(final String queryString, final boolean provideExtracts) throws IOException, QuerySyntaxException {
    if (_dir == null || queryString == null || queryString.trim().length() == 0) {
      return Collections.emptySet();
    }
    return doReadOperation(new ReadOperation<Set<SearchMatch>>() {
      public Set<SearchMatch> execute(final IndexReader reader, final Searcher searcher, final Analyzer analyzer) throws IOException, ParseException {
        LinkedHashSet<SearchMatch> results = new LinkedHashSet<SearchMatch>();
        // Prefer path, then title then content matches (match equality is on page name)
        for (String field : new String[] {FIELD_PATH, FIELD_TITLE, FIELD_CONTENT}) {
          results.addAll(query(reader, analyzer, searcher, field, queryString, provideExtracts));
        }
        return results;
      }
    });
  }

  @SuppressWarnings("unchecked")
  private LinkedHashSet<SearchMatch> query(final IndexReader reader, final Analyzer analyzer, final Searcher searcher, final String field, final String queryString, final boolean provideExtracts) throws IOException, ParseException {
    QueryParser parser = new QueryParser(field, analyzer);
    parser.setLowercaseExpandedTerms(!FIELD_PATH.equals(field));
    parser.setDefaultOperator(Operator.AND);
    Query query = parser.parse(queryString);
    Highlighter highlighter = null;
    if (provideExtracts) {
      query.rewrite(reader);
      highlighter = new Highlighter(new SimpleHTMLFormatter("<strong>", "</strong>"), new SimpleHTMLEncoder(), new QueryScorer(query));
    }
    Hits hits = searcher.search(query);
    LinkedHashSet<SearchMatch> results = new LinkedHashSet<SearchMatch>();
    Iterator<Hit> iter = hits.iterator();
    while (iter.hasNext()) {
      Hit hit = (Hit) iter.next();
      String text = hit.get(field);
      String extract = null;
      // The text is not stored for all fields, just provide a null extract.
      if (highlighter != null && text != null) {
        TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(text));
        // Get 3 best fragments and seperate with a "..."
        extract = highlighter.getBestFragments(tokenStream, text, 3, "...");
      }
      results.add(new SearchMatch(hit.get(FIELD_PATH), extract));
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
      });
    }
    catch (QuerySyntaxException ex) {
      throw new NoQueryPerformedException(ex);
    }
  }

  private void rememberLastIndexedRevision(final long revision) throws CorruptIndexException, LockObtainFailedException, IOException {
    replaceDocument(FIELD_PROPERTY_KEY, createPropertyDocument(PROPERTY_LAST_INDEXED_REVISION, String.valueOf(revision)));
  }

  public String escape(final String in) {
    return QueryParser.escape(in);
  }
  
}
