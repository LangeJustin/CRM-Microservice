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
import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

interface BestellungRepository extends CrudRepository<Bestellung, ObjectId> {
    /**
     * Suche nach einer Bestellung mit der gegebenen ID.
     * @param id Die ID der gesuchten Bestellung
     * @return Die gefundene Bestellung oder empty
     */
    Optional<Bestellung> findById(ObjectId id);

    /**
     * Suche nach allen Bestellungen, d.h. ohne Suchkriterium.
     * @return Alle Bestellungen als Stream
     */
    Stream<Bestellung> findAllBy();

    /**
     * Suche nach den Bestellungen eines Kunden mit gegebener Kunde-Id
     * @return Gefundene Bestellungen als Stream
     */
    Stream<Bestellung> findByKundeId(String kundeId);
}
