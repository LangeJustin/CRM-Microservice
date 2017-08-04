/*
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.hska.kunde.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

/**
 * Demozweck: Radiobuttons auf Clientseite
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
public enum GeschlechtType {
    MAENNLICH("M"),
    WEIBLICH("W");
    
    private static final Locale LOCALE_DEFAULT = Locale.getDefault();
    
    // FIXME Java 9: Map.of
    private static final Map<String, GeschlechtType> NAME_CACHE =
        new ConcurrentHashMap<>();
    static {
        Stream.of(GeschlechtType.values())
              .forEach(geschlecht -> {
            NAME_CACHE.put(geschlecht.value, geschlecht);
            NAME_CACHE.put(geschlecht.value.toLowerCase(LOCALE_DEFAULT),
                           geschlecht);
            NAME_CACHE.put(geschlecht.name(), geschlecht);
            NAME_CACHE.put(geschlecht.name().toLowerCase(LOCALE_DEFAULT),
                           geschlecht);
        });
    }
    
    private final String value;
    
    GeschlechtType(String value) {
        this.value = value;
    }
    
    // https://github.com/FasterXML/jackson-databind/wiki
    @JsonValue
    @SuppressWarnings("WeakerAccess")
    public String getValue() {
        return value;
    }
    
    /**
     * Konvertierung eines Strings in einen Enum-Wert
     * @param value Der String, zu dem ein passender Enum-Wert ermittelt werden
     *              soll. Keine Unterscheidung zwischen Gro&szlig;-
     *              und Kleinschreibung.
     * @return Passender Enum-Wert oder null
     */
    @SuppressWarnings("WeakerAccess")
    public static GeschlechtType build(String value) {
        return NAME_CACHE.get(value);
    }
    
    /**
     * Konvertierungsklasse f&uuml;r MongoDB, um einen String einzulesen und
     * ein Enum-Objekt von GeschlechtType zu erzeugen. Wegen @ReadingConverter
     * ist kein Lambda-Ausdruck m&ouml;glich.
     */
    @ReadingConverter
    public static class ReadConverter
                        implements Converter<String, GeschlechtType> {
        @Override
        public GeschlechtType convert(String value) {
            return GeschlechtType.build(value);
        }
    }
   
    /**
     * Konvertierungsklasse f&uuml;r MongoDB, um GeschlechtType in einen String
     * zu konvertieren. Wegen @WritingConverter ist kein Lambda-Ausdruck
     * m&ouml;glich.
     */
    @WritingConverter
    public static class WriteConverter
                        implements Converter<GeschlechtType, String> {
        @Override
        public String convert(GeschlechtType geschlechtType) {
            return geschlechtType == null ? null : geschlechtType.getValue();
        }
    }
}
