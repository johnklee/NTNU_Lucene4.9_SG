package demo.ch6;

import java.io.IOException;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.OpenBitSet;

public class SpecialsFilter extends Filter {
	private SpecialsAccessor accessor;

	public SpecialsFilter(SpecialsAccessor accessor) {
		this.accessor = accessor;
	}

	@Override
	public DocIdSet getDocIdSet(AtomicReaderContext ctx, Bits bits)
			throws IOException {
		AtomicReader reader = ctx.reader();
		OpenBitSet oBits = new OpenBitSet(reader.maxDoc());
		String[] isbns = accessor.isbns();
		for (String isbn : isbns) 
		{
			DocsEnum docEnum = reader.termDocsEnum(new Term("isbn", isbn));
			while(docEnum.nextDoc()!= DocIdSetIterator.NO_MORE_DOCS)
			{
				if(docEnum.freq()>0)
				{
					oBits.set(docEnum.docID());
				}
			}
		}
		return oBits;
	}
}
