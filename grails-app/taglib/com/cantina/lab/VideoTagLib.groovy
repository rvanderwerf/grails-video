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
 * For more information please visit http://grails.org/plugin/gvps.
 */
package com.cantina.lab

/**
 * Tag library for gvps plugin, mostly for rendering movies.
 *
 * @author Matt Chisholm
 * @author Adam Stachelek
 * @author Ryan Vanderwerf
 * @author Peter N. Steinmetz
 */
class VideoTagLib {

	static namespace = 'vid'

	private static final String TYPE_FLOWPLAYER = "flowplayer"
	private static final String TYPE_JWFLV = "jwflv"

	/**
	 * Render scripts and links to include appropriate player libraries.
	 * 
	 * @attr player (required). one of 'jwflv' or 'flowplayer'
	 * 
	 * :TODO: Should likely be replaced by resources modules which are then available 
	 * to the application.
	 */
	def includes = { attrs ->
        def player = attrs.player
        if (player == TYPE_JWFLV) {
		out << """\
            <script type='text/javascript' src="${r.resource(plugin:'gvps',dir:'jw-flv',file:'jwplayer.js')}"></script>
            <script type='text/javascript' src="${r.resource(plugin:'gvps',dir:'jw-flv',file:'swfobject.js')}"></script>
"""
        }
        if (player == TYPE_FLOWPLAYER) {
/*    out << """\
            <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.8/jquery.min.js"></script>
            <script src="http://releases.flowplayer.org/5.3.2/flowplayer.min.js"></script>
            <link rel="stylesheet" type="text/css" href="http://releases.flowplayer.org/5.3.2/skin/minimalist.css" />
"""  */
        }

	}

	/**
	 * Render script to display movie.
	 * 
	 * @attr id (optional) id of Movie object to render script for.
	 * @attr movie (optional) object to render script for. movie or id must be provided.
	 * @attr player (required) which player to use, must be 'jwflv' or 'flowplayer'
	 * @attr stream (optional) true if should stream data, otherwise will be downloaded
	 */
	def display = { attrs ->

		def movieId = attrs.id
		Movie movie = attrs.movie ?: Movie.get(movieId)
		def player = attrs.player
		def stream = attrs.stream


        if (!attrs.width) {
            if (grailsApplication?.config?.video?.player?.width) {
                attrs.width = grailsApplication?.config?.video?.player?.width
            } else {
                attrs.width = '320'
            }

        }
        if (!attrs.height) {
            if (grailsApplication?.config?.video?.player?.height) {
                attrs.height = grailsApplication?.config?.video?.player?.height
            } else {
                attrs.height = '260'
            }

        }
		
		if (player == TYPE_JWFLV) {
			if (movie.status != Movie.STATUS_CONVERTED) {
				out << "<p><b>FLV CONVERSION IN PROGRESS - Please refresh page</b></p>"
				return
			}

			String playerId = "player" + player + movie.id + stream

			if (stream == 'true') {
				out << """\
                    <p id='${playerId}'>
                    <a href='http://www.macromedia.com/go/getflashplayer'>Get Flash</a> to see this player.
                    </p>
                    <script type='text/javascript'>
                    var so = new SWFObject('${r.resource(plugin:'gvps',dir:'jw-flv',file:'player.swf')}','${playerId}',${attrs.width},${attrs.height},'7');
                    so.addVariable('file','${g.createLink(action: 'streamFlv', id: movie.id)}');
                    so.addParam('allowfullscreen','true');
                    so.addVariable('streamscript','${g.createLink(action: 'streamFlv', id: movie.id)}');
                    so.addVariable('image','${g.createLink(action: 'thumb', id: movie.id)}');
                    so.addVariable('provider','http');
                    so.write('${playerId}');
                   </script>"""
			}
			else {
				out << """\
                    <p id='${playerId}'>
                    <a href='http://www.macromedia.com/go/getflashplayer'>Get Flash</a> to see this player.
                    </p>
                    <script type='text/javascript'>
                    var so = new SWFObject('${r.resource(plugin:'gvps',dir:'jw-flv',file:'player.swf')}','${playerId}',${attrs.width},${attrs.height},'7');
                    so.addParam('allowfullscreen','true');
                    so.addVariable('file','${g.createLink(action: 'streamMp4', id: movie.id)}');
                    so.addVariable('image','${g.createLink(action: 'thumb', id: movie.id)}');
                    so.addVariable('provider','http');
                    so.write('${playerId}');
                   </script>"""
			}
		}
		else if (player == TYPE_FLOWPLAYER) {
			if (movie.status != Movie.STATUS_CONVERTED) {
				out << "<p><b>MP4 CONVERSION IN PROGRESS - Please refresh page</b></p>"
				return
			}

			String playerId = "player" + player + movie.id + stream

			if (stream == 'true') {
				out << """\
                <div class="flowplayer" data-debug="true">
                  <video src="${g.createLink(controller: 'movie', action: 'streamMp4', id: movie.id)}"></video>
                </div>
"""
			}
			else {
        out << """\
                <div class="flowplayer" data-debug="true">
                  <video src="${g.createLink(controller: 'movie', action: 'streamMp4', id: movie.id)}"></video>
                </div>
"""
			}
		}
	}

	/**
	 * Convert video length in seconds to human readable form.
	 * 
	 * @attr time length in seconds
	 */
	def convertVideoPlaytime = { attrs, body ->
		def num
		if (attrs.time instanceof java.lang.Number) num = attrs.time
		else num = Double.parseDouble(attrs.time)

		int minutes = num / 60 //automatically cast to int
		int seconds = num - minutes * 60 //subtract off whole minutes
		int hours = minutes / 60 //automatically cast to int
		minutes = minutes - hours * 60 //subtract off whole hours
		int days = hours / 24 //automatically cast to int
		hours = hours - days * 24 //subtract off whole days

		days > 1 ? out << days + " days " : days > 0 ? out << days + " day " : ""
		hours > 1 ? out << hours + " hours " : hours > 0 ? out << hours + " hour " : ""
		minutes > 1 ? out << minutes + " minutes " : minutes > 0 ? out << minutes + " minute " : ""
		seconds > 1 ? out << seconds + " seconds" : seconds > 0 ? out << seconds + " second" : ""
	}
}
