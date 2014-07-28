package demo.ch2;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

public class LockTest extends TestCase {
	private Version VER = Version.LUCENE_4_9;
	private Directory dir;

	protected void setUp() throws IOException {
		String indexDir = System.getProperty("java.io.tmpdir", "tmp")
				+ System.getProperty("file.separator") + "index";
		dir = FSDirectory.open(new File(indexDir));
	}

	public void testWriteLock() throws IOException {
		IndexWriterConfig iwConfig1 = new IndexWriterConfig(VER, new WhitespaceAnalyzer(VER));
		IndexWriterConfig iwConfig2 = new IndexWriterConfig(VER, new WhitespaceAnalyzer(VER));
		IndexWriter writer1 = new IndexWriter(dir, iwConfig1);
		IndexWriter writer2 = null;
		try {
			writer2 = new IndexWriter(dir, iwConfig2);
			fail("We should never reach this point");
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} finally {
			writer1.close();
			assertNull(writer2);
		}
	}
}
