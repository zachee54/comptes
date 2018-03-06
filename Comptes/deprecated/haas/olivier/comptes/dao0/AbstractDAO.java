package haas.olivier.comptes.dao0;

import java.io.IOException;

/** Interface de tous les objets d'accès aux données.
 * 
 * @author Olivier HAAS
 */
public interface AbstractDAO {
	
	/** Obtenir un DAO pour les comptes. */
	AbstractCompteDAO getCompteDAO();
	
	/** Obtenir un DAO pour les écritures. */
	AbstractEcritureDAO getEcritureDAO();
	
	/** Obtenir un DAO pour les opérations permanentes. */
	AbstractPermanentDAO getPermanentDAO();
	
	/** Obtenir un DAO pour les historiques des comptes. */
	AbstractSuiviDAO getHistoriqueDAO();
	
	/** Obtenir un DAO pour les soldes à vue des comptes. */
	AbstractSuiviDAO getSoldeAVueDAO();
	
	/** Obtenir un DAO pour les moyennes glissantes des comptes. */
	AbstractSuiviDAO getMoyenneDAO();
	
	/** Renvoie une propriété. */
	String getProperty(String prop);
	
	/** Définit une propriété. */
	void setProperty(String prop, String value);
	
	/** Charge les données. 
	 * @throws IOException */
	void load() throws IOException;
	
	/**
	 * Indique si la couche DAO a besoin d'être sauvegardée. Cette méthode n'a
	 * pas d'intérêt pour les implémentations qui enregistrent immédiatement
	 * les modifications.
	 * @return true si le DAO attend de sauvegarder, false si tout est déjà
	 *         sauvegardé.
	 */
	boolean mustBeSaved();

	/**
	 * Sauvegarde la couche de données en l'état. Cette méthode n'a d'intérêt
	 * que si mustBeSaved() == true.
	 * @throws IOException
	 */
	void save() throws IOException;
	
	/** Le nom du DAO, pour affichage utilisateur. */
	String getName();
	
	/** Le nom simple de la source, pour affichage utilisateur. */
	String getSource();
	
	/** Le nom complet de la source, pour affichage utilisateur. */
	String getSourceFullName();
	
	/** Efface toutes les données.
	 * Cette méthode est utile pour nettoyer la base avant d'injecter de
	 * nouvelles données. */
	void erase();
}
