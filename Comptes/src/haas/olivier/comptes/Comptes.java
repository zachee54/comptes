package haas.olivier.comptes;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.dao.cache.CacheDAOFactory;
import haas.olivier.comptes.dao.csv.CsvDAO;
import haas.olivier.comptes.gui.SimpleGUI;
import haas.olivier.comptes.info.UncaughtExceptionLogger;
import haas.olivier.info.DialogHandler;
import haas.olivier.util.NullPreferences;

public class Comptes implements Runnable {
	
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
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
	}// static
	
//	/** Nom de la clé déterminant le DAO à utiliser. */
//	public static final String DAO_NAME_PROPERTY = "source-format";
//	/** Nom de la valeur correspondant au DAO SerializeDAOFactory. */
//	public static final String DAO_NAME_SERIAL = "native";
//	/** Nom de la valeur correspondant au DAO CsvDAO. */
//	public static final String DAO_NAME_CSV = "csv";

	/** Nom de la clé déterminant le fichier (ou autre source) à utiliser. */
	public static final String SOURCE_NAME_PROPERTY = "source";

	public static void main(String[] args) {
		
		// Afficher les erreurs à l'écran
		displayUncaughtExceptions();
		
		// Lancer une nouvelle instance de l'application
		SwingUtilities.invokeLater(new Comptes(args));
	}// main
	
	/** Met en place un affichage des exceptions dans des boîtes de dialogue et
	 * un renvoi des exceptions non traitées vers le système de logs.
	 */
	private static void displayUncaughtExceptions() {
		
		// Afficher les logs importants à l'écran
		Logger.getLogger("").addHandler(new DialogHandler(null));
		
		// Intercepter les exceptions non traitées
		Thread.setDefaultUncaughtExceptionHandler(
				new UncaughtExceptionLogger());
	}// displayUncaughtExceptions
	
	/** Les préférences utilisateur. */
	private final Preferences prefs;
	
	/** Construit une instance de l'application Comptes.
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
		}// if
		
		// Ouvrir un fichier de comptes
		chooseFile(args);
	}// constructeur
	
	/** Choisit le fichier à ouvrir au lancement de l'application.
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
				loadCsvFile(file);						// Charger ce fichier
				fileInArg = true;
				break;									// Ignorer args suivants
			}// if
		}// for
		
		// Si pas de fichier de comptes spécifié en argument, ouvrir le dernier
		if (!fileInArg)
			loadLastFile();
	}// chooseFile
	
	/** Ouvre le dernier fichier utilisé, d'après les préférences. */
	private void loadLastFile() {
		
		// Le nom du fichier à charger
		String filename = prefs.get(SOURCE_NAME_PROPERTY, "");
		
		// S'il y a un nom de fichier spécifié, on l'ouvre
		if (!filename.isEmpty())
			loadCsvFile(new File(filename));
	}// loadLastFile
	
	/** Charge un fichier de comptes au format "CSV".
	 * <p>
	 * Si le fichier n'existe pas, la méthode ne fait rien.
	 * 
	 * @param file	Le fichier à charger.
	 */
	private boolean loadCsvFile(File file) {
		try {
			// Essayer de charger le fichier s'il existe
			if (file.exists()) {
				DAOFactory.setFactory(
						new CacheDAOFactory(CsvDAO.newInstance(file)),
						false);
				return true;
			}// if

		} catch (IOException e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE,
					"Impossible de charger le fichier " + file, e);
		}// try
		
		// Arrivée ici, le chargement a échoué
		return false;
	}// loadFile
	
	/** Lance l'interface graphique. */
	public void run() {
		new SimpleGUI(prefs);
	}// run
}// class
