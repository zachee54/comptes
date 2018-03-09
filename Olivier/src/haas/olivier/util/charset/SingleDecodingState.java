/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.charset;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

/** A decoding state using a single charset, using remplacement character if
 * necessary.
 * <p>
 * Unlike <code>MultiDecodingState</code>, no error will be reported from
 * decoding operation.
 *
 * @author Olivier HAAS
 */
class SingleDecodingState implements DecodingState {
	
	/** The single charset decoder to use. */
	private final CharsetDecoderFlusher decoderFlusher;
	
	/** Constructs a decoding state using a single charset.
	 * 
	 * @param context	The decoding context. 
	 * 
	 * @param decoder	The single charset decoder to use.
	 */
	public SingleDecodingState(DecodingContext context,
			CharsetDecoder decoder) {
		
		// Forbid error reporting
		decoder.onMalformedInput(CodingErrorAction.REPLACE);
		decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		decoderFlusher = new CharsetDecoderFlusher(decoder);
		
		// Make the source unresetable
		context.setUnresetableBufferDecoder();
	}// constructor

	/** Decodes with the single charset. No error reporting is provided.
	 * 
	 * @param context	The decoding context. This argument is ignored.
	 * 
	 * @param outBuf	The destination char buffer.
	 * 
	 * @param bufDecoder
	 * 					The buffer decoder.
	 * 
	 * @return			<code>true</code>.
	 * 
	 * @throws IOException
	 * 					If an exception occurs while reading the input.
	 */
	@Override
	public boolean decodeInto(DecodingContext context, CharBuffer outBuf,
			BufferDecoder bufDecoder) throws IOException {
		bufDecoder.decode(outBuf, decoderFlusher);
		return true;
	}// decodeInto
}
