package haas.olivier.comptes.gui.actions;

import haas.olivier.util.Observable;

/**
 * Un observable de changements de données dans le modèle de l'application. Bien
 * que les changements de données impactent toute l'application, chaque instance
 * a ses propres observateurs. Cela permet de ne mettre à jour que les
 * observateurs dont on a besoin. En contrepartie, il faut penser à mettre à
 * jour les autres avant de s'en servir.
 * 
 * @author Olivier HAAS
 */
public class DataObservable extends Observable<DataObserver> {

	public void notifyObservers() {
		for (DataObserver observer : observers) {
			observer.dataModified();
		}
	}
}
