package haas.olivier.info;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/** Un récepteur de messages mettant à jour un fichier de log.
 * <p>
 * Seuls les avertissements et les erreurs sont affichés ; les informations sont
 * ignorées.
 * 
 * @author Olivier HAAS
 */
public class LogMessageListener implements MessageListener {
	
	/** Le fichier de log. */
	private final File log;
	
	/** Construit un récepteur d'erreurs alimentant le fichier de log spécifié.
	 * 
	 * @param log	Un fichier.
	 */
	public LogMessageListener(File log) {
		this.log = log;
	}

	/** Ajoute l'exception au fichier de log en tant qu'erreur en précisant
	 * l'horodatage. */
	@Override
	public void error(String message, Throwable e) {
		writeLog("Erreur: "+message, e);
	}// error
	
	/** Ajoute l'exception au fichier de log en tant qu'avertissement en
	 * précisant l'horodatage. */
	@Override
	public void warning(String message, Throwable e) {
		writeLog("Avertissement: "+message, e);
	}// warning
	
	/** Aucune implémentation. */
	@Override
	public void conseil(String message) {
	}
	
	/** Aucune implémentation. */
	@Override
	public void info(String message) {
	}

	/** Complète le fichier de log avec un horodatage, un message et la trace
	 * de l'exception.
	 * 
	 * @param text	Le texte du message qui décrit le type d'événement.
	 * @param e		L'exception à décrire dans le log.
	 */
	private synchronized void writeLog(String text, Throwable e) {
		
		// Créer un fichier temporaire
		File tmp;
		String tmpName = log.getAbsolutePath();			// Partir du nom du log
		do {
			tmpName += "-tmp";							// Ajouter une extension
		} while ((tmp = new File(tmpName)).exists());	// Autant que nécessaire
		
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(	// Écriture
					new FileWriter(tmp)));
			
			// Copier les données antérieures du fichier log dans le temporaire
			try {
				in = new BufferedReader(new FileReader(log));	// Lecture
				String s;
				while ((s = in.readLine()) != null) {			// Copie
					out.println(s);
				}
			} catch (FileNotFoundException e1) {
				// Log non trouvé: partir d'un log vide
			}// try
			
			// Ajouter la trace de l'exception actuelle
			out.println("--- " + new Date() + " ---");	// Horodatage
			out.print(text+": ");						// Type d'événement
			e.printStackTrace(out);						// Trace de l'exception
			out.println();								// Passer une ligne
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (in != null)
				try {in.close();} catch (IOException e1) {}
			if (out != null)
				out.close();
		}// try
		
		// Remplacer l'ancien log par le nouveau
		if (!log.exists() || log.delete())
			tmp.renameTo(log);
	}// writeLog
}
