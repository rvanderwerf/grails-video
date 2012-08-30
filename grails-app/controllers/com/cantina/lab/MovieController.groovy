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

import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse

/**
 * Adds video support.
 *
 * @author Matt Chisholm
 * @author Adam Stachelek
 */
class MovieController {

	/**
	 * The input buffer size to use when serving resources.
	 */
	private static final int INPUT_BUFFER_SIZE = 2048

	/**
	 * The output buffer size to use when serving resources.
	 */
	protected static final int OUTPUT_BUFFER_SIZE = 2048

	/**
	 * MIME multipart separation string
	 */
	protected static final String mimeSeparation = "CATALINA_MIME_BOUNDARY"

	VideoService videoService

	static defaultAction = 'list'

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

	def list = {
		if (!params.max) params.max = 10
		[movieList: Movie.list(params)]
	}

	def show = {
		[movie : Movie.get(params.id)]
	}

	def display = {

		def movie = Movie.get(params.id)
		if (movie.status != movie.STATUS_CONVERTED) return

		File movieFile = new File(movie.pathFlv)

		// Parse range specifier
		response.setHeader "Cache-Control", "no-store, must-revalidate"
		response.setHeader "Expires", "Sat, 26 Jul 1997 05:00:00 GMT"
		response.setHeader "Accept-Ranges", "bytes"
		List<Range> ranges = parseRange(movieFile)

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

	def thumb = {

		def movie = Movie.get(params.id)
		if (movie.status != Movie.STATUS_CONVERTED) return

		response.contentType = "image/jpeg"
		response.outputStream << new File(movie.pathThumb).newInputStream()
	}

	def streamflv = {

		def movie = Movie.get(params.id)
		if (movie.status != Movie.STATUS_CONVERTED) return

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

	def delete = {
		def movie = Movie.get(params.id)
		if (movie) {
			//add the movie with the video service
			videoService.deleteMovie(movie)
			flash.message = "Movie $params.id deleted."
		}
		else {
			flash.message = "Movie not found with id $params.id"
		}
		redirect action: 'list'
	}

	def create = {
		[movie: new Movie(params)]
	}

	def save = {

		def movie = new Movie(params)

		//get the submitted file
		def f = request.getFile('theFile')

		//create unique file path to digital master movie
		String vidFilePath = grailsApplication.config.video.location + movie.key + ".master"

		//create source file to hold store the file contents
		File masterVid = new File(vidFilePath)

		//save contents to digital master file
		f.transferTo(masterVid)

		//assign new master video properties
		movie.newFile(masterVid)

		//set option master params
		movie.contentTypeMaster = f.contentType
		movie.fileName = f.originalFilename
		// movie.createDate = new Date()
		// movie.contentType = f.contentType

		//add the digital master to the conversion queue save the movie object to db
		videoService.putMovie(movie)

		if (!movie.hasErrors()) {
			videoService.convertVideo(movie)
			flash.message = "Movie ${movie.id} created."
			redirect(action:show,id:movie.id)
		}
		else {
			render view: 'create', model: [movie:movie]
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
	private List<Range> parseRange(File myFile) throws IOException {

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
}
