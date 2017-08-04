/*
 * Copyright (C) 2013 - 2017 Juergen Zimmermann, Hochschule Karlsruhe
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

import java.net.URL;
import java.time.LocalDate;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Email;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import static lombok.AccessLevel.NONE;

/**
 * Daten eines Kunden f&uuml;r die Anwendungslogik und zum Abspeichern.
 * In DDD: Kunde ist ein "Aggregate Root".
 * <img src="../../../../../images/Kunde.png" alt="Klassendiagramm">
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Document
@Getter
@Setter
@EqualsAndHashCode(of = "email", callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kunde extends Auditable implements Identifiable<ObjectId> {
    public static final String EMAIL_MSG = "{kunde.email.pattern}";

    @Id
    @Setter(NONE)
    private ObjectId id;

    @NotNull(message = "{kunde.nachname.notNull}")
    @Pattern(regexp = "[A-Z][a-z]+", message = "{kunde.nachname.pattern}")
    @Indexed
    private String nachname;
    
    @NotNull(message = "{kunde.email.notNull}")
    @Email(message = EMAIL_MSG)
    @Indexed(unique = true)
    private String email;

    private boolean newsletter;
    private LocalDate geburtsdatum;

    @Indexed(sparse = true)
    private Umsatz umsatz;
    
    private URL homepage;
    private GeschlechtType geschlecht;
    private FamilienstandType familienstand;

    @Singular("interesse")
    private Set<InteresseType> interessen;

    @Valid
    @NotNull(message = "{kunde.adresse.notNull}")
    private Adresse adresse;
    
    @Indexed(unique = true)
    private String username;
    
    @Transient
    private Account account;
}
