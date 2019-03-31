/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.diagram;


import java.awt.Color;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** Une légende pour les diagrammes.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class Legend extends JScrollPane implements DiagramModelObserver {

	/** Le dessinateur de diagramme. */
	private final Painter painter;
	
	/** Le contenu du scroll pane. */
	private final JPanel content = new JPanel();
	
	/** Construit une légende de diagramme.
	 * 
	 * @param painter
	 * 				Le dessinateur de diagramme. Il est utilisé pour accéder au
	 * 				modèle de données et pour obtenir un aperçu du tracé de la
	 * 				série.
	 */
	public Legend(Painter painter) {
		
		// Définir le comportement des barres de défilement
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		this.painter = painter;
		
		// Apparence
		setBorder(null);								// Pas de bordure
		content.setBackground(Color.white);				// Fond blanc
		
		// Ajouter le contenu
		setViewportView(content);
		
		// Disposition interne du contenu
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		
		// Créer le contenu
		createContent();
		
		// Écouter les changements dans le modèle
		painter.getModel().getObservable().addObserver(this);
	}// constructeur
	
	/** Crée le contenu de la légende : liste des séries et l'aperçu de leur
	 * tracé.
	 */
	private void createContent() {
		content.removeAll();
		
		content.add(Box.createVerticalGlue());
		for (Serie serie : painter.getModel().getSeries()) {
			JLabel label = new JLabel(
					serie.toString(),
					new SerieSample(serie, painter),
					JLabel.LEFT);
			// TODO position du texte ?
			content.add(label, 0);
		}// for séries
		content.add(Box.createVerticalGlue(), 0);
	}// createContent

	@Override
	public void diagramChanged() {
		createContent();
	}// dataChanged
}
