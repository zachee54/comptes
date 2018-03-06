package haas.olivier.comptes.dao0;

import java.io.IOException;
import java.util.TreeSet;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;

/**
 * Interface générique d'accès aux données pour les écritures.
 * Il s'agit des méthodes que tous les DAO pour les écritures doivent
 * implémenter par principe.
 * Cette interface est "Abstract" dans le sens où elle n'est pas faite pour être
 * implémentée seule. 
 *
 * @author Olivier HAAS
 */
public interface AbstractEcritureDAO {

	/**
	 * Charge toutes les écritures de la plus récente à la plus ancienne
	 * 
	 * @throws IOException
	 */
	TreeSet<Ecriture> getAll() throws IOException;
	
	/**
	 * Rafraîchit les références vers le compte spécifié.
	 * Un objet Compte peut être remplacé dans le DAO par une nouvelle instance
	 * Compte. Dans ce cas, toutes les instances Ecriture qui contenaient une
	 * référence vers l'ancienne instance doivent pointer vers la nouvelle.
	 * @param compte	Le compte vers lequel doivent pointer les écritures, en
	 * 					remplacement de toute autre instance Compte qui portait
	 * 					le même identifiant.
	 * @throws IOException 
	 */
	void refreshCompte(Compte compte) throws IOException;
}
