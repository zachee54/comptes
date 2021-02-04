package haas.olivier.comptes.dao.cache.hibernate;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
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
	
	/**
	 * Le type de modèle, pour affichage utilisateur.
	 */
	private String name;
	
	/**
	 * Le nom de la source, pour affichage utilisateur.
	 */
	private String source;
	
	/**
	 * Le nom complet de la source, pour affichage utilisateur.
	 */
	private String sourceFullName;
	
	/**
	 * Construit un accès Hibernate vers une source de données.
	 * 
	 * @param url			L'URL de la source de données.
	 * @param driver		Le nom qualifié du pilote.
	 */
	public HibernateCacheableDAO(String url, String driver) {
		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.url", url);
		properties.put("javax.persistence.jdbc.driver", driver);
		entityManagerFactory = Persistence.createEntityManagerFactory(
				"haas.olivier.comptes", properties);
		entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
	}
	
	@Override
	public Iterator<Banque> getBanques() throws IOException {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	@Override
	public Iterator<Compte> getComptes() throws IOException {
		return entityManager.createQuery(
				"select c from Compte c", Compte.class)
				.getResultList()
				.iterator();
	}

	@Override
	public Iterator<Ecriture> getEcritures() throws IOException {
		return entityManager.createQuery(
				"select e from Ecriture e", Ecriture.class)
				.getResultList()
				.iterator();
	}

	@Override
	public Iterator<Permanent> getPermanents(CachePermanentDAO cache)
			throws IOException {
		return entityManager.createQuery(
				"select p from Permanent p", Permanent.class)
				.getResultList()
				.iterator();
	}

	@Override
	public Iterator<Solde> getHistorique() throws IOException {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	@Override
	public Iterator<Solde> getSoldesAVue() throws IOException {
		return null;    // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

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

	@Override
	public void save(CacheDAOFactory cache) throws IOException {
		savePojos(cache.getCompteDAO().getAll(), getComptes());
		savePojos(cache.getEcritureDAO().getAll(), getEcritures());
		savePojos(cache.getPermanentDAO().getAll(), getPermanents(null));
		entityManager.getTransaction().commit();
		entityManager.getTransaction().begin();
	}
	
	/**
	 * Sauvegarde des POJOS.
	 * @param <P>
	 * 
	 * @param pojos
	 * 			Les POJOs à sauvegarder.
	 * 
	 * @param persistedIterator
	 * 			Les POJOs connus du contexte. Ceux qui ne sont pas dans
	 * 			<code>pojos</code> seront supprimés.
	 */
	private <P> void savePojos(Iterable<P> pojos,
			Iterator<P> persistedIterator) {
		Collection<P> persisted = gatherFromIterator(persistedIterator);
		for (P pojo : pojos) {
			if (entityManager.contains(pojo)) {
				entityManager.merge(pojo);
				persisted.remove(pojo);
			} else {
				entityManager.persist(pojo);
			}
		}
		
		// Supprimer les POJOs qui n'ont pas été spécifiés
		for (P pojo : persisted) {
			entityManager.remove(pojo);
		}
	}
	
	/**
	 * Rassemble les valeurs d'un itérateur.
	 * 
	 * @param iterator	L'itérateur parcourant les objets à rassembler.
	 * 
	 * @return			Une collection des objets parcourus par l'itérateur.<br>
	 * 					<b>Pour des raisons de performance, cette collection est
	 * 					un {@link java.util.IdentityHashMap.KeySet}, qui
	 * 					implémente <code>Collection</code> mais n'en respecte
	 * 					pas précisément le contrat</b>.
	 */
	private <P> Collection<P> gatherFromIterator(Iterator<P> iterator) {
		IdentityHashMap<P, Void> map = new IdentityHashMap<>();
		while (iterator.hasNext())
			map.put(iterator.next(), null);
		return map.keySet();
	}
	
	/**
	 * Rétablit l'état des POJOs lors du dernier enregistrement.
	 */
	void reload() throws IOException {
		entityManager.clear();
		refreshPojos(getComptes());
		refreshPojos(getEcritures());
	}
	
	/**
	 * Rétablit l'état des POJOs lors du dernier enregistrement.
	 * 
	 * @param pojosIterator	Un itérateur des POJOs à rafraîchir.
	 */
	private <P> void refreshPojos(Iterator<P> pojosIterator) {
		while (pojosIterator.hasNext())
			entityManager.refresh(pojosIterator.next());
	}

	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * Modifie le type de modèle pour affichage utilisateur.
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getSource() {
		return source;
	}

	/**
	 * Modifie le nom de la source pour affichage utilisateur.
	 */
	public void setSource(String source) {
		this.source = source;
	}
	
	@Override
	public String getSourceFullName() {
		return sourceFullName;
	}
	
	/**
	 * Modifie le nom complet de la source pour affichage utilisateur.
	 */
	public void setSourceFullName(String sourceFullName) {
		this.sourceFullName = sourceFullName;
	}

	@Override
	public boolean canBeSaved() {
		return true;
	}

	@Override
	public void close() {
		entityManager.getTransaction().commit();
		entityManagerFactory.close();
	}
}
