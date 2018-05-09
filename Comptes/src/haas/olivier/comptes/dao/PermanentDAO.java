package haas.olivier.comptes.dao;

import java.io.IOException;
import java.util.Collection;

import haas.olivier.comptes.Permanent;

/** L'interface d'accès aux données des opérations permanentes.
 * 
 * @author Olivier HAAS
 */
public interface PermanentDAO {

	/** Renvoie toutes les écritures permanentes. */
	public Collection<Permanent> getAll() throws IOException;
	
	/** Renvoie une opération permanente.
	 * 
	 * @param id	L'identifiant de l'opération permanente à renvoyer.
	 * 
	 * @return		L'opération ayant l'identifiant <code>id</code>, ou
	 * 				<code>null</code> s'il n'y a pas de telle opération.
	 */
	public Permanent get(int id);
	
	/** Ajoute une nouvelle opération permanente.
	 * 
	 * @param p	L'opération permanente à ajouter.
	 * 
	 * @return	Une nouvelle instance de l'opération permanente, comprenant en
	 * 			plus un identifiant.
	 */
	public Permanent add(Permanent p);
	
	/** Modifie une opération permanente existante.
	 * 
	 * @param p	La nouvelle opération permanente à mettre à jour.
	 */
	public void update(Permanent p);
	
	/** Supprime une opération permanente.
	 * 
	 * @param id	L'identifiant de l'opération permanente à supprimer.
	 */
	public void remove(int id);
}
