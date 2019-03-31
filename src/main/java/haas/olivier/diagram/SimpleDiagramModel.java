/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

/** Un modèle simple pour les diagrammes.
 * 
 * @author Olivier HAAS
 */
public class SimpleDiagramModel implements DiagramModel {

	/** Les données en abscisses. */
	private Object[] xValues;
	
	/** L'ordonnateur des séries du modèle. */
	private SeriesOrdener ordener;
	
	/** Construit un nouveau modèle de diagramme.
	 * 
	 * @param xValues	Les objets à utiliser comme étiquettes des abscisses.
	 */
	public SimpleDiagramModel(Object[] xValues) {
		ordener = new SeriesOrdener();
		setXValues(xValues);
	}// constructeur
	
	@Override
	public void add(Serie serie) {
		ordener.add(serie);
	}// addSerie

	@Override
	public void remove(Serie serie) {
		ordener.remove(serie);
	}// removeSerie

	@Override
	public Iterable<Serie> getSeries() {
		return ordener;
	}// getSeries

	@Override
	public void setXValues(Object[] xValues) {
		this.xValues = xValues;
		getObservable().dataChanged();
	}// setXValues

	@Override
	public Object[] getXValues() {
		return xValues;
	}// getXValues
	
	@Override
	public DiagramModelObservable getObservable() {
		return ordener.getObservable();
	}// getObservable

	@Override
	public SeriesOrdener getOrdener() {
		return ordener;
	}// getOrdener

	@Override
	public DiagramModel getAggregateView() {
		return new AggregateDiagramModel(this);
	}// getAggregateView
}