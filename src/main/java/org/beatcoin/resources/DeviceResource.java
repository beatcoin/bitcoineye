package org.beatcoin.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.beatcoin.BitcoinIServletConfig;
import org.beatcoin.dao.Account;
import org.beatcoin.dao.Device;
import org.beatcoin.pool.AddressPool;
import org.beatcoin.pool.PoolInitializer;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;

import com.google.inject.Injector;

@Path(DeviceResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class DeviceResource {
	public final static String PATH = "/devices";
	
	private final RNQuery query;
	
	private final GenericRepository dao;
	
	private final HttpServletRequest httpReq;
	
	private final AddressPool addressPool;
	private final Injector injector;
	
	@Inject public DeviceResource(ServletRequest request,
			AddressPool addressPool,
			Injector injector) {
		this.httpReq = (HttpServletRequest)request;
		this.query = (RNQuery)httpReq.getAttribute(RNQuery.QUERY_PARAM);
		this.dao = (GenericRepository)httpReq.getAttribute("gr");
		this.addressPool = addressPool;
		this.injector = injector;
	}
	
	@POST
	public Response createOnCollection(Device d) {
		if (d.getOwner()==null||d.getUuid()!=null){
			throw new WebApplicationException("owner missing or uuid passed", Response.Status.BAD_REQUEST);
		}
		Account owner = dao.getObjectById(d.getOwner().getId(), Account.class);
		d.setOwner(owner);
		String uuid = UUID.randomUUID().toString();
		PoolInitializer poolInitializer = injector.getInstance(PoolInitializer.class);
		poolInitializer.setPool(addressPool);
		poolInitializer.setAccount(uuid);
		poolInitializer.start();
		d.setUuid(uuid);
		dao.add(d);
		try {
			return Response.created(new URI(BitcoinIServletConfig.basePath+ PATH+"/"+d.getId())).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@DELETE
	public void delete(){
		dao.queryDelete(query, Device.class);
	}
	
	@PUT
	@Path("/{uuid}")
	public void updateEntity(Device e, @PathParam("uuid") String uuid) {
		RNQuery q = new RNQuery().addFilter("uuid", uuid);
		Device old = dao.queryEntity(q, Device.class);
		e.setId(old.getId());
		e.setLastConnected(System.currentTimeMillis());
		old.update(e);
	}

	@GET
	public List<Device> getEntity() {
		return dao.queryList(query, Device.class);
	}
	
}
