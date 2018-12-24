/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Un itérateur de séries qui renvoient des valeurs cumulées.
 * 
 * @author Olivier Haas
 */
class AggregateSerieIterator implements Iterator<Serie> {

	/** L'itérateur non cumulé. */
	private final Iterator<Serie> delegate;
	
	/** Les valeurs des abscisses à utiliser. */
	private final Object[] xValues;
	
	/** Les valeurs cumulées au stade actuel de l'itération. */
	private Map<Object, Number> values = new HashMap<>();
	
	/** Construit un itérateur de séries qui cumule les valeurs.
	 * 
	 * @param delegate	L'itérateur non cumulé.
	 */
	public AggregateSerieIterator(Iterator<Serie> delegate, Object[] xValues) {
		this.delegate = delegate;
		this.xValues = xValues;
	}// constructeur
	
	@Override
	public boolean hasNext() {
		return delegate.hasNext();
	}// hasNext

	@Override
	public Serie next() {
		Serie serie = delegate.next();
		values = aggregateValues(serie);
		return new Serie(serie.id, serie.toString(), serie.getColor(),
				serie.isScaled(), values);
	}// next
	
	/** Ajoute aux cumuls actuels les valeurs d'une nouvelle série.
	 * 
	 * @param serie	La nouvelle série à ajouter.
	 * @return		Un nouvelle Map de valeurs cumulées.
	 */
	private Map<Object, Number> aggregateValues(Serie serie) {
		Map<Object, Number> newValues = new HashMap<>();
		for (Object xValue : xValues)
			newValues.put(xValue, aggregateValue(serie, xValue));
		return newValues;
	}// aggregateValues
	
	/** Renvoie une valeur cumulée.
	 * 
	 * @param serie		La série contenant la valeur à ajouter aux précédentes.
	 * @param xValue	L'abscisse à laquelle correspond la valeur à calculer.
	 * 
	 * @return			Le cumul précédent à l'abscisse <code>xValue</code>,
	 * 					augmenté de la valeur de <code>serie</code> à cette même
	 * 					abscisse. Les valeurs <code>null</code> comptent pour
	 * 					zéro.
	 */
	private Number aggregateValue(Serie serie, Object xValue) {
		Number previous = values.get(xValue);
		Number serieValue = serie.get(xValue);
		return (previous == null ? 0 : previous.doubleValue())
				+ (serieValue == null ? 0 : serieValue.doubleValue());
	}// aggregateValue

}
