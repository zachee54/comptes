package haas.olivier.comptes.gui.diagram;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.diagram.DiagramAndAxisComponent;
import haas.olivier.diagram.DiagramFactory;
import haas.olivier.diagram.DiagramModel;
import haas.olivier.diagram.Serie;
import haas.olivier.diagram.SimpleDiagramModel;
import haas.olivier.util.Month;

/**
 * Une classe utilitaire construisant des diagrammes propres à l'application
 * <code>Comptes</code>.
 *
 * @author Olivier Haas
 */
public class ComptesDiagramFactory {

	/**
	 * Construit un diagramme affichant les moyennes glissantes des comptes
	 * budgétaires.
	 * 
	 * @throws IOException
	 */
	public static DiagramAndAxisComponent newMoyenne() throws IOException {
		return DiagramFactory.newCourbe(newModel(false, "moyennes",
				(c, month) -> {
					BigDecimal montant = c.getMoyenne(month);

					// Pour les dépenses, on préfère des montants en positif
					switch (c.getType()) {
					case DEPENSES:
					case DEPENSES_EN_EPARGNE:
						return montant.negate();
					default:
						return montant;
					}
				}));
	}
	
	/**
	 * Construit un diagramme affichant les soldes des comptes bancaires sous
	 * forme d'aires cumulées. Il donne une idée de l'évolution du patrimoine.
	 * 
	 * @throws IOException
	 */
	public static DiagramAndAxisComponent newPatrimoine() throws IOException {
		return DiagramFactory.newAire(newModel(true, "patrimoine",
				(c, month) -> c.getSoldeAVue(month)));
	}
	
	/**
	 * Crée un modèle de diagramme à partir de suivis des comptes et lui
	 * applique les propriétés précédemment enregistrées.
	 * 
	 * @param bancaire	<code>true</code> s'il faut prendre uniquement les
	 * 					comptes bancaires, <code>false</code> s'il faut
	 * 					prendre uniquement les comptes budgétaires.
	 * 
	 * @param name		Le nom sous lequel ont été enregistrées les propriétés
	 * 					du diagramme.
	 * 
	 * @param provider	Le fournisseur des suivis à utiliser : soldes à vue,
	 * 					moyennes, soldes théoriques, etc.
	 * 
	 * @throws IOException
	 */
	private static DiagramModel newModel(boolean bancaire, String name,
			BiFunction<Compte, Month, BigDecimal> provider) throws IOException {
		
		// Construire le modèle
		DiagramModel model = newChronoModel();
		DAOFactory factory = DAOFactory.getFactory();
		for (Compte compte : factory.getCompteDAO().getAll()) {
			if (compte.getType().isBancaire() == bancaire) {
				model.add(new Serie(
						compte.getId(), compte.getNom(), compte.getColor(),
						true, getSuivi(compte, provider)));
			}
		}
		
		// Appliquer les propriétés
		model.getOrdener().setMemento(
				factory.getPropertiesDAO().getDiagramProperties(name));
		
		return model;
	}
	
	/**
	 * Construit un modèle de diagramme contenant tous les mois comme
	 * étiquettes, et aucune série.
	 */
	private static DiagramModel newChronoModel() {
		Month debut = DAOFactory.getFactory().getDebut();
		Month today = new Month();
		List<Month> months = new ArrayList<Month>();
		for (Month month = debut; !month.after(today); month = month.getNext())
			months.add(month);
		return new SimpleDiagramModel(months.toArray());
	}
	
	/**
	 * Renvoie une collection des suivis (les montants) d'un compte sur
	 * l'ensemble de la période disponible.
	 * 
	 * @param compte	Le compte dont on veut collecter le suivi.
	 * 
	 * @param montantProvider
	 * 					L'objet d'accès aux données de suivi souhaitées :
	 * 					soldes à vue, soldes théoriques ou moyennes glissantes
	 * 					selon le cas. 
	 * 
	 * @return			Une nouvelle collection contenant les données de suivi.
	 */
	private static Map<Object, Number> getSuivi(Compte compte,
			BiFunction<Compte, Month, BigDecimal> montantProvider) {
		Month debut = DAOFactory.getFactory().getDebut();
		Month today = new Month();
		
		Map<Object, Number> suivi = new HashMap<>();
		for (Month month = debut; !month.after(today); month = month.getNext())
			suivi.put(month, montantProvider.apply(compte, month));
		
		return suivi;
	}
}
