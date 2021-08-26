package com.edheijer.weatherclient.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.Objects;

public class DateTimeUtils {

    private DateTimeUtils() {}

    /**
     * Convert given javax.time component to xml calendar
     * @param localDateTime to convert
     * @return xml gregorian calendar representation of given local date time
     */
    public static XMLGregorianCalendar asXmlGreGorianCalendar(final LocalDateTime localDateTime) {
        try {
            return Objects.nonNull(localDateTime) ?
                        DatatypeFactory.newInstance().newXMLGregorianCalendar(
                                GregorianCalendar.from(localDateTime.atZone(ZoneId.systemDefault()))) :
                        null;
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Failed to instantiate datatype factory.");
        }
    }

    /**
     * Convert given xml gregorian calendar to local date time
     * @param xmlGregorianCalendar to convert
     * @return local date time representation of given xml calendar
     */
    public static LocalDateTime asLocalDateTime(final XMLGregorianCalendar xmlGregorianCalendar) {
        return Objects.nonNull(xmlGregorianCalendar) ?
                    xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDateTime() :
                    null;
    }
}