package demo.ch3;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class ListExams extends TestCase{  
    public static Version LUCENE_VERSION = Version.LUCENE_4_9;  
    public Directory directory = null;  
    public IndexSearcher searcher = null;  
    public File indexpath = new File("./test");  
    protected String[] ids = { "1", "2" };  
    protected String[] unindexed = { "Ant in Action", "Junit in Action" };  
    protected String[] unstored = { "Amsterdam has lots of bridges",  
            "Venice has lots of canals" };  
    protected String[] subject = { "Ant in Action with Junit", "JUnit in Action, Second Edition" };  
      
    @Override  
    protected void tearDown() throws Exception  
    {                   
        directory.close();  
        searcher = null;              
    }  
      
    @Override  
    protected void setUp() throws Exception {          
        directory = FSDirectory.open(indexpath);  
        buildIndex();                 
    }  
      
    protected void buildIndex() throws Exception  
    {  
        // 2) Cretae IndexWriter  
        IndexWriter writer = getWriter();  
  
        // 3) Add document  
        FieldType ft1 = new FieldType(); ft1.setIndexed(true); ft1.setStored(true);
        FieldType ft2 = new FieldType(); ft2.setIndexed(true); ft2.setStored(true); ft2.setTokenized(false);        
        for (int i = 0; i < ids.length; i++) {  
            Document doc = new Document();  
            doc.add(new Field("id", ids[i], ft2));  
            doc.add(new Field("title", unindexed[i], ft2));  
            doc.add(new TextField("contents", unstored[i], Field.Store.YES));  
            doc.add(new Field("subject", subject[i], ft1));  
            writer.addDocument(doc);  
        }  
        writer.commit();  
        writer.close();  
    }  
      
    /** 
     * BD: The StandardAnalyzer applies a LowerCaseFilter that would make search insensitive. 
     * Reference: 
     *      - How to make lucene be case-insensitive 
     *        http://stackoverflow.com/questions/5512803/how-to-make-lucene-be-case-insensitive 
     * @return 
     * @throws IOException 
     */  
    private IndexWriter getWriter() throws IOException {  
        Analyzer alyz = new StandardAnalyzer(LUCENE_VERSION);         
        IndexWriterConfig iwConfig = new IndexWriterConfig(LUCENE_VERSION, alyz);  
        iwConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);  
        return new IndexWriter(directory, iwConfig);              
    }  
      
    private IndexSearcher getSearcher() throws IOException  
    {  
        if(searcher==null)  
        {  
            IndexReader idxReader = DirectoryReader.open(directory);  
            searcher = new IndexSearcher(idxReader);  
        }  
        return searcher;  
    }  
      
    /** 
     * BD: List 3.1 
     * @throws Exception 
     */  
    public void testTerm() throws Exception {                 
        // 1) Create IndexSearcher -> directory is built during setUp()  
        IndexSearcher searcher = getSearcher();  
          
        // 2) Build Single Term Query  
        Term t = new Term("subject", "ant");  
        Query query = new TermQuery(t);  
          
        // 3) Search  
        TopDocs docs = searcher.search(query, 10);  
          
        // 4) Confirm one hit for 'ant' query.  
        assertEquals("Ant in Action", 1, docs.totalHits);  
          
        // 5) Search again  
        t = new Term("subject", "junit");  
        docs = searcher.search(new TermQuery(t), 10);  
          
        // 6) Confirm two hit for 'junit' query.  
        assertEquals("Ant in Action, " + "JUnit in Action, Second Edition",  
                2, docs.totalHits);  
          
        // 7) Close searcher and directory.       
    }  
      
    /** 
     * BD: List 3.2 - QueryParser, which makes it trivial to translate search text into a Query 
     * @throws Exception 
     */  
    public void testQueryParser() throws Exception {  
        // 1) Create IndexSearcher -> directory is built during setUp()  
        IndexSearcher searcher = getSearcher();  
  
        // 2) Create QueryParser  
        QueryParser parser = new QueryParser(LUCENE_VERSION, "subject",  
                new SimpleAnalyzer(LUCENE_VERSION));  
          
        // 3) Query subject to have "JUNIT", "ANT" but without "MOCK";  
        Query query = parser.parse("+JUNIT +ANT -MOCK");  
        TopDocs docs = searcher.search(query, 10);  
          
        // 4) Assert to have 1 hit.  
        assertEquals(1, docs.totalHits);  
          
        // 5) Fetch the top1 document from search result.  
        Document d = searcher.doc(docs.scoreDocs[0].doc);  
          
        // 6) Assert its title to be "Ant in Action".  
        assertEquals("Ant in Action", d.get("title"));  
          
        // 7) Query again to have "mock" or "junit"  
        query = parser.parse("mock OR junit");  
        docs = searcher.search(query, 10);  
          
        // 8) Assert to have 2 hit.  
        assertEquals("Ant in Action, " + "JUnit in Action, Second Edition", 2,  
                docs.totalHits);  
          
        // 9) Close searcher and directory in tearDown()  
    }  
}  
