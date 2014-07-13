package demo.ch1;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class Searcher {
	private static Version VER = Version.LUCENE_4_9;
	
	public static void search(String indexDir, String q) throws IOException, ParseException {  
        // 3) Open index  
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));  
        IndexSearcher is = new IndexSearcher(reader);  
          
        // 4) Parser query  
        QueryParser parser = new QueryParser(VER, "contents",  new StandardAnalyzer(VER));  
        Query query;
        query = parser.parse(q);  
          
        // 5) Search index  
        long start = System.currentTimeMillis();  
        TopDocs hits = is.search(query, 10);  
        long end = System.currentTimeMillis();  
          
        // 6) Write search stat  
        System.err.println("Found " + hits.totalHits + " document(s) (in "  
                + (end - start) + " milliseconds) that matched query '" + q  
                + "':");  
          
        // 7) Retrieve matching docs  
        for (ScoreDoc scoreDoc : hits.scoreDocs) {  
            Document doc = is.doc(scoreDoc.doc);  
            System.out.println(doc.get("fullpath"));  
        }  
          
        // 8) Close IndexSearcher  
        reader.close();
    }  
      
    public static void main(String[] args) throws IllegalArgumentException,  
            IOException, ParseException {  
        if (args.length != 2) {  
            throw new IllegalArgumentException("Usage: java "  
                    + Searcher.class.getName() + " <index dir> <query>");  
        }  
        String indexDir = args[0];  // 1) Parser provided index directory  
        String q = args[1];         // 2) Parser provided query string  
        search(indexDir, q);  
    }  
}
