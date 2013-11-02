package org.beatcoin.pool;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.beatcoin.BitcoinIServletConfig;
import org.beatcoin.pojo.Address;

import com._37coins.bcJsonRpc.BitcoindInterface;

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
