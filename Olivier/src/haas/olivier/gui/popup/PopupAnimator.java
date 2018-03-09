/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.popup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Un animateur donnant une impression de mouvement pour afficher un masquer un
 * popup.
 *
 * @author Olivier HAAS
 */
public class PopupAnimator implements ActionListener {

	/**
	 * Le pas du coefficient d'animation.
	 * <p>
	 * Le coefficient peut varier entre 0 et 1. Il est mis à jour 100 fois par
	 * seconde.<br>
	 * Par conséquent, un pas de <code>x</code> rend une animation de
	 * <code>1/100*x</code> secondes, soit 1 seconde pour 0,01 ; un quart de
	 * seconde pour 0,04 ; 1/10<sup>ème</sup> de seconde pour 0,1 ; etc.
	 */
	private static final double STEP = 0.04;
	
	/**
	 * L'événement de changement. Il est unique dans toute l'instance.
	 */
	private final ChangeEvent changeEvent = new ChangeEvent(this);
	
	/**
	 * L'objet auquel signaler les changements de ratio d'affichage.
	 */
	private final ChangeListener listener;
	
	/**
	 * Le planificateur de tâche pour modifier
	 * {@link #ratio le ratio d'affichage}.
	 */
	private Timer timer;

	/**
	 * Le coefficient d'affichage du popup.<ul>
	 * <li>	S'il est égal à <code>0.0</code>, le popup n'est pas affiché et seul
	 * 		l'onglet est visible.
	 * <li>	S'il est égal à <code>1.0</code>, le popup est entièrement visible.
	 * <li>	Sinon, le popup est affiché à proportion du coefficient.
	 * </ul>
	 */
	private double ratio;
	
	/**
	 * Un drapeau indiquant que le popup est soit visible ou en train
	 * d'apparaître (<code>true</code>), soit invisible ou en train de
	 * disparaître (<code>false</code>).
	 */
	private boolean isShowing = false;
	
	/**
	 * Construit un animateur de popup.
	 * 
	 * @param listener	L'objet auquel signaler les changements de ratio
	 * 					d'affichage.
	 */
	public PopupAnimator(ChangeListener listener) {
		this.listener = listener;
	}

	/**
	 * Démarre une animation pour afficher le popup.
	 */
	public void showPopup() {
		startAnimation(true);
	}
	
	/**
	 * Démarre une animation pour masquer le popup.
	 */
	public void hidePopup() {
		startAnimation(false);
	}
	
	/**
	 * Démarre une animation pour afficher ou masquer le popup.<br>
	 * Si la dernière animation était dans le même sens, la méthode ne fait
	 * rien.
	 * <p>
	 * Au départ, le popup est masqué par défaut.
	 * 
	 * @param show	<code>true</code> pour une animation qui affiche le popup,
	 * 				<code>false</code> pour une animation qui le masque.
	 */
	private void startAnimation(boolean show) {
		if (isShowing != show) {
			isShowing = show;
			startTimer();
		}
	}
	
	/**
	 * Démarre un nouveau planificateur.
	 */
	private void startTimer() {
		if (timer != null)
			timer.stop();
		timer = new Timer(10, this);
		timer.start();
	}

	/**
	 * Modifie le ratio d'affichage du popup, en l'augmentant si
	 * <code>isShowing == true</code>, ou en le diminuant sinon.
	 * <p>
	 * Le pas de modification est de <code>0.04</code>, ce qui permet
	 * d'atteindre la valeur <code>0.0</code> ou <code>1.0</code> en 25 appels à
	 * la méthode.
	 * <p>
	 * Si le ratio limite est atteint, le {@link #timer} est arrêté.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (isShowing) {
			ratio += STEP;
			if (ratio >= 1.0) {
				ratio = 1.0;
				timer.stop();
			}
			
		} else {
			ratio -= STEP;
			if (ratio <= 0.0) {
				ratio = 0.0;
				timer.stop();
			}
		}
		
		listener.stateChanged(changeEvent);
	}

	/**
	 * Renvoie le ratio d'affichage du popup.
	 * 
	 * @return	Un nombre entre <code>0.0</code> (popup masqué) et
	 * 			<code>1.0</code> (popup entièrement affiché).
	 */
	public double getRatio() {
		return isShowing
				? Math.sqrt(1-(1-ratio)*(1-ratio))
				: 1-Math.sqrt(1-ratio*ratio);
	}
}
