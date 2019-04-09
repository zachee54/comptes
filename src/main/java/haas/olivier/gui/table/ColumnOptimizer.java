/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.table;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumnModel;

/**
 * Un optimiseur des largeurs de colonnes.
 * <p>
 * L'optimisation des colonnes est un algorithme qui se peut se révéler
 * chronophage si la table est grande ou si les Renderer sont complexes. Il
 * risque donc de provoquer un gel de l'interface graphique et doit donc être
 * exécuté en environnement multi-thread.<br>
 * Cela étant, l'algorithme utilise en permanence des objets Swing (
 * <code>JTable</code>, <code>TableModel</code>, <code>TableCellRenderer</code>,
 * etc) qui ne peuvent être accédés que par l'Event Dispatch Thread ; sinon on
 * risque de provoquer des erreurs de concurrence, donc des exceptions ou des
 * erreurs de données.
 * <p>
 * La solution adoptée ici consiste à morceler l'algorithme en petites tâches
 * indépendantes, à savoir l'optimisation d'<i>une</i> colonne. Ces tâches sont
 * exécutées dans l'EDT.<br>
 * Toutefois, elles ne doivent pas être programmées toutes en même temps, sinon
 * elles s'exécuteront l'une après l'autre de manière quasi-ininterrompue dans
 * l'EDT et provoqueront donc un gel de l'interface. Pour que l'interface puisse
 * fonctionner normalement, il faut permettre à d'autres tâches Swing de
 * s'intercaler entre deux traitements de colonnes. Pour cela, on ne programme
 * l'optimisation d'une colonne dans l'EDT qu'une fois que la précédente est
 * terminée, grâce à {@link javax.swing.SwingUtilities#invokeAndWait(Runnable)}.
 * <p>
 * La classe hérite de <code>Thread</code>, parce qu'il faut bien mobiliser un
 * thread à part entière pour programmer les tâches Swing d'optimisation des
 * colonnes.<br>
 * Cet algorithme ne peut pas être exécuté dans un <code>SwingWorker</code>.
 * Celui-ci est prévu pour exécuter une tâche unique en arrière-plan puis dans
 * l'EDT, et ne permet pas de récupérer la main à la fin de l'exécution de la
 * tâche dans l'EDT. Il autorise des tâches Swing successives, mais celles-ci
 * sont programmées sur le mode de fonctionnement de
 * <code>SwingUtilities.invokeLater(Runnable)</code>, c'est-à-dire que toutes
 * les tâches Swing risqueraient d'être programmées quasi en même temps, et non
 * au fur et à mesure de l'achèvement des précédentes.
 * 
 * @author Olivier HAAS
 */
public class ColumnOptimizer extends Thread {
	
	/**
	 * Une collection des instances en cours pour chaque table.
	 */
	private static Map<JTable, ColumnOptimizer> working =
			new HashMap<JTable, ColumnOptimizer>();

	/**
	 * La table à optimiser.
	 */
	private final JTable table;
	
	/**
	 * Le modèle des colonnes.
	 */ 
	private final TableColumnModel cm;
	
	/**
	 * La tâche de redimensionnement utilisée pour chaque colonne.
	 */
	private final ColumnOptimizerTask task;
	
	/**
	 * L'index de la colonne en cours d'optimisation.
	 */
	private int index;

	/**
	 * Construit un optimiseur de largeurs de colonnes.
	 * 
	 * @param table	La table à optimiser.
	 */
	private ColumnOptimizer(JTable table) {
		this.table = table;
		this.cm = table.getColumnModel();
		this.task = new ColumnOptimizerTask();
	}
	
	/**
	 * Optimise en arrière-plan les largeurs de colonnes d'une table.<br>
	 * Si une tâche d'optimisation est en cours pour la même table, elle est
	 * arrêtée.
	 * 
	 * @param table	La table à optimiser.
	 */
	public static synchronized void optimize(JTable table) {
		new ColumnOptimizer(table).start();
	}

	/**
	 * Enregistrer une instance auprès de la classe.
	 * 
	 * La synchronisation <b>au niveau de la classe</b> est nécessaire non
	 * seulement parce que <code>HashMap</code> n'est pas synchronisée, mais
	 * aussi pour éviter qu'une autre instance ne s'enregistre entre la
	 * consultation de la map et l'insertion de l'instance concernée.
	 * 
	 * @param optimizer	L'instance à enregistrer.
	 */
	private static synchronized void register(ColumnOptimizer optimizer) {
	
		// Arrêter une éventuelle tâche en cours sur la même table
		ColumnOptimizer co = working.get(optimizer.table);
		if (co != null)
			co.interrupt();
	
		// Enregistrer l'instance
		working.put(optimizer.table, optimizer);
	}

	/**
	 * Désenregistre une instance si elle est toujours associée à la table
	 * correspondante.
	 * 
	 * @param optimizer	L'instance à désenregistrer.
	 */
	private static synchronized void unregister(ColumnOptimizer optimizer) {
		if (working.get(optimizer.table) == optimizer)
			working.remove(optimizer.table);
	}

	/**
	 * Optimise les largeurs de colonnes par tâches successives dans l'EDT.
	 */
	@Override
	public void run() {
		try {
			register(this);
			int nbCols = cm.getColumnCount();
			for (index = 0; index < nbCols && !isInterrupted() ; index++) {
				
				/*
				 * invokeAndWait permet de garantir la cohérence de index,
				 * contrairement à invokeLater.
				 */
				SwingUtilities.invokeAndWait(task);
			}
			
		} catch (InterruptedException e) {
			interrupt();
		} catch (InvocationTargetException e) {
			Logger.getLogger(ColumnOptimizer.class.getName()).log(
					Level.WARNING,
					"Erreur pendant l'ajustement des colonnes",
					e);

		} finally {
			unregister(this);
		}
	}
	
	/**
	 * Une tâche de d'optimisation de colonne. Elle optimise une colonne à la
	 * fois.
	 * <p>
	 * On pourrait utiliser une classe à instancier pour chaque colonne, mais
	 * c'est une perte de performance inutile. Il est préférable d'instancier un
	 * objet unique capable de traiter n'importe quel index de colonne.
	 * <p>
	 * Dans la mesure où cet objet n'est exécuté que par un thread à la fois (en
	 * l'occurrence l'Event Dispatch Thread, chaque exécution n'étant au surplus
	 * programmée qu'après la fin de l'exécution précédente), il n'y a aucun
	 * problème de concurrence sur l'index de colonne à utiliser.
	 *
	 * @author Olivier HAAS
	 */
	private class ColumnOptimizerTask implements Runnable {

		/**
		 * Optimise la largeur de la colonne portant l'index en cours, s'il est
		 * toujours valable.
		 * <p>
		 * Si le modèle de table a changé entre deux invocations, l'index peut
		 * être obsolète.
		 */
		@Override
		public void run() {
			if (index < cm.getColumnCount()) {
				cm.getColumn(index).setPreferredWidth(
						SmartTable.getLargeurUtileColonne(table, index));
			}
		}
	}
}