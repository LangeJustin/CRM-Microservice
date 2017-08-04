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
package de.hska.kunde.service;

import com.querydsl.core.types.dsl.StringExpression;
import de.hska.kunde.entity.Kunde;
import de.hska.kunde.entity.QKunde;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Async;

// Queries werden durch Namenskonventionen deklariert wie bei Ruby-on-Rails.
// Von CrudRepository sind u.a. folgende Interfaces abgeleitet:
// * MongoRepository
// * JpaRepository

@SuppressWarnings({
    "checkstyle:MethodName",
    "PMD.MethodNamingConventions",
    "unused",
    "squid:S00100"
})
public interface KundeRepository extends CrudRepository<Kunde, ObjectId>,
                                  QueryDslPredicateExecutor<Kunde>,
                                  QuerydslBinderCustomizer<QKunde> {
    @FunctionalInterface
    interface NachnameProjection {
        String getNachname();
    }

    @FunctionalInterface
    interface EmailProjection {
        String getEmail();
    }

    /**
     * Suche nach einem Kunden mit der gegebenen ID.
     * @param id Die ID des gesuchten Kunden
     * @return Der gefundene Kunde oder empty
     */
    // SELECT * FROM kunde WHERE id = ...
    Optional<Kunde> findById(ObjectId id);

    /**
     * Suche nach einem Kunden mit der gegebenen Emailadresse.
     * @param email Die Emailadresse des gesuchten Kunden
     * @return Der gefundene Kunde oder empty
     */
    // SELECT * FROM kunde WHERE email = ...
    Optional<Kunde> findByEmail(String email);

    /**
     * Suche nach allen Kunden, d.h. ohne Suchkriterium.
     * Beachte: Der JavaEE-Standard JPA hat nur List, nicht Stream.
     * @return Alle Kunden als Stream
     */
    // SELECT * FROM kunde
    Stream<Kunde> findAllBy();
    
    /**
     * Suche nach Kunden mit dem gegebenen Nachnamen ohne Unterscheidung
     * zwischen Gro&szlig;- und Kleinschreibung.
     * @param nachname Der gemeinsame Nachname der gesuchten Kunden
     * @return Die gefundenen Kunden als Stream
     */
    // SELECT * FROM kunde WHERE nachname ...
    Stream<Kunde> findByNachnameIgnoreCase(String nachname);

    /**
     * Suche nach Kunden mit dem gegebenen Nachnamen ohne Unterscheidung
     * zwischen Gro&szlig;- und Kleinschreibung.
     * @param nachname Der gemeinsame Nachname der gesuchten Kunden
     * @return Die gefundenen Kunden als Stream
     */
    // SELECT * FROM kunde WHERE nachname ... LIKE ...
    Stream<Kunde> findByNachnameContainingIgnoreCase(String nachname);

    /**
     * Suche nach Kunden mit dem gegebenen Nachnamen.
     * @param nachname Der gemeinsame Nachname der gesuchten Kunden
     * @return Die gefundenen Kunden als Stream mit Sortierung gem&auml;&szlig;
     *         ihrer Emailadresse
     */
    // SELECT * FROM kunde WHERE nachname = ... ORDER BY email ASC
    Stream<Kunde> findByNachnameOrderByEmailAsc(String nachname);

    /**
     * Suche nach Kunden mit der gegebenen Postleitzahl.
     * @param plz Die gemeinsame Postleitzahl der gesuchten Kunden
     * @return Die gefundenen Kunden als Stream
     */
    // SELECT * FROM kunde JOIN adresse ON ... WHERE plz = ...
    Stream<Kunde> findByAdresse_Plz(String plz);

    Stream<NachnameProjection>
        findByNachnameStartingWithIgnoreCase(String prefix);

    Stream<EmailProjection> findByEmailStartingWithIgnoreCase(String prefix);

    /*
     * Suche nach Kunden anhand ihres Nachnamens durch eine (JSON-) Query
     * f&uuml;r MongoDB
     * @return Die gefundenen Kunden als Stream
     */
    // @Query("{\"nachname\": nachname}")
    // Stream<Kunde> findByQueryNachname(String nachname)
    
    /**
     * Asynchrone Suche nach Kunden mit dem gegebenen Nachnamen
     * @param nachname Der gemeinsame Nachname der gesuchten Kunden
     * @return Die gefundenen Kunden oder eine leere Liste
     */
    @Async
    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    CompletableFuture<List<Kunde>>
        readByNachnameContainingIgnoreCase(String nachname);

    @Override
    default void customize(QuerydslBindings bindings, QKunde kunde) {
        // Suche anhand des Nachnamens:
        //      Teilstring genuegt
        //      Keine Unterscheidung zwischen Gross- und Kleinschreibung
        bindings.bind(kunde.nachname)
                .first(StringExpression::containsIgnoreCase);
        
        // Suche anhand der Email-Adresse:
        //      Keine Unterscheidung zwischen Gross- und Kleinschreibung
        bindings.bind(kunde.email)
                .first(StringExpression::equalsIgnoreCase);

        // Keine Suche anhand des Attributs "password"
        bindings.excluding(kunde.account.password);
    }
}
