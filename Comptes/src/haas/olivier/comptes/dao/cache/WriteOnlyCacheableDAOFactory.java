package haas.olivier.comptes.dao.cache;

import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.util.Month;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Un <i>wrapper</i> de <code>CacheableDAOFactory</code> autorisant uniquement
 * la sauvegarde.<br>
 * En lecture, il simule une source vide.
 * <p>
 * Cette classe peut être utilisée pour envelopper une source de données
 * nouvellement créée : il n'y a aucune donnée à lire, et pour éviter à
 * l'implémentation concrète de devoir gérer les variables <code>null</code>, on
 * détourne les appels de méthodes vers un
 * <code>EmptyCacheableDAOFactory</code>.
 *
 * @see {@link haas.olivier.comptes.dao.cache.EmptyCacheableDAOFactory}
 *
 * @author Olivier HAAS
 */
public class WriteOnlyCacheableDAOFactory implements CacheableDAOFactory {

	/**
	 * La source de données déléguée pour la sauvegarde.
	 */
	private final CacheableDAOFactory writeFactory;
	
	/**
	 * Une source de données vide pour la lecture.
	 */
	private final EmptyCacheableDAOFactory readFactory =
			new EmptyCacheableDAOFactory();
	
	/**
	 * Construit une source de données en écriture seule, à partir de la source
	 * spécifiée.
	 * 
	 * @param writeFactory	La source de données à utiliser pour la sauvegarde.
	 */
	public WriteOnlyCacheableDAOFactory(CacheableDAOFactory writeFactory) {
		this.writeFactory = writeFactory;
	}
	
	@Override
	public Iterator<Banque> getBanques() throws IOException {
		return readFactory.getBanques();
	}

	@Override
	public Iterator<Compte> getComptes() throws IOException {
		return readFactory.getComptes();
	}

	@Override
	public Iterator<Ecriture> getEcritures(CompteDAO cDAO) throws IOException {
		return readFactory.getEcritures(cDAO);
	}

	@Override
	public Iterator<Permanent> getPermanents(CachePermanentDAO cache,
			CompteDAO cDAO) throws IOException {
		return readFactory.getPermanents(cache, cDAO);
	}

	@Override
	public Iterator<Entry<Month, Entry<Compte, BigDecimal>>> getHistorique()
			throws IOException {
		return readFactory.getHistorique();
	}

	@Override
	public Iterator<Entry<Month, Entry<Compte, BigDecimal>>> getSoldesAVue()
			throws IOException {
		return readFactory.getSoldesAVue();
	}

	@Override
	public Iterator<Entry<Month, Entry<Compte, BigDecimal>>> getMoyennes()
			throws IOException {
		return readFactory.getMoyennes();
	}

	@Override
	public CacheablePropertiesDAO getProperties() throws IOException {
		return readFactory.getProperties();
	}

	@Override
	public void save(DAOFactory factory) throws IOException {
		// Sauvegarder dans la source réelle
		writeFactory.save(factory);
	}

	/**
	 * Renvoie le nom de la source utilisée pour la sauvegarde.
	 */
	@Override
	public String getName() {
		return writeFactory.getName();
	}

	/**
	 * Renvoie le nom de la source utilisée pour la sauvegarde.
	 */
	@Override
	public String getSource() {
		return writeFactory.getSource();
	}

	/**
	 * Renvoie le nom complet de la source utilisée pour la sauvegarde.
	 */
	@Override
	public String getSourceFullName() {
		return writeFactory.getSourceFullName();
	}

	@Override
	public void close() throws IOException {
		readFactory.close();
		writeFactory.close();
	}

	@Override
	public boolean canBeSaved() {
		return writeFactory.canBeSaved();
	}

}
