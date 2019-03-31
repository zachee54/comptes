/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;


/** Un observateur de modèle de diagramme.
 * 
 * @author Olivier HAAS
 */
interface DiagramModelObserver {

	/** Méthode appelée lorsque les données du modèle ont été modifiées. */
	void diagramChanged();
}
