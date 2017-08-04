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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.val;
import org.bson.types.ObjectId;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

import static java.util.stream.Collectors.toList;

// ResourceAssemblerSupport aus Spring HATEOAS

/**
/**
 * Basisklasse f&uum;r Atom-Links mit Spring HATEOAS
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 * @param <E> Typ einer Entity-Klasse
 * @param <R> Typ einer Ressourceklasse
 */
abstract class AbstractResourceAssembler<E, R extends ResourceSupport>
               extends ResourceAssemblerSupport<E, R> {
    /**
     * Basis-Konstruktor f&uuml;r spezifische "ResourceAssembler"
     * gem&auml;&szig; Spring HATEOAS
     * @param controllerClass die zugeh&ouml;hrige Controller-Klasse
     * @param resourceType die Resource-Klasse der zu erzeugenden Objekte
     */
    AbstractResourceAssembler(Class<?> controllerClass, Class<R> resourceType) {
        super(controllerClass, resourceType);
    }
    
    /**
     * Konvertierung eines (gefundenen) Objektes in eine Resource
     * gem&auml;&szlig; Spring MVC oder Ausl&ouml;sen eines Response mit
     * Statuscode 404.
     * @param entity Gefundenes Objekt oder null
     * @return null bzw. HATEOAS-Resource in den abgeleiteten Klassen
     */
    @Override
    public R toResource(E entity) {
        if (entity == null) {
            throw new NotFoundException();
        }
        // Resource-Objekt zzgl. Self-Link mit der id erzeugen
        if (entity instanceof Identifiable) {
            @SuppressWarnings("unchecked")
            final Identifiable<ObjectId> identifiable = (Identifiable) entity;
            return createResourceWithId(identifiable.getId(), entity);
        }
        return instantiateResource(entity);
    }

    /**
     * Konvertierung eines (gefundenen) Objektes innerhalb von einem
     * Optional-Objekt in eine Resource gem&auml;&szlig; Spring MVC oder
     * Ausl&ouml;sen eines Response mit Statuscode 404.
     * @param opt Optional-Objekt
     * @return HATEOAS-Resource
     */
    R toResource(@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                 Optional<E> opt) {
        final val obj = opt.orElseThrow(NotFoundException::new);
        return toResource(obj);
    }

    /**
     * Konvertierung von einem Stream mit (gefundenen) Objekten in eine Liste
     * mit Resources gem&auml;&szlig; Spring MVC oder Ausl&ouml;sen eines
     * Response mit Statuscode 404. Der Stream wird dabei <b>geschlossen</b> und
     * ist danach nicht mehr benutzbar.
     * Mit dem eigenen Jackson-Konverter kann man zwar Streams in ein JSON-Array
     * konvertieren, aber bei einem (nicht-geschlossenen) Stream kann man nicht
     * erkennen, ob es evtl. keine Objekte gibt, so dass 404 nicht zur&uuml;ck
     * gegeben werden kann.
     * @param stream Das Stream-Objekt
     * @return Stream von HATEOAS-Resourcen
     */
    List<R> toResources(Stream<E> stream) {
        // collect() ist eine finale Operation auf einem Stream
        final val list = stream.map(this::toResource).collect(toList());
        if (list.isEmpty()) {
            throw new NotFoundException();
        }
        return list;
    }
}
