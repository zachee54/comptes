/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.cache;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import haas.olivier.comptes.Banque;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;

/**
 * L'interface des sources de données conçues pour fonctionner avec un cache.
 * 
 * @author Olivier HAAS
 */
public interface CacheableDAOFactory extends Closeable {

	/**
	 * Renvoie toutes les banques enregistrées dans la source. 
	 * 
	 * @throws IOException
	 */
	Iterator<Banque> getBanques() throws IOException;
	
	/**
	 * Renvoie tous les comptes. 
	 * 
	 * @throws IOException
	 */
	Iterator<Compte> getComptes() throws IOException;
	
	/**
	 * Renvoie toutes les écritures.
	 * 
	 * @throws IOException
	 */
	Iterator<Ecriture> getEcritures() throws IOException;
	
	/**
	 * Renvoie toutes les opérations permanentes.
	* 
	* @param cache	Le cache d'opérations permanentes qui appelle ce
	* 				constructeur. Cela permet, au cours de l'énumération des
	* 				<code>Permanent</code>s, de récupérer l'un d'eux déjà
	* 				instancié (utile pour les opérations dépendant d'une autre).
	* 
	 * @throws IOException
	*/
	Iterator<Permanent> getPermanents(CachePermanentDAO cache)
			throws IOException;
	
	/**
	 * Renvoie les historiques des comptes. 
	 * 
	 * @throws IOException
	 */
	Iterator<Solde> getHistorique() throws IOException;
	
	/**
	 * Renvoie les soldes à vue.
	 * 
	 * @throws IOException
	 */
	Iterator<Solde> getSoldesAVue() throws IOException;
	
	/**
	 * Renvoie les moyennes glissantes.
	 * 
	 * @throws IOException
	 */
	Iterator<Solde> getMoyennes() throws IOException;
	
	/**
	 * Renvoie un objet d'accès aux propriétés. 
	 * 
	 * @throws IOException
	 */
	CacheablePropertiesDAO getProperties() throws IOException;
	
	/**
	 * Sauvegarde les données.
	 * 
	 * @param cache	Le cache à sauvegarder.
	 * 
	 * @throws IOException
	 */
	void save(CacheDAOFactory cache) throws IOException;
	
	/**
	 * Renvoie le type de modèle pour affichage utilisateur.
	 */
	String getName();
	
	/**
	 * Renvoie le nom de la source pour affichage utilisateur.
	 */
	String getSource();
	
	/**
	 * Renvoie le nom complet de la source.
	 */
	String getSourceFullName();
	
	/**
	 * Indique si la source de données peut être sauvegardée en l'état.
	 */
	boolean canBeSaved();
}
