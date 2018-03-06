package haas.olivier.comptes.dao0;

import haas.olivier.comptes.Compte;

import java.io.IOException;
import java.util.Set;

/** Interface générique d'accès aux données pour les comptes.
 * Il s'agit de la méthode que tous les DAO de comptes doivent implémenter par
 * principe.
 * Cette interface est "Abstract" dans le sens où elle n'est pas faite pour être
 * implémentée seule. 
 * 
 * @author Olivier HAAS
 */
public interface AbstractCompteDAO {
	
	/**
	 * Récupère tous les comptes.
	 * Les comptes déjà instanciés sont récupérés sans créer de nouvelle
	 * instance, au moins jusqu'à la prochaine sauvegarde.
	 * 
	 * @return Un ensemble de tous les comptes ou null si la ressource est
	 *         indisponible.
	 * @throws IOException
	 */
	Set<Compte> getAll() throws IOException;
}
