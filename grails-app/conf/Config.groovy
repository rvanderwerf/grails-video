grails.doc.title = "GVPS (Grails Video Pseudo Streaming) Plugin"
grails.doc.authors = 'Ryan Vanderwerf, Peter N. Steinmetz'

log4j = {
	error  'org.codehaus.groovy.grails',
	       'org.springframework',
	       'org.hibernate',
	       'net.sf.ehcache.hibernate'
	debug 'com.cantina.lab'
}

video {
	location="/tmp/"  // location for storage of videos, can be on a shared drive
	yamdi{
		path="/usr/local/bin/yamdi"    // FLV metadata injector (IF TYPE= FLV)
	}
	ffmpeg  {
		fileExtension = "mp4"  // use flv or mp4
		// conversion args should not include an output format
		conversionArgs = "-b 600k -r 24 -ar 22050 -ab 96k"
		path="/usr/local/bin/ffmpeg"
		makethumb = "-an -ss 00:00:03 -an -r 2 -vframes 1 -y -f mjpeg"
	}
	ffprobe {
		path="/usr/local/bin/ffprobe" // finds length of movie
		params=""
	}
	flowplayer {
		version = "3.1.2" // use empty string for no version on file
	}
	swfobject {
		version = "" // used for jw-flv player, empty to not specify version
	}
	qtfaststart {
		path = "/usr/local/bin/qt-faststart" // if type == mp4 used to rearrange metadata
	}
}

// if you install this into several apps on the same database and only want some to process video, you can turn off here
video.enabled=true
