package haas.olivier.info;

import java.io.PrintWriter;

/** Un récepteur de messages chargé d'avertir l'utilisateur.
 * <p>
 * Cette implémentation de base utilise la console pour afficher les messages.
 * 
 * @author Olivier HAAS
 */
public class ConsoleMessageListener implements MessageListener {

	/** Affiche l'erreur sur System.err.
	 * 
	 * @param message	Le message à afficher.
	 * @param e			L'exception à traiter.
	 */
	@Override
	public void error(String message, Throwable e) {
		display("Erreur: "+message, e);
	}// error
	
	/** Affiche l'avertissement sur System.err.
	 * 
	 * @param message	Le message à afficher.
	 * @param e 		L'exception à traiter.
	 */
	@Override
	public void warning(String message, Throwable e) {
		display("Avertissement: "+message, e);
	}// warning
	
	/** Aucune implémentation. */
	@Override
	public void conseil(String message) {
		System.out.println(message);
	}
	
	/** Aucune implémentation. */
	@Override
	public void info(String message) {
	}// info
	
	private void display(String text, Throwable e) {
		PrintWriter out = new PrintWriter(System.err, true);
		out.print(text + ": ");
		e.printStackTrace(out);
		out.close();
	}// display
}
