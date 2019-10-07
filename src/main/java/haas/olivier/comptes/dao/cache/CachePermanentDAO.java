/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.cache;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.PermanentProport;
import haas.olivier.comptes.PermanentState;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.IdGenerator;
import haas.olivier.comptes.dao.PermanentDAO;

/**
 * Un objet d'accès aux données qui garde en cache toutes les opérations
 * permanentes.
 *
 * @author Olivier HAAS
 */
public class CachePermanentDAO implements PermanentDAO {

	/**
	 * Toutes les opérations permanentes.
	 */
	private final Map<Integer, Permanent> permanents = new HashMap<>();
	
	/**
	 * Drapeau indiquant si les données ont été modifiées depuis la dernière
	 * sauvegarde.
	 */
	private boolean mustBeSaved = false;
	
	/**
	 * Le générateur d'identifiants.
	 */
	// TODO Supprimer les identifiants des opérations permanentes
	private IdGenerator idGen = new IdGenerator();
	
	/**
	 * Construit un objet d'accès aux données qui garde en cache toutes les
	 * opérations permanentes.
	 * 
	 * @param factory	Une fabrique de DAO permettant de récupérer au cours de
	 * 					l'énumération les objets <code>Permanent</code> déjà
	 * 					instanciés (utile pour les instances qui dépendent d'une
	 * 					autre).
	 * 
	 * @param cDAO		Un DAO d'accès aux comptes, pour permettre de récupérer
	 * 					les objets <code>Compte</code> auxquelles doivent se
	 * 					référer les <code>Permanent</code>s à instancier.
	 * 
	 * @throws IOException
	 */
	public CachePermanentDAO(CacheableDAOFactory factory, CompteDAO cDAO)
			throws IOException {
		Iterator<Permanent> perm = factory.getPermanents(this);
		while (perm.hasNext()) {
			Permanent p = perm.next();		// L'opération permanente
			permanents.put(p.getId(), p);	// Ajouter l'opération permanente
			idGen.addId(p.getId());			// Mémoriser l'identifiant
		}
	}

	/**
	 * Renvoie toutes les opérations permanentes.<br>
	 * Les opérations dépendant d'une autre sont toujours parcourues après
	 * l'opération dont elles dépendent.
	 * <p>
	 * On utilise un <code>LinkedHashSet</code> pour permettre d'insérer les
	 * opérations permanentes après leurs dépendances, sans jamais avoir de
	 * doublon.
	 */
	@Override
	public Collection<Permanent> getAll() throws IOException {
		LinkedHashSet<Permanent> permanentSet = new LinkedHashSet<>();
		for (Permanent p : permanents.values())
			addWithDependances(p, permanentSet);
		return permanentSet;
	}
	
	/**
	 * Ajoute une opération permanente dans l'ensemble ordonné spécifié, après y
	 * avoir préalablement ajouté ses dépendances successives.
	 * 
	 * @param permanent	L'opération permanente à ajouter après ses éventuelles
	 * 					dépendances successives.
	 * 
	 * @param set		L'ensemble ordonné dans lequel ajouter
	 * 					<code>permanent</code> et ses dépendances.
	 */
	private void addWithDependances(Permanent permanent,
			LinkedHashSet<Permanent> set) {
		PermanentState state = permanent.getState();
		
		// Appel récursif pour s'assurer de l'insertion de la dépendance d'abord
		if (state instanceof PermanentProport)
			addWithDependances(((PermanentProport) state).dependance, set);
		
		set.add(permanent);
	}

	@Override
	public Permanent get(int id) {
		return permanents.get(id);
	}

	@Override
	public void add(Permanent p) {
		
		// Selon que l'opération à ajouter possède déjà un identifiant ou non
		if (p.getId() == null) {
			p.setId(idGen.getId());
			
		} else if (permanents.containsKey(p.getId())) {	// Identifiant existant
			throw new IllegalArgumentException(String.format(
					"Impossible d'ajouter au modèle : L'opération n°%s existe déjà",
					p.getId()));
		}
		
		permanents.put(p.getId(), p);
		mustBeSaved = true;							// Sauvegarde attendue
	}

	/**
	 * Cette méthode marque seulement que le modèle a été modifié.
	 * <p>
	 * Le cache gardant les références vers les instances, l'objet est déjà
	 * modifié dans le cache.
	 */
	@Override
	public void update(Permanent p) {
		mustBeSaved = true;
	}

	@Override
	public void remove(int id) {
		permanents.remove(id);						// Supprimer du cache
		mustBeSaved = true;							// Sauvegarde attendue
	}
	
	/**
	 * Efface toutes les données.
	 */
	void erase() {
		permanents.clear();
		idGen = new IdGenerator();
		mustBeSaved = true;
	}
	
	/**
	 * Indique si les données ont été modifiées depuis la dernière sauvegarde.
	 */
	boolean mustBeSaved() {
		return mustBeSaved;
	}

	/**
	 * Oblige l'objet à considérer que les modifications actuelles ont été
	 * sauvegardées.
	 */
	void setSaved() {
		mustBeSaved = false;
	}
}
