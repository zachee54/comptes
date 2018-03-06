package haas.olivier.comptes.gui.actions;

import haas.olivier.comptes.Compte;

/** Observer un changement de compte. */
public interface CompteObserver {
	void compteChanged(Compte compte);
}
