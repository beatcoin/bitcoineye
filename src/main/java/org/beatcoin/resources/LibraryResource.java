package org.beatcoin.resources;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
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
import org.apache.lucene.util.Version;
import org.beatcoin.BitcoinIServletConfig;
import org.beatcoin.pojo.Address;
import org.beatcoin.pojo.Song;
import org.beatcoin.pool.AddressPool;

import com.google.inject.Injector;

@Path(LibraryResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class LibraryResource {
	public final static String PATH = "/songs";
	
	private final Directory directory;
	private final Analyzer analyzer;
	private final AddressPool addressPool;
	
	@Inject
	public LibraryResource(Directory directory, Injector injector, AddressPool addressPool){
		this.directory = directory;
		this.analyzer = new StandardAnalyzer(Version.LUCENE_45);
		this.addressPool = addressPool;
	}
	
	
	@GET
	public Set<String> getPools(){
		Map<String,Set<Address>> pools = addressPool.getPools();
		return pools.keySet();
	}
		
	@POST
	@Path("/{account}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addSongs(Song song,
			@PathParam("account") String account){
		try{
			IndexWriter iwriter = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_45, analyzer));
		    Document doc = new Document();
		    doc.add(new Field("id", song.getId(), TextField.TYPE_STORED));
		    doc.add(new Field("title", song.getTitle(), TextField.TYPE_STORED));
		    if (null!= song.getArtist())doc.add(new Field("artist", song.getArtist(), TextField.TYPE_STORED));
		    if (null!= song.getAlbum())doc.add(new Field("album", song.getAlbum(), TextField.TYPE_STORED));
		    doc.add(new Field("account", account.replace("-", ""), TextField.TYPE_STORED)); 
		    iwriter.updateDocument(new Term("id", song.getId()+account),doc);
			iwriter.close();
			try {
				return Response.created(new URI(BitcoinIServletConfig.basePath+ PATH+"/"+account)).build();
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
			}
		}catch (IOException e){
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("/{account}")
	public List<Song> searchSongs(@QueryParam("query") String queryString,
			@PathParam("account") String account){
		if (null!=queryString && queryString.length()<2){
			return null;
		}
		List<Song> rv = new ArrayList<>();
		try{
		    DirectoryReader ireader = DirectoryReader.open(directory);
		    IndexSearcher isearcher = new IndexSearcher(ireader);
		    // Parse a simple query that searches for "text":
		    Query query = null;
		    if (null!=queryString){
			    QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_45,
			    		new String[] {"title", "artist", "abum"}, analyzer);
			    query = parser.parse(queryString);
		    }else{
			    QueryParser parser = new QueryParser(Version.LUCENE_45,"account", analyzer);
			    query = parser.parse(account.replace("-", ""));
		    }
		    Filter jakeFilter = new QueryWrapperFilter(new TermQuery(new Term("account", account.replace("-", "")))); 
		    ScoreDoc[] hits = isearcher.search(query, jakeFilter, 10).scoreDocs;
		    // Iterate through the results:
		    for (int i = 0; i < hits.length; i++) {
		      Document hitDoc = isearcher.doc(hits[i].doc);
		      Song s = new Song().setId(hitDoc.get("id"))
		    		  .setAlbum(hitDoc.get("album"))
		    		  .setArtist(hitDoc.get("artist"))
		    		  .setTitle(hitDoc.get("title"));
		      rv.add(s);
		    }
		    ireader.close();
		}catch (IOException|ParseException e){
			if (e instanceof IndexNotFoundException){
				throw new WebApplicationException(e, Response.Status.NOT_FOUND);
			}
			e.printStackTrace();
		}
		return rv;
	}
	
	@DELETE
	@Path("/{account}")
	public void remSongs(@PathParam("account") String account){
		try{
			IndexWriter iwriter = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_45, analyzer));
			QueryParser parser = new QueryParser(Version.LUCENE_45,"account", analyzer);
			Query query = parser.parse(account.replace("-", ""));
			iwriter.deleteDocuments(query);
			iwriter.close();
		}catch (IOException|ParseException e){
			e.printStackTrace();
		}
	}
	
}
