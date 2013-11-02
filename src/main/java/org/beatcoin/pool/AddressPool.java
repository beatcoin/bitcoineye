package org.beatcoin.pool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.beatcoin.pojo.Address;

public class AddressPool {
	
	private final Map<String,Set<Address>> pools;
	private boolean initialized;
	
	@Inject
	public AddressPool(PoolInitializer poolInitializer){
		pools = new HashMap<>();
		initialized = false;
		poolInitializer.setPool(this);
		poolInitializer.start();
	}
	
	/*
	 * reserve an address for for the queue
	 * 
	 * label: the account the address belongs to
	 * 
	 * returns:
	 * address: next available address
	 * null: none available
	 */
	public String reserveAddress(String account) throws NotInitializedException{
		if (!initialized){
			throw new NotInitializedException();
		}
		synchronized(pools){
			Set<Address> pool = pools.get(account);
			for (Address address: pool){
				if (!address.isReserved()){
					address.setReserved(true);
					return address.getAddress();
				}
			}
		}
		return null;
	}
	
	/*
	 * releases an address from use, typically when a song moves from queue to play
	 * 
	 * label: the account the address belongs to
	 * address: the address concerned
	 * 
	 * returns:
	 * true: if address released successfully
	 * false: pool, address not found, or address free already
	 */
	public boolean releaseAddress(String account, String address) throws NotInitializedException{
		if (!initialized){
			throw new NotInitializedException();
		}
		synchronized(pools){
			if (pools.containsKey(account)){
				Set<Address> pool = pools.get(account);
				for (Address addr: pool){
					if (addr.getAddress().equals(address)
							&& addr.isReserved()==true){
						addr.setReserved(false);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected HashSet<Address> addPool(String label){
		HashSet<Address> pool = new HashSet<Address>();
		synchronized(pools){
			pools.put(label, pool);
		}
		return pool;
	}
	
	protected Set<Address> getPool(String label){
		return pools.get(label);
	}
	
	protected Map<String,Set<Address>> getPools(){
		return pools;
	}
	
	protected void setInitialized(){
		this.initialized=true;
	}
	
	@Override
	public String toString(){
		if (initialized){
			StringBuilder sb = new StringBuilder();
			for (Entry<String,Set<Address>> pool:pools.entrySet()){
				sb.append("pool: "+pool.getKey()+" with "+pool.getValue().size()+" entries\n");
			}
			return sb.toString();
		}else{
			return "not initialized";
		}
	}

}
