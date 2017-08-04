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
package de.hska.kunde.config.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Entity-Klasse, um Benutzerkennungen bestehend aus Benutzernamen,
 * Passw&ouml;rter und Rollen zu repr&auml;sentieren, die in MongoDB verwaltet
 * werden.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Document
@SuppressWarnings("squid:S2160")
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class Account extends User {
    private static final long serialVersionUID = 1L;

    private final ObjectId id;

    /**
     * Konstruktor f&uuml;r ein Account-Objekt
     * @param id _id in MongoDB
     * @param username Benutzername
     * @param password Passwort
     * @param authorities Rollen
     */
    public Account(@JsonProperty("id")
                   @SuppressWarnings("SameParameterValue")
                   ObjectId id,
                   @JsonProperty("username") String username,
                   @JsonProperty("password") String password,
                   @JsonProperty("authorities")
                   Collection<SimpleGrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

    public ObjectId getId() {
        return id;
    }
    
    /**
     * Konvertierungsklasse f&uuml;r MongoDB, um einen String einzulesen und
     * eine Rolle als GrantedAuthority zu erzeugen. Wegen @ReadingConverter
     * ist kein Lambda-Ausdruck m&ouml;glich.
     */
    @ReadingConverter
    public static class RoleReadConverter
                        implements Converter<String, GrantedAuthority> {
        @Override
        public SimpleGrantedAuthority convert(String role) {
            return new SimpleGrantedAuthority(role);
        }
    }
    
    /**
     * Konvertierungsklasse f&uuml;r MongoDB, um eine Rolle (GrantedAuthority)
     * in einen String zu konvertieren. Wegen @WritingConverter ist kein
     * Lambda-Ausdruck m&ouml;glich.
     */
    @WritingConverter
    public static class RoleWriteConverter
                        implements Converter<GrantedAuthority, String> {
        @Override
        public String convert(GrantedAuthority grantedAuthority) {
            return grantedAuthority == null
                   ? null
                   : grantedAuthority.getAuthority();
        }
    }
}
