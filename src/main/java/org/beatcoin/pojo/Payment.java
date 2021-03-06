package org.beatcoin.pojo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Payment {
	
	private String address;
	private BigDecimal amount;
	private Long time;
	private String wallet;
	
	public String getAddress() {
		return address;
	}
	public Payment setAddress(String address) {
		this.address = address;
		return this;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public Payment setAmount(BigDecimal amount) {
		this.amount = amount;
		return this;
	}
	public Long getTime() {
		return time;
	}
	public Payment setTime(Long time) {
		this.time = time;
		return this;
	}
	public String getWallet() {
		return wallet;
	}
	public Payment setAccount(String wallet) {
		this.wallet = wallet;
		return this;
	}
	
	

}
