/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui;

import javax.swing.ImageIcon;

/** Classe statique pour le chargement des images.
 * 
 * @author Olivier HAAS
 */
public class IconLoader {
	
	/** Charge une icône à partir du répertoire de cette classe.
	 * 
	 * @param path			Le chemin de l'image à charger.
	 * @param description	La description à donner à l'image.
	 * @return				Une icône.
	 */
	public static ImageIcon createImageIcon(String path, String description) {
		return createImageIcon(IconLoader.class, path, description);
	}
	
	/** Charge une icône.
	 * 
	 * @param clazz			La classe de référence pour le chemin de l'image.
	 * 						En effet, si <code>path</code> est un chemin
	 * 						relatif, il faut savoir dans quel répertoire on se
	 * 						place. Il s'agit alors du répertoire de la classe de
	 * 						l'objet <code>source</code>.
	 * 						Si <code>path</code> est un chemin absolu, alors ce
	 * 						paramètre n'a pas d'importance. Néanmoins, il ne
	 * 						peut pas être <code>null</code>.
	 * 						
	 * @param path			Le chemin de l'image à charger.
	 * 
	 * @param description	La description à donner à l'image.
	 * 
	 * @return				Une icône, ou <code>null</code> si l'icône n'a pas
	 * 						pu être chargée.
	 */
	public static ImageIcon createImageIcon(Class<?> clazz, String path,
			String description) {
		java.net.URL imgURL = clazz.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Impossible de charger l'image " + path);
			return null;
		}
	}// createImageIcon
}
