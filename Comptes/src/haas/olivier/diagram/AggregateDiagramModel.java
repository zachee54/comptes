package haas.olivier.diagram;

import java.util.Iterator;

/** Un modèle de diagramme qui donne une vue cumulée des séries pendant leur
 * itération.
 * 
 * @author Olivier Haas
 */
class AggregateDiagramModel implements DiagramModel, Iterable<Serie> {

	/** Le modèle d'origine. */
	private final DiagramModel delegate;
	
	/** Construit un modèle de diagramme présentant une vue cumulée d'un autre
	 * modèle.
	 * 
	 * @param delegate	Le modèle d'origine, non cumulé.
	 */
	public AggregateDiagramModel(DiagramModel delegate) {
		this.delegate = delegate;
	}// constructeur
	
	@Override
	public Iterable<Serie> getSeries() {
		return this;
	}// getSeries

	@Override
	public Iterator<Serie> iterator() {
		return new AggregateSerieIterator(
				delegate.getSeries().iterator(), delegate.getXValues());
	}// iterator

	@Override
	public DiagramModel getAggregateView() {
		return this;
	}

	@Override
	public void add(Serie serie) {
		delegate.add(serie);
	}

	@Override
	public void remove(Serie serie) {
		delegate.remove(serie);
	}

	@Override
	public void setXValues(Object[] xValues) {
		delegate.setXValues(xValues);
	}

	@Override
	public Object[] getXValues() {
		return delegate.getXValues();
	}

	@Override
	public SeriesOrdener getOrdener() {
		return delegate.getOrdener();
	}

	@Override
	public DiagramModelObservable getObservable() {
		return delegate.getObservable();
	}

}
