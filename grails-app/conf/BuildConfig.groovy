grails.project.work.dir = 'target'
grails.project.source.level = 1.6

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
	}

	plugins {
		build(':release:2.0.4', ':rest-client-builder:1.0.2') {
			export = false
		}

		compile(":hibernate:$grailsVersion") {
			export = false
		}
        compile(":jquery:1.7.1") {
            export = false
        }
		compile ':quartz:1.0-RC2'
        runtime(":resources:1.2.RC2")
        runtime(":release:2.0.4")
        test(":spock:0.6")
    }
}
