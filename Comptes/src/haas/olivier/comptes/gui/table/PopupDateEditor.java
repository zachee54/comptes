package haas.olivier.comptes.gui.table;

import haas.olivier.util.Month;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

/**
 * Un calendrier pop-up pour éditer une cellule dans un JTable.
 * <i>Pattern Decorator</i>.
 * <p>
 * Le pop-up utilise un <code>TableCellEditor</code> délégué, à qui il délègue
 * notamment l'affichage du composant dans la cellule éditée.<br>
 * En pratique, le <code>PopupDateEditor</code> s'interpose entre la table (qui
 * gère le début, la fin et les suites de l'édition) et l'éditeur délégué. Il
 * affiche un calendrier pop-up cliquable qui prend la main sur l'éditeur
 * délégué lorsque l'utilisateur agit sur le pop-up. 
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class PopupDateEditor extends AbstractCellEditor
implements TableCellEditor, CellEditorListener, ActionListener,
MouseMotionListener, MouseWheelListener {

	// Couleurs
	private static final Color STANDARD		= Color.BLACK;
	private static final Color FOND			= new Color(255, 255, 240);
	private static final Color SELECTION	= new Color(  0,   0, 191);
	private static final Color ACTUAL		= Color.WHITE;
	private static final Color INACTIF		= Color.GRAY;
	
	// Polices
	private static Font NORMAL, BOLD;	// Police du L&F +la même en gras
	
	// Dimension
	private static final Dimension DIMENSION = new Dimension(200,180);
	
	// Constantes d'action
	private static final String PREVIOUS_YEAR = "previous year";
	private static final String PREVIOUS_MONTH = "previous month";
	private static final String NEXT_MONTH = "next month";
	private static final String NEXT_YEAR = "next year";
	
	private static Date today;
	static {
		Calendar cal = Calendar.getInstance();	// Aujourd'hui...
		cal.set(Calendar.HOUR_OF_DAY, 0);		// sans heure, min, sec, mil
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		today = cal.getTime();
	}
	
	// Jour du mois en 1 ou deux chiffres.
	private static SimpleDateFormat dayOfMonth = new SimpleDateFormat("d");
	
	// Format d'affichage du mois (gain de place)
	private static SimpleDateFormat monthFormat =
			new SimpleDateFormat("MMM yyyy");
	
	// Gestionnaire de pop-ups
	private static PopupFactory factory = PopupFactory.getSharedInstance();
	
	// Eléments structurels
	private Popup popup;				// Le pop-up
	private TableCellEditor delegate;	// L'éditeur délégué
	private Date editorDate = null;		// La date active pour le pop-up
	private boolean userClicked = false;// La date a-t-elle été choisie ici ?
	
	// Eléments d'affichage
	private Month monthDisplayed;		// Le mois affiché
	private JLabel titleMonth;			// Affichage du nom du mois
	private DateButton[] jours;			// Les boutons pour chaque jour affiché
	private Component mouseOver = null;	// Composant survolé
	
	/**
	 * Une étiquette améliorée qui gère les clics, les dates (optionnellement)
	 * et son apparence en fonction de la date associée.
	 * Elle sert pour la plupart des éléments cliquables du calendrier tels que
	 * jours du calendrier, bouton de validation et d'annulation.
	 * L'avantage de cette classe est qu'elle permet de modifier et rétablir
	 * facilement un style de couleurs, qui permet ainsi de gérer le survol par
	 * la souris.
	 */
	class DateButton extends JLabel implements MouseListener {
		
		/**
		 * La date, s'il y en a une (par défaut: <code>null</code>).
		 */
		private Date date = null;
		
		/**
		 * Construit une étiquette cliquable sans texte spécifique.
		 * Cet objet a vocation à servir pour les jours du mois. Elle gère
		 * elle-même les réponses aux clics.
		 * 
		 * @param motion	La classe qui gère le survol par la souris.
		 */
		public DateButton(MouseMotionListener motion) {
			this(motion, "");
			addMouseListener(this);					// Ecouter le clic
		}
		
		/**
		 * Construit une étiquette cliquable avec le texte spécifié.
		 * <p>
		 * Ce constructeur ne prévoit pas de gestion des clics, ce qui
		 * correspond notamment aux boutons "effacer" et "annuler", dont les
		 * <code>MouseListener</code>s sont définis par la classe englobante.
		 * 
		 * @param motion	La classe qui gère le survol par la souris.
		 */
		public DateButton(MouseMotionListener motion, String text) {
			super(text);							// Etiquette avec texte
			setHorizontalAlignment(SwingConstants.CENTER);	// Centrer
			setBackground(FOND);					// Couleur de fond
			setOpaque(true);						// Dessiner le fond (opaque)
			addMouseMotionListener(motion);			// Ecouter les mouvements	
		}

		public Date getDate() {
			return date;
		}
		
		/**
		 * Mémorise la date spécifiée et adapte le texte et l'apparence du
		 * bouton en fonction de cette date.
		 * 
		 * @param date	La nouvelle date.
		 */
		public void setDate(Date date) {
			this.date = date;
			setText(dayOfMonth.format(date));	// Ecrire la date sur le bouton
			updateStyle();
		}
		
		/**
		 * Modifie l'apparence du bouton en fonction de la date associée.
		 * <p>
		 * L'apparence diffère si la date correspond à la date actuellement
		 * stockée par l'éditeur, ou si elle ne fait pas partie du mois affiché
		 * (fin du mois précédent ou début du mois suivant).
		 * <p>
		 * S'il n'y a pas de date, il s'agit de l'apparence standard.
		 */
		public void updateStyle() {
			if (date != null && 
					(date.equals(editorDate)		// Sélection ?
						|| date.equals(today))) {	// ou date du jour ?
				setForeground(ACTUAL);				// Police de sélection
				setBackground(SELECTION);			// Fond de sélection
				setFont(BOLD);						// Gras
			} else {
				setFont(NORMAL);					// Pas gras
				setBackground(FOND);				// Couleur de fond
				if (date == null || monthDisplayed.includes(date)) {//Ce mois ?
					setForeground(STANDARD);	// Police couleur du mois
				} else {
					setForeground(INACTIF);		// Police couleur autres mois
				}
			}
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			editorDate = date;						// Mémoriser la date
			userClicked = true;						// L'utilisateur a cliqué
			stopCellEditing();						// Arrêter l'édition
		}

		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
	}// inner class DateButton
	
	/**
	 * Une instance utilisant l'éditeur délégué spécifié.
	 * 
	 * @param editor	L'éditeur délégué qui apparaîtra dans la cellule.
	 */
	public PopupDateEditor(TableCellEditor editor) {
		delegate = editor;
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		// Initialisation
		if (value instanceof Date) {	// Si la valeur actuelle est une date
			editorDate = (Date) value;				// Cette valeur par défaut
		} else {						// Sinon
			editorDate = today;
		}
		userClicked = false;				// Date pas encore choisie
		delegate.addCellEditorListener(this);// (Ré-)écouter l'éditeur délégué

		// Composant pop-up
		JPanel popupCal = new JPanel(new BorderLayout());
		popupCal.addMouseWheelListener(this);			// Ecouter la molette
		popupCal.setBorder(								// Bordure
				BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		// Eléments de la partie supérieure
		titleMonth = new JLabel();						// Affichage du mois
		titleMonth.setHorizontalAlignment(JLabel.CENTER);// Centré
		JButton prevYear	= new JButton("<<");		// Année précédente
		JButton nextYear	= new JButton(">>");		// Année suivante
		JButton prevMonth	= new JButton("<");			// Mois précédent
		JButton nextMonth	= new JButton(">");			// Mois suivant
		prevYear.setActionCommand(PREVIOUS_YEAR);		// Les actions attachées
		nextYear.setActionCommand(NEXT_YEAR);
		prevMonth.setActionCommand(PREVIOUS_MONTH);
		nextMonth.setActionCommand(NEXT_MONTH);
		
		// Pour tous les boutons de navigation
		JButton[] navigation = {prevYear, nextYear, prevMonth, nextMonth};
		for (JButton bouton : navigation) {
			bouton.addActionListener(this);				// Ecouter les actions
			bouton.setBorder(							// Bordure minimaliste
					BorderFactory.createEmptyBorder(5,5,5,5));
		}// for boutons de navigation

		// Disposer la partie supérieure
		JPanel title = new JPanel(new BorderLayout());
		JPanel titleInner = new JPanel(new BorderLayout());
		title.setBackground(FOND);						// Couleur de fond
		titleInner.setBackground(FOND);
		titleInner.add(titleMonth);						// Centre du petit panel
		titleInner.add(prevMonth, BorderLayout.WEST);	// Gauche intérieure
		titleInner.add(nextMonth, BorderLayout.EAST);	// Droite intérieure
		title.add(titleInner);							// Centre du grand panel							
		title.add(prevYear, BorderLayout.WEST);			// Gauche extérieure
		title.add(nextYear, BorderLayout.EAST);			// Droite extérieure
		popupCal.add(title, BorderLayout.NORTH);		// Partie haute du popup

		// Corps du pop-up: en-tête + 6 semaines
		JPanel corps = new JPanel(new GridLayout(7,7));	// Définir une grille
		corps.setBackground(FOND);						// Couleur de fond
		popupCal.add(corps);							// Insérer dans le panel

		// En-tête de semaine
		Calendar week = Calendar.getInstance();			// Un calendrier
		while (week.get(Calendar.DAY_OF_WEEK) != week.getFirstDayOfWeek()) {
			week.add(Calendar.DAY_OF_MONTH, -1);		// 1er jour de semaine
		}
		for (int i=0; i<7; i++ ) {
			JLabel nomJour = new JLabel(""+				// Etiquette
					week.getDisplayName(				// Jour de la semaine...
							Calendar.DAY_OF_WEEK,
							Calendar.SHORT, Locale.getDefault())
					.toUpperCase()						// ...en majuscules
					.charAt(0));						// ...première lettre
			nomJour.setHorizontalAlignment(
					SwingConstants.CENTER);				// Centrer
			nomJour.setFont(BOLD);						// Police grasse
			corps.add(nomJour);							// Ajouter à la grille
			week.add(Calendar.DAY_OF_MONTH, 1);			// Jour suivant
		}

		// Corps du calendrier
		jours = new DateButton[42];			// 42 cases (6 lignes 7 colonnes)
		for (int i=0; i<jours.length; i++) {		// Créer les boutons
			DateButton bouton = new DateButton(this);
			jours[i] = bouton;						// Mémoriser le bouton
			corps.add(bouton);						// Ajouter au panel
		}
		// Dériver la police de base
		NORMAL = jours[0].getFont();				// Police normale
		BOLD = NORMAL.deriveFont(Font.BOLD);		// Police grasse
		
		// Partie basse
		JPanel bas = new JPanel(new GridLayout(1,2));	// Le panel (2 parties)
		DateButton effacer = new DateButton(this, "Effacer");// Bouton effacer
		DateButton annuler = new DateButton(this, "Annuler");// Bouton annuler
		effacer.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {// Bouton "effacer" cliqué
				editorDate = null;					// Effacer la date
				userClicked = true;					// L'utilisateur a cliqué !
				stopCellEditing();					// Terminer l'édition
			}
			
		});// classe anonyme effacer
		annuler.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {// Bouton "annuler" cliqué
				cancelCellEditing();				// Annuler l'édition
			}
			
		});// classe anonyme annuler
		bas.add(effacer);							// Ajouter au panel du bas
		bas.add(annuler);
		popupCal.add(bas, BorderLayout.SOUTH);		// Ajouter au grand panel

		// Remplir les données du calendrier
		monthDisplayed = (editorDate == null)
				? Month.getInstance()				// Mois d'aujourd'hui
				: Month.getInstance(editorDate);	// ou mois sélectionné
		updateCalendar();							// Remplir les données
		
		// Gérer l'annulation de l'édition par la touche Echap
		popupCal.getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)	// InputMap
		.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),		// Touche Echap
				"cancel");
		popupCal.getActionMap()									// ActionMap
		.put("cancel", new AbstractAction() {					// Annulation
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cancelCellEditing();	// Annuler l'édition
			}
			
		});// anonymous AbstractAction
		
		// Dimension du calendrier
		popupCal.setPreferredSize(DIMENSION);
		
		// Positionnement du pop-up
		Point p = table.getLocationOnScreen();		// Position de la table
		Rectangle rect = table.getCellRect(			// Cellule
				row, column, false);
		p.translate(rect.x, rect.y);				// Position de la cellule
		Point p2 = new Point(p.x, p.y + rect.height);// Point bas
		if (table.getRootPane().contains(p2)) {		// Assez de place en bas ?
			p.translate(0, rect.height);			// Position sous la cellule
		} else {
			p.translate(0, -popupCal.getHeight());	// Mettre au-dessus
		}
		
		// Montrer le calendrier pop-up
		popup = factory.getPopup(table, popupCal, p2.x, p2.y);
		popup.show();
		
		// Renvoyer le composant de l'éditeur délégué
		return delegate.getTableCellEditorComponent(
				table, value, isSelected, row, column);
	}
	
	/**
	 * Remplit le calendrier en fonction de la date et du mois spécifiés.
	 * 
	 * @param mois	Le mois à afficher.
	 * @param date	La date à sélectionner. Si elle ne fait pas partie du mois,
	 * 				Alors qu'aucune date n'apparaît comme sélectionnée.
	 */
	private void updateCalendar() {
		
		titleMonth.setText(monthFormat.format(		// Nom du mois
				monthDisplayed.getFirstDay()));
		
		// Début du calendrier
		Calendar cal = Calendar.getInstance();		// Calendrier
		cal.setTime(monthDisplayed.getFirstDay());	// 1er jour de ce mois
		while (cal.get(Calendar.DAY_OF_WEEK)
				!= cal.getFirstDayOfWeek()) {	// Déplacer au début de semaine
			cal.add(Calendar.DAY_OF_MONTH, -1);	// La veille
		}

		for (int i=0; i<jours.length; i++) {
			Date cursorDate = cal.getTime();			// Le curseur de dates
			jours[i].setDate(cursorDate);
			cal.add(Calendar.DAY_OF_MONTH, 1);			// Voir le lendemain...
		}
	}
	
	/**
	 * Renvoie la valeur de l'éditeur.
	 * 
	 * @return	La valeur sur laquelle l'utilisateur a cliqué. Si l'utilisateur
	 * 			n'a pas cliqué sur le calendrier, renvoie la date de l'éditeur
	 * 			délégué, ou null si ce n'est pas une date.
	 */
	@Override
	public Object getCellEditorValue() {
		if (userClicked) {
			return editorDate;
		} else {
			Object value = delegate.getCellEditorValue();
			return (value instanceof Date) ? value : null;
		}
	}
	
	/**
	 * Cette méthode change le mois affiché par le calendrier popup en fonction
	 * de la commande passée.
	 * 
	 * @param e	Un <code>ActionEvent</code> contenant une commande de navigation
	 * 			PREVIOUS_YEAR, NEXT_YEAR, PREVIOUS_MONTH,NEXT_MONTH. */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// Partir du mois actuellement affiché
		Calendar cal = Calendar.getInstance();
		cal.setTime(monthDisplayed.getFirstDay());
		
		// Etudier l'action transmise
		String command = e.getActionCommand();
		if (PREVIOUS_YEAR.equals(command)) {
			cal.add(Calendar.YEAR, -1);				// Année précédente
		} else if (NEXT_YEAR.equals(command)) {
			cal.add(Calendar.YEAR, 1);				// Année suivante
		} else if (PREVIOUS_MONTH.equals(command)) {
			cal.add(Calendar.MONTH, -1);			// Mois précédent
		} else if (NEXT_MONTH.equals(command)) {
			cal.add(Calendar.MONTH, 1);				// Mois suivant
		} else {
			return;						// Rien du tout: pas de mise à jour !
		}
		monthDisplayed =
				Month.getInstance(cal.getTime());	// Redéfinir le mois affiché
		updateCalendar();							// Mettre à jour
	}

	/**
	 * Renvoie vers l'éditeur délégué.
	 */
	@Override
	public boolean isCellEditable(EventObject anEvent) {
		return delegate.isCellEditable(anEvent);
	}

	/**
	 * Renvoie vers l'éditeur délégué.
	 */
	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return delegate.shouldSelectCell(anEvent);
	}

	/**
	 * Stoppe l'éditeur sous-jacent et prévient les Listeners.
	 * 
	 * @return	la valeur retournée par l'éditeur délégué.
	 */
	@Override
	public boolean stopCellEditing() {
		fireEditingStopped();
		delegate.removeCellEditorListener(this);	// Cesser d'écouter avant
		return delegate.stopCellEditing();			// Arrêter le délégué après
	}

	/**
	 * Cessz d'écouter l'éditeur délégué, l'arrête et prévient les Listeners.
	 */
	@Override
	public void cancelCellEditing() {
		fireEditingCanceled();
		delegate.removeCellEditorListener(this);	// Cesser d'écouter avant
		delegate.cancelCellEditing();				// Annuler le délégué après
	}

	/**
	 * Interface CellEditorListener: méthode invoquée par l'éditeur délégué.<br>
	 * Retient la valeur de l'éditeur délégué si elle est valide, cesse
	 * de l'écouter et avertit les Listeners.<br>
	 * Par contre, si l'éditeur délégué contient un booléen qui vient d'être
	 * activé (<code>true</code>), la méthode ne fait rien: il convient de
	 * laisser l'utilisateur choisir une date sur le calendrier.
	 */
	@Override
	public void editingStopped(ChangeEvent e) {
		Object delegateValue = delegate.getCellEditorValue();
		if (delegateValue instanceof Boolean					// Un booléen
				&& ((Boolean) delegateValue).booleanValue()) {	// Vrai
			return;												// Ne rien faire
		}
		delegate.removeCellEditorListener(this);
		fireEditingStopped();
	}

	/**
	 * Interface CellEditorListener: méthode invoquée par l'éditeur délégué.
	 * Dans le cas général, cesse d'écouter l'éditeur délégué et prévient les
	 * Listeners.
	 */
	@Override
	public void editingCanceled(ChangeEvent e) {
		delegate.removeCellEditorListener(this);
		fireEditingCanceled();
	}
	
	/**
	 * Juste pour intercepter des annulations émanant de la table, et pas
	 * seulement d'un des éditeurs. Cela permet de masquer le pop-up.
	 */
	@Override
	public void removeCellEditorListener(CellEditorListener l) {
		popup.hide();
		super.removeCellEditorListener(l);
	}
	
	/**
	 * Change de mois en fonction du sens de rotation de la molette.
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() < 0) {
			actionPerformed(new ActionEvent(		// Mois précédent
					this, ActionEvent.ACTION_PERFORMED, PREVIOUS_MONTH));
		} else {
			actionPerformed(new ActionEvent(		// Mois suivant
					this, ActionEvent.ACTION_PERFORMED, NEXT_MONTH));
		}
	}

	/**
	 * Modifie l'aspect du composant source lorsqu'il est (ou n'est plus)
	 * survolé.
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		Component source = e.getComponent();
		if (!source.equals(mouseOver)) {		// Nouveau composant survolé
			source.setBackground(SELECTION);	// Coloriser
			source.setForeground(ACTUAL);
			if (mouseOver instanceof DateButton) {			// et s'il y avait un précédent
				((DateButton) mouseOver).updateStyle();
			}
			mouseOver = source;					// Mémoriser le composant
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {}
}
