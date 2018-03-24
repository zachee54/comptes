package haas.olivier.comptes.gui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Une <code>Action</code> qui puisse être déclenchée aussi à la fermeture d'une
 * fenêtre.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
class UniversalAction extends AbstractAction implements WindowListener {

	private ActionListener cible;
	
	public UniversalAction(ActionListener cible, String command) {
		this.cible = cible;								// La cible
		putValue(Action.ACTION_COMMAND_KEY, command);	// La commande
	}
	
	/**
	 * Réalise l'action sur la cible.
	 */
	private void perform(EventObject e) {
		cible.actionPerformed(new ActionEvent(
				e.getSource(),
				ActionEvent.ACTION_PERFORMED,
				(String) getValue(Action.ACTION_COMMAND_KEY)));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		perform(e);
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		perform(e);
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}
