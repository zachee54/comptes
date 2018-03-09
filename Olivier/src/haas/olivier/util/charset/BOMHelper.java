/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util.charset;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/** An helper for byte-order mark (BOM) detection.
 * <p>
 * This class maintains all known BOM and provides helper methods to detect
 * them.
 *
 * @author Olivier HAAS
 */
class BOMHelper {
	
	/** The supported byte order marks. */
	private final Map<byte[], Charset> boms =
			new HashMap<byte[], Charset>();
	
	/** Constructs a BOM helper to detect one of the known BOMs.
	 * <p>
	 * The available BOMs are hard-coded.
	 */
	BOMHelper() {
		boms.put(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
				Charset.forName("UTF-8"));
		boms.put(new byte[] {(byte) 0xFE,  (byte) 0xFF},
				Charset.forName("UTF-16BE"));
		boms.put(new byte[] {(byte) 0xFF,  (byte) 0xFE},
				Charset.forName("UTF-16LE"));
	}// constructor
	
	/** Return all known BOMs and their respective charsets. */
	public Map<byte[], Charset> availableBOMs() {
		return boms;
	}// availableBOMs
	
	/** Returns the length of the longest BOM. */
	public int getMaxBOM() {
		int maxBOM = 0;
		for (byte[] bom : boms.keySet()) {
			if (bom.length > maxBOM)
				maxBOM = bom.length;
		}// for
		return maxBOM;
	}// getMaxBOM
	
	/** Check whether bytes match the given BOM.
	 * 
	 * @param bom	The bytes of the BOM to look for.
	 * @param bytes	The bytes to test. Must have a length bigger or equal to
	 * 				<code>bom</code>'s length.
	 * 
	 * @return		<code>true</code> if <code>bytes</code> begins with all the
	 * 				bytes of <code>bom</code>.
	 */
	public boolean matches(byte[] bom, byte[] bytes) {
		for (int i=0; i<bom.length; i++) {
			if (bom[i] != bytes[i])					// Not the same bytes
				return false;						// No matching
		}// for
		return true;
	}// matches

}
