/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.cache;

import java.util.Map;

import haas.olivier.diagram.DiagramMemento;

/** Une interface pour l'accès aux propriétés des {@link CacheableDAOFactory}.
 * 
 * @author Olivier Haas
 */
public interface CacheablePropertiesDAO {

	/** Renvoie les mementos des propriétés de chaque diagramme, classés selon
	 * les noms des diagrammes.
	 */
	Map<String, DiagramMemento> getDiagramProperties();
}
