package demo.ch6;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.Version;

public class NumericRangeQueryParser extends QueryParser {
	public NumericRangeQueryParser(Version matchVersion, String field, Analyzer a) {
		super(matchVersion, field, a);
	}

	@Override
	public Query getRangeQuery(String field, String part1, String part2, boolean sInclusive, boolean eInclusive) throws ParseException {
		TermRangeQuery query = (TermRangeQuery) super.getRangeQuery(field, part1, part2, sInclusive, eInclusive);
		if ("price".equals(field)) 
		{
			return NumericRangeQuery.newDoubleRange("price",
					Double.parseDouble(query.getLowerTerm().utf8ToString()),
					Double.parseDouble(query.getUpperTerm().utf8ToString()),
					query.includesLower(), query.includesUpper());
		} 
		else 
		{
			return query;
		}
	}
}
