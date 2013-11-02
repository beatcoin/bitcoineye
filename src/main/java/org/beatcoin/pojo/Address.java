package org.beatcoin.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
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
