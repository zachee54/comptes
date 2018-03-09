/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.charset;

import java.io.Closeable;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/** A context managing different states of bytes decoding.
 * <p>
 * If a byte-order mark is specified, or if only one charset is expected, or if
 * all charsets but one from a <code>MultiDecodingState</code> were discarded,
 * the context uses a <code>SingleDecodingState</code>, which uses remplacement
 * characters and bytes buffering but not mark/reset operations.
 * <p>
 * If several charsets are specified, the context uses as state a
 * <code>MultiDecodingState</code>, allowing charset modifications on the fly,
 * but needing error reporting and mark/reset operations.
 * <p>
 * The following BOMs are supported : UTF-8, UTF-16LE, UTF-16BE.
 *
 * @author Olivier HAAS
 */
class DecodingContext implements Closeable {
	
	/** The buffer decoder to use as input. */
	private final BufferDecoder bufDecoder;
	
	/** The state of decoding : single or multi charset handler. */
	private DecodingState state;

	/** Constructs a decoding context to produce chars from the given byte
	 * stream, using the given charsets.
	 * 
	 * @param bufDecoder	The buffer decoder to use.
	 * @param charsets		An iterable over the successive charsets to use.
	 * 
	 * @throws IOException	If an error occurs while trying to read a BOM from
	 * 						the underlying input stream.
	 */
	public DecodingContext(BufferDecoder bufDecoder, Iterable<Charset> charsets)
			throws IOException {
		this.bufDecoder = bufDecoder;
		
		// Instance a multi-decoding state. It sets also the first state.
		new MultiDecodingState(this, charsets);
	}// constructor
	
	/** Turns the buffer decoder to unresetable. */
	void setUnresetableBufferDecoder() {
		bufDecoder.setUnresetable();
	}// setUnresetableBufferDecoder
	
	/** Modifies the decoding state.
	 * 
	 * @param state	The new decoding state.
	 */
	void setState(DecodingState state) {
		this.state = state;
	}// setState
	
	/** Clears the specified char buffer and fills it until it is full or the
	 * end of input stream has been reached.
	 * <p>
	 * The state may change during the attempt to fill the buffer. In this case,
	 * a new attempt is proceeded until a state succeeds.
	 * 
	 * @param outBuf	The char buffer	to fill.
	 * 
	 * @return			<code>true</code> if <code>outBuf</code> is full, so
	 * 					that a following method invocation is expected ;
	 * 					<code>false</code> if end of input has been reached.
	 * 
	 * @throws IOException
	 * 					If an exception occurs while reading the input.
	 */
	public boolean fill(CharBuffer outBuf) throws IOException {
		
		// Clear the char buffer
		outBuf.clear();
		
		// Try to decode with actual or successive states
		while (!state.decodeInto(this, outBuf, bufDecoder));

		// Flip the char buffer to make it ready for reading
		outBuf.flip();

		// Tell if something was written (non collapsed readable bytes)
		return outBuf.limit() > 0;
	}// fill
	
	@Override
	public void close() throws IOException {
		bufDecoder.close();
	}// close
}
