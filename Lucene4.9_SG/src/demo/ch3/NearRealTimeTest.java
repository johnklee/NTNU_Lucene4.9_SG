package demo.ch3;

import java.io.File;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class NearRealTimeTest extends TestCase{
	public File indexpath = new File("./test");  
    public static Version LUCENE_VERSION = Version.LUCENE_4_9;  
      
    public void testNearRealTime() throws Exception {  
        Directory dir = FSDirectory.open(indexpath);  
          
        Analyzer alyz = new StandardAnalyzer(LUCENE_VERSION);         
        IndexWriterConfig iwConfig = new IndexWriterConfig(LUCENE_VERSION, alyz);  
        iwConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);  
        IndexWriter writer = new IndexWriter(dir, iwConfig);          
        
        FieldType ft1 = new FieldType();        
        ft1.setIndexed(true); ft1.setStored(true); ft1.setTokenized(false);        
        for (int i = 0; i < 10; i++) {  
            Document doc = new Document();  
            doc.add(new Field("id", "" + i, ft1));  
            doc.add(new TextField("text", "aaa", Field.Store.YES));  
            writer.addDocument(doc);  
        }  
        // 1) Create near-real-time reader  
        IndexReader  reader = DirectoryReader.open(writer, true);  
          
        // 2) Wrapper reader into searcher  
        IndexSearcher searcher = new IndexSearcher(reader);  
        Query query = new TermQuery(new Term("text", "aaa"));  
        TopDocs docs = searcher.search(query, 1);  
          
        // 3) Searcher return 10 hits.  
        assertEquals(10, docs.totalHits);  
          
        // 4) Delete one document  
        writer.deleteDocuments(new Term("id", "7"));  
          
        // 5) Add one document  
        Document doc = new Document();  
        doc.add(new Field("id", "11", ft1));  
        doc.add(new TextField("text", "bbb", Field.Store.YES));  
        writer.addDocument(doc);  
        writer.commit();
          
        // 6) Reopen reader         
        IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader)reader);
        
        // 7) Confirm reader is new  
        assertTrue(newReader!=null);         
          
        // 8) Close old reader  
        reader.close();          
          
        // 9) Create new searcher and search again.  
        searcher = new IndexSearcher(newReader);  
        TopDocs hits = searcher.search(query, 10);  
        for(ScoreDoc sd:hits.scoreDocs)
        {
        	doc = newReader.document(sd.doc);
        	System.out.printf("\t[Test] Hit(%s): %s\n", doc.get("id"),doc.get("text"));
        }
        
        // 10) Confirm only 9 hits  
        assertEquals(9, hits.totalHits);  
        query = new TermQuery(new Term("text", "bbb"));  
          
        // 11) Confirm new added terms is searchable.  
        hits = searcher.search(query, 1);  
        assertEquals(1, hits.totalHits);  
          
        // 12) Close all resources.  
        newReader.close();  
        writer.close();  
    } 
}
