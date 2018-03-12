package haas.olivier.comptes.dao.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import haas.olivier.comptes.Banque;
import haas.olivier.comptes.dao.BanqueDAO;
import haas.olivier.comptes.dao.IdGenerator;

/**
 * Un objet d'accès aux données qui garde en cache toutes les instances des
 * banques.
 *
 * @author Olivier HAAS
 */
class CacheBanqueDAO implements BanqueDAO {

	/**
	 * La collection des banques.
	 */
	private final Map<Integer, Banque> banques = new HashMap<>();
	
	/**
	 * Drapeau indiquant si les données ont été modifiées depuis la dernière
	 * sauvegarde.
	 */
	private boolean mustBeSaved = false;
	
	/**
	 * Le générateur d'identifiants.
	 */
	private IdGenerator idGen = new IdGenerator();
	
	/**
	 * Construit un objet d'accès aux banques qui garde toutes les instances en
	 * cache.
	 * 
	 * @param banques	Toutes les banques de la source.
	 */
	public CacheBanqueDAO(Iterator<Banque> banques) {
		while (banques.hasNext()) {
			Banque b = banques.next();
			this.banques.put(b.id, b);
		}
	}
	
	@Override
	public Iterable<Banque> getAll() {
		return banques.values();
	}
	
	@Override
	public Banque get(int id) {
		return banques.get(id);
	}
	
	@Override
	public Banque add(Banque b) {
		
		// Selon que l'instance fournie a ou non un identifiant
		if (b.id == null) {
			
			// Créer une nouvelle
			b = new Banque(idGen.getId(), b.nom, b.getBytes());
			
		} else {
			// Ajouter celle-ci
			idGen.addId(b.id);
		}
		
		// Ajouter
		banques.put(b.id, b);
		mustBeSaved = true;							// DAO modifié
		return b;
	}
	
	/**
	 * Efface toutes les données.
	 */
	void erase() {
		banques.clear();
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
