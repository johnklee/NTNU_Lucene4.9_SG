package demo.ch4;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

public class CustAnalyzer extends Analyzer{
	public Version matchVersion = Version.LUCENE_4_9;  
	public CharArraySet stopWords = new CharArraySet(matchVersion, 0, false);

	@Override
	protected TokenStreamComponents createComponents(String field, Reader reader) {
		Tokenizer tok = new LowerCaseTokenizer(matchVersion, reader);
		TokenStream stopFilter = new StopFilter(matchVersion, tok, stopWords);
		return new TokenStreamComponents(tok, stopFilter);
	}
}
