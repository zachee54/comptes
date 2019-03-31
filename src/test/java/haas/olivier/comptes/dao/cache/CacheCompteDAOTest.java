/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.cache;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacheCompteDAOTest {

	/**
	 * Des comptes.
	 */
	private static final Collection<Compte> comptes = new HashSet<>();
	
	private static final Compte c1 = new Compte(1, TypeCompte.COMPTE_CARTE);
	private static final Compte c2 = new Compte(2, TypeCompte.RECETTES_EN_EPARGNE);
	private static final Compte c3 = new Compte(3, TypeCompte.COMPTE_EPARGNE);
	
	/**
	 * Source de données mockée.
	 */
	private static final CacheableDAOFactory cacheable =
			mock(CacheableDAOFactory.class);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Collection des comptes utilisés pour le test
		comptes.add(c1);
		comptes.add(c2);
		comptes.add(c3);
		
		// Définir le comportement de la source mockée
		when(cacheable.getBanques()).thenReturn(
				Collections.<Banque>emptyIterator());
		when(cacheable.getHistorique()).thenReturn(
				Collections.<Solde>emptyIterator());
		when(cacheable.getMoyennes()).thenReturn(
				Collections.<Solde>emptyIterator());
		when(cacheable.getSoldesAVue()).thenReturn(
				Collections.<Solde>emptyIterator());
		when(cacheable.getPermanents((CachePermanentDAO) any())).thenReturn(
				Collections.<Permanent>emptyIterator());
		when(cacheable.getProperties()).thenReturn(
				mock(CacheablePropertiesDAO.class));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	/**
	 * La fabrique.
	 */
	private DAOFactory factory;
	
	/**
	 * Objet testé.
	 */
	private CompteDAO dao;

	@Before
	public void setUp() throws Exception {
		
		Collection<Ecriture> ecritures = new ArrayList<>();
		ecritures.add(new Ecriture(1, new Date(), null, c1, c2, BigDecimal.ONE, null, null, null));
		ecritures.add(new Ecriture(2, new Date(), null, c2, c3, BigDecimal.TEN, null, null, null));
		when(cacheable.getEcritures()).thenReturn(
				ecritures.iterator());
		
		/*
		 * Utiliser un nouvel itérateur des comptes à chaque test (sinon, il ne
		 * marche que pour le premier test et ensuite il ne renvoie plus rien !)
		 */
		when(cacheable.getComptes()).thenReturn(comptes.iterator());
		
		// Fabrique
		factory = new CacheDAOFactory(cacheable);
		
		// Objet testé
		dao = factory.getCompteDAO();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAll() throws IOException {
		Collection<Compte> result = dao.getAll();
		assertEquals(3, result.size());
		for (Compte c : result)
			assertTrue(comptes.contains(c));
	}
	
	@Test
	public void testAdd() throws IOException {
		Compte c4 = new Compte(4, TypeCompte.COMPTE_EPARGNE);
		
		// Méthode testée
		dao.add(c4);
		
		Collection<Compte> all = dao.getAll();
		assertTrue(all.contains(c4));
		assertEquals(4, all.size());
	}
	
	@Test
	public void testRemove() throws IOException {
		dao.remove(c2);
		
		Collection<Compte> all = dao.getAll();
		assertFalse(all.contains(c2));
		assertEquals(2, all.size());
	}

}
