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

import de.hska.bestellung.entity.Bestellung;
import de.hska.bestellung.entity.Kunde;
import java.util.stream.Stream;
import javax.mail.MessagingException;

import lombok.val;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static java.time.LocalDate.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.bson.types.ObjectId.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.ReflectionUtils.setField;

public class BestellungServiceTest {
    private static final String KUNDE_NAME = "Test";
    private static final String KUNDE_EMAIL = "mail@test.de";
    
    @Mock
    private BestellungRepository repo;
    
    @Mock
    private KundeClient kundeClient;
    
    @Mock
    @SuppressWarnings("PMD.UnusedPrivateField")
    private Logger log;

    @InjectMocks
    private BestellungService service;
    
    @Before
    public void beforeEach() {
        initMocks(this);
    }

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
        @Test
        public void findById() {
            // Given
            final val id = get();
            final val bestellungMock = createBestellungMock(id);
            given(repo.findById(id)).willReturn(of(bestellungMock));
            final val kundeMock = createKundeMock();
            given(kundeClient.findById(anyString())).willReturn(kundeMock);
            doNothing().when(log).trace(anyString(), any(), any());

            // When
            final val bestellung = service.findById(id);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(bestellung).isPresent();
                //noinspection ConstantConditions
                softly.assertThat(bestellung.get().getId()).isEqualTo(id);
            });
        }

        @Test
        public void findByIdNotFound() {
            // Given
            final val id = get();
            given(repo.findById(id)).willReturn(empty());

            // When
            final val bestellung = service.findById(id);

            // Then
            assertThat(bestellung).isNotPresent();
        }    

        @Test
        public void findAll() {
            // Given
            final val id = get();
            final val bestellungMock = createBestellungMock(id);
            final val bestellungenMock = Stream.of(bestellungMock);
            given(repo.findAllBy()).willReturn(bestellungenMock);

            // When
            final val bestellungen = service.findAll();

            // Then
            assertThat(bestellungen).isNotEmpty();
        }    
    
    // -------------------------------------------------------------------------
    // S C H R E I B E N
    // -------------------------------------------------------------------------
        @Test
        public void save() throws MessagingException {
            // Given
            final val kundeMock = createKundeMock();
            given(kundeClient.findById(anyString())).willReturn(kundeMock);
            final val bestellungMock = createBestellungMock(null);
            final val bestellungMockResult = createBestellungMock(get());
            given(repo.save(bestellungMock)).willReturn(bestellungMockResult);

            // When
            final val result = service.save(bestellungMock);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(result).isPresent();
                //noinspection ConstantConditions
                softly.assertThat(result.get().getId())
                      .isEqualTo(bestellungMockResult.getId());
            });
        }
    
    // -------------------------------------------------------------------------
    // Hilfsmethoden fuer Mocking
    // -------------------------------------------------------------------------
    private Kunde createKundeMock() {
        return Kunde.builder().nachname(KUNDE_NAME).email(KUNDE_EMAIL).build();
    }
    
    private Bestellung createBestellungMock(ObjectId id) {
        final val bestellung = Bestellung.builder()
                                         .datum(now())
                                         .kundeId(get().toString())
                                         .build();
        setId(bestellung, id);
        return bestellung;
    }
    
    private void setId(Object obj, ObjectId id) {
        // Das private Attribut "id" ohne set-Methode setzen
        final val idField = findField(obj.getClass(), "id");
        makeAccessible(idField);
        setField(idField, obj, id);
    }
}
