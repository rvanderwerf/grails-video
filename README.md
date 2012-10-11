GrailsVideoPseudoStreamer plugin
===================================================

GrailsVideoPseudoStreamer(GVPS) Plugin, picked up and enhanced from original Cantina Consulting pre-1.0 Grails Plugin (Now compatible with Grails 2.x)

Video Plugin History –
	Abandonware created by Cantina Consulting made for Grails 1.0 RC1 - 2007
	Fixed up for Grails 1.3.X  by KPI Consulting (Ryan Vanderwerf and Shawn McKisson) – 2011
Also fixed streaming, used Tomcat byte range support so mp4 streaming will work
	Fixed up for Grails 2.0.x by Ryan – 2012
	Updated to use JWFLV 5.10
	Refactor MovieController, moved relevant streaming code to service
	Renamed plugin



You can help!
==============
Currently we need someone more familiar with flowplayer to upgrade the taglib to use the current version, which has
changed significantly.


TODOS:
- toggle related binary-based domain object so actual videos can be stored in DB (or no-SQL db) instead of FS
- toggle temp files being stored in working table in database, to avoid needing shared filesystem to process videos
- update html and styles on admin screens
- write actual unit tests
- convert taglibs to use gsp includes for flowplayer/jw-flv html
- add support for latest flowplayer versions



Security: Override either of these actions In MovieController
streamflv (Streaming FLV only)
display (mp4)

Add your security to your own actions with either Spring Security @Secured closure
Use SpringSecurity or other framework and protect via URI like
 Feel free to take what you need out of those methods or extend them to do any specialty work

FFMpeg (bundles with libavcodec) – quick overview
Parameters may vary based on version you are using

ex. conversionArgs = "-b 600k -r 24 -ar 22050 -ab 96k"
“-b 600k” set video bitrate of video to 600kbps
“-r 24” set framerate to 24 fps
“-ar 22050” audio sampling frequency rate
“-ab 96k” audio bit rate

Managing Video formats
Shells and runs ffmpeg to transcode video into common format
Users can upload anything you can throw at ffmpeg, the most common video transcoding engine available (You can plug something else in without tversoo much trouble)
Uses YAMDI to extract metadata from video
Uses ffprobe to extract mp4 data from footer and move to front of file to make streaming work
Has support out of the box for Flowplayer and Jw-Flv


Distribute the load
Uses Quartz plugin to transcode uploads
Pair with Clustered Quartz or Terracotta to distribute the load for large number of uploads (or offload to a separate set of servers that just process video)
See my distributed Quartz slides for setting up JDBCJobStore with clustering or Terracotta setup

JW-FLV Player

http://www.longtailvideo.com/players/jw-flv-player/

Open sourced flash and HTML5 video player, Cross platform playback

2 ways to embed:
	<!-- START OF THE PLAYER EMBEDDING TO COPY-PASTE -->
	<div id="mediaplayer">JW Player goes here</div>

	<script type="text/javascript" src="jwplayer.js"></script>
	<script type="text/javascript">
		jwplayer("mediaplayer").setup({
			flashplayer: "player.swf",
			file: "video.mp4",
			image: "preview.jpg"
		});
	</script>
	<!-- END OF THE PLAYER EMBEDDING -->


Or use taglib of plugin:
	    <vid:display movie='${movie}' player="jwflv" stream='true'/>

Parameters:

movie - movie domain object
id -id of movie object
stream - true/false stream or download
player - jwflv or flowplayer



Flowplayer

http://flowplayer.org/demos/plugins/streaming/index.html

Or Use taglib of plugin: (currently uses older version of flowplayer)
	    <vid:display movie='${movie}' player="flowplayer" stream='true'/>

Parameters:

movie - movie domain object
id -id of movie object
stream - true/false stream or download
player - jwflv or flowplayer


Configuration
-------------------
    video{
        location="/tmp/"  // or shared filesystem drive for a cluster
        yamdi{
            path="/usr/bin/yamdi"    // FLV metadata injector (IF TYPE= FLV)
        }
        ffmpeg  {
            fileExtension = "flv"  // use flv or mp4
            conversionArgs = "-b 600k -r 24 -ar 22050 -ab 96k"
            path="/usr/bin/ffmpeg"
            makethumb = "-an -ss 00:00:03 -an -r 2 -vframes 1 -y -f mjpeg"
        }
        ffprobe {
            path="/usr/bin/ffprobe" // finds length of movie
            params=""
        }
        flowplayer {
            version = "3.1.2" // use empty string for no version on file
        }
        swfobject {
            version = "" // used for jw-flv player
        }
        qtfaststart {
            path= “/usr/sbin/qtfaststart” // if type == mp4 rearrage metadata
        }
    }

Flowplayer Plugins

http://flowplayer.org/documentation/api/index.html
Flowplayer support various plugins to give special effects, enhanced control. They checked into google code project called 'flowplayer-plugins' and are broken up between flash and javascript plugins.
More info here: http://code.google.com/p/flowplayer-plugins/


More Information


https://github.com/rvanderwerf/grails-video
http://terracotta.org/downloads/open-source/catalog
http://flowplayer.org/
http://www.longtailvideo.com/players/jw-flv-player/
http://ffmpeg.org/
http://code.google.com/p/flowplayer-plugins/


Contact Me

Via twitter: https://twitter.com/RyanVanderwerf
Google+/email: rvanderwerf@gmail.com
Blog: http://rvanderwerf.blogspot.com





