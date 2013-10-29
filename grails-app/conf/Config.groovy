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
  player.height = '260'
  player.width =  '320'
  flowplayer {
    version = "3.1.2"
  }
  swfobject {
    version = ""
  }

  // Settings for two possible systems as used in development.
  // MacOS X with ffmpeg and yamdi installed via brew command
  if (System.properties.getProperty("os.name")=="Mac OS X") {
    location="/tmp/"
    yamdi {
      path="/usr/local/bin/yamdi"
    }
    ffmpeg {
      fileExtension = "mp4"  // should be mp4 or flv
      conversionArgs = "-b 600k -r 24 -ar 44100 -ab 128k"
      concatArgs = "-crf 27 -threads 4"
      path="/usr/local/bin/ffmpeg"
      makethumb = "-an -ss 00:00:03 -an -r 2 -vframes 1 -y -f mjpeg"
    }
    ffprobe {
      path="/usr/local/bin/ffprobe"
      params=""
    }
    qtfaststart {
      path = "/usr/local/bin/qt-faststart"
    }
  }
  // other systems
  else {
    location="/tmp/"
    yamdi {
      path="/usr/bin/yamdi"
    }
    ffmpeg {
      fileExtension = "mp4"
      conversionArgs = "-b 600k -r 24 -ar 44100 -ab 128k"
      path="/usr/bin/ffmpeg"
      makethumb = "-an -ss 00:00:03 -an -r 2 -vframes 1 -y -f mjpeg"
    }
    ffprobe {
      path="/usr/bin/ffprobe"
      params=""
    }
    qtfaststart {
      path = "/usr/bin/qt-faststart"
    }
  }
}

// if you install this into several apps on the same database and only want some to process video, you can turn off here
video.enabled=true
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
