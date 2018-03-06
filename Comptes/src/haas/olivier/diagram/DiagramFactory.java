package haas.olivier.diagram;

/** Une classe utilitaire fabriquant des diagrammes.
 * 
 * @author Olivier Haas
 */
public class DiagramFactory {

	/** Crée un diagramme affichant des courbes.
	 * <p>
	 * Les étiquettes de l'axe des abscisses sont sur les graduations (et non
	 * pas entre les graduations), et il existe des étiquettes de l'axe des
	 * ordonnées.
	 * 
	 * @param model	Le modèle de données à utiliser pour tracer la courbe.
	 * 
	 * @return		Un composant contenant le diagramme et les étiquettes des
	 * 				axes.
	 */
	public static DiagramAndAxisComponent newCourbe(DiagramModel model) {
		return newEdgeDiagram(new GridPainter(new PainterCourbe(model)));
	}// newCourbe
	
	/** Crée un diagramme affichant des aires cumulées.
	 * <p>
	 * Les étiquettes de l'axe des abscisses sont sur les graduations (et non
	 * pas entre les graduations), et il existe des étiquettes de l'axe des
	 * ordonnées.
	 * 
	 * @param model	Le modèle de données à utiliser pour tracer une courbe.
	 * 
	 * @return
	 */
	public static DiagramAndAxisComponent newAire(DiagramModel model) {
		return newEdgeDiagram(new GridPainter(new PainterAire(model)));
	}// newAire
	
	/** Crée un diagramme utilisant dont chaque abscisse correspond à une
	 * graduation.
	 * 
	 * @param painter	Le dessinateur de diagramme.
	 * 
	 * @return			Un diagramme avec son contexte, les étiquettes des
	 * 					abscisses correspondant à une graduation.
	 */
	private static DiagramAndAxisComponent newEdgeDiagram(
			Painter painter) {
		DiagramModel model = painter.getModel();
		
		DiagramComponent zone = new DiagramComponent(painter);
		zone.setBorder(DiagramBorder.newDiagramBorder(model, false));
		
		return new DiagramAndAxisComponent(
				zone,
				new XLabels(model, new EdgeXLabelsLayout()),
				new YLabels(painter.getEchelle()));
	}// newEdgeDiagram
}
