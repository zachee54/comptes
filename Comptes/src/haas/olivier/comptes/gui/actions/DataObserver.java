package haas.olivier.comptes.gui.actions;

/**
 * Observer sur un changement de données "en dur". Cette interface concerne les
 * changements de données "durs" tels que l'ajout, la modification ou la
 * suppression d'écritures, contrairement aux TableModelListener qui réagissent
 * même en cas de simples changements d'affichage (par exemple si l'utilisateur
 * affiche une autre période, le TableModel change, mais les écritures du DAO
 * n'ont pas changé).
 * 
 * @author Olivier HAAS
 */
public interface DataObserver {
	
	/** Indique un changement de données. */
	void dataModified();
}
