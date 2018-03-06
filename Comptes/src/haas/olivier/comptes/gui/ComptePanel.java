package haas.olivier.comptes.gui;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.CompteBancaire;
import haas.olivier.comptes.Ecriture;
import haas.olivier.util.Month;
import haas.olivier.comptes.ctrl.EcritureController;
import haas.olivier.comptes.dao.DAOFactory;
import haas.olivier.comptes.gui.actions.CompteObservable;
import haas.olivier.comptes.gui.actions.DataObservable;
import haas.olivier.comptes.gui.actions.DataObserver;
import haas.olivier.comptes.gui.actions.MonthObservable;
import haas.olivier.comptes.gui.actions.SoldesObservable;
import haas.olivier.comptes.gui.table.EcrituresTableModel;
import haas.olivier.comptes.gui.table.FinancialTable;
import haas.olivier.comptes.gui.table.SearchTableModel;
import haas.olivier.comptes.gui.table.SuiviTableModel;
import haas.olivier.comptes.gui.table.SyntheseTableModel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
/** Un panel qui contient tous les composants utiles pour gérer les comptes.
 * Chaque instance est propre à un type de compte, ce qui permet de séparer la
 * vision utilisateur en fonction des types.
 * Pour des raisons de simplicité, cette classe implémente aussi un observable
 * (pattern Observer) pour les changements de compte, puisque par principe
 * chaque instance maintient la sélection par l'utilisateur des comptes
 * correspondant à son type.
 * @author Olivier HAAS
 */
public class ComptePanel extends JPanel {
	
	/** Onglets intérieurs pour gérer le type de vue */
	private JTabbedPane containerVue;

	/** Table qui contient les écritures (que le modèle a besoin de connaître) */
	private FinancialTable tableEcritures;

	/** Modèle des écritures. */
	private EcrituresTableModel modelEcritures;

	/** Modèle des suivis de compte. */
	private SuiviTableModel modelSuivi;

	/** Modèle de la synthèse des comptes. */
	private SyntheseTableModel modelSynthese;
	
	/** Panel des soldes. */
	private SoldesPanel panelSoldes;

	/** Sélecteur de date (pour le mettre à jour à chaque changement d'onglet) */
	private DateSelector dateSelector;

	/** Observable gérant les changements de données. */
	private DataObservable dataObservable = new DataObservable();

	/**
	 * Construit un ComptePanel pour les comptes correspondant aux types
	 * spécifiés.
	 * 
	 * @param gui
	 * 			Interface graphique à prévenir en cas de changement de données,
	 * 			notamment pour lui permettre de connaître l'existence de
	 * 			modifications non encore sauvegardées. Peut	être null.
	 * 
	 * @param filter
	 * 			Le filtre à utiliser pour sélectionner les comptes à afficher.
	 */
	public ComptePanel(DataObserver gui, FilterCompte filter) {
		super(new BorderLayout());				// JPanel avec BorderLayout
		Month debut =							// Mois le plus ancien possible
				DAOFactory.getFactory().getDebut();

		// Observable gérant les mois et dates
		MonthObservable monthObservable = new MonthObservable();
		
		// Observable gérant les changements de soldes
		SoldesObservable soldesObservable =
				new SoldesObservable(dataObservable);
		
		// Permettre au GUI d'observer les changements de données
		if (gui != null) {
			dataObservable.addObserver(gui);
		}

		// Ajouter le sélecteur de date
		dateSelector = new DateSelector(debut, monthObservable);
		add(dateSelector, BorderLayout.WEST);

		// Liste déroulante des comptes
		JComboBox<Compte> compteComboBox = createComboBox(filter);

		// Observable gérant les changements de compte
		CompteObservable compteObservable =
				new CompteObservable(compteComboBox);

		// Le panel contenant les soldes
		panelSoldes = SoldesPanel.getInstance(filter.acceptsBancaires(),
				monthObservable, compteObservable, soldesObservable);

		// Insérer tout ça dans un panel en haut
		add(createTopPanel(compteComboBox, panelSoldes), BorderLayout.NORTH);

		// Insérer une table des écritures avec des modèles personnalisés
		modelEcritures = new EcrituresTableModel(monthObservable,
				compteObservable, dataObservable);
		modelSuivi = new SuiviTableModel(monthObservable, compteObservable,
				soldesObservable);
		modelSynthese = new SyntheseTableModel(monthObservable,
				compteObservable, soldesObservable, filter);
		add(createTable(monthObservable), BorderLayout.CENTER);
	}// constructeur

	/** Crée un panel contenant une <code>JComboBox</code> pour le choix du
	 * compte.
	 * 
	 * @param filter	Le filtre pour sélectionner les comptes à proposer.
	 */
	private JComboBox<Compte> createComboBox(FilterCompte filter) {

		// Récupérer tous les comptes
		Iterable<Compte> comptes;
		try {
			comptes = DAOFactory.getFactory().getCompteDAO().getAll();

		} catch (IOException e) {
			// Renvoyer un ensemble vide
			comptes = new HashSet<Compte>();
		}

		// Construire une liste des comptes de ce type
		List<Compte> comptes2display = new ArrayList<Compte>();
		for (Compte c : comptes) {

			// Si le compte est du type voulu, ajouter à la liste
			if (filter.accepts(c)) {
				comptes2display.add(c);
			}
		}// for comptes

		// Ajouter le compte abstrait d'épargne si besoin
		if (filter.accepts(Compte.compteEpargne)) {
			comptes2display.add(Compte.compteEpargne);
		}
		
		// Trier la liste des comptes
		Collections.sort(comptes2display);

		// Définir la ComboBox
		JComboBox<Compte> compteComboBox = new JComboBox<>(
				comptes2display.toArray(new Compte[comptes2display.size()]));

		/* Prévoir un Renderer spécifique s'il faut afficher des comptes
		 * bancaires. */
		if (filter.acceptsBancaires()) {

			/*
			 * Attribuer un ListCellRenderer pour aligner séparément nom et
			 * numéro
			 */
			compteComboBox.setRenderer(new ListCellRenderer<Compte>() {
				@Override
				public Component getListCellRendererComponent(
						JList<? extends Compte> list, Compte value, int index,
						boolean isSelected, boolean cellHasFocus) {
					
					// Si pas de compte
					if (value == null) {
						return new JLabel();				// Étiquette vide
					}

					// Créer un panel
					JPanel panel = new JPanel(new GridLayout(1, 2));

					// Créer l'étiquette du nom
					JLabel nom = (JLabel) new DefaultListCellRenderer()
							.getListCellRendererComponent(list,
									value.getNom(),			// Nom du compte
									index, isSelected, cellHasFocus);

					// Créer l'étiquette du numéro
					String numero = "";
					if (value instanceof CompteBancaire) {	// Au cas où
						// Numéro
						numero = ((CompteBancaire) value).getFormattedNumero();
					}

					JLabel num = (JLabel) new DefaultListCellRenderer()
							.getListCellRendererComponent(list, numero, index,
									isSelected, cellHasFocus);

					// Ajuster l'alignement de chacune
					nom.setHorizontalAlignment(JLabel.LEFT);
					num.setHorizontalAlignment(SwingConstants.RIGHT);

					// Empaqueter et renvoyer
					panel.add(nom);
					panel.add(num);
					return panel;
				}// getListCellRendererComponent
			});// classe anonyme
		}// if compte bancaire

		// Sélectionner le 1er item
		if (compteComboBox.getModel().getSize() > 0) {
			compteComboBox.setSelectedIndex(0);
		}

		return compteComboBox;
	}// createComboBox

	/** Crée une table de données pour le compte et le mois en cours. */
	private Component createTable(MonthObservable monthObservable) {
		containerVue = new JTabbedPane();				// Le panel

		// La table des écritures
		tableEcritures = new FinancialTable(modelEcritures);
		containerVue.add(new JScrollPane(tableEcritures), "Écritures");
		
		// La table des suivis dans un FlowLayout pour éviter un étalement
		ScrollablePanel panelSuivi =
				new ScrollablePanel(new FlowLayout(FlowLayout.LEADING));
		panelSuivi.add(new FinancialTable(modelSuivi));
		JScrollPane scrollPaneSuivi = new JScrollPane(panelSuivi);
		containerVue.add(scrollPaneSuivi, "Suivi");
		
		// La table de synthèse (idem)
		ScrollablePanel panelSynthese =
				new ScrollablePanel(new FlowLayout(FlowLayout.LEADING));
		FinancialTable tableSynthese = new FinancialTable(modelSynthese);
		panelSynthese.add(tableSynthese);
		JScrollPane scrollPaneSynthese = new JScrollPane(panelSynthese);
		containerVue.add(scrollPaneSynthese, "Autres comptes");
		
		// Le panneau de recherche
		JPanel searchPanel = new JPanel(new BorderLayout());
		containerVue.add(searchPanel, "Rechercher");	// Ajouter l'onglet
		
		// La barre de recherche
		JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchBar.add(new JLabel("Rechercher : "));		// Libellé Rechercher
		JTextField searchField = new JTextField(		// Champ de saisie
				SearchTableModel.getDocument(),		// Modèle unique pour tous
				null,									// Texte vide
				0);										// Pas de colonnes
		searchField.setPreferredSize(					// Forcer la largeur
				new Dimension(
						100,
						searchField.getPreferredSize().height));
		searchBar.add(searchField);						// Ajouter à la barre
		searchPanel.add(searchBar, BorderLayout.NORTH);	// Ajouter au panneau
		
		// La table de recherche
		SearchTableModel searchModel = SearchTableModel.getInstance(
						monthObservable,
						dataObservable,
						searchField.getDocument());
		searchPanel.add(new JScrollPane(new FinancialTable(searchModel)));
		
		return containerVue;
	}// createTable

	/**
	 * Crée un panel pour la barre supérieure. Je n'ai pas trouvé mieux qu'un
	 * GroupLayout pour éviter que les composants ne reviennent à la ligne (s'il
	 * n'y a pas assez de place) ou ne soient exagérément agrandis (s'il y a
	 * trop de place).
	 */
	private JPanel createTopPanel(Component c1, Component c2) {

		JPanel topPanel = new JPanel();
		GroupLayout layout = new GroupLayout(topPanel);
		topPanel.setLayout(layout);

		Component glue = Box.createHorizontalGlue();

		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setVerticalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.CENTER, false)
				.addComponent(c1, GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(glue)
				.addComponent(c2, GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));

		layout.setHorizontalGroup(layout
				.createSequentialGroup()
				.addComponent(c1, GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(glue)
				.addComponent(c2, GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));

		return topPanel;
	}

	/**
	 * Met à jour le contenu du panel. Cette méthode est appelée lorsqu'on
	 * change d'onglet. Concrètement, elle met à jour le sélecteur de date.
	 */
	public void update() {

		// Mettre à jour les données des composants du panel
		modelEcritures.update();
		modelSuivi.update();
		modelSynthese.update();
		panelSoldes.update();

		// Cas particulier du sélecteur de date
		if (MonthObservable.getDate() == null) {	// Pas de date ?
			dateSelector.monthChanged(MonthObservable.getMonth());	// La mois
		} else {									// Sinon
			dateSelector.dateChanged(MonthObservable.getDate());	// La date
		}
	}// update

	/** Efface l'écriture actuellement sélectionnée. */
	public void deleteCurrentEcriture() throws IOException {

		// Récupérer l'écriture sélectionnée
		Ecriture e = modelEcritures.getEcritureAt(tableEcritures
				.getSelectedRow()); // ligne de la table

		// Y a-t-il une écriture à cette ligne ?
		if (e != null
				&& JOptionPane.showConfirmDialog(
						// Demander confirmation
						this.getRootPane(),
						"Voulez-vous vraiment supprimer " +
						"l'écriture sélectionnée ?",
						"Effacer l'écriture",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION) {

			// Supprimer l'écriture
			EcritureController.remove(e.id);
			dataObservable.notifyObservers();
		}// if e non null et confirmation
	}// deleteCurrentEcriture
}// class ComptePanel

/**
 * Un JPanel implémentant Scrollable. Tout simplement pour faire défiler une
 * table dans un JScrollPane avec un incrément défini à l'avance.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
class ScrollablePanel extends JPanel implements Scrollable {

	public ScrollablePanel(LayoutManager l) {
		super(l);
	}

	@Override
	/** Implémentation par défaut
	 * @return	getPreferredSize() */
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	/** Retourne une valeur pré-définie pour les grandes incrémentations. */
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 48; // soit 3 lignes
	}

	@Override
	/** @return false */
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	/** Cette implémentation force la table à s'ajuster en largeur.
	 * @return true. */
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	/** Retourne une valeur pré-définie pour les incrémentations classiques */
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 16; // soit 1 ligne
	}
}// class ScrollablePanel