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

package de.hska.bestellung.rest;

import de.hska.bestellung.entity.Bestellposition;
import de.hska.bestellung.entity.Bestellung;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.ResourceSupport;

/**
 * Resource-Objekt f&uuml;r ein Bestellung-Objekt gem&auml;&szlig; Spring HATEOS
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
@SuppressWarnings("DefaultAnnotationParam")
class BestellungResource extends ResourceSupport {
    // id wird durch den Link "self" bereitgestellt

    private LocalDate datum;
    private String kundeId;
    private String kundeNachname;
    private List<Bestellposition> bestellpositionen;
    
    static BestellungResource of(Bestellung bestellung) {
        return BestellungResource.builder()
             .datum(bestellung.getDatum())
             .kundeId(bestellung.getKundeId())
             .kundeNachname(bestellung.getKundeNachname())
             .bestellpositionen(bestellung.getBestellpositionen())
             .build();
    }
}
