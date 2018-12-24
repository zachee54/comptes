/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import haas.olivier.util.Observable;

/** Un observable de changements de données dans un modèle de diagramme.
 * 
 * @author Olivier Haas
 */
class DiagramModelObservable extends Observable<DiagramModelObserver> {

	/** Notifie un changement de données aux observateurs. */
	public void dataChanged() {
		for (DiagramModelObserver o : observers)
			o.diagramChanged();
	}// dataChanged
}
