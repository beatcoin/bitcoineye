package org.beatcoin.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import org.apache.commons.lang3.RandomStringUtils;
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
import org.apache.lucene.util.Version;
import org.beatcoin.pojo.Song;

@Path(ArchiveResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class ArchiveResource {
	public final static String PATH = "/archives";
	
	private final Directory directory;
	private final Analyzer analyzer;
	
	@Inject
	public ArchiveResource(Directory directory){
		this.directory = directory;
		this.analyzer = new StandardAnalyzer(Version.LUCENE_45);
	}
		
	@POST
	@Path("/{account}/songs")
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,String> addSongs(List<Song> songs,
			@PathParam("account") String account){
		if (null==songs || songs.size()<1){
			throw new WebApplicationException("no songs in body", Response.Status.BAD_REQUEST);
		}
		Map<String,String> rv = null;
		if (account.equals("0")){
			account = UUID.randomUUID().toString();
			String token = RandomStringUtils.random(5, "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ123456789");
			rv = new HashMap<>();
			rv.put("id", account);
			rv.put("token", token);
		}
		try{
			IndexWriter iwriter = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_45, analyzer));
			for (Song song: songs){
			    Document doc = new Document();
			    doc.add(new Field("id", song.getId(), TextField.TYPE_STORED));
			    doc.add(new Field("title", song.getTitle(), TextField.TYPE_STORED));
			    if (null!= song.getArtist())doc.add(new Field("artist", song.getArtist(), TextField.TYPE_STORED));
			    if (null!= song.getAlbum())doc.add(new Field("album", song.getAlbum(), TextField.TYPE_STORED));
			    doc.add(new Field("account", account, TextField.TYPE_STORED)); 
			    iwriter.addDocument(doc);
			}
			iwriter.close();
			return rv;
		}catch (IOException e){
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("/{account}/songs")
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
			    query = parser.parse(account);
		    }
		    Filter jakeFilter = new QueryWrapperFilter(new TermQuery(new Term("account", account))); 
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
			e.printStackTrace();
		}
		return rv;
	}
	
	@DELETE
	@Path("/{account}/songs")
	public void remSongs(@PathParam("account") String account){
		try{
			IndexWriter iwriter = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_45, analyzer));
			QueryParser parser = new QueryParser(Version.LUCENE_45,"account", analyzer);
			Query query = parser.parse(account);
			iwriter.deleteDocuments(query);
			iwriter.close();
		}catch (IOException|ParseException e){
			e.printStackTrace();
		}
	}
	
}
