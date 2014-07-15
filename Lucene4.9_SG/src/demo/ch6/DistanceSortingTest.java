package demo.ch6;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class DistanceSortingTest extends TestCase {
	private RAMDirectory directory;
	private IndexSearcher searcher;
	private Query query;
	private Version VER = Version.LUCENE_4_9;
	private FieldType ntFieldType;

	@Override
	protected void setUp() throws Exception {
		directory = new RAMDirectory();
		ntFieldType = new FieldType();
		ntFieldType.setIndexed(true);
		ntFieldType.setStored(true);
		IndexWriterConfig iwConfig = new IndexWriterConfig(VER, new StandardAnalyzer(VER));
		IndexWriter writer = new IndexWriter(directory, iwConfig);
		addPoint(writer, "El Charro", "restaurant", 1, 2);
		addPoint(writer, "Cafe Poca Cosa", "restaurant", 5, 9);
		addPoint(writer, "Los Betos", "restaurant", 9, 6);
		addPoint(writer, "Nico's Taco Shop", "restaurant", 3, 8);
		writer.close();
		IndexReader reader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(reader);
		query = new TermQuery(new Term("type", "restaurant"));			
	}

	private void addPoint(IndexWriter writer, String name, String type, int x, int y) throws IOException {
		Document doc = new Document();
		//System.out.printf("%s\n", ntFieldType);
		doc.add(new Field("name", name, ntFieldType));
		doc.add(new Field("type", type, ntFieldType));
		doc.add(new IntField("x", x, Field.Store.YES));
		doc.add(new IntField("y", y, Field.Store.YES));
		doc.add(new Field("location", x + "," + y, ntFieldType));
		writer.addDocument(doc);
	}
	
	public void testNearestRestaurantToHome() throws Exception {
		  Sort sort = new Sort(new SortField("location", new DistanceComparatorSource(0, 0)));
		  TopDocs hits = searcher.search(query, null, 10, sort);
		  //TopDocs hits = searcher.search(query, 10);
		  System.out.printf("\t[Test] Hit %d...\n", hits.totalHits);
		  assertTrue(hits.totalHits>0);
		  assertEquals("closest",
		               "El Charro",
		               searcher.doc(hits.scoreDocs[0].doc).get("name"));
		  assertEquals("furthest",
		               "Los Betos",
		               searcher.doc(hits.scoreDocs[3].doc).get("name"));
	}
	
	public void testNeareastRestaurantToWork() throws Exception {
		Sort sort = new Sort(new SortField("location", new DistanceComparatorSource(10, 10)));
		TopFieldDocs docs = searcher.search(query, null, 3, sort); 	// 1)
		assertEquals(4, docs.totalHits);							// 2)
		assertEquals(3, docs.scoreDocs.length);						// 3)
		FieldDoc fieldDoc = (FieldDoc) docs.scoreDocs[0];			// 4)
			
		assertEquals("(10,10) -> (9,6) = sqrt(17)", 
				     new Float(Math.sqrt(17)), 
				     fieldDoc.fields[0]);							// 5) 
		Document document = searcher.doc(fieldDoc.doc);				// 6)
		assertEquals("Los Betos", document.get("name"));
	}
}
