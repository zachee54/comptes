//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.04.15 at 03:00:21 PM CEST 
//


package haas.olivier.comptes.dao.xml.jaxb.perm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for permanent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="permanent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="libelle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tiers" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="debit" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="credit" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="pointage" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="jours" type="{urn:haas.olivier.comptes.permanents}jours"/>
 *         &lt;element name="montants" type="{urn:haas.olivier.comptes.permanents}montants" minOccurs="0"/>
 *         &lt;element name="dependance" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                 &lt;attribute name="taux" type="{http://www.w3.org/2001/XMLSchema}double" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="nom" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "permanent", propOrder = {
    "libelle",
    "tiers",
    "debit",
    "credit",
    "pointage",
    "jours",
    "montants",
    "dependance"
})
public class Permanent {

    @XmlElement(required = true)
    protected String libelle;
    @XmlElement(required = true)
    protected String tiers;
    protected int debit;
    protected int credit;
    protected boolean pointage;
    @XmlElement(required = true)
    protected Jours jours;
    protected Montants montants;
    protected Permanent.Dependance dependance;
    @XmlAttribute
    protected Integer id;
    @XmlAttribute
    protected String nom;

    /**
     * Gets the value of the libelle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLibelle() {
        return libelle;
    }

    /**
     * Sets the value of the libelle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLibelle(String value) {
        this.libelle = value;
    }

    /**
     * Gets the value of the tiers property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTiers() {
        return tiers;
    }

    /**
     * Sets the value of the tiers property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTiers(String value) {
        this.tiers = value;
    }

    /**
     * Gets the value of the debit property.
     * 
     */
    public int getDebit() {
        return debit;
    }

    /**
     * Sets the value of the debit property.
     * 
     */
    public void setDebit(int value) {
        this.debit = value;
    }

    /**
     * Gets the value of the credit property.
     * 
     */
    public int getCredit() {
        return credit;
    }

    /**
     * Sets the value of the credit property.
     * 
     */
    public void setCredit(int value) {
        this.credit = value;
    }

    /**
     * Gets the value of the pointage property.
     * 
     */
    public boolean isPointage() {
        return pointage;
    }

    /**
     * Sets the value of the pointage property.
     * 
     */
    public void setPointage(boolean value) {
        this.pointage = value;
    }

    /**
     * Gets the value of the jours property.
     * 
     * @return
     *     possible object is
     *     {@link Jours }
     *     
     */
    public Jours getJours() {
        return jours;
    }

    /**
     * Sets the value of the jours property.
     * 
     * @param value
     *     allowed object is
     *     {@link Jours }
     *     
     */
    public void setJours(Jours value) {
        this.jours = value;
    }

    /**
     * Gets the value of the montants property.
     * 
     * @return
     *     possible object is
     *     {@link Montants }
     *     
     */
    public Montants getMontants() {
        return montants;
    }

    /**
     * Sets the value of the montants property.
     * 
     * @param value
     *     allowed object is
     *     {@link Montants }
     *     
     */
    public void setMontants(Montants value) {
        this.montants = value;
    }

    /**
     * Gets the value of the dependance property.
     * 
     * @return
     *     possible object is
     *     {@link Permanent.Dependance }
     *     
     */
    public Permanent.Dependance getDependance() {
        return dependance;
    }

    /**
     * Sets the value of the dependance property.
     * 
     * @param value
     *     allowed object is
     *     {@link Permanent.Dependance }
     *     
     */
    public void setDependance(Permanent.Dependance value) {
        this.dependance = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setId(Integer value) {
        this.id = value;
    }

    /**
     * Gets the value of the nom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNom() {
        return nom;
    }

    /**
     * Sets the value of the nom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNom(String value) {
        this.nom = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}int" />
     *       &lt;attribute name="taux" type="{http://www.w3.org/2001/XMLSchema}double" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Dependance {

        @XmlAttribute
        protected Integer id;
        @XmlAttribute
        protected Double taux;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link Integer }
         *     
         */
        public Integer getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link Integer }
         *     
         */
        public void setId(Integer value) {
            this.id = value;
        }

        /**
         * Gets the value of the taux property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getTaux() {
            return taux;
        }

        /**
         * Sets the value of the taux property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setTaux(Double value) {
            this.taux = value;
        }

    }

}