package org.beatcoin.pool;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.beatcoin.BitcoinIServletConfig;
import org.beatcoin.pojo.Address;
import org.beatcoin.pojo.Notification;

import com._37coins.bcJsonRpc.BitcoindInterface;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PoolInitializer extends Thread {
	
	private final BitcoindInterface client;
	private AddressPool addressPool;
	private String account;
	
	@Inject
	public PoolInitializer(BitcoindInterface client){
		this.client = client;
	}
	
	public void setPool(AddressPool addressPool){
		this.addressPool = addressPool;
	}
	
	public void setAccount(String account){
		this.account = account;
	}
	
	@Override
	public void run() {
		if (null==account){ // initialize all existing accounts
			synchronized(addressPool.getPools()){
				Map<String, BigDecimal> accounts = client.listaccounts(0);
				//filter all valid accounts
				for (Entry<String,BigDecimal> account: accounts.entrySet()){
					String uuid = account.getKey();
					if (uuid.length() - uuid.replace("-", "").length() == 4){
						addressPool.addPool(uuid);
					}
				}
				//for each account, get addresses
				for (Entry<String,Set<Address>> pool: addressPool.getPools().entrySet()){
					String uuid = pool.getKey();
					List<String> addresses = client.getaddressesbyaccount(uuid);
					for (String address : addresses){
						addressPool.getPool(uuid).add(
								new Address().setAddress(address).setReserved(false));
					}
				}
			}
			addressPool.setInitialized();
			Notification n = new Notification().setSubject("status")
					.setStatus("pool initialized");
			HttpClient client = HttpClientBuilder.create().build();
			try {
				HttpPost httpPost = new HttpPost(BitcoinIServletConfig.notUrl);
				StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(n), Consts.UTF_8);
				entity.setContentType("application/json");
				httpPost.setEntity(entity);
				client.execute(httpPost);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("pool initialized");
		}else{ // add a new account
			Set<Address> pool = addressPool.addPool(account);
			for (int i = 0;i<BitcoinIServletConfig.poolSize;i++){
				String address = client.getnewaddress(account);
				synchronized(addressPool.getPools()){
					pool.add(new Address().setAddress(address).setReserved(false));
				}
			}
			this.account = null;
		}
	}

}
