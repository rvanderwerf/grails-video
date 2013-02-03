/* Copyright 2006-2012 the original author or authors.
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

/**
 * Represents a video.
 *
 * @author Matt Chisholm
 * @author Adam Stachelek
 */
class Movie {

	static transients = ["theFile"]

	static mapping = {
		version false
		key column: 'video_key'
	}

	static final String STATUS_BLANK = "blank"
	static final String STATUS_REMOVED = "removed"
	static final String STATUS_NEW = "new"
	static final String STATUS_INPROGRESS = "inprogress"
	static final String STATUS_CONVERTED = "converted"
	static final String STATUS_FAILED = "failed"

	String url                 //url to the content source
	String title               //title of the content
	String fileName            //name of the file
	String description         //description of the content or abstract. should be short
	byte[] theFile             //binary data for the file
	Long size                  //size of content in bytes
	Date createDate            //date the content was created
	String createdBy           //the name of the person who created the content
	String pathFlv             //path to the flv file
	String pathThumb           //the path to the thumb file - jpg
	String pathMaster          //path to the digital master movie - i.e, mpeg, mov, etc...
	String contentType          //the content type of the movie - usually flv
	String contentTypeMaster   //the content type of the digital master movie
	Long playTime              //duration of the movie in number of seconds

	//unique key for the filenames on the file system
	String key = new UUID(System.currentTimeMillis(),
		System.currentTimeMillis()*System.currentTimeMillis()).toString()

	//status of the movie for the conversion process
	String status = STATUS_BLANK

	static constraints = {
		url(nullable:true)
		title(nullable:true)
		fileName(nullable:true)
		description(nullable:true)
		contentType(nullable:true)
		contentTypeMaster(nullable:true)
		theFile(nullable:true)
		pathFlv(nullable:true)
		pathMaster(nullable:true)
		pathThumb(nullable:true)
		size(nullable:true)
		createDate(nullable:true)
		createdBy(nullable:true)
		playTime(nullable:true)
	}

	void newFile(File masterFile) {
		//get the submitted file
		pathMaster = masterFile.canonicalPath
		size = masterFile.length()
		status = STATUS_NEW
	}
}
