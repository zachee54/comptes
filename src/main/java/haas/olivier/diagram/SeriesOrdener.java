/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/** Un ordonnateur de séries. Il gère l'ordre des séries et leur masquage
 * éventuel.
 * <p>
 * Les identifiants des séries masquées peuvent être enregistrées dans des
 * propriétés et être donc persistantes.
 * 
 * @author Olivier HAAS
 */
public class SeriesOrdener implements Iterable<Serie> {

	/** L'observable auquel notifier les changements. */
	private final DiagramModelObservable observable =
			new DiagramModelObservable();
	
	/** Le nom du diagramme. */
	private String name;

	/** La liste des séries, masquées ou non. */
	private final List<Serie> series = new ArrayList<>();
	
	/** La collection des séries masquées. */
	private final Collection<Serie> hidden = new HashSet<>();
	
	/** Ajoute une série. Par défaut, elle n'est pas masquée.
	 * 
	 * @param serie	La série à ajouter. Si elle figurait déjà parmi les séries,
	 * 				elle est ajoutée une seconde fois.
	 */
	public void add(Serie serie) {
		series.add(serie);
		observable.dataChanged();
	}// addSerie

	/** Supprime la première occurrence d'une série, masquée ou non.
	 * <p>
	 * Si la série spécifiée n'existe pas, la méthode est sans effet.
	 * 
	 * @param serie	La série à supprimer.
	 */
	public void remove(Serie serie) {
		series.remove(serie);
		observable.dataChanged();
	}// removeSerie

	/** Renvoie le nombre de séries. */
	public int getSeriesCount() {
		return series.size();
	}// getSeriesCount

	/** Renvoie la série à l'index spécifié.
	 * 
	 * @param index	L'index de la série souhaitée.
	 * 
	 * @return		La série souhaitée.
	 * 
	 * @throws IndexOutOfBoundsException
	 */
	public Serie getSerieAt(int index) {
		return series.get(index);
	}// getSerieAt

	/** Déplace une série vers la fin de la liste.
	 * <p>
	 * Si l'index correspond à la dernière série, ou si l'index ne correspond à
	 * aucune série, la méthode ne fait rien.
	 * 
	 * @param index	L'index de la série à déplacer.
	 */
	public void moveBack(int index) {
		if (index >=0 && index < series.size() - 1)
			Collections.swap(series, index, index + 1);
		observable.dataChanged();
	}// moveBack
	
	/** Déplace une série vers le début de la liste.
	 * <p>
	 * Si l'index correspond à la première série, ou si l'index ne correspond à
	 * aucune série, la méthode ne fait rien.
	 * 
	 * @param index	L'index de la série à déplacer.
	 */
	public void moveForward(int index) {
		if (index > 0 && index < series.size())
			Collections.swap(series, index, index - 1);
		observable.dataChanged();
	}// moveForward
	
	/** Indique si la série spécifiée est masquée. */
	public boolean isHidden(Serie serie) {
		return hidden.contains(serie);
	}// isHidden
	
	/** Affiche ou masque une série.
	 * 
	 * @param serie	La série à afficher ou masquer.
	 * @param hide	<code>true</code> pour masquer la série, <code>false</code>
	 * 				pour l'afficher.
	 */
	public void setHidden(Serie serie, boolean hide) {
		if (hide) {
			hidden.add(serie);
		} else {
			hidden.remove(serie);
		}
		observable.dataChanged();
	}// setHidden

	/** Renvoie un memento contenant l'état actuel de l'instance. */
	public DiagramMemento getMemento() {
		List<Integer> seriesId = new ArrayList<>();
		for (Serie serie : series)
			seriesId.add(serie.id);
		
		Set<Integer> hiddenId = new HashSet<>();
		for (Serie serie : hidden)
			hiddenId.add(serie.id);
		
		return new DiagramMemento(name, seriesId, hiddenId);
	}// getMemento
	
	/** Applique un état précédent.
	 * <p>
	 * Les séries sont placées dans l'ordre du memento fourni, et sont masquées
	 * ou affchées selon ce même memento.
	 * <p>
	 * Les séries qui figurent dans le memento mais pas dans cet instance sont
	 * ignorées.<br>
	 * Les séries qui figurent dans cette instance mais pas dans le memento sont
	 * affichées et placées en dernières, dans un ordre non spécifié.
	 * 
	 * @param memento	L'état précédent à reconstituer.
	 */
	public void setMemento(DiagramMemento memento) {
		
		// Récupérer le nom du diagramme
		name = memento.getName();
		
		// Classer les séries actuelles par identifiant
		Map<Integer, Serie> seriesById = new HashMap<>();
		for (Serie serie : series)
			seriesById.put(serie.id, serie);
		
		// Classer les séries dans le nouvel ordre
		series.clear();
		for (Integer id : memento.getSeries()) {
			Serie serie = seriesById.remove(id);
			if (serie != null)
				series.add(serie);
		}// for
		
		// Ajouter les séries non spécifiées dans le memento
		for (Serie serie : seriesById.values())
			series.add(serie);
		
		// Afficher ou masquer les séries
		hidden.clear();
		for (Serie serie : series) {
			if (memento.isHidden(serie.id))
				hidden.add(serie);
		}// for
	}// setMemento
	
	/** Renvoie l'observable des données du diagramme. */
	DiagramModelObservable getObservable() {
		return observable;
	}// getObservable
	
	@Override
	public Iterator<Serie> iterator() {
		return new SeriesIterator();
	}// iterator

	/** Un itérateur qui traverse les séries en ne renvoyant que celles qui sont
	 * visibles (non masquées).
	 *
	 * @author Olivier HAAS
	 */
	private class SeriesIterator implements Iterator<Serie> {

		/** Un itérateur de la collection englobante. */
		private final Iterator<Serie> it = series.iterator();

		/** La prochaine série à renvoyer. */
		private Serie next;

		@Override
		public boolean hasNext() {
			while (next == null && it.hasNext()) {
				Serie nextSerie = it.next();

				// N'accepter que les séries non masquées
				if (!isHidden(nextSerie))
					next = nextSerie;
			}// while

			return next != null;
		}// hasNext

		@Override
		public Serie next() {
			if (hasNext()) {
				Serie result = next;
				next = null;				// Oublier cette série pour la suite
				return result;
			}// if
			throw new NoSuchElementException();
		}// next

		/** Non implémenté.
		 * 
		 * @throws UnsupportedOperationException
		 * 			Dans tous les cas.
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}// remove
	}// private inner class SeriesIterator
}
