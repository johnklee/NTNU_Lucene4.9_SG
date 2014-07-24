package demo.ch6;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.payloads.AveragePayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import utils.TestUtil;

public class PayloadsTest extends TestCase {
	Directory 					dir;
	IndexWriter 				writer;
	BulletinPayloadsAnalyzer 	analyzer;
	private Version 			VER = Version.LUCENE_4_9;
	private FieldType 			ft1,ft2;

	protected void setUp() throws Exception {
		super.setUp();
		dir = new RAMDirectory();
		analyzer = new BulletinPayloadsAnalyzer(5.0F);
		IndexWriterConfig iwConfig = new IndexWriterConfig(VER, analyzer);		
		writer = new IndexWriter(dir, iwConfig);		
		ft1 = new FieldType();
		ft1.setStored(true); ft1.setIndexed(false);
		ft2 = new FieldType();
		ft2.setStored(false); ft2.setIndexed(true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		writer.close();
	}
	
	void addDoc(String title, String contents) throws IOException {
		System.out.printf("\t[Test] ******* Adding doc='%s' ********\n", title);
		Document doc = new Document();
		doc.add(new Field("title", title, ft1));
		doc.add(new Field("contents", contents, ft2));		
		if(contents.startsWith("Bulletin:"))
		{
			System.out.printf("\t[Test] '%s' is bulletin!\n", title);
			analyzer.setIsBulletin(true);
		}
		else analyzer.setIsBulletin(false);
		writer.addDocument(doc);
	}
	
	 public void testPayloadTermQuery() throws Throwable {
		 addDoc("Hurricane warning",
		        "Bulletin: A hurricane warning was issued " +
		        "at 6 AM for the outer great banks");
		 addDoc("Warning label maker",
		        "The warning label maker is a delightful toy for " +
		        "your precocious seven year old's warning needs");
		 addDoc("Tornado warning",
		        "Bulletin: There is a tornado warning for " +
		        "Worcester county until 6 PM today");
		IndexReader r = DirectoryReader.open(writer, true);
		writer.close();
		IndexSearcher searcher = new IndexSearcher(r);
		searcher.setSimilarity(new BoostingSimilarity());
		Term warning = new Term("contents", "warning");
		Query query1 = new TermQuery(warning);
		System.out.println("\nTermQuery results:");
		TopDocs hits = searcher.search(query1, 10);
		TestUtil.dumpHits(searcher, hits);		
		assertEquals("Warning label maker",
                searcher.doc(hits.scoreDocs[0].doc).get("title"));
		
		Query query2 = new PayloadTermQuery(warning, new AveragePayloadFunction());
		System.out.println("\nPayloadTermQuery results:");
		hits = searcher.search(query2, 10);
		TestUtil.dumpHits(searcher, hits);
		assertEquals("Warning label maker", searcher.doc(hits.scoreDocs[2].doc).get("title"));
		r.close();
	 }
}
