package haas.olivier.diagram;

import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.JComponent;


/** Un composant Swing représentant un diagramme les étiquettes des axes.
 * <p>
 * On utilise un 
 * {@link haas.olivier.diagram.DiagramComponent DiagramComponent}, et des
 * {@link haas.olivier.diagram.XLabels DiagramXPainter} et
 * {@link haas.olivier.diagram.YLabels DiagramYPainter}, qui ne sont pas
 * des <code>Component</code>s, et qui servent uniquement à compléter le rendu
 * graphique du diagramme pour l'affichage des étiquettes.
 * 
 * @author Olivier HAAS
 */
@SuppressWarnings("serial")
public class DiagramAndAxisComponent extends JComponent
implements DiagramModelObserver{
	
	/** La zone centrale du diagramme. */
	private DiagramComponent zone;
	
	/** Les étiquettes des abscisses. */
	private XLabels xLabels;
	
	/** Les étiquettes des ordonnées. */
	private YLabels yLabels;
	
	/** Construit la représentation graphique d'un diagramme.
	 * 
	 * @param zone		Le dessinateur de la zone graphique du diagramme.
	 * @param xLabels	Les étiquettes des abscisses.
	 * @param yLabels	Les étiquettes des ordonnées.
	 */
	public DiagramAndAxisComponent(DiagramComponent zone, XLabels xLabels,
			YLabels yLabels) {
		this.zone = zone;
		this.xLabels = xLabels;
		this.yLabels = yLabels;
		setLayout(new DiagramLayout(this));
		
		// Apparence
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// Ajouter les composants
		add(zone);
		add(xLabels);
		add(yLabels);
		
		// Se définir comme observateur du modèle à la place de la zone
		DiagramModelObservable observable = 
				zone.getPainter().getModel().getObservable();
		observable.removeObserver(zone);
		observable.addObserver(this);
	}// constructeur
	
	/** Définit un nouveau layout manager, à condition qu'il s'agisse d'un
	 * {@link haas.olivier.diagram.DiagramLayout DiagramLayout}.
	 */
	@Override
	public void setLayout(LayoutManager layout) {
		if (!(layout instanceof DiagramLayout)) {
			throw new IllegalArgumentException(
					"DiagramComponent n'accepte que des DiagramLayout");
		}// if
		
		super.setLayout(layout);
	}// setLayout
	
	/** Renvoie le composant de la zone graphique du diagrame. */
	public DiagramComponent getMainZone() {
		return zone;
	}// getZone
	
	/** Renvoie les étiquettes des abscisses. */
	XLabels getXLabels() {
		return xLabels;
	}// getXPainter
	
	/** Renvoie les étiquettes des ordonnées. */
	YLabels getYLabels() {
		return yLabels;
	}// getYPainter

	@Override
	public void diagramChanged() {
		yLabels.reloadGraduations();
		xLabels.reloadXValues();
		revalidate();
		repaint();
	}// labelsYChanged
}

