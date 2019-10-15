package haas.olivier.comptes.dao.hibernate;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;

import haas.olivier.comptes.dao.BanqueDAO;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.PermanentDAO;
import haas.olivier.comptes.dao.PropertiesDAO;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.util.Month;

/**
 * HibernateDAO.java
 * DOCUMENTEZ_MOI
 * @author Olivier HAAS
 * Date: 15 oct. 2019
 */
public class HibernateDAO extends DAOFactory implements Closeable {

	/**
	 * Contexte JPA généré dynamiquement.
	 */
	@PersistenceUnit
	private final EntityManagerFactory entityManagerFactory;
	
	/**
	 * Propriétés dynamiques du contexte JPA.
	 */
	
	public HibernateDAO(String url, String driver) {
		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.url", url);
		properties.put("javax.persistence.jdbc.driver", driver);
		entityManagerFactory = Persistence.createEntityManagerFactory(
				"haas.olivier.comptes", properties);
	}
	
	/**
	 * Crée un gestionnaire d'entités JPA avec les propriétés propres à
	 * l'instance actuelle.
	 *
	 * @return	Un nouveau gestionnaire d'entités.
	 */
	private EntityManager createEntityManager() {
		return entityManagerFactory.createEntityManager();
	}
	
	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#getBanqueDAO()
	 */
	@Override
	public BanqueDAO getBanqueDAO() {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	@Override
	public CompteDAO getCompteDAO() {
		return new HibernateCompteDAO(createEntityManager());
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#getEcritureDAO()
	 */
	@Override
	public EcritureDAO getEcritureDAO() {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#getPermanentDAO()
	 */
	@Override
	public PermanentDAO getPermanentDAO() {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#getHistoriqueDAO()
	 */
	@Override
	public SuiviDAO getHistoriqueDAO() {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#getSoldeAVueDAO()
	 */
	@Override
	public SuiviDAO getSoldeAVueDAO() {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#getMoyenneDAO()
	 */
	@Override
	public SuiviDAO getMoyenneDAO() {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#getPropertiesDAO()
	 */
	@Override
	public PropertiesDAO getPropertiesDAO() {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#canBeSaved()
	 */
	@Override
	public boolean canBeSaved() {
		return false; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#mustBeSaved()
	 */
	@Override
	public boolean mustBeSaved() {
		return false; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#save()
	 */
	@Override
	public void save() throws IOException {
		// DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#erase()
	 */
	@Override
	protected void erase() throws IOException {
		// DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#getDebut()
	 */
	@Override
	public Month getDebut() {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#getName()
	 */
	@Override
	public String getName() {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#getSource()
	 */
	@Override
	public String getSource() {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	/** 
	 * (methode de remplacement)
	 * {@inheritDoc}
	 * @see haas.olivier.comptes.dao.DAOFactory#getSourceFullName()
	 */
	@Override
	public String getSourceFullName() {
		return null; // DOCUMENTEZ_MOI Raccord de méthode auto-généré
	}

	@Override
	public void close() {
		entityManagerFactory.close();
	}
}
