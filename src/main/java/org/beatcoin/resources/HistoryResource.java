package org.beatcoin.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.beatcoin.pojo.Song;


@Path(HistoryResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class HistoryResource {
	public final static String PATH = "/histories";
	
	private final Cache cache;

	public HistoryResource(Cache cache){
		this.cache = cache;
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/{account}/songs")
	public List<Song> getSongs(@PathParam("account") String account){
		Element e = cache.get("history-"+account);
		if (null!=e){
			List<Song> list = (List<Song>)e.getObjectValue();
			Collections.reverse(list);
			return list;
		}else{
			return new ArrayList<>();
		}
	}
	
	
}
