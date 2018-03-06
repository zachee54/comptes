package haas.olivier.comptes.dao.csv;

import static org.junit.Assert.*;

import haas.olivier.util.Month;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.BeforeClass;
import org.junit.Test;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class CsvSuiviDAOTest {

	/** Le délimiteur utilisé pour la sauvegarde CSV. */
	private static final char DELIMITER = '|';
	
	/** Une collection de données de suivi. */
	private static final Map<Month, Map<Integer, BigDecimal>> data =
			new HashMap<>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Générer des données quelconques pour plusieurs mois
		Month month = new Month();
		Map<Integer, BigDecimal> subMap = new HashMap<>();
		subMap.put(1, BigDecimal.ONE);
		subMap.put(3, BigDecimal.TEN);
		subMap.put(-1, BigDecimal.TEN.negate());
		data.put(month, subMap);
		
		month = month.getNext();
		subMap = new HashMap<>();
		subMap.put(0, new BigDecimal("-42397.02"));
		subMap.put(5, new BigDecimal("2330"));
		data.put(month, subMap);
		
		month = month.getTranslated(2);
		subMap = new HashMap<>();
		subMap.put(1, BigDecimal.ONE);
		subMap.put(3, BigDecimal.TEN.negate());
		subMap.put(-1, new BigDecimal("9324.3"));
		data.put(month, subMap);
	}// setUpBeforeClass

	@Test
	public void testSave() throws IOException {
		CharArrayWriter out = null;
		CsvWriter writer = null;
		CsvReader reader = null;
		try {
			// Préparer un flux de sortie écrivant en mémoire
			out = new CharArrayWriter();
			writer = new CsvWriter(out, DELIMITER);
			
			// Méthode testée n°1
			CsvSuiviDAO.save(data, writer);
			
			// Préparer un flux pour relire ce qui a été écrit
			reader = new CsvReader(
					new CharArrayReader(out.toCharArray()),
					DELIMITER);
			
			// Méthode testée n°2
			@SuppressWarnings("resource")
			CsvSuiviDAO dao = new CsvSuiviDAO(reader);
			
			// Vérifier le contenu
			int n = 0;									// Compteur
			while (dao.hasNext()) {
				Entry<Month, Entry<Integer, BigDecimal>> e = dao.next();
				assertTrue(data.containsKey(e.getKey()));
				assertEquals(0,
						data.get(e.getKey()).get(e.getValue().getKey())
						.compareTo(e.getValue().getValue()));
				n++;
			}// while
			
			// Vérifier qu'il n'y a rien de plus que ce qu'on y a mis
			assertTrue(n == 8);
			
		} finally {
			if (writer != null) writer.close();
			if (out != null) out.close();
		}// try
	}// testSave
}
