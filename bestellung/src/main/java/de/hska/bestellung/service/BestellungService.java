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

import java.util.Optional;
import java.util.stream.Stream;

import lombok.val;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static java.time.LocalDate.now;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Anwendungslogik f&uuml;r Bestellungen.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Service
@CacheConfig(cacheNames = "bestellung_id")
public class BestellungService {
    private final BestellungRepository repo;
    // KundenClient ist ein *INTERFACE* mit Funktionalitaet von OpenFeign
    private final KundeClient kundeClient;
    private final Logger log;

    BestellungService(BestellungRepository repo,
                      KundeClient kundeClient,
                      Logger log) {
        this.repo = repo;
        this.kundeClient = kundeClient;
        this.log = log;
    }
    
    /**
     * Alle Bestellungen ermitteln
     * @return Alle Bestellungen
     */
    public Stream<Bestellung> findAll() {
        return repo.findAllBy();
    }

    /**
     * Eine Bestellung anhand seiner ID suchen
     * @param id Die Id der gesuchten Bestellung
     * @return Die gefundene Bestellung oder ein leeres Optional-Objekt
     */
    @Cacheable(key = "#id")
    public Optional<Bestellung> findById(ObjectId id) {
        final val bestellungOpt = repo.findById(id);
        
        bestellungOpt.ifPresent(bestellung -> {
            final val kundeId = bestellung.getKundeId();
            
            // Mit OpenFeign wird auf einen den Microservice kunden zugegriffen:
            // Falls es den gesuchten Kunden nicht gibt, gibt es durch Hystrix
            // eine Fallback-Loesung
            final val kunde = kundeClient.findById(kundeId);

            log.trace("Kunde mit ID={}: {}", kundeId, kunde);
            bestellung.setKundeNachname(kunde.getNachname());
        });
        
        return bestellungOpt;
    }

    /**
     * Eine Bestellung anhand seiner ID suchen
     * @param kundeId Die Id des gegebenenKunden
     * @return Die gefundene Bestellung oder ein leeres Optional-Objekt
     */
    public Stream<Bestellung> findByKundeId(String kundeId) {
        return repo.findByKundeId(kundeId);
    }
    
    /**
     * Eine neue Bestellung anlegen
     * @param bestellung Das Objekt der neu anzulegenden Bestellung
     * @return Die neu angelegte Bestellung mit generierter ID
     */
    public Optional<Bestellung> save(Bestellung bestellung) {
        requireNonNull(bestellung);
        bestellung.setDatum(now());
        final val kundeId = bestellung.getKundeId();
        final val kunde = kundeClient.findById(kundeId);
        if (kunde == null) {
            // Der Server "kunden" war nicht erreichbar
            return empty();
        }
        
        return of(repo.save(bestellung));
    }
}
