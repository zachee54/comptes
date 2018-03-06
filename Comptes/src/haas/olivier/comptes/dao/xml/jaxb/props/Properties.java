//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2017.12.03 à 06:55:02 PM CET 
//


package haas.olivier.comptes.dao.xml.jaxb.props;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour anonymous complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="diagrams">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="diagram" type="{urn:haas.olivier.comptes.properties}diagram" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "diagrams"
})
@XmlRootElement(name = "properties")
public class Properties {

    @XmlElement(required = true)
    protected Properties.Diagrams diagrams;

    /**
     * Obtient la valeur de la propriété diagrams.
     * 
     * @return
     *     possible object is
     *     {@link Properties.Diagrams }
     *     
     */
    public Properties.Diagrams getDiagrams() {
        return diagrams;
    }

    /**
     * Définit la valeur de la propriété diagrams.
     * 
     * @param value
     *     allowed object is
     *     {@link Properties.Diagrams }
     *     
     */
    public void setDiagrams(Properties.Diagrams value) {
        this.diagrams = value;
    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="diagram" type="{urn:haas.olivier.comptes.properties}diagram" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "diagram"
    })
    public static class Diagrams {

        protected List<Diagram> diagram;

        /**
         * Gets the value of the diagram property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the diagram property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDiagram().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Diagram }
         * 
         * 
         */
        public List<Diagram> getDiagram() {
            if (diagram == null) {
                diagram = new ArrayList<Diagram>();
            }
            return this.diagram;
        }

    }

}
