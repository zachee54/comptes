/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.csv;

import static org.junit.Assert.*;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.TypeCompte;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class CsvEcritureDAOTest {

	/**
	 * Collection contenant le jeu de données.
	 */
	private static final Collection<Ecriture> ECRITURES = new ArrayList<>();
	
	/**
	 * Un objet d'accès aux comptes utiles pour cette classe.
	 */
	private static final Map<Integer, Compte> COMPTES = new HashMap<>();
	
	/**
	 * Le séparateur de champs utilisé pour les tests.
	 */
	private static final char DELIMITER = '|';
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Des dates
		DateFormat df = new SimpleDateFormat("dd/MM/yy");
		Date date1 = df.parse("24/04/16"),
				date2 = df.parse("29/02/2012"),
				date3 = df.parse("01/01/2010");
		
		// Des comptes
		Compte c1 = new Compte(1, TypeCompte.DEPENSES);
		Compte c2 = new Compte(2, TypeCompte.COMPTE_COURANT);
		c2.setNumero(65L);
		Compte c3 = new Compte(3, TypeCompte.RECETTES_EN_EPARGNE);
		Compte c4 = new Compte(4, TypeCompte.COMPTE_CARTE);
		c4.setNumero(72L);
		
		COMPTES.put(1, c1);
		COMPTES.put(2, c2);
		COMPTES.put(3, c3);
		COMPTES.put(4, c4);
		
		// Constituer le jeu de données
		ECRITURES.add(new Ecriture(1, date1, null, c1, c2, BigDecimal.TEN, "libellé1", "tiers1", 5215742));
		ECRITURES.add(new Ecriture(2, date3, date2, c2, c4, BigDecimal.ONE, "libellé2", "tiers2", null));
		ECRITURES.add(new Ecriture(3, date1, date1, c3, c1, new BigDecimal("42932.49"), "libellé3", "tiers3", 166));
	}

	/**
	 * Écrit le jeu de données et se prépare à les relire.
	 */
	private static Reader reRead() throws IOException {
		
		// Un buffer pour stocker les données écrites
		CharArrayWriter writer = new CharArrayWriter();
		
		// Écrire le jeu de données avec la classe testée
		CsvEcritureDAO.save(ECRITURES.iterator(),
				new CsvWriter(writer, DELIMITER));
		
		// Renvoyer un lecteur
		return new CharArrayReader(writer.toCharArray());
	}
	
	/**
	 * Vérifie que le lecteur est fermé.
	 * 
	 * @see	{@link haas.olivier.comptes.dao.csv.CsvCompteDAOTest#checkClosed(Reader)}
	 */
	private static void checkClosed(Reader reader) {
		try {
			reader.read();
			fail("Aurait dû lever une IOException: le flux n'est pas fermé");
		} catch (IOException e) {
		}
	}
	
	/**
	 * Teste ce qu'il se passe si aucun <code>Reader</code> et aucun
	 * <code>CompteDAO</code> ne sont spécifiés.
	 */
	@Test
	public void testReaderNull() throws IOException {
		
		// Méthode testée
		CsvEcritureDAO dao = new CsvEcritureDAO(null, null);
		
		// Vérifier le comportement
		try {
			dao.next();
			fail("Aurait dû lever une NoSuchElementException");
		} catch (NoSuchElementException e) {
		}
	}

	/**
	 * Écrit puis relit des données.
	 */
	@Test
	public void test() throws IOException {
		
		// Écrire le jeu de données
		Reader reader = reRead();
		
		// Relire les données écrites
		CsvEcritureDAO dao =
				new CsvEcritureDAO(new CsvReader(reader, DELIMITER), COMPTES);
		
		// Vérifier leur contenu
		List<Ecriture> sol = new ArrayList<>(ECRITURES);	// La solution
		// Tant qu'il reste des écritures à trouver...
		while(!sol.isEmpty()) {
			// ...l'objet testé affirme en avoir...
			assertTrue(dao.hasNext());
			// ...et ce qu'il renvoie fait partie de la solution
			assertTrue(sol.remove(dao.next()));
		}
		
		// Rien de plus que ce qu'il fallait
		assertFalse(dao.hasNext());
		
		// Vérifier que le flux a été fermé automatiquement
		checkClosed(reader);
	}

	@Test
	public void testClose() throws IOException {
		
		// Prépare un objet qui va lire les données
		Reader reader = reRead();				// Lecteur de données écrites
		CsvEcritureDAO dao =
				new CsvEcritureDAO(new CsvReader(reader, DELIMITER), COMPTES);
		
		// Lire deux écritures
		dao.next();
		dao.next();
		
		// Fermer le flux
		dao.close();
		
		// Vérifier que le Reader a été fermé
		checkClosed(reader);
	}

}
