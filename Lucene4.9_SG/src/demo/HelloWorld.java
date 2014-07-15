package demo;

import java.io.File;
import java.io.IOException;

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
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import flib.util.io.QSReader;


public class HelloWorld {
	public static Version 	LUCENE_VER = Version.LUCENE_4_9;
	public static File 		TEST_DATAS = new File("data");
	public static int		TOP_N=10;
	
	public static String ReadDoc(File doc) throws IOException
	{
		QSReader qsr = new QSReader(doc);
		qsr.open();
		StringBuffer contentBuf = new StringBuffer();
		for(String line:qsr) contentBuf.append(String.format("%s\n", line));
		qsr.close();
		return contentBuf.toString();
	}
	
	/**
	 * Reference
	 * 	- SearchFiles.java: code to search a Lucene index.
	 *    http://lucene.apache.org/core/4_9_0/demo/src-html/org/apache/lucene/demo/SearchFiles.html
	 *  - IndexFiles.java: code to create a Lucene index.
	 *    http://lucene.apache.org/core/4_9_0/demo/src-html/org/apache/lucene/demo/IndexFiles.html
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException
	{
		// 1) Create IndexWriter
		Directory directory = new RAMDirectory();
		IndexWriterConfig iwConfig = new IndexWriterConfig(LUCENE_VER, new StandardAnalyzer(LUCENE_VER));
		IndexWriter writer = new IndexWriter(directory, iwConfig);
		
		// 2) Start Indexing Document
		System.out.printf("\t[Info] Start Indexing Document...\n");
		FieldType fieldType = new FieldType();		
		fieldType.setIndexed(true);
		//fieldType.setStored(true);
		FieldType ntFieldType = new FieldType();
		ntFieldType.setIndexed(false);
		ntFieldType.setStored(true);
		for(File f:TEST_DATAS.listFiles())
		{
			
			System.out.printf("\t\tIndexing %s...\n", f.getName());
			Document doc = new Document(); 
			doc.add(new TextField("contents", ReadDoc(f), Field.Store.YES));
			//doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))));
			//doc.add(new Field("contents", contentBuf.toString(), Field.Store.YES, Field.Index.ANALYZED)); // 7) Index file content.
			//doc.add(new Field("contents", new FileReader(f))); // 7) Index file content. 
		    doc.add(new Field("filename", f.getName(), ntFieldType)); // 8) Index filename  
		    doc.add(new Field("fullpath", f.getCanonicalPath(), ntFieldType)); // 9) Index full path
		    writer.addDocument(doc);
		}
		writer.close();
		
		// 3) Create new searcher  
		IndexReader idxReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(idxReader);
        
        // 4) Build single-term query.  
        Term t = new Term("contents", "index");  
        Query query = new TermQuery(t);
        
        // 5) Searching
        TopDocs qRst = searcher.search(query, TOP_N);
        System.out.printf("\t[Info] Total %d matched doc:\n", qRst.totalHits);
        //assert qRst.totalHits==3;
        for(ScoreDoc sd:qRst.scoreDocs)
        {
        	Document doc = searcher.doc(sd.doc);
        	System.out.printf("\t\t%s...(%.02f)\n", doc.getField("filename").stringValue(), sd.score);
        	//System.out.printf("%s\n\n", doc.getField("contents").stringValue());
        }
        directory.close();
	}
}
