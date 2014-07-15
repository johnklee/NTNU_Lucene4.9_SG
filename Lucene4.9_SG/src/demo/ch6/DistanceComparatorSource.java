package demo.ch6;

import java.io.IOException;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

// https://code.google.com/p/fattomato/source/browse/trunk/+fattomato/lucene3/test/com/lotus/lucene/distance/correct/DistanceComparatorSource.java?spec=svn140&r=140
public class DistanceComparatorSource extends FieldComparatorSource { // #1
	private int x;
	private int y;

	public DistanceComparatorSource(int x, int y) { // #2
		this.x = x;
		this.y = y;
	}

	@Override
	public FieldComparator<Float> newComparator(java.lang.String fieldName, int numHits, int sortPos, boolean reversed) throws IOException {
		return new DistanceScoreDocLookupComparator(fieldName, numHits);
	}

	private class DistanceScoreDocLookupComparator extends FieldComparator<Float> {		
		private FieldCache.Ints xDocs;
		private FieldCache.Ints yDocs;
		private float[] values; // #6
		private float bottom; // #7
		private float topVal;
		private String fieldName;

		public DistanceScoreDocLookupComparator(String fieldName, int numHits) throws IOException {
			values = new float[numHits];
			this.fieldName = fieldName;
			System.out.printf("\t[Test] FieldName=%s; numHits=%d\n", fieldName, numHits);
		}

		private float getDistance(int doc) { // #9				
			int deltax = xDocs.get(doc) - x; // #9			
			int deltay = yDocs.get(doc) - y; // #9			
			return (float) Math.sqrt(deltax * deltax + deltay * deltay); // #9
		}
		
		@Override
		public int compare(int slot1, int slot2) { // #10
			// Compare a hit at 'slot1' with hit 'slot2'.
			return Float.valueOf(values[slot1]).compareTo(values[slot2]);			
		}

		@Override
		public void setBottom(int slot) { // #11			
			bottom = values[slot];
		}

		@Override
		public int compareBottom(int doc) { // #12
			// Compare a new hit (docID) against the "weakest" (bottom) entry in the queue.
			float docDistance = getDistance(doc);
			if (bottom < docDistance)
				return -1;
			if (bottom > docDistance)
				return 1;
			return 0;
		}

		@Override
		public void copy(int slot, int doc) {
			// Installs a new hit into the priority queue. 
			// The FieldValueHitQueue calls this method when a new hit is competitive.
			values[slot] = getDistance(doc); 			
		}

		@Override
		public Float value(int slot) { // #14					
			return values[slot]; // #14
		}

		@Override
		public int compareTop(int doc) throws IOException {
			// Compare a new hit (docID) against the top value previously set by
			// a call to setTopValue(T).
			float docDistance = getDistance(doc);
			if (topVal < docDistance)
				return -1;
			if (topVal > docDistance)
				return 1;
			return 0;
		}

		@Override
		public FieldComparator<Float> setNextReader(AtomicReaderContext aRC) throws IOException {			
			// Invoked when the search is switching to the next segment. You may need to update internal state of the comparator, 
			// for example retrieving new values from the FieldCache.
			try
			{
				AtomicReader ar = aRC.reader();	
				System.out.printf("\t[Test] setNextReader...\n");
				xDocs = FieldCache.DEFAULT.getInts(ar, "x", false);
				yDocs = FieldCache.DEFAULT.getInts(ar, "y", false);
			}
			catch(Exception e){e.printStackTrace();}
			return this;
		}

		@Override
		public void setTopValue(Float top) {				
			topVal=top;
		}
	}

	public String toString() {
		return "Distance from (" + x + "," + y + ")";
	}
}
