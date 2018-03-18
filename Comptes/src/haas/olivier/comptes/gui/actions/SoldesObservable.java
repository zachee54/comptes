package haas.olivier.comptes.gui.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import haas.olivier.util.Observable;

/** Un Observable de changements de soldes (historiques, soldes à vue,
 * moyennes...).
 * <p>
 * En plus de permettre la mise à jour des composants affichant des soldes, cet
 * Observable commence par recalculer les soldes en arrière-plan à partir de la
 * date de modification.
 * <p>
 * Bien que les changements de soldes impactent toute l'application, chaque
 * instance a ses propres observateurs. Cela permet de ne mettre à jour que les
 * observateurs dont on a besoin. En contrepartie, il faut penser à mettre à
 * jour les autres avant de s'en servir.
 *
 * @author Olivier HAAS
 */
public class SoldesObservable extends Observable<SoldesObserver>
implements DataObserver {
	
	/** Construit un SoldesObservable appuyé obligatoirement sur un
	 * DataObservable: quand les données changent, il faut recalculer les
	 * soldes.
	 */
	public SoldesObservable(DataObservable dataObservable) {
		dataObservable.addObserver(this);
	}
	
	@Override
	public void dataModified() {
		notifyObservers();
	}// dataModified
	
	/** Met à jour les soldes et prévient ensuite les Observers. */
	public void notifyObservers() {
		try {
			for (SoldesObserver observer : observers)
				observer.soldesChanged();
		} catch (IOException e) {
			Logger.getLogger(getClass().getName()).log(
					Level.SEVERE,
					"Erreur pendant la détermination des soldes à afficher",
					e);
		}
	}// notifyObservers
}
