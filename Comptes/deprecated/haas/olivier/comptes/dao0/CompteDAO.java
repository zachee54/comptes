package haas.olivier.comptes.dao0;

import java.io.IOException;
import haas.olivier.comptes.Compte;

/**
 * Interface d'accès aux données pour les comptes.
 * Il s'agit de toutes les méthodes utilisées depuis l'extérieur du modèle.
 * 
 * @author Olivier HAAS
 */
public interface CompteDAO extends AbstractCompteDAO {

	/**
	 * Récupère un compte à partir de son identifiant.
	 * Si le compte a déjà été instancié, la méthode récupère l'instance
	 * existante, au moins jusqu'à la prochaine sauvegarde.
	 * 
	 * @param id
	 *            L'identifiant du compte souhaité.
	 * @return Le compte voulu.
	 * @throws IOException
	 *             Si la ressource est indisponible ou si le compte n'existe pas
	 */
	Compte get(int id) throws IOException;

	/**
	 * Ajoute un compte dans la couche données et renvoie le nouveau compte
	 * enregistré avec son nouvel identifiant. Si le compte a déjà un
	 * identifiant, il est stocké avec cet identifiant.
	 * 
	 * @return Le compte inséré (avec un identifiant non null)
	 * @throws IOException
	 *             Si le compte ne peut pas être relu après enregistrement, ou
	 *             s'il a un identifiant qui est déjà attribué à un autre
	 *             compte.
	 */
	Compte add(Compte c) throws IOException;

	/**
	 * Met à jour un compte dans la couche données. Le compte ayant le même
	 * identifiant est remplacé par celui-ci, dans le DAO. 
	 * Si aucun compte ne porte cet identifiant, la méthode ne fait rien.
	 * AVERTISSEMENT: Cette méthode ne met pas à jour les objets qui font
	 * référence à l'ancien compte. C'est typiquement le cas des Ecritures
	 * instanciées avant l'appel de cette méthode.
	 * Le contrat consiste à ce que l'objet qui appelle update(Compte) s'assure
	 * aussi que les objets utilisés ultérieurement par le programme pointent
	 * bien vers la nouvelle instance de Compte.
	 * Or, certains EcritureDAO utilisent une implémentation de get(int) qui
	 * renvoient plusieurs fois la même instance Ecriture. Une Ecriture obtenue
	 * après update(Compte) peut très bien avoir été instanciée avant !!
	 * Les SuiviDAO peuvent avoir besoin d'être modifiés, par exemple si
	 * on modifie le statut d'épargne du Compte, ou si un CompteBancaire est
	 * remplacé par un CompteBudget...
	 * De même, certains éléments de la vue peuvent avoir besoin d'être
	 * actualisés (combo box contenant la liste des Compte, etc).
	 * Ce problème n'intervient normalement que sur l'action de l'objet GUI qui
	 * modifie les Compte. C'est donc à lui d'en gérer les conséquences, et non
	 * au DAO.
	 * 
	 * @throws IOException
	 */
	void update(Compte c) throws IOException;

	/**
	 * Supprime un compte de la base.
	 * Cette méthode ne supprime pas les suivis du compte (historique, soldes à
	 * vue, moyennes glissantes). Pour cela, il est nécessaire d'appeler la
	 * méthode ad hoc dans les SuiviDAO.
	 * 
	 * @param id			L'identifiant du compte à supprimer.
	 * @throws IOException	Si le transfert des données échoue, ou si le compte
	 * 						est utilisé par une Ecriture.
	 */
	void remove(int id) throws IOException;
}
