package demo.ch3;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Explainer {
	public static Version LUCENE_VERSION = Version.LUCENE_4_9;
	
	public static void main(String[] args) throws Exception {
		String indexDir = "./test";
		String queryExpression = "Venice canals";
		Directory directory = FSDirectory.open(new File(indexDir));

		QueryParser parser = new QueryParser(LUCENE_VERSION, "contents", new StandardAnalyzer(LUCENE_VERSION));
		Query query = parser.parse(queryExpression);
		System.out.println("Query: " + queryExpression);
		IndexReader idxReader = DirectoryReader.open(directory);  
		IndexSearcher searcher = new IndexSearcher(idxReader);
		TopDocs topDocs = searcher.search(query, 10);
		for (ScoreDoc match : topDocs.scoreDocs) {
			Explanation explanation = searcher.explain(query, match.doc);
			System.out.println("----------");
			Document doc = searcher.doc(match.doc);
			System.out.println(doc.get("title"));
			System.out.println(explanation.toString());
		}
		idxReader.close();
		directory.close();
	}
}
