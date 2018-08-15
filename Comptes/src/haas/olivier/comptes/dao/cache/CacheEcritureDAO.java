package haas.olivier.comptes.dao.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.TreeSet;

import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.EcritureMissingArgumentException;
import haas.olivier.comptes.InconsistentArgumentsException;
import haas.olivier.comptes.dao.EcritureDAO;
import haas.olivier.comptes.dao.IdGenerator;
import haas.olivier.util.Month;
import haas.olivier.util.ReadOnlyIterator;

/**
 * Un objet d'accès aux données qui garde en cache toutes les écritures.
 * 
 * @author Olivier HAAS
 */
class CacheEcritureDAO implements EcritureDAO {
	
	/**
	 * Insère une écriture dans une collection à deux niveaux.
	 * 
	 * @param e			L'écriture à ajouter.
	 * 
	 * @param map		La collection à deux niveaux dans laquelle ajouter
	 * 					l'écriture.
	 * 
	 * @param date		La date déterminant dans quelle collection de deuxième
	 * 					niveau l'écriture doit être ajoutée.<br>
	 * 					Si <code>null</code>, alors l'écriture est ajoutée parmi
	 * 					les écritures du mois en cours.
	 * 
	 * @param pointages	<code>true</code> s'il faut créer des collections de
	 * 					deuxième niveau triant les écritures par pointages
	 * 					plutôt que par ordre naturel.
	 */
	private static void insert(Ecriture e,
			Map<Month, NavigableSet<Ecriture>> map, Date date,
			boolean pointages) {
		
		// Vérifier qu'une sous-collection existe pour le mois voulu
		Month month = Month.getInstance(date);	// Le mois (ou mois en cours)
		if (!map.containsKey(month)) {
			map.put(month, pointages			// Collection par pointages...
					? new TreeSet<Ecriture>(new Ecriture.SortPointages())
					: new TreeSet<Ecriture>());	// ...ou par ordre naturel
		}
		
		// Ajouter l'écriture
		map.get(month).add(e);
	}
	
	/**
	 * Insère un texte dans un index.
	 * 
	 * @param map	L'index.
	 * @param s		Le texte à insérer.
	 */
	private static void insertInIndex(Map<String, Integer> map, String s) {
		
		// Le nombre d'occurrences du texte (ou null si absent)
		Integer n = map.get(s);
		
		// Insérer dans l'index
		if (n == null) {
			map.put(s, 1);		// 1ère occurrence
		} else {
			map.put(s, ++n);	// Occurrence suivante
		}
	}

	/**
	 * Les écritures, triées par mois puis par ordre naturel.
	 */
	private final NavigableMap<Month, NavigableSet<Ecriture>> ecritures =
			new TreeMap<>();
	
	/**
	 * Les écritures, triées par mois de pointage puis par date de pointage.
	 */
	private final NavigableMap<Month, NavigableSet<Ecriture>> pointages =
			new TreeMap<>();
			
	/**
	 * Les écritures en fonction de leur identifiant.
	 */
	private final Map<Integer, Ecriture> nums = new HashMap<>();
	
	/**
	 * Le générateur d'identifiants.
	 */
	private IdGenerator idGen= new IdGenerator();
	
	/**
	 * Drapeau indiquant si les données ont été modifiées depuis la dernière
	 * sauvegarde.
	 */
	private boolean mustBeSaved;
	
	/**
	 * Construit un objet d'accès aux données qui garde en cache toutes les
	 * écritures.
	 *  
	 * @param ecritures	Un itérable de toutes les écritures.
	 */
	public CacheEcritureDAO(Iterator<Ecriture> ecritures) {
		while (ecritures.hasNext())
			add(ecritures.next());
		
		// Rien n'a changé pour l'instant (même si on vient d'appeler add() !)
		mustBeSaved = false;
	}
	
	@Override
	public Ecriture get(Integer id) {
		return nums.get(id);
	}
	
	@Override
	public Iterable<Ecriture> getAll() {
		return new EcrituresIterable(ecritures, false);
	}
	
	@Override
	public Iterable<Ecriture> getAllBetween(Month from, Month to) {
		return new EcrituresIterable(
				ecritures.tailMap(from, true).headMap(to, true), false);
	}

	@Override
	public Iterable<Ecriture> getAllSince(Month month) {
		return new EcrituresIterable(ecritures.tailMap(month, true), true);
	}

	@Override
	public Iterable<Ecriture> getPointagesSince(Month month) {
		return new EcrituresIterable(pointages.tailMap(month, true), true);
	}

	@Override
	public Iterable<Ecriture> getAllTo(Month month) {
		return new EcrituresIterable(ecritures.headMap(month, true), false);
	}

	@Override
	public Iterable<Ecriture> getPointagesTo(Month month) {
		return new EcrituresIterable(pointages.headMap(month, true), false);
	}

	@Override
	public void add(Ecriture e) {
		
		// S'il n'y a pas d'identifiant
		if (e.id == null) {
			
			// Attribuer un identifiant et réinstancier l'écriture
			try {
				e = new Ecriture(idGen.getId(), e.date, e.pointage, e.debit,
						e.credit, e.montant, e.libelle, e.tiers, e.cheque);
				
			} catch (EcritureMissingArgumentException
					| InconsistentArgumentsException e1) {
				// Ne devrait pas arriver puisqu'on prend les mêmes arguments
				throw new IllegalArgumentException(e1);
			}// try
			
		} else {
			
			// Mémoriser l'identifiant pour éviter de l'attribuer en double
			idGen.addId(e.id);
		}// if id
		
		// Ajouter l'écriture
		insert(e, ecritures, e.date, false);	// Collection ordre naturel
		insert(e, pointages, e.pointage, true);	// Collection ordre de pointage
		nums.put(e.id, e);						// Collection par numéros
		mustBeSaved = true;						// Sauvegarde attendue
	}
	
	@Override
	public void remove(int id) {
		
		// Supprimer de la collection par identifiants
		Ecriture e = nums.remove(id);
		
		// Supprimer de la collection triée par ordre naturel
		ecritures.get(Month.getInstance(e.date)).remove(e);
		
		// Supprimer de la collection triée par ordre de pointage
		pointages.get(Month.getInstance(e.pointage)).remove(e);
		
		// Marquer qu'une sauvegarde est attendue
		mustBeSaved = true;
	}
	
	@Override
	public void update(Ecriture e) {
		
		// Supprimer l'écriture existante portant cet identifiant
		remove(e.id);
		
		// Ajouter la nouvelle écriture à la place
		add(e);
		
		mustBeSaved = true;
	}
	
	/**
	 * Efface toutes les données.
	 */
	void erase() {
		ecritures.clear();
		pointages.clear();
		nums.clear();
		mustBeSaved = true;
		idGen = new IdGenerator();
	}

	@Override
	public Map<String, Integer> constructCommentIndex() {
		Map<String, Integer> result = new HashMap<>();
		
		// Parcourir toutes les écritures (par numéro, c'est plus simple)
		for (Ecriture e : nums.values()) {
			insertInIndex(result, e.libelle);	// Insérer le libellé
			insertInIndex(result, e.tiers);		// Insérer le nom du tiers
		}
		
		return result;
	}
	
	/**
	 * Renvoie le premier mois des écritures (le plus ancien).
	 * 
	 * @return	Un mois, ou <code>null</code> s'il n'y a aucune écriture.
	 */
	Month getDebut() {
		return ecritures.isEmpty() ? null : ecritures.firstKey();
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

}// class CacheEcritureDAO

/**
 * Un <i>wrapper</i> pour utiliser des <code>EcrituresIterator</code> sous
 * l'interface d'un <code>Iterable</code>.
 *
 * @author Olivier HAAS
 */
class EcrituresIterable implements Iterable<Ecriture> {

	/**
	 * La collection à faire parcourir par l'itérateur des écritures. Il s'agit
	 * d'une collection à deux niveaux (une collection de collections).
	 */
	private final Iterable<NavigableSet<Ecriture>> coll;
	
	/**
	 * Drapeau indiquant si les écritures doivent être parcourues dans l'ordre
	 * naturel (<code>true</code>) ou dans l'ordre inverse (<code>false</code>).
	 */
	private final boolean ordre;
	
	/**
	 * Renvoie un objet contenant des écritures et pouvant être parcouru avec
	 * l'interface <code>Iterable</code>.
	 * <p>
	 * Les valeurs, tant au premier qu'au deuxième niveau, seront parcourues
	 * dans l'ordre inverse.
	 * 
	 * @param map	Une <code>Map</code> dont les valeurs sont des collections
	 * 				d'écritures.
	 * 
	 * @param ordre	<code>true</code> si les écritures et les valeurs de 1er
	 * 				niveau (généralement des mois) doivent être triées dans leur
	 * 				ordre naturel.
	 */
	public EcrituresIterable(NavigableMap<?, NavigableSet<Ecriture>> map,
			boolean ordre) {
		
		// Trier les mois dans l'ordre chronologique, ou l'ordre inverse
		coll = (ordre ? map : map.descendingMap()).values();
		this.ordre = ordre;
	}

	@Override
	public Iterator<Ecriture> iterator() {
		return new EcrituresIterator(coll.iterator(), ordre);
	}
	
}// EcrituresIterable

/**
 * Un itérateur qui parcourt les écritures dans une collection à deux niveaux.
 * <p>
 * Cette classe est particulièrement utile pour parcourir les écritures, sachant
 * que <code>CacheEcritureDAO</code> stocke les écritures dans des collections à
 * deux niveaux.
 * 
 * @author Olivier HAAS
 */
class EcrituresIterator extends ReadOnlyIterator<Ecriture> {
	
	/**
	 * L'itérateur principal qui parcourt le premier niveau de la collection.
	 */
	private final Iterator<NavigableSet<Ecriture>> it1;
	
	/**
	 * L'itérateur de second niveau actuel.
	 */
	private Iterator<Ecriture> it2;
	
	/**
	 * Drapeau indiquant si les écritures doivent être parcourues dans l'ordre
	 * naturel (<code>true</code>) ou dans l'ordre inverse (<code>false</code>).
	 */
	private final boolean ordre;
	
	/**
	 * Construit un itérateur d'écritures à partir d'une collection à deux
	 * niveaux.
	 * <p>
	 * Les collections de deuxième niveau seront chacune parcourue en ordre
	 * inverse. Ainsi, 
	 * 
	 * @param it	Un <code>Iterator</code> dont les valeurs sont des
	 * 				collections d'écritures.
	 * 
	 * @param ordre	<code>true</code> si les écritures doivent être parcourues
	 * 				dans leur ordre naturel.
	 */
	public EcrituresIterator(Iterator<NavigableSet<Ecriture>> it,
			boolean ordre) {
		it1 = it;
		this.ordre = ordre;
	}

	@Override
	public boolean hasNext() {
		while (it2 == null || !it2.hasNext()) {	// Pas d'écriture suivante ?
			if (it1.hasNext()) {				// Essayer le lot suivant
				it2 = ordre
						? it1.next().iterator()				// Ordre naturel
						: it1.next().descendingIterator();	// Ordre inverse
			} else {
				return false;					// Pas de lot suivant = fini
			}
		}
		return true;
	}

	@Override
	public Ecriture next() {
		if (!hasNext())					// Repositionner le curseur si besoin
			throw new NoSuchElementException();
		return it2.next();				// Écriture suivante
	}
}// class EcrituresIterator
