package haas.olivier.comptes.gui2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import haas.olivier.comptes.Comptes;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.MessagesFactory;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.gui.table.EcrituresTableModel;
import haas.olivier.gui.IconLoader;
import haas.olivier.gui.PropertiesManager;
import haas.olivier.gui.SkeletonGUI;
import haas.olivier.info.HandledException;

/** L'interface graphique de l'application Comptes.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class ComptesGui extends SkeletonGUI<DossierComptes> {
	
	/** Bouton de sauvegarde */
	private JButton saveButton;
	
	/** Case d'information. */
	private JLabel infoLabel;
	
	/** Nom du DAO. */
	private JLabel daoLabel;
	
	/** Affichage du nom du dossier. */
	private JLabel sourceLabel;
	
	/** Construit une interface graphique de l'application. */
	public ComptesGui() {
		
		// Icône de la fenêtre
		setIconImage(IconLoader.createImageIcon(
				Comptes.class, "gui/images/sx10707.png", null).getImage());
		
		Container contentPane = getContentPane();
		
		// Barre d'état
		Component statusBar = createStatusBar();
		contentPane.add(statusBar, BorderLayout.SOUTH);
		
		// Barre d'outils
		JToolBar toolBar = createToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);
		
	}// constructeur
	
	/** Crée la barre d'état avec son contenu. */
	private Component createStatusBar() {
		JPanel statusBar = new JPanel(new BorderLayout());	// Panel
		infoLabel	= new JLabel();							// Informations
		daoLabel	= new JLabel();							// Nom du DAO
		sourceLabel	= new JLabel();							// Nom de la source
		
		// Effets 3D
		infoLabel.setBorder(BorderFactory.createLoweredBevelBorder());
		daoLabel.setBorder(BorderFactory.createLoweredBevelBorder());
		sourceLabel.setBorder(BorderFactory.createLoweredBevelBorder());
		
		// Polices normales (sans gras)
		Font policeNormale = infoLabel.getFont().deriveFont(Font.PLAIN);
		infoLabel.setFont(policeNormale);
		daoLabel.setFont(policeNormale);
		sourceLabel.setFont(policeNormale);
		
		// Valeurs initiales
		updateDaoName();
		
		// Partie droite de la barre
		JPanel right = new JPanel();								// Un panel
		right.setLayout(new BoxLayout(right, BoxLayout.LINE_AXIS));	// Layout
		right.add(daoLabel);										// Source
		right.add(sourceLabel);										// DAO
		
		// Ajouter à la barre d'état
		statusBar.add(infoLabel);						// Case d'informations
		statusBar.add(right, BorderLayout.EAST);		// Partie droite

		return statusBar;
	}// createStatusBar
	
	/** Crée la barre d'outils et son contenu. */
	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setBorderPainted(false);
		toolBar.setRollover(true);// TODO rollover utile ?
		
		// Le bouton de sauvegarde
		saveButton = new JButton(actionSave);
		saveButton.setHideActionText(true);					// Pas de texte
		toolBar.add(saveButton);
		
		toolBar.addSeparator();								// Séparateur
		
		// Le bouton de tri
		final JToggleButton triButton = new JToggleButton(
				IconLoader.createImageIcon(
						Comptes.class, "gui/images/sc_dbqueryedit.png",
						"Tri par pointages"));
		triButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				EcrituresTableModel.tri = triButton.isSelected()
						? EcrituresTableModel.TRI_POINTAGE	// Par pointages
						: EcrituresTableModel.TRI_DATE;		// Par dates
				getDossierActif().stateChanged(null);		// Mettre à jour
			}// actionPerformed
			
		});// classe anonyme ActionListener tri par pointages
		toolBar.add(triButton);
		
		toolBar.addSeparator();								// Séparateur
		
		// Le bouton des permanents
		JButton permanentsButton = new JButton(
				IconLoader.createImageIcon(
						Comptes.class, "gui/images/8-innovation_icone.png",
						"Générer les écritures permanentes"));
		permanentsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				// Un tableau de 12 mois, jusqu'à M+1
				Month[] months = new Month[12];			// 12 mois (convention)
				Month month = new Month().getNext();	// M+1
				for (int i=0; i<months.length; i++) {
					months[i] = month.getTranslated(-i);// ième mois avant
				}

				Month moisChoisi = (Month) JOptionPane.showInputDialog(
						ComptesGui.this,				// Frame principal
						"Choisissez le mois au titre duquel\ngénérer les " +
						"écritures permanentes",		// Texte du message
						"Ecritures permanentes",		// Titre de la fenêtre
						JOptionPane.INFORMATION_MESSAGE,// Type de message
						null,							// Pas d'icône
						months,							// Les mois
						month);							// Choix par défaut

				if (moisChoisi != null) {	// Si l'utilisateur n'a pas annulé
					Permanent.createAllEcritures(		// Générer les écritures
							moisChoisi);
					
					// Mettre à jour le DAO depuis le mois choisi -1
					try {
						Ecriture.update(moisChoisi.getPrevious());
					} catch (IOException e1) {
						MessagesFactory.getInstance().showErrorMessage(
								"Impossible de mettre à jour les données.");
						e1.printStackTrace();
					}// try
					
					// Mettre à jour l'affichage
					getDossierActif().stateChanged(null);
				}// if mois choisi
			}// actionPerformed
			
		});// classe anonyme ActionListener permanents
		toolBar.add(permanentsButton);
		
		// Le bouton pour effacer une écriture
		JButton deleteButton = new JButton(
				IconLoader.createImageIcon(
						Comptes.class, "gui/images/exerror.png",
						"Effacer cette écriture"));
		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getDossierActif().deleteCurrentEcriture();
			}// actionPerformed
			
		});// classe anonyme ActionListener delete
		toolBar.add(deleteButton);
		
		return toolBar;
	}// createToolBar

	/** Met à jour l'affichage du nom du DAO et de la source de données. */
	public void updateDaoName() {
		DossierComptes dossier = getDossierActif();
		
		if (dossier == null) {
			daoLabel.setText("");
			sourceLabel.setText("");
//			sourceLabel.setToolTipText("");
			
		} else {
			
			// Nom du DAO
			daoLabel.setText(" " + dossier.getDaoName() + " ");

			// Nom de la source (simple)
			sourceLabel.setText(" " + dossier.getDaoName() + " ");

			// Nom complet de la source
//			sourceLabel.setToolTipText(DAOFactory.getFactory().getSourceFullName());
		}
	}// updateDaoName
	
	@Override
	protected PropertiesManager getPropertiesManager() {
		return new PropertiesManager(
				new File("defaultconfig"),
				new File("prefs"),
				"Configuration Comptes");
	}// getPropertiesManager

	@Override
	protected String getApplicationName() {
		return "Comptes";
	}

	@Override
	protected DossierComptes getNewDossier() throws HandledException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DossierComptes getNewDossier(File file) throws HandledException {
		// TODO Auto-generated method stub
		return null;
	}

}
