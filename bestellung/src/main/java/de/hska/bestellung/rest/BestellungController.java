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

import de.hska.bestellung.entity.Bestellung;
import de.hska.bestellung.service.BestellungService;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import lombok.val;
import org.bson.types.ObjectId;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static de.hska.bestellung.util.Strings.isBlank;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.bind.annotation
              .RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.servlet.support
              .ServletUriComponentsBuilder.fromCurrentRequest;

/**
 * Controller-Objekt gem&auml;&szlig; SpringMVC f&uuml;r Requests zu
 * /bestellung.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@RestController
@RequestMapping("/")

// CORS fuer einen Webserver mit Port 443
@CrossOrigin(origins = "https://localhost",
             methods = {OPTIONS, GET, POST, PATCH, DELETE})
class BestellungController {
    // Die ObjectID von MongoDB ist eine 24-stellige HEX-Zahl bzw. 12-Byte-Zahl
    private static final String OBJECT_ID_PATTERN = "[0-9a-fA-F]{24}";
    
    private static final Method SAVE;
    static {
        try {
            SAVE = BestellungController.class.getMethod("save",
                                                        Bestellung.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private final BestellungService service;
    private final BestellungResourceAssembler assembler;
    
    BestellungController(BestellungService service,
                         BestellungResourceAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }
    
    /**
     * REST-Schnittstelle, um eine Bestellung anhand ihrer ID zu suchen
     * Statuscodes:
     * <ul>
     * <li>200 falls es eine Bestellung zur gesuchten ID gibt
     * <li>404 falls es keine Bestellung zur gesuchten ID gibt.
     * </ul>
     * @param id ID zur gesuchten Bestellung.
     * @return Gefundene Bestellung mit Atom-Links
     */
    @GetMapping("/{id:" + OBJECT_ID_PATTERN + '}')
    public ResponseEntity<BestellungResource> findById(
                         @PathVariable
                         ObjectId id,
                         @RequestHeader(value = IF_NONE_MATCH)
                         @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                         Optional<String> versionHeader) {
        final val bestellung = service.findById(id)
                                      .orElseThrow(NotFoundException::new);
        
        // Versionsnr bei If-None-Match ueberpruefen, ggf. Statuscode 304
        // http://docs.spring.io/spring-data/rest/docs/current/reference/html...
        // .../#headers.better-client-architecture
        final val version = bestellung.getVersion().toString();
        @SuppressWarnings("squid:S2159")
        final val tmpVersion = versionHeader.filter(v -> v.equals(version));
        if (tmpVersion.isPresent()) {
            return status(NOT_MODIFIED).build();
        }
        
        final val resource = assembler.toResource(bestellung);
        
        final val addLink =
            linkTo(BestellungController.class, SAVE).withRel("add");
        resource.add(addLink);

        // Entity Tag: Aenderungen an der angeforderten Ressource erkennen
        // Client: Spaetere GET-Requests mit Header Feld "If-None-Match"
        //         ggf. Response mit Statuscode NOT MODIFIED (s.o.)
        return ok().eTag("\"" + version + "\"").body(resource);
    }

    /**
     * REST-Schnittstelle, um Bestellungen zu suchen.
     * Statuscodes:
     * <ul>
     * <li>200 falls es mindestens eine Bestellung gibt
     * <li>404 falls es keine Bestellung gibt.
     * </ul>
     * @param kundeId ID eines gegebenen Kunden (oder null)
     * @return Die gefundenen Bestellungen.
     */
    @GetMapping
    public List<BestellungResource> findBy(@RequestParam(required = false)
                                           @Pattern(regexp = OBJECT_ID_PATTERN)
                                           String kundeId) {
        final val bestellungen = isBlank(kundeId)
                                 ? service.findAll()
                                 : service.findByKundeId(kundeId);

        return assembler.toResources(bestellungen);
    }
    
    /**
     * REST-Schnittstelle, um eine neue Bestellung mittels HTTP POST anzulegen
     * @param bestellung die neu anzulegende Bestellung
     * @return Location-Header innerhalb des HTTP-Response enth&auml;lt die
     *         URI, mit der die neue Bestellung gelesen werden kann.
     */
    @PostMapping
    public HttpEntity<Void> save(@Valid @RequestBody Bestellung bestellung) {
        final val bestellungSaved = service.save(bestellung)
                                           .orElseThrow(NotFoundException::new);

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        final val uri = fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(bestellungSaved.getId())
                        .toUri();
        return created(uri).build();
    }
}
