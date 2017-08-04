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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JsonProvider;
import de.hska.kunde.entity.Account;
import de.hska.kunde.entity.Adresse;
import de.hska.kunde.entity.InteresseType;
import de.hska.kunde.entity.Kunde;
import de.hska.kunde.entity.Umsatz;
import de.hska.kunde.util.PatchOperation;
import lombok.extern.log4j.Log4j2;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.jayway.jsonpath.Configuration.defaultConfiguration;
import static de.hska.kunde.config.Settings.DEV_PROFILE;
import static de.hska.kunde.entity.GeschlechtType.WEIBLICH;
import static de.hska.kunde.entity.InteresseType.LESEN;
import static de.hska.kunde.entity.InteresseType.REISEN;
import static de.hska.kunde.entity.InteresseType.SPORT;
import static java.math.BigDecimal.ONE;
import static java.util.Arrays.asList;
import static java.util.Locale.GERMAN;
import static java.util.Locale.GERMANY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.bson.types.ObjectId.isValid;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption.SSL;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpHeaders.IF_MATCH;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.parseMediaTypes;

@Tag("integration")
@ExtendWith(SpringExtension.class)
// Alternative zu @ContextConfiguration von Spring
// Default: webEnvironment = MOCK, d.h.
//          Mock Servlet Umgebung anstatt eines Embedded Servlet Containers
@SpringBootTest(webEnvironment = RANDOM_PORT)
// @SpringBootTest(webEnvironment = DEFINED_PORT, ...)
// ggf.: @DirtiesContext, falls z.B. ein Spring Bean modifiziert wurde
@ActiveProfiles(DEV_PROFILE)
@TestPropertySource(locations = "/controllertest.properties")
@DisplayName("End-to-End Test fuer den Microservice \"Kunde\"")
@Log4j2
@SuppressWarnings({
    "PMD.AvoidDuplicateLiterals",
    "squid:S2187",
    "UnnecessaryLocalVariable"
})
class KundeControllerTest {
    private static final String HOST = "localhost";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "p";
    private static final String PASSWORD_FALSCH = "?!$";

    private static final String ID_VORHANDEN = "000000000000000000000001";
    private static final String ID_INVALID = "YYYYYYYYYYYYYYYYYYYYYYYY";
    private static final String ID_NICHT_VORHANDEN = "999999999999999999999999";
    private static final String ID_UPDATE_PUT = "000000000000000000000002";
    private static final String ID_UPDATE_PATCH = "000000000000000000000003";
    private static final String ID_UPDATE_PNG = "000000000000000000000003";
    private static final String ID_UPDATE_MP4 = "000000000000000000000003";
    private static final String ID_DELETE = "000000000000000000000004";
    private static final String EMAIL_DELETE = "epsilon@hska.cn";

    private static final String NACHNAME = "alpha";

    private static final String NEUE_PLZ = "12345";
    private static final String NEUE_PLZ_INVALID = "1234";
    private static final String NEUER_ORT = "Testort";
    private static final String NEUER_NACHNAME = "Neuernachname";
    private static final String NEUER_NACHNAME_INVALID = "?!$";
    private static final String NEUE_EMAIL = "email@test.de";
    private static final String NEUE_EMAIL_INVALID = "email@";
    private static final LocalDate NEUES_GEBURTSDATUM =
            LocalDate.of(2016, 1, 31);
    private static final Currency NEUE_WAEHRUNG = Currency.getInstance(GERMANY);
    private static final String NEUE_HOMEPAGE = "https://test.de";
    private static final String NEUER_USERNAME = "test";

    private static final InteresseType NEUES_INTERESSE = SPORT;
    private static final InteresseType ZU_LOESCHENDES_INTERESSE = LESEN;

    private static final String ID_URI_TEMPLATE = "/{id}";
    private static final String EMAIL_URI_TEMPLATE = "?email={email}";

    private static final JsonProvider JSON_PROVIDER = defaultConfiguration()
                                                      .jsonProvider();

    private TestRestTemplate restTemplate;
    private String baseUri;
    private String baseUriId;
    private String baseUriEmail;

    // s.o. RANDOM_PORT
    @LocalServerPort
    @SuppressWarnings({"unused", "CanBeFinal"})
    private int port;

    @Value("server.httpsPort")
    private String httpsPort;

    @Autowired
    private ApplicationContext ctx;

    @BeforeEach
    void beforeEach() {
        final String schema;
        //noinspection ConstantConditions
        if (httpsPort == null) {
            schema = "http";
            restTemplate = new TestRestTemplate(USERNAME, PASSWORD);
        } else {
            schema = "https";
            restTemplate = new TestRestTemplate(USERNAME, PASSWORD, SSL);
        }

        // @BeforeAll nur bei static-Methoden
        baseUri = schema + "://" + HOST + ":" + port + "/kunde";
        baseUriId = baseUri + ID_URI_TEMPLATE;
        baseUriEmail = baseUri + EMAIL_URI_TEMPLATE;
        log.info("port = {}", port);

        assertThat(ctx).isNotNull();
    }

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
    @Nested
    class ReadTest {
        @Nested
        class IdTest {
            @Test
            @DisplayName("Suche mit vorhandener ID")
            void findById() throws IOException {
                // Given
                final val id = ID_VORHANDEN;

                // When
                final val resource =
                    restTemplate.getForObject(baseUriId,
                        KundeResourceClient.class, id);

                // Then
                log.debug("Gefundener Kunde = {}", resource);
                assertSoftly(softly -> {
                    softly.assertThat(resource).isNotNull();
                    softly.assertThat(resource.getNachname()).isNotNull();
                    softly.assertThat(resource.getEmail()).isNotNull();
                    softly.assertThat(resource.getAtomLinks()
                                              .get("self")
                                              .get("href")).endsWith("/" + id);
                });
            }

            @Test
            @DisplayName("Suche mit vorhandener ID und JsonPath")
            void findByIdJsonPath() throws IOException {
                // Given
                final val id = ID_VORHANDEN;

                // When
                final val bodyStr =
                    restTemplate.getForObject(baseUriId, String.class, id);

                // Then
                // Pruefungen auf Basis von JsonPath
                final val jsonObj = JSON_PROVIDER.parse(bodyStr);
                // { ...
                //     _links: {
                //         self: { href: https://... }
                final String self = JsonPath.read(jsonObj,
                                                  "$._links.self.href");
                assertThat(self).endsWith("/" + id);
            }

            @Test
            @DisplayName("Suche mit syntaktisch ungueltiger ID")
            void findByIdInvalid() {
                // Given
                final val id = ID_INVALID;

                // When
                final val response = restTemplate.getForEntity(baseUriId,
                                                       KundeResource.class, id);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
            }

            @Test
            @DisplayName("Suche mit nicht vorhandener ID")
            void findByIdNotFound() {
                // Given
                final val id = ID_NICHT_VORHANDEN;

                // When
                final val response = restTemplate.getForEntity(baseUriId,
                                                              String.class, id);

                // Then
                assertSoftly(softly -> {
                    softly.assertThat(response.getStatusCode())
                          .isEqualTo(NOT_FOUND);
                    softly.assertThat(response.getBody())
                          .contains(NOT_FOUND.getReasonPhrase());
                });
            }

            @Test
            @DisplayName("Suche mit ID, aber falschem Passwort")
            void findByIdFalschesPasswort() throws IOException {
                // Given
                final val id = ID_VORHANDEN;
                restTemplate = new TestRestTemplate(USERNAME, PASSWORD_FALSCH,
                        SSL);

                // When
                final val resource =
                          restTemplate.getForEntity(baseUriId,
                                                    KundeResource.class, id);

                // Then
                assertThat(resource.getStatusCode()).isEqualTo(UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Suche nach allen Kunden")
        void findAlleKunden() {
            // Given

            // When
            final val kunden =
                      restTemplate.getForObject(baseUri, KundeResource[].class);

            // Then
            assertThat(kunden).isNotEmpty();
        }

        @Test
        @DisplayName("Suche nach allen Kunden als Liste")
        @SuppressWarnings("WhitespaceAround")
        void findAlleKundenAsList() {
            // Given

            // When
            // Type Erasure: abstrakte Klasse mit protected Konstruktor
            final val typeRef =
                      new ParameterizedTypeReference<List<KundeResource>>() {};
            final val response =
                      restTemplate.exchange(baseUri, GET, null,
                                            typeRef);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(OK);
            final val kunden = response.getBody();
            assertThat(kunden).isNotEmpty();
        }

        @TestFactory
        @DisplayName("Suche mit vorhandenem Nachnamen")
        Stream<DynamicTest> findByNachname() {
            // Given
            final val nachname = NACHNAME.toLowerCase(GERMAN);

            // When
            final val kunden =
                    restTemplate.getForObject(baseUri + "?nachname=" + nachname,
                                              KundeResource[].class);

            // Then
            assertThat(kunden).isNotEmpty();
            return Stream.of(kunden)
                    .map(KundeResource::getNachname)
                    .map(n -> dynamicTest("Nachname: " + n,
                                          () -> assertThat(n)
                                             .isEqualToIgnoringCase(nachname)));
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
            @DisplayName("Abspeichern eines neuen Kunden")
            void save() throws MessagingException, MalformedURLException {
                // Given
                final val homepage = new URL(NEUE_HOMEPAGE);
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
                        .geburtsdatum(NEUES_GEBURTSDATUM)
                        .umsatz(umsatz)
                        .homepage(homepage)
                        .geschlecht(WEIBLICH)
                        .interesse(LESEN)
                        .interesse(REISEN)
                        .adresse(adresse)
                        .account(account)
                        .build();

                // When
                final val response = restTemplate.postForEntity(baseUri,
                                                        neuerKunde, Void.class);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(CREATED);
                final val location = response.getHeaders().getLocation();
                assertThat(location).isNotNull();
                final val locationStr = location.toString();
                final val indexLastSlash = locationStr.lastIndexOf('/');
                final val idStr = locationStr.substring(indexLastSlash + 1);
                assertThat(isValid(idStr)).isTrue();
            }

            @Test
            @DisplayName("Abspeichern eines neuen Kunden m. ungueltigen Werten")
            void saveInvalid() {
                // Given
                final val adresse = Adresse.builder()
                        .plz(NEUE_PLZ_INVALID)
                        .ort(NEUER_ORT)
                        .build();
                final val neuerKunde = Kunde.builder()
                        .nachname(NEUER_NACHNAME_INVALID)
                        .email(NEUE_EMAIL_INVALID)
                        .newsletter(true)
                        .geburtsdatum(NEUES_GEBURTSDATUM)
                        .geschlecht(WEIBLICH)
                        .interesse(LESEN)
                        .interesse(REISEN)
                        .adresse(adresse)
                        .build();

                // When
                final val response =
                          restTemplate.postForEntity(baseUri, neuerKunde,
                                                     String.class);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
                final val body = response.getBody();
                assertSoftly(softly -> {
                    softly.assertThat(body)
                          .contains("Pattern.kunde.adresse.plz");
                    softly.assertThat(body).contains("Pattern.kunde.nachname");
                    softly.assertThat(body).contains("Email.kunde.email");
                });
            }
        }

        @Nested
        class UpdateTest {
            @Test
            @DisplayName("Aendern eines vorhandenen Kunden durch Put")
            void updatePut() {
                // Given
                final String id = ID_UPDATE_PUT;

                // When
                final val responseOrig = restTemplate.getForEntity(baseUriId,
                                                            KundeResource.class,
                                                            id);

                final val resourceOrig = responseOrig.getBody();
                final Kunde kunde = Kunde.builder()
                        .id(new ObjectId(id))
                        .email(resourceOrig.getEmail() + "x")
                        .adresse(resourceOrig.getAdresse())
                        .username(resourceOrig.getUsername())
                        .build();

                final val etag = responseOrig.getHeaders().getETag();
                final val version = etag.substring(1, etag.length() - 1);
                //noinspection RawTypeCanBeGeneric
                final val headers = new LinkedMultiValueMap<String, String>();
                //noinspection unchecked
                headers.add(IF_MATCH, version);

                @SuppressWarnings("unchecked")
                final val httpEntity = new HttpEntity<>(kunde, headers);
                final ResponseEntity<String> response =
                          restTemplate.exchange(baseUri, PUT, httpEntity,
                                                String.class);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
                final val resourceUpdated =
                          restTemplate.getForObject(baseUriId,
                                                    KundeResource.class, id);
                assertThat(resourceUpdated.getEmail()).endsWith("x");
            }

            @Test
            @DisplayName("Aendern eines vorhandenen Kunden durch Patch")
            void updatePatch() {
                // Given
                final val id = ID_UPDATE_PATCH;
                final val replaceOp = PatchOperation.builder()
                                      .op("replace")
                                      .path("/email")
                                      .value(NEUE_EMAIL)
                                      .build();
                final val addOp = PatchOperation.builder()
                                  .op("add")
                                  .path("/interessen")
                                  .value(NEUES_INTERESSE.getValue())
                                  .build();
                final val removeOp = PatchOperation.builder()
                                     .op("remove")
                                     .path("/interessen")
                                     .value(ZU_LOESCHENDES_INTERESSE.getValue())
                                     .build();
                // FIXME Java 9: List.of
                final val operations = asList(replaceOp, addOp, removeOp);

                // When
                final val response = restTemplate.patchForObject(baseUriId,
                                                   new HttpEntity<>(operations),
                                                   Void.class, id);

                // Then
                assertThat(response).isNotNull();
            }
        }

        @Nested
        class MultimediaTest {
            @Test
            @DisplayName("Upload und Download eines PNG-Bildes")
            void uploadDownloadPng() {
                // Given
                final val id = ID_UPDATE_PNG;
                final val image =
                          Paths.get("src", "test", "resources",
                                    "rest", "image.png")
                               .toFile();
                final val parts = new LinkedMultiValueMap<String, Object>();
                //noinspection unchecked
                parts.add("file", new FileSystemResource(image));
                parts.add("content-type", "image/png");

                // When
                restTemplate.put(baseUriId, parts, id);

                // Then
                final byte[] bytesDownload =
                        restTemplate.getForObject(baseUriId + "/media",
                                byte[].class, id);
                assertSoftly(softly -> {
                    softly.assertThat(bytesDownload).isNotNull();
                    softly.assertThat(bytesDownload).isNotEmpty();
                    softly.assertThat(Long.valueOf(bytesDownload.length))
                          .isEqualTo(image.length());
                });
            }

            @Test
            @DisplayName("Upload und Download eines MP4-Videos")
            void uploadDownloadMp4() throws IOException {
                // Given
                final val id = ID_UPDATE_MP4;
                final val video =
                          Paths.get("src", "test", "resources",
                                    "rest", "video.mp4")
                               .toFile();
                final val parts =
                    new LinkedMultiValueMap<String, Object>();
                parts.add("file", new FileSystemResource(video));
                parts.add("content-type", "video/mp4");

                // When
                restTemplate.put(baseUriId, parts, id);

                // Then
                final val bytesDownload =
                          restTemplate.getForObject(baseUriId + "/media",
                                                    byte[].class, id);
                assertSoftly(softly -> {
                    softly.assertThat(bytesDownload).isNotNull();
                    softly.assertThat(bytesDownload).isNotEmpty();
                    softly.assertThat(Long.valueOf(bytesDownload.length))
                          .isEqualTo(video.length());
                });
            }
        }

        @Test
        @DisplayName("Loeschen eines vorhandenen Kunden mit der ID")
        void deleteById() {
            // Given
            final val id = ID_DELETE;

            // When
            restTemplate.delete(baseUriId, id);
        }

        @Test
        @DisplayName("Loeschen eines vorhandenen Kunden mit der Emailadresse")
        void deleteByEmail() {
            // Given
            final val email = EMAIL_DELETE;

            // When
            restTemplate.delete(baseUriEmail, email);
        }
    }

    @TestConfiguration
    @SuppressWarnings("unused")
    static class Config {
        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder()
                   .additionalMessageConverters(getHalMessageConverter())
                   .basicAuthorization(USERNAME, PASSWORD);
        }

        private HttpMessageConverter<?> getHalMessageConverter() {
            final val mapper = new ObjectMapper();
            mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.registerModule(new Jackson2HalModule());

            final val halConverter = new MappingJackson2HttpMessageConverter();
            halConverter.setSupportedMediaTypes(
                                               parseMediaTypes(HAL_JSON_VALUE));
            halConverter.setObjectMapper(mapper);

            return halConverter;
        }
    }
}
