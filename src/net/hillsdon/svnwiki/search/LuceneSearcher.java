package net.hillsdon.svnwiki.search;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;


public class LuceneSearcher implements SearchEngine, SearchIndexer {

  private static final String FIELD_PATH = "path";
  private static final String FIELD_CONTENT = "content";
  
  private final File _dir;

  public LuceneSearcher(final File dir) {
    _dir = dir;
  }
  
  public void index(final String path, final String content) throws IOException {
    if (_dir == null) {
      return;
    }
    IndexWriter writer = new IndexWriter(_dir, new StandardAnalyzer());
    try {
      Document document = new Document();
      document.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));
      document.add(new Field(FIELD_CONTENT, new StringReader(content)));
      writer.addDocument(document);
      writer.optimize();
    }
    finally {
      writer.close();
    }
  }

  @SuppressWarnings("unchecked")
  public Set<String> search(final String queryString) throws IOException, QuerySyntaxException {
    if (_dir == null) {
      return Collections.emptySet();
    }
    IndexReader reader = IndexReader.open(_dir);
    Searcher searcher = new IndexSearcher(reader);
    Analyzer analyzer = new StandardAnalyzer();
    QueryParser queryParser = new QueryParser(FIELD_CONTENT, analyzer);
    Query query;
    try {
      query = queryParser.parse(queryString);
    }
    catch (ParseException e) {
      throw new QuerySyntaxException(e.getMessage(), e);
    }
    Hits hits = searcher.search(query);
    LinkedHashSet<String> results = new LinkedHashSet<String>();
    Iterator<Hit> iter = hits.iterator();
    while (iter.hasNext()) {
      Hit hit = (Hit) iter.next();
      Document document = hit.getDocument();
      results.add(document.get(FIELD_PATH));
    }
    return results;
  }

}
