package demo.ch6;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.util.BytesRef;

public class BoostingSimilarity extends DefaultSimilarity {		
	@Override
	public float scorePayload(int doc,
            int start,
            int end,
            BytesRef payload)
	{
		if (payload != null) {
			float score = PayloadHelper.decodeFloat(payload.bytes);
			//System.out.printf("\t[Test] Score Payload=%.02f\n", score);
			return score;
		} else {
			//System.out.printf("\t[Test] Score Payload=%.02f\n", 1.0F);
			return 1.0F;
		}
	}
}
