/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JPanel;

/** Document de l'interface graphique, pouvant être ouvert, fermé et sauvegardé.
 *
 * @author Olivier HAAS
 */
public abstract class Dossier {

	/** Le composant graphique représentant le dossier.
	 * Par défaut, il s'agit d'un <code>JPanel</code> vide.
	 */
	protected JComponent component = new JPanel();
	
	/** @return le composant graphique de ce dossier. */
	public JComponent getComponent() {
		return component;
	}
	
	/** Remplace le composant graphique représentant le dossier. */
	public void setComponent(JComponent component) {
		this.component = component;
	}
	
	/** Renvoie le fichier utilisé par ce dossier. */
	public abstract File getFile();
	
	/** Renvoie le nom du dossier. */
	public abstract String getName();
	
	/** Indique si le dossier a été modifié depuis sa dernière sauvegarde.<br>
	 * Cette indication permet de solliciter l'utilisateur avant de fermer
	 * l'application, si certains dossiers ont besoin d'être sauvegardés.
	 * 
	 * @return	<code>true</code> si le dossier a besoin d'être sauvegardé,
	 * 			<code>false</code> sinon.
	 */
	public abstract boolean shouldBeSaved();
	
	/** Indique si le dossier peut se sauvegarder directement ou s'il a besoin
	 * d'une destination (un nom de fichier, par exemple).
	 * 
	 * @return	<code>true</code> s'il peut être sauvegardé directement,
	 * 			<code>false</code> s'il a besoin d'une destination.
	 */
	public abstract boolean hasSaveDest();
	
	/** Sauvegarde le dossier. */
	public abstract void save() throws IOException;
	
	/** Sauvegarde sous le fichier spécifié. */
	public abstract void saveAs(File file) throws IOException;
}
