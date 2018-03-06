package haas.olivier.diagram;

import static java.lang.Math.floor;
import static java.lang.Math.log10;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;

/** Une échelle pour les ordonnées d'un diagramme.
 * <p>
 * Il s'agit de la valeur minimum, la valeur maximum, l'ordre de grandeur, le
 * pas entre deux graduations...
 * <p>
 * Les instances de cette classe sont immuables.
 * 
 * @author Olivier HAAS
 */
class Echelle {
	
	/** Le calculateur de valeurs extrêmes. */
	private final Extrema extrema;
	
	/** La valeur plafond. */
	private BigDecimal sup;
	
	/** La valeur plancher. */
	private BigDecimal inf;
	
	/** Les graduations. */
	private Collection<BigDecimal> graduations = new ArrayList<>();
	
	/** Construit une échelle.
	 * <p>
	 * Par convention, les valeurs extrêmes sont <code>0</code> et 
	 * <code>1</code> à l'instanciation.
	 * 
	 * @param model	Le modèle du diagramme.
	 */
	public Echelle(DiagramModel model) {
		this.extrema = new Extrema(model);
		updateGraduations();
	}// constructeur

	/** Recalcule l'échelle en fonction des valeurs extrêmes du modèle. */
	public void updateGraduations() {
		
		// Les extrema
		extrema.updateExtrema();
		double min = extrema.getMin();
		double max = extrema.getMax();
		double range = max - min;
		
		// Définir les bornes de l'échelle
		int precision = getPrecision(range);
		updateRange(min, max, precision);
		
		// Créer les graduations
		BigDecimal step = getStep(range, precision);
		graduations.clear();
		for (BigDecimal g = getFirstGraduation(step); g.compareTo(sup) <= 0;
				g = g.add(step)) {
			graduations.add(g);
		}// for
	}// createGraduations
	
	/** Renvoie le logarithme d'un nombre en base 10, arrondi à l'entier
	 * inférieur.
	 * <p>
	 * Il s'agit du nombre de zéros qu'a la puissance de 10 immédiatement
	 * inférieure à ce nombre.<br>
	 * Ou encore : le plus grand entier <code>n</code> tel que
	 * <code>10<sup>n</sup> <= d</code>.
	 */
	private int getPrecision(double d) {
		return (int) floor(log10(d));
	}// getPrecision
	
	/** Redéfinit les bornes de l'échelle.<br>
	 * Cette méthode modifie {@link #inf} et {@link #sup}.
	 * <p>
	 * Les bornes sont obtenues en arrondissant les extrêmes vers l'extérieur de
	 * l'intervalle, au dixième de la précision spécifiée. C'est-à-dire que les
	 * bornes sont des multiples de <code>10<sup>precision-1</code>.
	 * 
	 * @param min		La valeur minimale du modèle de données.
	 * @param max		La valeur maximale du modèle de données.
	 * @param precision	La précision donnant l'ordre de grandeur de l'amplitude.
	 */
	private void updateRange(double min, double max, int precision) {
		inf = new BigDecimal(min).setScale(-precision+1, RoundingMode.FLOOR);
		sup = new BigDecimal(max).setScale(-precision+1, RoundingMode.CEILING);
	}// createRange
	
	/** Renvoie un pas de graduation adapté.<br>
	 * On calcule l'ordre de grandeur de l'amplitude. Si les extrêmes sont trop
	 * rapprochées en comparaison de cet ordre de grandeur, il y aura trop peu
	 * de graduations. On renvoie alors un pas plus petit en le divisant par 2,
	 * 5, 10 ou des multiples.
	 * <p>
	 * L'algorithme garantit en principe d'avoir 4 à 10 graduations sur
	 * l'amplitude de l'échelle.
	 */
	private BigDecimal getStep(double range, int precision) {
		BigDecimal scale = BigDecimal.ONE.scaleByPowerOfTen(precision);
		
		// Selon le rapport entre l'amplitude et son ordre de grandeur
		double rapport = range/scale.doubleValue();
		if (rapport >= 5) {
			// 5 à 10 graduations, c'est assez
			return scale;
			
		} else if (rapport >= 2) {
			// 2 à 5 graduations : on les subdivise en deux
			return scale.divide(new BigDecimal(2));
			
		} else if (rapport >= 1) {
			// 1 à 2 graduations : on les subdivise en cinq
			return scale.divide(new BigDecimal(5));
			
		} else {
			// 0 à 1 graduation : on les subdivise en dix
			return scale.movePointLeft(1);
		}// if
	}// getStep
	
	/** Renvoie la première graduation, calculée à partir de la borne inférieure
	 * de l'échelle et le pas de graduation.
	 * 
	 * @param step	Le pas de graduation.
	 * @return		Le multiple de <code>step</code> immédiatement supérieur ou
	 * 				égal à {@link #inf}.
	 */
	private BigDecimal getFirstGraduation(BigDecimal step) {
		return step.multiply(inf.divide(step, 0, RoundingMode.CEILING));
	}// getFirstGraduation
	
	/** Renvoie le minimum de l'échelle. */
	public double getMin() {
		return inf.doubleValue();
	}// getMin
	
	/** Renvoie le maximum de l'échelle. */
	public double getMax() {
		return sup.doubleValue();
	}// getMax

	/** Renvoie les graduations.
	 * 
	 * @return	Un itérable parcourant les graduations.
	 */
	public Iterable<BigDecimal> getGraduations() {
		return graduations;
	}// getGraduations
	
	/** Renvoie l'ordonnée d'un point, au sens de la localisation du composant,
	 * correspondant à la valeur spécifiée, au sens des données du diagramme.
	 * <p>
	 * Cette méthode est utile pour dessiner le diagramme à la hauteur
	 * correspondant à la valeur que l'on veut rendre graphiquement.
	 * 
	 * @param number	La valeur au sens des données du diagramme.
	 * @param height	La hauteur du composant.
	 * 
	 * @return			Un entier compris entre 0 (quand <code>value</code> est
	 * 					la valeur la plus élevée de l'echelle) et
	 * 					<code>height</code> (quand <code>value</code> est la
	 * 					valeur la moins élevée de l'echelle.)<br>
	 * 					Si <code>value</code> est au-delà des bornes de
	 * 					l'echelle, le résultat n'est pas entre
	 * 					<code>0</code> et <code>height</code>.
	 */
	public int getYFromValue(Number number, int height) {
		return getYFromValue(number == null ? 0 : number.doubleValue(), height);
	}// getYFromValue Number
	
	/** Renvoie l'ordonnée d'un point au sens de la localisation d'un composant
	 * graphique, correspondant à la valeur spécifiée au sens des données du
	 * diagramme.
	 * <p>
	 * Cette méthode est utile pour dessiner le diagramme à la hauteur
	 * correspondant à la valeur que l'on veut rendre graphiquement.
	 * 
	 * @param value		La valeur au sens des données du diagramme.
	 * @param height	La hauteur du composant.
	 * 
	 * @return			Un entier compris entre 0 (quand <code>value</code> est
	 * 					la valeur la plus élevée de l'echelle) et
	 * 					<code>height</code> (quand <code>value</code> est la
	 * 					valeur la moins élevée de l'echelle.)<br>
	 * 					Si <code>value</code> est au-delà des bornes de
	 * 					l'echelle, le résultat n'est pas entre
	 * 					<code>0</code> et <code>height</code>.
	 */
	public int getYFromValue(double value, int height) {
		return (int) ((height - 1)
				* (getMax() - value)
				/ (getMax() - getMin()));
	}// getYFromValue
}
