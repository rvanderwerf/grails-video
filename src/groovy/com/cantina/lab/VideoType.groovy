package com.cantina.lab;

/**
 * Enumeration of the video types which can be converted to.
 * 
 * @author Peter N. Steinmetz
 */
public enum VideoType {
	FLV ("flv"),
	MP4 ("mp4")
	
	private final String extension
	
	public VideoType(String fileExtension) {
		extension = fileExtension
	}
}
