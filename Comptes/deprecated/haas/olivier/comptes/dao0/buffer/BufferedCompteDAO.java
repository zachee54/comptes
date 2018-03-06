package haas.olivier.comptes.dao0.buffer;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.util.CacheCompte;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Buffer pour les BufferableCompteDAO.
 * 
 * @author Olivier HAAS
 */
public class BufferedCompteDAO implements CompteDAO {

	// Sous-couche DAO
	private BufferableCompteDAO dao;

	// Collections de comptes à ajouter/supprimer/mettre à jour
	private Set<Integer> remove = new HashSet<Integer>();
	private Map<Integer, Compte> add = new HashMap<Integer, Compte>();
	private Map<Integer, Compte> update = new HashMap<Integer, Compte>();

	// Cache de tous les comptes
	private CacheCompte cache;
	
	public BufferedCompteDAO(BufferableCompteDAO dao) {
		this.dao = dao;							// Mémoriser le DAO
		cache = new CacheCompte(dao);			// Créer le cache
	}
	
	/** @return	Le plus grand identifiant augmenté de 1, ou 0 s'il n'y a aucun
	 * 			compte. 
	 * @throws IOException */
	private int nextId() throws IOException {
		
		// Trouver le prochain id d'après le cache
		int maxId = cache.getNextId();
		
		// Chercher s'il y a un plus grand dans le buffer des écritures ajoutées
		for (Integer id : add.keySet()) {
			if (id >= maxId) {			// Aussi grand ou plus grand ?
				maxId = id + 1;			// Prendre encore plus grand
			}
		}
		return maxId;
	}// getMaxId

	@Override
	public Compte get(int id) throws IOException {

		// Compte supprimé ?
		if (remove.contains(id)) {
			MessagesFactory.getInstance().showErrorMessage(
					"Tentative de lire un compte supprimé");
			throw new IOException();
		}

		// Compte mis à jour ?
		if (update.containsKey(id)) {
			return update.get(id);
		}

		// Compte ajouté ?
		if (add.containsKey(id)) {
			return add.get(id);
		}
		
		// Aller lire le compte dans le cache
		for (Compte c : cache.getCache()) {
			if (new Integer(id).equals(c.id)) {	// Bon id ?
				return c;
			}
		}// for cache
		
		// Ni ajouté, ni mis à jour, ni dans le cache: erreur !
		throw new IOException();
	}// get

	@Override
	public Set<Compte> getAll() throws IOException {

		// Cloner le cache
		Set<Compte> all = cache.getCloneCache(); 

		// Enlever les comptes supprimés
		Iterator<Compte> it = all.iterator();
		while (it.hasNext()) {
			Compte c = it.next();
			if (remove.contains(c.id)) {
				it.remove();
			}
		}

		// Enlever les comptes mis à jour
		Iterator<Compte> it2 = all.iterator();
		while (it2.hasNext()) {
			Compte c = it2.next();
			if (update.containsKey(c.id)) {
				it2.remove();
			}
		}

		// Ajouter les comptes ajoutés ou mis à jour
		all.addAll(update.values());
		all.addAll(add.values());

		return all;
	}// getAll

	@Override
	public Compte add(Compte c) throws IOException {

		Integer id = c.id;

		// Un compte de même identifiant aurait-il été supprimé ?
		if (id != null && remove.contains(id)) {
			remove.remove(id);		// Ne pas supprimer
			update.put(id, c);		// Transformer en mise à jour
		}

		// Ajouter le compte
		Compte ajoute = null;
		if (id == null) {
			// Pas d'identifiant: cloner le compte en lui ajoutant un id

			// Définir un nouveau compte avec le nouvel identifiant
			if (c instanceof CompteBudget) {
				ajoute = new CompteBudget(nextId(), c.getNom(), c.getType());

			} else if (c instanceof CompteBancaire) {
				ajoute = new CompteBancaire(nextId(), c.getNom(),
						((CompteBancaire) c).getNumero(),
						c.getType());
			}

			// Autres propriétés du compte ajouté
			ajoute.setOuverture(c.getOuverture());
			ajoute.setCloture(c.getCloture());

		} else {
			// Déjà un identifiant: garder le même objet
			ajoute = c;
		}

		// Stocker le nouveau compte pour ajout
		add.put(ajoute.id, ajoute);

		return ajoute;
	}// add

	@Override
	public void update(Compte c) throws IOException {
		Integer id = c.id;

		// Ce compte aurait-il été supprimé ?
		if (remove.contains(id)) {

			// Le compte n'est pas censé exister: lever une exception
			throw new IOException();
		}

		// Ce compte a-t-il été ajouté récemment ?
		if (add.containsKey(id)) {

			// Remplacer le compte à ajouter par celui-ci
			add.put(id, c);

		} else {

			// Mettre à jour simplement ce compte
			update.put(id, c);
		}// if add containsKey
	}// update

	@Override
	public void remove(int id) throws IOException {
		Integer i = new Integer(id);
		
		// Vérifier que ce Compte n'est pas utilisé
		for (Ecriture e : DAOFactory.getFactory().getEcritureDAO().getAll()) {
			if (i.equals(e.debit.id) || i.equals(e.credit.id)) {
				MessagesFactory.getInstance().showErrorMessage(
						"Impossible de supprimer le compte n°" + id +
						"\nIl est utilisé dans l'écriture n°" + e.id);
				throw new IOException();
			}
		}// for all Ecritures

		if (add.containsKey(i)) {
			// Ce compte a été ajouté récemment

			// Ne pas l'ajouter
			add.remove(i);

		} else if (update.containsKey(i)) {
			// Mis à jour récemment

			// Ne pas mettre à jour
			update.remove(i);
		}

		// Supprimer dans tous les cas
		remove.add(i);
	}// remove
	
	/** Indique si ce DAO a besoin d'être sauvegardé. */
	public boolean mustBeSaved() {
		
		// Dès qu'il y a au moins un élément dans le buffer, il faut sauvegarder
		return ((add.size() + update.size() + remove.size()) != 0);
	}

	/**
	 * Sauvegarde les données en cours et vide le buffer.
	 * 
	 * @throws IOException
	 */
	void flush() throws IOException {

		// Sauvegarder
		dao.save(add, update, remove);

		synchronized(this) {
			
			// Vider le buffer
			add.clear();
			update.clear();
			remove.clear();
			
			// Vider le cache
			cache.clear();
		}
	}// save
	
	/** Efface toutes les données du buffer et du cache. */
	void erase() {
		add.clear();
		remove.clear();
		update.clear();
		cache = new CacheCompte(dao);
	}
}