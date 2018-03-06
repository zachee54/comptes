/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/** A decoder-flusher processing decoding, flushing and transition from one to
 * another transparently.
 * <p>
 * In fact, <code>CharsetDecoder</code> requires a fine use (see its Javadoc).
 * <br>The caller must call decode method as many times as necessary, then call
 * it once (and only once) again specifying the end of input, and finally call
 * the flush method.
 * <p>
 * The trouble is, the char producer shouldn't know about such stages. Moreover,
 * byte buffer manager can't easily guess the end of input before it happens,
 * and isn't concerned any longer as soon as the process enters flushing phase.
 * <p>
 * This class encapsulate the required behaviour so that the transition becomes
 * transparent to char producer.
 *
 * @see {@link java.nio.charset.CharsetDecoder}
 *
 * @author Olivier HAAS
 */
class CharsetDecoderFlusher {

	/** The charset decoder to use for decoding then flushing operations. */
	private final CharsetDecoder decoder;
	
	/** Flag indicating that the decoder has turned into flushing phase. */
	private boolean flushing = false;

	/** Constructs a decoder-flusher processing decoding, flushing and
	 * transition from one to another transparently.
	 * 
	 * @param decoder	The charset decoder to use for decoding then flushing
	 * 					operation.
	 */
	public CharsetDecoderFlusher(CharsetDecoder decoder) {
		this.decoder = decoder;
	}// constructor
	
	/** Write characters in output buffer either by decoding input buffer or
	 * flushing the decoder.<br>
	 * The object turns definitely into flushing phase if and only if
	 * <code>inBuf == null</code>.
	 * 
	 * @param inBuf		The input byte buffer to read. 
	 * @param outBuf	The output char buffer to write in.
	 * 
	 * @return			A <code>CoderResult</code> indicating the cause of end
	 * 					of decoding/flushing process.
	 */
	public CoderResult decode(ByteBuffer inBuf, CharBuffer outBuf) {
		
		// If no more input buffer, we must be in (or turn into) flushing phase
		if (inBuf == null)
			checkFlushing();
		
		// Process expected operation
		return flushing									// In either phase
				? decoder.flush(outBuf)					// Flush
				: decoder.decode(inBuf, outBuf, false);	// Decode
	}// decode
	
	/** Turns the object into flushing phase, if not already in. */
	private void checkFlushing() {
		if (!flushing) {
			flushing = true;

			// Notify end of input (required by CharsetDecoder specs)
			decoder.decode(
					ByteBuffer.wrap(new byte[0]),		// No data specified
					CharBuffer.wrap(new char[0]),		// No data expected
					true);								// This is the point !
		}// if not flushing
	}// checkFlushing
}
