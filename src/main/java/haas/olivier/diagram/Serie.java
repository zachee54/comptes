/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import java.awt.Color;
import java.util.Map;

/** Une série de données pour les diagrammes.
 * 
 * @author Olivier HAAS
 */
public class Serie {
	
	/** L'identifiant de la série, servant par exemple à déterminer, sauvegarder
	 * ou restaurer l'ordre des séries, et quelles séries doivent être masquées.
	 * <p>
	 * Le contrat déterminé par le diagramme et son modèle peut exiger que cet
	 * identifiant est en principe unique (deux séries différentes ont des
	 * identifiants différents) et persistant (si on instancie plusieur fois un
	 * diagramme, y compris dans des machines virtuelles différentes, le même
	 * identifiant désigne toujours les mêmes données). 
	 */
	public final int id;
	
	/** Le nom de la série de données. */
	private final String name;
	
	/** La couleur. */
	private final Color color;
	
	/** Indique si la série doit être prise en compte pour le calcul de
	 * l'échelle.
	 */
	private final boolean scaled;
	
	/** Les données. */
	private final Map<?,Number> data;
	
	/** Construit une série de données.
	 * 
	 * @param id			L'identifiant de la série.
	 * @param name			Le nom de la série de données.
	 * @param color			La couleur de la série.
	 * @param scaled		<code>true</code> si l'échelle doit tenir compte des
	 * 						valeurs de la série, <code>false</code> sinon.
	 * @param data			Les données. Les clés sont les étiquettes en
	 * 						abscisses.
	 */
	public <T> Serie(int id, String name, Color color, boolean scaled,
			Map<T,Number> data) {
		this.id = id;
		this.name = name;
		this.color = color;
		this.scaled = scaled;
		this.data = data;
	}// constructeur
	
	/** Renvoie la valeur de la série à l'abscisse spécifiée.
	 * 
	 * @return	La valeur de la série à cette abscisse, ou <code>null</code> si
	 * 			aucune valeur n'a été définie dans la série pour cette abscisse.
	 */
	public Number get(Object xValue) {
		return data.get(xValue);
	}// get
	
	/** Renvoie la couleur de la série. */
	Color getColor() {
		return color;
	}// getColor
	
	/** Indique si la série doit être prise en compte pour le calcul de
	 * l'échelle.
	 */
	public boolean isScaled() {
		return scaled;
	}// isScaled
	
	/** Renvoie le nom de la série. */
	@Override
	public String toString() {
		return name;
	}// toString
}
