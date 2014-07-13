package demo.ch1;

import demo.ch1.Indexer.TextFilesFilter;

public class IndexerEx1 {
	public static void main(String[] args) throws Exception{
		String indexDir = "./index";    // 1) Create index in this directory  
        String dataDir = "./data";      // 2) Index *.txt from this directory  
        long start = System.currentTimeMillis();  
        Indexer indexer = new Indexer(indexDir);  
        int numIndexed;  
        try 
        {  
            numIndexed = indexer.index(dataDir, new TextFilesFilter());  
        } 
        finally 
        {  
            indexer.close();  
        }  
        long end = System.currentTimeMillis();  
        System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds"); 
	}
}
