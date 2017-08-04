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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.bestellung.entity.Bestellposition;
import de.hska.bestellung.entity.Bestellung;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static de.hska.bestellung.config.Settings.DEV_PROFILE;
import static java.math.BigDecimal.TEN;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.bson.types.ObjectId.isValid;
import static org.springframework.boot.test.context.SpringBootTest
              .WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV_PROFILE)
@TestPropertySource(locations = "/controllertest.properties")
@Log4j2
@SuppressWarnings("UnnecessaryLocalVariable")
public class BestellungControllerTest {
    private static final String HOST = "localhost";

    private static final String ID_VORHANDEN = "100000000000000000000001";
    private static final String ID_INVALID = "YYYYYYYYYYYYYYYYYYYYYYYY";
    private static final String ID_NICHT_VORHANDEN = "999999999999999999999999";
    private static final String KUNDE_ID = "000000000000000000000001";
    private static final String ARTIKEL_ID = "200000000000000000000001";

    private static final String ID_URI_TEMPLATE = "/{id}";

    private TestRestTemplate restTemplate;
    private String baseUri;
    
    @SuppressFBWarnings({"NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD"})
    @SuppressWarnings("unused")
    private JacksonTester<BestellungResource> json;
    
    // s.o. RANDOM_PORT
    @LocalServerPort
    @SuppressWarnings({"CanBeFinal", "unused"})
    private int port;
    
    @Before
    public void beforeEach() throws MalformedURLException {
        restTemplate = new TestRestTemplate();
        baseUri = "http://" + HOST + ":" + port;
        log.info("port = {}", port);
        
        JacksonTester.initFields(this, new ObjectMapper());
    }

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
        @Test
        public void findById() throws IOException {
            // Given
            final val id = ID_VORHANDEN;

            // When
            final val resource =
                restTemplate.getForObject(baseUri + ID_URI_TEMPLATE,
                                          BestellungResourceClient.class, id);

            // Then
            assertThat(resource).isNotNull();
            log.debug("BestellungResource = {}", resource);
            assertSoftly(softly -> {
                softly.assertThat(resource).isNotNull();
                softly.assertThat(resource.getKundeNachname())
                      .isNotEqualTo("Dummy");
                softly.assertThat(resource.getAtomLinks()
                                          .get("self")
                                          .get("href")).endsWith("/" + id);
            });
        }    

        @Test
        public void findByIdInvalid() {
            // Given
            final val id = ID_INVALID;

            // When
            final val response =
                restTemplate.getForEntity(baseUri + "/" + id,
                                          BestellungResource.class);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_NOT_FOUND);
        }

        @Test
        public void findByIdNotFound() {
            // Given
            final val id = ID_NICHT_VORHANDEN;

            // When
            final val response = restTemplate.getForEntity(baseUri + "/" + id,
                                                           String.class);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_NOT_FOUND);
        }

        @Test
        public void findAlleBestellungen() {
            // Given

            // When
            final val bestellungen = restTemplate.getForObject(baseUri,
                                                    BestellungResource[].class);

            // Then
            assertThat(bestellungen).isNotEmpty();
        }
    
        @Test
        public void findByKundeId() throws IOException {
            // Given
            final val kundeId = KUNDE_ID;

            // When
            final val bestellungen =
                restTemplate.getForObject(baseUri + "?kundeId=" + kundeId,
                                          BestellungResource[].class);

            // Then
            assertThat(bestellungen).isNotEmpty();
            log.debug("Anzahl Bestellungen = {}", bestellungen.length);
            Stream.of(bestellungen)
                  .forEach(b -> assertThat(b.getKundeId())
                                .isEqualToIgnoringCase(kundeId));
        }

    // -------------------------------------------------------------------------
    // S C H R E I B E N
    // -------------------------------------------------------------------------
        @Test
        public void save() {
            // Given
            final val kundeId = KUNDE_ID;
            final val artikelId = ARTIKEL_ID;
            final val bestellposition = Bestellposition.builder()
                                                       .artikelId(artikelId)
                                                       .anzahl(1)
                                                       .einzelpreis(TEN)
                                                       .build();
            final val bestellpositionen = singletonList(bestellposition);
            final val neueBestellung = Bestellung.builder()
                                       .kundeId(kundeId)
                                       .bestellpositionen(bestellpositionen)
                                       .build();

            // When
            final val response =
                restTemplate.postForEntity(baseUri, neueBestellung, Void.class);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(HTTP_CREATED);
            final val location = response.getHeaders().getLocation();
            assertThat(location).isNotNull();
            final val locationStr = location.toString();
            final val indexLastSlash = locationStr.lastIndexOf('/');
            final val idStr = locationStr.substring(indexLastSlash + 1);
            assertThat(isValid(idStr)).isTrue();
        }
}
