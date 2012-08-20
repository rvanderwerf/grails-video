package com.cantina.lab
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.util.GrailsUtil

/* Copyright 2006-2007 the original author or authors.
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

/**
 * A background job that converts video for Grails.
 *
 * @author Matt Chisholm
 * @author Adam Stachelek
 */

class ConvertFLVJob {

	def timeout = 20000l     // execute job about every 20 seconds
	private static numTries = 0;
    private static final int MAX_ATTEMPTS = 10;
    static boolean running = false;

    static triggers = {
        simple repeatInterval: 5000l // execute job once in 5s seconds
    }
	def videoService

    def execute() {
        numTries++;
        log.info("Attempt " + numTries + " to run movie conversion.")

        def config = ConfigurationHolder.config
        if (!running && config.video.enabled) {
            try {
                running = true;

                log.info("Starting to convert new movies to flv")

                def start = System.currentTimeMillis()

                videoService.convertNewVideo()

                def end = System.currentTimeMillis()

                log.info("Finished converting new files (time: " + (end - start)/1000 + "s)")
        } catch (Throwable t) {
            log.error("Error when converting movies.",t);
        } finally {}
            running = false;
            numTries = 0;
        } else {
            log.info("Attempt Failed, conversion is in progress.")
        }

        if (numTries > MAX_ATTEMPTS ) {
            log.warn(numTries + " failed attempts have been made to run the move conversion. Unlocking.");
            numTries=0;
            running = false;
        }
	}
}
