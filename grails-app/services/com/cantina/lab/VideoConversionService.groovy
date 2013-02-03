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
	def log

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
	 * Execute a system command in a string.
	 *
	 * @param command to execute
	 * @return true if successful, false otherwise
	 */
	private boolean exec(String command) {
		try {
			log.debug "Executing $command"
			def out = new StringBuilder()
			def err = new StringBuilder()
			def proc = command.execute()

			def exitStatus = proc.waitForProcessOutput(out, err)
			if (out) log.debug "out:\n$out"
			if (err) log.debug "err:\n$err"

			log.debug "Process exited with status $exitStatus"

			return exitStatus == null || exitStatus == 0
		}
		catch (Exception e) {
			log.error("Error while executing command $command", e)
			return false
		}
	}

	/**
	 * Convert video in a source file into a target file while generating a thumbnail image.
	 *
	 * @param sourceVideo
	 * @param targetVideo
	 * @param thumb
	 * @return true if conversions successful, false otherwise.
	 */
	private boolean performConversion(File sourceVideo, File targetVideo, File thumb, VideoType targetType) {

		String convertedMovieFileExtension = mvals.ffmpeg.fileExtension
		boolean success = false

		switch (targetType) {
		
		case VideoType.FLV:
			String uniqueId = new UUID(System.currentTimeMillis(),
				System.currentTimeMillis() * System.currentTimeMillis()).toString()

			String tmpfile = mvals.location + uniqueId + "." + convertedMovieFileExtension

			File tmp = new File(tmpfile) //temp file for contents during conversion

			// :TODO: this will fail if pathnames contain spaces and should be redone with the array argument form
			String convertCmd = "${mvals.ffmpeg.path} -i ${sourceVideo.absolutePath} ${mvals.ffmpeg.conversionArgs} ${tmp.absolutePath}"
			String metadataCmd = "${mvals.yamdi.path} -i ${tmp.absolutePath} -o ${targetVideo.absolutePath} -l"
			String thumbCmd = "${mvals.ffmpeg.path} -i ${targetVideo.absolutePath} ${mvals.ffmpeg.makethumb} ${thumb.absolutePath}"

			success = exec(convertCmd) //kick off the command to convert movie to flv

			if (success) success = exec(metadataCmd) //kick off the command to add the metadata

			if (success) success = exec(thumbCmd) //kick off the command to create the thumb

			tmp.delete() //delete the tmp file
			break;
		
		case VideoType.MP4:
			def convertCmd = "${mvals.ffmpeg.path}"

			convertCmd+= " -i ${sourceVideo.absolutePath} ${mvals.ffmpeg.conversionArgs} ${targetVideo.absolutePath}"
			String metadataCmd = "${mvals.qtfaststart.path} ${targetVideo.absolutePath} ${targetVideo.absolutePath}.1"
			String deleteCmd = "rm -rf ${targetVideo.absolutePath}"
			String renameCmd = "mv ${targetVideo.absolutePath}.1 ${targetVideo.absolutePath}"
			String thumbCmd = "${mvals.ffmpeg.path} -i ${targetVideo.absolutePath} ${mvals.ffmpeg.makethumb} ${thumb.absolutePath}"

			success = exec(convertCmd) //kick off the command to convert movie to flv

			if (success) success = exec(metadataCmd) //kick off the command to generate/manipulate the metadata

			if (success) success = exec(deleteCmd) //kick off the command to generate/manipulate the metadata
			if (success) success = exec(renameCmd) //kick off the command to generate/manipulate the metadata

			if (success) success = exec(thumbCmd) //kick off the command to create the thumb
			break;
		}

		success
	}

	/**
	 * Extract playtime for a video file.
	 *
	 * @param file to extract playtime for
	 * @return playtime in seconds
	 * @throws Exception if failed to extract
	 */
	long extractVideoMetadata(Movie movie, String file) throws Exception {

		String command = "${mvals.ffprobe.path} ${mvals.ffprobe.params}" + file

		boolean success = exec(command)
		
		if (success) {
			String originalOutput = out.append(err).toString()

			def tokens = []
			originalOutput.splitEachLine(": ,\n") { line ->
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

			long res = tokens[i + 1].toString().toInteger() * 3600 + tokens[i + 2].toString().toInteger() * 60 + tokens[i + 3].toString().toFloat()
			return res
		}
		else {
			throw new IOException("Can't extract metadata")
		}
	}

}
