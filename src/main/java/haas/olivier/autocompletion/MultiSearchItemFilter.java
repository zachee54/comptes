package haas.olivier.autocompletion;


import java.util.Collection;

/** Un filtre d'items qui utilise une recherche sur plusieurs critères.
 * Le texte du filtre peut contenir plusieurs expressions séparées par des
 * espaces.
 * Les items retenus sont ceux qui vérifient toutes ces expressions 
 * (cf. <code>ItemFilter.isAccepted()</code>), quelles que soient leurs places
 * dans la chaîne.
 * 
 * @author Olivier HAAS
 */
public class MultiSearchItemFilter<T extends Comparable<? super T>>
extends ItemFilter<T> {

	/** Un tableau des expressions du filtre. */
	private String[] pattern;
	
	@Override
	public Collection<T> filter(Collection<T> values, String text) {
		pattern = text.split(" ");					// Séparer les expressions
		return super.filter(values, text);			// Faire le reste du travail
	}// filter
	
	@Override
	protected boolean isAccepted(T value, String text) {
		for (String p : pattern) {					// Pour chaque expression
			if (!super.isAccepted(value, p)) {		// Si l'item ne vérifie pas
				return false;						// Rejeter tout
			}
		}// for pattern
		return true;		// Arrivé ici, l'item vérifie toutes les expressions
	}// isAccepted
}
