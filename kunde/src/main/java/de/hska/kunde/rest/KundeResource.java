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
package de.hska.kunde.rest;

import de.hska.kunde.entity.Adresse;
import de.hska.kunde.entity.FamilienstandType;
import de.hska.kunde.entity.GeschlechtType;
import de.hska.kunde.entity.InteresseType;
import de.hska.kunde.entity.Kunde;
import de.hska.kunde.entity.Umsatz;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URL;
import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.ResourceSupport;

/**
 * Resource-Objekt f&uuml;r ein Kunden-Objekt gem&auml;&szlig; Spring HATEOS.
 * <img src="../../../../../images/KundeResource.png" alt="Klassendiagramm">
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Getter
@Setter
@SuppressWarnings("DefaultAnnotationParam")
@EqualsAndHashCode(of = "email", callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
class KundeResource extends ResourceSupport {
    // id wird durch den Link "self" bereitgestellt
    // private ObjectId id

    private String nachname;
    private String email;
    private boolean newsletter;
    private LocalDate geburtsdatum;
    private Umsatz umsatz;
    private URL homepage;
    private GeschlechtType geschlecht;
    private FamilienstandType familienstand;
    private Set<InteresseType> interessen;
    private Adresse adresse;
    private String username;
    
    static KundeResource of(Kunde kunde) {
        return KundeResource.builder()
                            .nachname(kunde.getNachname())
                            .email(kunde.getEmail())
                            .newsletter(kunde.isNewsletter())
                            .geburtsdatum(kunde.getGeburtsdatum())
                            .umsatz(kunde.getUmsatz())
                            .homepage(kunde.getHomepage())
                            .geschlecht(kunde.getGeschlecht())
                            .familienstand(kunde.getFamilienstand())
                            .interessen(kunde.getInteressen())
                            .adresse(kunde.getAdresse())
                            .username(kunde.getUsername())
                            .build();
    }
}
