package haas.olivier.comptes.gui.actions;

import java.io.IOException;

import haas.olivier.comptes.Compte;

/**
 * Un observateur des changements d'un compte.
 * 
 * @author Olivier Haas
 */
public interface CompteObserver {
	void compteChanged(Compte compte) throws IOException;
}
