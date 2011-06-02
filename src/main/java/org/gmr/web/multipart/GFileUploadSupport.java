/*
 * Copyright 2002-2008 the original author or authors.
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

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.WebUtils;

/**
 * <p>
 * Important Note: This src is modifed version of {@link org.springframework.web.multipart.CommonsFileUploadSupport} to
 * make it work in GAE.
 *
 * Base class for multipart resolvers that use Jakarta Commons FileUpload 1.1 or higher.
 *
 * <p>
 * Provides common configuration properties and parsing functionality for multipart requests, using a Map of Spring
 * GMultipartFile instances as representation of uploaded files and a String-based parameter Map as representation of
 * uploaded form fields.
 *
 * <p>
 * Subclasses implement concrete resolution strategies for Servlet environments: see GMultipartResolver. This base class
 * is not tied to those APIs, factoring out common functionality.
 *
 * @author kernel164
 * @author Juergen Hoeller
 * @see GMultipartFile
 * @see GMultipartResolver
 */
public abstract class GFileUploadSupport {

	protected final Log logger = LogFactory.getLog(getClass());

	private final GFileItemFactory fileItemFactory;

	private final FileUpload fileUpload;

	/**
	 * Instantiate a new GFileUploadSupport with its corresponding FileItemFactory and FileUpload instances.
	 *
	 * @see #newFileItemFactory
	 * @see #newFileUpload
	 */
	public GFileUploadSupport() {
		this.fileItemFactory = newFileItemFactory();
		this.fileUpload = newFileUpload(getFileItemFactory());
	}

	/**
	 * Return the underlying <code>GFileItemFactory</code> instance. There is
	 * hardly any need to access this.
	 *
	 * @return the underlying GFileItemFactory instance
	 */
	public GFileItemFactory getFileItemFactory() {
		return this.fileItemFactory;
	}

	/**
	 * Return the underlying <code>org.apache.commons.fileupload.FileUpload</code> instance. There is hardly any need to
	 * access this.
	 *
	 * @return the underlying FileUpload instance
	 */
	public FileUpload getFileUpload() {
		return this.fileUpload;
	}

	/**
	 * Set the maximum allowed size (in bytes) before uploads are refused. -1 indicates no limit (the default).
	 * Sets the maximum in memory thresold limit to max upload size.
	 *
	 * @param maxUploadSize the maximum upload size allowed
	 * @see org.apache.commons.fileupload.FileUploadBase#setSizeMax
	 * @see org.apache.commons.fileupload.FileItemFactory#setSizeThreshold
	 */
	public void setMaxUploadSize(int maxUploadSize) {
		this.fileUpload.setSizeMax(maxUploadSize);
		this.fileItemFactory.setSizeThreshold(maxUploadSize);
	}

	/**
	 * Set the default character encoding to use for parsing requests, to be applied to headers of individual parts and
	 * to form fields. Default is ISO-8859-1, according to the Servlet spec.
	 * <p>
	 * If the request specifies a character encoding itself, the request encoding will override this setting. This also
	 * allows for generically overriding the character encoding in a filter that invokes the
	 * <code>ServletRequest.setCharacterEncoding</code> method.
	 *
	 * @param defaultEncoding the character encoding to use
	 * @see javax.servlet.ServletRequest#getCharacterEncoding
	 * @see javax.servlet.ServletRequest#setCharacterEncoding
	 * @see WebUtils#DEFAULT_CHARACTER_ENCODING
	 * @see org.apache.commons.fileupload.FileUploadBase#setHeaderEncoding
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		this.fileUpload.setHeaderEncoding(defaultEncoding);
	}

	/**
	 * Returns the default encoding.
	 *
	 * @return the default encoding.
	 */
	protected String getDefaultEncoding() {
		String encoding = getFileUpload().getHeaderEncoding();
		if (encoding == null) {
			encoding = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		return encoding;
	}

	/**
	 * Factory method for a Commons GFileItemFactory instance.
	 *
	 * <p> Default implementation returns a standard GFileItemFactory. Can be overridden to use a custom subclass, e.g.
	 * for testing purposes.
	 *
	 * @return the new DiskFileItemFactory instance
	 */
	protected GFileItemFactory newFileItemFactory() {
		return new GFileItemFactory();
	}

	/**
	 * Factory method for a Commons FileUpload instance.
	 * <p>
	 * <b>To be implemented by subclasses.</b>
	 *
	 * @param fileItemFactory the Commons FileItemFactory to build upon
	 * @return the Commons FileUpload instance
	 */
	protected abstract FileUpload newFileUpload(FileItemFactory fileItemFactory);

	/**
	 * Determine an appropriate FileUpload instance for the given encoding.
	 * <p>
	 * Default implementation returns the shared FileUpload instance if the encoding matches, else creates a new
	 * FileUpload instance with the same configuration other than the desired encoding.
	 *
	 * @param encoding the character encoding to use
	 * @return an appropriate FileUpload instance.
	 */
	protected FileUpload prepareFileUpload(String encoding) {
		FileUpload fileUpload = getFileUpload();
		FileUpload actualFileUpload = fileUpload;

		// Use new temporary FileUpload instance if the request specifies
		// its own encoding that does not match the default encoding.
		if (encoding != null && !encoding.equals(fileUpload.getHeaderEncoding())) {
			actualFileUpload = newFileUpload(getFileItemFactory());
			actualFileUpload.setSizeMax(fileUpload.getSizeMax());
			actualFileUpload.setHeaderEncoding(encoding);
		}

		return actualFileUpload;
	}

	/**
	 * Parse the given List of Commons FileItems into a Spring MultipartParsingResult, containing Spring MultipartFile
	 * instances and a Map of multipart parameter.
	 *
	 * @param fileItems the Commons FileIterms to parse
	 * @param encoding the encoding to use for form fields
	 * @return the Spring MultipartParsingResult
	 * @see GMultipartFile#CommonsMultipartFile(org.apache.commons.fileupload.FileItem)
	 */
	protected MultipartParsingResult parseFileItems(List<FileItem> fileItems, String encoding) {
		MultiValueMap<String, MultipartFile> multipartFiles = new LinkedMultiValueMap<String, MultipartFile>();
		Map<String, String[]> multipartParameters = new HashMap<String, String[]>();

		// Extract multipart files and multipart parameters.
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField()) {
				String value = null;
				if (encoding != null) {
					try {
						value = fileItem.getString(encoding);
					} catch (UnsupportedEncodingException ex) {
						if (logger.isWarnEnabled()) {
							logger.warn("Could not decode multipart item '" + fileItem.getFieldName() + "' with encoding '" + encoding + "': using platform default");
						}
						value = fileItem.getString();
					}
				} else {
					value = fileItem.getString();
				}
				String[] curParam = multipartParameters.get(fileItem.getFieldName());
				if (curParam == null) {
					// simple form field
					multipartParameters.put(fileItem.getFieldName(), new String[] { value });
				} else {
					// array of simple form fields
					String[] newParam = StringUtils.addStringToArray(curParam, value);
					multipartParameters.put(fileItem.getFieldName(), newParam);
				}
			} else {
				// multipart file field
				GMultipartFile file = new GMultipartFile(fileItem);
				multipartFiles.add(file.getName(), file);
				if (logger.isDebugEnabled()) {
					logger.debug("Found multipart file [" + file.getName() + "] of size " + file.getSize() +
							" bytes with original filename [" + file.getOriginalFilename() + "], stored " +
							file.getStorageDescription());
				}
			}
		}
		return new MultipartParsingResult(multipartFiles, multipartParameters);
	}

	/**
	 * Cleanup the Spring MultipartFiles created during multipart parsing.
	 * <p>
	 * Deletes the underlying Commons FileItem instances.
	 *
	 * @param multipartFiles Collection of MultipartFile instances
	 * @see org.apache.commons.fileupload.FileItem#delete()
	 */
	protected void cleanupFileItems(Collection<MultipartFile> multipartFiles) {
		for (MultipartFile file : multipartFiles) {
			if (file instanceof GMultipartFile) {
				GMultipartFile cmf = (GMultipartFile) file;
				cmf.getFileItem().delete();
				if (logger.isDebugEnabled()) {
					logger.debug("Cleaning up multipart file [" + cmf.getName() + "] with original filename [" + cmf.getOriginalFilename() + "], stored " + cmf.getStorageDescription());
				}
			}
		}
	}

	/**
	 * Holder for a Map of Spring MultipartFiles and a Map of multipart parameters.
	 */
	protected static class MultipartParsingResult {

		private final MultiValueMap<String, MultipartFile> multipartFiles;

		private final Map<String, String[]> multipartParameters;

		/**
		 * Create a new MultipartParsingResult.
		 *
		 * @param mpFiles Map of field name to MultipartFile instance
		 * @param mpParams Map of field name to form field String value
		 */
		public MultipartParsingResult(MultiValueMap<String, MultipartFile> mpFiles, Map<String, String[]> mpParams) {
			this.multipartFiles = mpFiles;
			this.multipartParameters = mpParams;
		}

		/**
		 * Return the multipart files as Map of field name to MultipartFile instance.
		 */
		public MultiValueMap<String, MultipartFile> getMultipartFiles() {
			return this.multipartFiles;
		}

		/**
		 * Return the multipart parameters as Map of field name to form field String value.
		 */
		public Map<String, String[]> getMultipartParameters() {
			return this.multipartParameters;
		}
	}

}
