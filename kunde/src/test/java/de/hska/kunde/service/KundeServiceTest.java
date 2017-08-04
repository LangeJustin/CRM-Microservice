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

import com.mongodb.gridfs.GridFSFile;
import com.querydsl.core.types.Predicate;
import de.hska.kunde.config.security.AuthService;
import de.hska.kunde.entity.Account;
import de.hska.kunde.entity.Adresse;
import de.hska.kunde.entity.Auditable;
import de.hska.kunde.entity.Kunde;
import de.hska.kunde.entity.Umsatz;
import de.hska.kunde.service.KundeRepository.NachnameProjection;
import de.hska.kunde.service.KundeRepository.EmailProjection;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;
import java.util.stream.Stream;
import javax.mail.MessagingException;

import lombok.val;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.core.Authentication;

import static de.hska.kunde.entity.FamilienstandType.LEDIG;
import static de.hska.kunde.entity.GeschlechtType.WEIBLICH;
import static de.hska.kunde.entity.InteresseType.LESEN;
import static de.hska.kunde.entity.InteresseType.REISEN;
import static java.math.BigDecimal.ONE;
import static java.util.Collections.emptySet;
import static java.util.Locale.GERMAN;
import static java.util.Locale.GERMANY;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.bson.types.ObjectId.get;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.ReflectionUtils.setField;

@Tag("service")
@DisplayName("KundenService mit Mock fuer DB-Zugriff")
@SuppressWarnings("PMD.UnusedPrivateMethod")
class KundeServiceTest {
    private static final String PLZ = "12345";
    private static final String ORT = "Testort";
    private static final String NACHNAME = "Test";
    private static final String EMAIL = "theo@test.de";
    private static final LocalDate GEBURTSDATUM = LocalDate.of(2016, 1, 31);
    private static final Currency WAEHRUNG = Currency.getInstance(GERMANY);
    private static final String HOMEPAGE = "https://test.de";
    private static final String USERNAME = "test";
    private static final String PREFIX_NACHNAME = "a";
    private static final String PREFIX_EMAIL = "e";

    @InjectMocks
    private KundeService service;
    
    @Mock
    private KundeRepository repo;

    @Mock
    private ApplicationEventPublisher publisher;
    
    @Mock
    private AuthService authService;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    @SuppressWarnings("unused")
    private Logger logger;

    @Mock
    private GridFsTemplate gridFsTemplate;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    void succeeding() {
        // Given

        // When

        // Then
        assertThat(true).isTrue();
    }

    @Test
    @Disabled
    void failing() {
        // Given

        // When

        // Then
        assertThat(false).isTrue();
    }

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
    @Nested
    class ReadTest {
        @Nested
        class IdTest {
            @Test
            @DisplayName("Suche mit gegebener ID")
            void findById() {
                // Given
                final val id = get();
                final val kundeMock = createKundeMock(id, EMAIL, NACHNAME, PLZ);
                given(repo.findById(id)).willReturn(of(kundeMock));

                // When
                final val result = service.findById(id);

                // Then
                // Assertion = Expectation
                assertSoftly(softly -> {
                    softly.assertThat(result).isPresent();
                    //noinspection OptionalGetWithoutIsPresent
                    //noinspection ConstantConditions
                    softly.assertThat(result.get().getId()).isEqualTo(id);
                });
            }

            @Test
            @DisplayName("Suche mit nicht-vorhandener ID")
            void findByIdNotFound() {
                // Given
                final val id = get();
                given(repo.findById(id)).willReturn(empty());

                // When
                final val kunde = service.findById(id);

                // Then
                assertThat(kunde).isNotPresent();
            }
        }

        @Test
        @DisplayName("Suche alle Kunden")
        @SuppressWarnings("Duplicates")
        void findAll() {
            // Given
            final val id = get();
            final val kundeMock = createKundeMock(id, EMAIL, NACHNAME, PLZ);
            final val kundenMock = Stream.of(kundeMock);
            given(repo.findAllBy()).willReturn(kundenMock);

            // When
            final val kunden = service.findAll();

            // Then
            assertThat(kunden).isNotEmpty();
        }

        @Test
        @DisplayName("Suche alle Kunden")
        void findAll2() {
            // Given
            final val id = get();
            final val kundeMock = createKundeMock(id, EMAIL, NACHNAME, PLZ);
            final val kundenMock = Stream.of(kundeMock);
            given(repo.findAllBy()).willReturn(kundenMock);

            // When
            final val kunden = service.find(null, null, null);

            // Then
            assertThat(kunden).isNotEmpty();
        }

        @TestFactory
        @DisplayName("Suche mit vorhandenem Nachnamen")
        Stream<DynamicTest> findByNachname() {
            // Given
            final val nachname = NACHNAME.toLowerCase(GERMAN);
            final val kundeMock = createKundeMock(get(), EMAIL, nachname, PLZ);
            final val kundenMock = Stream.of(kundeMock);
            given(repo.findByNachnameContainingIgnoreCase(nachname))
                .willReturn(kundenMock);

            // When
            final val kunden =
                service.find(null, nachname.toLowerCase(GERMAN), null);

            // Then
            return kunden.map(Kunde::getNachname)
                         .map(n -> dynamicTest("Nachname: " + n,
                                       () -> assertThat(n)
                                             .isEqualToIgnoringCase(nachname)));
        }

        @TestFactory
        @DisplayName("Suche mit vorhandener Emailadresse")
        Stream<DynamicTest> findByEmail() {
            // Given
            final val email = EMAIL;
            final val kundeMock = createKundeMock(get(), email, NACHNAME, PLZ);
            given(repo.findByEmail(email)).willReturn(of(kundeMock));

            // When
            final val kunden = service.find(email, null, null);

            // Then
            return kunden.map(Kunde::getEmail)
                .map(e -> dynamicTest("Email: " + e,
                    () -> assertThat(e)
                        .isEqualToIgnoringCase(email)));
        }

        @Test
        @DisplayName("Suche mit nicht-vorhandener Emailadresse")
        void findByEmailNotFound() {
            // Given
            final val email = EMAIL;
            final Optional<Kunde> kundeEmpty = empty();
            given(repo.findByEmail(email)).willReturn(kundeEmpty);

            // When
            final val kunden = service.find(email, null, null);

            // Then
            assertThat(kunden).isEmpty();
        }

        @TestFactory
        @DisplayName("Suche mit vorhandener PLZ")
        Stream<DynamicTest> findByPlz() {
            // Given
            final val plz = PLZ;
            final val kundeMock = createKundeMock(get(), EMAIL, NACHNAME, plz);
            final val kundenMock = Stream.of(kundeMock);
            given(repo.findByAdresse_Plz(plz)).willReturn(kundenMock);

            // When
            final val kunden = service.find(null, null, plz);

            // Then
            return kunden.map(Kunde::getAdresse)
                .map(Adresse::getPlz)
                .map(p -> dynamicTest("PLZ: " + p,
                    () -> assertThat(p).isEqualTo(plz)));
        }

        @TestFactory
        @DisplayName("Suche mit vorhandenem Nachnamen")
        Stream<DynamicTest> findByNachnamePlz() {
            // Given
            final val nachname = NACHNAME.toLowerCase(GERMAN);
            final val plz = PLZ;
            final val kundeMock = createKundeMock(get(), EMAIL, nachname, plz);
            final val kundenMock = Collections.singletonList(kundeMock);
            final Predicate pred = any();
            given(repo.findAll(pred)).willReturn(kundenMock);

            // When
            final val kunden =
                service.find(null, nachname.toLowerCase(GERMAN), plz);

            // Then
            return kunden.map(k -> dynamicTest("Kunde: " + k,
                    () -> assertSoftly(softly -> {
                        softly.assertThat(k.getNachname())
                            .isEqualToIgnoringCase(nachname);
                        softly.assertThat(k.getAdresse().getPlz())
                            .isEqualTo(plz);
                    })));
        }

        @Test
        @DisplayName("Suche multimediale Daten")
        void findMedia() {
            // Given
            final val id = get();
            final val kundeMock = createKundeMock(id, EMAIL, NACHNAME, PLZ);
            given(repo.findById(id)).willReturn(of(kundeMock));
            // Alternative zu null: Mocking fuer einen InputStream ...
            final GridFsResource gridFsResourceMock = null;
            given(gridFsTemplate.getResource(anyString()))
                .willReturn(gridFsResourceMock);

            // When
            service.findMedia(id);

            // Then
            verify(gridFsTemplate).getResource(anyString());
        }

        @Test
        @DisplayName("Suche multimediale Daten zu nicht-vorhandenem Kunden")
        void findMediaOhneKunde() {
            // Given
            final val id = get();
            given(repo.findById(id)).willReturn(empty());

            // When
            service.findMedia(id);

            // Then
            verify(gridFsTemplate, never()).getResource(anyString());
        }

        @TestFactory
        @DisplayName("Suche nach Nachnamen")
        Stream<DynamicTest> findNachnamenByPrefix() {
            // Given
            final val prefix = PREFIX_NACHNAME.toLowerCase(GERMAN);
            final val nachnamenMock = createNachnameProjections(prefix);
            given(repo.findByNachnameStartingWithIgnoreCase(prefix))
                .willReturn(nachnamenMock);

            // When
            final val nachnamen =
                service.findNachnamenByPrefix(prefix);

            // Then
            return nachnamen.map(NachnameProjection::getNachname)
                .map(n -> dynamicTest("Nachname: " + n,
                    () -> assertThat(n).containsIgnoringCase(prefix)));
        }

        @TestFactory
        @DisplayName("Suche nach Emailadressen")
        Stream<DynamicTest> findEmailsByPrefix() {
            // Given
            final val prefix = PREFIX_EMAIL.toLowerCase(GERMAN);
            final val emailsMock = createEmailProjections(prefix);
            given(repo.findByEmailStartingWithIgnoreCase(prefix))
                .willReturn(emailsMock);

            // When
            final val emails =
                service.findEmailsByPrefix(prefix);

            // Then
            return emails.map(EmailProjection::getEmail)
                .map(e -> dynamicTest("Email: " + e,
                    () -> assertThat(e).containsIgnoringCase(prefix)));
        }
    }
    
    // -------------------------------------------------------------------------
    // S C H R E I B E N
    // -------------------------------------------------------------------------
    @Nested
    class WriteTest {
        @Nested
        class SaveTest {
            @Test
            @DisplayName("Neuen Kunden abspeichern")
            void save() throws MessagingException {
                // Given
                final val kundeMock = createKundeMock(null, EMAIL, NACHNAME,
                                                      PLZ);
                final val kundeMockResult = createKundeMock(get(), EMAIL,
                                                            NACHNAME, PLZ);
                given(repo.findByEmail(EMAIL)).willReturn(empty());
                // Mocking einer void-Methode
                doNothing().when(authService).save(any(Account.class));
                given(repo.save(kundeMock)).willReturn(kundeMockResult);

                // When
                final val result = service.save(kundeMock);

                // Then
                assertSoftly(softly -> {
                    softly.assertThat(result.getId()).isNotNull();
                    softly.assertThat(result.getNachname()).isEqualTo(NACHNAME);
                    softly.assertThat(result.getEmail()).isEqualTo(EMAIL);
                    softly.assertThat(result.getAdresse().getPlz())
                          .isEqualTo(PLZ);
                });

                // Auf dem Mock-Objekt publisher wurde die Methode
                // publishEvent() 1x aufgerufen
                verify(publisher).publishEvent(any(NeuerKundeEvent.class));
            }

            @Test
            @DisplayName("Neuer Kunde ohne Account")
            void saveNoAccount() {
                // Given
                final val kundeMock = createKundeMock(null, EMAIL, NACHNAME,
                                                      PLZ);
                kundeMock.setAccount(null);

                // When
                final val thrown =
                    catchThrowable(() -> service.save(kundeMock));

                // Then
                assertThat(thrown)
                    .isInstanceOf(InvalidAccountException.class)
                    .hasNoCause();
            }

            @Test
            @DisplayName("Neuer Kunde mit existierender Email")
            void saveEmailExists() {
                // Given
                final val kundeMock = createKundeMock(null, EMAIL, NACHNAME,
                                                      PLZ);
                given(repo.findByEmail(EMAIL)).willReturn(of(kundeMock));

                // When
                final val thrown =
                    catchThrowable(() -> service.save(kundeMock));

                // Then
                assertThat(thrown)
                    .isInstanceOf(EmailExistsException.class)
                    .hasNoCause();
            }
        }

        @Nested
        class UpdateTest {
            @Test
            @DisplayName("Vorhandenen Kunden aktualisieren")
            void update() {
                // Given
                final val id = get();
                final val kundeMock = createKundeMock(id, EMAIL, NACHNAME, PLZ);
                given(repo.findById(id)).willReturn(of(kundeMock));
                given(repo.findByEmail(EMAIL)).willReturn(of(kundeMock));
                given(repo.save(kundeMock)).willReturn(kundeMock);

                // When
                final val result =
                    service.update(kundeMock,
                                   kundeMock.getVersion().toString());

                // Then
                assertSoftly(softly -> {
                    softly.assertThat(result).isPresent();
                    //noinspection ConstantConditions
                    softly.assertThat(result.get().getId()).isEqualTo(id);
                });
            }

            @Test
            @DisplayName("Nicht-existierenden Kunden aktualisieren")
            void updateEmpty() {
                // Given
                final val id = get();
                final val kundeMock = createKundeMock(id, null, NACHNAME,
                                                      PLZ);
                final Optional<Kunde> kundeNotFound = empty();
                given(repo.findById(id)).willReturn(kundeNotFound);

                // When
                final val result = service.update(kundeMock, null);

                // Then
                assertThat(result).isNotPresent();
                verify(repo, never()).save(kundeMock);
            }

            @Test
            @DisplayName("Kunde aktualisieren mit falscher Versionsnummer")
            void updateInvalidVersion() {
                // Given
                final val id = get();
                final val kundeMock = createKundeMock(id, EMAIL, NACHNAME, PLZ);
                given(repo.findById(id)).willReturn(of(kundeMock));
                final val invalidVersion = "?!§";

                // When
                final val thrown =
                    catchThrowable(() -> service.update(kundeMock,
                                                        invalidVersion));

                // Then
                assertThat(thrown)
                    .isInstanceOf(ConcurrentUpdatedException.class)
                    .hasCauseInstanceOf(NumberFormatException.class);
                verify(repo, never()).save(kundeMock);
            }

            @Test
            @DisplayName("Kunde aktualisieren mit alter Versionsnummer")
            void updateAlteVersion() {
                // Given
                final val id = get();
                final val kundeMock = createKundeMock(id, EMAIL, NACHNAME, PLZ);
                given(repo.findById(id)).willReturn(of(kundeMock));
                final val alteVersion = "-1";

                // When
                final val thrown =
                    catchThrowable(() -> service.update(kundeMock,
                                                        alteVersion));

                // Then
                assertThat(thrown)
                    .isInstanceOf(ConcurrentUpdatedException.class)
                    .hasNoCause();
                verify(repo, never()).save(kundeMock);
            }

            @Test
            @DisplayName("Multimediale Daten speichern")
            void saveMedia() {
                // Given
                final val id = get();
                final val kundeMock = createKundeMock(id, EMAIL, NACHNAME, PLZ);
                given(repo.findById(id)).willReturn(of(kundeMock));
                // gridFsTemplate.delete(query);
                doNothing().when(gridFsTemplate).delete(any(Query.class));
                final InputStream inputStreamMock = any(InputStream.class);
                // final val contentType = "application/json"
                final GridFSFile gridFSFile = new GridFSFile() {
                };
                given(gridFsTemplate.store(inputStreamMock, anyString(),
                                           anyString()))
                    .willReturn(gridFSFile);

                // When
                // service.save(inputStreamMock, id, contentType)

                // Then
                // verify(gridFsTemplate).store(inputStreamMock, anyString(),
                //                              contentType)
            }
        }

        @Nested
        class DeleteTest {
            @Test
            @DisplayName("Vorhandenen Kunden loeschen")
            void delete() {
                // Given
                final val id = get();
                given(repo.exists(id)).willReturn(true);
                given(authentication.getAuthorities()).willReturn(emptySet());
                given(authentication.getPrincipal()).willReturn(null);

                // When
                service.delete(id, authentication);

                // Then
                verify(repo).exists(id);
                verify(repo).delete(id);
            }

            @Test
            @DisplayName("Nicht-vorhandenen Kunden loeschen")
            void deleteNotExisting() {
                // Given
                final val id = get();
                given(repo.exists(id)).willReturn(false);
                given(authentication.getAuthorities()).willReturn(emptySet());
                given(authentication.getPrincipal()).willReturn(null);

                // When
                service.delete(id, authentication);

                // Then
                verify(repo).exists(id);
                verify(repo, never()).delete(id);
            }

            @Test
            @DisplayName("Kunde loeschen ohne Authentication: Kein Logging")
            void deleteNoAuthentication() {
                // Given
                final val id = get();
                given(repo.exists(id)).willReturn(true);

                // When
                service.delete(id, null);

                // Then
                verify(repo).exists(id);
                verify(repo).delete(id);
            }

            @Test
            @DisplayName("Kunde mittels Email loeschen: Kein Logging von Auth")
            void deleteEmail() {
                // Given
                final val email = EMAIL;
                final val id = get();
                final val kundeMock = createKundeMock(id, EMAIL, NACHNAME, PLZ);
                given(repo.findByEmail(email)).willReturn(of(kundeMock));
                given(repo.exists(id)).willReturn(true);

                // When
                service.delete(email, null);

                // Then
                verify(repo).delete(any(Kunde.class));
            }
        }
    }
    
    // -------------------------------------------------------------------------
    // Hilfsmethoden fuer Mocking
    // -------------------------------------------------------------------------
    private Kunde createKundeMock(ObjectId id, String email, String nachname,
                                  String plz) {
        final val adresse = createAdresseMock(plz);
        final val kunde = createKundeMock(nachname, email, adresse);
        setId(kunde, id);
        return kunde;
    }
    
    private Adresse createAdresseMock(String plz) {
        final val plzMock = plz == null ? PLZ : plz;
        return Adresse.builder()
                      .plz(plzMock)
                      .ort(ORT)
                      .build();
    }

    private Kunde createKundeMock(String nachname, String email,
                                  Adresse adresse) {
        final URL homepage;
        try {
            homepage = new URL(HOMEPAGE);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        final val umsatz = Umsatz.builder()
                                 .betrag(ONE)
                                 .waehrung(WAEHRUNG)
                                 .build();
        final val account = Account.builder()
                                   .username(USERNAME)
                                   .password("p")
                                   .build();
        final val emailMock = email == null ? EMAIL : email;
        final val kunde = Kunde.builder()
                               .nachname(nachname)
                               .email(emailMock)
                               .newsletter(true)
                               .umsatz(umsatz)
                               .homepage(homepage)
                               .geburtsdatum(GEBURTSDATUM)
                               .geschlecht(WEIBLICH)
                               .familienstand(LEDIG)
                               .interesse(LESEN)
                               .interesse(REISEN)
                               .adresse(adresse)
                               .account(account)
                               .build();
        setKundeVersion0(kunde);
        return kunde;
    }

    private void setKundeVersion0(Kunde kunde) {
        // Das private Attribut "id" ohne set-Methode setzen
        final val versionField = findField(Auditable.class, "version");
        makeAccessible(versionField);
        setField(versionField, kunde, 0);
    }
    
    private void setId(Object obj, ObjectId id) {
        // Das private Attribut "id" ohne set-Methode setzen
        final val idField = findField(obj.getClass(), "id");
        makeAccessible(idField);
        setField(idField, obj, id);
    }

    private Stream<NachnameProjection>
            createNachnameProjections(String prefix) {
        return Stream.of(() -> "A" + prefix, () -> "B" + prefix);
    }

    private Stream<EmailProjection> createEmailProjections(String prefix) {
        return Stream.of(() -> prefix + "@hska.de", () -> prefix + "@test.de");
    }
}
