package haas.olivier.autocompletion;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/** Un champ de saisie proposant une liste de choix en auto-complétion.
 * 
 * @author Olivier HAAS
 */
public class CompletionTextField<T> extends JTextField
implements DocumentListener, ListSelectionListener,
MouseListener, ActionListener {
	private static final long serialVersionUID = -1051833788846424652L;
	
	/** Gestionnaire de pop-ups. */
	private static PopupFactory factory = PopupFactory.getSharedInstance();
	
	/** Le pop-up affichant la liste des suggestions. */
	private Popup popup;
	
	/** Le modèle de données utilisé pour faire des suggestions. */
	private CompletionModel<T> model;
	
	/** Indique si l'utilisateur peut valider un texte saisi ne correspondant à
	 * aucune valeur prédéfinie.
	 */
	private boolean allowCustomValues;
	
	/** La valeur actuelle. */
	private Object value = getText();
	
	/** La liste des suggestions. */
	private JList<T> suggest = new JList<>();
	
	/** Le panneau de défilement contenant la liste de sélection. */
	private JScrollPane scroll = new JScrollPane(suggest);
	
	/** Sélectionne un nouvel item par décalage depuis l'item actuel. Si aucun
	 * item n'était sélectionné précédemment, ou si le nouvel index est hors
	 * intervalle, un item par défaut est sélectionné.
	 * 
	 * @param delta		Le décalage d'index à effectuer.
	 * @param defaut	L'index de l'item à sélectionner par défaut.
	 */
	private void moveSelectedIndex(int delta, int defaut) {
		int sel = suggest.getSelectedIndex();
		int index = sel + delta;
		if (sel == -1										// Pas de sélection
				|| index < 0								// Trop haut
				|| index >= suggest.getModel().getSize()) {	// Trop bas
			suggest.setSelectedIndex(defaut);				// Index par défaut
		} else {
			suggest.setSelectedIndex(index);
		}
		suggest.ensureIndexIsVisible(suggest.getSelectedIndex());
	}// moveSelectedIndex
	
	@SuppressWarnings("serial")
	/** Action modifiant la sélection dans la liste : sélection de l'item
	 * suivant.
	 */
	private Action bas = new AbstractAction() {

		@Override
		/** Sélectionne l'item suivant dans la liste.
		 * Si aucun item n'est sélectionné, ou si l'item sélectionné est le
		 * dernier, la méthode sélectionne le premier.
		 */
		public void actionPerformed(ActionEvent e) {
			moveSelectedIndex(1, 0);
		}// actionPerformed
		
	};// classe anonyme AbstractAction bas
	
	@SuppressWarnings("serial")
	/** Action modifiant la sélection dans la liste : sélection de l'item
	 * précédent.
	 */
	private Action haut = new AbstractAction() {

		@Override
		/** Sélectionne l'item précédent dans la liste.
		 * Si aucun item n'est sélectionné, ou si l'item sélectionné est le
		 * premier, la méthode sélectionne le dernier.
		 */
		public void actionPerformed(ActionEvent e) {
			moveSelectedIndex(-1, suggest.getModel().getSize()-1);
		}// actionPerformed
		
	};// classe anonyme AbstractAction haut
	
	// Constructeurs
	
	/** Construit un champ de saisie avec auto-complétion utilisant le modèle
	 * spécifié.
	 * @param model	Le modèle à utiliser.
	 */
	@SuppressWarnings({ "serial" })
	public CompletionTextField(CompletionModel<T> model,
			boolean allowCustomValues) {
		this.allowCustomValues = allowCustomValues;
		
		// Définir les actions des flèches haut et bas
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "bas");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "haut");
		getActionMap().put("bas", bas);
		getActionMap().put("haut", haut);
		
		/* Faire réagir la liste à la touche Entrée.
		 * Sinon, si l'utilisateur clique un item à la souris et appuie sur
		 * Entrée, il ne se passe rien parce que la liste a le focus et ne
		 * réagit pas. */
		suggest.getInputMap().put(								// Touche Entrée
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "entree");
		suggest.getActionMap().put("entree", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				CompletionTextField.this.fireActionPerformed();	// Valider
			}// actionPerformed
		});// classe anonyme AbstractAction
		
		// Paramétrer la liste de suggestions
		suggest.addListSelectionListener(this);	// Écouter la sélection de liste
		suggest.addMouseListener(this);			// Écouter clics sur la liste
		suggest.setCellRenderer(				// Rendu des items
				new DefaultListCellRenderer() {
//				new BasicComboBoxRenderer() {		// Apparence "ComboBox"
			@Override public String getToolTipText() {
				// Afficher le texte dans un tooltip des items de la liste
				return getText();
			}// getToolTipText
		});// DefaultListCellRenderer
		
		addActionListener(this);				// Écouter sa propre validation
		getDocument().addDocumentListener(this);// Écouter la saisie
		setModel(model);						// Mémoriser le modèle
		model.check();						// Vérifier que le modèle est chargé
	}// constructeur
	
	/** Masque le popup. Le pop-up n'est ensuite plus récupérable. */
	public void hidePopup() {
		if (popup != null) {
			popup.hide();
			popup = null;
		}
	}// hidePopup
	
	/** Crée un popup sur la table active, contenant le JScrollPane.
	 * 
	 * @param x	L'abscisse du nouveau popup.
	 * @param y	L'ordonnée du nouveau popup.
	 * @return	Un nouveau popup.
	 */
	private Popup createPopup(int x, int y) {
		return factory.getPopup(getParent(), scroll, x, y);
	}// createPopup
	
	// Interface DocumentListener
	
	@Override
	/** @see textChanged() */
	public void insertUpdate(DocumentEvent e) {
		textChanged();
	}
	
	@Override
	/** @see textChanged() */
	public void removeUpdate(DocumentEvent e) {
		textChanged();
	}
	
	@Override
	/** Aucune implémentation (respect de l'interface DocumentListener). */ 
	public void changedUpdate(DocumentEvent e) {
	}
	
	/** Méthode appelée lors des changements dans le champ de saisie.
	 * La méthode utilise la nouvelle saisie pour mettre à jour la liste des
	 * suggestions.
	 */
	private void textChanged() {
		
		/* Réclamer le focus
		 * En utilisant cette classe dans un éditeur de table, il arrive qu'on
		 * puisse taper du texte dedans sans que le champ de saisie ait le
		 * focus... et c'est gênant pour le fonctionnement de l'inputMap. */
		requestFocusInWindow();
		
		String text = getText();					// Nouveau texte saisi
		Collection<T> suggestions =
				model.filter(text);					// Filtrer items possibles
		
		/* Remplacer le modèle des suggestions.
		 * Rq: Par un effet collatéral, la valeur actuelle est supprimée. */
		DefaultListModel<T> model =					// Nouveau modèle
				new DefaultListModel<>();
		for (T t : suggestions) {					// Ajouter tous les éléments
			model.addElement(t);
		}
		suggest.setModel(model);					// Appliquer le modèle
		
		// Choisir le nouveau texte comme valeur actuelle
		if (allowCustomValues) {					// Valeurs perso possibles ?
			value = text;							// Retenir le nouveau texte
		}
		
		// Supprimer le popup
		hidePopup();
		
		// Déterminer le comportement d'affichage et sélection à adopter ensuite
		if (model.isEmpty()) {				// Pas de suggestions ?
			return;							// Arrêter ici
		} else if (!allowCustomValues		// Sinon, si pas de valeurs perso
				&& !text.isEmpty()) {		// Et du texte (non vide)
			suggest.setSelectedIndex(0);	// Sélectionner la première suggest
		}// if model empty
		Component parent = getParent();		// Composant parent
		if (parent == null) {				// Pas de parent (invisible)
			return;							// Arrêter ici
		}
		
		// Créer un pop-up pour afficher la liste
		
		// Localisation par défaut
		Point p = parent.getLocationOnScreen();		// Localisation du parent
		Rectangle rect = getBounds();				// Rectangle du champ saisie
		p.translate(rect.x, rect.y + rect.height);	// Point bas-gauche du champ
		
		// Dimensions par défaut
		Dimension screenSize =						// Dimensions de l'écran
				Toolkit.getDefaultToolkit().getScreenSize();
		JViewport view = scroll.getViewport();		// Vue du défilement
		view.setPreferredSize(new Dimension(
				suggest.getPreferredSize().width,	// Largeur de la liste
				view.getPreferredSize().height));	// Hauteur par défaut
		
		// Vérifier la largeur
		if (scroll.getPreferredSize().width
				> screenSize.width) {				// Trop large pour l'écran ?
			scroll.setPreferredSize(new Dimension(
					screenSize.width,				// Largeur de l'écran
					scroll.getPreferredSize().height));// Hauteur par défaut
		}// trop large
		
		// Vérifier que le popup n'est pas trop à droite par rapport à l'écran
		int bordDroit =								// Abscisse extrême droite
				p.x + scroll.getPreferredSize().width;
		if (bordDroit > screenSize.width) {			// Débordement à droite ?
			p.translate(							// Décaler un peu à gauche
					screenSize.width - bordDroit, 0);
		}// if débordement droit

		// Vérifier que le popup n'est pas trop bas par rapport à l'écran
		int hpopup =
				scroll.getPreferredSize().height;	// Hauteur du popup
		int hDispoBas = screenSize.height - p.y;	// Hauteur dispo en-desous
		int hDispoHaut = p.y - rect.height;			// Hauteur dispo au-dessus
		int hDispo = hDispoBas;						// Hauteur dispo (a priori)
		if (hpopup > hDispoBas						// Si déborde en bas et
				&& hDispoHaut > hDispoBas) {		//davantage de place en haut
			hDispo = hDispoHaut;					// Regarder au-dessus
			int y = hDispoHaut - hpopup;			// Nouvelle ordonnée
			if (y < 0) y = 0;						// Minimum zéro
			p.translate(0, y - p.y);				// Décaler vers le haut
		}// if trop bas
		
		// Vérifier la hauteur
		if (hpopup > hDispo) {
			scroll.setPreferredSize(new Dimension(
					scroll.getPreferredSize().width,// Largeur déjà définie
					hDispo));						// Hauteur maximale
		}// trop haut
		
		// Afficher
		popup = createPopup(p.x, p.y);				// Créer le popup
		popup.show();								// Afficher
	}// textChanged
	
	// Interface ListSelectionListener
	
	@Override
	/** Retient comme valeur l'item sélectionné dans la liste de suggestions.
	 * Interface ListSelectionListener.
	 * 
	 * Cette méthode est appelée par la liste de suggestions elle-même lorsque
	 * la sélection change.
	 */
	public void valueChanged(ListSelectionEvent e) {
		value = suggest.getSelectedValue();
	}
	
	// Gestion de la valeur à renvoyer
	
	/** Renvoie la valeur sélectionnée parmi les valeurs possibles.
	 * 
	 * @return	La valeur sélectionnée, ou null si aucun item n'a été
	 * 			sélectionné au cours de la saisie.
	 */
	public T getSelectedValue() {
		return (T) suggest.getSelectedValue();
	}
	
	/** Renvoie la valeur saisie : soit l'item choisi par l'utilisateur dans la
	 * liste s'il y en a un, soit le texte saisi directement par l'utilisateur.
	 */
	public Object getValue() {
		return value;
	}// getTypedValue
	
	// Getters et Setters divers

	/** Renvoie le modèle de données utilisé. */
	public CompletionModel<T> getModel() {
		return model;
	}
	
	/** Change le modèle de données à utiliser. */
	public void setModel(CompletionModel<T> model) {
		this.model = model;
	}

	/** Retourne la bordure du popup. */
	public Border getPopupBorder() {
		return scroll.getBorder();
	}
	
	/** Modifie la bordure du popup. */
	public void setPopupBorder(Border border) {
		scroll.setBorder(border);
	}
	
	// Interface ActionListener
	
	@Override
	/** Masque le pop-up et modifie le texte du champ de saisie pour l'ajuster à
	 * la sélection (sans provoquer de recherche de suggestions).
	 */
	public void actionPerformed(ActionEvent e) {
		
		// Inscrire le texte validé dans le champ de saisie
		getDocument().removeDocumentListener(this);	// Cesser d'écouter
		setText(value == null						// Imposer le texte
				? "" : value.toString());
		getDocument().addDocumentListener(this);	// Réécouter pour la suite
		
		// Masquer le popup
		hidePopup();
	}// actionPerformed

	// Interface MouseListener
	
	@Override
	/** Le double-clic dans la liste provoque la validation.
	 * L'item validé est forcément celui sur lequel l'utilisateur a cliqué...
	 * (pas de traitement de l'item par cette méthode)
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			fireActionPerformed();
		}
	}// mouseClicked

	@Override public void mousePressed(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
}
