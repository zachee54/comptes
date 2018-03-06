/*
 * Copyright (c) 2018 Olivier HAAS - Tous droits réservés
 */
package haas.olivier.util;

public class StringSimilitude {
	
	public static void main(Object[] args) {
//		if (args.length < 2) {
//			throw new IllegalArgumentException("Au moins deux chaînes !");
//		}
		
		String a = "Christophe Vaux";
		String b = "_Christian Valdenaire Christophe";
		
		char[] s1 = a.toString().toLowerCase().toCharArray();
		char[] s2 = b.toString().toLowerCase().toCharArray();
		
		String simil = "";					// Similitude globale
		
		// Parcourir la chaîne 1
		for (int i=0; i<s1.length; i++) {
			String buff = "";				// Plus grande similitude d'ici
			int max = 0; 					// Longueur plus grande similitude
			int n=0;						// Longueur d'une similitude
			
			// A partir de ce point de la chaîne 1, parcourir la chaîne 2
			for (int j=0; j<s2.length; j++) {
				String temp = "";			// Buffer de similitude
				
				// Rechercher une similitude à partir des points i et j
				while (i+n < s1.length				// Avant la fin de s1
						&& j+n < s2.length			// Avant la fin de s2
						&& s1[i+n] == s2[j+n]) {	// Et similitude sur n
					n++;					// = 1 caractère de plus en commun
					temp += s2[j+n];		// Ajouter à la chaîne mémoire
				}
				
				// Comparer avec les autres similitudes partant de i
				if (temp.length() > buff.length()) {
					buff = temp;			// Garder la plus grande
					max = n;
				}
				
				j += n;						// Sauter la sous-chaîne identique
				n = 0;						// Recommencer à zéro
			}// for s2
			
			// Ajouter la similitude au résultat
			if (max > 0) {
				simil += buff;
			}
			i += max;						// Sauter la sous-chaîne retenue
		}// for s1
		System.out.println(simil);
	}// main
}
