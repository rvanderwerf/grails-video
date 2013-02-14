package com.cantina.lab

import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.services.ServiceUnitTestMixin
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import spock.lang.Specification

/**
 * Tests of VideoService.
 *
 * These depend on the video configuration variables being set properly.
 *
 * @author Peter N. Steinmetz
 */

@TestFor(VideoService)
@Mock(Movie)
class VideoServiceSpec extends Specification {
	
	def testInputFile = new File("test/integration/resources/shortSamp.mp4")
	
	def setup() {
		service.videoConversionService = mockService(VideoConversionService)
		service.videoConversionService.afterPropertiesSet()
		service.afterPropertiesSet()
	}

	def "test creation of new movie with conversion"() {
		setup:
		Movie mov = new Movie()
		mov.newFile(testInputFile)
		
		expect:
		service.convertVideo(mov)
		def foundMovies = Movie.findAllByStatus("converted")
		foundMovies.size() == 1
		Movie found = foundMovies[0]
		found.pathMaster == testInputFile.getAbsolutePath()
		File flvFl = new File(found.pathFlv)
		flvFl.exists()
		File thumbFl = new File(found.pathThumb)
		thumbFl.exists()
		found.playTime == 5
		
		cleanup:
		service.deleteConversionProducts(mov)
	}
}
