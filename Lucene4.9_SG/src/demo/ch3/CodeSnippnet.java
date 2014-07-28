package demo.ch3;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class CodeSnippnet {
	
	
	public static void main(String args[])
	{
		BooleanQuery query = new BooleanQuery();  
		query.add(new FuzzyQuery(new Term("field", "kountry")),  
		           BooleanClause.Occur.MUST);  
		query.add(new TermQuery(new Term("title", "western")),  
		           BooleanClause.Occur.SHOULD);  
		System.out.printf("\t[Test] %s\n", query.toString("title"));  
		System.out.printf("\t[Test] %s\n", query.toString("field"));  
		System.out.printf("\t[Test] %s\n", query.toString());  
	}
}
