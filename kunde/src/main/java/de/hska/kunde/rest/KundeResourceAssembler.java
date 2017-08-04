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

import de.hska.kunde.entity.Kunde;
import org.springframework.stereotype.Component;

import static de.hska.kunde.rest.KundeResource.of;

@Component
/* package */ class KundeResourceAssembler
      extends AbstractResourceAssembler<Kunde, KundeResource> {
    KundeResourceAssembler() {
        super(KundeController.class, KundeResource.class);
    }
    
    @Override
    protected KundeResource instantiateResource(Kunde kunde) {
        return of(kunde);
    }
}
