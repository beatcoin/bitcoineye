package org.beatcoin.resources;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
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
import org.beatcoin.pojo.Song;
import org.beatcoin.pool.AddressPool;
import org.beatcoin.pool.NotInitializedException;

@Path(QueueResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class QueueResource {
	public final static String PATH = "/queues";
	
	private final Cache cache;
	private final Directory directory;
	private final Analyzer analyzer;
	private final AddressPool addressPool;
	
	@Inject
	public QueueResource(Cache cache,
			Directory directory, AddressPool addressPool){
		this.directory = directory;
		this.analyzer = new StandardAnalyzer(Version.LUCENE_45);
		this.cache = cache;
		this.addressPool = addressPool;
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/{account}")
	public Song[] getSongs(@PathParam("account") String account){
		Element e = cache.get(account);
		if (null!=e){
			PriorityBlockingQueue<Song> pq = (PriorityBlockingQueue<Song>)e.getObjectValue();
			return pq.toArray(new Song[pq.size()]);
		}else{
			return new Song[0];
		}
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/{account}")
	public Song playSong(@PathParam("account") String account){
		Element e = cache.get(account);
		if (null!=e){
			PriorityBlockingQueue<Song> pq = (PriorityBlockingQueue<Song>)e.getObjectValue();
			Song next = pq.peek();
			if (null==next || next.getSum().compareTo(BigDecimal.ZERO)<=0){
				return null;
			}else{
				next = pq.poll();
			}
			String address = next.getAddress();
			//add to history
			Element e1 = cache.get("history-"+account);
			List<Song> hl = (null!=e1)?(List<Song>)e1.getObjectValue():new ArrayList<Song>();
			hl.add(next.setAddress(null).setPlayedAt(System.currentTimeMillis()));
			cache.put(new Element("history-"+account, hl));
			//release address
			try {
				boolean released = addressPool.releaseAddress(account, address);
				if (!released){
					throw new WebApplicationException("address not in pool", Response.Status.NOT_FOUND);
				}
			} catch (NotInitializedException ex) {
				throw new WebApplicationException(ex, Response.Status.PRECONDITION_FAILED);
			}
			return next;
		}else{
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@PUT
	@Path("/{account}")
	public Song pushSongToQueue(@PathParam("account") String account, Song song){
		Song rv = null;
		try{
			//get song from index
		    DirectoryReader ireader = DirectoryReader.open(directory);
		    IndexSearcher isearcher = new IndexSearcher(ireader);
		    Query query = null;
		    QueryParser parser = new QueryParser(Version.LUCENE_45,"id", analyzer);
			query = parser.parse("\""+song.getId()+"\"");
		    Filter jakeFilter = new QueryWrapperFilter(new TermQuery(new Term("account", account.replace("-", "")))); 
		    ScoreDoc[] hits = isearcher.search(query, jakeFilter, 1).scoreDocs;
		    Document hitDoc = isearcher.doc(hits[0].doc);
		    rv = new Song().setId(hitDoc.get("id"))
		    		  .setAlbum(hitDoc.get("album"))
		    		  .setArtist(hitDoc.get("artist"))
		    		  .setTitle(hitDoc.get("title"));
		    ireader.close();
		    //******
		    //TODO: check if not in queue already
		    //****
		    //get a bitcoin address
		    String address = null;
			try {
				address = addressPool.reserveAddress(account);
			} catch (NotInitializedException e) {
				throw new WebApplicationException(e, Response.Status.PRECONDITION_FAILED);
			}
			if (address==null){
				//actually go into the existing queue and use oldest empty address
				throw new WebApplicationException("pool exhausted", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE);
			}
		    //add to queue
			rv.setAddress(address).setSum(new BigDecimal("0.0").setScale(8)).setQueuedAt(System.currentTimeMillis());
			Element e = cache.get(account);
			PriorityBlockingQueue<Song> pq = (null!=e)?(PriorityBlockingQueue<Song>)e.getObjectValue():new PriorityBlockingQueue<Song>();
			pq.add(rv);
			cache.put(new Element(account,pq));
			//notify via socket
			BitcoinIServletConfig.server.getRoomOperations(account).sendMessage("hallo");
		}catch (IOException|ParseException e){
			e.printStackTrace();
		}
		return rv;
	}

}
