package haas.olivier.comptes.dao0;

import haas.olivier.comptes.Banque;

import java.util.Set;

public interface AbstractBanqueDAO {

	/** Renvoie toutes les banques contenues dans le modèle. */
	public Set<Banque> getAll();
}
