/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import haas.olivier.comptes.dao.PropertiesDAO;
import haas.olivier.diagram.DiagramMemento;

/** Un objet d'accès aux propriétés en cache.
 * 
 * @author Olivier HAAS
 */
class CachePropertiesDAO implements PropertiesDAO {
	
	/** Les propriétés de chaque diagramme en fonction de leurs noms. */
	private final Map<String, DiagramMemento> diagramProperties;
	
	/** Indique si les propriétés ont été modifiées et doivent être
	 * sauvegardées.
	 */
	private boolean mustBeSaved = false;
	
	/** Crée un objet d'accès aux propriétés.
	 * 
	 * @param diagramProperties
	 * 			Les propriétés des diagrammes, en fonction du nom du diagramme.
	 */
	public CachePropertiesDAO(CacheablePropertiesDAO props) {
		this.diagramProperties = new HashMap<>(props.getDiagramProperties());
	}// constructeur
	
	@Override
	public DiagramMemento getDiagramProperties(String name) {
		DiagramMemento memento = diagramProperties.get(name);
		return memento == null
				? new DiagramMemento(name, Collections.<Integer>emptyList(),
						Collections.<Integer>emptySet())
				: memento;
	}// getDiagramProperties
	
	@Override
	public void setDiagramProperties(String name, DiagramMemento memento) {
		diagramProperties.put(name, memento);
		mustBeSaved = true;
	}// setDiagramProperties

	@Override
	public Iterable<String> getDiagramNames() {
		return diagramProperties.keySet();
	}// getDiagramNames
	
	/** Efface toutes les propriétés. */
	void erase() {
		diagramProperties.clear();
	}// clear
	
	/** Indique si les propriétés ont été modifiées et doivent être
	 * sauvegardées.
	 */
	boolean mustBeSaved() {
		return mustBeSaved;
	}// mustBeSaved
	
	/** Indique que les propriétés ont été sauvegardées. */
	void setSaved() {
		mustBeSaved = false;
	}// setSaved
}
