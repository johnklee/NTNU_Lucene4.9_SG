package demo.ch6;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.Version;

public class CQPWithSpanQuery extends QueryParser {

	public CQPWithSpanQuery(Version matchVersion, String f, Analyzer a) {
		super(matchVersion, f, a);		
	}

	@Override
	protected Query getFieldQuery(String field, String queryText, int slop) throws ParseException {
		Query orig = super.getFieldQuery(field, queryText, slop);
		System.out.printf("\t[Test] FieldQuery: %s\n", orig.getClass().getName());
		if (!(orig instanceof PhraseQuery)) 
		{
			return orig;
		}
		PhraseQuery pq = (PhraseQuery) orig;
		Term[] terms = pq.getTerms();
		SpanTermQuery[] clauses = new SpanTermQuery[terms.length];
		for (int i = 0; i < terms.length; i++) {
			clauses[i] = new SpanTermQuery(terms[i]);
		}
		SpanNearQuery query = new SpanNearQuery(clauses, slop, true);
		return query;
	}
	
	@Override
	public Query createPhraseQuery(String field,
            					   String queryText,
            					   int phraseSlop)
	{
		System.out.printf("\t[Test] Create PhraseQuery: %s\n", queryText);
		return createPhraseQuery(field, queryText, phraseSlop);
	}
}
