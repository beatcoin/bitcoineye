package org.beatcoin.pojo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Playlist implements Serializable{
	private static final long serialVersionUID = -1424743423107157828L;
	
	private String name;
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

}
