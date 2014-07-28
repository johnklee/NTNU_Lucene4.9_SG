package demo.ch6;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.payloads.AveragePayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
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
		ft2.setStored(true); ft2.setIndexed(true); 
		ft2.setStoreTermVectors(true); 
		ft2.setStoreTermVectorPositions(true);
		ft2.setStoreTermVectorOffsets(true);
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
		for(ScoreDoc sd:hits.scoreDocs)
		{
			Document doc = r.document(sd.doc);
			System.out.printf("\t[Info] Content:\n%s\n", doc.get("contents"));
			System.out.printf("\t[Info] Iterate Terms in %s\n", doc.get("title"));
			Terms terms = r.getTermVector(sd.doc, "contents");
			TermsEnum termsEnum= terms.iterator(TermsEnum.EMPTY);
			BytesRef term;
			while((term=termsEnum.next())!=null){
				String docTerm = term.utf8ToString();
				DocsAndPositionsEnum docPosEnum = termsEnum.docsAndPositions(null, null, DocsAndPositionsEnum.FLAG_OFFSETS);							
				docPosEnum.nextDoc();
				System.out.printf("\t\tTerm='%s' (%d): \n", docTerm, docPosEnum.freq());
                //Retrieve the term frequency in the current document
                int freq=docPosEnum.freq();
                for(int i=0; i<freq; i++){
                    int position=docPosEnum.nextPosition();
                    int start=docPosEnum.startOffset();
                    int end=docPosEnum.endOffset();
                    //Store start, end and position in a list
                    System.out.printf("\t\t\tPos=%d; Start=%d; End=%d\n", position, start, end);
                }
			}
		}
		r.close();
	 }
}
