package haas.olivier.comptes.gui.actions;

/** Observer sur un changement de soldes (historiques, soldes à vue,
 * moyennes...).
 *
 * @author Olivier HAAS
 */
public interface SoldesObserver {
	void soldesChanged();
}
