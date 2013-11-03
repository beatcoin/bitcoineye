package org.beatcoin.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Song {
	
	private String id;
	private String title;
	private String artist;
	private String album;
	private Long length;
	
	public String getId() {
		return id;
	}
	public Song setId(String id) {
		this.id = id;
		return this;
	}
	public String getTitle() {
		return title;
	}
	public Song setTitle(String title) {
		this.title = title;
		return this;
	}
	public String getArtist() {
		return artist;
	}
	public Song setArtist(String artist) {
		this.artist = artist;
		return this;
	}
	public String getAlbum() {
		return album;
	}
	public Song setAlbum(String album) {
		this.album = album;
		return this;
	}
	public Long getLength() {
		return length;
	}
	public Song setLength(Long length) {
		this.length = length;
		return this;
	}
	
	

}
