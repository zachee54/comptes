package haas.olivier.comptes.dao.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.dao.CompteDAO;

/**
 * Un cache des comptes.
 * 
 * @author Olivier HAAS
 */
class CacheCompteDAO implements CompteDAO {
	
	/**
	 * Drapeau indiquant si les données ont été modifiées depuis la dernière
	 * sauvegarde.
	 */
	private boolean mustBeSaved;
	
	/**
	 * Les instances existantes.
	 */
	private final IdentityHashMap<Compte, Void> instances =
			new IdentityHashMap<>();
	
	/**
	 * Construit un objet d'accès aux comptes.
	 */
	public CacheCompteDAO(Iterator<Compte> it) {
		while (it.hasNext())
			add(it.next());
		
		// Remettre à false car rien n'a changé pour l'instant
		mustBeSaved = false;
	}

	@Override
	public Collection<Compte> getAll() throws IOException {
		List<Compte> list = new ArrayList<>(instances.size());
		list.addAll(instances.keySet());
		Collections.sort(list);
		return list;
	}
	
	@Override
	public void add(Compte compte) {
		instances.put(compte, null);
		mustBeSaved = true;
	}
	
	@Override
	public void remove(Compte compte) throws IOException {
		instances.remove(compte);
		mustBeSaved = true;
	}
	
	@Override
	public void erase() {
		instances.clear();
		mustBeSaved = true;
	}
	
	/**
	 * Indique si des modifications ont eu lieu depuis la dernière sauvegarde.
	 */
	public boolean mustBeSaved() {
		return mustBeSaved;
	}
	
	/**
	 * Indique que les données viennent d'être sauvegardées.
	 */
	public void setSaved() {
		mustBeSaved = false;
	}
}