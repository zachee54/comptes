/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.xml;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentSoldeur;
import haas.olivier.comptes.dao.cache.CachePermanentDAO;
import haas.olivier.comptes.dao.xml.JaxbPermanentDAO;
import haas.olivier.util.Month;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JaxbPermanentDAOTest {

	/**
	 * Collection des objets à manipuler.
	 */
	private final Collection<Permanent> permanents = new ArrayList<>();
	
	@Mock
	private Compte c1, c2, c3;
	
	/**
	 * Un cache pour les opérations permanentes, permettant de retrouver des
	 * objets lus précédemment.
	 */
	@Mock
	private CachePermanentDAO cache;
	
	/**
	 * Une collection des comptes par identifiants.
	 */
	private final Map<Integer, Compte> comptes = new HashMap<>();
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		// Comportement des comptes
		when(c1.getId()).thenReturn(1);
		when(c2.getId()).thenReturn(2);
		when(c3.getId()).thenReturn(3);
		
		// La collection des comptes
		comptes.put(1, c1);
		comptes.put(2, c2);
		comptes.put(3, c3);
		
		// Des mois
		Month month = Month.getInstance(),
				month2 = month.getNext(),
				month3 = month2.getNext(),
				month4 = month3.getTranslated(5),
				month0 = month.getTranslated(-14);
		
		// Définir les opérations permanentes
		Permanent p1 = new Permanent(1, "permanent<1", c1, c2, "libellé<1", "tiers1", false, new HashMap<Month, Integer>());
		PermanentFixe p1State = new PermanentFixe(new HashMap<Month, BigDecimal>());
		p1.setState(p1State);
		Permanent p2 = new Permanent(2, "permanent2", c2, c1, "libellé&2", "tiers2", true, new HashMap<Month, Integer>());
		p2.setState(new PermanentProport(p1, new BigDecimal("0.2")));
		Permanent p3 = new Permanent(3, "permanent3", c3, c2, "libellé3", "tiers3", true, new HashMap<Month, Integer>());
		p3.setState(new PermanentSoldeur(p3.debit));
		
		// Ajouter dans la collection
		permanents.add(p1);
		permanents.add(p2);
		permanents.add(p3);
		
		// Définir des données plus précises
		p1.jours.put(month, 15);
		p1.jours.put(month2, 2);
		p1.jours.put(month4, 0);
		p1State.montants.put(month0, new BigDecimal("514623.1"));
		p1State.montants.put(month4, BigDecimal.TEN.negate());
		p2.jours.put(month3, -15);
		p3.jours.put(month, 7);
		p3.jours.put(month4, 45);
	}
	
	@Test
	public void testSave() throws IOException, XMLStreamException, FactoryConfigurationError {
		InputStream reader = null;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			
			// Méthode testée n°1 : sauvegarde
			JaxbPermanentDAO.save(permanents.iterator(), out);
			
			// Relecteur
			reader = new ByteArrayInputStream(out.toByteArray());
			
			// Méthode testée n°2 : relecture
			JaxbPermanentDAO dao = new JaxbPermanentDAO(reader, cache, comptes);
			
			/*
			 * Parcourir les opérations relues.
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
			}
			
			assertFalse(dao.hasNext());					// Rien de plus
			
			// Vérifier que toutes les opérations ont été réinstanciées
			for (Permanent p : permanents)
				assertTrue(resultat.contains(p));
			
		} finally {
			if (reader != null) reader.close();
		}
	}

}
