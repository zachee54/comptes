package haas.olivier.util;

import static org.junit.Assert.*;

import haas.olivier.util.Month;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MonthTest {

	/**
	 * Un parseur pour les besoins du tests.
	 */
	private static DateFormat parser;
	
	/**
	 * Objet testé, instancié à partir du constructeur
	 * {@link haas.olivier.util.Month#Month(Date)}.
	 */
	private Month monthDate;
	
	/**
	 * Objet testé, instancié à partir du constructeur
	 * {@link haas.olivier.util.Month#Month(long)}.
	 */
	private Month monthLong;
	
	/**
	 * Objet testé, instancié à partir du constructeur
	 * {@link haas.olivier.util.Month#Month(int, int)}.
	 */
	private Month monthMoisAnnee;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parser = new SimpleDateFormat("dd/MM/yy");
	}

	@Before
	public void setUp() throws Exception {
		Date date = parser.parse("31/03/13");
		monthDate = new Month(date);
		monthLong = new Month(date.getTime());
		monthMoisAnnee = new Month(2013, 3);
	}

	/**
	 * Teste que le constructeur sans argument correspond au constructeur
	 * prenant la date actuelle.
	 */
	@Test
	public void testMonth() {
		Month month1 = new Month();
		Month month2 = new Month(new Date());
		assertEquals(month1, month2);
	}

	@Test
	public void testGetYearFromDate() {
		assertEquals(2013, monthDate.getYear());
	}
	
	@Test
	public void testGetYearFromLong() {
		assertEquals(2013, monthLong.getYear());
	}
	
	@Test
	public void testGetYearFromMoisAnnee() {
		assertEquals(2013, monthMoisAnnee.getYear());
	}
	
	/**
	 * Vérifie l'année avec une instance située en janvier.
	 */
	@Test
	public void testGetYearInJanuaryFromDate() throws ParseException {
		Month monthJanuary = new Month(parser.parse("01/01/14"));
		assertEquals(2014, monthJanuary.getYear());
	}
	
	/**
	 * Vérifie l'année avec une instance située en janvier.
	 */
	@Test
	public void testGetYearInJanuaryFromLong() throws ParseException {
		Month monthJanuary = new Month(parser.parse("01/01/14").getTime());
		assertEquals(2014, monthJanuary.getYear());
	}
	
	/**
	 * Vérifie l'année avec une instance située en janvier.
	 */
	@Test
	public void testGetYearInJanuaryFromMoisAnnee() throws ParseException {
		Month monthJanuary = new Month(2014, 1);
		assertEquals(2014, monthJanuary.getYear());
	}
	
	/**
	 * Vérifie l'année avec une instance située en décembre.
	 */
	@Test
	public void testGetYearInDecemberFromDate() throws ParseException {
		Month monthDecember = new Month(parser.parse("31/12/12"));
		assertEquals(2012, monthDecember.getYear());
	}
	
	/**
	 * Vérifie l'année avec une instance située en décembre.
	 */
	@Test
	public void testGetYearInDecemberFromLong() throws ParseException {
		Month monthDecember = new Month(parser.parse("31/12/12").getTime());
		assertEquals(2012, monthDecember.getYear());
	}
	
	/**
	 * Vérifie l'année avec une instance située en décembre.
	 */
	@Test
	public void testGetYearInDecemberFromMoisAnnee() throws ParseException {
		Month monthDecember = new Month(2012, 12);
		assertEquals(2012, monthDecember.getYear());
	}
	
	@Test
	public void testGetNumInYearFromDate() {
		assertEquals(3, monthDate.getNumInYear());
	}
	
	@Test
	public void testGetNumInYearFromLong() {
		assertEquals(3, monthLong.getNumInYear());
	}
	
	@Test
	public void testGetNumInYearFromMoisAnnee() {
		assertEquals(3, monthMoisAnnee.getNumInYear());
	}
	
	@Test
	public void testGetTranslatedinPastFromDate() throws ParseException {
		Month monthPast = new Month(parser.parse("30/11/2012"));
		assertEquals(monthDate.getTranslated(-4), monthPast);
	}
	
	@Test
	public void testGetTranslatedinPastFromLong() throws ParseException {
		Month monthPast = new Month(parser.parse("30/11/2012"));
		assertEquals(monthLong.getTranslated(-4), monthPast);
	}
	
	@Test
	public void testGetTranslatedinPastFromMoisAnnee() throws ParseException {
		Month monthPast = new Month(parser.parse("30/11/2012"));
		assertEquals(monthMoisAnnee.getTranslated(-4), monthPast);
	}

	@Test
	public void testGetTranslatedinFutureFromDate() throws ParseException {
		Month monthFuture = new Month(parser.parse("30/04/2015"));
		assertEquals(monthDate.getTranslated(25), monthFuture);
	}
	
	@Test
	public void testGetTranslatedinFutureFromLong() throws ParseException {
		Month monthFuture = new Month(parser.parse("30/04/2015"));
		assertEquals(monthLong.getTranslated(25), monthFuture);
	}
	
	@Test
	public void testGetTranslatedinFutureFromMoisAnnee() throws ParseException {
		Month monthFuture = new Month(parser.parse("30/04/2015"));
		assertEquals(monthMoisAnnee.getTranslated(25), monthFuture);
	}

	@Test
	public void testGetNextFromDate() throws ParseException {
		Month monthNext = new Month(parser.parse("15/04/13"));
		assertEquals(monthDate.getNext(), monthNext);
	}
	
	@Test
	public void testGetNextFromLong() throws ParseException {
		Month monthNext = new Month(parser.parse("15/04/13"));
		assertEquals(monthLong.getNext(), monthNext);
	}
	
	@Test
	public void testGetNextFromMoisAnnee() throws ParseException {
		Month monthNext = new Month(parser.parse("15/04/13"));
		assertEquals(monthMoisAnnee.getNext(), monthNext);
	}

	@Test
	public void testGetPreviousFromDate() throws ParseException {
		Month monthPrevious = new Month(parser.parse("03/02/13"));
		assertEquals(monthDate.getPrevious(), monthPrevious);
	}
	
	@Test
	public void testGetPreviousFromLong() throws ParseException {
		Month monthPrevious = new Month(parser.parse("03/02/13"));
		assertEquals(monthLong.getPrevious(), monthPrevious);
	}
	
	@Test
	public void testGetPreviousFromMoisAnnee() throws ParseException {
		Month monthPrevious = new Month(parser.parse("03/02/13"));
		assertEquals(monthMoisAnnee.getPrevious(), monthPrevious);
	}

	@Test
	public void testIncludesDateFromDate() throws ParseException {
		assertTrue(monthDate.includes(parser.parse("14/03/13")));
		assertFalse(monthDate.includes(parser.parse("28/02/13")));
		assertFalse(monthDate.includes(parser.parse("15/04/13")));
		assertFalse(monthDate.includes(parser.parse("31/03/14")));
	}
	
	@Test
	public void testIncludesDateFromLong() throws ParseException {
		assertTrue(monthLong.includes(parser.parse("14/03/13")));
		assertFalse(monthLong.includes(parser.parse("28/02/13")));
		assertFalse(monthLong.includes(parser.parse("15/04/13")));
		assertFalse(monthLong.includes(parser.parse("31/03/14")));
	}
	
	@Test
	public void testIncludesDateFromMoisAnnee() throws ParseException {
		assertTrue(monthMoisAnnee.includes(parser.parse("14/03/13")));
		assertFalse(monthMoisAnnee.includes(parser.parse("28/02/13")));
		assertFalse(monthMoisAnnee.includes(parser.parse("15/04/13")));
		assertFalse(monthMoisAnnee.includes(parser.parse("31/03/14")));
	}

	@Test
	public void testAfterDateFromDate() throws ParseException {
		assertTrue(monthDate.after(parser.parse("15/02/13")));
	}
	
	@Test
	public void testAfterDateFromLong() throws ParseException {
		assertTrue(monthLong.after(parser.parse("15/02/13")));
	}
	
	@Test
	public void testAfterDateFromMoisAnnee() throws ParseException {
		assertTrue(monthMoisAnnee.after(parser.parse("15/02/13")));
	}

	@Test
	public void testNotAfterSameDateFromDate() throws ParseException {
		assertFalse(monthDate.after(parser.parse("04/03/13")));
	}
	
	@Test
	public void testNotAfterSameDateFromLong() throws ParseException {
		assertFalse(monthLong.after(parser.parse("04/03/13")));
	}
	
	@Test
	public void testNotAfterSameDateFromMoisAnnee() throws ParseException {
		assertFalse(monthMoisAnnee.after(parser.parse("04/03/13")));
	}

	@Test
	public void testNotAfterDateFromDate() throws ParseException {
		assertFalse(monthDate.after(parser.parse("18/07/13")));
	}
	
	@Test
	public void testNotAfterDateFromLong() throws ParseException {
		assertFalse(monthLong.after(parser.parse("18/07/13")));
	}
	
	@Test
	public void testNotAfterDateFromMoisAnnee() throws ParseException {
		assertFalse(monthMoisAnnee.after(parser.parse("18/07/13")));
	}

	@Test
	public void testBeforeDateFromDate() throws ParseException {
		assertTrue(monthDate.before(parser.parse("03/04/13")));
	}
	
	@Test
	public void testBeforeDateFromLong() throws ParseException {
		assertTrue(monthLong.before(parser.parse("03/04/13")));
	}
	
	@Test
	public void testBeforeDateFromMoisAnnee() throws ParseException {
		assertTrue(monthMoisAnnee.before(parser.parse("03/04/13")));
	}

	@Test
	public void testNotBeforeSameDateFromDate() throws ParseException {
		assertFalse(monthDate.before(parser.parse("01/03/13")));
	}
	
	@Test
	public void testNotBeforeSameDateFromLong() throws ParseException {
		assertFalse(monthLong.before(parser.parse("01/03/13")));
	}
	
	@Test
	public void testNotBeforeSameDateFromMoisAnnee() throws ParseException {
		assertFalse(monthMoisAnnee.before(parser.parse("01/03/13")));
	}

	@Test
	public void testNotBeforeDateFromDate() throws ParseException {
		assertFalse(monthDate.before(parser.parse("04/02/13")));
	}
	
	@Test
	public void testNotBeforeDateFromLong() throws ParseException {
		assertFalse(monthLong.before(parser.parse("04/02/13")));
	}
	
	@Test
	public void testNotBeforeDateFromMoisAnnee() throws ParseException {
		assertFalse(monthMoisAnnee.before(parser.parse("04/02/13")));
	}

	@Test
	public void testToStringFromDate() {
		assertEquals("mars 2013", monthDate.toString());
	}
	
	@Test
	public void testToStringFromLong() {
		assertEquals("mars 2013", monthLong.toString());
	}
	
	@Test
	public void testToStringFromMoisAnnee() {
		assertEquals("mars 2013", monthMoisAnnee.toString());
	}
}
