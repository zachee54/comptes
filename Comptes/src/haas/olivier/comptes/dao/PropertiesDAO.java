package haas.olivier.comptes.dao;

import haas.olivier.diagram.DiagramMemento;

/** L'objet d'accès aux propriétés sauvegardée dans la source de données.
 * 
 * @author Olivier Haas
 */
public interface PropertiesDAO {

	/** Renvoie les propriétés d'un diagramme.
	 * 
	 * @param name	Le nom du diagramme souhaité.
	 * 
	 * @return		Un memento des propriétés précédemment enregistrées pour ce
	 * 				diagramme. Si rien n'avait été enregistré, le memento est
	 * 				une instance sans autre données que le nom du diagramme.
	 */
	DiagramMemento getDiagramProperties(String name);
	
	/** Met à jour les propriétés d'un diagramme.
	 * 
	 * @param name		Le nom du diagramme à mettre à jour.
	 * @param memento	Les nouvelles propriétés du diagramme.
	 */
	void setDiagramProperties(String name, DiagramMemento memento);
	
	/** Renvoie les noms de tous les diagrammes connnus. */
	Iterable<String> getDiagramNames();
}
