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
 *
 * Current code base at: https://github.com/rvanderwerf/grails-video
 * Contact rvanderwerf@gmail.com
 */

class VideoGrailsPlugin {

	def version = "0.2"
	def grailsVersion = '2.0 > *'
	def author = "Ryan Vanderwerf"
	def authorEmail = "rvanderwerf@gmail.com"
	def title = "Video Plugin"
	def documentation = "https://github.com/rvanderwerf/grails-video"
	def description = '''\
This plugin is written for the Grails web application framework, and intends to make it relatively easy to host videos. The goals of this plugin are as follows:

* Host, manage and display video assets
* Provide an easy mechanism to convert standard movie formats Quicktime, MPEG, etc. to the flash movie format FLV
* Perform movie conversions (i.e. MPEG -> FLV) or (MPEG -> MP4) asynchronously
* Provide options for JW-FLV or Flowplayer
*

This plugin uses serveral utilities to work with video assets. Please see https://github.com/rvanderwerf/grails-video for more information.
'''

	def license = 'APACHE'
	def developers = [
            [name:  'Ryan Vanderwerf', email: 'rvanderwerf@gmail.com'],
            [name: 'Cantina Consulting <www.cantinaconsulting.com>', email: 'info@cantinaconsulting.com']]
//	def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPVIDEO'] TODO
	def scm = [url: 'https://github.com/rvanderwerf/grails-video']
}
