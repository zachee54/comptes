/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.cache.CacheableDAOFactory;
import haas.olivier.comptes.dao.cache.MultiCacheableDAOFactory;
import haas.olivier.comptes.dao.csv.CsvDAO;
import haas.olivier.comptes.dao.mysql.MySqlDAO;
import haas.olivier.comptes.gui.SimpleGUI;
import haas.olivier.comptes.info.UncaughtExceptionLogger;
import haas.olivier.info.DialogHandler;
import haas.olivier.util.NullPreferences;

public class Comptes implements Runnable {
	
	/** Le Logger de la classe. */
	private static final Logger LOGGER =
			Logger.getLogger(Comptes.class.getName());
	
	// Spécifier le Look&Feel
	static {
		try {
			// CrossPlatform (Metal)
//			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			
			// Thème alternatif pour le L&F Metal
//			javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new javax.swing.plaf.metal.DefaultMetalTheme());
			
			// System
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			// Nimbus
			try {
			    for (LookAndFeelInfo info :
			    	UIManager.getInstalledLookAndFeels()) {
			        if ("Nimbus".equals(info.getName())) {
			            UIManager.setLookAndFeel(info.getClassName());
			            break;
			        }
			    }
			} catch (Exception e) {
				UIManager.setLookAndFeel(
						UIManager.getCrossPlatformLookAndFeelClassName());
			}
			
			// Motif
//			UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			
			// GTK
//			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			
			// Windows
//			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			
			// Windows Classic
//			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
		} catch (ClassNotFoundException e1) {
			LOGGER.log(Level.CONFIG, "Look&Feel non trouvé", e1);
		} catch (InstantiationException e1) {
			LOGGER.log(Level.CONFIG, "Look&Feel impossible à instancier", e1);
		} catch (IllegalAccessException e1) {
			LOGGER.log(Level.CONFIG, "Accès impossible au Look&Feel", e1);
		} catch (UnsupportedLookAndFeelException e1) {
			LOGGER.log(Level.CONFIG, "Look&Feel non supporté", e1);
		}
	}

	/**
	 * Nom de la clé déterminant le fichier (ou autre source) à utiliser.
	 */
	public static final String SOURCE_NAME_PROPERTY = "source";

	public static void main(String[] args) {
		
		// Afficher les erreurs à l'écran
		displayUncaughtExceptions();
		
		// Lancer une nouvelle instance de l'application
		SwingUtilities.invokeLater(new Comptes(args));
	}
	
	/**
	 * Met en place un affichage des exceptions dans des boîtes de dialogue et
	 * un renvoi des exceptions non traitées vers le système de logs.
	 */
	private static void displayUncaughtExceptions() {
		
		// Afficher les logs importants à l'écran
		Logger.getLogger("").addHandler(new DialogHandler(null));
		
		// Intercepter les exceptions non traitées
		Thread.setDefaultUncaughtExceptionHandler(
				new UncaughtExceptionLogger());
	}
	
	/**
	 * Les préférences utilisateur.
	 */
	private final Preferences prefs;
	
	/**
	 * Construit une instance de l'application Comptes.
	 * 
	 * @param args	Si l'option <code>-nostore</code> est donnée en argument,
	 * 				les préférences utilisateurs ne sont pas lues depuis le
	 * 				systèmes et les modifications sur les préférences ne seront
	 * 				pas sauvegardées.
	 * 				<p>
	 * 				Si les arguments contiennent un nom de fichier existant,
	 * 				l'application essaye de l'ouvrir comme un fichier de
	 * 				comptes.
	 */
	private Comptes(String[] args) {
		
		// Instancier les préférences
		if (Arrays.asList(args).contains("-nostore")) {
			
			// Option nostore: préférences non stockées
			prefs = new NullPreferences();
			
		} else {
			
			// Préférences normales
			prefs = Preferences.userNodeForPackage(getClass());
		}
		
		// Ouvrir un fichier de comptes
		chooseFile(args);
	}
	
	/**
	 * Choisit le fichier à ouvrir au lancement de l'application.
	 * <p>
	 * Si <code>args</code> contient un nom de fichier existant, on essaye
	 * d'ouvrir ce fichier.<br>
	 * Sinon, on ouvre le dernier fichier enregistré dans les préférences.
	 * 
	 * @param args	Les arguments donnés en ligne de commande, comportant
	 * 				éventuellement un nom de fichier de comptes existant.
	 */
	private void chooseFile(String[] args) {
		
		// Charger le fichier spécifié en argument, s'il y en a un
		boolean fileInArg = false;						// Fichier spécifié ?
		for (String arg : args) {
			File file = new File(arg);
			if (file.exists()) { 						// Si fichier existant
				try {
					loadCacheable(loadCsvFile(file));	// Charger ce fichier
					fileInArg = true;
					break;								// Ignorer args suivants
					
				} catch (IOException e) {
					LOGGER.log(Level.WARNING,
							"Impossible de charger le fichier " + file, e);
				}
			}
		}
		
		// Si pas de fichier de comptes spécifié en argument, ouvrir le dernier
		if (!fileInArg)
			loadLastFile();
	}
	
	private void loadCacheable(CacheableDAOFactory cacheable) throws IOException {
		DAOFactory.setFactory(new CacheDAOFactory(cacheable));
	}
	
	/**
	 * Ouvre le dernier fichier utilisé, d'après les préférences.
	 */
	private void loadLastFile() {
		
		// Le nom du fichier à charger
		String sourceName = prefs.get(SOURCE_NAME_PROPERTY, "");
		
		try {
			DAOFactory.setFactory(new CacheDAOFactory(
					getCacheableFromProperty(sourceName)));
			
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					"Impossible de charger les comptes automatiquement", e);
		}
	}
	
	private CacheableDAOFactory getCacheableFromProperty(String sourceName)
			throws IOException {
		if (sourceName.startsWith("[multi]")) {
			Scanner scanner = new Scanner(sourceName);
			scanner.useDelimiter("[multi]");
			
			CacheableDAOFactory mainDAO =
					getCacheableFromProperty(scanner.next());
			CacheableDAOFactory backupDAO =
					getCacheableFromProperty(scanner.next());
			
			scanner.close();
			
			return new MultiCacheableDAOFactory(mainDAO, backupDAO);
			
		} else if (sourceName.startsWith("jdbc:mysql:")) {
			Scanner scanner = new Scanner(sourceName);
			scanner.useDelimiter(":");
			
			String host = scanner.next();
			int port = scanner.nextInt();
			String database = scanner.next();
			String username = scanner.next();
			StringBuilder password = new StringBuilder(scanner.next());
			while (scanner.hasNext()) {
				password.append(':');
				password.append(scanner.next());
			}
			scanner.close();
			
			return loadDatabase(
					host, port, database, username, password.toString());
			
		} else if (!sourceName.isEmpty()) {
			return loadCsvFile(new File(sourceName));
		}
		
		return null;
	}
	
	/**
	 * Charge un fichier de comptes au format "CSV".
	 * <p>
	 * Si le fichier n'existe pas, la méthode ne fait rien.
	 * 
	 * @param file	Le fichier à charger.
	 * 
	 * @throws IOException
	 */
	private CacheableDAOFactory loadCsvFile(File file) throws IOException {
		try {
			// Essayer de charger le fichier s'il existe
			if (file.exists()) {
				return CsvDAO.newInstance(file);
			}
			throw new FileNotFoundException(file.getAbsolutePath());

		} catch (IOException e) {
			throw new IOException(
					"Impossible de charger le fichier " + file, e);
		}
	}
	
	/**
	 * Charge les comptes depuis une base de données SQL.
	 * 
	 * @param host		L'adresse du serveur.
	 * @param port		Le numéro de port.
	 * @param database	Le nom de la base de données.
	 * @param username	Le nom d'utilisateur de la base de données.
	 * @param password	Le mot de passe de connexion à la base de données.
	 * 
	 * @return			Un <code>CacheableDAOFactory</code> prêt à l'emploi.
	 * 
	 * @throws IOException
	 */
	private CacheableDAOFactory loadDatabase(String host, int port,
			String database, String username, String password)
					throws IOException {
		try {
			return new MySqlDAO(host, port, database, username, password);
			
		} catch (IOException e) {
			throw new IOException(
					"Impossible de réouvrir la base de données", e);
		}
	}
	
	/**
	 * Lance l'interface graphique.
	 */
	public void run() {
		new SimpleGUI(prefs);
	}
}
