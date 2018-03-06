package haas.olivier.comptes.gui;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;

/** Un filtre pour déterminer quel compte doit être affiché dans ce
 * panneau.
 */
public class FilterCompte {

	/** Les types acceptés. */
	private final TypeCompte[] types;

	/** Indique si le filtre accepte des comptes bancaires. */
	private boolean someBancaires = false;

	/** Construit un filtre qui sélectionne uniquement les comptes dont le
	 * type est spécifié.
	 * 
	 * @param types	Les types de comptes acceptés
	 */
	public FilterCompte(TypeCompte... types) {
		this.types = types;

		// Déterminer s'il y a des types bancaires dans les arguments
		for (TypeCompte type : types) {
			if (type.isBancaire()) {
				someBancaires = true;
				break;
			}
		}// for types
	}// constructeur
	
	/** Détermine si le filtre accepte des comptes bancaires. */
	public boolean acceptsBancaires() {
		return someBancaires;
	}

	/** Détermine si le compte spécifié est accepté par le filtre.
	 * 
	 *  @return	true si le compte est d'un des types spécifiés à l'instantiation
	 *  		et n'est pas clôturé. */
	public boolean accepts(Compte compte) {
		if (compte.getCloture() != null) {			// Pas de comptes clôturés
			return false;
		}
		
		for (TypeCompte type : types) {				// Pour chaque type accepté
			if (compte.getType() == type) {			// Si le bon type
				return true;						// Accepter le compte
			}
		}// for types
		return false;								// Sinon, ne pas accepter
	}// accepts

}
