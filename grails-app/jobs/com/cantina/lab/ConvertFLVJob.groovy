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

/**
 * Converts video.
 *
 * @author Matt Chisholm
 * @author Adam Stachelek
 */
class ConvertFLVJob {

	private static int numTries = 0
	private static final int MAX_ATTEMPTS = 10

	long timeout = 20000 // execute job about every 20 seconds

	static boolean running = false

	static triggers = {
		simple repeatInterval: 5000l // execute job once in 5s seconds
	}

	def grailsApplication
	def videoService

	def execute() {
		numTries++
		log.info "Attempt $numTries to run movie conversion."

		def config = grailsApplication.config
		if (!running && config.video.enabled) {
			try {
				running = true

				log.info "Starting to convert new movies to flv"

				long start = System.currentTimeMillis()

				videoService.convertNewVideo()

				long end = System.currentTimeMillis()

				log.info "Finished converting new files (time: ${(end - start)/1000} s)"
			}
			catch (Throwable t) {
				log.error "Error when converting movies.", t
			}
			running = false
			numTries = 0
		}
		else {
			log.info "Attempt Failed, conversion is in progress."
		}

		if (numTries > MAX_ATTEMPTS ) {
			log.warn "$numTries failed attempts have been made to run the move conversion. Unlocking."
			numTries = 0
			running = false
		}
	}
}
