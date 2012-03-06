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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

/**
 * <p>This src is modified from Spring 3.0 CommonsMultipartResolver.java and renamed to GMultipartResolver
 * to make it work in GAE.
 *
 * <p>Servlet-based {@link org.springframework.web.multipart.MultipartResolver} implementation for <a
 * href="http://jakarta.apache.org/commons/fileupload">Jakarta Commons FileUpload</a> 1.2 or above.
 *
 * <p>
 * Provides "maxUploadSize" and "defaultEncoding" settings as bean properties
 * (inherited from {@link GFileUploadSupport}).
 *
 * See corresponding ServletFileUpload / GFileItemFactory properties ("sizeMax", "sizeThreshold", "headerEncoding") for details in
 * terms of defaults and accepted values.
 * </p>
 *
 * @author kernel164
 */
public class GMultipartResolver extends GFileUploadSupport implements MultipartResolver {
	private boolean resolveLazily = false;

	/**
	 * Set whether to resolve the multipart request lazily at the time of file or parameter access.
	 * <p>
	 * Default is "false", resolving the multipart elements immediately, throwing corresponding
	 * exceptions at the time of the {@link #resolveMultipart} call. Switch this to "true" for lazy
	 * multipart parsing, throwing parse exceptions once the application attempts to obtain
	 * multipart files or parameters.
	 */
	public void setResolveLazily(boolean resolveLazily) {
		this.resolveLazily = resolveLazily;
	}

	/**
	 * Initialize the underlying
	 * <code>org.apache.commons.fileupload.servlet.ServletFileUpload</code> instance. Can be
	 * overridden to use a custom subclass, e.g. for testing purposes.
	 *
	 * @param fileItemFactory the Commons FileItemFactory to use
	 * @return the new ServletFileUpload instance
	 */
	@Override
	protected FileUpload newFileUpload(FileItemFactory fileItemFactory) {
		return new ServletFileUpload(fileItemFactory);
	}

	/**
	 * Returns true if the request has multipart.
	 */
	public boolean isMultipart(HttpServletRequest request) {
		return (request != null && ServletFileUpload.isMultipartContent(request));
	}

	/**
	 * Resolves multipart request.
	 */
	public MultipartHttpServletRequest resolveMultipart(final HttpServletRequest request) throws MultipartException {
		if (this.resolveLazily) {
			return new DefaultMultipartHttpServletRequest(request) {
				@Override
				protected void initializeMultipart() {
					MultipartParsingResult parsingResult = parseRequest(request);
					setMultipartFiles(parsingResult.getMultipartFiles());
					setMultipartParameters(parsingResult.getMultipartParameters());
				}
			};
		} else {
			MultipartParsingResult parsingResult = parseRequest(request);
			return new DefaultMultipartHttpServletRequest(request, parsingResult.getMultipartFiles(), parsingResult.getMultipartParameters());
		}
	}

	/**
	 * Parse the given servlet request, resolving its multipart elements.
	 *
	 * @param request the request to parse
	 * @return the parsing result
	 * @throws MultipartException if multipart resolution failed.
	 */
	@SuppressWarnings("unchecked")
	protected MultipartParsingResult parseRequest(HttpServletRequest request) throws MultipartException {
		String encoding = determineEncoding(request);
		FileUpload fileUpload = prepareFileUpload(encoding);
		try {
			List<FileItem> fileItems = ((ServletFileUpload) fileUpload).parseRequest(request);
			return parseFileItems(fileItems, encoding);
		} catch (FileUploadBase.SizeLimitExceededException ex) {
			throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
		} catch (FileUploadException ex) {
			throw new MultipartException("Could not parse multipart servlet request", ex);
		}
	}

	/**
	 * Determine the encoding for the given request. Can be overridden in subclasses.
	 * <p>
	 * The default implementation checks the request encoding, falling back to the default encoding
	 * specified for this resolver.
	 *
	 * @param request current HTTP request
	 * @return the encoding for the request (never <code>null</code>)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding
	 * @see #setDefaultEncoding
	 */
	protected String determineEncoding(HttpServletRequest request) {
		String encoding = request.getCharacterEncoding();
		if (encoding == null) {
			encoding = getDefaultEncoding();
		}
		return encoding;
	}

	/**
	 * Clean up multi part.
	 */
	public void cleanupMultipart(MultipartHttpServletRequest request) {
		if (request != null) {
			try {
				cleanupFileItems(request.getFileMap().values());
			} catch (Throwable ex) {
				logger.warn("Failed to perform multipart cleanup for servlet request", ex);
			}
		}
	}

}
