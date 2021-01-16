/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.csv;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.dao.cache.CacheSuiviDAO;
import haas.olivier.comptes.dao.cache.Solde;
import haas.olivier.util.Month;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class CsvSuiviDAOTest {

	/**
	 * Le délimiteur utilisé pour la sauvegarde CSV.
	 */
	private static final char DELIMITER = '|';
	
	/**
	 * Une collection de données de suivi.
	 */
	private static final CacheSuiviDAO CACHE = mock(CacheSuiviDAO.class);
	
	/**
	 * Une Map des comptes.
	 */
	private static final Map<Integer, Compte> COMPTES = new HashMap<>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Compte compteSpecial = mock(Compte.class);
		Compte compte0 = mock(Compte.class);
		Compte compte1 = mock(Compte.class);
		Compte compte3 = mock(Compte.class);
		Compte compte5 = mock(Compte.class);
		
		when(compteSpecial.getId()).thenReturn(-1);
		when(compte0.getId()).thenReturn(0);
		when(compte1.getId()).thenReturn(1);
		when(compte3.getId()).thenReturn(3);
		when(compte5.getId()).thenReturn(5);
		
		COMPTES.put(-1, compteSpecial);
		COMPTES.put(0, compte0);
		COMPTES.put(1, compte1);
		COMPTES.put(3, compte3);
		COMPTES.put(5, compte5);
		
		// Des données quelconques pour plusieurs mois
		Month month = Month.getInstance();
		when(CACHE.get(compte1, month)).thenReturn(BigDecimal.ONE);
		when(CACHE.get(compte3, month)).thenReturn(BigDecimal.TEN);
		when(CACHE.get(compteSpecial, month))
		.thenReturn(BigDecimal.TEN.negate());
		
		Month monthNext1 = month.getNext();
		when(CACHE.get(compte0, monthNext1))
		.thenReturn(new BigDecimal("-42397.02"));
		when(CACHE.get(compte5, monthNext1)).thenReturn(new BigDecimal("2330"));
		
		Month monthNext2 = month.getTranslated(2);
		when(CACHE.get(compte1, monthNext2)).thenReturn(BigDecimal.ONE);
		when(CACHE.get(compte3, monthNext2))
		.thenReturn(BigDecimal.TEN.negate());
		when(CACHE.get(compteSpecial, monthNext2))
		.thenReturn(new BigDecimal("9324.3"));
		
		// Les comptes en cache
		when(CACHE.getComptes()).thenReturn(new ArrayList<>(COMPTES.values()));
		
		// Les mois en cache
		when(CACHE.getMonths()).thenReturn(
				Arrays.asList(new Month[] {month, monthNext1, monthNext2}));
	}

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
			CsvSuiviDAO.save(CACHE, writer);
			
			// Préparer un flux pour relire ce qui a été écrit
			reader = new CsvReader(
					new CharArrayReader(out.toCharArray()),
					DELIMITER);
			
			// Méthode testée n°2
			try (CsvSuiviDAO dao = new CsvSuiviDAO(reader, COMPTES)) {

				// Vérifier le contenu
				int n = 0;									// Compteur
				while (dao.hasNext()) {
					Solde solde = dao.next();
					assertEquals(0,
							CACHE.get(solde.compte, solde.month)
							.compareTo(solde.montant));
					n++;
				}

				// Vérifier qu'il n'y a rien de plus que ce qu'on y a mis
				assertEquals(8, n);
			}
			
		} finally {
			if (writer != null) writer.close();
			if (out != null) out.close();
		}
	}
}
