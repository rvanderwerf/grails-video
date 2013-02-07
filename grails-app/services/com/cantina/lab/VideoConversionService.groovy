/* Copyright 2006-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information please visit www.cantinaconsulting.com
 * or email info@cantinaconsulting.com
 */
package com.cantina.lab

import java.io.File;

/**
 * Service to convert videos and obtain metadata from them.
 * 
 * @author Matt Chisholm
 * @author Adam Stachelek
 * @author Ryan Vanderwerf
 * @author Peter N. Steinmetz
 */
class VideoConversionService {
	
	def grailsApplication
	private def mvals
	
	/**
	 * Update configuration settings.
	 */
	void afterPropertiesSet() {
		mvals = grailsApplication.config.video
	}

	/**
	 * Enumeration of the video types which can be converted to.
	 * @author peter
	 *
	 */
	public enum VideoType {
		FLV,
		MP4
	}
	
	/**
	 * Map of VideoType enumerations to ffmpeg type specifications.
	 */
	private def videoTypeExtensionMap = [(VideoType.FLV) : "flv", (VideoType.MP4) : "mp4"]
	
	/**
	 * Execute a system command from an array of command and arguments
	 *
	 * @param command to execute
	 * @return true if successful, false otherwise
	 */
	private boolean exec(List cmdArr) {
		try {
			log.debug "Executing $cmdArr"
			def out = new StringBuilder()
			def err = new StringBuilder()
			def proc = Runtime.getRuntime().exec((String[])cmdArr)

			def exitStatus = proc.waitForProcessOutput(out, err)
			if (out) log.debug "out:\n$out"
			if (err) log.debug "err:\n$err"

			log.debug "Process exited with status $exitStatus"

			return exitStatus == null || exitStatus == 0
		}
		catch (Exception e) {
			log.error("Error while executing command $cmdArr", e)
			return false
		}
	}

	/**
	 * Create a thumbnail image from a source video file.
	 * 
	 * @param sourceVideo to create thumbnail from
	 * @param 
	 */
	boolean createThumbnail(File sourceVideo, File thumbFile) {
		def thumbCmdArr = [mvals.ffmpeg.path,"-y","-i",sourceVideo.absolutePath]
		mvals.ffmpeg.makethumb.tokenize(' ').each { arg -> thumbCmdArr << arg }
		thumbCmdArr << thumbFile.absolutePath
		def success = exec(thumbCmdArr)
		success
	}
	
	/**
	 * Convert video in a source file into a target file while generating a thumbnail image.
	 *
	 * @param sourceVideo
	 * @param targetVideo
	 * @param thumb
	 * @return true if conversions successful, false otherwise.
	 */
	boolean performConversion(File sourceVideo, File targetVideo, File thumb, VideoType targetType) {

		boolean success = false
		
		File tmp = File.createTempFile("video","tmp")
		
		// ensure we have -y to override any existing file by the same name
		def convertCmdArr = [mvals.ffmpeg.path,"-y","-i", sourceVideo.absolutePath]
		mvals.ffmpeg.conversionArgs.tokenize(' ').each {arg -> convertCmdArr << arg }
		convertCmdArr << "-f" << videoTypeExtensionMap[targetType] << tmp.absolutePath
		success = exec(convertCmdArr)
		
		// generation of metadata depends on type of conversion
		if (success) {
			switch (targetType) {
		
				case VideoType.FLV:
					def metadataCmdArr = [mvals.yamdi.path, "-i", tmp.absolutePath, "-o",targetVideo.absolutePath,"-l"]
					success = exec(metadataCmdArr)
				break;
		
				case VideoType.MP4:
					def metadataCmdArr = [mvals.qtfaststart.path, tmp.absolutePath, targetVideo.absolutePath]
					success = exec(metadataCmdArr)
				break;
			}
		}

		if (success) {
			success = createThumbnail(targetVideo,thumb)
		}
		
		tmp.delete() //delete the tmp file

		success
	}

	/**
	 * Extract playtime from ffprobe output string.
	 * 
	 * Requires whitespace after last part of seconds.
	 * 
	 * @param outStr
	 * @return playtime in seconds, or -1 if not in string
	 */
	private long getPlaytimeFromString(String outStr) {
		def tokens = []
		outStr.splitEachLine(": ,\n") { line ->
			List list = line.toString().tokenize(": ,")
			list.each { item -> tokens << item }
		}

		int i
		int count = tokens.size()
		for (i = 0; i < count; i++) {
			if (tokens[i].toString().contains("Duration")) {
				break
			}
		}
		if (i>=count) return -1;	// can't find Duration

		long res = tokens[i + 1].toString().toInteger() * 3600 + tokens[i + 2].toString().toInteger() * 60 + tokens[i + 3].toString().toFloat()
		return res
	}
	
	/**
	 * Extract playtime for a video file.
	 *
	 * @param videoFile to extract playtime for
	 * @return playtime in seconds
	 * @throws Exception if failed to extract
	 */
	long extractVideoPlaytime(File videoFile) throws Exception {

		String[] cmdArr
		if (mvals.ffprobe.params != '') {
			cmdArr = [mvals.ffprobe.path, mvals.ffprobe.params, videoFile.getAbsolutePath()]
		} else {
			cmdArr = [mvals.ffprobe.path, videoFile.getAbsolutePath()]
		}
		
		try {
			log.debug "Executing $cmdArr"
			def out = new StringBuilder()
			def err = new StringBuilder()
			def proc = Runtime.getRuntime().exec(cmdArr)

			def exitStatus = proc.waitForProcessOutput(out, err)
			
			if (log.isDebugEnabled()) {
				if (out) log.debug "out:\n$out"
				if (err) log.debug "err:\n$err"
				log.debug "Process exited with status $exitStatus"
			}
			
			if (exitStatus==null || exitStatus==0) {
				String originalOutput = out.append(err).toString()
				long res = getPlaytimeFromString(originalOutput)
				if (res>=0) return res
				else throw new IOException("Can't parse playtime from string: " + originalOutput)
			} else {
				throw new IOException("Error status from ffprobe = " + exitStatus)
			}
		}
		catch (Exception e) {
			log.error("Error while executing command $cmdArr", e)
			throw e
		}
	}

}
