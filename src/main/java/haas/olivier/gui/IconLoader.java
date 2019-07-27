/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui;

import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

/**
 * Classe statique pour le chargement des images.
 * 
 * @author Olivier HAAS
 */
public class IconLoader {
	
	private IconLoader() {
	}
	
	/**
	 * Charge une image si elle est disponible.
	 * 
	 * @param path	Le chemin de l'image à charger.
	 * @return		Une image, ou <code>null</code>.
	 */
	public static Image loadImage(String path) {
		ImageIcon icon = loadIcon(path);
		return icon == null ? null : icon.getImage();
	}
	
	/**
	 * Charge une icône si elle est disponible.
	 * 
	 * @param path	Le chemin de l'image à charger.
	 * @return		Une icône, ou <code>null</code>.
	 */
	public static ImageIcon loadIcon(String path) {
		return loadIcon(path, null);
	}
	
	/**
	 * Charge une icône si elle est disponible.
	 * 
	 * @param path			Le chemin de l'image à charger.
	 * @param description	La description à donner à l'image.
	 * @return				Une icône.
	 */
	public static ImageIcon loadIcon(String path, String description) {
		java.net.URL imgURL = IconLoader.class.getResource(path);
		if (imgURL == null) {
			Logger.getLogger(IconLoader.class.getName()).log(
					Level.CONFIG, "Impossible de charger l''image {0}", path);
			return null;
		} else {
			return new ImageIcon(imgURL, description);
		}
	}
}
