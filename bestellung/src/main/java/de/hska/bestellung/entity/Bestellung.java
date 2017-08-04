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
package de.hska.bestellung.entity;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static lombok.AccessLevel.NONE;

/**
 * Daten einer Bestellung f&uuml;r die Anwendungslogik und zum Abspeichern.
 * In DDD: Bestellung ist ein "Aggregate Root".
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Document
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor                                                    // Jackson
@AllArgsConstructor                                                   // Builder
@Builder
@SuppressWarnings("DefaultAnnotationParam")
public class Bestellung extends Auditable implements Identifiable<ObjectId> {
    @Id
    @Setter(NONE)
    private ObjectId id;

    private LocalDate datum;
    
    /**
     * Der Microservice f&uuml;r Bestellungen kennt nicht das Datenformat der
     * IDs aus dem Microservice f&uuml;r Kunden
     */
    @NotNull(message = "{bestellung.kundeId.notNull}")
    private String kundeId;
    
    @Transient
    private String kundeNachname;

    @NotEmpty(message = "{bestellung.bestellpositionen.notEmpty}")
    @Valid
    private List<Bestellposition> bestellpositionen;
}
