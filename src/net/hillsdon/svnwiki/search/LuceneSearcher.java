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
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

/**
 * Uses lucene to provide search capabilities.
 * 
 * @author mth
 */
public class LuceneSearcher implements SearchEngine, SearchIndexer {

  private static final String FIELD_PATH = "path";
  private static final String FIELD_CONTENT = "content";
  /**
   * We tokenize the wiki word to allow e.g. 'another' to find 'AnotherNewPage'.
   */
  private static final String FIELD_TITLE = "title";
  
  private final File _dir;

  /**
   * @param dir The search index lives here.  
   *            If null is passed the search will behave as a null implemenation.
   */
  public LuceneSearcher(final File dir) {
    _dir = dir;
  }
  
  public void  index(final String path, final String content) throws IOException {
    if (_dir == null) {
      return;
    }
    // Lucene allows multiple non-deleting readers and at most one writer
    // at a time.  It maintains a lock file but we never want it to
    // fail to take the lock, so serialize writes.
    synchronized (_dir) {
      IndexWriter writer = new IndexWriter(_dir, createAnalyzer());
      try {
        writer.deleteDocuments(new Term(FIELD_PATH, path));
        Document document = new Document();
        document.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(FIELD_TITLE, pathToTitle(path).toString(), Field.Store.YES, Field.Index.TOKENIZED));
        // We store the content in order to show matching extracts.
        document.add(new Field(FIELD_CONTENT, content, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);
        writer.optimize();
      }
      finally {
        writer.close();
      }
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

  private Analyzer createAnalyzer() {
    Analyzer text = new StandardAnalyzer() {
      public TokenStream tokenStream(final String fieldName, final Reader reader) {
        return new PorterStemFilter(super.tokenStream(fieldName, reader));
      }
    };
    PerFieldAnalyzerWrapper perField = new PerFieldAnalyzerWrapper(text);
    perField.addAnalyzer(FIELD_PATH, new KeywordAnalyzer());
    return perField;
  }
  
}
