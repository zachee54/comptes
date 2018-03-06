package haas.olivier.diagram;

/** Une interface pour les modèles de diagrammes.
 * 
 * @author Olivier HAAS
 */
public interface DiagramModel {

	/** Ajoute une série. */
	void add(Serie serie);
	
	/** Retire une série.
	 * <p>
	 * Si la série a été ajoutée plusieurs fois au modèle (ce qui ne devrait
	 * logiquement jamais arriver), le comportement est indéfini.
	 * 
	 * @param serie	La série à enlever.
	 */
	void remove(Serie serie);
	
	/** Renvoie les séries. */
	Iterable<Serie> getSeries();
	
	/** Renvoie une vue du modèle qui cumule les séries entre elles pendant leur
	 * itération.
	 * 
	 * @return	Une vue du modèle actuel qui cumule les séries, ou
	 * 			<code>this</code> si l'objet actuel est déjà une vue cumulée.
	 */
	DiagramModel getAggregateView();
	
	/** Définit les valeurs à retenir en abscisses.
	 * 
	 * @param xValues	Les valeurs en abscisses.
	 */
	void setXValues(Object[] xValues);
	
	/** Renvoie les valeurs en abscisses. */
	Object[] getXValues();
	
	/** Renvoie l'ordonnateur de toutes les séries, y compris les séries
	 * masquées.
	 */
	SeriesOrdener getOrdener();
	
	/** Renvoie l'observable du modèle. */
	DiagramModelObservable getObservable();
}
