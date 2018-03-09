/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Checksum {
	
	public static String md5(File file)
			throws NoSuchAlgorithmException, IOException {
		return checksum(file, "MD5");
	}// md5
	
	public static String checksum(File file, String algorithm)
			throws NoSuchAlgorithmException, IOException {
		return digest(file, MessageDigest.getInstance(algorithm));
	}// checksum
	
	private static String digest(File file, MessageDigest md)
			throws IOException {
		DigestInputStream in = null;
		try {
			in = new DigestInputStream(
					new BufferedInputStream(new FileInputStream(file)),
					md);
			while (in.read() != -1);
			StringBuilder s = new StringBuilder();
			for (byte b : in.getMessageDigest().digest())
				s.append(String.format("%02X", b));
			return s.toString();

		} finally {
			if (in != null) in.close();
		}// try
	}// digest
}
