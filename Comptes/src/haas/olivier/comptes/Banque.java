package haas.olivier.comptes;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/** Une banque.
 * <p>
 * Cette classe sert à identifier les établissements bancaires dans lesquels
 * sont ouverts les comptes bancaires.
 * 
 * @author Olivier HAAS
 */
public class Banque {

	/** L'identifiant de la banque. */
	public final Integer id;
	
	/** Le nom de la banque. */
	public final String nom;
	
	/** L'icône représentant l'image de la banque. */
	private Icon icon;
	
	/** Le contenu binaire de l'image. Il s'agit du contenu du fichier image
	 * d'origine. */
	private final byte[] bytes;
	
	/** Construit une banque.
	 * 
	 * @param id		L'identifiant unique de la banque.
	 * @param nom		Le nom de la banque.
	 * @param bytes		Le contenu binaire de l'image. Il s'agit du fichier
	 * 					image d'origine.
	 */
	public Banque(Integer id, String nom, byte[] bytes) {
		this.id = id;
		this.nom = nom;
		this.bytes = bytes;
		
		// Créer l'icône
		try {
			// Lire l'image d'après le contenu binaire
			Image image = ImageIO.read(new ByteArrayInputStream(bytes));
			
			// Transformer en icône, sauf si l'image n'a pas pu être lue
			if (image != null)
				icon = new ImageIcon(image, nom);
			
		} catch (IOException e) {
			// Jamais soulevée puisu'on utilise ByteArrayInputStream
		}// try
	}// constructeur
	
	/** Renvoie l'icône de la banque. */
	public Icon getIcon() {
		return icon;
	}// getIcon
	
	/** Renvoie le contenu binaire de l'image utilisée pour cette banque. */
	public byte[] getBytes() {
		return bytes;
	}// getBytes
}
