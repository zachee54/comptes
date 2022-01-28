/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.ctrl.EcritureController;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.PermanentDAO;
import haas.olivier.comptes.dao.SuiviDAO;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.cache.EmptyCacheableDAOFactory;
import haas.olivier.comptes.dao.cache.Solde;
import haas.olivier.util.Month;

/**
 * La fabrique d'accès aux données.
 * 
 * @author Olivier HAAS
 */
public abstract class DAOFactory {

	/**
	 * L'instance en cours d'utilisation.
	 */
	private static DAOFactory factory;
	
	/**
	 * Renvoie l'instance en cours d'utilisation.
	 */
	public static DAOFactory getFactory() {
		
		// Vérifier qu'il existe une implémentation
		if (factory == null) {
			try {
				factory = new CacheDAOFactory(new EmptyCacheableDAOFactory());
				
			} catch (Exception e) {
				Logger.getLogger(DAOFactory.class.getName()).log(
					Level.SEVERE,
					"Impossible de créer un modèle de données vide.",
					e);
			}
		}
		
		return factory;
	}
	
	/**
	 * Met en place une nouvelle source de données.
	 * <p>
	 * Si une autre source de données était précédemment définie, elle est
	 * remplacée par la nouvelle. Les données de l'ancienne source sont
	 * ignorées.
	 * 
	 * @param dao	La nouvelle source de données.
	 * 
	 * @throws IOException
	 */
	public static void setFactory(DAOFactory daoFactory) throws IOException {
		setFactory(daoFactory, false);
	}
	
	/**
	 * Met en place une nouvelle source de données.
	 * <p>
	 * Si une autre source de données était précédemment définie, elle est
	 * remplacée par la nouvelle.
	 * <p>
	 * Les données peuvent être transférées de l'ancienne à la nouvelle. Dans ce
	 * cas, les données qui pouvaient éventuellement être contenues dans la
	 * nouvelle instance sont écrasées.
	 * 
	 * @param newFactory	La nouvelle source de données. Si
	 * 						<code>replace == true</code>, toutes les données
	 * 						antérieurement contenues dans cette source seront
	 * 						supprimées.
	 * 
	 * @param replace		Si <code>true</code>, les données dont transférées
	 * 						depuis l'ancienne instance.
	 * 
	 * @throws IOException
	 */
	public static void setFactory(DAOFactory newFactory, boolean replace)
			throws IOException {
		
		// Rien à faire si la nouvelle instance est déjà celle qui est en place
		if (factory == newFactory)
			return;
		
		// Transférer les données si nécessaire
		if (factory != null && replace) {
			
			// Effacer les données éventuellement présentes dans la nouvelle
			newFactory.erase();
			
			// Les DAO de la nouvelle fabrique
			EcritureDAO eDAO	= newFactory.getEcritureDAO();
			CompteDAO cDAO		= newFactory.getCompteDAO();
			PermanentDAO pDAO	= newFactory.getPermanentDAO();
			
			// Transférer les comptes dans la nouvelle fabrique
			for (Compte c : factory.getCompteDAO().getAll())
				cDAO.add(c);
			
			// Transférer les écritures dans la nouvelle fabrique
			for (Ecriture e : factory.getEcritureDAO().getAll())
				eDAO.add(e);
			
			// Transférer les opérations permanentes dans la nouvelle fabrique
			for (Permanent p : factory.getPermanentDAO().getAll())
				pDAO.add(p);
			
			// Transférer les suivis
			transferSuivi(factory.getHistoriqueDAO(),
					newFactory.getHistoriqueDAO());
			transferSuivi(factory.getSoldeAVueDAO(),
					newFactory.getSoldeAVueDAO());
			transferSuivi(factory.getMoyenneDAO(),
					newFactory.getMoyenneDAO());
			
			// Sauvegarder la nouvelle fabrique
			newFactory.save();
		}
		
		// Remplacer l'ancienne par la nouvelle
		factory = newFactory;
		
		if (!factory.canSaveSuivis()) {
			EcritureController.updateSuivis(factory.getDebut());
		}
	}
	
	/**
	 * Transfère les données d'un suivi vers un autre.<br>
	 * En principe : les données de suivi d'une ancienne fabrique vers les
	 * données de suivi de même type dans une nouvelle fabrique.
	 * 
	 * @param source	Le suivi dont les données doivent être copiées.
	 * @param dest		Le suivi dans lequel copier les données.
	 */
	private static void transferSuivi(SuiviDAO source, SuiviDAO dest) {
		Iterator<Solde> soldesIterator = source.getAll();
		while (soldesIterator.hasNext())
			dest.set(soldesIterator.next());
	}
	
	/**
	 * Renvoie l'objet d'accès aux données des banques.
	 */
	public abstract BanqueDAO getBanqueDAO();
	
	/**
	 * Renvoie l'objet d'accès aux données des comptes.
	 */
	public abstract CompteDAO getCompteDAO();
	
	/**
	 * Renvoie l'objet d'accès aux données des écritures.
	 */
	public abstract EcritureDAO getEcritureDAO();
	
	/**
	 * Renvoie l'objet d'accès aux données des opérations permanentes.
	 */
	public abstract PermanentDAO getPermanentDAO();
	
	/**
	 * Renvoie l'objet d'accès aux soldes des comptes.
	 */
	public abstract SuiviDAO getHistoriqueDAO();
	
	/**
	 * Renvoie l'objet d'accès aux soldes à vue des comptes bancaires.
	 */
	public abstract SuiviDAO getSoldeAVueDAO();
	
	/**
	 * Renvoie l'objet d'accès aux moyennes glissantes des comptes budgétaires.
	 */
	public abstract SuiviDAO getMoyenneDAO();
	
	/**
	 * Renvoie l'objet d'accès aux propriétés sauvegardées dans la source de
	 * données.
	 */
	public abstract PropertiesDAO getPropertiesDAO();
	
	/**
	 * Indique si la classe est capable de sauvegarder les suivis.
	 * 
	 * Si <code>false</code>, les soldes doivent être recalculés après le
	 * chargement.
	 * 
	 * @return	<code>true</code> si la classe est capable de sauvegarder les
	 * 			suivis.
	 */
	public abstract boolean canSaveSuivis();
	
	/**
	 * Indique si la source peut être sauvegardée en l'état.
	 * <p>
	 * C'est généralement le cas, sauf si la source est en lecture seule ou si
	 * sa destination n'est pas spécifiée (cas d'un modèle créé de toutes
	 * pièces, par exemple).
	 */
	public abstract boolean canBeSaved();
	
	/**
	 * Indique si les données ont besoin d'être sauvegardées.
	 */
	public abstract boolean mustBeSaved();
	
	/**
	 * Sauvegarde les données.
	 */
	public abstract void save() throws IOException;
	
	/**
	 * Efface toutes les données.
	 */
	protected abstract void erase() throws IOException;
	
	/**
	 * Renvoie le mois le plus ancien dans le modèle.
	 */
	public abstract Month getDebut();
	
	/**
	 * Renvoie le nom du type de modèle (base de données, csv,
	 * sérialisation...).
	 */
	public abstract String getName();
	
	/**
	 * Renvoie le nom simple de la source (par exemple : nom du fichier).
	 */
	public abstract String getSource();
	
	/**
	 * Renvoie le nom complet de la source (par exemple : le chemin complet du
	 * fichier).
	 */
	public abstract String getSourceFullName();
	
	/**
	 * Renvoie le chemin relatif vers la source (par exemple : le chemin relatif
	 * du fichier par rapport au répertoire courant).
	 * 
	 * Cette implémentation renvoie {@link #getSourceFullName()}.
	 */
	public String getSourceRelativeName() {
		return getSourceFullName();
	}
}
