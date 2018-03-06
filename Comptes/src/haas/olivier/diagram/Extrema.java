package haas.olivier.diagram;

/** Un calculateur des valeurs extrêmes du modèle de diagramme.
 * 
 * @author Olivier Haas
 */
class Extrema {
	
	/** Le modèle du diagramme. */
	private final DiagramModel model;
	
	/** La valeur minimale actuelle. */
	private Number min;
	
	/** La valeur maximale actuelle. */
	private Number max;
	
	/** Construit un calculateur de valeurs extrêmes pour le modèle de diagramme
	 * spécifié.
	 * 
	 * @param model	Le modèle de diagramme.
	 */
	public Extrema(DiagramModel model) {
		this.model = model;
		updateExtrema();
	}// constructeur
	
	/** Calcule les valeurs extrêmes du modèle et les affecte à {@link #min} et
	 * {@link #max}.
	 * <p>
	 * S'il n'y a pas de valeur, la méthode affecte <code>null</code> aux deux
	 * variables.
	 */
	public void updateExtrema() {
		min = max = null;
		Iterable<Serie> series = model.getSeries();
		for (Serie serie : series) {
			
			// Ne pas traiter les séries auxquelles l'échelle est insensible
			if (!serie.isScaled())
				continue;

			for (Object x : model.getXValues()) {
				Number value = serie.get(x);
				
				// Ne pas traiter les valeurs null
				if (value == null)
					continue;

				double doubleValue = value.doubleValue();
				if (min == null || min.doubleValue() > doubleValue)
					min = doubleValue;
				if (max == null || max.doubleValue() < doubleValue)
					max = doubleValue;
			}// for x
			
			// Valeurs par défaut s'il n'y avait aucune série
			if (min == null) {
				min = 0.0;
				max = 1.0;
			}// if empty
		}// for serie
	}// updateExtrema

	public double getMin() {
		return min.doubleValue();
	}// getMin
	
	public double getMax() {
		return max.doubleValue();
	}// getMax
}
