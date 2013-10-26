package org.beatcoin.pojo;

import java.math.BigDecimal;

public class Notification {
	
	
	private String address;
	private BigDecimal amount;
	private Long time;
	public String getAddress() {
		return address;
	}
	public Notification setAddress(String address) {
		this.address = address;
		return this;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public Notification setAmount(BigDecimal amount) {
		this.amount = amount;
		return this;
	}
	public Long getTime() {
		return time;
	}
	public Notification setTime(Long time) {
		this.time = time;
		return this;
	}
	
	

}
