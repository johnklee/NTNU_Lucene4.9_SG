package demo.ch1;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import flib.util.io.QSReader;

public class Indexer {
	private Version VER = Version.LUCENE_4_9;
	private IndexWriter writer;
	private FieldType ntFieldType;
	

	public static class TextFilesFilter implements FileFilter {
		public boolean accept(File path) {
			// 6) Index .txt only.
			return path.getName().toLowerCase().endsWith(".txt");
		}
	}

	public Indexer(String indexDir) throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir));
		IndexWriterConfig iwConfig = new IndexWriterConfig(VER, new StandardAnalyzer(VER));
		writer = new IndexWriter(dir, iwConfig);
		ntFieldType = new FieldType();
		ntFieldType.setIndexed(false);
		ntFieldType.setStored(true);
	}

	public void close() throws IOException {
		writer.close();
	}

	public int index(String dataDir, FileFilter filter) throws Exception {
		File[] files = new File(dataDir).listFiles();
		for (File f : files) {
			if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead()
					&& (filter == null || filter.accept(f))) {
				indexFile(f);
			}
		}
		return writer.numDocs(); // 5) Return the number of indexed docs.
	}

	public String readTxt(File doc) throws IOException
	{
		QSReader qsr = new QSReader(doc);
		qsr.open();
		StringBuffer contentBuf = new StringBuffer();
		for(String line:qsr) contentBuf.append(String.format("%s\n", line));
		qsr.close();
		return contentBuf.toString();
	}
	
	protected Document getDocument(File f) throws Exception {
		Document doc = new Document();		
		doc.add(new TextField("contents", readTxt(f), Field.Store.YES));
		doc.add(new Field("filename", f.getName(), ntFieldType)); // 8) Index filename  
	    doc.add(new Field("fullpath", f.getCanonicalPath(), ntFieldType)); // 9) Index full path
		return doc;
	}

	private void indexFile(File f) throws Exception {
		System.out.println("Indexing " + f.getCanonicalPath());
		Document doc = getDocument(f);
		writer.addDocument(doc); // 10) Add doc to Lucene index
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			throw new IllegalArgumentException("Usage: java "
					+ Indexer.class.getName() + " <index dir> <data dir>");
		}
		String indexDir = args[0]; // 1) Create index in this directory
		String dataDir = args[1]; // 2) Index *.txt from this directory
		long start = System.currentTimeMillis();
		Indexer indexer = new Indexer(indexDir);
		int numIndexed;
		try {
			numIndexed = indexer.index(dataDir, new TextFilesFilter());
		} finally {
			indexer.close();
		}
		long end = System.currentTimeMillis();
		System.out.println("Indexing " + numIndexed + " files took "
				+ (end - start) + " milliseconds");
	}
}
