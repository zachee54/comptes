/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.charset;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/** A buffered <code>Reader</code> which reads chars from a
 * <code>DecodingContext</code>.
 *
 * @author Olivier HAAS
 */
class CharBufferReader extends Reader {

	/** The char buffer used to temporarily store the decoded characters. */
	private final CharBuffer buf;
	
	/** The decoding context to use to refill the char buffer. */
	private final DecodingContext decoding;
	
	/** Constructs a buffered <code>Reader</code> which reads chars from the
	 * given <code>DecodingContext</code>.
	 * 
	 * @param decoding	The decoding context.
	 * @param size		The size of internal char buffer.
	 */
	public CharBufferReader(DecodingContext decoding, int size) {
		this.decoding = decoding;

		// Allocate the char buffer
		buf = CharBuffer.allocate(size);
		buf.flip();	// Flip the buffer so that it's at first empty for reading
	}// constructor
	
	@Override
	public int read(char[] dest, int off, int len) throws IOException {
		
		/* If nothing available, try to get more chars.
		 * If furthermore char stream is over, return -1.
		 */
		if (!buf.hasRemaining() && !decoding.fill(buf))
			return -1;
		
		// Read as many bytes as possible with this buffer
		int n = Math.min(len, buf.remaining());		// Nb of chars to return
		buf.get(dest, off, n);						// Write the chars
		return n;									// Return the count
	}// read

	/** Closes the <code>DecodingContext</code>. */
	@Override
	public void close() throws IOException {
		decoding.close();
	}// close
}
