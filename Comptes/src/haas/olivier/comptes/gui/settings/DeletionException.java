package haas.olivier.comptes.gui.settings;

/**
 * Une exception soulevée lors d'un problème de suppression d'une opération
 * permanente.
 *
 * @author Olivier Haas
 */
class DeletionException extends Exception {
	private static final long serialVersionUID = 8542506431544283004L;
	
	DeletionException(String message) {
		super(message);
	}
	
	DeletionException(Throwable cause) {
		super(cause);
	}
}
