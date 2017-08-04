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
package de.hska.bestellung.service;

import de.hska.bestellung.config.DbConfig;
import de.hska.bestellung.entity.Bestellung;

import lombok.val;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static de.hska.bestellung.config.Settings.DEV_PROFILE;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    DbConfig.class,
    RepositoryPopulatorConfig.class,
    JacksonAutoConfiguration.class
    // EmbeddedMongoAutoConfiguration braucht noch das Maven-Artifakt
    // "de.flapdoodle.embed:de.flapdoodle.embed.mongo" und ausserdem kann man
    // die Version von MongoDB nicht konfigurieren
})
@ActiveProfiles(DEV_PROFILE)
public class BestellungRepositoryTest {
    private static final ObjectId ID_VORHANDEN =
        new ObjectId("100000000000000000000001");
    private static final ObjectId ID_NICHT_VORHANDEN =
        new ObjectId("999999999999999999999999");
    private static final String KUNDE_ID_VORHANDEN = "000000000000000000000001";
    
    @Autowired
    private BestellungRepository repo;
    
    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
        @Test
        public void findAll() {
            // Given

            // When
            final val bestellungen = repo.findAllBy();

            // Then
            assertThat(bestellungen).isNotEmpty();
        }

        @Test
        public void findById() {
            // Given
            final val id = ID_VORHANDEN;

            // When
            final val bestellung = repo.findById(id);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(bestellung).isPresent();
                softly.assertThat(bestellung.get().getId()).isEqualTo(id);
            });
        }

        @Test
        public void findByIdNotFound() {
            // Given
            @SuppressWarnings("UnnecessaryLocalVariable")
            final val id = ID_NICHT_VORHANDEN;

            // When
            final val bestellung = repo.findById(id);

            // Then
            assertThat(bestellung).isNotPresent();
        }
    
    // -------------------------------------------------------------------------
    // S C H R E I B E N
    // -------------------------------------------------------------------------
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        @Test
        public void save() {
            // Given
            final val kundeId = KUNDE_ID_VORHANDEN;
            final val datum = now();
            final val bestellung = Bestellung.builder()
                                             .datum(datum)
                                             .kundeId(kundeId)
                                             .build();

            // When
            final val result = repo.save(bestellung);

            // Then
            final val id = result.getId();
            assertThat(id).isNotNull();
            final val tmp = repo.findById(id);
            assertSoftly(softly -> {
                softly.assertThat(tmp).isPresent();
                softly.assertThat(tmp.get().getDatum()).isNotNull();
                softly.assertThat(tmp.get().getKundeId()).isEqualTo(kundeId);
            });
        }
}
