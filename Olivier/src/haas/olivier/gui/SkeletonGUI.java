/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.beans.EventHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

/** Une interface graphique de base permettant d'ouvrir un ou plusieurs dossiers
 * dans des fenêtres internes.
 * La classe propose notamment des menus et barre d'outils de base avec les
 * actions essentielles.
 *
 * @author Olivier HAAS
 */
// TODO Externaliser les fonctionnalités pour rendre la classe plus malléable
@SuppressWarnings("serial")
public abstract class SkeletonGUI<D extends Dossier> extends JFrame {
	
	/** Le Logger de cette classe. */
	private static final Logger LOGGER =
			Logger.getLogger(SkeletonGUI.class.getName());
	
	/** Préférence utilisateur: nom de la propriété pour placement horizontal.*/
	protected static final String POS_X = "pos_x";
	/** Préférence utilisateur: nom de la propriété pour placement vertical.*/
	protected static final String POS_Y = "pos_y";
	/** Préférence utilisateur: nom de la propriété de hauteur de la fenêtre.*/
	protected static final String WIDTH = "width";
	/** Préférence utilisateur: nom de la propriété de largeur de la fenêtre. */
	protected static final String HEIGHT = "height";
	/** Préférence utilisateur: nom de la propriété indiquant une fenêtre
	 * maximisée en largeur. */
	protected static final String MAXIMIZEDH = "maximizedh";
	/** Préférence utilisateur: nom de la propriété indiquant une fenêtre
	 * maximisée en hauteur. */
	protected static final String MAXIMIZEDV = "maximizedv";
	/** Préférence utilisateur: nom de la propriété des derniers dossiers
	 * ouverts. */
	protected static final String RECENT = "recent";
	/** Préférence utilisateur: nom de la propriété du nombre de dossiers
	 * récents à retenir. */
	protected static final String NB_RECENT = "nb_recent";
	/** Séparateur de champs pour les préférences utilisateurs.<br>
	 * Il est utilisé pour les propriétés multi-valeurs.
	 * <p>
	 * Ce séparateur est protégé par un antislash (doublé dans la déclaration)
	 * s'il s'agit d'un caractère utilisé dans les expressions régulières.
	 */
	private static final String PROP_SEPARATOR = "|"; // Pipe
	
	/**
	 * L'observateur de sélection de dossier.
	 */
	private final DossierSelectionObservable<D> dossierSelectionObservable =
			new DossierSelectionObservable<D>();
	
	/** Les préférences utilisateur. */
	protected final Preferences prefs;
	
	/** Table des items de menu pour ouvrir les dossiers récents */
	protected JMenuItem[] recentFileItems;
	
	/** Collection des dossiers ouverts. */
	protected Set<D> dossiers = new HashSet<D>();

	/** Menu Fichier. */
	protected JMenu menuFichier = new JMenu("Fichier");
	/** Menu Fenêtre. */
	protected JMenu menuFenetre = new JMenu("Fenêtre");
	/** Menu Affichage. */
	protected JMenu menuAffichage = new JMenu("Affichage");
	
	/** Action créant un nouveau dossier. */
	protected Action actionNouveau;
	/** Action ouvrant un nouveau dossier. */
	protected Action actionOuvrir;
	/** Action sauvegardant le dossier actuel. */
	protected Action actionSave;
	/** Action sauvegardant le dossier actuel dans une autre source. */
	protected Action actionSaveAs;
	/** Action fermant le dossier actuel. */
	protected Action actionFermer;
	/** Action icône "croix" appelant l'action <code>actionFermer</code>. */
	protected Action actionCroixFermer;
	/** Action quittant l'application. */
	protected Action actionQuitter;
	
	/** Le dossier actif. */
	private D dossierActif;
	
	/** Construit une fenêtre graphique de base et des trames de menus.
	 * 
	 * @param prefs	Les préférences utilisateur.
	 */
	public SkeletonGUI(Preferences prefs) {
		this.prefs = prefs;							// Mémoriser préférences
		
		// Titre de la fenêtre
		setTitle(null);
		
		// Configurer la fenêtre
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(							// Quitter en fermant
				EventHandler.create(WindowListener.class,
						this, "quit", null, "windowClosing"));

		// Barre de menus
		JMenuBar menuBar = new JMenuBar();			// Créer barre de menu
		setJMenuBar(menuBar);						// Appliquer à la fenêtre
		
		// Configurer les menus de base
		// Nouveau
		actionNouveau = new AbstractAction(			// Action nouveau
				"Nouveau...",
				IconLoader.createImageIcon("images/sc05500.png", null)) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// Ajouter un nouveau dossier vierge
				try {addDossier(getNewDossier());} catch (Exception e1) {}
			}// actionPerformed
			
		};// classe anonyme AbstractAction
		
		// Ouvrir
		actionOuvrir = new SimpleAction(			// Action ouvrir
				"Ouvrir",
				IconLoader.createImageIcon("images/sc05501.png", null),
				EventHandler.create(ActionListener.class, this, "open"));
		
		// Enregistrer
		actionSave = new AbstractAction(
				"Enregistrer",
				IconLoader.createImageIcon("images/sc05502.png", null)) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					saveClean(getDossierActif());	// Sauvegarder proprement
				} catch (IOException e1) {
					LOGGER.log(Level.SEVERE, "Impossible d'enregistrer", e);
				}// try
			}// actionPerformed
			
		};// classe anonyme AbstractAction
		actionSave.setEnabled(false);				// Désactiver au départ
		
		// Enregistrer sous...
		actionSaveAs = new AbstractAction(		// Action Enregistrer sous... 
				"Enregistrer sous...",
				IconLoader.createImageIcon("images/sc05502.png", null)) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					saveAs(getDossierActif());		// Enregistrer sous...
				} catch (IOException e1) {
					LOGGER.log(Level.SEVERE, "Impossible d'enregistrer", e);
				}// try
			}// actionPerformed
			
		};// classe anonyme AbstractAction
		actionSaveAs.setEnabled(false);				// Désactiver au départ
		
		// Fermer
		actionFermer = new AbstractAction(		// Action fermer
				"Fermer",
				IconLoader.createImageIcon("images/sc_closedoc.png", null)) {

			@Override
			public void actionPerformed(ActionEvent e) {
				closeDossier(getDossierActif());	// Fermer
			}// actionPerformed
			
		};// classe anonyme AbstractAction
		actionFermer.setEnabled(false);				// Désactiver au départ
		
		actionCroixFermer = new SimpleAction(		// Action croix pour fermer
				null, (Icon) UIManager.get("InternalFrame.closeIcon"),
				actionFermer);
		actionCroixFermer.setEnabled(false);		// Désactiver au départ
		
		// Quitter
		actionQuitter = new SimpleAction("Quitter",	// Action quitter
				 IconLoader.createImageIcon("images/sc_quit.png", null),
				 EventHandler.create(ActionListener.class, this, "quit")); 
		
		// Menu Fichier (vide pour l'instant)
		menuBar.add(menuFichier);					// Ajouter le menu Fichier
		
		// Récupérer la liste des fichiers récents
		
		// Liste des noms de fichiers à partir de la propriété brute
		String[] recentFiles = prefs.get(RECENT, "").split(
				Pattern.quote(PROP_SEPARATOR));		// Quoter caractère(s) regex
		List<String> fileList = new ArrayList<String>(recentFiles.length);
		for (String fileName : recentFiles)
			fileList.add(fileName);

		// Retirer les éléments vides (notamment si la propriété est "")
		Iterator<String> it = fileList.iterator();	// Itérateur
		while (it.hasNext()) {
			if (it.next().isEmpty())				// Si l'élément est ""
				it.remove();						// Retirer
		}// while

		// Construire une table des items de menu correspondants
		recentFileItems = new JMenuItem[prefs.getInt(NB_RECENT, 4)];

		/* Remplir la table.
		 * On doit utiliser deux compteurs d'index. En effet, il est possible
		 * que certaines entrées récentes n'existent plus, auquel cas la
		 * création de l'action échoue et il y a un décalage entre la liste des
		 * préférences et la liste réelle.
		 */
		int j = 0;									// Curseur liste finale
		for (int i=0; i<fileList.size() && i<recentFileItems.length; i++) {
			try {
				recentFileItems[j] = 				// Item pointant le fichier
						new JMenuItem(newRecentAction(fileList.get(i)));
				
				// Incrémenter le compteur d'index, sauf en cas d'exception
				j++;

			} catch (RecentFileException e) {		// La source n'existe plus
				// Tant pis
			}// try
		}// for

		// Remplir le menu Fichier (y compris les dossiers récents)
		fillMenuFichier();
		
//		/* Récupérer le nombre de fichiers récents à mémoriser.
//		 * Par ricochet, cela remplit en même temps le menu Fichier.
//		 * Cette méthode doit donc être appelée après la récupération des
//		 * noms de dossiers.
//		 */
//		updateRecentFilesCount();
		
		// Menu style d'affichage
		JMenu menuStyle = new JMenu("Style");
		menuAffichage.add(menuStyle);
		ButtonGroup lfGroup = new ButtonGroup();
		for (final LookAndFeelInfo lf : UIManager.getInstalledLookAndFeels()) {
			
			Action action = new AbstractAction(lf.getName()) {	// Chgt de L&F
				
				@Override
				public void actionPerformed(ActionEvent e) {
					setLookAndFeel(lf.getClassName());
				}// actionPerformed
				
			};// classe anonyme AbstractAction
			
			JRadioButtonMenuItem radioButton =
					new JRadioButtonMenuItem(action);	// Item de menu radio
			lfGroup.add(radioButton);					// Ajouter au groupe
			if (lf.getName().equals(					// C'est le L&F préféré?
					prefs.get("plaf", UIManager.getLookAndFeel().getName()))) {
				radioButton.setSelected(true);			// Sélectionner bouton
			}// if L&F préféré
			menuStyle.add(radioButton);					// Insérer dans le menu
		}// for LookAndFeel
		
		// TODO Implémenter la barre d'outils
	}// constructeur
	
	/** Rétablit la taille, la position et la maximisation éventuelle de la
	 * fenêtre, telle qu'elle figure dans les préférences utilisateurs.<br>
	 * Ces valeurs sont ajustées si besoin pour correspondre aux dimensions
	 * maximales de l'écran.
	 */
	protected void packAndResize() {
		try {
			if (prefs.nodeExists("")) {
			
				// Récupérer la taille par défaut de la fenêtre
				int x = prefs.getInt(POS_X, -1);		// Abscisse
				int y = prefs.getInt(POS_Y, -1);		// Ordonnée
				int width = prefs.getInt(WIDTH, 800);	// Pref largeur
				int height = prefs.getInt(HEIGHT, 600);	// Pref hauteur

				// Cadrer les dimensions dans l'écran
				Dimension ecran =						// Dimensions écran
						Toolkit.getDefaultToolkit().getScreenSize();
				if (width > ecran.width)	 			// Largeur maxi
					width = ecran.width;
				if (height > ecran.height)				// Hauteur maxi
					height = ecran.height;
				setPreferredSize(
						new Dimension(width, height));	// Dimensions à utiliser

				// Cadrer la position
				if (x < 0 || y < 0) {					// Pos non spécifiée
					setLocationByPlatform(true);		// Placement automatique

				} else {								// Utiliser les prefs
					if (x + width > ecran.width)		// Abscisse maxi
						x = ecran.width - width;
					if (y + height > ecran.height)		// Ordonnée maxi
						y = ecran.height - height;
					setLocation(x, y);					// Position à utiliser
				}// if propriété(s) de position null

				// Empaqueter le contenu de la fenêtre
				pack();

				// Maximiser la fenêtre si elle l'était
				if (prefs.getBoolean(MAXIMIZEDH, false)) {
					// Pleine largeur
					setExtendedState(
							prefs.getBoolean(MAXIMIZEDV, false)
							? MAXIMIZED_BOTH			// Pleine hauteur aussi
									: MAXIMIZED_HORIZ);	// Pleine largeur seule
					pack();								// Repaqueter la fenêtre

				} else if (prefs.getBoolean(MAXIMIZEDV, false)) {
					setExtendedState(MAXIMIZED_VERT);	// Pleine hauteur seule
					pack();								// Repaqueter la fenêtre
				}// if maximized
				
				return;									// Terminer ici !
			}// if prefs existe
			
		} catch (HeadlessException e) {					// Erreurs diverses
			e.printStackTrace();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}// try
		
		// Solution de secours si prefs n'existe pas ou en cas d'erreur
		setLocationByPlatform(true);					// Placement automatique
		pack();											// Empaqueter
	}// packAndSize
	
	/** Remplit le menu Fichier avec les items adéquats.
	 * <p>
	 * Cette méthode peut être appelée plusieurs fois, notamment lorsque le
	 * nombre de dossiers récents change (soit parce qu'on a changé le nombre de
	 * dossiers à mémoriser, soit parce que la liste était incomplète et qu'on
	 * en ajoute un nouveau).
	 */
	protected void fillMenuFichier() {
		menuFichier.removeAll();						// Remettre à blanc
		menuFichier.add(new JMenuItem(actionNouveau));	// Item nouveau du menu
		menuFichier.add(new JMenuItem(actionOuvrir));	// Item ouvrir du menu
		menuFichier.add(new JMenuItem(actionSave));		// Item "save" du menu
		menuFichier.add(new JMenuItem(actionSaveAs));	// Item du menu
		menuFichier.add(new JMenuItem(actionFermer));	// Item fermer du menu
		
		// Liste des dossiers récents
		addRecentFiles(menuFichier);
		menuFichier.addSeparator();						// Séparateur
		menuFichier.add(new JMenuItem(actionQuitter));	// Item quitter du menu
	}// fillMenuFichier
	
	/** Crée une <code>Action</code> permettant d'ouvrir le dossier spécifié.
	 * 
	 * @param source	La source permettant d'ouvrir le dossier. Cet argument
	 * 					est fourni au format texte.
	 * 
	 * @return			Une nouvelle <code>Action</code> dont la propriété
	 * 					<code>ACION_COMMAND_KEY</code> est <code>source</code>.
	 * 
	 * @throws RecentFileException
	 * 					Si la création de l'<code>Action</code> a échoué.
	 */
	protected Action newRecentAction(String source) throws RecentFileException {
		if (source == null)
			throw new RecentFileException();
		
		return new RecentFileAction(new File(source));
	}// getRecentAction
	
	/** Ajoute des items de menu ouvrant les fichiers récents.
	 * 
	 * @param menu	Le menu dans lequel ajouter les items.
	 */
	protected void addRecentFiles(JMenu menu) {
		if (recentFileItems != null) {					// Si non null
			if (recentFileItems.length > 0 && recentFileItems[0] != null)
				menu.addSeparator();					//Séparateur si non vide
			for (JMenuItem item : recentFileItems) {	// Chaque dossier récent
				if (item != null)						// S'il y en a encore
					menu.add(item);						// Ajouter l'item
			}// for item
		}// if array non null
	}// addRecentFiles
	
	/** Ajoute une croix à droite de la barre de menu pour permettre la
	 * fermeture du dossier en cours.
	 */
	protected void addCroixFermer() {
		JButton boutonCroix = 
				new JButton(actionCroixFermer);				// Le bouton
		boutonCroix.setBorderPainted(false);				// Pas de bordure
		boutonCroix.setContentAreaFilled(false);			// Pas de fond
		boutonCroix.setFocusPainted(false);					// Pas de pointillés
		boutonCroix.setHideActionText(true);				// Pas de texte
		
		// Pas d'icône si l'action est désactivée
		boutonCroix.setDisabledIcon(new Icon() {			// Icône vide
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {}
			@Override
			public int getIconWidth() {return 0;}
			@Override
			public int getIconHeight() {return 0;}
		});// classe anonyme Icon vide
		
		// Insérer dans la barre de menu
		JMenuBar menuBar = getJMenuBar();					// Barre de menu
		menuBar.add(Box.createHorizontalGlue());			// Pousser à droite
		menuBar.add(boutonCroix);							// Bouton croix
	}// addCroixFermer
	
	/** Active ou désactive les actions qui sont liées à la présence d'un
	 * dossier, telles que "sauvegarder sous", "fermer", etc.
	 * 
	 * @param enabled	L'état que doivent avoir ces actions.
	 */
	protected void setDossierActionsEnabled(boolean enabled) {
		actionSaveAs.setEnabled(enabled);
		actionFermer.setEnabled(enabled);
		actionCroixFermer.setEnabled(enabled);
	}// setDossierActionsEnabled
	
	/** Lit dans les préférences le nombre maximum de dossiers récents à
	 * mémoriser et ajuste le tableau des items de menu si besoin.
	 * <p>
	 * Le tableau des items de menu permettant d'accéder aux dossiers récents
	 * peut être tronquée par cette méthode, le cas échéant.<br>
	 * Le menu Fichier est ensuite mis à jour.
	 * <p>
	 * Cette méthode est appelée soit pour initialiser les items de menu des
	 * fichiers récents à l'intérieur du menu Fichier, soit pour modifier le
	 * nombre de dossiers à retenir au cours de la vie de l'application.
	 */
	protected void updateRecentFilesCount() {
		
		// Lire le nombre maximum de dossiers à mémoriser
		int nb = prefs.getInt(NB_RECENT, 4);
		
		// Créer et remplir une nouvelle table des items de menu
		JMenuItem[] newRecentList = new JMenuItem[nb];	// Nouvelle table
		if (recentFileItems != null) {					// S'il en avait une
			for (int i=0; i<nb && i<recentFileItems.length; i++) {// Min tailles
				newRecentList[i] = recentFileItems[i];	// Recopier les items
			}// for item
		}// if not null
		recentFileItems = newRecentList;				// Remplacer la table
		
		// Mettre à jour le menu Fichier
		fillMenuFichier();
	}// setNbRecentFiles

	/** Met à jour la table des items de menu permettant d'accéder aux dossiers
	 * récents, en insérant en premier la source spécifiée.
	 * <p>
	 * Cette méthode est appelée lorsqu'un dossier est ouvert ou sauvegardé, et
	 * que le nom de ce dossier doit donc être inséré ou déplacé en première
	 * position.
	 * 
	 * @param action	L'action à insérer en premier. Cette action contient en
	 * 					commande le nom de la source de données à utiliser.
	 */
	protected void updateRecentFilesArray(Action recentAction) {
		
		/* Le principe consiste à placer en premier l'action de la nouvelle
		 * source, puis à déplacer d'un cran par récurrence chaque action qui
		 * était déjà dans la liste. On s'arrête soit quand il n'y a plus de
		 * place, soit quand il y a une place vide, soit quand on retombe sur
		 * une action qui pointait vers la première source (cas d'une source qui
		 * figurait déjà dans la liste des dossiers récents, et qui doit juste
		 * repasser en premier dans la liste).
		 */
		Object command = recentAction.getValue(	//Commande de la nouvelle action
				Action.ACTION_COMMAND_KEY);
		Action action = recentAction;			// Pointeur action
		for (int i=0; i<recentFileItems.length; i++) {	// Parcourir liste
			JMenuItem item = recentFileItems[i];		// Item actuel
			
			// S'il n'y a plus d'item, insérer seulement l'action en cours
			if (item == null) {
				recentFileItems[i] =			// Nouvel item action en cours
						new JMenuItem(action);
				fillMenuFichier();				// Refaire le menu (+1 item)
				break;							// Plus rien à faire
			}// if item null
			
			// Sinon, placer l'action ici et recommencer avec la suivante
			Action temp = item.getAction();		// Mémoriser action suivante
			item.setAction(action);				// Placer ici action en cours
			action = temp;						// Continuer action suivante
			
			/* Si l'action qu'on vient de retirer pointe vers le fichier
			 * d'origine, pas besoin de la déplacer. On arrête là !
			 */
			if (command.equals(action.getValue(Action.ACTION_COMMAND_KEY)))
				break;
		}// for menu items
		
		// Mémoriser de manière persistante la liste des derniers fichiers
		String recentFiles = "";					// Chaîne résultat
		boolean first = true;						// Drapeau 1er item
		for (JMenuItem item : recentFileItems) {	// Parcourir la liste
			if (item != null) {						// S'il y a un item
				// Ajouter le nom du fichier à la liste (+séparateur)
				recentFiles +=
						(first ? "" : PROP_SEPARATOR)//Séparateur (sauf 1er)
						+ item.getAction().getValue(// Nom de fichier
								Action.ACTION_COMMAND_KEY);
				first = false;						// N'est plus le premier
			}// if item non null
		}// for items de menu

		// Définir la propriété
		try {
			if (prefs.nodeExists(""))
				prefs.put(RECENT, recentFiles);
			
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}// try
	}// updateRecentFileListe
	
	/** Sélectionne le dossier et l'affiche dans la fenêtre.<br>
	 * Le nom du dossier est ajouté au titre de la fenêtre.
	 */
	protected void selectDossier(D dossier) {
		add(dossier.getComponent());
		D oldDossier = dossierActif;
		dossierActif = dossier;					// Mémoriser la sélection
		setTitle(dossier.getName());			// Nom du dossier dans le titre
		actionSave.setEnabled(					// Autoriser save direct...
				dossier.hasSaveDest());			// ...si possible
		updateWindowsList();					// Sélection dans menu Fenêtre
		
		// Notifier les observateurs
		dossierSelectionObservable.dossierChanged(dossierActif, oldDossier);
		
		// Repeindre tout
		JRootPane root = getRootPane();
		root.revalidate();
		root.repaint();
	}// selectDossier
	
	/** Renvoie le dossier actif. */
	public D getDossierActif() {
		return dossierActif;
	}// getDossierActif
	
	/** Ajoute un dossier à la fenêtre, et le sélectionne. */
	protected void addDossier(D dossier) {
		dossiers.add(dossier);					// Ajouter à la collection
		updateWindowsList();					// Mettre à jour le menu Fenêtre
		selectDossier(dossier);					// Sélectionner
		
		// Activer toutes les actions liées à la présence d'un dossier
		setDossierActionsEnabled(true);
	}// addDossier
	
	/** Ouvre une boîte de dialogue pour charger un dossier.
	 * <p>
	 * <b>Attention : cette méthode est appelée par un <code>EventHandler</code>
	 * (non visible par le compilateur).</b>
	 */
	public void open() {
		JFileChooser chooser = getFileChooser();
		if (chooser.showOpenDialog(this) ==
				JFileChooser.APPROVE_OPTION) {	// Validation par le bouton OK
			open(chooser.getSelectedFiles());
		}// if bouton OK
	}// open
	
	/** Charge directement le fichier spécifié.
	 * 
	 * @param file	Le fichier à charger.
	 */
	private void open(File file) {
		try {
			addDossier(getNewDossier(file));// Ouvrir le dossier
			updateRecentFilesArray(			// Màj la liste des fichiers récents
					newRecentAction(file.getAbsolutePath()));
			
		} catch (RecentFileException e) {
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					"Impossible d'ouvrir le fichier " + file.getName(), e);
		}// try
	}// open file
	
	/** Charge chaque fichier spécifié. Il y a autant de nouveaux dossiers que
	 * de fichiers spécifiés.
	 * 
	 * @param files	Les fichiers à charger.
	 */
	protected void open(File[] files) {
		for (File file : files)
			open(file);
	}// open files
	
	/** Sauvegarde proprement un dossier.
	 * <p>
	 * Si le dossier ne peut pas être sauvegardé directement, la méthode
	 * demande à l'utilisateur s'il veut le sauvegarder sous un autre nom.
	 * 
	 * @throws IOException
	 * 			Si le dossier n'a pas été sauvegardé.
	 */
	private void saveClean(D dossier) throws IOException {
		try {
			dossier.save();					// Sauvegarder
		} catch (IOException e) {			// Si problème
			saveAs(dossier);				// Essayer de sauvegarder ailleurs
		}// try
	}// saveOnClose
	
	/** Ouvre une boîte de dialogue pour permettre à l'utilisateur d'enregistrer
	 * le dossier spécifié sous la destination de son choix.
	 * 
	 * @param dossier		Le dossier à enregistrer.
	 * 
	 * @throws IOException	Si le dossier n'a pas pu être enregistré.
	 */
	private void saveAs(D dossier) throws IOException {
		JFileChooser chooser = getFileChooser();		// Sélecteur de fichier
		
		// Afficher la boîte de dialogue
		if (chooser.showSaveDialog(this)
				== JFileChooser.APPROVE_OPTION) {		// Utilisateur OK
			
			dossier.saveAs(chooser.getSelectedFile());	// Sauvegarder sous
			
			// Fichiers récents
			try {
				updateRecentFilesArray(
						newRecentAction(dossier.getFile().getAbsolutePath()));
			} catch (RecentFileException e) {			// Tant pis
			}// try
		}// if bouton ok
	}// saveAs
	
	/** Ferme le dossier spécifié, après avoir vérifié qu'il était sauvegardé ou
	 * avoir demandé à l'utilisateur s'il fallait le faire.
	 * 
	 * @return	<code>true</code> si le dossier a pu être fermé correctement,
	 * 			<code>false</code> sinon.
	 */
	boolean closeDossierSafe(D dossier) {
		
		// Vérifier le statut de la sauvegarde
		if (dossier.shouldBeSaved()) {
			int choix = JOptionPane.showConfirmDialog(
					this,
					"Voulez-vous sauvegarder le dossier " +
					dossier.getName() + " ?",
					"Fermer",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			
			switch(choix) {
			
			case JOptionPane.YES_OPTION:				// Oui, sauvegarder
				try {
					saveClean(dossier);					//Sauvegarder proprement
				} catch (IOException e) {
					return false;						// Sinon arrêter
				}// try
				break;
				
			case JOptionPane.NO_OPTION:					// Non, pas sauvegarder
				break;									// Continuer
				
			case JOptionPane.CANCEL_OPTION:				// Annuler
				return false;							// Arrêter tout
			}// switch
		}// if dossier saved
		
		// Sauvegarde OK : fermer le dossier
		closeDossier(dossier);
		return true;
	}// closeDossierSafe
	
	/** Ferme le dossier sans précaution particulière (sauvegardé ou pas). */
	protected void closeDossier(D dossier) {
		dossiers.remove(dossier);					// Suppr de la mémoire
		updateWindowsList();						// Mettre le menu à jour
		if (dossierActif == dossier) {				// Si affiché (actif)
			if (dossiers.isEmpty()) {					// Et pas d'autre ouvert
				
				// Retirer le dossier de la fenêtre
				Component comp = dossier.getComponent();// Composant du dossier
				Container parent = comp.getParent();	// Son parent
				if (parent != null) {
					parent.remove(comp);				// Retirer du parent
					validate();							// Revalider la fenêtre
					repaint();
				}// if parent non null
				
				// Plus de dossier actif
				dossierActif = null;					// Plus de sélection
				setTitle(null);							// Rien dans le titre
				actionSaveAs.setEnabled(false);			// Action save as grisée
				setDossierActionsEnabled(false);		// Autres actions grisée
			} else {								// Si autres dossiers	
				selectDossier(						// Prendre n'importe lequel
						dossiers.iterator().next());
			}// if autres dossiers
		}// if sélectionné
	}// closeDossier
	
	/** Quitte l'application après avoir fermé proprement les dossiers un par
	 * un.
	 * <p>
	 * <b>Attention : cette méthode est appelée par un <code>EventHandler</code>
	 * (non visible par le compilateur).</b>
	 */
	public void quit() {
		
		// Sauvegarder les préférences qui ne sont pas mises à jour en continu
		try {
			setAllPreferences();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}// try
		
		// Fermer chaque dossier
		boolean allClosed = true;			// Marqueur de fermeture correcte
		Collection<D> list =				// Nouvelle collection pour éviter
				new ArrayList<D>(dossiers);	// les modifications concurrentes
		for (D d : list)
			allClosed = allClosed && closeDossierSafe(d);// Fermeture correcte ?
		
		// Fermer la fenêtre principale
		if (allClosed)						// Si tout est OK
			dispose();						// Fermer
	}// quit
	
	/** Met à jour la liste des fenêtres dans le menu. */
	private void updateWindowsList() {
		menuFenetre.removeAll();						// Effacer tout
		
		ButtonGroup group = new ButtonGroup();			// Groupe de boutons
		Dossier actif = getDossierActif();				// Dossier actif
		
		// Pour chaque dossier ouvert
		for (final D dossier : dossiers) {
			
			// Avec une Action au nom du dossier
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(
					new AbstractAction(dossier.getName()) {
						
				@Override
				public void actionPerformed(ActionEvent e) {
					selectDossier(dossier);	// Sélectionner le dossier
				}// actionPerformed
				
			});// classe anonyme AbstractAction
			
			// Ajouter au groupe
			group.add(item);
			if (dossier == actif)						// Si dossier actif
				item.setSelected(true);					// Sélectionner
			
			// Ajouter un menu
			menuFenetre.add(item);
		}// for dossier
	}// updateWindowsList
	
	/** Modifie l'apparence de la fenêtre.
	 * 
	 * @param lfName	Le nom du LookAndFeel à appliquer. S'il n'est pas
	 * 					disponible, la méthode ne fait rien.
	 */
	protected void setLookAndFeel(String lfName) {
		try {
			UIManager.setLookAndFeel(lfName);
			SwingUtilities.updateComponentTreeUI(this);
			pack();
			
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}// try
	}// setLookAndFeel
	
	/** Sauvegarde dans les préférences utilisateurs toutes les informations qui
	 * ne sont pas mises à jour en continu.
	 * <p>
	 * Par exemple, cette méthode peut redéfinir les préférences afférentes à la
	 * position de la fenêtre.<br>
	 * <i>A contrario</i>, la liste des derniers dossiers ouverts ou les options
	 * d'affichage sont, en principe, définies ailleurs dans le programme, au
	 * moment de leur changement. Elles n'ont donc en principe pas besoin d'être
	 * reprises ici.
	 * 
	 * @throws BackingStoreException
	 * 				Si les préférences utilisateur ne sont pas
	 * 				accessibles.
	 */
	public void setAllPreferences() throws BackingStoreException {
		if (prefs.nodeExists("")) {
			// Position de la fenêtre
			int state = getExtendedState();					// Statut de la fenêtre

			// Détecter la maximisation
			Boolean maximizedH = (state & MAXIMIZED_HORIZ) != 0,// Pleine largeur ?
					maximizedV = (state & MAXIMIZED_VERT) != 0;// Pleine hauteur ?
			// Mémoriser la maximisation
			prefs.putBoolean(MAXIMIZEDH, maximizedH);
			prefs.putBoolean(MAXIMIZEDH, maximizedV);

			// Mémoriser les coordonnées (sauf si maximisation)
			if (!maximizedH) {
				prefs.putInt(POS_Y, getLocation().y);
				prefs.putInt(WIDTH, getSize().width);
			}
			if (!maximizedV) {
				prefs.putInt(POS_X, getLocation().x);
				prefs.putInt(HEIGHT, getSize().height);
			}
		}// if prefs existe
	}// setAllProperties
	
	/** Renvoie un <code>JFileChooser</code> standard.
	 * <p>
	 * Cette méthode peut être réécrite pour personnaliser le
	 * <code>JFileChooser</code>, notamment du point de vue des filtres
	 * acceptés.
	 */
	protected JFileChooser getFileChooser() {
		return  new JFileChooser();
	}// getFileChooser
	
	/** Change le titre de la fenêtre en y intégrant le texte spécifié en plus
	 * du nom de l'application.
	 */
	@Override
	public void setTitle(String title) {
		super.setTitle(getApplicationName() +
				((title == null || title.isEmpty())
				? "" : " - " + title));
	}// setTitle
	
	/** Renvoie le nom de l'application. */
	protected abstract String getApplicationName();
	
	/** Renvoie un nouveau dossier vierge. */
	protected abstract D getNewDossier();
	
	/** Renvoie un dossier contenant les données du fichier spécifié.
	 * 
	 * @param file	Le fichier contenant les données à charger.
	 * 
	 * @throws IOException
	 * 				Si le fichier n'a pas pu être ouvert correctement.
	 */
	protected abstract D getNewDossier(File file) throws IOException;
	
	/**
	 * Renvoie l'objet qui surveille les changements de sélection de dossier.
	 *
	 * @return	L'observable unique.
	 */
	public DossierSelectionObservable<D> getDossierSelectionObservable() {
		return dossierSelectionObservable;
	}
	
	/** Une <code>Action</code> qui sert à ouvrir un fichier récent.
	 * <p>
	 * Cette classe sert uniquement aux <code>JMenuItem</code> permettant
	 * d'ouvrir un des fichiers récemment ouverts.
	 * <p>
	 * Pour permettre le réordonnancement de la liste des fichiers récents sans
	 * nécessairement réécrire le contenu du menu, il est prévu que ces actions
	 * puissent être redistribuées entre les items existants.<br>
	 * Pour cette raison, il n'était pas opportun d'en faire une classe
	 * abstraite : on a besoin d'accéder aux données de l'<code>Action</code>
	 * après son instanciation.
	 *
	 * @author Olivier HAAS
	 */
	private class RecentFileAction extends AbstractAction {

		/** Le fichier récemment ouvert. */
		private File file;
		
		/** Construit une <code>Action</code> permettant d'ouvrir le fichier
		 * spécifié.
		 * 
		 * @param file	Le fichier à ouvrir par cette <code>Action</code>.
		 * 
		 * @throws RecentFileException
		 * 				Si le fichier n'existe pas
		 */
		private RecentFileAction(File file) throws RecentFileException {
			this.file = file;					// Mémoriser le fichier
			
			// Vérifier que le fichier existe
			if (!file.exists())
				throw new RecentFileException();
			
			// Mémoriser comme commande le nom complet du fichier
			putValue(Action.ACTION_COMMAND_KEY, file.getAbsolutePath());
			
			// Donner à l'action le nom du fichier sans extension
			String fileName = file.getName();	// Nom de fichier
			int i = fileName.lastIndexOf('.');	// Position du dernier point
			if (i>0 && i<fileName.length()) {	// S'il y a un point
				fileName =						// Texte avant le point
						fileName.substring(0, i);
			}
			putValue(Action.NAME, fileName);	// Utiliser nom sans extension
		}// constructeur
		
		/** Ouvre le fichier vers lequel pointe l'action. */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			open(file);							// Ouvrir le fichier
		}// actionPerformed
	}// nested class RecentFileAction

	/** Une exception indiquant que la création d'une <code>Action</code> pour un
	 * dossier récent a échoué.
	 * 
	 * @author Olivier HAAS
	 */
	protected static class RecentFileException extends Exception {
		public RecentFileException() {
		}// constructeur
	}// class RecentFileException

}// class SkeletonGUI