package demo.ch2;

import java.io.IOException;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class IndexingTest extends TestCase{
	private Version VER = Version.LUCENE_4_9;
	protected String[] ids = { "1", "2" };  
    protected String[] unindexed = { "Netherlands", "Italy" };  
    protected String[] unstored = { "Amsterdam has lots of bridges",  
            "Venice has lots of canals" };  
    protected String[] text = { "Amsterdam", "Venice" };  
    private Directory directory;  
  
    protected void setUp() throws Exception {  
        // 1) Run before every test  
        directory = new RAMDirectory();  
          
        // 2) Cretae IndexWriter  
        IndexWriter writer = getWriter();          
          
        // 3) Add document  
        FieldType ft1 = new FieldType();
        ft1.setStored(true); ft1.setTokenized(false); ft1.setIndexed(true);
        FieldType ft2 = new FieldType();
        ft2.setStored(true); ft2.setIndexed(false); 
        for (int i = 0; i < ids.length; i++) {  
            Document doc = new Document();              
            doc.add(new Field("id", ids[i], ft1));  
            doc.add(new Field("country", unindexed[i], ft2));  
            doc.add(new TextField("contents", unstored[i], Field.Store.NO));  
            doc.add(new TextField("city", text[i], Field.Store.YES)); 
            doc.add(new StringField("dateStr", DateTools.dateToString(new Date(), DateTools.Resolution.YEAR), Field.Store.YES));
            writer.addDocument(doc);  
        }  
        writer.close();  
    }  
  
    private IndexWriter getWriter() throws IOException {  
        // 2) Create IndexWriter  
    	IndexWriterConfig iwConfig = new IndexWriterConfig(VER, new WhitespaceAnalyzer(VER));
    	//iwConfig.setInfoStream(System.out);
        return new IndexWriter(directory, iwConfig);  
    }  
  
    protected int getHitCount(String fieldName, String searchString)  
            throws IOException {  
        // 4) Create new searcher 
    	IndexReader reader = DirectoryReader.open(directory);  
        IndexSearcher searcher = new IndexSearcher(reader);         
          
        // 5) Build single-term query.  
        Term t = new Term(fieldName, searchString);  
        Query query = new TermQuery(t);  
          
        // 6) Get number of hit.  
        int hitCount = searcher.search(query, 10).totalHits;  
        reader.close();  
        return hitCount;  
    }  
  
    public void testIndexWriter() throws IOException {  
        // 7) Verify writer document count.  
        IndexWriter writer = getWriter();  
        assertEquals(ids.length, writer.numDocs());  
        writer.close();  
    }  
  
    public void testIndexReader() throws IOException {  
        // 8) Verify reader document count.       
        IndexReader reader = DirectoryReader.open(directory);
        assertEquals(ids.length, reader.maxDoc());  
        assertEquals(ids.length, reader.numDocs());  
        reader.close();  
    }  
    
    public void testDeleteBeforeOptimize() throws IOException {       
        IndexWriter writer = getWriter();  
        assertEquals(2, writer.numDocs()); // Verify 2 doc in index.  
        writer.deleteDocuments(new Term("id", "1")); // Delete the first doc  
        writer.commit();  
        // 1) Verify index has deletion.  
        assertTrue(writer.hasDeletions());  
          
        // 2) Verify the one document being deleted.  
        assertEquals(2, writer.maxDoc()); // Deleted document still in memory. Not flush yet.  
        assertEquals(1, writer.numDocs());  
        writer.close();  
    }  
    
    public void testUpdate() throws IOException {  
        assertEquals(1, getHitCount("city", "Amsterdam"));  
        IndexWriter writer = getWriter();  
        FieldType ft1 = new FieldType();
        ft1.setStored(true); ft1.setTokenized(false); ft1.setIndexed(true);
        FieldType ft2 = new FieldType();
        ft2.setStored(true); ft2.setIndexed(false); 
        Document doc = new Document();  
        doc.add(new Field("id", "1", ft1));  
        doc.add(new Field("country", "Netherlands", ft2));  
        doc.add(new TextField("contents", "Den Haag has a lot of museums", Field.Store.NO));  
        doc.add(new TextField("city", "ABC", Field.Store.YES));          
        writer.updateDocument(new Term("id", "1"), doc);  
        writer.commit();          
        writer.close();       
        assertEquals(0, getHitCount("city", "Amsterdam"));  
        assertEquals(1, getHitCount("city", "ABC"));                
    }  
    
    public void testAnyway() throws IOException {
    	IndexReader reader = DirectoryReader.open(directory);  
        IndexSearcher searcher = new IndexSearcher(reader);
        for(int i=0; i<reader.maxDoc(); i++)
        {
        	Document doc = searcher.doc(i);
        	System.out.printf("\t[Info] country=%s\n", doc.get("country"));
        	System.out.printf("\t[Info] dateStr=%s\n", doc.get("dateStr"));
        }
        
        Directory ramDir = new RAMDirectory(directory, new IOContext());
        IndexWriterConfig iwConfig = new IndexWriterConfig(VER, new WhitespaceAnalyzer(VER));
        IndexWriter writer = new IndexWriter(ramDir, iwConfig);
    }
    
      
    /*public void testDeleteAfterOptimize() throws IOException {  
        System.out.printf("\t[Test] testDeleteAfterOptimize()...\n");  
        IndexWriter writer = getWriter();  
        assertEquals(2, writer.numDocs());  
        writer.deleteDocuments(new Term("id", "1"));  
          
        // 3) Optimize to compact deletion.  
        writer.optimize();    
        writer.commit();  
        assertFalse(writer.hasDeletions());  
        assertEquals(1, writer.maxDoc());  // Deleted document already being flushed.x  
        assertEquals(1, writer.numDocs());  
        writer.close();  
    }  */
}
