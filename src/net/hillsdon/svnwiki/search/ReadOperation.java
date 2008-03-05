package net.hillsdon.svnwiki.search;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Searcher;

/**
 * A read operation that gets given all the stuff it might need.
 * 
 * No cleanup of the passed in objects need be performed.
 * 
 * @author mth
 *
 * @param <T>
 */
public interface ReadOperation<T> {

  T execute(IndexReader reader, Searcher searcher, Analyzer analyzer) throws IOException, ParseException;
  
}
