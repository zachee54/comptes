/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.charset;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;

/** A decoding state that decodes bytes using multiple predefined charsets.
 * <p>
 * The charsets are used successively, each one being discarded as soon as an
 * error occurs.<br>
 * If only one charset remains available, this instance gives up the job.
 * <p>
 * The charsets' order is given at instantiation time. This order should be
 * choosen carefully, since some charsets never report any error, as they're
 * able to give a sense to any byte sequence. Therefore, charsets specified
 * after such a charset will never be examined.
 *
 * @author Olivier HAAS
 */
class MultiDecodingState implements DecodingState {
	
	/** The actual charset decoder to use. */
	private CharsetDecoderFlusher decoderFlusher;
	
	/** The iterator over the successive charsets to use. */
	private final Iterator<Charset> charsetIt;
	
	/** Constructs a decoding state that tries multiple successive charsets, and
	 * set it as the current state.
	 * <p>
	 * <i>Warning : If </i><code>charsets</code><i> contains only a single
	 * charset, this instance will discard itself as soon and set a
	 * </i><code>SingleDecodingState</code><i> as new state in
	 * </i><code>context</code>.
	 * 
	 * @param context	The decoding context.
	 * @param charsets	The successive charsets to use.
	 */
	MultiDecodingState(DecodingContext context, Iterable<Charset> charsets) {
		charsetIt = charsets.iterator();
		if (!charsetIt.hasNext()) {
			throw new IllegalArgumentException(
					"At least one charset must be specified");
		}
		
		// Set ourself as current state in context
		context.setState(this);
		
		// Install first charset decoder (might set in turn another state)
		nextDecoder(context);
	}// constructor

	@Override
	public boolean decodeInto(DecodingContext context, CharBuffer outBuf,
			BufferDecoder bufDecoder) throws IOException {
		
		// Mark start position of in and out buffers
		outBuf.mark();
		bufDecoder.mark();
		
		// Try to decode with successive charset decoders
		while (bufDecoder.decode(outBuf, decoderFlusher).isError()) {
			outBuf.reset();							// Reset output buffer
			bufDecoder.reset();						// Reset input buffers
			if (!nextDecoder(context))				// Try with another decoder
				return false;
		}// while
		return true;
	}// decodeInto
	
	/** Changes to the next charset.
	 * 
	 * @param context	The decoding context. Its state may be changed during
	 * 					the execution of the method.
	 * 
	 * @return			<code>true</code> if a new decoder could be installed,
	 * 					<code>false</code> if a single decoder was left and
	 * 					therefore the state has been changed.
	 */
	private boolean nextDecoder(DecodingContext context) {
		
		// Get next charset's decoder
		CharsetDecoder decoder = charsetIt.next().newDecoder();
		
		// If this is the last one, give up the job and pass to single state
		if (!charsetIt.hasNext()) {
			context.setState(new SingleDecodingState(context, decoder));
			return false;
		}// if
		
		// Install the new charset decoder
		decoderFlusher = new CharsetDecoderFlusher(decoder);
		
		return true;
	}// nextDecoder
}
