// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

if(System.properties["${appName}.config.location"]) {
    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
}
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

// log4j configuration
// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'
    debug   'com.cantina.lab'
}



video{

    location="/tmp/"
    
    yamdi{

        path="/usr/bin/yamdi"
    }

    ffmpeg  {
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



grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
// if you install this into several apps on the same database and only want some to process video, you can turn off here
video.enabled=true
