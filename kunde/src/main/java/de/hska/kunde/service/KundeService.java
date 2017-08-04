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

import lombok.val;
import com.querydsl.core.types.Predicate;
import de.hska.kunde.config.security.AuthService;
import de.hska.kunde.entity.Kunde;
import de.hska.kunde.entity.QKunde;
import de.hska.kunde.service.KundeRepository.EmailProjection;
import de.hska.kunde.service.KundeRepository.NachnameProjection;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import static de.hska.kunde.util.Strings.isBlank;
import static de.hska.kunde.util.Strings.isNotBlank;
import static java.lang.Integer.parseInt;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.StreamSupport.stream;

/**
 * Anwendungslogik f&uuml;r Kunden.
 * <img src="../../../../../images/KundeService.png" alt="Klassendiagramm">
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Service
@CacheConfig(cacheNames = "kunde_id")
public class KundeService {
    private final KundeRepository repo;
    private final GridFsTemplate gridFsTemplate;
    private final ApplicationEventPublisher publisher;
    private final AuthService authService;
    private final Logger log;

    // Nicht private, damit Spring Aspects davon eine Klasse ableiten kann
    KundeService(KundeRepository repo,
                 @Lazy ApplicationEventPublisher publisher,
                 @Lazy AuthService authService,
                 @Lazy GridFsTemplate gridFsTemplate,
                 Logger log) {
        this.repo = repo;
        this.publisher = publisher;
        this.authService = authService;
        this.gridFsTemplate = gridFsTemplate;
        this.log = log;
    }
    
    /**
     * Alle Kunden ermitteln
     * @return Alle Kunden
     */
    public Stream<Kunde> findAll() {
        return repo.findAllBy();
    }

    /**
     * Einen Kunden anhand seiner ID suchen. Falls der DB-Server nicht
     * erreichbar ist, gibt es eine Fallback-Methode.
     * @param id Die Id des gesuchten Kunden
     * @return Der gefundene Kunde oder ein leeres Optional-Objekt
     */
    @Cacheable(key = "#id")
    public Optional<Kunde> findById(ObjectId id) {
        return repo.findById(id);
    }

    /**
     * Kunden anhand eines Suchkriteriums ermitteln
     * @param email Emailadresse
     * @param nachname Nachname ohne Unterscheidung zwischen Gro&szlig;- und
     *                 Kleinschreibung
     * @param plz Postleitzahl
     * @return Gefundene Kunden
     */
    @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
    public Stream<Kunde> find(String email, String nachname, String plz) {
        // Die Email ist eindeutig
        if (isNotBlank(email)) {
            final val kunde = repo.findByEmail(email);
            // FIXME Java 9 return kunde.stream()
            if (!kunde.isPresent()) {
                return Stream.empty();
            }
            return Stream.of(kunde.get());
        }

        if (isBlank(nachname) && isBlank(plz)) {
            return repo.findAllBy();
        }

        if (isBlank(plz)) {
            return repo.findByNachnameContainingIgnoreCase(nachname);
        }

        if (isBlank(nachname)) {
            return repo.findByAdresse_Plz(plz);
        }

        final val qkunde = QKunde.kunde;
        final val pred = qkunde.nachname.containsIgnoreCase(nachname)
                               .and(qkunde.adresse.plz.eq(plz));
        final val kunden = repo.findAll(pred);
        // http://stackoverflow.com/questions/23932061/...
        // ...convert-iterable-to-stream-using-java-8-jdk
        return stream(kunden.spliterator(), false);
    }

    /**
     * Kunden anhand von Suchkriterien ermitteln
     * @param predicate Pr&auml;dikat f&uuml;r QueryDsl
     * @return Gefundene Kunden
     */
    public Stream<Kunde> find(Predicate predicate) {
        final val kunden = repo.findAll(predicate);
        // http://stackoverflow.com/questions/23932061/...
        // ...convert-iterable-to-stream-using-java-8-jdk
        return stream(kunden.spliterator(), false);
    }

    /**
     * Multimediale Datei (Bild oder Video) zu einem Kunden mit gegebener ID
     * ermitteln
     * @param id Kunde-ID
     * @return Multimediale Datei, falls sie existiert. Sonst empty().
     */
    public Optional<GridFsResource> findMedia(ObjectId id) {
        final val kunde = repo.findById(id);
        if (!kunde.isPresent()) {
            return empty();
        }

        final val filename = kunde.get().getId().toString();
        final val gridFsResource = gridFsTemplate.getResource(filename);
        return ofNullable(gridFsResource);
    }

    /**
     * Nachnamen anhand eines Pr&auml;fix ermitteln
     * @param prefix Pr&auml;fix f&uuml;r Nachnamen
     * @return Gefundene Nachnamen
     */
    public Stream<NachnameProjection> findNachnamenByPrefix(String prefix) {
        return repo.findByNachnameStartingWithIgnoreCase(prefix);
    }

    /**
     * Emailadressen anhand eines Pr&auml;fix ermitteln
     * @param prefix Pr&auml;fix f&uuml;r Email
     * @return Gefundene Emailadressen
     */
    public Stream<EmailProjection> findEmailsByPrefix(String prefix) {
        return repo.findByEmailStartingWithIgnoreCase(prefix);
    }

    /**
     * Einen neuen Kunden anlegen
     * @param kunde Das Objekt des neu anzulegenden Kunden
     * @return Der neu angelegte Kunde mit generierter ID
     */
    public Kunde save(Kunde kunde) {
        requireNonNull(kunde);
        final val account = kunde.getAccount();
        if (account == null) {
            throw new InvalidAccountException();
        }

        // Email in Kleinbuchstaben verwalten
        kunde.setEmail(kunde.getEmail().toLowerCase(Locale.getDefault()));

        repo.findByEmail(kunde.getEmail()).ifPresent(k -> {
            throw new EmailExistsException();
        });

        account.setRollen(singletonList("ROLE_KUNDE"));
        authService.save(account);

        kunde.setUsername(account.getUsername());
        final val neuerKunde = repo.save(kunde);
        log.trace("Neuer Kunde: {}", neuerKunde);
        log.trace("Neue Adresse: {}", neuerKunde::getAdresse);

        publisher.publishEvent(new NeuerKundeEvent(neuerKunde));
        return neuerKunde;
    }

    /**
     * Multimediale Daten aus einem Inputstream werden persistent zur gegebenen
     * Kunden-ID abgespeichert. Der Inputstream wird am Ende geschlossen.
     * @param id Kunde-ID
     * @param inputStream Inputstream mit multimedialen Daten.
     * @param contentType MIME-Type, z.B. image/png
     * @return ID der neuangelegten multimedialen Datei
     */
    public Optional<Object> save(InputStream inputStream, ObjectId id,
                                 String contentType) {
        requireNonNull(id);
        // Nur zu einem existierenden Kunden werden multimediale Daten abgelegt
        final val kunde = repo.findById(id);
        if (!kunde.isPresent()) {
            return empty();
        }

        final val filename = id.toString();

        // ggf. multimediale Datei loeschen
        final val criteria = Criteria.where("filename").is(filename);
        final val query = new Query(criteria);
        gridFsTemplate.delete(query);

        // store() schliesst auch den Inputstream
        // FIXME Spring Data MongoDB 2: ObjectId als Rueckgabetyp
        // Abhilfe: final val gridFSFile = gridFsTemplate.findOne(mediaId)
        final val gridFSFile =
            gridFsTemplate.store(inputStream, filename, contentType);
        gridFSFile.validate();
        log.debug("Anzahl Bytes: {}", gridFSFile::getLength);
        return Optional.of(gridFSFile.getId());
    }

    /**
     * Einen vorhandenen Kunden aktualisieren
     * @param kunde Das Objekt mit den neuen Daten
     * @param versionStr Versionsnummer
     * @return Der aktualisierte Kunde oder ein leeres Optional-Objekt, falls
     *         es keinen Kunden mit der angegebenen ID gibt
     */
    @CachePut(key = "#kunde.id")
    @SuppressWarnings("squid:S2159")
    public Optional<Kunde> update(Kunde kunde, String versionStr) {
        requireNonNull(kunde);
        final val id = kunde.getId();
        final val kundeDb = repo.findById(id);
        if (!kundeDb.isPresent()) {
            return empty();
        }

        final int version;
        try {
            version = parseInt(versionStr);
        } catch (NumberFormatException e) {
            log.debug("Ungueltige Version: {}", versionStr);
            throw new ConcurrentUpdatedException(e);
        }

        final val versionDb = kundeDb.get().getVersion();
        if (version < versionDb) {
            throw new ConcurrentUpdatedException();
        }

        // Wurde die Email auf eine bereits existierende Email geaendert?
        final val neueEmail = kunde.getEmail();
        repo.findByEmail(neueEmail)
            .filter(k -> !k.getId().equals(id))
            .ifPresent(k -> {
            throw new EmailExistsException();
        });

        final val aktualisierterKunde = repo.save(kunde);
        log.trace("Aktualisierter Kunde: {}", aktualisierterKunde);
        return Optional.of(aktualisierterKunde);
    }

    /**
     * Einen vorhandenen Kunden l&ouml;schen
     * @param id Die ID des zu l&ouml;schenden Kunden
     * @param authentication Das Objekt mit den Authentifizierungs-Informationen
     *                       (aus dem Controller)
     * @return true falls es zur ID ein Kundenobjekt gab, das gel&ouml;scht
     *         wurde; false sonst
     */
    // erfordert zusaetzliche Konfiguration in SecurityConfig
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CacheEvict(key = "#id")
    public boolean delete(ObjectId id, Authentication authentication) {
        // Alternative zu DI fuer Authentication:
        // SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        
        if (authentication != null) {
            log.debug("Rollen: {}", authentication::getAuthorities);
            log.debug("Principal: {}", authentication::getPrincipal);
        }

        // EmptyResultDataAccessException bei delete(), falls es zur gegebenen
        // ID kein Objekt gibt
        // http://docs.spring.io/spring/docs/current/javadoc-api/org/...
        // ...springframework/dao/EmptyResultDataAccessException.html
        if (!repo.exists(id)) {
            return false;
        }

        repo.delete(id);
        return true;
    }

    /**
     * Einen vorhandenen Kunden l&ouml;schen
     * @param email Die Email des zu l&ouml;schenden Kunden
     * @param authentication Das Objekt mit den Authentifizierungs-Informationen
     *                       (aus dem Controller)
     * @return true falls es zur Email ein Kundenobjekt gab, das gel&ouml;scht
     *         wurde; false sonst
     */
    // erfordert zusaetzliche Konfiguration in SecurityConfig
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public boolean delete(String email, Authentication authentication) {
        // Alternative zu DI fuer Authentication:
        // SecurityContextHolder.getContext().getAuthentication().getPrincipal()

        if (authentication != null) {
            log.debug("Rollen: {}", authentication::getAuthorities);
            log.debug("Principal: {}", authentication::getPrincipal);
        }

        final boolean[] result = {false};
        repo.findByEmail(email).ifPresent(kunde -> {
            repo.delete(kunde);
            result[0] = true;
        });
        return result[0];
    }
}
