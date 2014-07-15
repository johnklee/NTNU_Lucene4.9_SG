package demo.ch6;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

public class BookLinkCollector extends Collector{
	private Map<String, String> documents = new HashMap<String, String>();
	private Scorer scorer;	
	private int baseID;
	private IndexReader reader;

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void setScorer(Scorer scorer) {
		this.scorer = scorer;
	}
	  
	@Override
	public void setNextReader(AtomicReaderContext context) throws IOException {		
		reader = context.reader();				
		baseID=context.docBase;
	}
	
	@Override
	public void collect(int docID) {
		try {							
			Document doc = reader.document(docID+baseID);			
			String url = doc.get("url");
			String title =doc.get("title2");
			String cnt = doc.get("contents");			
			documents.put(url, title);
			System.out.println(title + ":" + scorer.score());			
		} catch (IOException e) {
		}
	}
	
	public Map<String,String> getLinks() {
	    return Collections.unmodifiableMap(documents);
	}
}
