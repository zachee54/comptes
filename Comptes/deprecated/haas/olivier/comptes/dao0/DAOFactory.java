package haas.olivier.comptes.dao0;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;


import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.serialize.SerializeDAOFactory;

/**
 * Fabrique d'accès aux données.
 * Permet d'obtenir un Data Access Object du type voulu.
 * 
 * @author Olivier HAAS
 */
public abstract class DAOFactory implements AbstractDAO {

	/** La fabrique de DAO.
	 * Par convention, elle ne doit jamais être null. Une implémentation par
	 * défaut est définie à l'initialisation de la classe.
	 */
	private static DAOFactory factory = null;
	static {
	}// static factory par défaut
	
	/** Obtenir la fabrique de DAO en cours. */
	public static DAOFactory getFactory() {
		if (factory == null) {				// S'il n'y a pas de fabrique
			try {
				// Créer une fabrique par défaut au format natif
				factory = new CacheDAOFactory(new SerializeDAOFactory(
						new File("Sans nom.cpt")));
			} catch (IOException e) {
				MessagesFactory.getInstance().showErrorMessage(
						"Impossible de créer un DAO au format par défaut");
				e.printStackTrace();
			}// try
		}// if factory null
		return factory;						// Retourner la fabrique
	}// getFactory
	
	/** Utiliser une implémentation spécifique de fabrique de DAO. S'il y avait
	 * une ancienne implémentation, elle est remplacée par la nouvelle.
	 * Les données ne sont pas transférées de l'ancienne à la nouvelle.
	 * 
	 * @param factory	La nouvelle fabrique de DAO.
	 */
	public static void setFactory(DAOFactory daoFactory) {
		try {
			setFactory(daoFactory, false);
		} catch (Exception e) {
			MessagesFactory.getInstance().showErrorMessage(
					"Impossible de définir comme source : " + daoFactory);
			e.printStackTrace();
		}
	}// setFactory

	/**
	 * Utiliser une implémentation spécifique de fabrique de DAO. S'il y avait
	 * une ancienne implémentation, elle est remplacée par la nouvelle.
	 * Les données peuvent être transférées de l'ancienne à la nouvelle. Dans ce
	 * cas, les données qui pouvaient être contenues dans la nouvelle
	 * implémentation sont écrasées.
	 * 
	 * @param daoFactory	Une fabrique de DAO. Toutes les données
	 * 						antérieurement contenues dans cette fabrique seront
	 * 						supprimées.
	 * 
	 * @param replace		Si true, les données dont transférées depuis
	 * 						l'ancienne fabrique.
	 * 
	 * @throws IOException	Si la lecture/écriture échoue dans la couche de
	 * 						données.
	 */
	public static void setFactory(DAOFactory daoFactory, boolean replace)
			throws IOException {
		
		// Transférer les données si nécessaire
		if (replace) {
			
			/* Charger les données de l'ancienne fabrique avant de supprimer les
			 * données de la nouvelle (des fois que ce soit la même...) */
			Set<Compte> comptes			= factory.getCompteDAO().getAll();
			TreeSet<Ecriture> ecritures	= factory.getEcritureDAO().getAll();
			Set<Permanent> permanents	= factory.getPermanentDAO().getAll();
			Map<Month,Map<Integer,BigDecimal>>
				historiques				= factory.getHistoriqueDAO().getAll(),
				soldes					= factory.getSoldeAVueDAO().getAll(),
				moyennes				= factory.getMoyenneDAO().getAll();
			
			// Effacer les données éventuellement présentes dans la nouvelle
			try {
				daoFactory.erase();
			} catch (NullPointerException e) {
				// Ca peut arriver si la source n'existe pas encore
			}
			
			// Les DAO de la nouvelle fabrique
			EcritureDAO eDAO = daoFactory.getEcritureDAO();
			CompteDAO cDAO = daoFactory.getCompteDAO();
			PermanentDAO pDAO = daoFactory.getPermanentDAO();
			
			// Transférer les comptes dans la nouvelle fabrique
			for (Compte c : comptes) {
				cDAO.add(c);
			}
			
			// Transférer les écritures dans la nouvelle fabrique
			for (Ecriture e : ecritures) {
				eDAO.add(e);
			}
			
			// Transférer les opérations permanentes dans la nouvelle fabrique
			for (Permanent p : permanents) {
				pDAO.add(p);
			}
			
			// Lier les SuiviDAO de destination aux données
			Map<SuiviDAO, Map<Month,Map<Integer,BigDecimal>>> suivisDAO =
					new HashMap<SuiviDAO, Map<Month,Map<Integer,BigDecimal>>>();
			suivisDAO.put(daoFactory.getHistoriqueDAO(), historiques);// histo
			suivisDAO.put(daoFactory.getSoldeAVueDAO(), soldes);	// soldes
			suivisDAO.put(daoFactory.getMoyenneDAO(), moyennes);	// moyennes
			
			// Transférer les suivis (nettoyage + insertion)
			for (SuiviDAO sDAO : suivisDAO.keySet()) {	// Pour chaque SuiviDAO
				
				// Effacer les données du nouveau SuiviDAO (si besoin)
				Map<Month,Map<Integer,BigDecimal>> suivis =
						sDAO.getAll();				// Tous ses suivis
				Month debut = null;				
				for (Month month : suivis.keySet()) {// Trouver le 1er mois
					if (debut == null ||
							(month != null && debut.after(month))) {
						debut = month;				// Mois plus ancien
					}
				}
				if (debut != null) {
					sDAO.removeFrom(debut);			// Effacer depuis ce mois
				}
				
				// Insérer les nouvelles valeurs
				Map<Month,Map<Integer,BigDecimal>> donnees =
						suivisDAO.get(sDAO);		// Les données à insérer
				for (Entry<Month,Map<Integer,BigDecimal>> e1 :
					donnees.entrySet()) {			// Pour chaque mois
					for (Entry<Integer,BigDecimal> e2 :
						e1.getValue().entrySet()) {	// Pour chaque compte
						sDAO.set(
								e2.getKey(),
								e1.getKey(),
								e2.getValue());
					}// for compte
				}// for month
			}// for DAO de suivi
			
			// Sauvegarder la nouvelle fabrique
			daoFactory.save();
		}// if replace
		
		// Remplacer l'ancienne par la nouvelle
		factory = daoFactory;
	}// setFactory avec replace

	/** Obtenir un DAO pour les comptes. */
	@Override
	public abstract CompteDAO getCompteDAO();

	/** Obtenir un DAO pour les écritures. */
	@Override
	public abstract EcritureDAO getEcritureDAO();
	
	/** Obtenir un DAO pour les opérations permanentes. */
	@Override
	public abstract PermanentDAO getPermanentDAO();

	/** Obtenir un DAO pour les historiques de comptes. */
	@Override
	public abstract SuiviDAO getHistoriqueDAO();

	/** Obtenir un DAO pour les soldes à vue. */
	@Override
	public abstract SuiviDAO getSoldeAVueDAO();

	/** Obtenir un DAO pour les moyennes glissantes. */
	@Override
	public abstract SuiviDAO getMoyenneDAO();

	/** Renvoie le premier mois du modèle. */
	public Month getDebut() {
		try {
			return new Month(new SimpleDateFormat("dd/MM/yy").parse("01/09/01"));
			
		} catch (ParseException e) {	// Ne devrait jamais arriver
			e.printStackTrace();
			return null;
		}// try
	}// getDebut
	
	/** Aucune implémentation (par défaut).
	 * @return	false
	 */
	@Override
	public boolean mustBeSaved() {
		return false;
	}
	
	/** Aucune implémentation (par défaut). */
	@Override
	public void save() throws IOException {
	}
}// class
