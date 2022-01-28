package haas.olivier.comptes.dao.cache;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;

import com.sun.istack.logging.Logger;

import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;

/**
 * Un objet d'accès aux données permettant un multi-save. Les données sont
 * sauvegardées simultanément dans deux formats différents.
 * 
 * Cette couche utilise un DAO principal, d'où sont lues les données. Lors de la
 * sauvegarde, les données sont sauvegardées à la fois dans ce DAO de manière
 * normale, et dans le DAO secondaire où elles écrasent les données
 * préexistantes.
 * 
 * Les deux DAO doivent implémenter <code>CacheableDAOFactory</code>, car cette
 * couche est fait pour être utilisée avec un cache.
 *
 * @author Olivier Haas
 */
public class MultiCacheableDAOFactory implements CacheableDAOFactory {

	/** Accès principal aux données. */
	private final CacheableDAOFactory mainDAO;
	
	/** Accès aux ressources servant de sauvegarde. */
	private final CacheableDAOFactory backupDAO;
	
	/**
	 * Construit un objet d'accès aux données cachable et multi-save.
	 * 
	 * @param mainDAO	L'accès aux données en lecture et écriture.
	 * @param backupDAO	L'accès en écriture aux données de sauvegarde. Les
	 * 					données préexistantes ne seront pas lues, et seront
	 * 					écrasées lors de la sauvegarde.
	 */
	public MultiCacheableDAOFactory(
			CacheableDAOFactory mainDAO, CacheableDAOFactory backupDAO) {
		this.mainDAO = mainDAO;
		this.backupDAO = backupDAO;
	}
	
	@Override
	public void close() throws IOException {
		mainDAO.close();
		backupDAO.close();
	}

	@Override
	public Iterator<Banque> getBanques() throws IOException {
		return mainDAO.getBanques();
	}

	@Override
	public Iterator<Compte> getComptes() throws IOException {
		return mainDAO.getComptes();
	}

	@Override
	public Iterator<Ecriture> getEcritures() throws IOException {
		return mainDAO.getEcritures();
	}

	@Override
	public Iterator<Permanent> getPermanents(CachePermanentDAO cache)
			throws IOException {
		return mainDAO.getPermanents(cache);
	}

	@Override
	public Iterator<Solde> getHistorique() throws IOException {
		return mainDAO.getHistorique();
	}

	@Override
	public Iterator<Solde> getSoldesAVue() throws IOException {
		return mainDAO.getSoldesAVue();
	}

	@Override
	public Iterator<Solde> getMoyennes() throws IOException {
		return mainDAO.getMoyennes();
	}

	@Override
	public CacheablePropertiesDAO getProperties() throws IOException {
		return mainDAO.getProperties();
	}

	@Override
	public void save(CacheDAOFactory cache) throws IOException {
		mainDAO.save(cache);
		
		try {
			backupDAO.save(cache);
		} catch (IOException e) {
			Logger.getLogger(getClass()).log(
					Level.WARNING,
					"Les données ont été sauvegardées, mais la sauvegarde de la copie de secours a échoué.",
					e);
		}
	}

	@Override
	public String getName() {
		return String.format("%s + %s", mainDAO.getName(), backupDAO.getName());
	}

	@Override
	public String getSource() {
		return String.format("%s,%s",
				mainDAO.getSource(), backupDAO.getSource());
	}

	@Override
	public String getSourceFullName() {
		return String.format("[multi]%s[multi]%s",
				mainDAO.getSourceFullName(),
				backupDAO.getSourceFullName());
	}
	
	@Override
	public String getSourceRelativeName() {
		return String.format("[multi]%s[multi]%s",
				mainDAO.getSourceRelativeName(),
				backupDAO.getSourceRelativeName());
	}

	@Override
	public boolean canSaveSuivis() {
		return mainDAO.canSaveSuivis();
	}

	@Override
	public boolean canBeSaved() {
		return mainDAO.canBeSaved();
	}

}
