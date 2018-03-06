package haas.olivier.comptes.dao0;

import haas.olivier.comptes.Permanent;

import java.io.IOException;
import java.util.Set;

/** Interface générique d'accès aux données pour les opérations permanentes.
 * Il s'agit de la méthode que tous les DAO d'opérations permanentes doivent
 * implémenter par principe.
 * Cette interface est "Abstract" dans le sens où elle n'est pas faite pour être
 * implémentée seule. 
 *
 * @author Olivier HAAS
 */
public interface AbstractPermanentDAO {

	/**
	 * Charge tous les Permanents.
	 * 
	 * @throws IOException
	 */
	Set<Permanent> getAll() throws IOException;	
}
