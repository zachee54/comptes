/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Comptes;
import haas.olivier.util.Month;
import haas.olivier.comptes.Permanent;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.gui.actions.DataObserver;
import haas.olivier.comptes.gui.diagram.ComptesDiagramFactory;
import haas.olivier.comptes.gui.diagram.DiagramFrame;
import haas.olivier.comptes.gui.settings.SetupCompte;
import haas.olivier.comptes.gui.settings.SetupDAO;
import haas.olivier.comptes.gui.settings.SetupPermanent;
import haas.olivier.comptes.gui.table.EcrituresTableModel;
import haas.olivier.gui.IconLoader;
import haas.olivier.gui.PropertiesController;
import haas.olivier.info.DialogHandler;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Interface graphique classique pour la gestion des comptes.
 * 
 * @author Olivier HAAS
 */
public class SimpleGUI implements WindowListener, ChangeListener, DataObserver,
ActionListener, PropertiesController {
	
	/**
	 * Le Logger de cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(SimpleGUI.class.getName());
	
	/**
	 * Nom de la clé déterminant la position à l'écran (abscisse).
	 */
	private static final String POS_X = "pos_X";
	
	/**
	 * Nom de la clé déterminant la position à l'écarn (ordonnée).
	 */
	private static final String POS_Y = "pos_Y";
	
	/**
	 * Nom de la clé déterminant la largeur de la fenêtre.
	 */
	private static final String WIDTH = "width";
	
	/**
	 * Nom de la clé déterminant la hauteur de la fenêtre.
	 */
	private static final String HEIGHT = "height";

	/**
	 * Constante représentant l'action de paramétrage des opérations
	 * permanentes.
	 */
	private static final String PERMANENTS  = "permanents";
	
	/**
	 * Constante représentant l'action de suppression d'une écriture.
	 */
	private static final String DELETE = "delete";
	
	/**
	 * Constante représentant l'action de tri par pointages.
	 */
	private static final String POINTAGES = "pointages";
	
	/**
	 * Préférences utilisateur.
	 */
	private final Preferences prefs;
	
	/**
	 * La fenêtre.
	 */
	private JFrame frame;
	
	/**
	 * Les onglets.
	 */
	private JTabbedPane tabs;
	
	/**
	 * Bouton de sauvegarde.
	 */
	private JButton saveButton;
	
	/**
	 * Bouton d'effacement.
	 */
	private JButton deleteButton;
	
	/**
	 * Bouton de tri par pointages.
	 */
	private JToggleButton triButton;
	
	/**
	 * Case d'information.
	 */
	private JLabel infoLabel;
	
	/**
	 * Étiquette contenant le nom du DAO.
	 */
	private JLabel daoLabel;
	
	/**
	 * Étiquette contenant le nom de fichier.
	 */
	private JLabel sourceLabel;
	
	/**
	 * Action Enregistrer.
	 */
	private Action actionSave;
	
	/**
	 * Action diagramme moyennes.
	 */
	private Action diagMoyAction;
	
	/**
	 * Action diagramme patrimoine.
	 */
	private Action diagPatrimAction;

	@SuppressWarnings("serial")
	public SimpleGUI(Preferences prefs) {
		this.prefs = prefs;
		
		// Action Enregistrer
		actionSave = new AbstractAction("Enregistrer",
				IconLoader.loadIcon("/images/sc05502.png")) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					DAOFactory.getFactory().save();	// Sauvegarder
					dataModified();					// Actualiser l'icône
					
				} catch (IOException e1) {
					LOGGER.log(Level.SEVERE, "Échec de l'enregistrement", e1);
				}
			}
			
		};// classe anonyme AbstractAction
		
		// Active l'action si les données ont besoin d'être sauvegardées
		actionSave.setEnabled(DAOFactory.getFactory().mustBeSaved());
		
		// Action affichant le diagramme des moyennes glissantes
		diagMoyAction = new AbstractAction("Moyennes glissantes",
				IconLoader.loadIcon("/images/typepointline_16.png")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new DiagramFrame(ComptesDiagramFactory.newMoyenne());
					
				} catch (IOException e1) {
					LOGGER.log(Level.SEVERE,
							"Impossible de créer le diagramme des moyennes",
							e1);
				}
			}
			
		};// classe anonyme AbstractAction
		
		// Action affichant le diagramme de l'évolution du patrimoine
		diagPatrimAction = new AbstractAction("Évolution du patrimoine",
				IconLoader.loadIcon("/images/typearea_16.png")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new DiagramFrame(ComptesDiagramFactory.newPatrimoine());
					
				} catch (IOException e1) {
					LOGGER.log(Level.SEVERE,
							"Impossible de créer le diagramme d'évolution "
							+ "du patrimoine", e1);
				}
			}

		};// classe anonyme AbstractAction
		
		Component statusBar = createStatusBar();	// Barre d'état
		JToolBar toolBar = createToolBar();			// Barre d'outils
		tabs = new JTabbedPane();					// Onglets
		// Créer les onglets
		createTabs();

		// Intercepter les changements d'onglet
		tabs.addChangeListener(this);

		// Cadre principal
		frame = new JFrame("Comptes");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);				// Événement de fermeture
		frame.setIconImage(							// Icône de la fenêtre
				IconLoader.loadImage("/images/sx10707.png"));
		
		// Insérer les éléments
		Container contentPane = frame.getContentPane();
		contentPane.add(tabs);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		frame.setJMenuBar(createMenuBar());
		showFrame();
		
		// Afficher les messages dans une boîte de dialogue modale du frame
		LogManager.getLogManager().reset();			// Supprimer l'existant
		Logger rootLogger = Logger.getLogger("");
		rootLogger.addHandler(new ConsoleHandler());// Afficher sur la console
		rootLogger.addHandler(						// et à l'écran depuis frame
				new DialogHandler(frame));
	}
	
	public void createTabs() {
		tabs.removeAll();	// Enlever les onglets actuels s'il y en a
		
		// Créer un onglet pour chaque type de compte
		tabs.addTab("Courant", new ComptePanel(
				this, new FilterCompte(TypeCompte.COMPTE_COURANT)));
		tabs.addTab("Carte", new ComptePanel(
				this, new FilterCompte(TypeCompte.COMPTE_CARTE)));
		tabs.addTab("Epargne", new ComptePanel(
				this, new FilterCompte(
						TypeCompte.COMPTE_EPARGNE, TypeCompte.SUIVI_EPARGNE)));
		tabs.addTab("Enfants", new ComptePanel(
				this, new FilterCompte(TypeCompte.ENFANTS)));
		tabs.addTab("Recettes", new ComptePanel(
				this, new FilterCompte(TypeCompte.RECETTES)));
		tabs.addTab("Dépenses", new ComptePanel(
				this, new FilterCompte(TypeCompte.DEPENSES)));
		tabs.addTab("Emprunt", new ComptePanel(
				this, new FilterCompte(TypeCompte.EMPRUNT)));
		tabs.add("Investissements", new ComptePanel(
				this, new FilterCompte(
						TypeCompte.DEPENSES_EN_EPARGNE,
						TypeCompte.RECETTES_EN_EPARGNE)));
		
		/*
		 * Pour les comptes clôturés, un filtre spécial qui n'accepte que les
		 * comptes clôturés.
		 */
		tabs.addTab("Clôturés", new ComptePanel(
				this, new FilterCompte() {
					
					@Override
					public boolean acceptsBancaires() {
						return true;				// Imiter comptes bancaires
					}
					
					@Override
					public boolean accepts(Compte c) {
						return c.getCloture() != null;	// Compte clôturé
					}
					
				}));// classe anonyme FilterCompte
		
		tabs.setSelectedIndex(0);	// Sélectionner le 1er onglet par défaut
		
		// Mettre à jour l'onglet initial (simuler un changement d'onglet)
		stateChanged(null);
	}
	
	@SuppressWarnings("serial")
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();			// Barre de menu
		
		// Menu Fichier
		JMenu fichier = new JMenu("Fichier");
		menuBar.add(fichier);
		
		// Ouvrir un fichier
		fichier.add(new AbstractAction("Ouvrir") {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SetupDAO(SimpleGUI.this, frame, SetupDAO.OPEN);
			}
		});// classe anonyme AbstractAction
		
		// Enregistrer
		fichier.add(actionSave);
		
		// Enregistrer sous...
		fichier.add(new AbstractAction("Enregistrer sous...") {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SetupDAO(SimpleGUI.this, frame, SetupDAO.SAVE);
			}
		});// classe anonyme AbstractAction
		
		// Menu diagrammes
		JMenu diagrams = new JMenu("Graphiques");
		menuBar.add(diagrams);
		diagrams.add(diagMoyAction);
		diagrams.add(diagPatrimAction);
		
		// Menu  Outils
		JMenu outils = new JMenu("Outils");
		menuBar.add(outils);
		
		// Gestion des comptes
		outils.add(new AbstractAction("Gestion des comptes...") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Lancer la gestion des comptes
				new SetupCompte(SimpleGUI.this, frame);
			}
		});// classe anonyme AbstractAction
		
		// Gestion des Permanents
		outils.add(new AbstractAction("Opérations permanentes...") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new SetupPermanent(frame, SimpleGUI.this);
				} catch (IOException e1) {
					LOGGER.log(
							Level.SEVERE,
							"Impossible d'afficher la fenêtre de paramétrage des opérations permanentes",
							e1);
				}
			}
		});// classe anonyme AbstractAction
		
		return menuBar;
	}
	
	/**
	 * Crée la barre d'outils.
	 */
	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setBorderPainted(false);
		toolBar.setRollover(true);
		
		// Le bouton de sauvegarde
		saveButton = new JButton(actionSave);
		saveButton.setHideActionText(true);					// Pas de texte
		toolBar.add(saveButton);
		
		toolBar.addSeparator();								// Séparateur
		
		// Le bouton de tri
		triButton = new JToggleButton(IconLoader.loadIcon(
				"/images/sc_dbqueryedit.png", "Tri par pointages"));
		triButton.setActionCommand(POINTAGES);
		triButton.addActionListener(this);
		toolBar.add(triButton);
		
		toolBar.addSeparator();								// Séparateur
		
		// Le bouton des permanents
		JButton permanentsButton = new JButton(IconLoader.loadIcon(
				"/images/8-innovation_icone.png",
				"Générer les écritures permanentes"));
		permanentsButton.setActionCommand(PERMANENTS);
		permanentsButton.addActionListener(this);
		toolBar.add(permanentsButton);
		
		// Le bouton pour effacer une écriture
		deleteButton = new JButton(IconLoader.loadIcon(
				"/images/exerror.png",
				"Effacer cette écriture"));
		deleteButton.setActionCommand(DELETE);
		deleteButton.addActionListener(this);
		toolBar.add(deleteButton);
		
		toolBar.addSeparator();								// Séparateur
		
		// Le diagramme des moyennes glissantes
		JButton buttonMoy = new JButton(diagMoyAction);
		buttonMoy.setHideActionText(true);
		toolBar.add(buttonMoy);
		
		// Le diagramme de l'évolution du patrimoine
		JButton buttonPatrim = new JButton(diagPatrimAction);
		buttonPatrim.setHideActionText(true);
		toolBar.add(buttonPatrim);
		
		return toolBar;
	}
	
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
	}
	
	/**
	 * Affiche la fenêtre principale en tenant compte des préférences de
	 * l'utilisateur.
	 */
	private void showFrame() {
		
		// Récupérer les propriétés de position (en texte)
		int posX = prefs.getInt(POS_X, -1);
		int posY = prefs.getInt(POS_Y, -1);
		int width = prefs.getInt(WIDTH, -1);
		int height = prefs.getInt(HEIGHT, -1);
		
		// Si l'une des propriétés n'existe pas
		if (posX == -1 || posY == -1 || width == -1 ||  height == -1) {
			frame.setLocationByPlatform(true);		// Position par défaut
			frame.setPreferredSize(
					new Dimension(900,600));		// Forcer les dimensions
		
		} else {									// Utiliser les préférences
			// Cadrer les dimensions dans l'écran
			Dimension ecran = Toolkit.getDefaultToolkit().getScreenSize();
			if (width > ecran.width) {				// Largeur maxi
				width = ecran.width;
			}
			if (height > ecran.height) {			// Hauteur maxi
				height = ecran.height;
			}
			frame.setPreferredSize(
					new Dimension(width, height));	// Dimensions à utiliser
			
			// Cadrer la position
			if (posX < 0) {							// Abscisse mini
				posX = 0;
			}
			if (posY < 0) {							// Ordonnée mini
				posY = 0;
			}
			if (posX + width > ecran.width) {		// Abscisse maxi
				posX = ecran.width - width;
			}
			if (posY + height > ecran.height) {		// Ordonnée maxi
				posY = ecran.height - height;
			}
			frame.setLocation(posX, posY);			// Position à utiliser
		}
		frame.pack();
		frame.setVisible(true);						// Afficher la fenêtre
		
//		//S'enregistrer comme contrôleur de propriétés pour pouvoir les modifier
//		propManager.addController(this);
	}

	/**
	 * Met à jour l'affichage du nom du DAO et de la source de données, et les
	 * enregistre dans les préférences utilisateurs.
	 */
	public void updateDaoName() {
		DAOFactory factory = DAOFactory.getFactory();
		
		// Nom du DAO
		daoLabel.setText(" " + factory.getName() + " ");
		
		// Nom de la source (simple)
		sourceLabel.setText(" " + factory.getSource() + " ");
		
		// Nom complet de la source
		String fullName = factory.getSourceFullName();
		sourceLabel.setToolTipText(fullName);
		
//		// Mémoriser le type de DAO
//		String daoName = factory.getName();
//		if ("Cache Serialize".equals(daoName)) {
//			prefs.put(Comptes.DAO_NAME_PROPERTY, Comptes.DAO_NAME_SERIAL);
//		} else if ("CSV".equals(daoName)) {
//			prefs.put(Comptes.DAO_NAME_PROPERTY, Comptes.DAO_NAME_CSV);
//		} else {
//			MessagesFactory.getInstance().showErrorMessage(
//					"Type de DAO non reconnu : préférences non sauvegardées.");
//		}
		
		// Enregistrer dans les préférences
		if (fullName != null) {
			prefs.put(Comptes.SOURCE_NAME_PROPERTY,
					Paths.get(new File("").getAbsolutePath()).relativize(
							new File(factory.getSourceFullName()).toPath())
							.toString());
		}
	}
	
	/**
	 * Met à jour l'onglet sélectionné.
	 * <p>
	 * Cette méthode est appelée lorsque l'utilisateur change d'onglet.
	 * Elle utilise un fil d'exécution à part pour ne pas bloquer la fenêtre.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		new SwingWorker<Void,Void>() {
			public Void doInBackground() throws IOException {
				Component tab = tabs.getSelectedComponent();
				if (tab instanceof ComptePanel) {	// Bonne classe et non null 
					((ComptePanel) tab).update();
				}
				return null;
			}
		}.execute();
	}

	/**
	 * Met à jour l'interface à chaque changement de données.
	 * <p>
	 * En pratique, il s'agit d'activer ou désactiver la commande Enregistrer
	 * (commande du menu et bouton de la barre d'outils).
	 */
	@Override
	public void dataModified() {
		// Actualise le statut de l'action Enregistrer
		actionSave.setEnabled(DAOFactory.getFactory().mustBeSaved());
	}

	/**
	 * Reçoit les actions de l'utilisateur sur la partie générale de l'interface
	 * utilisateur.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == PERMANENTS) {	// Action permanents

			// Un tableau de mois, jusqu'à M+1
			Month[] months = new Month[12];			// 12 mois (par convention)
			Month month = Month.getInstance().getNext();// M+1
			for (int i=0; i<months.length; i++) {
				months[i] = month.getTranslated(-i);	// ième mois avant
			}

			Month moisChoisi = (Month) JOptionPane.showInputDialog(
					frame,							// Frame principal
					"Choisissez le mois au titre duquel\ngénérer les " +
					"écritures permanentes",		// Texte du message
					"Ecritures permanentes",		// Titre de la fenêtre
					JOptionPane.INFORMATION_MESSAGE,// Type de message
					null,							// Pas d'icône
					months,							// Les mois
					month);							// Choix par défaut

			// Si l'utilisateur n'a pas annulé
			if (moisChoisi != null) {
				
				// Générer les écritures
				Permanent.createAllEcritures(moisChoisi);
				
				// Mettre à jour l'affichage
				stateChanged(null);
			}// if mois choisi

		} else if (e.getActionCommand() == DELETE) {		// Action effacer
			try {
				((ComptePanel) tabs.getSelectedComponent())
				.deleteCurrentEcriture();
				
			} catch (IOException e1) {
				LOGGER.log(
						Level.SEVERE,
						"Impossible de supprimer l'écriture",
						e1);
			}

		} else if (e.getActionCommand() == POINTAGES) {		// Action tri
			EcrituresTableModel.triPointage = triButton.isSelected();
			stateChanged(null);									// Mettre à jour
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {}

	@Override
	public void windowClosed(WindowEvent arg0) {}

	@Override
	public void windowClosing(WindowEvent e) {
		if (DAOFactory.getFactory().mustBeSaved()) {

			int choix = JOptionPane.showOptionDialog(	// Choix utilisateur
					null,
					"Voulez-vous sauvegarder les modifications ?",
					"Quitter",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					null,
					JOptionPane.YES_OPTION);

			switch(choix) {
			case JOptionPane.YES_OPTION:
				try {
					DAOFactory.getFactory().save();		// Sauvegarder

				} catch (IOException e1) {				// Raté
					Logger.getLogger(SimpleGUI.class.getName()).log(
							Level.SEVERE, "Impossible de sauvegarder", e1);
					windowClosing(e);					// Recommencer
					return;
				}
				break;

			case JOptionPane.NO_OPTION:		break;		// Rien de spécial

			case JOptionPane.CANCEL_OPTION:
			case JOptionPane.CLOSED_OPTION:
				return;									// Annuler la fermeture
			}
		}

//		// Enregistrer les propriétés
//		try {
//			propManager.saveProperties();
//		} catch (IOException e2) {
//			e2.printStackTrace();
//		}// try
		
		// Quitter
		frame.dispose();
		try {
			wait(10000);								// Attendre un peu...
		} catch (InterruptedException e1) {
		} finally {
			System.exit(0);								// Quitter brutalement !
		}
	}
	
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	
	@Override
	public void windowIconified(WindowEvent arg0) {}

	@Override
	public void windowOpened(WindowEvent arg0) {}

	/**
	 * Mémorise les préférences relatives à la position de la fenêtre.
	 */
	@Override
	public void setAllProperties() {
		prefs.putInt(POS_X, frame.getLocation().x);
		prefs.putInt(POS_Y, frame.getLocation().y);
		prefs.putInt(WIDTH, frame.getSize().width);
		prefs.putInt(HEIGHT, frame.getSize().height);
	}
}
