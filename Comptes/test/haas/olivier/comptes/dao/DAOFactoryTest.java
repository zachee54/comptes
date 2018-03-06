package haas.olivier.comptes.dao;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.PermanentFixe;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.PermanentDAO;
import haas.olivier.comptes.dao.SuiviDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class DAOFactoryTest {

	/**
	 * Des mois.
	 */
	private Month month1 = new Month(), month2 = month1.getNext();
	
	/**
	 * Des montants.
	 */
	private BigDecimal d1 = BigDecimal.TEN, d2 = new BigDecimal("20");
	
	/**
	 * Des fabriques mockées.
	 */
	@Mock
	private DAOFactory factory1, factory2;
	
	/**
	 * Des accès mockés aux comptes.
	 */
	@Mock
	private CompteDAO cDAO1, cDAO2;
	
	/**
	 * Des accès mockés aux écritures.
	 */
	@Mock
	private EcritureDAO eDAO1, eDAO2;
	
	/**
	 * Des accès mockés aux opérations permanentes.
	 */
	@Mock
	private PermanentDAO pDAO1, pDAO2;
	
	/**
	 * Des suivis mockés.
	 */
	@Mock
	private SuiviDAO hDAO1, hDAO2, sDAO1, sDAO2, mDAO1, mDAO2;
	
	/**
	 * Des comptes.
	 */
	private final Compte compte1 = new Compte(TypeCompte.DEPENSES_EN_EPARGNE),
			compte2 = new Compte(TypeCompte.EMPRUNT);
	
	/**
	 * Une écriture.
	 */
	private Ecriture ecriture;
	
	/**
	 * Une opération permanente.
	 */
	private final Permanent permanent = new PermanentFixe(3, null, compte2,
			compte1, "", "", false, Collections.<Month,Integer>emptyMap(),
			Collections.<Month,BigDecimal>emptyMap());
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);	// Créer les Mocks
		
		// Comptes mockés
		Set<Compte> comptes = Collections.<Compte>singleton(compte1);
		
		// Écritures mockées
		ecriture = new Ecriture(2, new Date(), null, compte1, compte2,
				BigDecimal.ZERO, null, null, null);
		Set<Ecriture> ecritures = Collections.<Ecriture>singleton(ecriture);
		
		// Permanents mockés
		Set<Permanent> permanents =
				Collections.<Permanent>singleton(permanent);
		
		// Suivis mockés
		Map<Month, Map<Compte,BigDecimal>> suivi = new HashMap<>();
		suivi.put(month1, new HashMap<>());
		suivi.put(month2, new HashMap<>());
		suivi.get(month1).put(compte1, d1);
		suivi.get(month1).put(compte2, d2);
		suivi.get(month2).put(compte1, d2);
		
		// Fabriques mockées
		when(factory1.getCompteDAO()).thenReturn(cDAO1);
		when(factory2.getCompteDAO()).thenReturn(cDAO2);
		when(factory1.getEcritureDAO()).thenReturn(eDAO1);
		when(factory2.getEcritureDAO()).thenReturn(eDAO2);
		when(factory1.getPermanentDAO()).thenReturn(pDAO1);
		when(factory2.getPermanentDAO()).thenReturn(pDAO2);
		when(factory1.getHistoriqueDAO()).thenReturn(hDAO1);
		when(factory2.getHistoriqueDAO()).thenReturn(hDAO2);
		when(factory1.getSoldeAVueDAO()).thenReturn(sDAO1);
		when(factory2.getSoldeAVueDAO()).thenReturn(sDAO2);
		when(factory1.getMoyenneDAO()).thenReturn(mDAO1);
		when(factory2.getMoyenneDAO()).thenReturn(mDAO2);
		when(cDAO1.getAll()).thenReturn(comptes);
		when(eDAO1.getAll()).thenReturn(ecritures);
		when(pDAO1.getAll()).thenReturn(permanents);
		when(hDAO1.getAll()).thenReturn(suivi);
		when(sDAO1.getAll()).thenReturn(suivi);
		when(mDAO1.getAll()).thenReturn(suivi);
		when(cDAO2.getAll()).thenReturn(comptes);
		when(eDAO2.getAll()).thenReturn(ecritures);
		when(pDAO2.getAll()).thenReturn(permanents);
		when(hDAO2.getAll()).thenReturn(suivi);
		when(sDAO2.getAll()).thenReturn(suivi);
		when(mDAO2.getAll()).thenReturn(suivi);
	}
	
	/**
	 * Teste qu'il y a toujours une fabrique, même sans définition explicite.
	 */
	@Test
	public void testGetFactoryNull() {
		assertNotNull(DAOFactory.getFactory());
	}

	/**
	 * Teste la définition et redéfinition simples d'une fabrique.
	 */
	@Test
	public void testSetFactoryDAOFactory() throws IOException {
		DAOFactory.setFactory(factory1);
		assertSame(factory1, DAOFactory.getFactory());
		
		DAOFactory.setFactory(factory2);
		verifyZeroInteractions(factory1);
		assertSame(factory2, DAOFactory.getFactory());
	}

	/**
	 * Teste la définition et redéfinition simples d'une fabrique.
	 */
	@Test
	public void testSetFactoryDAOFactoryBooleanFalse() throws IOException {
		DAOFactory.setFactory(factory1);
		assertSame(factory1, DAOFactory.getFactory());
		
		DAOFactory.setFactory(factory2);
		verifyZeroInteractions(factory1);
		assertSame(factory2, DAOFactory.getFactory());
	}
	
	/**
	 * Teste la bascule d'une fabrique à l'autre.
	 */
	@Test
	public void testSetFactoryDAOFactoryBoolean() throws Exception {
		DAOFactory.setFactory(factory1, false);
		
		// Méthode testée : transférer les données
		DAOFactory.setFactory(factory2, true);
		
		assertSame(factory2, DAOFactory.getFactory());
		
		verify(factory2).erase();
		verify(cDAO2).add(compte1);
		verify(eDAO2).add(ecriture);
		verify(pDAO2).add(permanent);
		
		verify(hDAO2).set(compte1, month1, d1);
		verify(hDAO2).set(compte2, month1, d2);
		verify(hDAO2).set(compte1, month2, d2);
		
		verify(sDAO2).set(compte1, month1, d1);
		verify(sDAO2).set(compte2, month1, d2);
		verify(sDAO2).set(compte1, month2, d2);
		
		verify(mDAO2).set(compte1, month1, d1);
		verify(mDAO2).set(compte2, month1, d2);
		verify(mDAO2).set(compte1, month2, d2);
	}
}
