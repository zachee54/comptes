package haas.olivier.fxcpta.test;

import static org.junit.Assert.*;

import haas.olivier.info.LogMessageListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LogHandlerTest {
	
	private File f = new File("logtest");
	private LogMessageListener lh;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		f.delete();						// Effacer le fichier
		lh = new LogMessageListener(f);	// Objet testé
	}// setUp

	@After
	public void tearDown() throws Exception {
		if (f.exists()) f.delete();
	}
	
	/** Indique si le fichier test contient la chaîne spécifiée. 
	 * @throws IOException */
	private boolean logContains(String s) throws IOException {
		BufferedReader r = null;
		try{
			r = new BufferedReader(new FileReader(f));	// Lire
		String ligne = null;
		while ((ligne = r.readLine()) != null) {			// Chaque ligne
			if (ligne.contains(s)) return true;				// Contient-t-elle ?
		}// while ligne
		return false;										// Pas trouvé
		} finally {
			if (r != null) r.close();
		}// try
	}// logContains

	@Test
	public void testError() throws IOException {
		String s = "mon erreur";
		lh.error(s, new Exception());	// Méthode testée
		assertTrue(logContains(s));		// Vérification
	}// testError

	@Test
	public void testWarning() throws IOException {
		String s = "mon avertissement";
		lh.warning(s, new Exception());	// Méthode testée
		assertTrue(logContains(s));		// Vérification
	}// testWarning

	@Test
	public void testInfo() throws IOException {
		String s = "mon info";
		lh.info(s);						// Méthode testée
		assertFalse(f.exists());		// Pas besoin des infos dans un log
	}// testInfo

	@Test
	public void testSuccessifs() throws IOException {
		String s1 = "erreur1",
				s2 = "avertissement",
				s3 = "erreur3",
				info = "info";
		
		// Méthodes testées
		lh.error(s1, new Exception());
		lh.warning(s2, new Exception());
		lh.info(info);					// Sans effet en principe
		lh.error(s3, new Exception());
		
		// Vérifications
		assertTrue(logContains(s1));
		assertTrue(logContains(s2));
		assertTrue(logContains(s3));
		assertFalse(logContains(info));
	}// testSuccessifs
}
