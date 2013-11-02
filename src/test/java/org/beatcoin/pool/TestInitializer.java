package org.beatcoin.pool;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.beatcoin.BitcoinIServletConfig;
import org.beatcoin.pojo.Address;

public class TestInitializer extends PoolInitializer {
	
	private AddressPool addressPool;
	private String account;
	private final Map<String,Set<String>> data;

	public TestInitializer(Map<String,Set<String>> data) {
		super(null);
		this.data = data;
	}
	
	public void setPool(AddressPool addressPool){
		this.addressPool = addressPool;
	}
	
	public void setAccount(String account){
		this.account = account;
	}
	
	@Override
	public synchronized void start() {
		if (null==account){ // initialize all existing accounts
			synchronized(addressPool.getPools()){
				//filter all valid accounts
				for (String uuid: data.keySet()){
					if (uuid.length() - uuid.replace("-", "").length() == 4){
						addressPool.addPool(uuid);
					}
				}
				//for each account, get addresses
				for (Entry<String,Set<Address>> pool: addressPool.getPools().entrySet()){
					String uuid = pool.getKey();
					Set<String> addresses = data.get(uuid); 
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
				String address = "newAddress123";
				synchronized(addressPool.getPools()){
					pool.add(new Address().setAddress(address).setReserved(false));
				}
			}
			this.account = null;
		}
	}

}
