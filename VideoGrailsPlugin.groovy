import org.codehaus.groovy.grails.commons.ApplicationHolder

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
*
* Current code base at: https://github.com/rvanderwerf/grails-video
* Contact rvanderwerf@gmail.com
*/

/**
 * Created by IntelliJ IDEA.
 * User: rvanderwerf / mchisholm
 * Date: Nov 9, 2007
 * To change this template use File | Settings | File Templates.
 */

class VideoGrailsPlugin {
        def version = "0.2"
        def author = "Ryan Vanderwerf and Cantina Consulting <www.cantinaconsulting.com>"
        def authorEmail = "rvanderwerf@gmail.com"
        def title = "Provides tools to host and manage video with grails"
        def documentation = "https://github.com/rvanderwerf/grails-video"
        def description = '''\
This plugin is written for the Groovy on Grails web application framework, and intends to make it relatively easy to host videos.  The goals of this plugin are as follows:

* Host, manage and display video assets
* Provide an easy mechanism to convert standard movie formats Quicktime, MPEG, etc. to the flash movie format FLV
* Perform movie conversions (i.e. MPEG -> FLV) or (MPEG -> MP4) asynchronously
* Provide options for JW-FLV or
*

This plugin uses serveral utilities to work with video assets. Please see https://github.com/rvanderwerf/grails-video for more information.
'''



        def doWithSpring = {
                // TODO Implement runtime spring config (optional)
        }
        def doWithApplicationContext = { applicationContext ->
                // TODO Implement post initialization spring config (optional)
        }
        def doWithWebDescriptor = { xml ->
                // TODO Implement additions to web.xml (optional)
        }
        def doWithDynamicMethods = { ctx ->
                // TODO Implement additions to web.xml (optional)
        }
        def onChange = { event ->
                // TODO Implement code that is executed when this class plugin class is changed
                // the event contains: event.application and event.applicationContext objects
        }
        def onApplicationChange = { event ->
                // TODO Implement code that is executed when any class in a GrailsApplication changes
                // the event contain: event.source, event.application and event.applicationContext objects
        }



}
