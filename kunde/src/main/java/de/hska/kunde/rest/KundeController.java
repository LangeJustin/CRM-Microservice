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

import de.hska.kunde.service.KundeRepository.EmailProjection;
import de.hska.kunde.service.KundeRepository.NachnameProjection;
import lombok.val;
import com.querydsl.core.types.Predicate;
import de.hska.kunde.entity.InteresseType;
import de.hska.kunde.entity.Kunde;
import de.hska.kunde.service.KundeService;
import de.hska.kunde.util.PatchOperation;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static de.hska.kunde.util.Strings.isBlank;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.http.HttpHeaders.IF_MATCH;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.servlet.support
              .ServletUriComponentsBuilder.fromCurrentRequestUri;

// import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn

/**
 * Controller-Objekt gem&auml;&szlig; SpringMVC f&uuml;r Requests zu /kunde.
 * <img src="../../../../../images/KundeController.png" alt="Klassendiagramm">
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@RestController
@RequestMapping("/kunde")
@SuppressWarnings({
    "PMD.UnusedPrivateMethod",
    "squid:UnusedPrivateMethod",
    "squid:S2159",
    "unused",
    "WeakerAccess"
})
class KundeController {
    // Die ObjectID von MongoDB ist eine 24-stellige HEX-Zahl bzw. 12-Byte-Zahl
    private static final String OBJECT_ID_PATTERN = "[0-9a-fA-F]{24}";
    private static final String ID_URI_TEMPLATE = "/{id:" + OBJECT_ID_PATTERN
                                                  + '}';
    private static final String NEWLINE = System.getProperty("line.separator");

    private static final Method FIND;
    private static final Method SAVE;
    private static final Method UPDATE;
    private static final Method DELETE_METHOD;
    static {
        try {
            FIND = KundeController.class.getDeclaredMethod("find",
                                                           Predicate.class);
            SAVE = KundeController.class.getDeclaredMethod("save", Kunde.class);
            UPDATE = KundeController.class.getDeclaredMethod("update",
                                                             ObjectId.class,
                                                             Collection.class,
                                                             String.class);
            DELETE_METHOD = KundeController.class.getDeclaredMethod("delete",
                                          ObjectId.class, Authentication.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private final KundeService service;
    private final KundeResourceAssembler assembler;
    private final Validator validator;
    private final Logger log;

    // Constructor Injection
    KundeController(KundeService service,
                    KundeResourceAssembler assembler,
                    Validator validator,
                    Logger log) {
        this.service = service;
        this.assembler = assembler;
        this.validator = validator;
        this.log = log;
    }

    /**
     * REST-Schnittstelle, um einen Kunden anhand seiner ID zu suchen
     * Statuscodes:
     * <ul>
     * <li>200 falls es einen Kunden zur gesuchten ID gibt
     * <li>404 falls es keinen Kunden zur gesuchten ID gibt.
     * </ul>
     * @param id ID zum gesuchten Kunden.
     * @param versionHeader If-none-match im Request-Header
     * @return Gefundener Kunde mit Atom-Links
     */
    @GetMapping(ID_URI_TEMPLATE)
    // Eine der beiden Rollen ist erforderlich
    // @Secured({"ROLE_ADMIN", "ROLE_KUNDE"})
    HttpEntity<KundeResource> findById(@PathVariable ObjectId id,
                                       @RequestHeader(value = IF_NONE_MATCH)
                                       @SuppressWarnings(
                                           "OptionalUsedAsFieldOrParameterType")
                                       Optional<String> versionHeader) {
        final val kunde = service.findById(id)
                                 .orElseThrow(NotFoundException::new);

        // Versionsnr bei If-None-Match ueberpruefen, ggf. Statuscode 304
        // http://docs.spring.io/spring-data/rest/docs/current/reference/html...
        // .../#headers.better-client-architecture
        final val version = kunde.getVersion().toString();
        final val tmpVersion = versionHeader.filter(v -> v.equals(version));
        if (tmpVersion.isPresent()) {
            return status(NOT_MODIFIED).build();
        }

        // Ressource mit Atom-Links
        final val resource = assembler.toResource(kunde);
        final val listLink = linkTo(KundeController.class, FIND)
                             .withRel("list");
        final val addLink = linkTo(KundeController.class, SAVE)
                            .withRel("add");
        final val updateLink =
            linkTo(KundeController.class, UPDATE, id, null).withRel("update");
        final val removeLink =
            linkTo(KundeController.class, DELETE_METHOD, id, null)
            .withRel("remove");
        resource.add(listLink, addLink, updateLink, removeLink);

        // Entity Tag: Aenderungen an der angeforderten Ressource erkennen
        // Client: Spaetere GET-Requests mit Header Feld "If-None-Match"
        //         ggf. Response mit Statuscode NOT MODIFIED (s.o.)
        return ok().eTag("\"" + version + "\"").body(resource);
    }

    /**
     * Kunden anhand von Suchkriterien ermitteln
     * @param predicate Pr&auml;dikat f&uuml;r QueryDsl
     * @return Gefundene Kunden
     */
    @GetMapping
    List<KundeResource> find(@QuerydslPredicate(root = Kunde.class)
                             Predicate predicate) {
        final val kunden = service.find(predicate);
        return assembler.toResources(kunden);
    }

    /**
     * Nachnamen (ohne Duplikate) anhand eines Pr&auml;fix ermitteln
     * @param prefix Pr&auml;fix f&uuml;r Nachnamen
     * @return Gefundene Nachnamen
     */
    @GetMapping("/prefix/nachname/{prefix}")
    List<String> findNachnamen(@PathVariable String prefix) {
        final val nachnamen = new ArrayList<String>();
        service.findNachnamenByPrefix(prefix)
               .map(NachnameProjection::getNachname)
               .filter(n -> !nachnamen.contains(n))
               .forEach(nachnamen::add);
        return nachnamen;
    }

    /**
     * Nachnamen anhand eines Pr&auml;fix ermitteln
     * @param prefix Pr&auml;fix f&uuml;r Nachnamen
     * @return Gefundene Nachnamen
     */
    @GetMapping("/prefix/email/{prefix}")
    List<String> findEmails(@PathVariable String prefix) {
        return service.findEmailsByPrefix(prefix)
                      .map(EmailProjection::getEmail)
                      .collect(toList());
    }

    /**
     * REST-Schnittstelle, um multimediale Daten zu einem Kunden mit gegebener
     * Kunde-ID herunterzuladen
     * @param id Kunde-ID
     * @return Das Medium als Bytestrom
     */
    @GetMapping(ID_URI_TEMPLATE + "/media")
    HttpEntity<InputStreamResource> download(@PathVariable ObjectId id)
                                    throws IOException {
        final val gridFsResource = service.findMedia(id)
                                          .orElseThrow(NotFoundException::new);
        final val length = gridFsResource.contentLength();
        final val mediaType = parseMediaType(gridFsResource.getContentType());
        return ok().contentLength(length)
                   .contentType(mediaType)
                   .body(gridFsResource);
    }

    /**
     * REST-Schnittstelle, um einen neuen Kunden mittels HTTP POST neu anzulegen
     * Statuscodes:
     * <ul>
     * <li>201 falls der neue Kunde erfolgreich angelegt wurde
     * <li>400 falls die Daten des neu anzulegenden Kunden fehlerhaft sind
     * </ul>
     * @param kunde der neu anzulegende und validierte Kunde
     * @return Location-Header innerhalb des HTTP-Response enth&auml;lt die
     *         URI, mit der der neue Kunde gelesen werden kann.
     */
    @PostMapping
    HttpEntity<Void> save(@RequestBody @Valid Kunde kunde) {
        final val kundeSaved = service.save(kunde);

        final val uri = fromCurrentRequestUri()
                        .path("/{id}")
                        .buildAndExpand(kundeSaved.getId())
                        .toUri();
        return created(uri).build();
    }

    /**
     * REST-Schnittstelle, um einen Kunden durch HTTP PUT zu aktualisieren.
     * Statuscodes:
     * <ul>
     * <li>204 bei erfolgreicher Aktualisierung
     * <li>400 bei fehlerhaften Aktualisierungsdaten
     * <li>404 falls es keinen Kunden zur gegebenen ID gibt.
     * </ul>
     * @param kunde zu aktualisierender Kunde
     * @param version If-match im Request-Header
     * @return Response mit ETag
     */
    @PutMapping
    HttpEntity<Void> update(@RequestBody Kunde kunde,
                            @RequestHeader(value = IF_MATCH) String version) {
        final val vorhandenerKunde =
            service.findById(kunde.getId()).orElseThrow(NotFoundException::new);

        vorhandenerKunde.setNachname(kunde.getNachname());
        vorhandenerKunde.setEmail(kunde.getEmail());
        vorhandenerKunde.setNewsletter(kunde.isNewsletter());
        vorhandenerKunde.setGeburtsdatum(kunde.getGeburtsdatum());
        vorhandenerKunde.setUmsatz(kunde.getUmsatz());
        vorhandenerKunde.setHomepage(kunde.getHomepage());
        vorhandenerKunde.setGeschlecht(kunde.getGeschlecht());
        vorhandenerKunde.setInteressen(kunde.getInteressen());
        vorhandenerKunde.setAdresse(kunde.getAdresse());
        vorhandenerKunde.setUsername(kunde.getUsername());

        final val aktualisierterKunde =
            service.update(vorhandenerKunde, version)
                   .orElseThrow(NotFoundException::new);

        // Entity Tag: Aenderungen an der angeforderten Ressource erkennen
        // Client: Spaetere GET-Requests mit Header Feld "If-None-Match"
        //         ggf. Response mit Statuscode NOT MODIFIED (s.o.)
        return noContent().eTag("\"" + aktualisierterKunde.getVersion() + "\"")
                          .build();
    }

    /**
     * REST-Schnittstelle, um einen Kunden mit gegebener ID durch HTTP PATCH zu
     * aktualisieren. Statuscodes:
     * <ul>
     * <li>204 bei erfolgreicher Aktualisierung
     * <li>400 bei fehlerhaften Aktualisierungsdaten
     * <li>404 falls es keinen Kunden zur gegebenen ID gibt.
     * </ul>
     * @param id ID des zu aktualisierenden Kunden
     * @param operations PATCH-Operationen
     * @param version If-match im Request-Header
     * @return Response mit ETag
     */
    // http://williamdurand.fr/2014/02/14/please-do-not-patch-like-an-idiot
    // https://github.com/spring-projects/spring-hateoas/issues/471
    @PatchMapping(path = ID_URI_TEMPLATE,
                  consumes = "application/eccccccccccccccj-patch+json")
    HttpEntity<Object> update(@PathVariable ObjectId id,
                             @RequestBody Collection<PatchOperation> operations,
                             @RequestHeader(value = IF_MATCH) String version) {
        if (isBlank(version)) {
            return status(PRECONDITION_FAILED).body(null);
        }

        final val kunde = service.findById(id)
                                 .orElseThrow(NotFoundException::new);

        final val replaceOps = operations
                               .stream()
                               .filter(value -> "replace".equals(value.getOp()))
                               .collect(toList());
        replaceOps(kunde, replaceOps);

        final val addOps = operations.stream()
                                   .filter(value -> "add".equals(value.getOp()))
                                   .collect(toList());
        addOps(kunde, addOps);

        final val removeOps = operations
                              .stream()
                              .filter(value -> "remove".equals(value.getOp()))
                              .collect(toList());
        removeOps(kunde, removeOps);

        final val aktualisierterKunde =
            service.update(kunde, version).orElseThrow(NotFoundException::new);

        // Entity Tag: Aenderungen an der angeforderten Ressource erkennen
        // Client: Spaetere GET-Requests mit Header Feld "If-None-Match"
        //         ggf. Response mit Statuscode NOT MODIFIED (s.o.)
        return noContent().eTag("\"" + aktualisierterKunde.getVersion() + "\"")
                          .build();
    }

    private void replaceOps(Kunde kunde,
                            Collection<PatchOperation> replaceOps) {
        log.trace("replace: [{}]", replaceOps);
        if (replaceOps.isEmpty()) {
            return;
        }

        final val violations =
            new HashSet<ConstraintViolation<KundeResource>>();
        replaceOps.forEach(op -> replaceOp(op, kunde, violations));
        if (violations.isEmpty()) {
            return;
        }

        final val violationMsg = violations.stream()
                                           .map(ConstraintViolation::getMessage)
                                           .collect(toList());
        throw new BadRequestException(join(NEWLINE, violationMsg));
    }

    private void replaceOp(PatchOperation op, Kunde kunde,
                    Collection<ConstraintViolation<KundeResource>> violations) {
        final val path = op.getPath();
        if ("/nachname".equals(path)) {
            final val nachname = op.getValue();
            violations.addAll(validator.validateValue(KundeResource.class,
                    "nachname",
                    nachname));
            if (violations.isEmpty()) {
                kunde.setNachname(nachname);
            }
        }
        if ("/email".equals(path)) {
            final val email = op.getValue();
            violations.addAll(validator.validateValue(KundeResource.class,
                    "email",
                    email));
            if (violations.isEmpty()) {
                kunde.setEmail(email);
            }
        }
    }

    private void addOps(Kunde kunde, Collection<PatchOperation> addOps) {
        log.trace("add: [{}]", addOps);
        if (addOps.isEmpty()) {
            return;
        }

        final val fehlermeldungen = new ArrayList<String>();
        addOps.stream()
            .filter(op -> "/interessen".equals(op.getPath()))
            .forEach(op -> addOp(op, kunde, fehlermeldungen));
        if (fehlermeldungen.isEmpty()) {
            return;
        }

        throw new BadRequestException(join(NEWLINE, fehlermeldungen));
    }

    private void addOp(PatchOperation op, Kunde kunde,
                       Collection<String> fehlermeldungen) {
        final val interesseStr = op.getValue();
        final val interesse = InteresseType.build(interesseStr);
        if (interesse == null) {
            fehlermeldungen.add(interesseStr + " ist kein Interesse");
        } else {
            log.trace("Zusaetzliches Interesse: {}", interesse);
            if (kunde.getInteressen() == null) {
                kunde.setInteressen(new HashSet<>());
            }
            kunde.getInteressen().add(interesse);
        }
    }

    private void removeOps(Kunde kunde, Collection<PatchOperation> removeOps) {
        log.trace("remove: [{}]", removeOps);
        if (removeOps.isEmpty()) {
            return;
        }

        final val fehlermeldungen = new ArrayList<String>();
        removeOps.stream()
                 .filter(op -> "/interessen".equals(op.getPath()))
                 .forEach(op -> removeOp(op, kunde, fehlermeldungen));
        if (fehlermeldungen.isEmpty()) {
            return;
        }

        throw new BadRequestException(join(NEWLINE, fehlermeldungen));
    }

    @SuppressWarnings("squid:S2175")
    private void removeOp(PatchOperation op, Kunde kunde,
                          Collection<String> fehlermeldungen) {
        final val interesseStr = op.getValue();
        final val interesse = InteresseType.build(interesseStr);
        if (interesse == null) {
            fehlermeldungen.add(interesseStr + " ist kein Interesse");
        } else {
            log.trace("Zu loeschendes Interesse: {}", interesse);
            kunde.getInteressen().remove(interesse);
        }
    }

    /**
     * Ein Bild oder Video zu gegebener Kunde-ID hochladen.
     * @param id Kunde-ID
     * @param file injiziertes Objekt f&uuml;r die hochgeladene Datei
     */
    @PutMapping(ID_URI_TEMPLATE)
    void upload(@PathVariable ObjectId id, @RequestParam MultipartFile file)
         throws IOException {
        final val inputStream = file.getInputStream();
        final val contentType = file.getContentType();
        // inputStream wird innerhalb von service.save() geschlossen
        service.save(inputStream, id, contentType)
               .orElseThrow(NotFoundException::new);
    }

    /**
     * REST-Schnittstelle, um einen Kunden mit gegebener ID zu l&ouml;schen
     * @param id Die ID des zu l&ouml;schenden Kunden
     * @param authentication das durch Authentifizierung injizierte Objekt
     */
    @DeleteMapping(ID_URI_TEMPLATE)
    @ResponseStatus(NO_CONTENT)
    void delete(@PathVariable ObjectId id, Authentication authentication) {
        final val result = service.delete(id, authentication);
        if (!result) {
            throw new NotFoundException();
        }
    }

    /**
     * REST-Schnittstelle, um einen Kunden mit gegebener Email zu l&ouml;schen
     * @param email Die Email des zu l&ouml;schenden Kunden
     * @param authentication das durch Authentifizierung injizierte Objekt
     */
    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    void delete(@RequestParam String email, Authentication authentication) {
        final val result = service.delete(email, authentication);
        if (!result) {
            throw new NotFoundException();
        }
    }
}
