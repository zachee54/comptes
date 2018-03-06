package haas.olivier.comptes.dao0;

import java.io.IOException;
import haas.olivier.comptes.Permanent;

/**
 * Interface d'accès aux données pour les instances Permanent.
 * 
 * @author Olivier HAAS
 */
public interface PermanentDAO extends AbstractPermanentDAO {

	/**
	 * Ajoute un Permanent et en renvoie un nouveau avec le nouvel index. Si le
	 * Permanent a déjà un index, il est enregistré avec le même index.
	 * 
	 * @param Le
	 *            Permanent à ajouter
	 * @return Le nouveau Permanent, avec son index défini.
	 * @throws IOException
	 */
	Permanent add(Permanent p) throws IOException;

	/**
	 * Met à jour un Permanent. Le Permanent à mettre à jour est déterminé par
	 * son index.
	 * 
	 * @throws IOException
	 */
	void update(Permanent p) throws IOException;

	/**
	 * Supprime le Permanent correspondant à l'index spécifié.
	 * 
	 * @throws IOException
	 */
	void remove(int id) throws IOException;

	/**
	 * Charge un Permanent. Si le Permanent a déjà été instancié, la méthode
	 * renvoie l'instance existante.
	 * 
	 * @param id
	 *            L'identifiant du Permanent voulu.
	 * @return Le Permanent voulu.
	 * @throws IOException
	 */
	Permanent get(int id) throws IOException;
}
