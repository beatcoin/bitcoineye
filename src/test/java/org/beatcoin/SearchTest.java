package org.beatcoin;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Test;

public class SearchTest {
	
	@Test
	public void testSerach() throws IOException, ParseException{
		 Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);

	    // Store the index in memory:
	    Directory directory = new RAMDirectory();
	    //index
	    IndexWriter iwriter = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_45, analyzer));
	    //content
	    Document doc = new Document();
	    doc.add(new Field("fieldname", "This is the text to be indexed.", TextField.TYPE_STORED));
	    doc.add(new Field("other", "other field", TextField.TYPE_STORED));
	    doc.add(new Field("account", "jake", TextField.TYPE_STORED)); 
	    iwriter.addDocument(doc);
	    doc = new Document();
	    doc.add(new Field("fieldname", "hallo", TextField.TYPE_STORED));
	    doc.add(new Field("other", "ollah other", TextField.TYPE_STORED));
	    doc.add(new Field("account", "jake2", TextField.TYPE_STORED)); 
	    iwriter.addDocument(doc);
	    iwriter.close();
	    
	    // Now search the index:
	    DirectoryReader ireader = DirectoryReader.open(directory);
	    IndexSearcher isearcher = new IndexSearcher(ireader);
	    // Parse a simple query that searches for "text":
	    QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_45,new String[] {"fieldname", "other"}, analyzer);
	    Query query = parser.parse("other");
	    Filter jakeFilter = new QueryWrapperFilter(new TermQuery(new Term("account", "jake"))); 
	    ScoreDoc[] hits = isearcher.search(query, jakeFilter, 1000).scoreDocs;
	    
	    Assert.assertEquals(1, hits.length);
	    // Iterate through the results:
	    for (int i = 0; i < hits.length; i++) {
	      Document hitDoc = isearcher.doc(hits[i].doc);
	      Assert.assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
	    }
	    ireader.close();
	    directory.close();
	}

}
