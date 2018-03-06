package haas.olivier.comptes.gui2;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.buffer.BufferedDAOFactory;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.csv.CsvDAO;
import haas.olivier.comptes.dao.serialize.SerializeDAOFactory;
import haas.olivier.comptes.gui.ComptePanel;
import haas.olivier.comptes.gui.FilterCompte;
import haas.olivier.comptes.gui.actions.DataObserver;
import haas.olivier.gui.Dossier;
import haas.olivier.info.HandledException;

public class DossierComptes extends Dossier implements ChangeListener {

	// Constantes de types de DAO
	
	/** Constante désignant un DAO au format CSV. */
	private static final String CSV =
			"Fichier .csvz (données tableur zippées)";
	/** Constante désignant un DAO au format de sérialisation standard. */
	private static final String SERIAL=
			"Format natif (sérialisation Java compressée)";
	
	// Filtres de fichiers acceptés
	
	/** Filtre de fichiers .csvz */
	private static FileFilter filterCSV =
			new FileNameExtensionFilter(CSV, "csvz");	// Filtre CSV
	/** Filtre de fichiers .cpt */
	private static FileFilter filterSerial =
			new FileNameExtensionFilter(SERIAL, "cpt");	// Filtre natif
	
	/** La fabrique d'objets d'accès aux données. */
	private DAOFactory dao;
	
	/** Un observateur de changement de données, correspondant en principe à la
	 * vue englobante.
	 */
	public DataObserver gui;
	
	/** Panneau à onglets pour chaque type de comptes. */
	private JTabbedPane tabs;
	
	/** Construit un dossier de comptes.
	 * 
	 * @param gui	Un observateur de changement de données. Il s'agit en
	 * 				principe de l'interface graphique dans laquelle sera affiché
	 * 				le dossier, pour que l'interface prenne en compte, par
	 * 				exemple, l'existence de modifications non sauvegardées.
	 * 
	 * @param file	Un fichier contenant les données du dossier à charger.
	 * 
	 * @throws IOException 
	 */
	public DossierComptes(DataObserver gui, File file) throws IOException {
		this.gui = gui;
		
		// Ouvrir le fichier en fonction du format de fichier selon l'extension
		if (filterCSV.accept(file)) {					// Fichier CSV
			dao = new BufferedDAOFactory(new CsvDAO(file));

		} else if (filterSerial.accept(file)) {			// Sérialisation
			dao = new CacheDAOFactory(new SerializeDAOFactory(file));
		} else {
			throw new IOException("Extension de fichier non reconnue :" + file);
		}// if filtre
		
		// Recharger les données
//		DAOFactory.getFactory().load();
		
		// Recréer les onglets de l'interface principale
		createTabs();
		
		// Intercepter les changements d'onglet
		tabs.addChangeListener(this);

	}// constructeur

	/** Crée ou recrée les onglets des différents types de compte. */
	private void createTabs() {
		tabs.removeAll();	// Enlever les onglets actuels s'il y en a
		
		// Créer un onglet pour chaque type de compte
		tabs.addTab("Courant", new ComptePanel(
				gui, new FilterCompte(TypeCompte.COMPTE_COURANT)));
		tabs.addTab("Carte", new ComptePanel(
				gui, new FilterCompte(TypeCompte.COMPTE_CARTE)));
		tabs.addTab("Epargne", new ComptePanel(
				gui, new FilterCompte(
						TypeCompte.COMPTE_EPARGNE, TypeCompte.SUIVI_EPARGNE)));
		tabs.addTab("Enfants", new ComptePanel(
				gui, new FilterCompte(TypeCompte.ENFANTS)));
		tabs.addTab("Recettes", new ComptePanel(
				gui, new FilterCompte(TypeCompte.RECETTES)));
		tabs.addTab("Dépenses", new ComptePanel(
				gui, new FilterCompte(TypeCompte.DEPENSES)));
		tabs.addTab("Emprunt", new ComptePanel(
				gui, new FilterCompte(TypeCompte.EMPRUNT)));
		tabs.add("Investissements", new ComptePanel(
				gui, new FilterCompte(
						TypeCompte.DEPENSES_EN_EPARGNE,
						TypeCompte.RECETTES_EN_EPARGNE)));
		
		/* Pour les comptes clôturés, un filtre spécial qui n'accepte que les
		 * comptes clôturés */
		tabs.addTab("Clôturés", new ComptePanel(
				gui, new FilterCompte() {
					
					@Override
					public boolean acceptsBancaires() {
						return true;				// Imiter comptes bancaires
					}
					
					@Override
					public boolean accepts(Compte c) {
						return c.getCloture() != null;	// Compte clôturé
					}// accepts
					
				}));// classe anonyme FilterCompte
		
		tabs.setSelectedIndex(0);	// Sélectionner le 1er onglet par défaut
		
		// Mettre à jour l'onglet initial (simuler un changement d'onglet)
		stateChanged(null);
	}// createTabs

	@Override
	/** Met à jour l'onglet sélectionné.
	 * <p>
	 * Cette méthode est appelée lorsque l'utilisateur change d'onglet.
	 * Elle utilise un fil d'exécution à part pour ne pas bloquer la fenêtre.
	 */
	public void stateChanged(ChangeEvent e) {
		new SwingWorker<Void,Void>() {
			public Void doInBackground() {
				Component tab = tabs.getSelectedComponent();
				if (tab instanceof ComptePanel) {	// Bonne classe et non null 
					((ComptePanel) tab).update();
				}
				return null;
			}// doInBackground
		}.execute();
	}// stateChanged
	
	public void deleteCurrentEcriture() {
		// TODO à implémenter
	}

	@Override
	public File getFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getDaoName() {
		// TODO
		return null;
	}

	@Override
	public boolean shouldBeSaved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasSaveDest() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void save() throws HandledException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveAs(File file) throws HandledException {
		// TODO Auto-generated method stub
		
	}

}
