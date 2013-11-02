package org.beatcoin.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Notification {
	
	private String subject;
	private Payment payment;
	private String status;
	public String getSubject() {
		return subject;
	}
	public Notification setSubject(String subject) {
		this.subject = subject;
		return this;
	}
	public Payment getPayment() {
		return payment;
	}
	public Notification setPayment(Payment payment) {
		this.payment = payment;
		return this;
	}
	public String getStatus() {
		return status;
	}
	public Notification setStatus(String status) {
		this.status = status;
		return this;
	}
	
	

}
