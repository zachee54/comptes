/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Icon;

/** Une implémentation concrète et multi-usages de l'interface
 * <code>Action</code>.<br>
 * Elle a surtout l'intérêt de pouvoir utiliser des <code>EventHandler</code>
 * pour les instructions très simples, ce qui évite d'avoir à dériver
 * <code>AbstractAction</code> avec des classes anonymes.
 *
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class SimpleAction extends AbstractAction {

	private final ActionListener listener;
	
	/** Construit une action simple avec des valeurs par défaut pour le texte et
	 * l'icône.
	 * 
	 * @param listener	L'objet à notifier lorsque l'action de produit.
	 */
	public SimpleAction(ActionListener listener) {
		this.listener = listener;
	}// constructeur simple
	
	/** Construit une action simple avec une icône par défaut.
	 * 
	 * @param text		Le texte de l'action.
	 * @param listener	L'objet à notifier lorsque l'action de produit.
	 */
	public SimpleAction(String text, ActionListener listener) {
		super(text);
		this.listener = listener;
	}// constructeur texte

	/** Construit une action simple.
	 * 
	 * @param text		Le texte de l'action.
	 * @param icon		L'icône de l'action.
	 * @param listener	L'objet à notifier lorsque l'action de produit.
	 */
	public SimpleAction(String text, Icon icon, ActionListener listener) {
		super(text, icon);
		this.listener = listener;
	}// constructeur texte et icône
	
	/** Fait suivre la notification au listener défini à l'instanciation. */
	@Override
	public void actionPerformed(ActionEvent e) {
		listener.actionPerformed(e);
	}// actionPerformed

}
