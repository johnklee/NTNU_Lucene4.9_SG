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
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class PhraseQueryTest extends TestCase {
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
		Document doc = new Document();
		doc.add(new TextField("field",
				"the quick brown fox jumped over the lazy dog", Field.Store.YES));
		writer.addDocument(doc);
		IndexReader reader = DirectoryReader.open(writer, true);
		searcher = new IndexSearcher(reader);
	}

	protected void tearDown() throws IOException {
		writer.close();		
		dir.close();
	}

	private boolean matched(String[] phrase, int slop) throws IOException {
		PhraseQuery query = new PhraseQuery();
		query.setSlop(slop);
		for (String word : phrase) {
			query.add(new Term("field", word));
		}
		TopDocs matches = searcher.search(query, 10);
		return matches.totalHits > 0;
	}

	public void testSlopComparison() throws Exception {
		String[] phrase = new String[] { "quick", "fox" };
		assertFalse("exact phrase not found", matched(phrase, 0));
		assertTrue("close enough", matched(phrase, 1));
	}
	
	public void testReverse() throws Exception {  
	    String[] phrase = new String[] { "fox", "quick" };  
	    assertFalse("hop flop", matched(phrase, 2));  
	    assertTrue("hop hop slop", matched(phrase, 3));  
	}
	
	public void testMultiple() throws Exception {  
	    assertFalse("not close enough",  
	            matched(new String[] { "quick", "jumped", "lazy" }, 3));  
	    assertTrue("just enough",  
	            matched(new String[] { "quick", "jumped", "lazy" }, 4));  
	    assertFalse("almost but not quite",  
	            matched(new String[] { "lazy", "jumped", "quick" }, 7));  
	    assertTrue("bingo",  
	            matched(new String[] { "lazy", "jumped", "quick" }, 8));  
	}
}
