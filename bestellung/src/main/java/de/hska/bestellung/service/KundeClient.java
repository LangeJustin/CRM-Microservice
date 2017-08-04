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

import de.hska.bestellung.entity.Kunde;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static de.hska.bestellung.service.KundeClient.SERVICE_NAME;

/**
 * REST-Client mit Open Feign f&uuml;r "Kunde".
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */

// BEACHTE: KEINE WSDL WIE BEI SOAP NOTWENDIG !!!

@FeignClient(name = SERVICE_NAME, fallback = KundeClientFallback.class)
//@RibbonClient(name = SERVICE_NAME)
@FunctionalInterface   // nur 1 Methode im Interface
@SuppressWarnings("squid:S1214")
public interface KundeClient {
    String SERVICE_NAME = "kunde";
    // Den Basis-Pfad nicht mit @RequestMapping beim Interface definieren, weil
    // sonst das Interface zu einem Spring-Bean wird
    String BASE_PATH = "/kunde";

    @GetMapping(BASE_PATH + "/{id}")
    Kunde findById(@PathVariable String id);
}
