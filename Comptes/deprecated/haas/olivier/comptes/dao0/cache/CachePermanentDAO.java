package haas.olivier.comptes.dao0.cache;

import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.dao.PermanentDAO;
import haas.olivier.comptes.dao.util.CachePermanent;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/** Une classe d'accès aux Permanents, implémentant un cache.
 * Lors de la sauvegarde, toutes les données du cache sont sauvegardées et
 * écrasent toutes les anciennes données.
 *
 * @author Olivier HAAS
 */
public class CachePermanentDAO implements PermanentDAO {

	/** La sous-couche DAO. */
	private CacheablePermanentDAO dao;
	
	/** Le cache. */
	private CachePermanent cache;
	
	/** Indicateur de modification des données. */
	boolean mustBeSaved = false;
	
	/** Construit un CachePermanentDAO pointant vers le CacheablePermanentDAO
	 * spécifié.
	 * Les données sont chargées à la création de l'objet.
	 * @throws IOException 
	 */
	public CachePermanentDAO(CacheablePermanentDAO dao) {
		this.dao = dao;
		cache = new CachePermanent(dao);
	}// constructeur
	
	@Override
	/** Renvoie tous les Permanents. La collection renvoyée peut être modifiée
	 * librement. */
	public Set<Permanent> getAll() throws IOException {
		return cache.getCloneCache();
	}

	@Override
	public Permanent add(Permanent p) throws IOException {
		Permanent nouveau = p;
		
		// S'il faut lui trouver un identifiant
		if (p.id == null) {
			
			// Réinstancier selon le type de Permanent
			switch (p.type) {
			case Permanent.FIXE:
				nouveau = new Permanent(cache.getNextId(), p.nom, p.debit,
						p.credit, p.jours, p.montants);
				break;
				
			case Permanent.PROPORTIONNEL:
				nouveau = new Permanent(cache.getNextId(), p.nom, p.debit,
						p.credit, p.jours, p.dependance, p.taux);
				break;
				
			case Permanent.SOLDER:
				nouveau = new Permanent(cache.getNextId(), p.nom,
						p.compteASolder, p.credit, p.jours);
			}// switch
			
			nouveau.pointer = p.pointer;
			nouveau.libelle = p.libelle;
			nouveau.tiers = p.tiers;
		}// if id null
		
		// Ajouter le nouveau Permanent
		cache.getCache().add(nouveau);
		
		// Indiquer que les données ont été modifiées
		mustBeSaved = true;

		return nouveau;
	}// add

	@Override
	public void update(Permanent p) throws IOException {
		remove(p.id);								// Supprimer l'ancien
		add(p);										// Insérer le nouveau
	}// update

	@Override
	public void remove(int id) throws IOException {
		Iterator<Permanent> it = cache.getCache().iterator();
		while (it.hasNext()) {
			if (id == it.next().id.intValue()) {	// Bon id ?
				it.remove();						// Supprimer
				
				// Indiquer que les données ont été modifiées
				mustBeSaved = true;
				
				break;								// Arrêter là
			}// if id
		}// while cache
	}// remove

	@Override
	public Permanent get(int id) throws IOException {
		for (Permanent p : cache.getCache()) {
			if (p.id.intValue() == id) {			// Bon id ?
				return p;							// Renvoyer ce résultat
			}
		}// for cache
		throw new IOException();					// Introuvable : erreur !
	}// get
	
	/** Sauvegarde tout le contenu du cache en écrasant les anciennes données.
	 * @throws IOException 
	 */
	public void save() throws IOException {
		dao.save(cache.getCache());
	}
	
	/** Efface toutes les données du cache. */
	void erase() {
		cache = new CachePermanent(dao);
	}
}
