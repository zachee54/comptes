package haas.olivier.comptes.dao0.cache;

import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.PermanentDAO;
import haas.olivier.comptes.dao.SuiviDAO;

import java.io.IOException;
import java.util.Properties;

/**
 * Une fabrique de DAO implémentant un cache.
 * Les données sont entièrement chargées en mémoire à partir d'une sous-couche
 * avant d'être exploitées.
 * 
 * @author Olivier HAAS
 */
public class CacheDAOFactory extends DAOFactory {

	/** La sous-couche DAO. */
	private CacheableDAOFactory dao;
	
	/** Tous les DAO à fabriquer. */
	private CacheCompteDAO cDAO;
	private CacheEcritureDAO eDAO;
	private CachePermanentDAO pDAO;
	private CacheSuiviDAO hDAO, sDAO, mDAO;
	
	/** Les propriétés des diagrammes. */
	private Properties properties;
	
	/** Drapeau indiquant si les données générales du modèle ont été modifiées
	 * depuis la dernière sauvegarde.<br>
	 * En pratique, il s'agit de changements sur les propriétés.
	 */
	private boolean hasChanged = false;
	
	/** Construit un DAO implémentant un cache.
	 * Les données sont chargées dès la création de l'objet.
	 * @param dao
	 * 			Un CacheableDAO qui contiendra les données à mettre en cache.
	 * @throws IOException 
	 */
	public CacheDAOFactory(CacheableDAOFactory dao) {
		this.dao = dao;
		
		// Fabriquer les DAO.
		cDAO = new CacheCompteDAO(dao.getCompteDAO());			// Comptes
		eDAO = new CacheEcritureDAO(dao.getEcritureDAO());		// Ecritures
		pDAO = new CachePermanentDAO(dao.getPermanentDAO());	// Permanents
		hDAO = new CacheSuiviDAO(dao.getHistoriqueDAO());		// Historiques
		sDAO = new CacheSuiviDAO(dao.getSoldeAVueDAO());		// Soldes à vue
		mDAO = new CacheSuiviDAO(dao.getMoyenneDAO());			// Moyennes
	}// constructeur
		
	@Override
	public void load() throws IOException {
		dao.load();					// Charger les données dans la sous-couche
	}// load
	
	/** @return Le nom du DAO sous-jacent, précédé de "Cache". */
	@Override
	public String getName() {
		return "Cache " + dao.getName();
	}

	@Override
	public String getSource() {
		return dao.getSource();
	}

	@Override
	public String getSourceFullName() {
		return dao.getSourceFullName();
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
	public PermanentDAO getPermanentDAO() {
		return pDAO;
	}

	@Override
	public SuiviDAO getHistoriqueDAO() {
		return hDAO;
	}

	@Override
	public SuiviDAO getSoldeAVueDAO() {
		return sDAO;
	}

	@Override
	public SuiviDAO getMoyenneDAO() {
		return mDAO;
	}
	
	@Override
	public String getProperty(String prop) {
		if (properties == null)
			properties = dao.getProperties();
		String value = properties.getProperty(prop);
		return value == null ? "" : value;
	}// getProperty
	
	@Override
	public void setProperty(String prop, String value) {
		if (properties == null)
			properties = dao.getProperties();
		properties.setProperty(prop, value);
		hasChanged = true;
	}// setProperty

	@Override
	public boolean mustBeSaved() {
		return hasChanged
				|| cDAO.mustBeSaved
				|| eDAO.mustBeSaved
				|| pDAO.mustBeSaved
				|| hDAO.mustBeSaved
				|| sDAO.mustBeSaved
				|| mDAO.mustBeSaved;
	}// mustBeSaved
	
	/** Sauvegarde toutes les données.
	 * L'implémentation est laissée aux DAO thématiques. */
	@Override
	public void save() throws IOException {
		
		// Remplacer les données
		cDAO.save();
		eDAO.save();
		pDAO.save();
		hDAO.save();
		sDAO.save();
		mDAO.save();
		
		// Déclencher la sauvegarde physique
		dao.flush();
		
		// Si on arrive ici, indiquer les données comme sauvegardées
		cDAO.mustBeSaved = false;
		eDAO.mustBeSaved = false;
		pDAO.mustBeSaved = false;
		hDAO.mustBeSaved = false;
		sDAO.mustBeSaved = false;
		mDAO.mustBeSaved = false;
		hasChanged = false;
	}// save

	@Override
	public void erase() {
		dao.erase();
		eDAO.erase();
		cDAO.erase();
		pDAO.erase();
		hDAO.erase();
		sDAO.erase();
		mDAO.erase();
	}// erase
}
