/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemHeadersSupport;
import org.apache.commons.fileupload.ParameterParser;

/**
 *
 * <p>Important Note: This src is modifed version of {@link org.apache.commons.fileupload.disk.DiskFileItem}
 * to make it work under GAE using Spring.
 *
 * <p> All the File related codes are removed. For more info please check {@link org.apache.commons.fileupload.disk.DiskFileItem}
 *
 * <p>The class is an implementation of the {@link org.apache.commons.fileupload.FileItem FileItem} interface.
 *
 * @author kernel164
 */
public class GFileItem implements FileItem, FileItemHeadersSupport {

	// ----------------------------------------------------- Manifest constants

	private static final long serialVersionUID = -8090661404150222661L;

	/**
	 * Default content charset to be used when no explicit charset parameter is provided by the sender. Media subtypes
	 * of the "text" type are defined to have a default charset value of "ISO-8859-1" when received via HTTP.
	 */
	public static final String DEFAULT_CHARSET = "ISO-8859-1";

	// ----------------------------------------------------------- Data members
	/**
	 * The name of the form field as provided by the browser.
	 */
	private String fieldName;

	/**
	 * The content type passed by the browser, or <code>null</code> if not defined.
	 */
	private final String contentType;

	/**
	 * Whether or not this item is a simple form field.
	 */
	private boolean isFormField;

	/**
	 * The original filename in the user's filesystem.
	 */
	private final String fileName;

	/**
	 * Cached contents of the file.
	 */
	private byte[] cachedContent;

	/**
	 * Output stream for this item.
	 */
	private transient GOutputStream dfos;

	/**
	 * The threshold above which uploads will be stored on disk.
	 */
	private int sizeThreshold;

	/**
	 * The file items headers.
	 */
	private FileItemHeaders headers;

	// ----------------------------------------------------------- Constructors

	/**
	 * Constructs a new <code>GFileItem</code> instance.
	 *
	 * @param fieldName The name of the form field.
	 * @param contentType The content type passed by the browser or <code>null</code> if not specified.
	 * @param isFormField Whether or not this item is a plain form field, as opposed to a file upload.
	 * @param fileName The original filename in the user's filesystem, or <code>null</code> if not specified.
	 * @param sizeThreshold The threshold, in bytes, below which items will be retained in memory. (sizeThresold will always be equal to file upload limit)
	 */
	public GFileItem(String fieldName, String contentType, boolean isFormField, String fileName, int sizeThreshold) {
		this.fieldName = fieldName;
		this.contentType = contentType;
		this.isFormField = isFormField;
		this.fileName = fileName;
		this.sizeThreshold = sizeThreshold;
	}

	// ------------------------------- Methods from javax.activation.DataSource

	/**
	 * Returns an {@link java.io.InputStream InputStream} that can be used to retrieve the contents of the file.
	 *
	 * @return An {@link java.io.InputStream InputStream} that can be used to retrieve the contents of the file.
	 *
	 * @throws IOException if an error occurs.
	 */
	public InputStream getInputStream() throws IOException {
		if (cachedContent == null) {
			cachedContent = dfos.getData();
		}
		return new ByteArrayInputStream(cachedContent);
	}

	/**
	 * Returns the content type passed by the agent or <code>null</code> if not defined.
	 *
	 * @return The content type passed by the agent or <code>null</code> if not defined.
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Returns the content charset passed by the agent or <code>null</code> if not defined.
	 *
	 * @return The content charset passed by the agent or <code>null</code> if not defined.
	 */
	@SuppressWarnings("unchecked")
	public String getCharSet() {
		ParameterParser parser = new ParameterParser();
		parser.setLowerCaseNames(true);
		// Parameter parser can handle null input
		Map<String, String> params = parser.parse(getContentType(), ';');
		return params.get("charset");
	}

	/**
	 * Returns the original filename in the client's filesystem.
	 *
	 * @return The original filename in the client's filesystem.
	 */
	public String getName() {
		return fileName;
	}

	// ------------------------------------------------------- FileItem methods

	/**
	 * Provides a hint as to whether or not the file contents will be read from memory.
	 *
	 * @return <code>true</code> if the file contents will be read from memory; <code>false</code> otherwise.
	 */
	public boolean isInMemory() {
		return true;
	}

	/**
	 * Returns the size of the file.
	 *
	 * @return The size of the file, in bytes.
	 */
	public long getSize() {
		if (cachedContent != null) {
			return cachedContent.length;
		} else {
			return dfos.getData().length;
		}
	}

	/**
	 * Returns the contents of the file as an array of bytes.
	 *
	 * @return The contents of the file as an array of bytes.
	 */
	public byte[] get() {
		if (cachedContent == null) {
			cachedContent = dfos.getData();
		}
		return cachedContent;
	}

	/**
	 * Returns the contents of the file as a String, using the specified encoding. This method uses {@link #get()} to
	 * retrieve the contents of the file.
	 *
	 * @param charset The charset to use.
	 *
	 * @return The contents of the file, as a string.
	 *
	 * @throws UnsupportedEncodingException if the requested character encoding is not available.
	 */
	public String getString(final String charset) throws UnsupportedEncodingException {
		return new String(get(), charset);
	}

	/**
	 * Returns the contents of the file as a String, using the default character encoding. This method uses
	 * {@link #get()} to retrieve the contents of the file.
	 *
	 * @return The contents of the file, as a string.
	 *
	 * @todo Consider making this method throw UnsupportedEncodingException.
	 */
	public String getString() {
		byte[] rawdata = get();
		String charset = getCharSet();
		if (charset == null) {
			charset = DEFAULT_CHARSET;
		}
		try {
			return new String(rawdata, charset);
		} catch (UnsupportedEncodingException e) {
			return new String(rawdata);
		}
	}

	/**
	 * This method is not supported.
	 *
	 * @param file The <code>File</code> into which the uploaded item should be stored.
	 */
	public void write(File file) {
		throw new UnsupportedOperationException("Not possible in GAE.");
	}

	/**
	 * Does nothing.
	 */
	public void delete() {
		// As all the data are in Heap, it will be garbage collected.
	}

	/**
	 * Returns the name of the field in the multipart form corresponding to this file item.
	 *
	 * @return The name of the form field.
	 *
	 * @see #setFieldName(java.lang.String)
	 *
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Sets the field name used to reference this file item.
	 *
	 * @param fieldName The name of the form field.
	 *
	 * @see #getFieldName()
	 *
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * Determines whether or not a <code>FileItem</code> instance represents a simple form field.
	 *
	 * @return <code>true</code> if the instance represents a simple form field; <code>false</code> if it represents an
	 * uploaded file.
	 *
	 * @see #setFormField(boolean)
	 *
	 */
	public boolean isFormField() {
		return isFormField;
	}

	/**
	 * Specifies whether or not a <code>FileItem</code> instance represents a simple form field.
	 *
	 * @param state <code>true</code> if the instance represents a simple form field; <code>false</code> if it
	 * represents an uploaded file.
	 *
	 * @see #isFormField()
	 *
	 */
	public void setFormField(boolean state) {
		isFormField = state;
	}

	/**
	 * Returns an {@link java.io.OutputStream OutputStream} of the file.
	 *
	 * @return An {@link java.io.OutputStream OutputStream} of the file.
	 *
	 * @throws IOException if an error occurs.
	 */
	public OutputStream getOutputStream() throws IOException {
		if (dfos == null) {
			dfos = new GOutputStream(sizeThreshold);
		}
		return dfos;
	}

	// -------------------------------------------------------- Private methods

	/**
	 * Returns a string representation of this object.
	 *
	 * @return a string representation of this object.
	 */
	@Override
	public String toString() {
		return "name=" + this.getName() + ", size=" + this.getSize() + "bytes, " + "isFormField=" + isFormField() + ", FieldName=" + this.getFieldName();
	}

	// -------------------------------------------------- Serialization methods

	/**
	 * Writes the state of this object during serialization.
	 *
	 * @param out The stream to which the state should be written.
	 *
	 * @throws IOException if an error occurs.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		// Read the data
		cachedContent = get();

		// write out values
		out.defaultWriteObject();
	}

	/**
	 * Reads the state of this object during deserialization.
	 *
	 * @param in The stream from which the state should be read.
	 *
	 * @throws IOException if an error occurs.
	 * @throws ClassNotFoundException if class cannot be found.
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		// read values
		in.defaultReadObject();

		OutputStream output = getOutputStream();
		if (cachedContent != null) {
			output.write(cachedContent);
		}
		output.close();

		cachedContent = null;
	}

	/**
	 * Returns the file item headers.
	 *
	 * @return The file items headers.
	 */
	public FileItemHeaders getHeaders() {
		return headers;
	}

	/**
	 * Sets the file item headers.
	 *
	 * @param pHeaders The file items headers.
	 */
	public void setHeaders(FileItemHeaders pHeaders) {
		headers = pHeaders;
	}
}
