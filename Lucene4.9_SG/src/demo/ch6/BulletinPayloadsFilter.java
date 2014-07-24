package demo.ch6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;

import demo.ch6.BulletinPayloadsAnalyzer.BulletinBean;

public class BulletinPayloadsFilter extends TokenFilter {
	private CharTermAttribute termAtt;
	private PayloadAttribute payloadAttr;
	private BytesRef boostPayload;	
	private BulletinBean bb;
		

	BulletinPayloadsFilter(TokenStream in, BulletinBean bb) {
		super(in);		
		payloadAttr = addAttribute(PayloadAttribute.class);
		termAtt = addAttribute(CharTermAttribute.class);
		boostPayload = new BytesRef(PayloadHelper.encodeFloat(bb.warningBoost));
		this.bb =bb;
	}

	public final boolean incrementToken() throws IOException {
		if (input.incrementToken()) {						
			List<Character> chars = new ArrayList<Character>();
			/*System.out.printf("\t[Test] Check term: ");
			for(char c:termAtt.buffer()) {
				System.out.printf("'%c (%d)' ", c, (int)c);				
				if(c==0) break;				
				chars.add(c);
			}*/
			char charArray[] = new char[chars.size()];
			for(int i=0; i<chars.size(); i++) charArray[i]=chars.get(i);
			String term = String.valueOf(charArray);
			//System.out.printf(" -> '%s' (%d|%s)\n", term, term.length(), bb.isBulletin);
			if (bb.isBulletin && term.startsWith("warning")) {
				//System.out.printf("\t[Test] '%s' with payload...\n", term);
				payloadAttr.setPayload(boostPayload);
			} else {
				payloadAttr.setPayload(null);
			}
			return true;
		} else {
			return false;
		}
	}
}
