package com.cantina.lab

import spock.lang.Specification

/**
 * Tests of VideoMetadata
 *
 * @author Peter N. Steinmetz
 * Date: 8/8/13
 * Time: 6:25 PM
 */
class VideoMetadataSpec extends Specification {

  def "test conversion of duration string"() {
    expect:
    VideoMetadata.getPlaytimeFromString(str) == val

    where:
    str<< ["  Duration: 00:00:05.01, start: 0.000000, bitrate: 536 kb/s",
       "  Duration: 00:10:05.01 ",
       "  Duration: 01:02:05 "]
    val<< [5, 605, 3725]
  }

  def "test detection of video string"() {
    expect:
    VideoMetadata.hasVideoStream(str) == val

    where:
    str<< ['    Stream #0:0(eng): Video: h264 (High)',
           '    Stream #0:0(eng): Audio: aac ([64][0][0][0]']
    val<< [true, false]
  }
}
