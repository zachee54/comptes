/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.gui.settings;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.csv.CsvDAO;
import haas.olivier.comptes.gui.SimpleGUI;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

// TODO Classe à remplacer à terme par DossierComptes
/**
 * Une classe gérant le choix d'un fichier de données à ouvrir, créer ou
 * enregistrer.
 *
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class SetupDAO extends JFileChooser {
	
	/**
	 * Le Logger de cette classe.
	 */
	private static final Logger LOGGER =
			Logger.getLogger(SetupDAO.class.getName());
	
	// Constantes d'actions
	public static final int OPEN = 1;
	public static final int SAVE = 2;
	
	// Constantes désignant les DAO
	private static final String CSV =
			"Fichier .csvz (données tableur dans une archive)";
	private static final String SERIAL=
			"Format natif (sérialisation Java compressée)";
	
	private SimpleGUI gui;			// Gestionnaire de l'interface principale
	private FileFilter filterCSV =
			new FileNameExtensionFilter(CSV, "csvz");	// Filtre CSV
	private FileFilter filterSerial =
			new FileNameExtensionFilter(SERIAL, "cpt");	// Filtre natif
	
	/**
	 * Ouvre un boîte de dialogue de sélection du fichier, et réalise l'action
	 * correspondante.
	 */
	public SetupDAO(SimpleGUI gui, int action) {
		this.gui = gui;
		
		// Insérer les filtres dans le sélecteur de fichiers
		addChoosableFileFilter(filterSerial);
		addChoosableFileFilter(filterCSV);
		
		// Par défaut: format natif
		setFileFilter(filterSerial);
		
		// Ne pas accepter de filtre "Tous les fichiers (*.*)"
		setAcceptAllFileFilterUsed(false);
		
		// Déterminer l'action à effectuer
		switch(action) {
		case OPEN :	openFile(); break;
		case SAVE : saveFile(); break;
		}
		
		// Mettre à jour l'affichage du nom de DAO et fichier
		gui.updateDaoName();
	}
	
	/**
	 * Charge une nouvelle source de données.
	 */
	private void openFile() {
		if (showOpenDialog(gui.getFrame())
				== JFileChooser.APPROVE_OPTION) {		// Bouton OK
			File file = getSelectedFile();				// Fichier à ouvrir
			FileFilter filter = getFileFilter();		// Filtre DAO
			
			try {
				// Ouvrir le fichier en fonction du DAO choisi
				if (filter.equals(filterCSV)) {			// Fichier CSV
					DAOFactory.setFactory(
							new CacheDAOFactory(CsvDAO.newInstance(file)),
							false);

//				} else if (filter.equals(filterSerial)) {// Sérialisation
//					DAOFactory.setFactory(
//							new CacheDAOFactory(new SerializeDAOFactory(file)),
//							false);
				}
				
				// Recréer les onglets de l'interface principale
				gui.createTabs();
				
			} catch (IOException e) {
				LOGGER.log(
						Level.SEVERE,
						"Impossible d'ouvrir le fichier " + file.getName(),
						e);
			}
		}
	}
	
	/**
	 * Bascule les données vers une nouvelle source de données.
	 * Les données potentiellement contenues dans la source, si elle existe
	 * déjà, seront écrasées et remplacées par les données en cours
	 * d'utilisation.
	 */
	private void saveFile() {
		if (showSaveDialog(gui.getFrame())
				== JFileChooser.APPROVE_OPTION) {		// Bouton OK
			File file = getSelectedFile();				// Fichier à écrire
			FileFilter filter = getFileFilter();		// Filtre DAO
			
			// Ajouter l'extension si elle n'y est pas
			if (!filter.accept(file)) {	//Le filtre ne reconnaît pas l'extension
				
				// Déterminer quelle extension
				String ext = null;
				if (filter == filterCSV) {
					ext = ".csvz";
				} else if(filter == filterSerial) {
					ext = ".cpt";
				}
				
				// Ajouter l'extension
				file = new File(file.getAbsolutePath() + ext);
			}
			
			try {
				
				// Redéfinir le DAO
				if (filter.equals(filterCSV)) {			// Fichier CSV
					DAOFactory.setFactory(
							new CacheDAOFactory(
									CsvDAO.newInstance(file)), true);
					
//				} else if (filter.equals(filterSerial)) {// Natif
//					DAOFactory.setFactory(
//							new CacheDAOFactory(new SerializeDAOFactory(file)),
//							true);
				} else {
					return;					// Filtre inconnu = ne rien faire
				}
				
				// Sauvegarder les données immédiatement
				DAOFactory.getFactory().save();
				
			} catch (IOException e) {
				LOGGER.log(
						Level.SEVERE,
						"Impossible de sauvegarder le fichier "+file.getName(),
						e);
			}
		}
	}
}
