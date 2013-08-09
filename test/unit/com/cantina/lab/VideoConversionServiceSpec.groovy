package com.cantina.lab

import grails.test.mixin.TestFor
import spock.lang.Shared
import spock.lang.Specification

/**
 * Tests of VideoConversionService.
 * 
 * These depend on the video configuration variables being set properly.
 * 
 * @author Peter N. Steinmetz
 */
@TestFor(VideoConversionService)
class VideoConversionServiceSpec extends Specification {
	
	@Shared
  def testInputFile = new File("test/integration/resources/shortSamp.mp4")
	
	def setup() {
		service.afterPropertiesSet()
	}
	
	def "test extractVideoPlaytime"() {
		expect: 
		service.extractVideoPlaytime(testInputFile) == 5
	}
	
	def "test generation of thumbnail image"() {
		setup:
		def outputThumb = File.createTempFile("video", ".jpg")
		
		expect:
		service.createThumbnail(testInputFile,outputThumb)
		outputThumb.length() > 512 // at least 512 bytes for one frame
	}
		
	def "test video conversion to flash video FLV"() {
		setup:
		def outputFile = File.createTempFile("video", ".flv")
		def outputThumb = File.createTempFile("video", ".jpg")
		
		expect:
		service.performConversion(testInputFile,outputFile,outputThumb,VideoType.FLV)
		outputFile.length() >= testInputFile.length() - 1e3 // not more than 1K shorter
		outputThumb.length() > 512 // at least 512 bytes for one frame
		
		cleanup:
		outputFile.delete()
		outputFile.delete()
	}
	
	def "test video conversion to MP4 video"() {
		setup:
		def outputFile = File.createTempFile("video", ".mp4")
		def outputThumb = File.createTempFile("video", ".jpg")
		
		expect:
		service.performConversion(testInputFile,outputFile,outputThumb,VideoType.MP4)
		outputFile.length() >= testInputFile.length() - 1e3 // not more than 1K shorter
		outputThumb.length() > 512 // at least 512 bytes for one frame
		
		cleanup:
		outputFile.delete()
		outputThumb.delete()
	}

  def "test extractMetadata"() {
    expect:
    service.extractMetadata(inputFile) == vmd

    where:
    inputFile << [ testInputFile, new File("test/integration/resources/audioOnly.mp4") ]
    vmd << [ new VideoMetadata(duration: 5, hasVideo: true),
             new VideoMetadata(duration:5, hasVideo:false) ]
  }
}
