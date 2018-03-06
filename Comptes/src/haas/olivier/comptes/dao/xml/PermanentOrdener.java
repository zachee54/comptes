package haas.olivier.comptes.dao.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import haas.olivier.comptes.dao.xml.jaxb.perm.Permanent;
import haas.olivier.comptes.dao.xml.jaxb.perm.Permanent.Dependance;
import haas.olivier.util.ReadOnlyIterator;

/** Un itérateur qui réordonne les opérations permanentes JAXB de façon à
 * traiter les opérations dépendantes après leurs dépendances.
 *
 * @author Olivier HAAS
 */
class PermanentOrdener extends ReadOnlyIterator<Permanent> {

	/** Les identifiants des opérations déjà parcourues. */
	private final Set<Integer> ids = new HashSet<Integer>();
	
	/** Les opérations dépendantes en attente de leurs dépendances. */
	private final List<Permanent> dependants = new ArrayList<Permanent>();
	
	/** L'itérateur d'origine. */
	private final Iterator<Permanent> it;
	
	/** Construit un itérateur qui réordonne les opérations permanentes JAXB
	 * pour respecter l'ordre des dépendances.
	 * 
	 * @param it	Un itérateur d'opérations permanentes JAXB.
	 */
	PermanentOrdener(Iterator<Permanent> it) {
		this.it = it;
	}// constructeur
	
	@Override
	public boolean hasNext() {
		return it.hasNext() || !dependants.isEmpty();
	}// hasNext

	@Override
	public Permanent next() {
		
		// Parcourir l'itérateur original tant qu'il n'est pas terminé
		while (it.hasNext()) {
			Permanent next = it.next();
			Dependance dependance = next.getDependance();

			if (dependance == null || ids.contains(dependance.getId())) {
				// Pas une dépendante, ou la dépendance est déjà passée
				
				// Ajouter à la collection des opérations permanentes traitées
				ids.add(next.getId());
				
				// Renvoyer cette opération
				return next;

			} else {
				// Opération dépendante et dépendance indisponible
				dependants.add(next);
			}// if
		}// while
		
		// Trouver une opération dépendante pouvant être traitée maintenant
		for (int i=0; i<dependants.size(); i++) {
			Permanent p = dependants.get(i);
			
			// Si la dépendance existe
			if (ids.contains(p.getDependance().getId())) {
				dependants.remove(i);
				return p;
			}// if
		}// for
		
		throw new RuntimeException(
				"Problème de dépendance des opérations permanentes : dépendance absente ou boucle entre les dépendances.");
	}// next
}
