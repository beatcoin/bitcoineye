package org.beatcoin.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.beatcoin.BitcoinIServletConfig;
import org.beatcoin.dao.Account;
import org.beatcoin.pool.AddressPool;
import org.beatcoin.pool.NotInitializedException;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;

@Path(WalletResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class WalletResource {
	public final static String PATH = "/accounts";
	private final RNQuery query;
	
	private final GenericRepository dao;
	
	private final HttpServletRequest httpReq;
	private final AddressPool addressPool;
	
	@Inject
	public WalletResource(ServletRequest request,
			AddressPool addressPool) {
		this.httpReq = (HttpServletRequest)request;
		this.query = (RNQuery)httpReq.getAttribute(RNQuery.QUERY_PARAM);
		this.dao = (GenericRepository)httpReq.getAttribute("gr");
		this.addressPool = addressPool;
	}
	
	@POST
	public Response create(Account account){
		dao.add(account);
		try {
			return Response.created(new URI(BitcoinIServletConfig.basePath+ PATH+"/"+account.getId())).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}		
	}
	
	@GET
	public List<Account> getEntity() {
		return dao.queryList(query, Account.class);
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
				throw new WebApplicationException("address not in pool", Response.Status.NOT_FOUND);
			}
		} catch (NotInitializedException e) {
			throw new WebApplicationException(e, Response.Status.PRECONDITION_FAILED);
		}
	}

}
