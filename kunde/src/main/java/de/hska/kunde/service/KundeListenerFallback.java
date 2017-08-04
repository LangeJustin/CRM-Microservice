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

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Fallback zum Listener f&uuml;r neue Kunden.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Service
class KundeListenerFallback {
    private final Logger log;
    
    KundeListenerFallback(Logger log) {
        this.log = log;
    }

    void onSave(NeuerKundeEvent event) {
        // TODO Abspeichern der noch nicht gesendeten Email
        log.error("Fehler beim Senden der Email: Nachname = {}",
                  event::getKunde);
    }
}
