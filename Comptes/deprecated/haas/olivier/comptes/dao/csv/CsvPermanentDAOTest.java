package haas.olivier.comptes.dao.csv;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.cache.CachePermanentDAO;
import haas.olivier.comptes.dao.csv.CsvPermanentDAO;
import haas.olivier.util.Month;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class CsvPermanentDAOTest {

	/** Séparateur de champs. */
	private static final char DELIMITER = '|';
	
	/** Collection des objets à manipuler. */
	private final Collection<Permanent> permanents = new ArrayList<>();
	
	/** Des comptes. */
	@Mock private Compte c1, c2;
	@Mock private CompteBancaire c3;
	
	/** Un cache pour les opérations permanentes, permettant de retrouver des
	 * objets lus précédemment.
	 */
	@Mock private CachePermanentDAO cache;
	
	/** Un objet d'accès aux comptes. */
	@Mock private CompteDAO cDAO;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		// Comportement des comptes
		when(c1.getId()).thenReturn(1);
		when(c2.getId()).thenReturn(2);
		when(c3.getId()).thenReturn(3);
		
		// Comportement du mock CompteDAO
		when(cDAO.get(1)).thenReturn(c1);
		when(cDAO.get(2)).thenReturn(c2);
		when(cDAO.get(3)).thenReturn(c3);
		
		// Des mois
		Month month = new Month(),
				month2 = month.getNext(),
				month3 = month2.getNext(),
				month4 = month3.getTranslated(5),
				month0 = month.getTranslated(-14);
		
		// Définir les opérations permanentes
		PermanentFixe p1 = new PermanentFixe(1, "permanent1", c1, c2, "libellé1", "tiers1", false, new HashMap<Month, Integer>(), new HashMap<Month, BigDecimal>());
		Permanent p2 = new PermanentProport(2, "permanent2", c2, c1, "libellé2", "tiers2", true, new HashMap<Month, Integer>(), p1, new BigDecimal("0.2")),
				p3 = new PermanentSoldeur(3, "permanent3", c3, c2, "libellé3", "tiers3", true, new HashMap<Month, Integer>());
		
		// Ajouter dans la collection
		permanents.add(p1);
		permanents.add(p2);
		permanents.add(p3);
		
		// Définir des données plus précises
		p1.jours.put(month, 15);
		p1.jours.put(month2, 2);
		p1.jours.put(month4, 0);
		p1.montants.put(month0, new BigDecimal("514623.1"));
		p1.montants.put(month4, BigDecimal.TEN.negate());
		p2.jours.put(month3, -15);
		p3.jours.put(month, 7);
		p3.jours.put(month4, 45);
	}// setUp
	@Test
	public void testSave() throws IOException {
		CharArrayWriter out = null;
		CsvWriter writer = null;
		CsvReader reader = null;
		try {
			out = new CharArrayWriter();
			writer = new CsvWriter(out, DELIMITER);
			
			// Méthode testée n°1 : sauvegarde
			CsvPermanentDAO.save(permanents.iterator(), writer);
			writer.flush();
			
			// Relecteur
			reader = new CsvReader(new CharArrayReader(out.toCharArray()),
					DELIMITER);
			
			// Méthode testée n°2 : relecture
			CsvPermanentDAO dao = new CsvPermanentDAO(reader, cache, cDAO);
			
			/* Parcourir les opérations relues.
			 * Au fur et à mesure de la relecture, on les référence dans le
			 * cache mocké
			 */
			Collection<Permanent> resultat = new ArrayList<>();
			for (int n=0; n<permanents.size(); n++) {
				assertTrue(dao.hasNext());				// On peut lire la suite
				Permanent p = dao.next();				// Obtenir l'opération
				resultat.add(p);						// Stocker l'opération
				
				// Simuler son stockage dans le cache
				when(cache.get(p.id)).thenReturn(p);
			}// for
			
			assertFalse(dao.hasNext());					// Rien de plus
			
			// Vérifier que toutes les opérations ont été réinstanciées
			for (Permanent p : permanents)
				assertTrue(resultat.contains(p));
			
		} finally {
			if (reader != null) reader.close();
			if (writer != null) writer.close();
			if (out != null) out.close();
		}// try
	}// save

}
