package org.beatcoin.dao;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;

import org.restnucleus.dao.Model;

@PersistenceCapable
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
public class Playlist extends Model{
	private static final long serialVersionUID = 8148709326509281147L;
	
	@Persistent
	@Index
	@Unique
	private String name;
	@Persistent
	private Boolean active;
	public String getName() {
		return name;
	}
	public Playlist setName(String name) {
		this.name = name;
		return this;
	}
	public Boolean getActive() {
		return active;
	}
	public Playlist setActive(Boolean active) {
		this.active = active;
		return this;
	}
	
	public void update(Model newInstance) {
		Playlist n = (Playlist)newInstance;
		if (null != n.getActive())this.setActive(n.getActive());
	}
	

}
