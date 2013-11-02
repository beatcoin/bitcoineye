package org.beatcoin.pojo;

public class Address {
	
	private String address;
	private boolean reserved;
	
	public String getAddress() {
		return address;
	}
	public Address setAddress(String address) {
		this.address = address;
		return this;
	}
	public boolean isReserved() {
		return reserved;
	}
	public Address setReserved(boolean reserved) {
		this.reserved = reserved;
		return this;
	}
	
	

}
