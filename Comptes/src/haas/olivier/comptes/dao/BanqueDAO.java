package haas.olivier.comptes.dao;

import haas.olivier.comptes.Banque;

/** L'interface d'accès aux données des banques.
 * 
 * @author Olivier HAAS
 */
public interface BanqueDAO {

	/** Renvoie tous les instances <code>Banque</code>. */
	Iterable<Banque> getAll();
	
	/** Renvoie une instance de banque.
	 * 
	 * @param id	L'identifiant de la banque souhaitée.
	 * @return		La banque souhaitée, ou <code>null</code> si aucune banque
	 * 				connue ne porte cet identifiant.
	 */
	Banque get(int id);
	
	/** Ajoute une nouvelle banque.
	 * <p>
	 * Si <code>b</code> a un identifiant <code>null</code>, alors elle est
	 * réinstanciée avec un identifiant immédiatement supérieur au plus grand
	 * identifiant actuel.
	 * 
	 * @param b	La banque à ajouter.
	 * 
	 * @return	<code>b</code>, si celle-ci avait un identifiant, ou sinon une
	 * 			nouvelle contenant les mêmes valeurs avec en plus un
	 * 			identifiant.
	 */
	Banque add(Banque b);
}
