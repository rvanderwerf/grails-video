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

class GvpsGrailsPlugin {

	def version = "0.3"
	def grailsVersion = '1.3.4 > *'
	def author = "Ryan Vanderwerf"
	def authorEmail = "rvanderwerf@gmail.com"
	def title = "GVPS (Grails Video Pseudo Streamer) Plugin"
	def documentation = "https://github.com/rvanderwerf/grails-video"
	def description = '''\
This Grails web application plugin makes it relatively easy to host videos.
'''

	def license = 'APACHE'
	def developers = [
            [name:  'Ryan Vanderwerf', email: 'rvanderwerf@gmail.com'],
			[name: 'Peter N. Steinmetz', email: 'ndoc3@steinmetz.org'],
            [name: 'Cantina Consulting <www.cantinaconsulting.com>', email: 'info@cantinaconsulting.com']]
//	def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPGVPS'] TODO
	def scm = [url: 'https://github.com/rvanderwerf/grails-video']
}
