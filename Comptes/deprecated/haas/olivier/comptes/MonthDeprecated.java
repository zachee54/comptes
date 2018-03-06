package haas.olivier.comptes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Implémente un mois calendaire. Chaque instance de Month est égale aux
 * instances de Month correspondant au même mois et à la même année, et aux
 * instances Date correspondant à une date dans ce mois et cette année. Casté en
 * objet Date, il correspond au 1er jour du mois, à minuit.
 * 
 * @author olivier
 */
public class MonthDeprecated extends Date {

	private static final long serialVersionUID = -1783417908838511139L;

	// Format d'affichage
	private static final DateFormat df = new SimpleDateFormat("MMMM yyyy");

	// Constructeurs

	/** Crée une instance correspondant au mois de la date système. */
	public Month() {
		this(new Date().getTime());
	}

	/**
	 * Crée une instance correspondant au mois et à l'année de la date indiquée.
	 * 
	 * @param date
	 *            La date dont le mois doit être renvoyé
	 */
	public Month(Date date) {
		this(date.getTime());
	}

	/**
	 * Crée une instance correspondant au mois et à l'année indiqués.
	 * 
	 * @param date
	 *            Date au format long dont le mois doit être renvoyé
	 */
	public Month(long date) {

		// Construire comme un objet Date
		super(date);

		// Créer un calendrier à cette date/heure
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);

		// Régler le jour du mois à 1 et le temps à 0h0m0s0
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		// Régler cet objet à la nouvelle date/heure
		this.setTime(cal.getTimeInMillis());
	}

	// Méthodes

	/**
	 * Renvoie un mois décalé de n mois par rapport à celui-ci (en avant ou en
	 * arrière)
	 * 
	 * @param n
	 *            Entier positif (mois suivants) ou négatif (mois précédents)
	 * @return Un nouvel objet Month
	 */
	public Month getTranslated(int n) {

		// Un calendrier à ce mois
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);

		// Translater de n mois
		cal.add(Calendar.MONTH, n);
		return new Month(cal.getTimeInMillis());
	}

	/**
	 * Renvoie le mois calendaire suivant
	 * 
	 * @return nouvelle instance Month
	 */
	public Month getNext() {
		return getTranslated(1);
	}

	/**
	 * Renvoie le mois calendaire précédent
	 * 
	 * @return nouvelle instance Month
	 */
	public Month getPrevious() {
		return getTranslated(-1);
	}

	/**
	 * Détermine si une date correspond à ce mois. Un objet Date (incluant, par
	 * héritage, le cas d'un objet Month) est inclus dans ce mois lorsqu'il
	 * correspond au même mois et à la même année. Cela permet d'identifier
	 * l'appartenance d'une date à ce mois ou l'égalité de deux mois.
	 * 
	 * @args date La date
	 * @returns true si le paramètre est une date incluse dans le mois, ou une
	 *          instance équivalente de Month, false sinon ou si date est null.
	 */
	public boolean includes(Date date) {
		if (date == null) {
			return false;
		}

		Calendar cal = Calendar.getInstance(); // Un calendrier

		// Obtenir le mois et l'année de cet objet
		cal.setTime(this);
		int MoisThis = cal.get(Calendar.MONTH);
		int AnneeThis = cal.get(Calendar.YEAR);

		// Obtenir le mois et l'année de obj
		cal.setTime(date);
		int MoisObj = cal.get(Calendar.MONTH);
		int AnneeObj = cal.get(Calendar.YEAR);

		return (MoisThis == MoisObj) && (AnneeThis == AnneeObj);
	}

	@Override
	/** Compare ce mois avec une date
	 * Une date est incluse dans ce Month si elle correspond au même mois
	 * calendaire. Sinon, elle lui est strictement inférieure ou strictement
	 * supérieure selon qu'elle se trouve avant ou après.
	 * Si l'argument est un objet Month, le raisonnement est le même.
	 * 
	 * @args	date	La date ou le mois à comparer
	 * @returns -1 si date est antérieure, 1 si elle est supérieure, 0 si elle
	 * est dans ce mois
	 */
	public int compareTo(Date date) {
		if (this.includes(date)) {
			return 0;
		} else {
			return super.compareTo(date);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Month) {
			return this.compareTo((Month) obj) == 0;
		}
		return false;
	}// equals
	
	@Override
	public int hashCode() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		
		int res = 31;
		int mul = 19;
		
		res = mul*res + cal.get(Calendar.MONTH);
		res = mul*res + cal.get(Calendar.YEAR);
		return mul*res;
	}// hashCode

	@Override
	/** Détermine si ce mois est après la date donnée.
	 * @return	true s'il est après la date,
	 * 			false s'il est avant ou si elle est pendant.
	 */
	public boolean after(Date date) {
		return !includes(date) && super.after(date);
	}

	@Override
	/** Détermine si ce mois est avant la date donnée.
	 * @return	true s'il est avant la date
	 * 			false s'il est après ou si elle est pendant. 
	 */
	public boolean before(Date date) {
		return !includes(date) && super.before(date);
	}

	@Override
	/** Écrit le mois en toutes lettres */
	public String toString() {
		return df.format(this);
	}
}
