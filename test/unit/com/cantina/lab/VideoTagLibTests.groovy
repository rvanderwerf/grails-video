package com.cantina.lab

import org.grails.plugin.resource.ResourceProcessor
import org.grails.plugin.resource.ResourceTagLib

import grails.test.mixin.TestFor
import grails.test.mixin.Mock

import com.google.protobuf.ByteString.Output
import org.junit.Before;

/**
 * Unit tests of VideoTagLib in the gvps plugin.
 */
@TestFor(VideoTagLib)
@Mock(Movie)
class VideoTagLibTests {

  Movie mov
	@Before
    void setUp() {
    mov = new Movie(title: "Test Movie",status:Movie.STATUS_CONVERTED).save()
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
    assert output.contains('<script src="http://releases.flowplayer.org/5.3.2/flowplayer.min.js"')

    rTagLibControl.verify()
  }

  void testJwflvDisplay() {
    // setup mock of ResourceTagLib for r: namespace in our tagLib
    def rTagLibControl = mockFor(ResourceTagLib)
    rTagLibControl.demand.resource(1..1) { attrs ->
       return "/static/plugins/" +  attrs.plugin + "/" + attrs.dir + "/" + attrs.file }
    def rTagLib = rTagLibControl.createMock()
    tagLib.metaClass.getR = { return rTagLib }

    def output = applyTemplate("<vid:display player='jwflv' id='${mov.id}'/>")
    assert output.contains("<p id='playerjwflv1null'>")
    assert output.contains("""so.addVariable('file','/movie/streamMp4/${mov.id}')""")

    rTagLibControl.verify()
  }

  void testFlowplayerDisplayWithoutAttributes() {
    def output = applyTemplate("<vid:display player='flowplayer' id='${mov.id}'/>")
    assert output.contains('<div class="flowplayer " >')
    assert output.contains("""<video src="/movie/streamMp4/${mov.id}" type="video/mp4" />""")
  }

  void testFlowplayerWithDivAttributes() {
    def output = applyTemplate("<vid:display player='flowplayer' id='${mov.id}' data-ratio='0.75'/>")
    assert output.contains('<div class="flowplayer " data-ratio="0.75" >')
    assert output.contains("""<video src="/movie/streamMp4/${mov.id}" type="video/mp4" />""")
  }

  void testFlowplayerWithVideoAttributes() {
    def output = applyTemplate("<vid:display player='flowplayer' id='${mov.id}' autoplay='' preload=''/>")
    assert output.contains('<div class="flowplayer " >')
    assert output.contains("""<video src="/movie/streamMp4/${mov.id}" type="video/mp4" autoplay preload />""")
  }

  void testFlowplayerWithDivClasses() {
    def output = applyTemplate("<vid:display player='flowplayer' id='${mov.id}' fixed-controls='' color-alt=''/>")
    assert output.contains('<div class="flowplayer fixed-controls color-alt " >')
    assert output.contains("""<video src="/movie/streamMp4/${mov.id}" type="video/mp4" />""")
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
