/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.charset;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/** A factory of <code>Reader</code>s which can read a byte stream with multiple
 * predefined charsets, changing of charset on the fly to find the probable
 * input charset.
 *
 * @author Olivier HAAS
 */
public class MultiCharsetReaderFactory {

	/** Creates a new <code>Reader</code> which reads the specified stream,
	 * trying the specified charsets on the fly.
	 * <p>
	 * If the stream begins with a BOM, the corresponding charset is used even
	 * though it isn't in the specified charsets.
	 * <p>
	 * The following BOM are supported : UTF-8, UTF-16LE, UTF-16BE.
	 * 
	 * @param in	The byte input stream to read.
	 * 
	 * @param charsetNames
	 * 				The names of the successive charsets to use.
	 * 
	 * @return		A new reader.
	 * 
	 * @throws IOException
	 * 					If an error occurs while reading the beginning of
	 * 					<code>in</code>.
	 */
	public static Reader createMultiCharsetReader(InputStream in,
			String... charsetNames) throws IOException {
		List<Charset> charsets = new ArrayList<Charset>();
		
		// Detect BOM, if any
		BufferDecoder bufDecoder = new BufferDecoder(new BufferSequence(in));
		Charset bomCharset = detectBOM(bufDecoder);
		
		// Set charsets' list with either detected charset or specified ones
		if (bomCharset == null) {
			for (String charsetName : charsetNames) {
				charsets.add(Charset.forName(charsetName));
			}
		} else {
			charsets.add(bomCharset);
		}// if

		// Return the reader
		return new CharBufferReader(
				new DecodingContext(bufDecoder, charsets),
				1<<12);	// use a 4ko internal char buffer
	}// createMultiCharsetReader
	
	/** Tries to detect a byte-order mark at actual position.
	 * <p>
	 * The method reads 3 bytes out of the <code>BufferDecoder</code>.<br>
	 * The method garantees that the next read after execution will return the
	 * first bytes of the stream, without the detected BOM if any.
	 * <p>
	 * Detected charsets are UTF-8, UTF-16LE and UTF-16BE.
	 * 
	 * @param bufDecoder	The buffer decoder to use to read bytes.
	 * 
	 * @return				The charset corresponding to the detected byte order
	 * 						mark, or <code>null</code> if the bytes don't
	 * 						correspond to a known BOM.
	 * 
	 * @throws IOException	If an error occurs while reading the underlying
	 * 						input stream.
	 */
	private static Charset detectBOM(BufferDecoder bufDecoder)
			throws IOException {
		BOMHelper bomHelper = new BOMHelper();
		
		// Get enough bytes from stream beginning
		bufDecoder.mark();
		byte[] bytes = bufDecoder.getBytes(bomHelper.getMaxBOM());
		bufDecoder.reset();								// Re-read afterwards
		
		// Compare bytes with each known BOM
		for (Entry<byte[], Charset> bomEntry :
			bomHelper.availableBOMs().entrySet()) {
			
			byte[] bom = bomEntry.getKey();				// BOM bytes

			// If this BOM has been successfully detected
			if (bomHelper.matches(bom, bytes)) {
				bufDecoder.getBytes(bom.length);		// Don't re-read the BOM
				return bomEntry.getValue();				// Appropriate charset
			}// if detected
		}// forBom

		// No BOM detected
		return null;
	}// detectBOM
}
