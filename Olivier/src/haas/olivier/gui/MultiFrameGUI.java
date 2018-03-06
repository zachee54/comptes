/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui;

import haas.olivier.gui.Dossier;
import haas.olivier.gui.SkeletonGUI;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/** Une interface graphique de base utilisant des cadres internes pour afficher
 * les dossiers ouverts.
 * Les dossiers ajoutés à cette interface graphique doivent utiliser un
 * <code>JInternalFrame</code> comme composant graphique renvoyé par la méthode
 * <code>getComponent()</code>.
 *
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public abstract class MultiFrameGUI<D extends Dossier> extends SkeletonGUI<D>
implements InternalFrameListener {

	/** Le bureau multi-fenêtres. */
	protected JDesktopPane bureau = new JDesktopPane();
	
	/** La carte des cadres internes, associés à leur dossier. */
	protected Map<D,JInternalFrame> frames = new HashMap<D,JInternalFrame>();
	
	/** Nombre de dossiers déjà ouverts pour déterminer la meilleure position
	 * d'affichage en cascade pour la prochaine fenêtre */
	private int cascade = 0;
	
	/** Construit une interface graphique gérant des cadres.
	 * 
	 * @param prefs	Les préférences utilisateur.
	 */
	public MultiFrameGUI(Preferences prefs) {
		super(prefs);
		
		// Couleur de fond du bureau
		bureau.setBackground(Color.LIGHT_GRAY);
		
		// Ajouter un bureau dans la fenêtre
		add(bureau);
	}// constructeur
	
	/** Cherche le dossier correspondant au cadre spécifié.
	 * 
	 * @param frame	Le cadre dont on veut chercher le dossier.
	 * @return		Le dossier correspondant au cadre, ou <code>null</code> si
	 * 				aucun dossier ne correspond.
	 */
	private D getDossierFromFrame(JInternalFrame frame) {
		// Parcourir les dossiers
		for (Entry<D,JInternalFrame> entry : frames.entrySet()) {
			// Si c'est ce cadre
			if (entry.getValue() == frame)
				return entry.getKey();
		}// for entry
		return null;
	}// getDossierFromFrame
	
	/** Sélectionne le cadre correspondant au dossier spécifié.
	 * @param dossier	Le dossier à sélectionner.
	 */
	@Override
	protected void selectDossier(D dossier) {
		bureau.setSelectedFrame(frames.get(dossier));
	}
	
	/** Renvoie le dossier correspondant au cadre sélectionné. */
	@Override
	public D getDossierActif() {
		return getDossierFromFrame(bureau.getSelectedFrame());
	}
	
	@Override
	protected void addDossier(D dossier) {
		JInternalFrame inFrame =			// Cadre graphique du dossier
				(JInternalFrame) dossier.getComponent();
		inFrame.setDefaultCloseOperation(	// Ne pas gérer les evts de ferm.
				JInternalFrame.DO_NOTHING_ON_CLOSE);
		inFrame.addInternalFrameListener(	// Intercepter les evts de fermeture
				this);
		frames.put(dossier, inFrame);		// Ajouter à la liste des dossiers
		bureau.add(inFrame);				// Ajouter graphiquement
		inFrame.setTitle(dossier.getName());// Titre du cadre interne
		
		// Positionner le nouveau cadre dans la fenêtre
		inFrame.pack();									// Préparer le cadre
		int offset = 30;								// Décalage de position
		if (!bureau.contains(							// Si bureau trop petit
				offset*cascade + inFrame.getWidth(),
				offset*cascade + inFrame.getHeight())) {
			cascade = 0;								// Recommencer au coin
		}
		inFrame.setLocation(							// Position en cascade
				offset*cascade, offset*cascade++);
		inFrame.setVisible(true);						// Rendre visible
		
		// Terminer par l'ajout du dossier selon la classe mère
		super.addDossier(dossier);
	}// addDossier
	
	@Override
	protected void closeDossier(D dossier) {
		JInternalFrame inFrame =
				(JInternalFrame) dossier.getComponent();// Le cadre du dossier
		inFrame.dispose();								// Fermer le cadre
		bureau.remove(inFrame);							// Supprimer du bureau
		super.closeDossier(dossier);					// Fermer le dossier
	}// closeDossier
	
	// Interface InternalFrameListener
	
	/** Ferme le dossier. */
	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		closeDossierSafe(getDossierFromFrame((JInternalFrame) e.getSource()));
	}// internalFrameClosing
	
	@Override public void internalFrameOpened(InternalFrameEvent e) {}
	@Override public void internalFrameClosed(InternalFrameEvent e) {}
	@Override public void internalFrameIconified(InternalFrameEvent e) {}
	@Override public void internalFrameDeiconified(InternalFrameEvent e) {}
	@Override public void internalFrameActivated(InternalFrameEvent e) {}
	@Override public void internalFrameDeactivated(InternalFrameEvent e) {}
}
