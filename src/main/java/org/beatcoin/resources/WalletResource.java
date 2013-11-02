package org.beatcoin.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.beatcoin.pool.AddressPool;
import org.beatcoin.pool.NotInitializedException;
import org.beatcoin.pool.PoolInitializer;

import com.google.inject.Injector;

@Path(WalletResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class WalletResource {
	public final static String PATH = "/accounts";
	
	private final AddressPool addressPool;
	private final Injector injector;
	
	@Inject
	public WalletResource(AddressPool addressPool,
			Injector injector){
		this.addressPool = addressPool;
		this.injector = injector;
	}
	
	@POST
	public Map<String,String> create(){
		String uuid = UUID.randomUUID().toString();
		Map<String,String> rv = new HashMap<>();
		rv.put("uuid", uuid);
		PoolInitializer poolInitializer = injector.getInstance(PoolInitializer.class);
		poolInitializer.setPool(addressPool);
		poolInitializer.setAccount(uuid);
		poolInitializer.start();
		return rv;
	}
	
	
	@POST
	@Path("/{account}/reserveAddress")
	public Map<String,String> reserveAddress(
			@PathParam("account") String account){
		Map<String,String> rv = new HashMap<>();
		String address = null;
		try {
			address = addressPool.reserveAddress(account);
		} catch (NotInitializedException e) {
			throw new WebApplicationException(e, Response.Status.PRECONDITION_FAILED);
		}
		if (address==null){
			throw new WebApplicationException("pool exhausted", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE);
		}
		rv.put("address", address);
		rv.put("account", account);
		return rv;
	}
	
	@POST
	@Path("/{account}/releaseAddress")
	public void releaseAddress(
			@PathParam("account") String account,
			@FormParam("address") String address){
		try {
			boolean released = addressPool.releaseAddress(account, address);
			if (!released){
				throw new WebApplicationException("pool exhausted", Response.Status.NOT_FOUND);
			}
		} catch (NotInitializedException e) {
			throw new WebApplicationException(e, Response.Status.PRECONDITION_FAILED);
		}
	}

}
