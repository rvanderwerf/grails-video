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
	 * The input buffer fileSize to use when serving resources.
	 */
	private static final int INPUT_BUFFER_SIZE = 2048

	/**
	 * The output buffer fileSize to use when serving resources.
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
		[movieList: Movie.list(params),movieInstanceTotal:Movie.count()]
	}

	def show = {
		[movie : Movie.get(params.id)]
	}

	def streamMp4 = {

		def movie = Movie.get(params.id)
		if (movie.status != movie.STATUS_CONVERTED) return

		videoService.streamMp4(params, request, response, movie)
	}

	def thumb = {

		def movie = Movie.get(params.id)
		if (movie.status != Movie.STATUS_CONVERTED) return

		response.contentType = "image/jpeg"
		response.outputStream << new File(movie.pathThumb).newInputStream()
	}

	def streamFlv = {

		def movie = Movie.get(params.id)
		if (movie.status != Movie.STATUS_CONVERTED) return

		videoService.streamFlv(params,request,response,movie)
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



}
