log4j = {
	error  'org.codehaus.groovy.grails',
	       'org.springframework',
	       'org.hibernate',
	       'net.sf.ehcache.hibernate'
	debug 'com.cantina.lab'
}

video {

    location="/tmp/"

    yamdi {
        path="/usr/bin/yamdi"
    }

    ffmpeg {
        fileExtension = "flv"
        conversionArgs = "-b 600k -r 24 -ar 22050 -ab 96k"
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
        path = "/usr/local/bin/qtfaststart"
    }
}

// if you install this into several apps on the same database and only want some to process video, you can turn off here
video.enabled=true
