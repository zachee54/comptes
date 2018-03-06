package haas.olivier.diagram;

import java.util.List;
import java.util.Set;

/** Un memento des diagrammes, qui permet d'encapsuler les identifiants des
 * séries, leur ordre, et le fait qu'elles soient masquées ou visibles.
 * <p>
 * Il s'agit d'un objet immuable.
 *
 * @author Olivier Haas
 */
public class DiagramMemento {

	/** Le nom du diagramme. */
	private final String name;
	
	/** Les identifiants uniques des séries, dans l'ordre des séries.
	 */
	private final List<Integer> series;
	
	/** Les identifiants des séries masquées, parmi celles comprises dans
	 * {@link #series}.
	 */
	private final Set<Integer> hidden;
	
	/** Construit un memento de diagramme.
	 * 
	 * @param name		Le nom du diagramme.
	 * 
	 * @param series	Les identifiants uniques des séries, dans l'ordre des
	 * 					séries. 
	 * 
	 * @param hidden	Les identifiants des séries masquées, parmi celles
	 * 					comprises dans <code>series</code>.
	 */
	public DiagramMemento(String name, List<Integer> series,
			Set<Integer> hidden) {
		this.name = name;
		this.series = series;
		this.hidden = hidden;
	}// constructeur
	
	/** Renvoie le nom du diagramme. */
	public String getName() {
		return name;
	}// getName
	
	/** Renvoie la liste des identifiants des séries, dans l'ordre.
	 * 
	 * @return	La liste des identifiants. Les modifications sur cette liste
	 * 			sont répercutées directement dans le memento.
	 */
	public List<Integer> getSeries() {
		return series;
	}// getSeries
	
	/** Indique si une série est masquée.
	 * 
	 * @param id	L'identifiant de la série recherchée.
	 * 
	 * @return		<code>true</code> si la série est connue et masquée,
	 * 				<code>false</code> sinon.
	 */
	public boolean isHidden(int id) {
		return hidden.contains(id);
	}// isHidden
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DiagramMemento))
			return false;
		
		DiagramMemento memento = (DiagramMemento) o;
		return series.equals(memento.series) && hidden.equals(memento.hidden);
	}// equals
}
