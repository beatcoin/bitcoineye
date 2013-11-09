package org.beatcoin.dao;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.restnucleus.dao.Model;

@PersistenceCapable
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
public class Account extends Model{
	private static final long serialVersionUID = -860017262192514098L;
	
	@Persistent
	@Index
	private String email;
	@Persistent
	private String password;
	@Persistent
	private String payoutAddress;

	public String getEmail() {
		return email;
	}
	public Account setEmail(String email) {
		this.email = email;
		return this;
	}
	public String getPassword() {
		return password;
	}
	public Account setPassword(String password) {
		this.password = password;
		return this;
	}
	public String getPayoutAddress() {
		return payoutAddress;
	}
	public Account setPayoutAddress(String payoutAddress) {
		this.payoutAddress = payoutAddress;
		return this;
	}
	
	public void update(Model newInstance) {
		Account n = (Account) newInstance;
		if (null != n.getPayoutAddress())this.setPayoutAddress(n.getPayoutAddress());
	}
	
}
