grails.war.destFile = "grails-video.war"
grails.war.copyToWebAppLib = { args ->

}

grails.project.dependency.resolution = {
  // inherit Grails' default dependencies
  inherits("global") {
    // uncomment to disable ehcache
    // excludes 'ehcache'
  }
  log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
  repositories {

    mavenLocal()


    grailsPlugins()
    grailsHome()
    grailsRepo "http://plugins.grails.org"


    mavenCentral()

  }
  plugins {



  }
  dependencies {
    // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.


  }

}
