//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.20 at 03:33:43 PM PDT 
//


package parsers.PMC;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{}day"/>
 *         &lt;element ref="{}month"/>
 *         &lt;element ref="{}year"/>
 *         &lt;element ref="{}season"/>
 *         &lt;element ref="{}string-date"/>
 *         &lt;element ref="{}x"/>
 *       &lt;/choice>
 *       &lt;attribute name="pub-type" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="publication-format" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="date-type" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="iso-8601-date" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="calendar" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dayOrMonthOrYear"
})
@XmlRootElement(name = "pub-date")
public class PubDate {

    @XmlElements({
        @XmlElement(name = "day", type = Day.class),
        @XmlElement(name = "month", type = Month.class),
        @XmlElement(name = "year", type = Year.class),
        @XmlElement(name = "season", type = Season.class),
        @XmlElement(name = "string-date", type = StringDate.class),
        @XmlElement(name = "x", type = X.class)
    })
    protected List<Object> dayOrMonthOrYear;
    @XmlAttribute(name = "pub-type")
    @XmlSchemaType(name = "anySimpleType")
    protected String pubType;
    @XmlAttribute(name = "publication-format")
    @XmlSchemaType(name = "anySimpleType")
    protected String publicationFormat;
    @XmlAttribute(name = "date-type")
    @XmlSchemaType(name = "anySimpleType")
    protected String dateType;
    @XmlAttribute(name = "iso-8601-date")
    @XmlSchemaType(name = "anySimpleType")
    protected String iso8601Date;
    @XmlAttribute(name = "calendar")
    @XmlSchemaType(name = "anySimpleType")
    protected String calendar;
    @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String lang;

    public java.util.Date getDate() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String dayStr = "00",
                monthStr = "00",
                yearStr = "2000";
        for (Object o: dayOrMonthOrYear){
            if (o instanceof Day){
                dayStr = ((Day) o).getContent();
            }
            if (o instanceof Month){
                monthStr = ((Month) o).getContent();
                monthStr = monthStr.replaceAll("\n", "");
                monthStr = monthStr.replaceAll("\t", "");
            }
            if (o instanceof Year){
                yearStr = ((Year) o).getContent();
                yearStr = yearStr.replaceAll("\n", "");
                yearStr = yearStr.replaceAll("\t", "");
            }
        }
        String dateInString = String.format("%s-%s-%s", dayStr, monthStr, yearStr);
        return formatter.parse(dateInString);
    }

    /**
     * Gets the value of the dayOrMonthOrYear property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dayOrMonthOrYear property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDayOrMonthOrYear().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Day }
     * {@link Month }
     * {@link Year }
     * {@link Season }
     * {@link StringDate }
     * {@link X }
     * 
     * 
     */
    public List<Object> getDayOrMonthOrYear() {
        if (dayOrMonthOrYear == null) {
            dayOrMonthOrYear = new ArrayList<Object>();
        }
        return this.dayOrMonthOrYear;
    }

    /**
     * Gets the value of the pubType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPubType() {
        return pubType;
    }

    /**
     * Sets the value of the pubType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPubType(String value) {
        this.pubType = value;
    }

    /**
     * Gets the value of the publicationFormat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublicationFormat() {
        return publicationFormat;
    }

    /**
     * Sets the value of the publicationFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublicationFormat(String value) {
        this.publicationFormat = value;
    }

    /**
     * Gets the value of the dateType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateType() {
        return dateType;
    }

    /**
     * Sets the value of the dateType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateType(String value) {
        this.dateType = value;
    }

    /**
     * Gets the value of the iso8601Date property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIso8601Date() {
        return iso8601Date;
    }

    /**
     * Sets the value of the iso8601Date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIso8601Date(String value) {
        this.iso8601Date = value;
    }

    /**
     * Gets the value of the calendar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCalendar() {
        return calendar;
    }

    /**
     * Sets the value of the calendar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCalendar(String value) {
        this.calendar = value;
    }

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLang(String value) {
        this.lang = value;
    }

}
