package com.cantina.lab

import org.grails.plugin.resource.ResourceProcessor
import org.grails.plugin.resource.ResourceTagLib

import grails.test.mixin.TestFor

import com.google.protobuf.ByteString.Output;

/**
 * Unit tests of VideoTagLib in the gvps plugin.
 */
@TestFor(VideoTagLib)
class VideoTagLibTests {
	
	@Before
	void setUp() {
		
	}
	
    void tearDown() {
        // Tear down logic here
    }

    void testIncludes() {
		// setup mock of ResourceTagLib for r: namespace in our tagLib
		def rTagLibControl = mockFor(ResourceTagLib)
		rTagLibControl.demand.resource(2..2) { attrs -> return "/static/plugins/" + 
																 attrs.plugin + "/" +
																 attrs.dir + "/" + attrs.file }
		def rTagLib = rTagLibControl.createMock()
		tagLib.metaClass.getR = { return rTagLib }
		
		// no player defined
		assertOutputEquals('','<vid:includes/>')
		// JW-FLV
		def output = applyTemplate("<vid:includes player='jwflv'/>")
		assert output.contains('src="/static/plugins/gvps/jw-flv/jwplayer.js"')
		// Flowplayer
		output = applyTemplate("<vid:includes player='flowplayer'/>")
		assert output == ""
		
		rTagLibControl.verify()
    }
	
	void testJwflvDisplay() {
		
	}
	
	void testFlowplayerDisplay() {
		
	}
	
	void testConvertVideoPlaytime() {
		// test if provided with fractional number
		double val = (((1 * 24) + 3)*60 + 15)*60 + 27.5
		def tagStr = "<vid:convertVideoPlaytime time='${val}'/>"
		def output = applyTemplate(tagStr)
		assert output == "1 day 3 hours 15 minutes 27 seconds"
		
		// test with whole number
		tagStr = "<vid:convertVideoPlaytime time='15'/>"
		output = applyTemplate(tagStr)
		assert output == "15 seconds"
	}
}
