package haas.olivier.autocompletion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Une interface de filtre et tri pour des tableaux de valeurs.
 *
 * @author Olivier HAAS
 */
public class ItemFilter<T> {

	/** Le comparateur utilisé pour trier les données filtrées. */
	private Comparator<T> comparator = null;
	
	/** Renvoie le comparateur utilisé pour le tri des données. */
	public Comparator<T> getComparator() {
		return comparator;
	}
	
	/** Définit un nouveau comparateur pour le tri des données. */
	public void setComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}
	
	/** Filtre et trie les données.
	 * 
	 * @param values	Les données à filtrer et trier.
	 * @param text		Le texte du filtre
	 * @return			Un nouveau tableau contenant les seules données
	 * 					correspondant au filtre, et triées dans un ordre
	 * 					spécifique à l'implémentation.
	 */
	public Collection<T> filter(Collection<T> values, String text) {
		
		// Créer une liste résultat
		List<T> list = new ArrayList<T>();			// Liste résultat
		if (values == null) {						// Si aucune valeur donnée
			return list;							// Retourner la liste vide
		}
		
		// Filtrer les valeurs
		for (T v : values) {
			if (isAccepted(v, text)) {
				list.add(v);
			}
		}// for case
		
		// Trier les valeurs restantes
		if (comparator != null) {
			
			// Utiliser le comparateur spécifié
			Collections.sort(list, comparator);
			
		} else {
			
			// Tenter d'utiliser un ordre naturel pour les éléments
			try {
				Collections.sort(list, new Comparator<T>() {
					@Override
					@SuppressWarnings("unchecked")
					public int compare(T o1, T o2) {
						return ((Comparable<T>) o1).compareTo(o2);
					}// compare
				});// classe anonyme Comparator
				
			} catch (ClassCastException e) {			// Pas comparable
				if (!list.isEmpty()) {
					Object o = list.get(0);
					System.err.println("Impossible de trier des objets" +
							(o != null ? " de classe " + o.getClass() : ""));
				}// if list empty
			}// try sort
		}// if comparator null
		return list;
	}// filter
	
	/** Détermine si l'objet spécifié doit être accepté au regard du texte
	 * utilisé pour le filtre.
	 * Cette implémentation retient tous les objets dont la conversion en chaîne
	 * de caractères contient le texte spécifié, insensiblement à la casse.
	 * 
	 * @param value	L'objet à tester.
	 * @param text	Le texte utilisé pour le filtre
	 * @return		true si l'objet doit être accepté, false s'il doit être
	 * 				filtré. 
	 */
	protected boolean isAccepted(T value, String text) {
		return value != null && value.toString() != null &&
				value.toString().toLowerCase()		//Conversion texte minuscule
				.contains(text.toLowerCase());		// Commence par le filtre
	}// isAccepted
}
