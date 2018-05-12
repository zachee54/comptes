package haas.olivier.comptes.gui.table;

import haas.olivier.autocompletion.CacheCompletionModel;
import haas.olivier.autocompletion.CompletionEditor;
import haas.olivier.autocompletion.CompletionModel;
import haas.olivier.autocompletion.DefaultCompletionModel;
import haas.olivier.autocompletion.IndexCompletionModel;
import haas.olivier.comptes.Compte;
import haas.olivier.comptes.Ecriture;
import haas.olivier.comptes.dao.DAOFactory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * Une JTable personnalisée pour afficher des informations monétaires.
 * <p>
 * La table utilise des formats d'affichage, des éditeurs et des largeurs de
 * colonnes personnalisés pour les dates et mois, montants, comptes et numéros
 * de chèques.
 * <p>
 * Elle gère les sélections par ligne entière, et déplace la sélection
 * intelligemment en fonction des événements.
 * <p>
 * L'interface Action permet de proposer des raccourcis clavier Tab et Shift+Tab
 * pour l'édition rapide de plusieurs cellules.
 * <p>
 * La table gère également des infobulles personnalisées en fonction de la
 * largeur des cellules.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class FinancialTable extends JTable implements TableCellRenderer,
		Action {
	
	/**
	 * Un Renderer pour les BigDecimal.<br>
	 * Juste pour un format d'affichage plus naturel.
	 */
	public static class MontantTableCellRenderer
	extends DefaultTableCellRenderer {
		
		/**
		 * Le formateur de <code>BigDecimal</code>.
		 */
		private static final DecimalFormat BIGDECIMAL_FORMATTER =
				new DecimalFormat(
						"#,##0.00;- #",
						new DecimalFormatSymbols(Locale.FRANCE));
		
		static {
			// Ajouter des espaces après pour décoller les nombres de la marge
			BIGDECIMAL_FORMATTER.setPositiveSuffix("  ");
			BIGDECIMAL_FORMATTER.setNegativeSuffix("  ");
		}
		
		/**
		 * Écrit le <code>BigDecimal</code> selon le format prédéfini.
		 */
		@Override
		public void setValue(Object value) {

			// Comment écrire le nombre au format texte
			if (value instanceof BigDecimal				// BigDecimal non nul
					&& ((BigDecimal) value).signum() != 0) {

				// Écrire le montant formaté
				setText(BIGDECIMAL_FORMATTER.format(
						((BigDecimal) value).doubleValue()));

			} else {									// Nul ou autre classe
				
				// Ne rien écrire
				setText("");
			}

			// Alignement à droite
			setHorizontalAlignment(SwingConstants.RIGHT);
		}
	}// public static nested class MontantCellRenderer
	
	/**
	 * Format des chèques: entiers à 7 chiffres.
	 */
	private static final String CHEQUE_FORMAT = "%07d";

	/**
	 * Format des dates: jj/mm/aaaa.
	 */
	private static final DateFormat DATE_FORMATTER =
			new SimpleDateFormat("dd/MM/yyyy");

	// Couleurs
	public static final Color DEPENSE = new Color(0, 0, 190);
	public static final Color RECETTE = new Color(190, 0, 0);
	private static final Color NEUTRE = new Color(63, 63, 63);
	private static final Color FOND = new Color(255, 255, 255);
	private static final Color GRILLE = new Color(200, 200, 200);

	// Largeurs des colonnes
	private static final int WIDTH_CHEQUE = 64;
	private static final int WIDTH_POINTAGE = 15;
	private static final int WIDTH_DATE = 83;
	private static final int WIDTH_MOIS = 130;
	private static final int WIDTH_MONTANT = 85;
	private static final int WIDTH_STRING = 150;

	/**
	 * Liste déroulante pour le choix des comptes dans la table.
	 */
	private static JComboBox<Compte> boxComptes = new JComboBox<Compte>();
	static {
		// Personnaliser l'affichage par une séparation des types de comptes
		boxComptes.setRenderer(new ComptesComboBoxRenderer());

		// Style de police normal (au lieu de gras)
		boxComptes.setFont(boxComptes.getFont().deriveFont(Font.PLAIN));

		/* Remplir la comboBox. Appel statique pour ne pas remplir la comboBox à
		 * chaque instanciation. */
		updateComptesEditor();
	}
	
	// Réduire le délai d'apparition des infobulles
	static {
		ToolTipManager.sharedInstance().setInitialDelay(250);
	}
	
	/**
	 * Remplit la liste déroulante de choix des comptes, utilisée par le
	 * <code>TableCellEditor</code> spécifique aux objets <code>Compte</code>.
	 * <p>
	 * La méthode est publique pour pouvoir forcer la mise à jour depuis un
	 * autre objet (modification des comptes, par exemple).
	 */
	public static void updateComptesEditor() {
		try {
			// Obtenir tous les comptes
			Collection<Compte> comptesSet =
					DAOFactory.getFactory().getCompteDAO().getAll();

			// Trier les comptes
			List<Compte> comptes = new ArrayList<Compte>(comptesSet);
			Collections.sort(comptes);

			// Supprimer tous les comptes
			boxComptes.removeAllItems();
			
			// Ajouter chaque compte
			for (Compte c : comptes) {
				boxComptes.addItem(c);
			}
			
		} catch (IOException e1) {	
			e1.printStackTrace();
			// TODO Exception à gérer
		}
	}

	/**
	 * Construit une <code>JTable</code> avec quelques modifications de
	 * formatage.<br>
	 * La colonne des chèques fait l'objet d'un Renderer spécifique.<br>
	 * Les <code>TableCellRenderer</code> par défaut des classes
	 * <code>BigDecimal</code>, <code>Date</code> et <code>Month</code> sont
	 * redéfinis.
	 * 
	 * @param model
	 *            Le modèle de la table.
	 */
	public FinancialTable(FinancialTableModel model) {
		super(model);

		// Mode de sélection
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellSelectionEnabled(false);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(true);

		// Configurer les Renderers et les colonnes
		configure();
		
		// Écouter la touche Tab en mode d'édition
		InputMap inputMapAncestor =
				getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMapAncestor.put(							// Tab
				KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "editNext");
		inputMapAncestor.put(KeyStroke.getKeyStroke(	// Shift + tab
				KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), "editNext");
		getActionMap().put("editNext", this);
	}
	
	/**
	 * Définit des Renderers personnalisés pour les <code>BigDecimal</code>, les
	 * <code>Date</code> et les <code>Month</code>.
	 */
	@Override
	protected void createDefaultRenderers() {
		super.createDefaultRenderers();
		
		// Remplacer le Renderer par défaut pour la classe BigDecimal
		setDefaultRenderer(BigDecimal.class,
				new MontantTableCellRenderer());
		
		// Remplacer le Renderer par défaut pour la classe Date
		setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {
			
			@Override
			public void setValue(Object value) {
				setText(value instanceof Date
						? DATE_FORMATTER.format((Date) value)// Date
								: ""); 					// Pas de date = pas de texte
			}
			
		});// classe anonyme
	}
	
	/**
	 * Définit un éditeur personnalisé utilisant un popup pour les
	 * <code>Date</code>.
	 */
	@Override
	protected void createDefaultEditors() {
		super.createDefaultEditors();
		
		// Spécifier un CellEditor pop-up avec JTextField pour les dates
		setDefaultEditor(
				Date.class,
				new PopupDateEditor(
						new SimpleDateEditor(DATE_FORMATTER, GRILLE)));
	}
	
	/**
	 * Configure la table en ajustant la taille des colonnes, les Renderers et
	 * les Editors par colonne.
	 */
	protected void configure() {
		
		// Configurer les colonnes
		if (dataModel instanceof FinancialTableModel) {
			ColumnType[] disposition =
					((FinancialTableModel) dataModel).getDisposition();
			for (int i = 0; i < disposition.length; i++) {
				configure(columnModel.getColumn(i), disposition[i]);
			}
		}
	}

	protected void configure(TableColumn column, ColumnType type) {
		switch(type) {
		case CHEQUE:								// Chèques
			column.setMinWidth(WIDTH_CHEQUE);		// Largeur minimale

			// Définir un Renderer spécifique pour cette colonne
			column.setCellRenderer(new DefaultTableCellRenderer() {
				
				@Override
				public void setValue(Object value) {

					/* Est-ce un Integer ?
					 * (Ça peut aussi être l'en-tête de colonne...)
					 */
					if (value instanceof Integer) {

						// Changer l'affichage de l'Integer
						setText(String.format(CHEQUE_FORMAT, value));

						// Aligner à droite
						setHorizontalAlignment(SwingConstants.RIGHT);

					} else {
						// Pas de n° de chèque: ne rien écrire
						setText("");
					}
				}
				
			});// classe anonyme DefaultTableCellRenderer
			

			// Définir un éditeur spécifique pour les chèques
			if (dataModel instanceof EcrituresTableModel) {
				column.setCellEditor(new ChequeCellEditor(
						((EcrituresTableModel) dataModel).getDataObservable()));
			}
			break;

		case POINTAGE:								// Pointages
			column.setMaxWidth(WIDTH_POINTAGE);		// Largeur maximale
			
			/*
			 * Une case à cocher pour l'édition
			 * On a besoin de la définir nous-mêmes car la classe JTable ne
			 * définit l'éditeur par défaut des Boolean qu'après l'affectation
			 * du modèle, alors qu'ici on est appelé depuis setModel.
			 */
			JCheckBox checkBox = new JCheckBox();
			checkBox.setHorizontalAlignment(JCheckBox.CENTER);
			column.setCellEditor(					// Éditeur popup et checkbox
					new PopupDateEditor(new DefaultCellEditor(checkBox)));
			break;

		case DATE:									// Dates
		case DATE_POINTAGE:							// Dates de pointage
			column.setMinWidth(WIDTH_DATE);			// Largeur minimale
			break;

		case MOIS:									// Mois
			column.setMinWidth(WIDTH_MOIS);			// Largeur minimale
			break;

		case MONTANT:								// Montants
		case HISTORIQUE:							// Historique
		case AVUE:									// Solde àvue
		case MOYENNE:								// Moyenne glissante
			column.setMinWidth(WIDTH_MONTANT);		// Largeur minimale
			break;

		case TIERS:									// Nom du tiers
		case LIBELLE:								// Libellé
			column.setPreferredWidth(WIDTH_STRING);	// Largeur préférée

			// Définir un éditeur spécifique (liste déroulante)

			// Modèle de base
			CompletionModel<Entry<String,Integer>> model1 =
					new DefaultCompletionModel<Entry<String,Integer>>() {
				
				@Override
				public void load() {
					// Charger l'index des commentaires
					try {
						setValues(
								DAOFactory.getFactory().getEcritureDAO()
								.constructCommentIndex().entrySet());
					} catch (IOException e) {
						e.printStackTrace();
						// TODO Exception à gérer
					}
				}
			};//classe anonyme DefaultCompletionModel

			// Modèle indexé
			CompletionModel<String> model2 =
					new IndexCompletionModel<String>(model1);

			// Modèle avec cache
			CompletionModel<String> model3 =
					new CacheCompletionModel<String>(

							new CacheCompletionModel.ContextProvider() {
								
								@Override
								public Object getContext() {
									// Le contexte = le DAO écritures
									return DAOFactory.getFactory()
											.getEcritureDAO();
								}
								
							},// classe anonyme ContextProvider

							model2);

			// Tout mettre dans un éditeur
			column.setCellEditor(
					new CompletionEditor<String>(model3, true));
			break;

		case COMPTE:								// Compte
		case CONTREPARTIE:							// Contrepartie
			column.setCellEditor(					// Éditeur combobox
					new DefaultCellEditor(boxComptes));
			column.setPreferredWidth(WIDTH_STRING);	// Largeur préférée
			break;

		default:	// Sans objet, sauf pour le compilateur
		}
	}

	/**
	 * Modifie le modèle.
	 * <p>
	 * Cette méthode vérifie que le nonuveau modèle est une instance
	 * <code>FinancialTableModel</code>.
	 * 
	 * @throws IllegalArgumentException
	 * 			Si le modèle n'est pas un <code>FinancialTableModel</code>.
	 */
	@Override
	public void setModel(TableModel model) {
		if (model instanceof FinancialTableModel) {
			super.setModel(model);
		} else {
			throw new IllegalArgumentException("Seuls les FinancialTableModel" +
					" sont autorisés avec une FinancialTable");
		}
	}

	/**
	 * Renvoie cet objet.
	 * <p>
	 * Cette méthode permet d'intercepter les Renderer au moment où ils sont
	 * appelés. L'implémentation de l'interface TableCellRenderer permet de
	 * contrôler la création des RendererComponent et de les coloriser avant de
	 * les retourner.
	 * 
	 * @return this
	 */
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return this;
	}

	/**
	 * Récupère le Component généré par le TableCellRenderer de la classe
	 * JTable, puis le colorise en fonction des données du modèle.
	 * <p>
	 * Cette méthode ne peut pas être externalisée dans un
	 * <code>TableCellRenderer</code> parce qu'on a besoin d'accéder à la
	 * méthode <code>getCellRenderer(int,int)</code> de la classe mère, ce qui
	 * n'est possible que dans la classe actuelle. En effet, la méthode
	 * <code>getCellRenderer(int,int)</code> a été réécrite pour intercepter les
	 * appels aux renderers.
	 * 
	 * @return	Un composant colorisé.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		// Récupérer le Component généré par le Renderer de JTable
		Component component = super.getCellRenderer(row, column)
				.getTableCellRendererComponent(table, value, isSelected,
						hasFocus, row, column);

		// Forcer la couleur de la grille: bordure gauche et basse
		((JComponent) component).setBorder(BorderFactory.createMatteBorder(0,
				1, 1, 0, GRILLE));

		// Récupérer le modèle
		TableModel model = table.getModel();
		assert (model instanceof FinancialTableModel);

		// Récupérer le montant de l'écriture affichée sur cette ligne
		BigDecimal montant;
		if (value instanceof BigDecimal) {	// La cellule contient un montant

			// Utiliser le signe du nombre contenu dans la cellule
			montant = (BigDecimal) value;

		} else {							// La cellule contient autre chose

			// Utiliser le signe du montant principal de cette ligne
			montant = ((FinancialTableModel) model).getMontantAt(row);
		}

		// Déterminer la couleur à utiliser
		Color couleur;
		if (montant.signum() > 0) {			// Couleur pour montant positif
			couleur = RECETTE;
		} else if (montant.signum() < 0) {	// Couleur pour montant négatif
			couleur = DEPENSE;
		} else { // Montant nul: couleur neutre
			couleur = NEUTRE;
		}

		// Coloriser
		if (isSelected) {						// Sélectionné
			component.setBackground(couleur);	// Arrière-plan
			component.setForeground(FOND);		// Couleur du fond
		} else {								// Non sélectionné
			component.setBackground(FOND);		// Arrière-plan
			component.setForeground(couleur);	// Couleur de police
		}

		return component;
	}

	/**
	 * Détermine le tooltip à afficher au survol de la souris sur la table.
	 * <p>
	 * Seule la colonne contenant les cases à cocher de pointage donne lieu à un
	 * tooltip, qui contient alors la date de pointage.
	 */
	@Override
	public String getToolTipText(MouseEvent e) {
		String toolTip = null;

		// Arrêter là si on n'a pas un FinancialTableModel
		if (!(dataModel instanceof FinancialTableModel)) {
			return toolTip;
		}

		// Trouver les coordonnées de la cellule survolée
		Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);
		int realColumnIndex = convertColumnIndexToModel(colIndex);
		int realRowIndex = convertRowIndexToModel(rowIndex);

		// Obtenir le type de la colonne
		FinancialTableModel model = (FinancialTableModel) dataModel;
		ColumnType columnType = model.disposition[realColumnIndex];

		// Est-ce la colonne des pointages ?
		if (columnType == ColumnType.POINTAGE
				&& model instanceof EcrituresTableModel) {

			// Obtenir l'écriture de cette ligne
			Ecriture ecriture = ((EcrituresTableModel) model)
					.getEcritureAt(realRowIndex);

			// Mettre dans le tooltip la date de pointage... ou rien
			toolTip = (ecriture == null || ecriture.pointage == null)
					? "" : DATE_FORMATTER.format(ecriture.pointage);

		} else {										// Autre colonne

			// Récupérer le Component de la cellule
			Component comp = getCellRenderer(rowIndex, colIndex)
					.getTableCellRendererComponent(this,
							model.getValueAt(rowIndex, colIndex), false, false,
							rowIndex, colIndex);

			// Vérifier que c'est un JLabel
			if (comp instanceof JLabel) {

				// Calculer la largeur de la colonne
				int availableWidth = getColumnModel().getColumn(colIndex)
						.getWidth();

				// Soustraire les espaces et la largeur de la bordure
				availableWidth -= getIntercellSpacing().getWidth();
				Insets borderInsets = ((JComponent) comp).getBorder()
						.getBorderInsets(comp);
				availableWidth -= (borderInsets.left + borderInsets.right);

				// Comparer la taille du texte et du JLabel
				FontMetrics fm = getFontMetrics(getFont());
				String cellText = ((JLabel) comp).getText();
				if (fm.stringWidth(cellText) > availableWidth) {
					toolTip = cellText;		// Trop long: afficher le toolTip
				}
			}
		}
		return toolTip;
	}

	/**
	 * Met à jour la table.
	 * <p>
	 * En plus de l'appel à la méthode de la classe mère, cette méthode permet
	 * de remettre à jour la configuration des colonnes (taille, Renderer, etc)
	 * au cas où la structure du FinancialTableModel ait changé.
	 * <p>
	 * Elle permet aussi, en cas de changement du compte sélectionné, de
	 * sélectionner le compte adéquat dans la table de synthèse.
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		// Appeler la méthode de la classe mère
		super.tableChanged(e);

		// Si la structure a changé
		if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
			configure();							// Reconfigurer les colonnes
		}
		
		// Pour la table de synthèse des comptes, sélectionner le compte actuel
		if (dataModel instanceof SyntheseTableModel) {

			// Ligne contenant le compte
			int row = ((SyntheseTableModel) dataModel).getActualCompteRow();

			// Sélectionner cette ligne
			try {
				setRowSelectionInterval(row, row);
			} catch (IllegalArgumentException e1) {	// Modèle pas opérationnel
			}										// Tant pis
		}
	}

	/**
	 * Déplace l'éditeur vers la droite ou vers la gauche.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!isEditing()) {
			return;
		} // Seulement en édition !

		// Obtenir l'éditeur actuel et l'arrêter propement
		int row = getEditingRow();
		int col = getEditingColumn();
		TableCellEditor editor = getCellEditor(row, col);
		editor.stopCellEditing();

		// Obtenir la nouvelle ligne sélectionnée
		Integer selected = getSelectedRow();

		// Voir s'il faut se déplacer à gauche ou à droite
		if ((e.getModifiers() & ActionEvent.SHIFT_MASK)
				== ActionEvent.SHIFT_MASK) { // Touche Shift
			col--; // Colonne à gauche
		} else {
			col++; // Colonne à droite
		}

		// Lancer l'édition de la cellule adjacente (s'il y en a une)
		editCellAt(selected == null ? row : selected, col);
	}

	/**
	 * Interface <code>Action</code>. Aucune implémentation.
	 */
	@Override
	public Object getValue(String arg0) {
		return null;
	}

	/**
	 * Interface <code>Action</code>. Aucune implémentation.
	 */
	@Override
	public void putValue(String arg0, Object arg1) {
	}
}