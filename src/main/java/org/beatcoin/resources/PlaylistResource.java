package org.beatcoin.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.beatcoin.BitcoinIServletConfig;
import org.beatcoin.dao.Device;
import org.beatcoin.dao.Playlist;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;


@Path(PlaylistResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class PlaylistResource {
	public final static String PATH = "/playlists";
	
	private final GenericRepository dao;
	
	@Inject public PlaylistResource(ServletRequest request) {
		HttpServletRequest httpReq = (HttpServletRequest)request;
		dao = (GenericRepository)httpReq.getAttribute("gr");
	}
	

	@GET
	@Path("/{uuid}")
	public Set<Playlist> getEntity(@PathParam("uuid") String uuid, 
			@QueryParam("active") String active) {
		RNQuery q = new RNQuery().addFilter("uuid", uuid);
		Device device = dao.queryEntity(q, Device.class);
		if (null!=active){
			Set<Playlist> rv = new HashSet<>();
			for (Playlist p: device.getPlaylists()){
				if (p.getActive()){
					rv.add(p);	
				}
			}
			return rv;
		}else{
			return device.getPlaylists();
		}
	}
	
	@PUT
	@Path("/{uuid}")
	public void updateEntity(Playlist p, @PathParam("uuid") String uuid) {
		RNQuery q = new RNQuery().addFilter("uuid", uuid);
		Device device = dao.queryEntity(q, Device.class);
		if (p.getName()==null){
			throw new WebApplicationException("name missing", Response.Status.BAD_REQUEST);
		}
		boolean updated = false;
		for (Playlist pl : device.getPlaylists()){
			if (pl.getName().equalsIgnoreCase(p.getName())){
				pl.update(p);
				updated = true;
			}
		}
		if (!updated){
			throw new WebApplicationException("no playlist affected", Response.Status.NOT_FOUND);
		}
	}

	@POST
	@Path("/{uuid}")
	public Response createList(Playlist p, @PathParam("uuid") String uuid){
		RNQuery q = new RNQuery().addFilter("uuid", uuid);
		Device device = dao.queryEntity(q, Device.class);
		if (p.getName()==null){
			throw new WebApplicationException("id missing", Response.Status.BAD_REQUEST);
		}
		for (Playlist pl : device.getPlaylists()){
			if (pl.getName().equalsIgnoreCase(p.getName())){
				throw new WebApplicationException("id collision", Response.Status.CONFLICT);
			}
		}
		device.getPlaylists().add(p);
		try{
			return Response.created(new URI(BitcoinIServletConfig.basePath+ PATH+"/"+uuid)).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

}
