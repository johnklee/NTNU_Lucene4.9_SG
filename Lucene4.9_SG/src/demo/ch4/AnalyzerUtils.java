package demo.ch4;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Version;

public class AnalyzerUtils {
	public static void displayTokens(Analyzer analyzer, String text) throws IOException {  
	    displayTokens(analyzer.tokenStream("contents", new StringReader(text)));  
	}  
	  
	// http://www.hankcs.com/program/java/lucene-4-6-1-java-lang-illegalstateexception-tokenstream-contract-violation.html
	public static void displayTokens(TokenStream stream) throws IOException {
		stream.reset();
		CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);  
	    while (stream.incrementToken()) {  
	        System.out.print("[" + String.valueOf(term.buffer()) + "] ");  
	    }  
	    stream.close();
	} 
	
	public static void displayTokensWithFullDetails(Analyzer analyzer, String text) throws IOException  
	{  
	    // Perform analysis  		
	    TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
	    stream.reset();
	    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);  
	       
	    // Obtain attributes in interest  
	    PositionIncrementAttribute posIncr = stream.addAttribute(PositionIncrementAttribute.class);  
	    OffsetAttribute offset = stream.addAttribute(OffsetAttribute.class);  
	    TypeAttribute type = stream.addAttribute(TypeAttribute.class);  
	      
	    int position = 0;  
	    // Iteratively through all tokens  
	    while (stream.incrementToken()) {  
	        int increment = posIncr.getPositionIncrement();  
	        if (increment > 0) {  
	            position = position + increment;  
	            System.out.println();  
	            System.out.print(position + ": ");  
	        }  
	        System.out.print("[" + String.valueOf(term.buffer()) + ":" + offset.startOffset()  
	                + "->" + offset.endOffset() + ":" + type.type() + "] ");  
	    }  
	    System.out.println();  
	}
	
	public static void main(String args[]) throws Exception
	{
		AnalyzerUtils.displayTokensWithFullDetails(new SimpleAnalyzer(Version.LUCENE_4_9), "The quick brown fox....");
	}
}
