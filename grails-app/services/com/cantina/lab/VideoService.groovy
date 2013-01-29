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
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest

/**
 * Manages video assets.
 *
 * @author Matt Chisholm
 * @author Adam Stachelek
 * @author Ryan Vanderwerf
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

	private void buildLocalPath() {
		def f = new File(grailsApplication.config.video.location)
		if (!f.exists()) {
			f.mkdirs()
		}
	}

	private boolean performConversion(File sourceVideo, File targetVideo, File thumb) {

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
		}

		success
	}


	void putMovie(Movie movie) {
            Movie.withTransaction {
            movie.status = Movie.STATUS_NEW
            movie.save()
        }
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


	void convertVideo(Movie movie) {

        Movie.withTransaction {
            movie.status = Movie.STATUS_INPROGRESS
            movie.save(flush: true)
        }

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

        Movie.withTransaction() {
		    movie.save()
        }
	}

	/**
	 * Convert all videos whose status is 'new'.
	 */
	void convertNewVideo() {
		log.debug "Querying for '$Movie.STATUS_NEW' movies."
		def results = Movie.findAllByStatus(Movie.STATUS_NEW)

		log.debug "Found ${results.size()} movie(s) to convert"

		//TODO: kick off coversions in parallel   - create quartz job upon upload instead of polling
		for (Movie movie in results) {
			log.debug "Converting movie with key $movie.key"
			convertVideo movie
		}
	}

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

	private boolean extractVideoMetadata(Movie movie, String file) {

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



    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param cacheEntry The cache entry for the source resource
     * @param ostream The output stream to write to
     * @param ranges Enumeration of the ranges the client wanted to retrieve
     * @param contentType Content type of the resource
     * @throws IOException if an input/output error occurs
     */
    protected void copy(InputStream istream, ServletOutputStream ostream, Iterator<Range> ranges, String contentType) throws IOException {

        IOException exception
        while (exception == null && ranges.hasNext()) {

            Range currentRange = ranges.next()

            // Writing MIME header.
            ostream.println()
            ostream.println "--$mimeSeparation"
            if (contentType != null) {
                ostream.println "Content-Type: $contentType"
            }

            ostream.println "Content-Range: bytes ${currentRange.start}-${currentRange.end}/currentRange.length"
            ostream.println()

            // Printing content
            exception = copyRange(istream, ostream, currentRange.start, currentRange.end)

            istream.close()
        }

        ostream.println()
        ostream.print "--" + mimeSeparation + "--"

        if (exception) {
            throw exception
        }
    }



    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param cacheEntry The cache entry for the source resource
     * @param ostream The output stream to write to
     * @param range Range the client wanted to retrieve
     * @throws IOException if an input/output error occurs
     */
    protected void copy(InputStream istream, ServletOutputStream ostream, Range range) throws IOException {

        IOException exception = copyRange(istream, ostream, range.start, range.end)

        istream.close()

        if (exception) {
            throw exception
        }
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    private IOException copyRange(InputStream istream, ServletOutputStream ostream, long start, long end) {

        long skipped = 0
        try {
            skipped = istream.skip(start)
        }
        catch (IOException e) {
            return e
        }

        if (skipped < start) {
            return new IOException("start is less than skipped")
        }

        IOException exception
        long bytesToRead = end - start + 1

        byte[] buffer = new byte[INPUT_BUFFER_SIZE]
        int len = buffer.length
        while ((bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = istream.read(buffer)
                if (bytesToRead >= len) {
                    ostream.write(buffer, 0, len)
                    bytesToRead -= len
                }
                else {
                    ostream.write(buffer, 0, (int) bytesToRead)
                    bytesToRead = 0
                }
            }
            catch (IOException e) {
                exception = e
                len = -1
            }
            if (len < buffer.length) {
                break
            }
        }

        exception
    }

    /**
     * Parse the range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Vector of ranges
     */
    private List<Range> parseRange(HttpServletRequest request, HttpServletResponse response, File myFile) throws IOException {

        long fileLength = myFile.length()
        if (fileLength == 0) {
            return null
        }

        String rangeHeader = request.getHeader("Range")
        if (rangeHeader == null) {
            return null
        }

        // bytes is the only range unit supported (and I don't see the point of adding new ones)
        if (!rangeHeader.startsWith("bytes")) {
            response.addHeader "Content-Range", "bytes */$fileLength"
            response.sendError HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE
            return null
        }

        rangeHeader = rangeHeader.substring(6)

        // the ranges which are successfully parsed
        List<Range> result = []
        StringTokenizer commaTokenizer = new StringTokenizer(rangeHeader, ",")

        // Parsing the range list
        while (commaTokenizer.hasMoreTokens()) {
            String rangeDefinition = commaTokenizer.nextToken().trim()

            Range currentRange = new Range()
            currentRange.length = fileLength

            int dashPos = rangeDefinition.indexOf('-')
            if (dashPos == -1) {
                response.addHeader "Content-Range", "bytes */$fileLength"
                response.sendError HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE
                return null
            }

            if (dashPos == 0) {
                try {
                    long offset = Long.parseLong(rangeDefinition)
                    currentRange.start = fileLength + offset
                    currentRange.end = fileLength - 1
                }
                catch (NumberFormatException e) {
                    response.addHeader("Content-Range", "bytes */" + fileLength)
                    response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE)
                    return null
                }
            }
            else {
                try {
                    currentRange.start = Long.parseLong(rangeDefinition.substring(0, dashPos))
                    if (dashPos < rangeDefinition.length() - 1) {
                        currentRange.end = Long.parseLong(rangeDefinition.substring(dashPos + 1, rangeDefinition.length()))
                    }
                    else {
                        currentRange.end = fileLength - 1
                    }
                }
                catch (NumberFormatException e) {
                    response.addHeader "Content-Range", "bytes */$fileLength"
                    response.sendError HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE
                    return null
                }
            }

            if (!currentRange.validate()) {
                response.addHeader("Content-Range", "bytes */" + fileLength)
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE)
                return null
            }

            result.add(currentRange)
        }

        return result
    }

    private static class Range {

        long start
        long end
        long length

        boolean validate() {
            if (end >= length) {
                end = length - 1
            }
            return (start >= 0) && (end >= 0) && (start <= end) && (length > 0)
        }
    }


    void streamFlv(Map params, HttpServletRequest request, HttpServletResponse response, Movie movie) {
        int buffer = 8192
        int start = 0

        def jwFlvPosition = params.pos //jw-flv player seek param
        def flwplrStart = params.start //flowplayer seek param
        log.debug "$request.remoteAddr: request start pos=$params.start for movie id$movie.id"
        long seek = (jwFlvPosition) ? jwFlvPosition.toLong() : 0 //check for a seek from jw-flv
        if (!seek) seek = (flwplrStart) ? flwplrStart.toLong() : 0 //if no seek found check from flowplayer

        File movieFile = new File(movie.pathFlv)

        response.contentType = movie.contentType
        response.setHeader "Cache-Control", "no-store, must-revalidate"
        response.setHeader "Expires", "Sat, 26 Jul 1997 05:00:00 GMT"
        response.setHeader "Content-Length", movie.size.toString() //without this, firefox wont allow pseudostreaming
        def data = new byte[buffer]

        FileInputStream movieInputStream = new FileInputStream(movieFile)
        def responseStream = response.outputStream

        //This byte stream is a hearder that will be sent to the client upon a seek
        //This the decoding of this header is
        // byte array [70,76,86,1,1,0,0,0,9,0,0,0,9]
        // = [0x47,0x4c, 0x56, 1,1,0,0,0,9,0,0,0,9]
        // = ['f','l','v',1,1,0,0,0,9,0,0,0,9]
        def flvHeader = "RkxWAQEAAAAJAAAACQ==".decodeBase64()

        try {

            /* If the start value for pseudostreaming was passed in and greater than 0, then skip N bytes of the stream
               and return an FLV header reporting that we are seeking */
            if (seek.longValue() > 0) {
                movieInputStream.skip(seek)
                responseStream << flvHeader
                responseStream.flush()
            }

            int length = movieInputStream.read(data)
            while (length != -1) {
                responseStream << data
                responseStream.flush()
                length = movieInputStream.read(data)
            }
        }
        catch (Exception e) {
            log.error "Error while streaming video.", e
        }
        finally {
            responseStream.flush() // send any remaining bytes
            //flvout.close() // close the stream to flash
            movieInputStream.close() // close the file stream
        }


    }


    void streamMp4(Map params, HttpServletRequest request, HttpServletResponse response, Movie movie) {

        File movieFile = new File(movie.pathFlv)

        // Parse range specifier
        response.setHeader "Cache-Control", "no-store, must-revalidate"
        response.setHeader "Expires", "Sat, 26 Jul 1997 05:00:00 GMT"
        response.setHeader "Accept-Ranges", "bytes"
        List<groovy.lang.Range> ranges = parseRange(request, response, movieFile)

        ServletOutputStream oStream = response.outputStream

        if (!ranges) {
            //Full content response
            response.contentType = movie.contentType
            response.setHeader "Content-Length", movieFile.length().toString()
            oStream << movieFile.newInputStream()
        }
        else {
            // Partial content response.
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT)

            if (ranges.size() == 1) {

                groovy.lang.Range range = ranges[0]
                response.addHeader "Content-Range", "bytes ${range.start}-${range.end}/$range.length"
                long length = range.end - range.start + 1
                if (length < Integer.MAX_VALUE) {
                    response.setContentLength((int) length)
                }
                else {
                    // Set the content-length as String to be able to use a long
                    response.setHeader "content-length", length.toString()
                }

                response.contentType = movie.contentType

                try {
                    response.setBufferSize(OUTPUT_BUFFER_SIZE)
                }
                catch (IllegalStateException e) {
                    // Silent catch
                }

                if (oStream) {
                    copy(movieFile.newInputStream(), oStream, range)
                }
            }
            else {
                response.setContentType "multipart/byteranges; boundary=$mimeSeparation"

                try {
                    response.setBufferSize(OUTPUT_BUFFER_SIZE)
                }
                catch (IllegalStateException e) {
                    // Silent catch
                }
                if (oStream) {
                    copy(movieFile.newInputStream(), oStream, ranges.iterator(), movie.contentType)
                }
                else {
                    // we should not get here
                    throw new IllegalStateException()
                }
            }
        }



    }
}
