package demo.ch3;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class WildcardQueryCase extends TestCase{
	public static Version LUCENE_VERSION = Version.LUCENE_4_9;  
    public File idxRoot = new File("./test");  
    private Directory dir;  
    private IndexSearcher searcher;  
    private IndexWriter writer;  
      
    protected void setUp() throws IOException {  
        dir = FSDirectory.open(idxRoot);  
        Analyzer alyz = new WhitespaceAnalyzer(LUCENE_VERSION);  
        IndexWriterConfig iwConfig = new IndexWriterConfig(LUCENE_VERSION, alyz);  
        writer = new IndexWriter(dir, iwConfig);  
        writer.deleteAll();               
    }  
      
    private void indexSingleFieldDocs(Field[] fields) throws Exception {  
        for (Field f : fields) {  
            Document doc = new Document();  
            doc.add(f);  
            writer.addDocument(doc);  
        }         
        IndexReader reader = DirectoryReader.open(writer, true);  
        searcher = new IndexSearcher(reader);  
    }  
      
    protected void tearDown() throws IOException {  
        writer.close();          
        dir.close();          
    }  
    
    public void testWildcard() throws Exception {  
        indexSingleFieldDocs(new Field[] {  
                new TextField("contents", "wild", Field.Store.YES),  
                new TextField("contents", "child", Field.Store.YES),  
                new TextField("contents", "mild", Field.Store.YES),  
                new TextField("contents", "mildew", Field.Store.YES) });  
        Query query = new WildcardQuery(new Term("contents", "?ild*"));  
        TopDocs matches = searcher.search(query, 10);  
        assertEquals("child no match", 3, matches.totalHits);  
        assertEquals("score the same", matches.scoreDocs[0].score,  
                matches.scoreDocs[1].score, 0.0);  
        assertEquals("score the same", matches.scoreDocs[1].score,  
                matches.scoreDocs[2].score, 0.0);  
    }
}
