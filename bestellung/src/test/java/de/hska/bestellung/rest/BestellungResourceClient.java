/*
 * Copyright (C) 2017 Juergen Zimmermann, Hochschule Karlsruhe
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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@ToString(callSuper = true)
@EqualsAndHashCode(exclude = "atomLinks", callSuper = true)
@NoArgsConstructor
class BestellungResourceClient extends BestellungResource {
    private Map<String, Map<String, String>> atomLinks;

    Map<String, Map<String, String>> getAtomLinks() {
        return atomLinks;
    }

    @JsonProperty("_links")
    @SuppressWarnings("unused")
    void setAtomLinks(Map<String, Map<String, String>> atomLinks) {
        this.atomLinks = atomLinks;
    }
}
