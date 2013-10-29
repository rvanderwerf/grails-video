/* Copyright 2006-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information please visit www.cantinaconsulting.com
 * or email info@cantinaconsulting.com
 */
package com.cantina.lab

import java.io.File;

/**
 * Service to convert videos and obtain metadata from them.
 * 
 * @author Matt Chisholm
 * @author Adam Stachelek
 * @author Ryan Vanderwerf
 * @author Peter N. Steinmetz
 */
class VideoConversionService {
	
	def grailsApplication
	private def mvals
	
	/**
	 * Update configuration settings.
	 */
	void afterPropertiesSet() {
		mvals = grailsApplication.config.video
	}
	
	/**
	 * Create a thumbnail image from a source video file.
	 * 
	 * @param sourceVideo to create thumbnail from
	 * @param 
	 */
	boolean createThumbnail(File sourceVideo, File thumbFile) {
    afterPropertiesSet()
		def thumbCmdArr = [mvals.ffmpeg.path,"-y","-i",sourceVideo.absolutePath]
		mvals.ffmpeg.makethumb.tokenize(' ').each { arg -> thumbCmdArr << arg }
		thumbCmdArr << thumbFile.absolutePath
		def success = SysCmdUtils.exec(thumbCmdArr)
		success
	}
	
	/**
	 * Convert video in a source file into a target file while generating a thumbnail image.
	 *
	 * @param sourceVideo
	 * @param targetVideo
	 * @param thumb
	 * @return true if conversions successful, false otherwise.
	 */
	boolean performConversion(File sourceVideo, File targetVideo, File thumb, VideoType targetType) {
    afterPropertiesSet()
		boolean success = false
		
		File tmp = File.createTempFile("video","tmp")
		
		// ensure we have -y to override any existing file by the same name
		def convertCmdArr = [mvals.ffmpeg.path,"-y","-i", sourceVideo.absolutePath]
		mvals.ffmpeg.conversionArgs.tokenize(' ').each {arg -> convertCmdArr << arg }
		convertCmdArr << "-f" << targetType.extension << tmp.absolutePath
		success = SysCmdUtils.exec(convertCmdArr)
		
		// generation of metadata depends on type of conversion
		if (success) {
			switch (targetType) {
		
				case VideoType.FLV:
					def metadataCmdArr = [mvals.yamdi.path, "-i", tmp.absolutePath, "-o",targetVideo.absolutePath,"-l"]
					success = SysCmdUtils.exec(metadataCmdArr)
				break;
		
				case VideoType.MP4:
					def metadataCmdArr = [mvals.qtfaststart.path, tmp.absolutePath, targetVideo.absolutePath]
					success = SysCmdUtils.exec(metadataCmdArr)
				break;
			}
		}

		if (success) {
			success = createThumbnail(targetVideo,thumb)
		}
		
		tmp.delete() //delete the tmp file

		success
	}

  /**
   * Concatenate multiple input video files and convert to a single output video, which is made
   * quick-startable.
   *
   * @param inputs List of input Files
   * @param output output file
   * @param targetType enumeration of desired output type
   * @return true if concatenation successful, false otherwise
   */
  boolean concatVideos(List inputs,File output,VideoType targetType) {

    afterPropertiesSet()
    boolean success = false

    File tmp = File.createTempFile("video","tmp")

    // construct arguments for ffmpeg command, which uses a complex filter
    String istreamsSpec = ""
    for (i in 0..(inputs.size-1)) istreamsSpec = istreamsSpec + "[" + i + ":0] [" + i + ":1] "
    String filterSpec = istreamsSpec + "concat=n=" + inputs.size + ":v=1:a=1 [v] [a]"

    def args = [mvals.ffmpeg.path]
    inputs.each { entry ->
      args.push "-i"
      args.push entry.getAbsolutePath()
    }
    if (inputs.size > 1) {
      args.push "-filter_complex"
      args.push filterSpec
      args.push "-map"
      args.push "[v]"
      args.push "-map"
      args.push "[a]"
    }
    args.push "-vcodec"
    args.push "libx264"
    args.push "-y"   // use -y to override any existing file by the same name

    if (mvals.ffmpeg.concatArgs != '') {
      mvals.ffmpeg.concatArgs.tokenize(' ').each {arg -> args << arg }
    }

    args.push "-f"
    args.push targetType.extension
    args.push tmp.absolutePath

    success = SysCmdUtils.exec(args)

    // making quick startable depends on type of conversion
    if (success) {
      switch (targetType) {

        case VideoType.FLV:
          def metadataCmdArr = [mvals.yamdi.path, "-i", tmp.absolutePath, "-o",output.absolutePath,"-l"]
          success = SysCmdUtils.exec(metadataCmdArr)
          break;

        case VideoType.MP4:
          def metadataCmdArr = [mvals.qtfaststart.path, tmp.absolutePath, output.absolutePath]
          success = SysCmdUtils.exec(metadataCmdArr)
          break;
      }
    }

    tmp.delete() //delete the tmp file

    success
  }

  /**
   * Concatenate videos with Map of arguments.
   */
  boolean concatVideos(Map args) {
    concatVideos(args.inputs,args.output,args.targetType)
  }

  /**
	 * Extract playtime for a video file.
	 *
	 * @param videoFile to extract playtime for
	 * @return playtime in seconds
	 * @throws Exception if failed to extract
	 */
	long extractVideoPlaytime(File videoFile) throws Exception {
    afterPropertiesSet()
		def cmdArr = [mvals.ffprobe.path]
		if (mvals.ffprobe.params != '') {
			mvals.ffprobe.params(' ').each {arg -> cmdArr << arg }
		}
		cmdArr << videoFile.getAbsolutePath()
		
		try {
			def out = new StringBuilder()
			def err = new StringBuilder()
			
			def success = SysCmdUtils.exec(cmdArr,out,err)
						
			if (success) {
				String originalOutput = out.append(err).toString()
				long res = VideoMetadata.getPlaytimeFromString(originalOutput)
				if (res>=0) return res
				else throw new IOException("Can't parse playtime from string: " + originalOutput)
			} else {
				throw new IOException("Error status from ffprobe = " + exitStatus)
			}
		}
		catch (Exception e) {
			log.error("Error while executing command $cmdArr", e)
			throw e
		}
	}

  /**
   * Extract VideoMetadata for a file.
   *
   * @param videoFile to extract metadata for
   * @return VideoMetadata
   * @throws Exception if failed to extract
   */
  VideoMetadata extractMetadata(File videoFile) throws Exception {
    afterPropertiesSet()
    def cmdArr = [mvals.ffprobe.path]
    if (mvals.ffprobe.params != '') {
      mvals.ffprobe.params(' ').each {arg -> cmdArr << arg }
    }
    cmdArr << videoFile.getAbsolutePath()

    try {
      def out = new StringBuilder()
      def err = new StringBuilder()

      def success = SysCmdUtils.exec(cmdArr,out,err)

      if (success) {
        String originalOutput = out.append(err).toString()
        VideoMetadata res = new VideoMetadata()
        res.duration = VideoMetadata.getPlaytimeFromString(originalOutput)
        if (res.duration<0) log.info("Can't parse playtime from string: " + originalOutput)
        res.hasVideo = VideoMetadata.hasVideoStream(originalOutput)
        return res
      } else {
        throw new IOException("Error status from ffprobe = " + exitStatus)
      }
    }
    catch (Exception e) {
      log.error("Error while executing command $cmdArr", e)
      throw e
    }

  }

}
