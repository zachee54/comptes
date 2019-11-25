package haas.olivier.comptes.dao.cache.hibernate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.cache.CachePermanentDAO;
import haas.olivier.comptes.dao.cache.CacheableDAOFactory;
import haas.olivier.comptes.dao.cache.CacheablePropertiesDAO;
import haas.olivier.comptes.dao.cache.Solde;

/**
 * HibernateDAO.java
 * DOCUMENTEZ_MOI
 * @author Olivier HAAS
 * Date: 15 oct. 2019
 */
public class HibernateCacheableDAO implements CacheableDAOFactory {

	/**
	 * Contexte JPA généré dynamiquement.
	 */
	@PersistenceUnit
	private final EntityManagerFactory entityManagerFactory;
	
	/**
	 * Session JPA.
	 */
	private final EntityManager entityManager;
	
	public HibernateCacheableDAO(String url, String driver) {
		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.url", url);
		properties.put("javax.persistence.jdbc.driver", driver);
		entityManagerFactory = Persistence.createEntityManagerFactory(
				"haas.olivier.comptes", properties);
		entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
	}
	
	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#getBanques()
	 */
	@Override
	public Iterator<Banque> getBanques() throws IOException {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#getComptes()
	 */
	@Override
	public Iterator<Compte> getComptes() throws IOException {
		return entityManager.createQuery(
				"select c from Compte c", Compte.class)
				.getResultList()
				.iterator();
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#getEcritures()
	 */
	@Override
	public Iterator<Ecriture> getEcritures() throws IOException {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#getPermanents(haas.olivier.comptes.dao.cache.CachePermanentDAO)
	 */
	@Override
	public Iterator<Permanent> getPermanents(CachePermanentDAO cache)
			throws IOException {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#getHistorique()
	 */
	@Override
	public Iterator<Solde> getHistorique() throws IOException {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#getSoldesAVue()
	 */
	@Override
	public Iterator<Solde> getSoldesAVue() throws IOException {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#getMoyennes()
	 */
	@Override
	public Iterator<Solde> getMoyennes() throws IOException {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#getProperties()
	 */
	@Override
	public CacheablePropertiesDAO getProperties() throws IOException {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#save(haas.olivier.comptes.dao.cache.CacheDAOFactory)
	 */
	@Override
	public void save(CacheDAOFactory cache) throws IOException {
		for (Compte compte : cache.getCompteDAO().getAll()) {
			if (entityManager.contains(compte)) {
				entityManager.merge(compte);
			} else {
				entityManager.persist(compte);
			}
		}
		entityManager.getTransaction().commit();
		entityManager.getTransaction().begin();
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#getName()
	 */
	@Override
	public String getName() {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#getSource()
	 */
	@Override
	public String getSource() {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#getSourceFullName()
	 */
	@Override
	public String getSourceFullName() {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.cache.CacheableDAOFactory#canBeSaved()
	 */
	@Override
	public boolean canBeSaved() {
		return false;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	@Override
	public void close() {
		entityManagerFactory.close();
	}
}
