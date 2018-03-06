package haas.olivier.comptes.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import haas.olivier.comptes.dao.csv.CsvDAO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class CsvDAOTest {

	private static final String FILENAME = "test/testcomptestmp.csv";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// Effacer le fichier temporaire
		new File(FILENAME).delete();
	}

	@Before
	public void setUp() throws Exception {

		// Lire le fichier original
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				new File("test/testcomptes.csv")));

		// Ecrire dans un fichier temporaire (écraser la version précédente)
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(FILENAME, false));

		// Copier le fichier "en dur"
		int b;
		while ((b = in.read()) != -1) {
			out.write(b);
		}

		// Fermer les ressources
		out.close();
		in.close();
	}

	@After
	public void tearDown() throws Exception {
		// Effacer le fichier temporaire
		new File(FILENAME).delete();
	}

	@Test
	public void testGetReader() throws FileNotFoundException, IOException {
		CsvReader reader = CsvDAO.getReader(new char[0]);
		assertNotNull(reader);
		reader.close();
	}

	@Test
	public void testGetWriter() throws IOException {
		// Créer un Writer
		CharArrayWriter out = new CharArrayWriter();
		CsvWriter writer = CsvDAO.getWriter(out);
		writer.write("test");

		// Vérifier
		assertEquals(4, out.toCharArray().length);

		// Nettoyer
		writer.close();
	}

	@Test
	public void testClose() throws IOException {
		CsvWriter writer = mock(CsvWriter.class);
		CsvReader reader = mock(CsvReader.class);

		// Pas d'exception si le fichier temporaire n'existe pas
		CsvDAO.close(reader, writer);

		// Vérifier la clôture du reader et du writer
		verify(reader).close();
		verify(writer).close();
	}
}
