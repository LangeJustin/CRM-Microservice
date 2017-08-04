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
package de.hska.kunde.config.dev;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;

import java.io.UnsupportedEncodingException;

import static de.hska.kunde.config.Settings.DEV_PROFILE;
import static java.util.Base64.getEncoder;
import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Einen CommandLineRunner zur Ausgabe f&uuml;r BASIC-Authentifizierung
 * bereitstellen.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@SuppressWarnings("squid:S1214")
public interface LogBasicAuth {
    String USERNAME = "admin";
    @SuppressWarnings("squid:S2068")
    String PASSWORD = "p";

    /**
     * Spring Bean, um einen CommandLineRunner f&uuml;r das Profil "dev"
     * bereitzustellen.
     * @return CommandLineRunner
     */
    @Bean
    @Qualifier("LogBasicAuthRunner")
    @Description("Ausgabe fuer BASIC-Authentifizierung")
    @Profile(DEV_PROFILE)
    default CommandLineRunner logBasicAuth()
                              throws UnsupportedEncodingException {
        final val log = getLogger();
        final val input = String.format("%s:%s", USERNAME, PASSWORD)
                .getBytes("ISO-8859-1");
        final val encoded = "Basic " + getEncoder().encodeToString(input);

        return args -> log.warn("BASIC Authentication:   >>>" + encoded
                                + "<<<");
    }

}
