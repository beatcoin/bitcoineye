package org.beatcoin.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com._37coins.bcJsonRpc.BitcoindInterface;

@Path(WalletResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class WalletResource {
	public final static String PATH = "/wallets";
	
	private final BitcoindInterface client;
	
	@Inject
	public WalletResource(BitcoindInterface client){
		this.client = client;
	}
	
	@POST
	public Map<String,String> create(){
		String uuid = UUID.randomUUID().toString();
		Map<String,String> rv = new HashMap<>();
		rv.put("uuid", uuid);
		return rv;
	}
	
	
	@POST
	@Path("/{wallet}/addresses")
	public Map<String,List<String>> createAddresses(
			@PathParam("wallet") String wallet){
		Map<String,List<String>> rv = new HashMap<>();
		rv.put("addresses", new ArrayList<String>());
		rv.get("addresses").add(client.getnewaddress(wallet));
		return rv;
	}

}
