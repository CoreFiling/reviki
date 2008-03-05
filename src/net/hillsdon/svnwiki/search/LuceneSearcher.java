package net.hillsdon.svnwiki.search;

import static net.hillsdon.svnwiki.text.WikiWordUtils.pathToTitle;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
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
      IndexWriter writer = new IndexWriter(_dir, new StandardAnalyzer());
      try {
        writer.deleteDocuments(new Term(FIELD_PATH, path));
        Document document = new Document();
        document.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(FIELD_TITLE, pathToTitle(path).toString(), Field.Store.YES, Field.Index.TOKENIZED));
        // We store the content in order to show matching extracts.
        document.add(new Field(FIELD_CONTENT, path + "\n" + content, Field.Store.YES, Field.Index.TOKENIZED));
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
        Analyzer analyzer = new StandardAnalyzer();
        LinkedHashSet<SearchMatch> results = query(reader, analyzer, searcher, new QueryParser(FIELD_TITLE, analyzer), queryString);
        results.addAll(query(reader, analyzer, searcher, new QueryParser(FIELD_CONTENT, analyzer), queryString));
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
  private LinkedHashSet<SearchMatch> query(final IndexReader reader, final Analyzer analyzer, final Searcher searcher, final QueryParser queryParser, final String queryString) throws IOException, QuerySyntaxException {
    Query query;
    try {
      query = queryParser.parse(queryString);
      query.rewrite(reader);
    }
    catch (ParseException e) {
      throw new QuerySyntaxException(e.getMessage(), e);
    }
    Hits hits = searcher.search(query);
    Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(), new SimpleHTMLEncoder(), new QueryScorer(query));
    
    LinkedHashSet<SearchMatch> results = new LinkedHashSet<SearchMatch>();
    Iterator<Hit> iter = hits.iterator();
    while (iter.hasNext()) {
      Hit hit = (Hit) iter.next();
      String text = hit.get(queryParser.getField());
      String extract = null;
      if (text != null) {
        TokenStream tokenStream = analyzer.tokenStream(queryParser.getField(), new StringReader(text));
        // Get 3 best fragments and seperate with a "..."
        extract = highlighter.getBestFragments(tokenStream, text, 3, "...");
      }
      results.add(new SearchMatch(hit.get(FIELD_PATH), extract));
    }
    return results;
  }

}
