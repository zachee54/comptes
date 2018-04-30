package haas.olivier.comptes.gui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Une implémentation générique d'<code>Action</code> pour permettre
 * d'encapsuler un <code>ActionListener</code> ou pour être déclenchée aussi à
 * la fermeture d'une fenêtre.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
class UniversalAction extends AbstractAction implements WindowListener {

	/**
	 * L'objet à notifier lorsque l'action est déclenchée.
	 */
	private transient ActionListener target;
	
	/**
	 * Construit une action générique.
	 * 
	 * @param target	L'objet à notifier lorsque l'action est déclenchée.
	 */
	UniversalAction(ActionListener target) {
		this(target, null);
	}
	
	/**
	 * Construit une action générique.
	 * 
	 * @param target	L'objet à notifier lorsque l'action est déclenchée.
	 * @param command	La commande de l'action.
	 */
	public UniversalAction(ActionListener target, String command) {
		this.target = target;
		putValue(Action.ACTION_COMMAND_KEY, command);
	}
	
	/**
	 * Réalise l'action sur la cible.
	 */
	private void perform(EventObject e) {
		target.actionPerformed(new ActionEvent(
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
		/* Rien à faire */
	}

	@Override
	public void windowClosing(WindowEvent e) {
		perform(e);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		/* Rien à faire */
	}

	@Override
	public void windowIconified(WindowEvent e) {
		/* Rien à faire */
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		/* Rien à faire */
	}

	@Override
	public void windowActivated(WindowEvent e) {
		/* Rien à faire */
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		/* Rien à faire */
	}
}
