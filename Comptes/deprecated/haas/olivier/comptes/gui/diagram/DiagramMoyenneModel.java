package haas.olivier.comptes.gui.diagram;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBudget;
import haas.olivier.comptes.dao.CompteDAO;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.diagram.DiagramModel;
import haas.olivier.diagram.Serie;
import haas.olivier.util.Month;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Le modèle de données pour les diagrammes affichant des courbes représentant
 * les moyennes glissantes des postes budgétaires.
 *
 * @author Olivier HAAS
 */
public class DiagramMoyenneModel extends ChronoDiagramModel {
	
	/** La valeur minimale. */
	private Number min;
	
	/** La valeur maximale. */
	private Number max;
	
	/** Construit un modèle de diagramme en courbes représentant les moyennes
	 * glissantes.
	 * 
	 * @throws IOException
	 */
	public DiagramMoyenneModel() throws IOException {
	}// constructeur

	@Override
	List<Serie> createSeriesImpl() throws IOException {
		
		// Créer les séries
		List<Serie> series = new ArrayList<Serie>();	// Liste à compléter
		CompteDAO dao = DAOFactory.getFactory().getCompteDAO();
		for (Compte compte : dao.getAll()) {			// Pour chaque compte
			if (compte instanceof CompteBudget) {		// Compte budget uniqt
				Map<Month,Number> data =				// Collec des valeurs
						new HashMap<>();
				
				// Compléter les moyennes glissantes dans la collection
				for (Month month : getXValues()) {
					
					// Récupérer la moyenne
					Number value =						// La valeur
							((CompteBudget) compte).getMoyenne(month);
					
					// Modifier le signe si besoin
					switch (compte.getType()) {
					case DEPENSES:
					case DEPENSES_EN_EPARGNE:
						// Inverser pour avoir les dépenses en positif
						value = -value.doubleValue();
						break;
					default:
					}// switch
					
					data.put(month, value);				// Mémoriser
				}// for month
				
				// Créer la série
				series.add(new Serie(compte.getId(), compte.getNom(),
						compte.getColor(), true, getXValues(), data));
			}// if budget
		}// for compte
		return series;
	}// createSeries

	@Override
	protected void naturalSort(List<Serie> series) {
		Collections.sort(series, new Comparator<Serie>() {
			
			/** L'accès aux données sur les comptes. */
			private final CompteDAO dao =
					DAOFactory.getFactory().getCompteDAO();

			@Override
			public int compare(Serie o1, Serie o2) {
				try {
					return dao.get(o1.id).compareTo(dao.get(o2.id));
				} catch (IOException e) {
					return 0;
				}// try
			}// compare
			
		});
	}// naturalSort
	
	@Override
	protected void notifyObservers() {
		
		// Recalculer d'abord les extrema des données modifiées
		min = max = null;
		for (Serie serie : getSeries()) {
			if (serie.isScaled()) {
				for (Object x : getXValues()) {
					double value = serie.get(x).doubleValue();

					// Vérifier le minimum
					if (min == null || min.doubleValue() > value)
						min = value;

					// Vérifier le maximum
					if (max == null || max.doubleValue() < value)
						max = value;
				}// for x
			}// if sensibilité de l'échelle à cette série
		}// for serie
		
		// Avertir les observateurs
		super.notifyObservers();
	}// notifyObservers
	
	@Override
	public Number getMax() {
		// Tester null au cas où il n'y aurait aucune donnée
		return max == null ? 1.0 : max;
	}// getMax

	@Override
	public Number getMin() {
		// Tester null au cas où il n'y aurait aucune donnée
		return min == null ? 0.0 : min;
	}// getMin
}
