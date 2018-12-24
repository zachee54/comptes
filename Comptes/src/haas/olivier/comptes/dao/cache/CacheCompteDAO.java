/*
 * Copyright 2013-2018 Olivier HAAS. All rights reserved.
 */
package haas.olivier.comptes.dao.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import haas.olivier.comptes.Compte;
import haas.olivier.comptes.TypeCompte;
import haas.olivier.comptes.dao.CompteDAO;

/**
 * Un cache des comptes.
 * 
 * @author Olivier HAAS
 */
class CacheCompteDAO implements CompteDAO {
	
	/**
	 * Drapeau indiquant si les données ont été modifiées depuis la dernière
	 * sauvegarde.
	 */
	private boolean mustBeSaved;
	
	/**
	 * Les instances existantes, et leurs identifiants.
	 */
	private final HashSet<Compte> instances = new HashSet<>();
	
	/**
	 * Construit un objet d'accès aux comptes.
	 */
	public CacheCompteDAO(Iterator<Compte> it) {
		while (it.hasNext())
			add(it.next());
		
		// Remettre à false car rien n'a changé pour l'instant
		mustBeSaved = false;
	}

	@Override
	public Collection<Compte> getAll() throws IOException {
		List<Compte> list = new ArrayList<>(instances);
		Collections.sort(list);
		return list;
	}
	
	@Override
	public Compte createAndAdd(TypeCompte type) {
		Compte compte = new Compte(findFirstUnusedId(), type);
		add(compte);
		return compte;
	}
	
	/**
	 * Renvoie le premier identifiant inutilisé.
	 * 
	 * @return	Le premier identifiant inutilisé.
	 */
	private int findFirstUnusedId() {
		Set<Integer> ids = instances.stream()
				.map(Compte::getId)
				.collect(Collectors.toSet());
		return IntStream.range(0, Integer.MAX_VALUE)
				.filter(i -> !ids.contains(i))
				.findFirst().getAsInt();
	}

	@Override
	public void add(Compte compte) {
		instances.add(compte);
		mustBeSaved = true;
	}
	
	@Override
	public void remove(Compte compte) throws IOException {
		instances.remove(compte);
		mustBeSaved = true;
	}
	
	@Override
	public void erase() {
		instances.clear();
		mustBeSaved = true;
	}
	
	/**
	 * Indique si des modifications ont eu lieu depuis la dernière sauvegarde.
	 */
	public boolean mustBeSaved() {
		return mustBeSaved;
	}
	
	/**
	 * Indique que les données viennent d'être sauvegardées.
	 */
	public void setSaved() {
		mustBeSaved = false;
	}
}