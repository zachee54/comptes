package haas.olivier.comptes;

import haas.olivier.comptes.dao.CompteDAO;

import java.io.IOException;

/** Une classe déclarant <code>public</code> les méthodes abstraites de
 * {@link haas.olivier.comptes.dao.CompteDAO}, et permettant ainsi de mocker la
 * classe abstraite.<br>
 * Autrement, ce n'était pas possible car les méthodes publiques de
 * <code>CompteDAO</code> sont déclarées <code>final</code>.
 *
 * @author Olivier HAAS
 */
public class MockeableCompteDAO extends CompteDAO {
	
	private final Iterable<Compte> comptes;
	
	public MockeableCompteDAO(Iterable<Compte> comptes) {
		this.comptes = comptes;
	}// constructeur

	@Override
	public Iterable<Compte> getAllImpl() throws IOException {return comptes;}

	@Override
	public Compte getImpl(int id) throws IOException {return null;}

	@Override
	public Compte addImpl(Compte c) throws IOException {return null;}

	@Override
	public void updateImpl(Compte c) throws IOException {}

	@Override
	public void removeImpl(int id) throws IOException {}
}// public inner class MockeableCompteDAO
