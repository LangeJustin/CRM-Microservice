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
package de.hska.bestellung.config.dev;

import de.hska.bestellung.service.BestellungService;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;

import static de.hska.bestellung.config.Settings.DEV_PROFILE;
import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Einen CommandLineRunner bereitstellen.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
public interface LogAlleBestellungen {
    /**
     * Spring Bean, um einen CommandLineRunner f&uuml;r das Profil "dev"
     * bereitzustellen.
     * @param bestellungService Injiziertes Service-Objekt, um die
     *        Bestellungen auszulesen
     * @return CommandLineRunner
     */
    @Bean
    @Qualifier("LogAlleBestellungenRunner")
    @Description("Ausgabe aller Bestellungen beim Start des Microservice")
    @Profile(DEV_PROFILE)
    default CommandLineRunner logAlleBestellungen(BestellungService
                                                      bestellungService) {
        final val log = getLogger();
        return args -> bestellungService.findAll().forEach(log::warn);
    }
}
