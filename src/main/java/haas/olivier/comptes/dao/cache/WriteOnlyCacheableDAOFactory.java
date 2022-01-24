/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.cache;

import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import java.io.IOException;
import java.util.Iterator;

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
	public Iterator<Ecriture> getEcritures() throws IOException {
		return readFactory.getEcritures();
	}

	@Override
	public Iterator<Permanent> getPermanents(CachePermanentDAO cache)
			throws IOException {
		return readFactory.getPermanents(cache);
	}

	@Override
	public Iterator<Solde> getHistorique() throws IOException {
		return readFactory.getHistorique();
	}

	@Override
	public Iterator<Solde> getSoldesAVue() throws IOException {
		return readFactory.getSoldesAVue();
	}

	@Override
	public Iterator<Solde> getMoyennes() throws IOException {
		return readFactory.getMoyennes();
	}

	@Override
	public CacheablePropertiesDAO getProperties() throws IOException {
		return readFactory.getProperties();
	}

	@Override
	public void save(CacheDAOFactory cache) throws IOException {
		// Sauvegarder dans la source réelle
		writeFactory.save(cache);
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
	public boolean canSaveSuivis() {
		return writeFactory.canSaveSuivis();
	}

	@Override
	public boolean canBeSaved() {
		return writeFactory.canBeSaved();
	}

}
