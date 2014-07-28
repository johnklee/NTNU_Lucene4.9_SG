package demo.ch3;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import utils.TestUtil;
import flib.util.io.QSReader;

public class SearchTestCase extends TestCase{
	private RAMDirectory directory;	
	private Version VER = Version.LUCENE_4_9;
	private FieldType ntFieldType;
	private FieldType nt2FieldType;
	private FieldType ft3;
	private File books = new File("books");
	private Analyzer analyzer;

	@Override
	protected void setUp() throws Exception {
		directory = new RAMDirectory();
		ntFieldType = new FieldType();
		ntFieldType.setIndexed(true);
		//ntFieldType.setTokenized(false);
		//ntFieldType.setOmitNorms(true);
		ntFieldType.setStored(true);
		nt2FieldType = new FieldType();
		nt2FieldType.setStored(true);
		nt2FieldType.setTokenized(false);
		ft3 = new FieldType();
		ft3.setIndexed(true); ft3.setStored(true); ft3.setTokenized(false); ft3.setOmitNorms(true); 
		analyzer = new StandardAnalyzer(VER);
		IndexWriterConfig iwConfig = new IndexWriterConfig(VER, analyzer);
		IndexWriter writer = new IndexWriter(directory, iwConfig);
		addBooks(writer);
		writer.close();				
	}

	@Override
	protected void tearDown() throws Exception {
		directory.close();				
	}

	private void addBooks(IndexWriter writer) throws IOException {
		for (File book : books.listFiles()) {
			String link = "";
			String cnt = "";
			String isbn = "";
			String cost = "";
			String date = "";
			String cate = "";
			String subj = "";
			String title = book.getName().replaceAll(".txt", "");
			if (book.isFile()) {
				QSReader qsr = new QSReader(book);
				qsr.open();
				qsr.hasNext();
				subj = qsr.next();
				qsr.hasNext();
				link = qsr.next();
				qsr.hasNext();
				cost = qsr.next();
				qsr.hasNext();
				date = qsr.next();
				qsr.hasNext();
				isbn = qsr.next();
				qsr.hasNext();
				cate = qsr.next();
				qsr.hasNext();
				cnt = qsr.next();
				qsr.close();
			}
			System.out.printf("\t[Info] Add Book=%s:%s\n", title, link);
			Document doc = new Document();
			doc.add(new Field("subject", subj, ntFieldType));
			doc.add(new Field("subject2", subj, ft3));
			doc.add(new Field("url", link, ntFieldType));
			doc.add(new Field("isbn", isbn, ntFieldType));
			doc.add(new FloatField("cost", Float.valueOf(cost), Field.Store.YES));
			doc.add(new Field("title", title, ft3));
			/*String title2 = String.valueOf(title.charAt(0)).toLowerCase();
			System.out.printf("\tTitle2=%s\n", title2);*/
			doc.add(new Field("title2", title.toLowerCase(), ft3));
			doc.add(new Field("category", cate, ft3));
			doc.add(new TextField("contents", cnt, Field.Store.YES));
			doc.add(new IntField("pubdate", Integer.valueOf(date), Field.Store.YES));
			//System.out.printf("\t[Info] Content:\n%s\n\n", cnt);
			writer.addDocument(doc);
		}
	}
    
    // Listing 3-1
    public void testTerm() throws Exception {                 
        // 1) Create IndexSearcher -> directory is built during setUp()  
        IndexReader idxReader = DirectoryReader.open(directory);  
        IndexSearcher searcher = new IndexSearcher(idxReader);  
          
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
        assertEquals("Ant in Action, JUnit in Action, Second Edition",  1, docs.totalHits);  
          
        // 7) Close searcher and directory.  
        idxReader.close();         
    } 
    
    public IndexSearcher getSearcher() throws Exception
    {
    	IndexReader idxReader = DirectoryReader.open(directory); 
    	return new IndexSearcher(idxReader);
    }
    
    public void testMatchAll() throws Exception
    {
    	IndexSearcher searcher = getSearcher();
    	TopDocs matchers = searcher.search(new MatchAllDocsQuery(), 100);
    	System.out.printf("\t[Info] Total doc=%d\n", matchers.totalHits);
    	for(ScoreDoc sd:matchers.scoreDocs)
    	{
    		Document doc = searcher.doc(sd.doc);
    		System.out.printf("\tTitle2=%s\n", doc.get("title2"));
    	}
    }
    
    // Listing 3-2
    public void testQueryParser() throws Exception {
    	// 1) Create IndexSearcher -> directory is built during setUp()  
        IndexSearcher searcher = getSearcher();  
      
        // 2) Create QueryParser  
        QueryParser parser = new QueryParser(VER, "subject", analyzer);  
          
        // 3) Query subject to have "JUNIT" without "MOCK";  
        Query query = parser.parse("+JUNIT -Groovy");  
        TopDocs docs = searcher.search(query, 10);  
          
        // 4) Assert to have 1 hit.  
        assertEquals(1, docs.totalHits);  
          
        // 5) Fetch the top1 document from search result.  
        Document d = searcher.doc(docs.scoreDocs[0].doc);  
          
        // 6) Assert its title to be "Ant in Action".
        System.out.printf("\t[Info] DOC Title=%s\n", d.get("title"));
        assertEquals("JUnitInAction", d.get("title"));  
          
        // 7) Query again to have "groovy" or "junit"  
        query = parser.parse("groovy OR junit");  
        docs = searcher.search(query, 10);  
          
        // 8) Assert to have 2 hit.  
        assertEquals("JUnit in Action & Groovy in Action", 2, docs.totalHits);  
          
        // 9) Close searcher and directory in tearDown()
    }
    
	public void testKeyword() throws Exception {
		IndexSearcher searcher = getSearcher();
		Term t = new Term("isbn", "9781935182023");
		Query query = new TermQuery(t);
		TopDocs docs = searcher.search(query, 10);
		assertEquals("JUnit in Action, Second Edition", 1, docs.totalHits);
	} 
	
	public void testTermRangeQuery() throws Exception {
		IndexSearcher searcher = getSearcher();		
		TermRangeQuery query = TermRangeQuery.newStringRange("title2", "a", "b", true, true);
		TopDocs matches = searcher.search(query, 100);
		assertEquals(2, matches.totalHits);
	}
	
	public void testInclusive() throws Exception {
		IndexSearcher searcher = getSearcher();
		// pub date of TTC was September 2006
		NumericRangeQuery<Integer> query = NumericRangeQuery.newIntRange(
				"pubdate", 20100510, 20100512, true, true);
		TopDocs matches = searcher.search(query, 10);
		assertEquals(1, matches.totalHits);
	}
	
	public void testPrefix() throws Exception {
		IndexSearcher searcher = getSearcher();
		Term term = new Term("category", "/technology/computers/programming");
		PrefixQuery query = new PrefixQuery(term);
		TopDocs matches = searcher.search(query, 10); // Search with
														// sub-category
		int programmingAndBelow = matches.totalHits;
		System.out.printf("\t[Test] Prog and Below=%d\n", programmingAndBelow);
		matches = searcher.search(new TermQuery(term), 10); // Search without
															// sub-category
		int justProgramming = matches.totalHits;
		System.out.printf("\t[Test] Just Prog=%d\n", justProgramming);
		assertTrue(programmingAndBelow > justProgramming);
	}
	
	// Listing 3.6 Using BooleanQuery to combine required subqueries
	public void testAnd() throws Exception {
		TermQuery searchingBooks = new TermQuery(new Term("subject", "action"));
		Query books2010 = NumericRangeQuery.newIntRange("pubdate", 20100510, 20110303, true, true);
		BooleanQuery searchingBooks2010 = new BooleanQuery();
		searchingBooks2010.add(searchingBooks, BooleanClause.Occur.MUST);
		searchingBooks2010.add(books2010, BooleanClause.Occur.MUST);
		IndexSearcher searcher = getSearcher();
		TopDocs matches = searcher.search(searchingBooks2010, 10);
		assertTrue(TestUtil.hitsIncludeTitle(searcher, matches, "JUnitInAction"));
	}
	
	// Listing 3.7 Using BooleanQuery to combine optional subqueries.
	public void testOr() throws Exception {
		TermQuery methodologyBooks = new TermQuery(new Term("category",
				"/technology/computers/programming/framework"));
		TermQuery easternPhilosophyBooks = new TermQuery(new Term("category",
				"/technology/computers/programming/library"));
		BooleanQuery enlightenmentBooks = new BooleanQuery();
		enlightenmentBooks.add(methodologyBooks, BooleanClause.Occur.SHOULD);
		enlightenmentBooks.add(easternPhilosophyBooks, BooleanClause.Occur.SHOULD);
		IndexSearcher searcher = getSearcher();
		TopDocs matches = searcher.search(enlightenmentBooks, 10);
		System.out.println("or = " + enlightenmentBooks);
		assertEquals(5, matches.totalHits);
	}
	
	public void testTermRangeQuery2() throws Exception   
	{  
		Analyzer alyz = new WhitespaceAnalyzer(VER);
	    QueryParser queryParser = new QueryParser(VER, "subject2", alyz);  
	    queryParser.setLowercaseExpandedTerms(false);  
	    Query query = queryParser.parse("[A TO \"Lucene In Action\"]");  
	    assertTrue(query instanceof TermRangeQuery);  
	    System.out.printf("\t[Test] %s\n", query);  
	    IndexSearcher searcher = getSearcher();
	    TopDocs matches = searcher.search(query, 10);  
	    System.out.printf("\t[Test] Hit %d\n", matches.totalHits);
	    //TestUtil.hitsIncludeTitle(searcher, matches, "subject2", "");
	    assertEquals(6, matches.totalHits);  
	    query = queryParser.parse("{A TO \"Lucene In Action\"}");
	    System.out.printf("\t[Test] %s\n", query);
	    matches = searcher.search(query, 10);  
	    System.out.printf("\t[Test] Hit %d\n", matches.totalHits);
	    //TestUtil.hitsIncludeTitle(searcher, matches, "subject2", "");
	    assertEquals(5, matches.totalHits);  
	}
}
