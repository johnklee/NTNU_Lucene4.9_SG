package demo.ch6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;

public class AllDocCollector extends Collector {
	List<ScoreDoc> docs = new ArrayList<ScoreDoc>();
	private Scorer scorer;
	private int docBase;

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void setScorer(Scorer scorer) {
		this.scorer = scorer;
	}

	@Override
	public void setNextReader(AtomicReaderContext context) {
		this.docBase = context.docBase;
	}

	@Override
	public void collect(int doc) throws IOException {
		docs.add(new ScoreDoc(doc + docBase, scorer.score()));
	}

	public void reset() {
		docs.clear();
	}

	public List<ScoreDoc> getHits() {
		return docs;
	}
}
