/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/** Un observable statique qui surveille l'état de la mémoire heap.
 * <p>
 * Cette classe ne peut pas implémenter {@link haas.olivier.util.Observable} car
 * elle utilise une {@link java.util.Queue} pour gérer ses observateurs, et non
 * pas une {@link java.util.List}.<br>
 * De toute façon, la classe est entièrement statique et c'est logique puisqu'il
 * s'agit par nature de considérations globales de la JVM.
 * <p>
 * Au départ, la classe s'instanciait normalement. Le but était de maintenir une
 * instance par "module" de l'application (par exemple un par dossier) afin de
 * permettre au ramasse-miettes de supprimer les observateurs lorsque l'ensemble
 * du module était éligible. Ce comportement a été remplacé par un système de
 * références faibles, qui sont faites exactement pour ça.
 * 
 * @author Olivier HAAS
 */
public class MemoryObservable {

	/** Le seuil d'activation (32 Mo). */
	private static final int ALERT = 1 << 25;
	
	/** Le seuil critique (256 ko). */
	private static final int CRITIC = 1 << 18;
	
	/** Le runtime en cours. */
	private static final Runtime RUNTIME = Runtime.getRuntime();
	
	/** La taille maximale de la mémoire de la JVM. */
	private static final long MAX_MEM = RUNTIME.maxMemory();
	
	/** Les observateurs. */
	private static final Queue<WeakReference<MemoryObserver>> OBSERVERS =
			new LinkedList<WeakReference<MemoryObserver>>();
	
	/** Ajoute un observateur de mémoire.
	 * <p>
	 * Si cet observateur est déjà enregistré, il figurera deux fois dans la
	 * collection des observateurs.
	 * <p>
	 * L'observable ne garde qu'une référence faible
	 * ({@link java.lang.ref.WeakReference WeakReference}) vers l'observateur.
	 * Il est donc inutile de le désenregistrer.
	 * 
	 * @param observer	Le nouvel observateur
	 */
	public static synchronized void addObserver(MemoryObserver observer) {
		OBSERVERS.add(new WeakReference<MemoryObserver>(observer));
	}// addObserver
	
	/** Vérifie l'état de la mémoire et lance des procédures de délestage auprès
	 * des observateurs si nécessaire.
	 * 
	 * @throws CriticalMemoryException
	 * 				Si la mémoire est dans un état critique.
	 */
	public static synchronized void check() throws CriticalMemoryException {
		
		// Nettoyer la file en enlevant les références obsolètes
		Iterator<WeakReference<MemoryObserver>> it = OBSERVERS.iterator();
		while (it.hasNext()) {
			if (it.next().get() == null)
				it.remove();
		}// while
		
		// Tester si la mémoire est critique
		if (isLow()) {
			
			/* Délester les observateurs au plus une fois chacun.
			 * 
			 * On fait une rotation sur les observateurs pour ne pas solliciter
			 * toujours les mêmes: avec plusieurs appels successifs rapprochés,
			 * ça risquerait de bloquer inutilement l'application en délestant
			 * toujours de petites données sur le même observateur.
			 * 
			 * Pour éviter une boucle infinie, on exécute la boucle autant de
			 * fois qu'il y a d'observateurs, et on s'arrête quand l'un d'eux a
			 * réussi.
			 */
			for (int i=OBSERVERS.size(); i>0; i--) {// Autant de fois que d'obs
				
				// Récupérer l'observateur suivant et sa référence faible
				WeakReference<MemoryObserver> observerRef =
						OBSERVERS.poll();
				MemoryObserver observer = observerRef.get();

				// Si l'observateur existe encore
				if (observer != null) {
					
					// Déplacer l'observateur à la fin de la queue
					OBSERVERS.add(observerRef);

					// Demander un délestage
					try {
						if (observer.deleste()) {	// Si ça fonctionne
							System.gc();			// Suggérer une collecte
							return;					// Arrêter ici
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}// try
				}// if observateur
			}// for observateur
		}// if is low
	}//check
	
	/** Indique si la mémoire libre est en-dessous du seuil d'activation. 
	 * 
	 * @throws CriticalMemoryException
	* 				Si la mémoire est dans un état critique.
	*/
	private static boolean isLow() throws CriticalMemoryException {
		
		// Calculer la mémoire encore libre
		final long freeMem = RUNTIME.freeMemory()
				+ (MAX_MEM == Long.MAX_VALUE	// Ajouter extensions possibles
						? 0
						: MAX_MEM - RUNTIME.totalMemory());
		
		// Avertir si la mémoire est dans un état critique
		if (freeMem < CRITIC)
			throw new CriticalMemoryException();
		
		// Renvoyer le résultat (seuil d'alerte dépassé ou non)
		return freeMem < ALERT;
	}// isLow
	
	
	/** Constructeur privé pour interdire l'instanciation. */
	private MemoryObservable() {}
}
