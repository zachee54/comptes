package haas.olivier.diagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.math.BigDecimal;

/** Un décorateur de dessinateur de diagramme qui ajoute une grille horizontale
 * et un cadre.
 * 
 * @author Olivier HAAS
 */
public class GridPainter implements Painter {

	/** Le dessinateur délégué. */
	private final Painter delegate;
	
	/** Construit un dessinateur de grille de diagramme.
	 * 
	 * @param delegate	Le dessinateur de diagrammes délégué.
	 */
	public GridPainter(Painter delegate) {
		this.delegate = delegate;
	}// constructeur
	
	@Override
	public void paintDiagram(Graphics2D g, Rectangle bounds) {
		
		// Utiliser des pointillés fins
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(
				1.0f,
				BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_BEVEL,
				10.0f,
				new float[] {1.0f, 4.0f},
				1.0f));
		
		// Graduations horizontales
		Echelle echelle = getEchelle();
		for (BigDecimal graduation : echelle.getGraduations()) {
			int y = echelle.getYFromValue(graduation, bounds.height);
			g2.drawLine(0, y, bounds.width, y);
		}// for graduation
		
		// Dessiner le reste normalement	
		delegate.paintDiagram(g, bounds);
		
		// Dessiner le cadre
		g.setStroke(new BasicStroke(1.0f));
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, bounds.width-1, bounds.height-1);
	}// paint
	
	@Override
	public void paintSample(Serie serie, Graphics2D g, Rectangle bounds) {
		delegate.paintSample(serie, g, bounds);
	}// paintSample

	@Override
	public String getToolTipText(Point p, Rectangle bounds) {
		return delegate.getToolTipText(p, bounds);
	}// getToolTipText

	@Override
	public DiagramModel getModel() {
		return delegate.getModel();
	}// getModel

	@Override
	public Echelle getEchelle() {
		return delegate.getEchelle();
	}// getEchelle
}
