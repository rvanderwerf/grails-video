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
	
	/**
	 * Find a VideoType given its file extension.
	 * 
	 * @param ext
	 * @return VideoType with ext as extension, or null if none found.
	 */
	public static VideoType findByExtension(String ext) {
		VideoType res = null
		
		for (VideoType vt : VideoType.values()) {
			if (vt.extension.equalsIgnoreCase(ext)) {
				res = vt
				break;
			}
		}
		return res
	}
}
