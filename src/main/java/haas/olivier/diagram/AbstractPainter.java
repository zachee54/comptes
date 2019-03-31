/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

/** Une classe abstraite de dessinateur de diagrammes.
 * 
 * @author Olivier HAAS
 */
abstract class AbstractPainter implements Painter {

	/** Le modèle du diagramme. */
	private final DiagramModel model;
	
	/** L'échelle du diagramme. */
	private final Echelle echelle;
	
	/** Construit un dessinateur abstrait de diagramme.
	 * 
	 * @param model		Le modèle de diagramme.
	 */
	AbstractPainter(DiagramModel model) {
		this(model, new Echelle(model));
	}// constructeur simple
	
	/** Construit un dessinateur abstrait de diagramme en utilisant une échelle
	 * particulière.
	 * 
	 * @param model		Le modèle de diagramme.
	 * @param echelle	L'échelle à utiliser.
	 */
	AbstractPainter(DiagramModel model, Echelle echelle) {
		this.model = model;
		this.echelle = echelle;
	}// constructeur
	
	@Override
	public DiagramModel getModel() {
		return model;
	}// getModel
	
	@Override
	public Echelle getEchelle() {
		return echelle;
	}// getEchelle
}
