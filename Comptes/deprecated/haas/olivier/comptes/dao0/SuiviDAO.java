package haas.olivier.comptes.dao0;

import haas.olivier.util.Month;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Interface d'accès aux données pour les suivis de comptes tels que historique,
 * soldes à vues, moyennes glissantes.
 * Il s'agit de toutes les méthodes utilisées depuis l'extérieur du modèle.
 * 
 * @author Olivier HAAS
 */
public interface SuiviDAO extends AbstractSuiviDAO {

	/**
	 * Récupère un montant dans le suivi du compte. Aucune exception n'est levée
	 * en cas d'erreur de lecture/écriture afin de ne pas alourdir la gestion
	 * des exceptions par le contrôleur.
	 * 
	 * @return Le montant du suivi du compte. S'il n'y a pas de suivi à cette
	 *         date, renvoie null
	 */
	BigDecimal get(int id, Month month);

	/**
	 * Modifie un montant dans le suivi d'un compte. Si un montant est déjà
	 * enregistré, il est remplacé par le nouveau
	 * 
	 * @param id
	 *            Le numéro du compte concerné
	 * @param month
	 *            Le mois au titre duquel modifier le suivi du compte.
	 * @param valeur
	 *            Le montant à substituer.
	 * @throws IOException
	 */
	void set(int id, Month month, BigDecimal valeur) throws IOException;

	/**
	 * Supprime le suivi de tous les comptes à partir d'un mois.
	 * 
	 * @param month
	 *            Mois concerné
	 * @throws IOException
	 */
	void removeFrom(Month month) throws IOException;
}
