package haas.olivier.autocompletion;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/** Un CellEditor de String supportant l'autocomplétion par pop-up. */
public class CompletionEditor<T> extends DefaultCellEditor implements AncestorListener {
	private static final long serialVersionUID = -1437200188350992006L;

	/** Le champ de saisie avec auto-complétion. */
	private CompletionTextField<T> field;
	
	/** La valeur de la cellule. */
	private Object value;
	
	/** Indique s'il faut sélectionner le texte au démarrage de l'édition. */
	private boolean selectOnEdit = false;
	
	/** Le modèle de données utilisé par le champ de saisie pour
	 * l'auto-complétion. */
	protected CompletionModel<T> model;
	
	/** Construit un éditeur avec auto-complétion en utilisant une couleur par
	 * défaut.
	 * 
	 * @param model
	 * 			Le modèle de données à utiliser pour l'auto-complétion.
	 * @param allowCustomValues
	 * 			Indique si l'éditeur peut accepter des valeurs non comprises
	 * 			dans le modèle.
	 */
	public CompletionEditor(CompletionModel<T> model,
			boolean allowCustomValues) {
		this(model, allowCustomValues, new Color(200, 200, 200));
	}// constructeur
	
	/** Construit un éditeur avec auto-complétion.
	 * 
	 * @param model
	 * 			Le modèle de donnée à utiliser pour l'auto-complétion.
	 * @param allowCustomValues
	 * 			Indique si l'éditeur peut accepter des valeurs non comprises
	 * 			dans le modèle.
	 * @param color
	 * 			La couleur de la bordure du champ de saisie et du pop-up.
	 */
	@SuppressWarnings("unchecked")
	public CompletionEditor(CompletionModel<T> model,
			boolean allowCustomValues, Color color) {
		
		// Un éditeur classique
		super(new CompletionTextField<T>(model, allowCustomValues));
		
		// Récupérer le champ de saisie
		field = (CompletionTextField<T>) getComponent();
		
		// Bordure de la couleur spécifiée
		field.setBorder(BorderFactory.createLineBorder(color));
		
		// Mettre la même bordure au panneau de défilement de la liste
		field.setPopupBorder(BorderFactory.createLineBorder(color));
		
		// Mémoriser le modèle pour le mettre à jour à chaque début d'édition
		this.model = model;
		
		// [Workaround] Éviter de rater la fin de l'édition
		field.addAncestorListener(this);	// Surveiller le champ de saisie
	}// constructeur
	
    /** Renvoie le champ texte utilisé pour la saisie. */
    protected CompletionTextField<T> getField() {
        return field;
    }// getField
    
	/** Vérifie que le modèle de données est chargé avant de lancer l'édition.
	 */
    @Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		// Retenir la valeur de départ
		this.value = value;
		
		// Vérifier que le modèle est chargé
		model.check();
		
		// Champ de saisie préparé par la classe mère
		Component comp = super.getTableCellEditorComponent( 
				table, value, isSelected, row, column);
		
		// Sélectionner le texte si cela a été demandé
		if (selectOnEdit) {
			
			// Lancer APRÈS le clic de souris
			SwingUtilities.invokeLater(new Runnable() {
				@Override public void run() {
					field.selectAll();
				}// run
			});// classe anonyme Runnable
		}// if selectOnEdit
		
		// Renvoyer le composant
		return comp;
	}// getTableCellEditorComponent
	
	@Override
	public boolean stopCellEditing() {
		
		// Récupérer la valeur à conserver
		value = field.getValue();
		
		field.hidePopup();						// Supprimer le popup
		return super.stopCellEditing();			// Stopper l'édition normalement
	}// stopCellEditing

	@Override
	public void cancelCellEditing() {
		field.hidePopup();						// Supprimer le popup
		super.cancelCellEditing();				// Annuler normalement
	}// cancelCellEditing

	@Override
	public Object getCellEditorValue() {
		return value;
	}// getCellEditorValue

	/** Indique si le texte est sélectionné automatiquement au démarrage de
	 * l'édition.
	 */
	public boolean getSelectOnEdit() {
		return selectOnEdit;
	}
	
	/** Détermine si le texte doit être sélectionné automatiquement au démarrage
	 * de l'édition.
	 */
	public void setSelectOnEdit(boolean bool) {
		selectOnEdit = bool;
	}
	
	// Interface AncestorListener
	
	@Override
	public void ancestorAdded(AncestorEvent event) {
	}

	/** [Workaround]
	 * Masque le popup, pour éviter qu'il ne reste affiché si l'édition
	 * s'arrête sans un appel à <code>stopCellEditing()</code> ni
	 * <code>cancelCellEditing()</code>. Ca ne devrait pas arriver, mais
	 * pourtant ça arrive !?
	 */
	@Override
	public void ancestorRemoved(AncestorEvent event) {
		cancelCellEditing();
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
	}
}
