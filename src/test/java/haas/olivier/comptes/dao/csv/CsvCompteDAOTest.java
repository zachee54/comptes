/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.csv;

import static org.junit.Assert.*;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class CsvCompteDAOTest {

	/**
	 * Collection contenant le jeu de données.
	 */
	private static List<Compte> comptes = new ArrayList<>(3);
	
	/**
	 * Le séparateur de champs utilisé pour le test.
	 */
	private static final char DELIMITER = '|';
	
	/**
	 * Sauvegarde le jeu de données à partir de la classe testée (ce qui revient
	 * à tester la méthode
	 * {@link haas.olivier.comptes.dao.csv.CsvCompteDAO#save(java.util.Iterator, CsvWriter)})
	 * et instancie un objet capable de relire les données sauvegardées.
	 * 
	 * @return	Un <code>Reader</code> prêt à relire les données sauvegardées
	 * 			par la classe testée.
	 * 
	 * @throws IOException
	 */
	private static Reader reRead() throws IOException {
		
		// Un buffer pour stocker les données écrites
		CharArrayWriter writer = new CharArrayWriter();
		
		// Écrire le jeu de données avec la classe testée
		CsvCompteDAO.save(comptes, new CsvWriter(writer, DELIMITER));
		
		// Renvoyer un lecteur
		return new CharArrayReader(writer.toCharArray());
	}

	/**
	 * Vérifie que le <code>Reader</code> spécifié a bien été fermé.
	 * <p>
	 * En pratique, on vérifie qu'une tentative de lecture lève une
	 * <codE>IOException</code>, parce qu'il n'y a pas de méthode spécifique
	 * pour vérifier la fermeture.
	 */
	private static void checkClosed(Reader reader) {
		try {
			reader.read();
			fail("Aurait dû lever une IOException: le flux n'a pas été fermé");
		} catch (IOException e) {
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Constituer le jeu de données
		Compte compte1 = new Compte(1, TypeCompte.RECETTES);
		compte1.setNom("compte1");
		compte1.setOuverture(new Date());
		
		Compte compte2 = new Compte(2, TypeCompte.COMPTE_COURANT);
		compte2.setNom("compte2");
		compte2.setNumero(150125879823L);
		
		Compte compte3 = new Compte(3, TypeCompte.DEPENSES_EN_EPARGNE);
		compte3.setNom("compte3");
		compte3.setCloture(new Date());
		
		comptes.add(compte1);
		comptes.add(compte2);
		comptes.add(compte3);
	}
	
	/**
	 * Teste ce qu'il se passe si aucun <code>CsvReader</code> n'est spécifié à
	 * l'instanciation.
	 */
	@Test
	public void testReaderNull() throws IOException {
		
		// Méthode testée
		@SuppressWarnings("resource")
		CsvCompteDAO dao = new CsvCompteDAO(null);
		
		// Vérifier le comportement
		assertFalse(dao.hasNext());
		try {
			dao.next();
			fail("Aurait dû lever une NoSuchElementException");
		} catch (NoSuchElementException e) {
		}
	}
	
	/**
	 * Teste l'écriture puis la relecture de comptes.
	 */
	@Test
	public void test() throws IOException {
		
		// Écrire les données
		Reader reader = reRead();
		
		// Relire les données écrites
		@SuppressWarnings("resource")
		CsvCompteDAO dao = new CsvCompteDAO(				// Objet testé
				new CsvReader(reader, DELIMITER));
		
		// Vérifier leur contenu
		List<Compte> sol = new ArrayList<>(comptes);		// Solution
		// Tant qu'il reste des comptes à trouver...
		while (!sol.isEmpty()) {
			//...l'objet testé affirme en avoir...
			assertTrue(dao.hasNext());
			//...et ce qu'il renvoie fait partie de la solution
			assertTrue(sol.remove(dao.next()));
		}
		
		// Rien de plus que ce qu'il fallait
		assertFalse(dao.hasNext());
		
		// Vérifier que le flux a été fermé automatiquement
		checkClosed(reader);
	}

	@Test
	public void testClose() throws IOException {
		
		// Préparer un objet qui va lire des données
		Reader reader = reRead();				// Lecteur de données écrites
		CsvCompteDAO dao = new CsvCompteDAO(new CsvReader(reader, DELIMITER));
		
		// Lire deux comptes
		dao.next();
		dao.next();
		
		// Fermer le flux
		dao.close();
		
		// Vérifier que le Reader a été fermé
		checkClosed(reader);
	}

}
