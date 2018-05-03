/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Checksum {
	
	private Checksum() {
	}
	
	/**
	 * Renvoie la clé MD5 du fichier spécifié.
	 *
	 * @param file	Le fichier.
	 * @return		La clé MD5 du fichier.
	 * 
	 * @throws IOException
	 */
	public static String md5(File file) throws IOException {
		return checksum(file, "MD5");
	}
	
	/**
	 * La somme du contrôle d'un fichier selon l'algorithme spécifié.
	 *
	 * @param file		Le fichier.
	 * 
	 * @param algorithm	Le nom de l'algorithme, au sens de
	 * 					{@link java.security.MessageDigest#getInstance(String)}.
	 * 
	 * @return			La somme de contrôle.
	 * 
	 * @throws IOException
	 */
	public static String checksum(File file, String algorithm)
			throws IOException {
		try {
			return digest(file, MessageDigest.getInstance(algorithm));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Algorithme indisponible", e);
		}
	}
	
	/**
	 * Calcule la somme de contrôle d'un fichier.
	 *
	 * @param file	Le fichier.
	 * @param md	Le calculateur de somme de contrôle.
	 * 
	 * @return		La somme de contrôle.
	 * 
	 * @throws IOException
	 */
	private static String digest(File file, MessageDigest md)
			throws IOException {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			DigestInputStream in = new DigestInputStream(
					new BufferedInputStream(fileInputStream), md);
			
			fullyReadStream(in);
			return getContentHash(in.getMessageDigest());
			
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
	}
	
	/**
	 * Parcourt un flux de lecture jusqu'à la fin sans tenir compte de son
	 * contenu.
	 *
	 * @param in	Le flux à parcourir.
	 * 
	 * @throws IOException
	 */
	private static void fullyReadStream(InputStream in) throws IOException {
		int c;
		do {
			c = in.read();
		} while (c != -1);
	}

	/**
	 * Renvoie la clé de hachage du contenu spécifié.
	 *
	 * @param messageDigest	Le contenu haché.
	 * 
	 * @return	La clé de hachage.
	 */
	private static String getContentHash(MessageDigest messageDigest) {
		StringBuilder s = new StringBuilder();
		for (byte b : messageDigest.digest()) {
			s.append(String.format("%02X", b));
		}
		return s.toString();
	}
}
