package haas.olivier.comptes.gui.actions;

import java.io.IOException;

/**
 * Un observateur sur un changement de soldes (historiques, soldes à vue,
 * moyennes...).
 *
 * @author Olivier HAAS
 */
public interface SoldesObserver {
	void soldesChanged() throws IOException;
}
