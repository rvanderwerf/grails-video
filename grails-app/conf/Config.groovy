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

    location="/tmp/"
    player.height = '260'
    player.width =  '320'
    yamdi {
        path="/usr/bin/yamdi"
    }

    ffmpeg {
        fileExtension = "mp4"
        //fileExtension = "flv"

        //conversionArgs = "-b 600k -r 24 -ar 22050 -ab 96k"
        conversionArgs = "-b 600k -r 24 -ar 44100 -ab 128k"
        path="/usr/bin/ffmpeg"
        makethumb = "-an -ss 00:00:03 -an -r 2 -vframes 1 -y -f mjpeg"
    }
    ffprobe {
        path="/usr/bin/ffprobe"
        params=""
    }
    flowplayer {
        version = "3.1.2"
    }
    swfobject {
        version = ""
    }
    qtfaststart {
        path = "/usr/bin/qt-faststart"
    }
}

// if you install this into several apps on the same database and only want some to process video, you can turn off here
video.enabled=true
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
