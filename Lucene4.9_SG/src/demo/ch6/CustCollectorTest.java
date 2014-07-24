package demo.ch6;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import flib.util.io.QSReader;

public class CustCollectorTest extends TestCase {
	private RAMDirectory directory;
	private IndexSearcher searcher;
	private Query query;
	private Version VER = Version.LUCENE_4_9;
	private FieldType ntFieldType;
	private FieldType nt2FieldType;
	private File books = new File("books");

	@Override
	protected void setUp() throws Exception {
		directory = new RAMDirectory();
		ntFieldType = new FieldType();
		ntFieldType.setIndexed(true);
		ntFieldType.setStored(true);
		nt2FieldType = new FieldType();
		nt2FieldType.setStored(true);
		nt2FieldType.setTokenized(false);
		IndexWriterConfig iwConfig = new IndexWriterConfig(VER,
				new StandardAnalyzer(VER));
		IndexWriter writer = new IndexWriter(directory, iwConfig);
		addBooks(writer);
		writer.close();
		IndexReader reader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(reader);
		query = new TermQuery(new Term("contents", "java"));
	}

	@Override
	protected void tearDown() throws Exception {
		directory.close();
		searcher.getIndexReader().close();
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
			doc.add(new Field("url", link, ntFieldType));
			doc.add(new Field("isbn", isbn, ntFieldType));
			doc.add(new FloatField("cost", Float.valueOf(cost), Field.Store.YES));
			doc.add(new Field("title2", title, ntFieldType));
			doc.add(new TextField("category", cate, Field.Store.YES));
			doc.add(new TextField("contents", cnt, Field.Store.YES));
			writer.addDocument(doc);
		}
	}

	public void testCollecting() throws Exception {
		BookLinkCollector collector = new BookLinkCollector();
		searcher.search(query, collector);
		Map<String, String> linkMap = collector.getLinks();
		System.out.printf("\t[Info] Map Size=%d:\n", linkMap.size());
		Iterator<Entry<String, String>> iter = linkMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> e = iter.next();
			System.out.printf("\t\t%s->%s\n", e.getKey(), e.getValue());
		}
		assertEquals("AntInAction",
				linkMap.get("http://www.manning.com/loughran/"));

		/*
		 * TopDocs docs = searcher.search(query, 10);
		 * System.out.printf("\t[Test] Hit %d\n", docs.totalHits); for(ScoreDoc
		 * sd:docs.scoreDocs) { Document doc = searcher.doc(sd.doc);
		 * System.out.printf("\tTitle=%s\n", doc.get("title2"));
		 * System.out.printf("%s\n\n", doc.get("contents")); }
		 */
	}

	public void testCustomFilter() throws Exception {
		Query allBooks = new TermQuery(new Term("contents", "manning"));
		String[] isbns = new String[] { "1933988940", "9781935182023" };
		SpecialsAccessor accessor = new TestSpecialsAccessor(isbns);
		Filter filter = new SpecialsFilter(accessor);
		TopDocs hits = searcher.search(allBooks, filter, 10);
		assertEquals("the specials", isbns.length, hits.totalHits);
	}

	public void testFilteredQuery() throws Exception {
		// 1)
		String[] isbns = new String[] { "9781935182023" };		
		
		// 2)
		SpecialsAccessor accessor = new TestSpecialsAccessor(isbns);		
		Filter filter = new SpecialsFilter(accessor);
		WildcardQuery educationBooks = new WildcardQuery(new Term("category", "*education*"));
		FilteredQuery edBooksOnSpecial = new FilteredQuery(educationBooks, filter);
		
		// 3)
		TermQuery logoBooks = new TermQuery(new Term("subject", "action"));
		
		// 4)
		BooleanQuery logoOrEdBooks = new BooleanQuery();
		//logoOrEdBooks.add(logoBooks, BooleanClause.Occur.SHOULD);
		logoOrEdBooks.add(edBooksOnSpecial, BooleanClause.Occur.SHOULD);
		TopDocs hits = searcher.search(logoOrEdBooks, 10);
		System.out.println(logoOrEdBooks.toString());
		assertEquals("Education & Special", 1, hits.totalHits);
	}
}
