package haas.olivier.comptes.dao0.buffer;

import haas.olivier.comptes.MessagesFactory;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.PermanentDAO;
import haas.olivier.comptes.dao.util.CachePermanent;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BufferedPermanentDAO implements PermanentDAO {

	// Sous-couche DAO
	private BufferablePermanentDAO dao;
	
	// Cache de toutes les opérations permanentes
	private CachePermanent cache;

	// Collections d'opérations permanentes à ajouter/supprimer/mettre à jour
	private Set<Integer> remove = new HashSet<Integer>();
	private Map<Integer, Permanent> add = new HashMap<Integer, Permanent>();
	private Map<Integer, Permanent> update = new HashMap<Integer, Permanent>();
	
	public BufferedPermanentDAO(BufferablePermanentDAO dao) {
		this.dao = dao;							// Mémoriser le DAO
		cache = new CachePermanent(dao);		// Créer le cache
	}
	
	/** @return	Le plus grand identifiant augmenté de 1, ou 0 s'il n'y a aucun
	 * 			Permanent.
	 * @throws IOException 
	 */
	private int nextId() throws IOException {
		
		// Chercher dans le cache
		int maxId = cache.getNextId();
		
		// Chercher dans le buffer des écritures ajoutées
		for (Integer id : add.keySet()) {
			if (id >= maxId) {			// Aussi grand ou plus grand ?
				maxId = id + 1;			// Prendre encore plus grand
			}
		}
		return maxId;
	}// nextId

	@Override
	public Permanent get(int id) throws IOException {

		// Opération permanente supprimée ?
		if (remove.contains(id)) {
			MessagesFactory.getInstance().showErrorMessage(
					"Tentative de lire une opération permanente supprimée");
			throw new IOException();
		}

		// Opération permanente mise à jour ?
		if (update.containsKey(id)) {
			return update.get(id);
		}

		// Opération permanente ajoutée ?
		if (add.containsKey(id)) {
			return add.get(id);
		}

		// Sinon, aller lire dans le cache
		for (Permanent p : cache.getCache()) {
			if (new Integer(id).equals(p.id)) {
				return p;
			}
		}// for cache
		
		// Inconnu au bataillon: erreur !
		throw new IOException();
	}// get

	@Override
	public Set<Permanent> getAll() throws IOException {

		// Cloner le cache (pour pouvoir modifier la collection)
		Set<Permanent> all = cache.getCloneCache();

		// Enlever les opérations permanentes supprimées
		Iterator<Permanent> it = all.iterator();
		while (it.hasNext()) {
			Permanent p = it.next();
			if (remove.contains(p.id)) {
				it.remove();
			}
		}

		// Enlever les opérations permanentes mises à jour
		Iterator<Permanent> it2 = all.iterator();
		while (it2.hasNext()) {
			Permanent p = it2.next();
			if (update.containsKey(p.id)) {
				it2.remove();
			}
		}

		// Ajouter les opérations permanentes ajoutées ou mises à jour
		all.addAll(update.values());
		all.addAll(add.values());

		return all;
	}// getAll

	@Override
	public Permanent add(Permanent p) throws IOException {

		Integer id = p.id;

		// Un permanent de même identifiant aurait-il été supprimé ?
		if (id != null && remove.contains(id)) {
			remove.remove(id); // Ne pas supprimer
			update.put(id, p); // Transformer en mise à jour
		}

		// Ajouter l'opération permanente
		Permanent ajoute = p;
		if (id == null) {
			// Pas d'identifiant: cloner le Permanent en ajoutant un id

			switch (p.type) {
			case Permanent.FIXE:
				ajoute = new Permanent(nextId(), p.nom, p.debit, p.credit,
						p.jours, p.montants);
				break;
				
			case Permanent.PROPORTIONNEL:
				ajoute = new Permanent(nextId(), p.nom, p.debit, p.credit,
						p.jours, p.dependance, p.taux);
				break;
				
			case Permanent.SOLDER:
				ajoute = new Permanent(nextId(), p.nom, p.compteASolder,
						p.credit, p.jours);
			}// switch
			
			// Définir les autres paramètres
			ajoute.pointer = p.pointer;
			ajoute.tiers = p.tiers;
			ajoute.libelle = p.libelle;
		}// if id null

		// Stocker le nouveau Permanent pour ajout
		add.put(ajoute.id, ajoute);

		return ajoute;
	}// add

	@Override
	public void update(Permanent p) throws IOException {
		Integer id = p.id;

		// Ce Permanent aurait-il été supprimé ?
		if (remove.contains(id)) {

			// Le Permanent n'est pas censé exister: lever une exception
			throw new IOException();
		}

		// Ce Permanent a-t-il été ajouté récemment ?
		if (add.containsKey(id)) {

			// Remplacer le Permanent à ajouter par celui-ci
			add.put(id, p);

		} else {

			// Mettre à jour simplement
			update.put(id, p);
		}// if add containsKey
	}// update

	@Override
	public void remove(int id) {
		Integer i = new Integer(id);

		if (add.containsKey(i)) {
			// Ce Permanent a été ajouté récemment

			// Ne pas l'ajouer
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
	}// flush
	
	
	/** Efface toutes les données du buffer et du cache. */
	void erase() {
		add.clear();
		remove.clear();
		update.clear();
		cache = new CachePermanent(dao);
	}// erase
}
