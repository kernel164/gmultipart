/*
 * Copyright 2002-2006 the original author or authors.
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
 */

package org.gmr.web.multipart;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>This src is modified from {@link org.springframework.web.multipart.commons.CommonsMultipartFile CommonsMultipartFile}
 * and renamed to GMultipartResolver to make it work in GAE.
 *
 * <p>MultipartFile implementation for Jakarta Commons FileUpload.
 *
 * <p>
 * <b>NOTE:</b> As of Spring 2.0, this class requires Commons FileUpload 1.1 or higher. The
 * implementation does not use any deprecated FileUpload 1.0 API anymore, to be compatible with
 * future Commons FileUpload releases.
 *
 * @author kernel164
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @see GMultipartResolver
 */
public class GMultipartFile implements MultipartFile, Serializable {

	private static final long serialVersionUID = -2270490133870606L;

	protected static final Log logger = LogFactory.getLog(GMultipartFile.class);

	private final FileItem fileItem;

	private final long size;

	/**
	 * Create an instance wrapping the given FileItem.
	 *
	 * @param fileItem the FileItem to wrap
	 */
	public GMultipartFile(FileItem fileItem) {
		this.fileItem = fileItem;
		this.size = this.fileItem.getSize();
	}

	/**
	 * Return the underlying <code>org.apache.commons.fileupload.FileItem</code> instance. There is
	 * hardly any need to access this.
	 */
	public final FileItem getFileItem() {
		return this.fileItem;
	}

	/**
	 * Get file name.
	 */
	public String getName() {
		return this.fileItem.getFieldName();
	}

	/**
	 * Get original file name.
	 */
	public String getOriginalFilename() {
		String filename = this.fileItem.getName();
		if (filename == null) {
			// Should never happen.
			return "";
		}
		// check for Unix-style path
		int pos = filename.lastIndexOf("/");
		if (pos == -1) {
			// check for Windows-style path
			pos = filename.lastIndexOf("\\");
		}
		if (pos != -1) {
			// any sort of path separator found
			return filename.substring(pos + 1);
		} else {
			// plain name
			return filename;
		}
	}

	/**
	 * Get file content type.
	 */
	public String getContentType() {
		return this.fileItem.getContentType();
	}

	/**
	 * Is empty file?
	 */
	public boolean isEmpty() {
		return (this.size == 0);
	}

	/**
	 * Get file size.
	 */
	public long getSize() {
		return this.size;
	}

	/**
	 * Get file content as byte array.
	 */
	public byte[] getBytes() {
		byte[] bytes = this.fileItem.get();
		return (bytes != null ? bytes : new byte[0]);
	}

	/**
	 * Get input stream.
	 */
	public InputStream getInputStream() throws IOException {
		InputStream inputStream = this.fileItem.getInputStream();
		return (inputStream != null ? inputStream : new ByteArrayInputStream(new byte[0]));
	}

	/**
	 * This method is not supported in GAE.
	 */
	public void transferTo(File dest) throws IOException, IllegalStateException {
		throw new UnsupportedOperationException("not possible.");
	}

	/**
	 * Determine whether the multipart content is still available. Always true.
	 */
	protected boolean isAvailable() {
		return true;
	}

	/**
	 * Return a description for the storage location of the multipart content.
	 * In this implementation, it returns "in memory" always.
	 */
	public String getStorageDescription() {
		return "in memory";
	}

}
