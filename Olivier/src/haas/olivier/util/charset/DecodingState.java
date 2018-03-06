/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.charset;

import java.io.IOException;
import java.nio.CharBuffer;

/** Interface for decoding states.
 * <p>
 * They produce chars from a <code>CharsetDecoder</code> and may use either one
 * single charset or many successive charsets.
 *
 * @author Olivier HAAS
 */
interface DecodingState {
	
	/** Decodes as many chars as possible into the specified char buffer.
	 * 
	 * @param context	The decoding context. His state may be changed during
	 * 					the execution of this method.
	 * 
	 * @param outBuf	The destination char buffer.
	 * 
	 * @param bufDecoder
	 * 					The buffer decoder.
	 * 
	 * @return			<code>true</code> if <code>outBuf</code> is full or the
	 * 					end of stream has been reached, <code>false</code> if
	 * 					the state has changed and the method should be called
	 * 					again on the new state.
	 * 
	 * @throws IOException
	 * 					If an exception occurs while reading the input.
	 */
	boolean decodeInto(DecodingContext context, CharBuffer outBuf,
			BufferDecoder bufDecoder) throws IOException;
}
