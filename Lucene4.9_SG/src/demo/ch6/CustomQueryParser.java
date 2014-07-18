package demo.ch6;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

public class CustomQueryParser extends QueryParser {
	public CustomQueryParser(Version matchVersion, String field, Analyzer analyzer) {
		super(matchVersion, field, analyzer);
	}

	@Override
	protected final Query getWildcardQuery(String field, String termStr) throws ParseException {
		throw new ParseException("Wildcard not allowed");
	}

	@Override
	protected Query getFuzzyQuery(String field, String term, float minSimilarity) throws ParseException {
		throw new ParseException("Fuzzy queries not allowed");
	}
}