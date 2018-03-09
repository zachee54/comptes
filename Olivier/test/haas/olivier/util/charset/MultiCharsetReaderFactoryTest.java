package haas.olivier.util.charset;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiCharsetReaderFactoryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/** Test the class using the specified text, encoded with the specified
	 * charset, and using successively UTF-8 and ISO-8859-15 charsets for
	 * decoding.
	 * 
	 * @param text	The text to encode/decode.
	 * 
	 * @param encodingCharset
	 * 				The charset to use to encode <code>text</code>.
	 * 
	 * @throws IOException
	 */
	private void launch(String text, String encodingCharsetName)
			throws IOException {
		launch(text, encodingCharsetName, new ByteArrayOutputStream(), "UTF-8",
				"ISO-8859-15");
	}// launch default
	
	/** Test the class using the specified text, encoded with the specified
	 * charset.
	 * 
	 * @param text	The text to encode/decode.
	 * 
	 * @param encodingCharsetName
	 * 				The charset to use to encode <code>text</code>.
	 * 
	 * @param out
	 * 				The output stream the encoding of <code>text</code> will be
	 * 				written in. This argument permits to write bytes at the
	 * 				beginning of the stream before invoking the method.
	 * 
	 * @param decodingCharsetNames
	 * 				The successive charsets to the tested object must use.
	 * 
	 * @throws IOException
	 */
	private void launch(String text, String encodingCharsetName,
			ByteArrayOutputStream out, String... decodingCharsetNames)
					throws IOException {
		Writer writer = null;
		BufferedReader reader = null;
		try{
			// Prepare encoded data
			writer = new OutputStreamWriter(out,
					Charset.forName(encodingCharsetName));
			writer.write(text);
			writer.flush();
			
			// Test
			reader = new BufferedReader(
					MultiCharsetReaderFactory.createMultiCharsetReader(
							new ByteArrayInputStream(out.toByteArray()),
							decodingCharsetNames));
			String result = "";
			String line;
			while ((line = reader.readLine()) != null)
				result += line;
			
			// Verify
			assertEquals(text, result);
			
		} finally {
			if (reader != null) reader.close();
			if (writer != null) writer.close();
			if (out != null) out.close();
		}// try
	}// launch
	
	/** Test an empty string. */
	@Test
	public void testEmpty() throws IOException {
		launch("", "ISO-8859-15");
	}// testEmpty
	
	/** Test a small encoded in ISO charset. */
	@Test
	public void testIso() throws IOException {
		launch("Que j'aime à faire connaître un nombre utile aux sages ! Immortel Archimède, artiste, ingénieur, qui de ton jugement peut priser la valeur ? Pour moi ton problème eut de sérieux avantages.",
				"ISO-8859-15");
	}// test
	
	/** Test a small string encoded in UTF-8. */
	@Test
	public void testUTF8() throws IOException {
		launch("Que j'aime à faire connaître un nombre utile aux sages ! Immortel Archimède, artiste, ingénieur, qui de ton jugement peut priser la valeur ? Pour moi ton problème eut de sérieux avantages.",
				"UTF-8");
	}// testUTF8

	/** Test a long string which quickly jumps to ISO instead of UTF-8.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testBigTextSingleDecoding() throws IOException {
		launch(getBigText(), "ISO-8859-15");
	}// testBigTextSingleDecoding
	
	/** Test a long string encoded with UTF-8, keeping also much more data in
	 * internal buffers.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testBigTextMultiDecoding() throws IOException {
		launch(getBigText(), "UTF-8");
	}// testBigTextMultiDecoding
	
	/** Generate a very long string (at least 2^15) containing special
	 * characters.
	 */
	private String getBigText() {		
		String base = "jbgqkuéè:]@`";					// Pattern to repeat
		String text = base;								// Generated text
		while (text.length() <= 1<<15)					// While < 2^15
			text += text;								// Double text length
		return text;
	}// getBigText
	
	/** Test a string beginning with UTF-8 BOM, without explicitly specifiying
	 * UTF-8 charset for decoding.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testUTF8BOM() throws IOException {
		
		// Write UTF-8 BOM
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0xEF);
		out.write(0xBB);
		out.write(0xBF);
		
		// Test with an encoding-specific string, without specifying UTF-8
		launch("é#~uå",
				"UTF-8",				// Encode in UTF-8
				out,
				"ISO-8859-15");			// Decode with latin9 (bad indication)
	}// testUTF8BOM
	
	/** Test a string beginning with UTF-16BE BOM, without explicitly specifying
	 * UTF-16BE charset for decoding.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testUTF16BEBOM() throws IOException {
		
		// Write UTF-16BE BOM
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0xFE);
		out.write(0xFF);
		
		// Test with an ecoding-specifig string, without specifying UTF-16BE
		launch("é#~uå",
				"UTF-16BE",				// Encode in UTF-16BE
				out,
				"ISO-8859-15");			// Decode with latin9 (bad indication)
	}// testUTF16BEBOM
	
	/** Test a string beginning with UTF-16BE BOM, without explicitly specifying
	 * UTF-16BE charset for decoding.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testUTF16LEBOM() throws IOException {
		
		// Write UTF-16LE BOM
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0xFF);
		out.write(0xFE);
		
		// Test with an ecoding-specifig string, without specifying UTF-16LE
		launch("é#~uå",
				"UTF-16LE",				// Encode in UTF-16LE
				out,
				"ISO-8859-15");			// Decode with latin9 (bad indication)
	}// testUTF16LEBOM
}
