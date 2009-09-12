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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ThresholdingOutputStream;

/**
 * <p> Important Note: This src is modified version of {@link org.apache.commons.io.output.DeferredFileOutputStream}
 * to make it work in GAE and spring.
 *
 *<p> An output stream which will retain data in memory (always) as GAE doesn't suport file handling.
 * If the stream reaches the threshold , the UnsupportedOperationException will be thrown.
 * To fix, try changing the thresold limit.
 *
 * <p>
 * This class originated in FileUpload processing. In this use case, you do not know in advance the
 * size of the file being uploaded.
 * Anyways, the file is stored in memory (for speed and GAE doesn't support file handling.)
 *
 * @author kernel164
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author gaxzerow
 */
public class GOutputStream extends ThresholdingOutputStream {

	// ----------------------------------------------------------- Data members

	/**
	 * The output stream to which data will be written prior to the theshold being reached.
	 */
	private final ByteArrayOutputStream memoryOutputStream;

	/**
	 * True when close() has been called successfully.
	 */
	private boolean closed = false;

	// ----------------------------------------------------------- Constructors

	/**
	 * Constructs an instance of this class which will trigger throw
	 * UnsupportedOperationException if the specified threshold is reached.
	 *
	 * @param threshold The number of bytes at which to trigger an event.
	 * @param outputFile The file to which data is saved beyond the threshold.
	 */
	public GOutputStream(long threshold) {
		super((int) threshold);
		memoryOutputStream = new ByteArrayOutputStream();
	}

	// --------------------------------------- ThresholdingOutputStream methods

	/**
	 * Returns the current output stream. This may be memory based or disk based, depending on the
	 * current state with respect to the threshold.
	 *
	 * @return The underlying output stream.
	 *
	 * @exception IOException if an error occurs.
	 */
	@Override
	protected OutputStream getStream() throws IOException {
		return memoryOutputStream;
	}

	/**
	 * Not possible in GAE. Will never reach!!
	 * If it happens, try changing max upload size setting.
	 */
	@Override
	protected void thresholdReached() {
		throw new UnsupportedOperationException("Not possible in GAE. Will never reach!! Try changing max upload size setting.");
	}

	// --------------------------------------------------------- Public methods

	/**
	 * Determines whether or not the data for this output stream has been retained in memory.
	 *
	 * @return always <code>true</code>
	 */
	public boolean isInMemory() {
		return true;
	}

	/**
	 * Returns the data for this output stream as an array of bytes.
	 *
	 * @return The data for this output stream, or <code>null</code> if no such data is available.
	 */
	public byte[] getData() {
		return memoryOutputStream.toByteArray();
	}

	/**
	 * Closes underlying output stream, and mark this as closed
	 *
	 * @exception IOException if an error occurs.
	 */
	@Override
	public void close() throws IOException {
		super.close();
		closed = true;
	}

	/**
	 * Writes the data from this output stream to the specified output stream, after it has been
	 * closed.
	 *
	 * @param out output stream to write to.
	 * @exception IOException if this stream is not yet closed or an error occurs.
	 */
	public void writeTo(OutputStream out) throws IOException {
		// we may only need to check if this is closed if we are working with a
		// file
		// but we should force the habit of closing wether we are working with
		// a file or memory.
		if (!closed) {
			throw new IOException("Stream not closed");
		}

		memoryOutputStream.writeTo(out);
	}
}
