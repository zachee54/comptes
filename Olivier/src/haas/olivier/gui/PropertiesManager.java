/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui;

import haas.olivier.gui.PropertiesController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;

/** Gestionnaire de préférences utilisateur.
 * Cette classe lit et écrit les préférences utilisateurs stockées de manière
 * persistante.
 * Typiquement, il s'agit de gérer les fichiers de configuration stockés dans le
 * répertoire personnel de l'utilisateur.
 *
 * @author Olivier HAAS
 */
public class PropertiesManager {
	
	/** Fichier de préférences imposé pour toutes les instances.
	 * Cette propriété est utile en environnement de test pour éviter de créer
	 * ou manipuler le vrai fichier de préférences.
	 */
	private static File forcedPrefFile = null;
	
	/** Impose un fichier de préférences prédéfini à toutes les nouvelle
	 * instances.
	 * @param file	Le fichier de préférences prédéfini.
	 */
	public static void forcePrefFile(File file) {
		forcedPrefFile = file;
	}
	
	/** Fichier de préférences utilisateur. */
	private final File prefFile;
	
	/** Commentaire à insérer dans le fichier de préférences. */
	private String comment;
	
	/** Collection des contrôleurs de propriétés. */
	private ArrayList<PropertiesController> controllers =
			new ArrayList<PropertiesController>();
	
	/** Les préférences utilisateur. */
	private Properties userProps = null;
	
	/** Construit un gestionnaire de propriétés.
	 * Les propriétés seront sauvegardées dans un sous-répertoire du répertoire
	 * personnel de l'utilisateur (/home/... sur Linux ou C:\Documents and
	 * Settings\... sous Windows XP).
	 * 
	 * @param defaut	Un fichier de préférences par défaut.
	 * 
	 * @param subfolder	Le nom du sous-répertoire à créer et utiliser dans le
	 * 					répertoire personnel de l'utilisateur.
	 * 
	 * @param prefFileName
	 * 					Le nom du fichier à utiliser pour lire et stocker les
	 * 					préférences.
	 * 
	 * @param comment	Le commentaire à insérer dans le fichier de préférences.
	 */
	public PropertiesManager(File defaut, String subfolder,
			String prefFileName, String comment) {
		this(
				defaut,										// Props par défaut
				new File(
						new File(
								(String) System.getProperty("user.home"),//Home
								subfolder),					// Sous-répertoire
						prefFileName),						// Fichier de prefs
				comment);									// Commentaire
	}// constructeur simple
	
	/** Construit un gestionnaire de propriétés.
	 * Les propriétés seront sauvegardées dans le fichier spécifié.
	 * 
	 * @param defaut	Un fichier de préférences par défaut.
	 * 
	 * @param pref		Le fichier de stockage des préférences (peut ne pas
	 * 					exister).
	 * 
	 * @param comment	Le commentaire à insérer dans le fichier de préférences.
	 */
	public PropertiesManager(File defaut, File pref, String comment) {
		this.comment = comment;
		this.prefFile = (forcedPrefFile == null)	// Fichier imposé ?
				? pref								// Le fichier spécifié
				: forcedPrefFile;					// Le fichier imposé
		
		// Charger les propriétés par défaut
		InputStream defaultIn = null;				// Flux d'entrée par défaut
		try {
			defaultIn = new FileInputStream(defaut);	// Fichier "hardcodé"
			Properties defaultProps = new Properties();	// Propriétés par défaut
			defaultProps.load(defaultIn);				// Charger
			userProps = new Properties(defaultProps);	// Props user + défaut
		} catch (Exception e) {
			userProps = new Properties();				// Props user seules
		} finally {
			if (defaultIn != null) {
				try {
					defaultIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}// try

		// Charger les propriétés utilisateur
		InputStream in = null;						// Flux d'entrée utilisateur
		try {
			if (prefFile.exists()) {
				in = new FileInputStream(prefFile);
				userProps.load(in);						// Charger
			}
		} catch (Exception e) {
		} finally {
			if (in != null) {
				try {
					in.close();							// Fermer le flux
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}// try
	}// constructeur
	
	/** @return	Les préférences utilisateurs, ou des préférences vides si les
	 * 			préférences utilisateur et les préférences par défaut n'ont pas
	 * 			pu être chargées.
	 */
	public Properties getProperties() {
		if (userProps == null) {			// Instancier par défaut si null
			userProps = new Properties();
		}
		return userProps;
	}// getProperties
	
	/** Enregistre un nouveau contrôleur de propriétés. */
	public void addController(PropertiesController c) {
		controllers.add(c);
	}
	
	/** Sauvegarde les propriétés. 
	 * @throws IOException */
	public void saveProperties() throws IOException {
		
		// Mettre à jour toutes les propriétés
		for (PropertiesController c : controllers) {
			c.setAllProperties();
		}
		
		// Créer les répertoires parents s'ils n'existent pas
		File parent = prefFile.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		
		// Sauvegarder
		OutputStream out = null;						// Flux de sortie
		try {
			out = new FileOutputStream(prefFile);
			userProps.store(out, comment);				// Sauvegarder
		} catch (Exception e) {
			throw new IOException(
					"Erreur lors de l'enregistrement des préférences " +
					"utilisateur");
		} finally {
			if (out != null) {
				try {
					out.close();						// Fermer le flux
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}// try
	}// saveProperties
}
