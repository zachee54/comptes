package haas.olivier.comptes;

/** Une exception indiquant qu'une donnée indispensable n'a pas été fournie lors
 * de l'instanciation d'une <code>Ecriture</code>.
 *
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class EcritureMissingArgumentException extends Exception {
	
	/** Construit une exception indiquant qu'une donnée indispensable n'a pas
	 * été fournie lors de l'instanciation d'une écriture.
	 * 
	 * @param message	Le message.
	 * @param id		L'identifiant de l'écriture dont l'instanciation a
	 * 					échoué, ou <code>null</code> s'il s'agissait d'une
	 * 					nouvelle écriture.
	 */
	EcritureMissingArgumentException(String message, Integer id) {
		super(message + (id == null ? "" : " (écriture n°" + id + ")") );
	}// constructeur
}
