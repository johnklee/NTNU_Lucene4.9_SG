package demo.ch6;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import flib.util.io.QSReader;

public class CustQueryParserTest extends TestCase {
	private RAMDirectory directory;
	private IndexSearcher searcher;
	private Query query;
	private Version VER = Version.LUCENE_4_9;
	private FieldType ntFieldType;
	private File books = new File("books");
	private Analyzer analyzer;

	@Override
	protected void setUp() throws Exception {
		directory = new RAMDirectory();
		ntFieldType = new FieldType();
		ntFieldType.setIndexed(true);
		ntFieldType.setStored(true);
		analyzer = new StandardAnalyzer(VER);
		IndexWriterConfig iwConfig = new IndexWriterConfig(VER, analyzer);
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
		try {
			DateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
			for (File book : books.listFiles()) {
				String link = "";
				String cnt = "";
				String price = "";
				String pubmon = "";
				String title = book.getName().replaceAll(".txt", "");
				if (book.isFile()) {
					QSReader qsr = new QSReader(book);
					qsr.open();
					qsr.hasNext();
					link = qsr.next();
					qsr.hasNext();
					price = qsr.next();
					qsr.hasNext();
					pubmon = qsr.next();
					qsr.hasNext();
					cnt = qsr.next();
					qsr.close();
				}
				System.out.printf("\t[Info] Add Book=%s:%s (%.02f)\n", title,
						link, Double.valueOf(price));
				Document doc = new Document();
				doc.add(new Field("url", link, ntFieldType));
				doc.add(new Field("title2", title, ntFieldType));
				doc.add(new TextField("contents", cnt, Field.Store.YES));
				doc.add(new DoubleField("price", Double.valueOf(price),
						Field.Store.YES));
				doc.add(new LongField("pubmonth", dateformat.parse(pubmon).getTime(), Field.Store.YES));
				writer.addDocument(doc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testCustomQueryParser() {
		CustomQueryParser parser = new CustomQueryParser(VER, "field", analyzer);
		try {
			parser.parse("a?t");
			fail("Wildcard queries should not be allowed");
		} catch (ParseException expected) {
		}
		try {
			parser.parse("xunit~");
			fail("Fuzzy queries should not be allowed");
		} catch (ParseException expected) {
		}
	}

	public void testNumericRangeQuery() throws Exception {
		String expression = "price:[50 TO 90]";
		QueryParser parser = new NumericRangeQueryParser(VER, "subject",
				analyzer);
		Query query = parser.parse(expression);
		System.out.println(expression + " parsed to " + query);
	}

	public void testDateRangeQuery() throws Exception {
		String expression = "pubmonth:[01/01/2010 TO 06/01/2010]";
		QueryParser parser = new NumericDateRangeQueryParser(VER, "subject", analyzer);
		parser.setDateResolution("pubmonth", DateTools.Resolution.MONTH);
		parser.setLocale(Locale.US);
		Query query = parser.parse(expression);
		System.out.println(expression + " parsed to " + query);
		TopDocs matches = searcher.search(query, 10);
		assertTrue("expecting at least one result !", matches.totalHits > 0);
	}
	
	public void testPhraseQuery() throws Exception {
		CQPWithSpanQuery parser = new CQPWithSpanQuery(VER, "contents", analyzer);
		Query query = parser.parse("singleTerm");
		assertTrue("TermQuery", query instanceof TermQuery);
		query = parser.parse("\"phrase test\"");
		System.out.printf("\t[Test] PhraseQuery: %s\n", query.getClass().getName());
		assertTrue("SpanNearQuery", query instanceof SpanNearQuery);
	}
}
