/* Copyright 2006-2012 the original author or authors.
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

import org.springframework.beans.factory.InitializingBean
import org.springframework.transaction.annotation.Transactional

/**
 * Manages video assets.
 *
 * @author Matt Chisholm
 * @author Adam Stachelek
 */
class VideoService implements InitializingBean {

	static transactional = false

	def grailsApplication

	private mvals

	// Todo: research video capture from flash ie, red5

	void afterPropertiesSet() {
		buildLocalPath()
		mvals = grailsApplication.config.video
	}

	void buildLocalPath() {
		def f = new File(grailsApplication.config.video.location)
		if (!f.exists()) {
			f.mkdirs()
		}
	}

	boolean performConversion(File sourceVideo, File targetVideo, File thumb) {

		String convertedMovieFileExtension = mvals.ffmpeg.fileExtension
		boolean success = false

		if (convertedMovieFileExtension == "flv") {
			String uniqueId = new UUID(System.currentTimeMillis(),
				System.currentTimeMillis() * System.currentTimeMillis()).toString()

			String tmpfile = mvals.location + uniqueId + "." + convertedMovieFileExtension

			File tmp = new File(tmpfile) //temp file for contents during conversion

			String convertCmd = "${mvals.ffmpeg.path} -i ${sourceVideo.absolutePath} ${mvals.ffmpeg.conversionArgs} ${tmp.absolutePath}"
			String metadataCmd = "${mvals.yamdi.path} -i ${tmp.absolutePath} -o ${targetVideo.absolutePath} -l"
			String thumbCmd = "${mvals.ffmpeg.path} -i ${targetVideo.absolutePath} ${mvals.ffmpeg.makethumb} ${thumb.absolutePath}"

			success = exec(convertCmd) //kick off the command to convert movie to flv

			if (success) success = exec(metadataCmd) //kick off the command to add the metadata

			if (success) success = exec(thumbCmd) //kick off the command to create the thumb

			tmp.delete() //delete the tmp file
		}
		else if (convertedMovieFileExtension == "mp4") {

			String convertCmd = "${mvals.ffmpeg.path} -i ${sourceVideo.absolutePath} ${mvals.ffmpeg.conversionArgs} ${targetVideo.absolutePath}"
			String metadataCmd = "${mvals.qtfaststart.path} ${targetVideo.absolutePath} ${targetVideo.absolutePath}.1"
			String deleteCmd = "rm -rf ${targetVideo.absolutePath}"
			String renameCmd = "mv ${targetVideo.absolutePath}.1 ${targetVideo.absolutePath}"
			String thumbCmd = "${mvals.ffmpeg.path} -i ${targetVideo.absolutePath} ${mvals.ffmpeg.makethumb} ${thumb.absolutePath}"

			success = exec(convertCmd) //kick off the command to convert movie to flv

			if (success) success = exec(metadataCmd) //kick off the command to generate/manipulate the metadata

			if (success) success = exec(deleteCmd) //kick off the command to generate/manipulate the metadata
			if (success) success = exec(renameCmd) //kick off the command to generate/manipulate the metadata

			if (success) success = exec(thumbCmd) //kick off the command to create the thumb
		}

		success
	}

	@Transactional
	void putMovie(Movie movie) {
		movie.status = Movie.STATUS_NEW
		movie.save()
	}

	void deleteMovie(Movie movie) {
		delete(movie.pathMaster)
		delete(movie.pathThumb)
		delete(movie.pathFlv)
		movie.delete()
	}

	private void delete(String path) {
		File file = new File(path)
		if (file.exists()) file.delete()
	}

	@Transactional
	void convertVideo(Movie movie) {

		movie.status = Movie.STATUS_INPROGRESS
		movie.save(flush: true)

		File vid = new File(movie.pathMaster)

		//create unique file paths for assets created during conversion (flv and thumb)
		String convertedMovieFileExtension = mvals.ffmpeg.fileExtension
		String convertedMovieThumbnailExtension = "jpg"
		String convertedMovieFilePath = mvals.location + movie.key + "." + convertedMovieFileExtension
		String convertedMovieThumbnailFilePath = mvals.location + movie.key + "." + convertedMovieThumbnailExtension

		File flv = new File(convertedMovieFilePath)
		File thumb = new File(convertedMovieThumbnailFilePath)

		performConversion(vid, flv, thumb)

		movie.pathFlv = convertedMovieFilePath
		movie.pathThumb = convertedMovieThumbnailFilePath
		movie.size = flv.length()
		movie.contentType = mvals.ffmpeg.contentType

		movie.playTime = 0
		movie.createDate = new Date()
		movie.url = "/movie/display/" + movie.id

		extractVideoMetadata(movie, convertedMovieFilePath)

		if (flv.exists()) {
			movie.status = Movie.STATUS_CONVERTED
		}
		else {
			movie.status = Movie.STATUS_FAILED
		}

		movie.save()
	}

	void convertNewVideo() {
		log.debug "Querying for '$Movie.STATUS_NEW' movies."
		def results = Movie.findAllByStatus(Movie.STATUS_NEW)

		log.debug "Found ${results.size()} movie(s) to convert"

		//TODO: kick off coversions in parallel
		for (Movie movie in results) {
			log.debug "Converting movie with key $movie.key"
			convertVideo movie
		}
	}

	boolean exec(String command) {
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

	boolean extractVideoMetadata(Movie movie, String file) {

		// String command = "${mvals.ffprobe.path} -pretty -i " + file + " 2>&1 | grep \"Duration\" | cut -d ' ' -f 4 | sed s/,//"
		String command = "${mvals.ffprobe.path} ${mvals.ffprobe.params}" + file

		try {
			log.debug "Executing $command"
			def out = new StringBuilder()
			def err = new StringBuilder()
			def proc = command.execute()

			def exitStatus = proc.waitForProcessOutput(out, err)
			if (out) log.debug "out:\n$out"
			if (err) log.debug "err:\n$err"

			log.debug "Process exited with status $exitStatus"

			if (exitStatus == null || exitStatus == 0) {
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

				movie.playTime = tokens[i + 1].toString().toInteger() * 3600 + tokens[i + 2].toString().toInteger() * 60 + tokens[i + 3].toString().toFloat()
				return true
			}
			return false
		}
		catch (Exception e) {
			log.error("Error while executing command $command", e)
			return false
		}
	}
}
