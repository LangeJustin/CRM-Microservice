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

import de.hska.kunde.config.DbConfig;
import de.hska.kunde.entity.Account;
import de.hska.kunde.entity.Adresse;
import de.hska.kunde.entity.Kunde;
import de.hska.kunde.entity.Umsatz;
import de.hska.kunde.service.KundeRepository.EmailProjection;
import de.hska.kunde.service.KundeRepository.NachnameProjection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Currency;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import lombok.val;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static de.hska.kunde.config.Settings.DEV_PROFILE;
import static de.hska.kunde.entity.GeschlechtType.WEIBLICH;
import static de.hska.kunde.entity.InteresseType.LESEN;
import static de.hska.kunde.entity.InteresseType.REISEN;
import static java.math.BigDecimal.ONE;
import static java.util.Locale.GERMAN;
import static java.util.Locale.GERMANY;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

// FIXME Parallele Ausfuehrung noch nicht mit JUnit 5
@Tag("repository")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    DbConfig.class,
    RepositoryPopulatorConfig.class,
    JacksonAutoConfiguration.class,
    // fuer GridFsTemplate
    MongoDataAutoConfiguration.class
})
@TestPropertySource(locations = "/repositorytest.properties")
@ActiveProfiles(DEV_PROFILE)
@DisplayName("Repository mit Spring Data fuer den Zugriff auf MongoDB")
@SuppressWarnings("squid:S2187")
class KundeRepositoryTest {
    private static final ObjectId ID_VORHANDEN =
        new ObjectId("000000000000000000000001");
    private static final ObjectId ID_NICHT_VORHANDEN =
        new ObjectId("999999999999999999999999");
    private static final ObjectId ID_UPDATE =
        new ObjectId("000000000000000000000002");
    private static final ObjectId ID_DELETE =
        new ObjectId("000000000000000000000005");
    
    private static final String NACHNAME_VORHANDEN = "al";
    private static final String NACHNAME_PREFIX = NACHNAME_VORHANDEN;
    private static final String NACHNAME_NICHT_VORHANDEN = "xx";
    private static final String EMAIL_VORHANDEN = "alpha@hska.edu";
    private static final String EMAIL_NICHT_VORHANDEN = "nichtVorhanden";
    private static final String PLZ_VORHANDEN = "76133";

    private static final String NEUE_PLZ = "12345";
    private static final String NEUER_ORT = "Testort";
    private static final String NEUER_NACHNAME = "Neuernachname";
    private static final String NEUE_EMAIL = "email@test.de";
    private static final LocalDate NEUES_GEBURTSDATUM =
        LocalDate.of(2016, 1, 31);
    private static final Currency NEUE_WAEHRUNG = Currency.getInstance(GERMANY);
    private static final String NEUE_HOMEPAGE = "https://test.de";
    private static final String NEUER_USERNAME = "test";

    @Autowired
    private KundeRepository repo;

    @Autowired
    private ApplicationContext ctx;

    @BeforeEach
    void beforeEach() {
        assertThat(ctx).isNotNull();
    }

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
    @Nested
    class ReadTest {
        @SuppressWarnings("UnnecessaryLocalVariable")
        @Nested
        class IdTest {
            @Test
            @DisplayName("Suche mit vorhandener ID")
            void findById() {
                // Given
                final val id = ID_VORHANDEN;

                // When
                final val kunde = repo.findById(id);

                // Then
                assertSoftly(softly -> {
                    softly.assertThat(kunde).isPresent();
                    //noinspection OptionalGetWithoutIsPresent
                    //noinspection ConstantConditions
                    softly.assertThat(kunde.get().getId()).isEqualTo(id);
                });
            }

            @Test
            @DisplayName("Suche mit nicht-vorhandener ID")
            void findByIdNotFound() {
                // Given
                final val id = ID_NICHT_VORHANDEN;

                // When
                final val kunde = repo.findById(id);

                // Then
                assertThat(kunde).isNotPresent();
            }
        }

        @Test
        @DisplayName("Alle Kunden suchen")
        void findAll() {
            // Given

            // When
            try (val kunden = repo.findAllBy()) {

                // Then
                assertThat(kunden).isNotEmpty();
            }
        }

        @Test
        @DisplayName("Suche mit vorhandener Email")
        void findByEmail() {
            // Given
            final val email = EMAIL_VORHANDEN;

            // When
            final val kunde = repo.findByEmail(email);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(kunde).isPresent();
                //noinspection OptionalGetWithoutIsPresent,ConstantConditions
                softly.assertThat(kunde.get().getEmail()).isEqualTo(email);
            });
        }

        @Test
        @DisplayName("Suche mit nicht-vorhandener Email")
        void findByEmailNotFound() {
            // Given
            @SuppressWarnings("UnnecessaryLocalVariable")
            final val email = EMAIL_NICHT_VORHANDEN;

            // When
            final val kunde = repo.findByEmail(email);

            // Then
            assertThat(kunde).isNotPresent();
        }

        @TestFactory
        @DisplayName("Suche mit vorhandenem Nachnamen (Kleinschreibung)")
        Stream<DynamicTest> findByNachnameContainingIgnoreCase() {
            // Given
            final val nachname = NACHNAME_VORHANDEN.toLowerCase(GERMAN);

            // When
            try (val kunden = repo.findByNachnameIgnoreCase(nachname)) {

                // Then
                return kunden.map(Kunde::getNachname)
                             .map(n -> dynamicTest("Nachname: " + n,
                                       () -> assertThat(n)
                                             .isEqualToIgnoringCase(nachname)));
            }
        }

        @Test
        @DisplayName("Suche mit nicht-vorhandenem Nachnamen")
        @SuppressWarnings("checkstyle:RightCurly")
        void findByNachnameNichtVorhanden() {
            // Given
            @SuppressWarnings("UnnecessaryLocalVariable")
            final val nachname = NACHNAME_NICHT_VORHANDEN;

            // When
            try (Stream<Kunde> kunden =
                               repo.findByNachnameIgnoreCase(nachname)) {

                // Then
                assertThat(kunden).isEmpty();
            }
        }

        @TestFactory
        @DisplayName("Suche mit vorhandener Postleitzahl")
        Stream<DynamicTest> findByPlz() {
            // Given
            final val plz = PLZ_VORHANDEN;

            // When
            try (val kunden = repo.findByAdresse_Plz(plz)) {

                // Then
                return kunden.map(Kunde::getAdresse)
                             .map(Adresse::getPlz)
                             .map(p -> dynamicTest("PLZ: " + p,
                                                   () -> assertThat(p)
                                                         .isEqualTo(plz)));
            }
        }

        @TestFactory
        @DisplayName("Suche Nachnamen mit Prefix")
        Stream<DynamicTest> findNachnamenByPrefix() {
            // Given
            final val prefix = NACHNAME_PREFIX;

            // When
            try (val nachnamen =
                     repo.findByNachnameStartingWithIgnoreCase(prefix)) {

                // Then
                return nachnamen.map(NachnameProjection::getNachname)
                    .map(n -> dynamicTest("Nachname: " + n,
                         () -> assertThat(n.toLowerCase(GERMAN))
                               .startsWith(prefix.toLowerCase(GERMAN))));
            }
        }

        @TestFactory
        @DisplayName("Suche Emails mit Prefix")
        Stream<DynamicTest> findEmailsByPrefix() {
            // Given
            final val prefix = NACHNAME_PREFIX;

            // When
            try (val emails =
                     repo.findByEmailStartingWithIgnoreCase(prefix)) {

                // Then
                return emails.map(EmailProjection::getEmail)
                    .map(e -> dynamicTest("Email: " + e,
                        () -> assertThat(e.toLowerCase(GERMAN))
                            .startsWith(prefix.toLowerCase(GERMAN))));
            }
        }

        @DisplayName("Asynchrone Suche mit vorhandem Nachnamen")
        @TestFactory
        Stream<DynamicTest> readByNachnameAsync()
                            throws InterruptedException, ExecutionException,
                                   TimeoutException {
            // Given
            final val nachname = NACHNAME_VORHANDEN.toLowerCase(GERMAN);

            // When
            final val futureResult =
                repo.readByNachnameContainingIgnoreCase(nachname);
            final val result = futureResult.get(1, SECONDS);

            // Then
            assertThat(result).isNotEmpty();
            return result.stream()
                    .map(Kunde::getNachname)
                    .map(n -> dynamicTest("Nachname: " + n,
                                       () -> assertThat(n)
                                             .containsIgnoringCase(nachname)));
        }
    }
    
    // -------------------------------------------------------------------------
    // S C H R E I B E N
    // -------------------------------------------------------------------------
    @Nested
    class WriteTest {
        @Test
        @DisplayName("Einen neuen Kunden abspeichern")
        void save() throws MalformedURLException {
            // Given
            final val neueHomepage = new URL(NEUE_HOMEPAGE);
            final val umsatz = Umsatz.builder()
                                     .betrag(ONE)
                                     .waehrung(NEUE_WAEHRUNG)
                                     .build();
            final val adresse = Adresse.builder()
                                       .plz(NEUE_PLZ)
                                       .ort(NEUER_ORT)
                                       .build();
            final val account = Account.builder()
                                       .username(NEUER_USERNAME)
                                       .password("p")
                                       .build();
            final val neuerKunde = Kunde.builder()
                                        .nachname(NEUER_NACHNAME)
                                        .email(NEUE_EMAIL)
                                        .newsletter(true)
                                        .umsatz(umsatz)
                                        .homepage(neueHomepage)
                                        .geburtsdatum(NEUES_GEBURTSDATUM)
                                        .geschlecht(WEIBLICH)
                                        .interesse(LESEN)
                                        .interesse(REISEN)
                                        .adresse(adresse)
                                        .account(account)
                                        .build();

            // When
            final val result = repo.save(neuerKunde);

            // Then
            final val id = result.getId();
            assertThat(id).isNotNull();
            final val tmp = repo.findById(id);
            assertSoftly(softly -> {
                softly.assertThat(tmp).isPresent();
                //noinspection OptionalGetWithoutIsPresent,ConstantConditions
                softly.assertThat(tmp.get().getNachname())
                      .isEqualTo(NEUER_NACHNAME);
            });
        }

        @Test
        @DisplayName("Einen vorhandenen Kunden aktualisieren")
        void update() {
            // Given
            final val id = ID_UPDATE;
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            final val kunde = repo.findById(id)
                                  .orElseThrow(IllegalStateException::new);
            kunde.setNachname(NEUER_NACHNAME);

            // When
            final val result = repo.save(kunde);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(result).isNotNull();
                softly.assertThat(result.getId()).isEqualTo(id);
                softly.assertThat(result.getNachname())
                      .isEqualTo(NEUER_NACHNAME);
            });
        }

        @Test
        @DisplayName("Einen vorhandenen Kunden loeschen")
        void delete() {
            // Given
            final val id = ID_DELETE;

            // When
            repo.delete(id);

            // Then
            final val notFound = repo.findById(id);
            assertThat(notFound).isNotPresent();
        }
    }
}
