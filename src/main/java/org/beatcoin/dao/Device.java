package org.beatcoin.dao;

import java.io.IOException;
import java.util.Set;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Serialized;

import org.beatcoin.pojo.Playlist;
import org.restnucleus.dao.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@PersistenceCapable
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
public class Device extends Model{
	private static final long serialVersionUID = -707859541424223150L;
	
	@Persistent
	@Index
	private String uuid;
	@Persistent
	private String streamUrl;
	@Persistent
	private Long lastConnected;
	@Persistent
	private Account owner;
	@Persistent
	@Serialized
	private String playlists;
	
	public String getUuid() {
		return uuid;
	}
	public Device setUuid(String uuid) {
		this.uuid = uuid;
		return this;
	}
	public String getStreamUrl() {
		return streamUrl;
	}
	public Device setStreamUrl(String streamUrl) {
		this.streamUrl = streamUrl;
		return this;
	}
	public Long getLastConnected() {
		return lastConnected;
	}
	public Device setLastConnected(Long lastConnected) {
		this.lastConnected = lastConnected;
		return this;
	}
	public Account getOwner() {
		return owner;
	}
	public Device setOwner(Account owner) {
		this.owner = owner;
		return this;
	}
	
	public String getPlaylists() {
		return playlists;
	}
	public Device setPlaylists(String playlists) {
		this.playlists = playlists;
		return this;
	}
	@JsonIgnore
	public Set<Playlist> getPlaylistSet() {
		if (null==playlists)
			return null;
		try {
			return new ObjectMapper().readValue(playlists, new TypeReference<Set<Playlist>>(){});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	@JsonIgnore
	public Device setPlaylistSet(Set<Playlist> playlists) {
		try {
			this.playlists = new ObjectMapper().writeValueAsString(playlists);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return this;
	}
	public void update(Model newInstance) {
		Device n = (Device)newInstance;
		if (null != n.getLastConnected())this.setLastConnected(n.getLastConnected());
		if (null != n.getStreamUrl())this.setStreamUrl(n.getStreamUrl());
	}
	

}
