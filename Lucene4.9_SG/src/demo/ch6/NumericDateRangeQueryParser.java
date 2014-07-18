package demo.ch6;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.Version;

public class NumericDateRangeQueryParser extends QueryParser {
	public NumericDateRangeQueryParser(Version matchVersion, String field, Analyzer a) {
		super(matchVersion, field, a);
	}

	@Override
	public Query getRangeQuery(String field, String part1, String part2, boolean sInc, boolean eInc) throws ParseException {
		TermRangeQuery query = (TermRangeQuery) super.getRangeQuery(field, part1, part2, sInc, eInc);
		if ("pubmonth".equals(field)) 
		{
			DateFormat dateformat = new SimpleDateFormat("yyyyMM");
			Resolution res = this.getDateResolution("pubmonth");
			System.out.printf("\t[Test] Resolution=%s\n", res);
			System.out.printf("\t[Test] Lower Term: %s\n", query.getLowerTerm().utf8ToString());
			System.out.printf("\t[Test] Upper Term: %s\n", query.getUpperTerm().utf8ToString());
			try
			{
				return NumericRangeQuery.newLongRange("pubmonth",
                        dateformat.parse(query.getLowerTerm().utf8ToString()).getTime(),
                        dateformat.parse(query.getUpperTerm().utf8ToString()).getTime(),
                        query.includesLower(), query.includesUpper());
			}
			catch(Exception e){throw new ParseException("");}
		} else {
			return query;
		}
	}
}
