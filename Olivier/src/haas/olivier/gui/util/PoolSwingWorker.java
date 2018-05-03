/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.gui.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingWorker;

/**
 * Un <code>SwingWorker</code> qui utilise un pool personnalisé.
 * <p>
 * Depuis le JDK 6u19 (au moins), la classe <code>SwingWorker</code> utilise un
 * pool de thread composé de... un seul thread. Par conséquent, les tâches
 * d'arrière-plan s'exécutent l'une après l'autre sans chevauchement.
 * <p>
 * Bug or feature ?? En tout cas cela pose problème dans mes applications.<br>
 * D'abord pour prioriser les tâches.<br>
 * Ensuite, parce que si l'utilisateur lance une tâche et la remplace aussitôt
 * par une autre, il est de bon ton d'annuler la première tâche devenue
 * osbolète. Sauf que ce n'est pas toujours possible, notamment si les tâches
 * s'enchaînent par effet domino, y compris à des endroits très éloignés dans le
 * code. Dans ce cas, la nouvelle tâche doit attendre patiemment que les tâches
 * "domino" soient terminées. Bien que l'UI ne soit pas bloquée (car l'EDT ne
 * l'est pas), cela donne l'impression à l'utilisateur que l'application ne
 * réagit plus.
 * <p>
 * Cette classe personnalise <code>SwingWorker</code> en utilisant un autre pool
 * pour les tâches à exécuter.
 * <p>
 * Cela oblige, en bonne pratique, à fermer l'<code>ExecutorService</code> en
 * en appelant <code>shutdown</code> juste avant de quitter l'application.
 * 
 * @author Olivier HAAS
 *
 * @param <T>
 * @param <V>
 */
public abstract class PoolSwingWorker<T,V> extends SwingWorker<T, V> {

	public static final ExecutorService EXECUTOR =
			Executors.newCachedThreadPool();
	
	/**
	 * Exécute la tâche dans le pool personnalisé.
	 */
	public void executeInPool() {
		EXECUTOR.execute(this);
//		execute();
	}
}
