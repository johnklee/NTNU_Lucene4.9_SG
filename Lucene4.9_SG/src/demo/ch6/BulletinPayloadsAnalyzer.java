package demo.ch6;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;


// https://github.com/sudarshang/l4ia/blob/master/src/lia/extsearch/payloads/BulletinPayloadsAnalyzer.java
// http://fossies.org/dox/lucene-4.9.0-src/StandardAnalyzer_8java_source.html
public class BulletinPayloadsAnalyzer extends Analyzer {
	private Version	VER = Version.LUCENE_4_9;
	private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;
	private BulletinBean bb;
	
	class BulletinBean{		
		public boolean	isBulletin=false;
		public float	warningBoost;
		public BulletinBean(boolean isBtn, float boost){this.isBulletin=isBtn; this.warningBoost=boost;}
	}
	
	BulletinPayloadsAnalyzer(float boost) {
		bb = new BulletinBean(false, boost);
	}

	void setIsBulletin(boolean v) {
		bb.isBulletin=v;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {			
		StandardTokenizer src = new StandardTokenizer(VER, reader);
		src.setMaxTokenLength(maxTokenLength);		
		//System.out.printf("\t[Test] CreateComponents for field=%s (%s)...\n", fieldName, isBulletin);
		TokenStream tok = new BulletinPayloadsFilter(src, bb);
		return new TokenStreamComponents(src, tok);
	}
}
