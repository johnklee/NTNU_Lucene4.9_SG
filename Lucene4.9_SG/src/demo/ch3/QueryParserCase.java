package demo.ch3;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;

public class QueryParserCase extends TestCase{
	public static Version LUCENE_VERSION = Version.LUCENE_4_9;  
	
	public void testTermQuery() throws Exception {  
	    Analyzer alyz = new WhitespaceAnalyzer(LUCENE_VERSION);  
	    QueryParser parser = new QueryParser(LUCENE_VERSION, "subject", alyz);  
	    Query query = parser.parse("computers");  
	    System.out.println("term: " + query);  
	}
	
	public void testLowercasing() throws Exception   
	{  
		Analyzer alyz = new WhitespaceAnalyzer(LUCENE_VERSION);
	    Query q = new QueryParser(LUCENE_VERSION, "field", alyz).parse("PrefixQuery*");  
	    assertEquals("lowercased", "prefixquery*", q.toString("field"));  
	    QueryParser qp = new QueryParser(LUCENE_VERSION, "field", alyz);  
	    qp.setLowercaseExpandedTerms(false);  
	    q = qp.parse("PrefixQuery*");  
	    //System.out.printf("\t[Test] %s\n", q.toString("field"));  
	    assertEquals("not lowercased", "PrefixQuery*", q.toString("field"));  
	} 
	
	public void testPhraseQuery() throws Exception  
	{  
		Analyzer alyz = new WhitespaceAnalyzer(LUCENE_VERSION);
	     Query q = new QueryParser(LUCENE_VERSION, "field",new StandardAnalyzer(LUCENE_VERSION))  
	                               .parse("\"This is Some Phrase*\"");  
	     assertEquals("analyzed", "\"? ? some phrase\"", q.toString("field"));  
	     q = new QueryParser(LUCENE_VERSION, "field", alyz).parse("\"term\"");  
	     assertTrue("reduced to TermQuery", q instanceof TermQuery);  
	}
	
	public void testSlop() throws Exception  
	{  
		Analyzer alyz = new WhitespaceAnalyzer(LUCENE_VERSION);
	    Query q = new QueryParser(LUCENE_VERSION, "field", alyz).parse("\"exact phrase\"");  
	    assertEquals("zero slop", "\"exact phrase\"", q.toString("field"));  
	    QueryParser qp = new QueryParser(LUCENE_VERSION, "field", alyz);  
	    qp.setPhraseSlop(5);  
	    q = qp.parse("\"sloppy phrase\"");  
	    assertEquals("sloppy, implicitly", "\"sloppy phrase\"~5",q.toString("field"));  
	} 
	
	public void testFuzzyQuery() throws Exception {  
		Analyzer alyz = new WhitespaceAnalyzer(LUCENE_VERSION);
	    QueryParser parser = new QueryParser(LUCENE_VERSION, "subject", alyz);  
	    Query query = parser.parse("kountry~");  
	    System.out.println("fuzzy1: " + query);  	    
	    query = parser.parse("kountry~1");  
	    System.out.println("fuzzy2: " + query);
	    FuzzyQuery fzyQuery = new FuzzyQuery(new Term("kountry"), 0);
	    System.out.printf("fuzzy3: %s\n", fzyQuery);
	}
}
