/*
 * Copyright 2013-2021 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.cache;

import java.io.IOException;

import haas.olivier.comptes.dao.BanqueDAO;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.PropertiesDAO;
import haas.olivier.util.Month;

/**
 * Une couche du modèle implémentant un cache.
 * <p>
 * Les données sont intégralement chargées en mémoire, la source n'étant lue
 * qu'une fois au départ.
 *
 * @author Olivier HAAS
 */
public class CacheDAOFactory extends DAOFactory {

	/**
	 * La source de données.
	 */
	private final CacheableDAOFactory dao;
	
	/**
	 * L'objet d'accès aux données des banques.
	 */
	private final CacheBanqueDAO bDAO;
	
	/**
	 * L'objet d'accès aux données des comptes.
	 */
	private final CacheCompteDAO cDAO;
	
	/**
	 * L'objet d'accès aux données des écritures.
	 */
	private final CacheEcritureDAO eDAO;
	
	/**
	 * L'objet d'accès aux données des opérations permanentes.
	 */
	private final CachePermanentDAO pDAO;
	
	/**
	 * L'objet d'accès aux historiques des comptes.
	 */
	private final CacheSuiviDAO hDAO;
	
	/**
	 * L'objet d'accès aux soldes à vues.
	 */
	private final CacheSuiviDAO sDAO;
	
	/**
	 * L'objet d'accès aux moyennes glissantes.
	 */
	private final CacheSuiviDAO mDAO;
	
	/**
	 * L'objet d'accès aux propriétés.
	 */
	private final CachePropertiesDAO propsDAO;
	
	/**
	 * Construit un cache par-dessus une source de données.
	 * 
	 * @param dao	La source de données.
	 * 
	 * @throws IOException
	 */
	public CacheDAOFactory(CacheableDAOFactory dao) throws IOException {
		
		// Utiliser un leurre s'il n'y a pas de factory
		if (dao == null)
			dao = new EmptyCacheableDAOFactory();
		this.dao = dao;
		
		// Créer les DAO par types
		try{
			bDAO = new CacheBanqueDAO(dao.getBanques());
			cDAO = new CacheCompteDAO(dao.getComptes());
			eDAO = new CacheEcritureDAO(dao.getEcritures());
			/* Exception: Pour l'instanciation des opérations permanentes, le
			 * CacheableDAO a besoin de faire appel à la couche supérieure (i.e.
			 * le cache des permanents) puisque les Permanents sont
			 * inter-dépendants.
			 * Du coup on instancie d'abord le cache avec une référence vers la
			 * fabrique Cacheable, et le cache appellera lui-même l'énumération
			 * CacheableDAOFactory.getPermanents(...) en passant sa propre
			 * référence.
			 */
			pDAO = new CachePermanentDAO(dao, cDAO);
			hDAO = new CacheSuiviDAO(dao.getHistorique());
			sDAO = new CacheSuiviDAO(dao.getSoldesAVue());
			mDAO = new CacheSuiviDAO(dao.getMoyennes());
			propsDAO = new CachePropertiesDAO(dao.getProperties());
			
		} finally {
			dao.close();
		}
	}
	
	/**
	 * Renvoie la couche inférieure d'accès aux données.
	 * 
	 * @return	La couche inférieure d'accès aux données.
	 */
	public CacheableDAOFactory getDelegate() {
		return dao;
	}

	@Override
	public BanqueDAO getBanqueDAO() {
		return bDAO;
	}

	@Override
	public CompteDAO getCompteDAO() {
		return cDAO;
	}

	@Override
	public EcritureDAO getEcritureDAO() {
		return eDAO;
	}

	@Override
	public CachePermanentDAO getPermanentDAO() {
		return pDAO;
	}

	@Override
	public CacheSuiviDAO getHistoriqueDAO() {
		return hDAO;
	}

	@Override
	public CacheSuiviDAO getSoldeAVueDAO() {
		return sDAO;
	}

	@Override
	public CacheSuiviDAO getMoyenneDAO() {
		return mDAO;
	}

	@Override
	public PropertiesDAO getPropertiesDAO() {
		return propsDAO;
	}

	@Override
	public boolean canBeSaved() {
		return dao.canBeSaved();
	}

	@Override
	public boolean mustBeSaved() {
		return bDAO.mustBeSaved()
				|| cDAO.mustBeSaved()
				|| eDAO.mustBeSaved()
				|| pDAO.mustBeSaved()
				|| propsDAO.mustBeSaved();
	}

	@Override
	public void save() throws IOException {
		dao.save(this);

		// Tout marquer comme sauvegardé
		bDAO.setSaved();
		cDAO.setSaved();
		eDAO.setSaved();
		pDAO.setSaved();
		propsDAO.setSaved();
	}

	@Override
	protected void erase() throws IOException {
		eDAO.erase();
		cDAO.erase();
		bDAO.erase();
		pDAO.erase();
		hDAO.erase();
		sDAO.erase();
		mDAO.erase();
		propsDAO.erase();
	}

	/**
	 * @returns	Le mois le plus ancien, ou le mois actuel s'il n'y a aucune
	 * 			écriture.
	 * 
	 * @see {@link haas.olivier.comptes.dao.DAOFactory#getDefaultDebut()}
	 */
	@Override
	public Month getDebut() {
		Month debut = eDAO.getDebut();
		return debut == null ? Month.getInstance() : debut;
	}

	@Override
	public String getName() {
		return dao.getName();
	}

	@Override
	public String getSource() {
		return dao.getSource();
	}

	@Override
	public String getSourceFullName() {
		return dao.getSourceFullName();
	}
}
