package net.hillsdon.svnwiki.search;

import static net.hillsdon.svnwiki.text.WikiWordUtils.pathToTitle;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

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

  private static final String FIELD_PATH = "path";
  private static final String FIELD_CONTENT = "content";
  /**
   * We tokenize the wiki word to allow e.g. 'another' to find 'AnotherNewPage'.
   */
  private static final String FIELD_TITLE = "title";
  
  private static final String FIELD_PROPERTY_KEY = "property";
  private static final String FIELD_PROPERTY_VALUE = "property-value";
  private static final String PROPERTY_LAST_INDEXED_REVISION = "last-indexed-revision";
  
  private final File _dir;

  /**
   * @param dir The search index lives here.  
   *            If null is passed the search will behave as a null implemenation.
   * @throws IOException If we fail to create the index.
   */
  public LuceneSearcher(final File dir) throws IOException {
    _dir = dir;
    if (dir != null && !IndexReader.indexExists(dir)) {
      new IndexWriter(dir, createAnalyzer(), true).close();
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
  
  private Document createWikiPageDocument(final String path, final String content) {
    Document document = new Document();
    document.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));
    document.add(new Field(FIELD_TITLE, pathToTitle(path).toString(), Field.Store.YES, Field.Index.TOKENIZED));
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
  public synchronized void index(final String path, final long revision, final String content) throws IOException {
    if (_dir == null) {
      return;
    }
    replaceDocument(FIELD_PATH, createWikiPageDocument(path, content));
    if (revision > getHighestIndexedRevision()) {
      replaceDocument(FIELD_PROPERTY_KEY, createPropertyDocument(PROPERTY_LAST_INDEXED_REVISION, String.valueOf(revision)));
    }
  }
  
  public Set<SearchMatch> search(final String queryString) throws IOException, QuerySyntaxException {
    if (_dir == null || queryString == null || queryString.trim().length() == 0) {
      return Collections.emptySet();
    }
    IndexReader reader = IndexReader.open(_dir);
    try {
      Searcher searcher = new IndexSearcher(reader);
      try {
        Analyzer analyzer = createAnalyzer();
        LinkedHashSet<SearchMatch> results = new LinkedHashSet<SearchMatch>();
        // Prefer path, then title then content matches (match equality is on page name)
        for (String field : new String[] {FIELD_PATH, FIELD_TITLE, FIELD_CONTENT}) {
          results.addAll(query(reader, analyzer, searcher, field, queryString));
        }
        return results;
      }
      finally {
        searcher.close();
      }
    }
    finally {
      reader.close();
    }
  }

  @SuppressWarnings("unchecked")
  private LinkedHashSet<SearchMatch> query(final IndexReader reader, final Analyzer analyzer, final Searcher searcher, final String field, final String queryString) throws IOException, QuerySyntaxException {
    QueryParser parser = new QueryParser(field, analyzer);
    parser.setDefaultOperator(Operator.AND);
    Query query;
    try {
      query = parser.parse(queryString);
    }
    catch (ParseException e) {
      throw new QuerySyntaxException(e.getMessage(), e);
    }
    query.rewrite(reader);
    Hits hits = searcher.search(query);
    Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(), new SimpleHTMLEncoder(), new QueryScorer(query));
    
    LinkedHashSet<SearchMatch> results = new LinkedHashSet<SearchMatch>();
    Iterator<Hit> iter = hits.iterator();
    while (iter.hasNext()) {
      Hit hit = (Hit) iter.next();
      String text = hit.get(field);
      String extract = null;
      // The text is not stored for all fields, just provide a null extract.
      if (text != null) {
        TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(text));
        // Get 3 best fragments and seperate with a "..."
        extract = highlighter.getBestFragments(tokenStream, text, 3, "...");
      }
      results.add(new SearchMatch(hit.get(FIELD_PATH), extract));
    }
    return results;
  }

  public long getHighestIndexedRevision() throws IOException {
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
    IndexReader reader = IndexReader.open(_dir);
    try {
      Searcher searcher = new IndexSearcher(reader);
      try {
        Hits hits = searcher.search(new TermQuery(new Term(FIELD_PROPERTY_KEY, propertyName)));
        Iterator<?> iterator = hits.iterator();
        if (iterator.hasNext()) {
          return ((Hit) iterator.next()).get(FIELD_PROPERTY_VALUE);
        }
        return null;
      }
      finally {
        searcher.close();
      }
    }
    finally {
      reader.close();
    }
  }
  
}
