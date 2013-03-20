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

import org.springframework.beans.factory.InitializingBean
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest

/**
 * Manages Movies.
 *
 * @author Matt Chisholm
 * @author Adam Stachelek
 * @author Ryan Vanderwerf
 * @author Peter N. Steinmetz
 */
class VideoService implements InitializingBean {

	static transactional = false

	def grailsApplication
	def videoConversionService

  // buffer size for servlet response buffer
  static int responseBufferSize = 1024*16

  // buffer size for transfer buffer
  static int transferBufferSize = 1024*16

  // :TODO: should really be chosen as a random or a UUID or check content to ensure no overlap
  static String mimeSeparation = "gvps-mime-boundary"

	// copy of the video configuration settings
	private mvals

	// Todo: research video capture from flash ie, red5

	/**
	 * Update configuration settings and rebuild storage path, if needed.
	 */
	void afterPropertiesSet() {
		buildLocalPath()
		mvals = grailsApplication.config.video
	}

	/**
	 * Construct local file storage if doesn't exist,
	 * as configured by the video.location variable.
	 */
	private void buildLocalPath() {
		def f = new File(grailsApplication.config.video.location)
		if (!f.exists()) {
			f.mkdirs()
		}
	}

	/**
	 * Save movie into database, marking as needing conversion.
	 * 
	 * @param movie to store
	 */
	void putMovie(Movie movie) {
        Movie.withTransaction {
            movie.status = Movie.STATUS_NEW
            movie.save()
        }
	}

	/**
	 * Delete movie from database and storage files.
	 * 
	 * @param movie to delete
	 */
	void deleteMovie(Movie movie) {
		delete(movie.pathMaster)
		deleteConversionProducts(movie)
		movie.delete()
	}
	
	/**
	 * Delete video products of conversion.
	 */
	void deleteConversionProducts(Movie movie) {
		delete(movie.pathThumb)
		delete(movie.pathFlv)
	}
	
	/**
	 * Delete a file at a path, working even if file doesn't exist.
	 * 
	 * @param path to delete
	 */
	private void delete(String path) {
		if (path==null) return
		File file = new File(path)
		if (file.exists()) file.delete()
	}

	/**
	 * Get VideoType that we are converting to based on configuration.
	 */
	VideoType getConversionVideoType() {
		String convertedMovieFileExtension = mvals.ffmpeg.fileExtension
		VideoType.findByExtension(convertedMovieFileExtension)
	}
	
	/**
	 * Setup paths for conversion from Movie.pathMaster
	 * 
	 * @param mov Movie to set conversions paths for
	 */
	private void setupConversionPaths(Movie mov) {
		
		//create unique file paths for assets created during conversion (flv and thumb)
		String convertedMovieFileExtension = mvals.ffmpeg.fileExtension
		File mvalsLocationFile = new File(mvals.location)
		String convertedMovieThumbnailExtension = "jpg"
		String convertedMovieName = mov.key + "." + convertedMovieFileExtension
		String convertedMovieThumbnailName = mov.key + "." + convertedMovieThumbnailExtension

		File flv = new File(mvalsLocationFile, convertedMovieName)
		File thumb = new File(mvalsLocationFile, convertedMovieThumbnailName)
		
		mov.pathFlv = flv.getCanonicalPath()
		mov.pathThumb = thumb.getCanonicalPath()
	}
	
	/**
	 * Fill in playTime from converted video.
	 * 
	 * @param movie to fill playTime in.
	 */
	private void fillPlayTime(Movie movie) {
		File convVideo = new File(movie.pathFlv)
		try {
			movie.playTime = videoConversionService.extractVideoPlaytime(convVideo)
		} catch (Exception e) {
			log.warn("Can't extract video metadata for file " + convVideo.getAbsolutePath())
		}
	}
	
	/**
	 * Fill in size, type, date and url for a Movie.
	 */
	private void fillSizeTypeDateUrl(Movie movie) {
		movie.fileSize = new File(movie.pathFlv).length()
		movie.contentType = getConversionVideoType().extension

		movie.createDate = new Date()
		movie.url = "/movie/display/" + movie.id

		fillPlayTime(movie)
	}
	
	/**
	 * Convert Movie to format specified by the video.ffmpeg.fileExtension variable.
	 * 
	 * @param Movie to convert, assumed to have valid pathMaster field.
	 */
	void convertVideo(Movie movie) {

        Movie.withTransaction {
            movie.status = Movie.STATUS_INPROGRESS
            movie.save(flush: true)
        }

		File vid = new File(movie.pathMaster)
		
		setupConversionPaths(movie)
		
		File convVideo = new File(movie.pathFlv)
		videoConversionService.performConversion(vid, convVideo, new File(movie.pathThumb), getConversionVideoType())

		fillSizeTypeDateUrl(movie)
		
		if (convVideo.exists()) {
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
	 * Import a pre-converted video into the system, creating a Movie object.
	 * 
	 * Moves the file, creates a thumbnail, and updates the database.
	 * 
	 * @return Movie object
	 */
	Movie importConvertedVideo(File convertedVideoFile) {
		
		def movie = new Movie()
		movie.newFile(convertedVideoFile)
		movie.save(flush:true)
		
		movie.withTransaction {
			
			movie.status = Movie.STATUS_INPROGRESS
			movie.save(flush: true)
			
			setupConversionPaths(movie)
			
			// move file into storage area, which can run quickly if input 
			// and storage area are on same volume
			def cmdArr =[ "mv", convertedVideoFile.getAbsolutePath(), movie.pathFlv ]
			if (!SysCmdUtils.exec(cmdArr)) {
				log.error("Unable to copy converted movie: " + movie.pathMaster)
				movie.status = Movie.STATUS_FAILED
				movie.save(flush:true)
			}
			
			if (movie.status == Movie.STATUS_INPROGRESS) {
				File thumbFile = new File(movie.pathThumb)
				if (!videoConversionService.createThumbnail(new File(movie.pathFlv),thumbFile)) {
					log.error("Can't create thumbnail file for video:"+movie.pathMaster)
					movie.status = Movie.STATUS_FAILED
					movie.save(flush:true)
				}
      }
			
			if (movie.status == Movie.STATUS_INPROGRESS) {
				fillSizeTypeDateUrl(movie)	
				fillPlayTime(movie)
			}
			
			if (movie.status == Movie.STATUS_INPROGRESS) {
        movie.status = Movie.STATUS_CONVERTED
        movie.save()
			}
		}
  
    return movie
	}
	
	/**
	 * Convert all movies whose status is 'new'.
	 */
	void convertNewVideo() {
		log.debug "Querying for '$Movie.STATUS_NEW' movies."
		def results = Movie.findAllByStatus(Movie.STATUS_NEW)

		log.debug "Found ${results.size()} movie(s) to convert"

		//TODO: kick off conversions in parallel   - create quartz job upon upload instead of polling
		for (Movie movie in results) {
			log.debug "Converting movie with key $movie.key"
			convertVideo movie
		}
	}
	
    /**
     * Copy ranges of content of the specified input stream to the specified
     * output stream, and ensure that the input stream is closed before returning
     * (even in the face of an exception).
     *
     * @param istream InputStream to read data from
     * @param ostream ServletOutputStream to write to
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

            ostream.println "Content-Range: bytes ${currentRange.start}-${currentRange.end}/${currentRange.length}"
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
     * Copy a range of content of the specified input stream to the specified
     * output stream, and ensure that the input stream is closed before returning
     * (even in the face of an exception).
     *
     * @param instream InputStream to read from
     * @param ostream ServletOutputStream to write to
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
     * Copy a range of contents of the specified input stream to the specified
     * output stream.
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing or null if none encountered
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

        byte[] buffer = new byte[transferBufferSize]
        int validBytes = buffer.length
        while ((bytesToRead > 0) && (validBytes >= buffer.length)) {
            try {
                validBytes = istream.read(buffer)
                // if at end of input stream
                if (validBytes<0) {
                  exception = new IOException("Attempt to read past end of input.")
                }
                // if all bytes read should be written
                else if (bytesToRead >= validBytes) {
                    ostream.write(buffer, 0, validBytes)
                    bytesToRead -= validBytes
                }
                // otherwise only write those requested
                else {
                    ostream.write(buffer, 0, (int) bytesToRead)
                    bytesToRead = 0
                }
            }
            catch (IOException e) {
                exception = e
                validBytes = -1
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

	/**
	 * Range of content to serve.
   *
   * These ranges are inclusive, the byte at offset end is part of the range.
	 */
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

	/**
	 * Stream contents of a Movie as an flv file.
	 * 
	 * @param params which may contain pos or start attributes
	 * @param request servlet request
	 * @param response servlet response
	 * @param movie to stream
	 */
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

	/**
	 * Stream contents of a Movie as a mp4 file.
	 *
	 * @param params which are unused
	 * @param request servlet request
	 * @param response servlet response
	 * @param movie to stream
	 */
    void streamMp4(Map params, HttpServletRequest request, HttpServletResponse response, Movie movie) {
        logRequest(request)

        File movieFile = new File(movie.pathFlv)

        // Parse range specifier
        response.setHeader "Cache-Control", "no-store, must-revalidate"
        response.setHeader "Expires", "Sat, 26 Jul 1997 05:00:00 GMT"
        response.setHeader "Accept-Ranges", "bytes"
        List<Range> ranges = parseRange(request, response, movieFile)

        ServletOutputStream oStream = response.outputStream

        // :TODO: a temporary fixed value, which should reflect the movie.contentType
        String contentType = "video/mp4"

        if (!ranges) {
            //Full content response
            response.contentType = contentType
            response.setHeader "Content-Length", movieFile.length().toString()
            oStream << movieFile.newInputStream()
        }
        else {
            // Partial content response.
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT)

            if (ranges.size() == 1) {

                Range range = ranges[0]
                response.addHeader "Content-Range", "bytes ${range.start}-${range.end}/$range.length"
                long length = range.end - range.start + 1
                if (length < Integer.MAX_VALUE) {
                    response.setContentLength((int) length)
                }
                else {
                    // Set the content-length as String to be able to use a long
                    response.setHeader "content-length", length.toString()
                }

                response.contentType = contentType

                try {
                    response.setBufferSize(responseBufferSize)
                }
                catch (IllegalStateException e) {
                    log.warn("Can't set HttpServletResponse buffer size.",e)
                }

                if (oStream) {
                    copy(movieFile.newInputStream(), oStream, range)
                }
            }
            else {
                response.setContentType "multipart/byteranges; boundary=$mimeSeparation"

                try {
                    response.setBufferSize(responseBufferSize)
                }
                catch (IllegalStateException e) {
                  log.warn("Can't set HttpServletResponse buffer size.",e)
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

    /**
     * Log contents of the request at debug level.
     */
    private void logRequest(HttpServletRequest request) {
      log.debug("RequestUrl:"+request.getRequestURL().toString())
      Enumeration<String> headerNames = request.getHeaderNames()
      headerNames.each{ String hdrName ->
        Enumeration<String> headerVals = request.getHeaders(hdrName)
        StringBuilder vals = new StringBuilder()
        headerVals.eachWithIndex { String val, int i ->
          if (i!=0) vals.append(',')
          vals.append(val)
        }
        log.debug("Header:" + hdrName + ":" + vals.toString())
      }
    }
}
