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

package de.hska.kunde;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.system.ApplicationPidFileWriter;

import static de.hska.kunde.config.Settings.BANNER;
import static de.hska.kunde.config.Settings.PROPS;

/**
 * Start des Microservice
 */
// @Configuration @EnableAutoConfiguration @ComponentScan
@SpringBootApplication
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor",
                   "PMD.UseUtilityClass"})
public class Microservice {
    @SuppressWarnings("WeakerAccess")
    Microservice() {
        // Leerer Konstruktor gemaess Spring Boot
    }

    /**
     * Start des Microservice
     * @param args Zus&auml;tzliche Argumente fuer den Microservice
     */
    @SuppressWarnings("checkstyle:UncommentedMain")
    public static void main(String[] args) {
        // Auskommentieren um ein Passwort zu verschluesseln
        // Beachte: Spring Boot erlaubt nur 1 Klasse mit main()
        // final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(9)
        // System.out.println(">>> Hash: " + passwordEncoder.encode("..."))

        new SpringApplicationBuilder(Microservice.class)
            .banner(BANNER)
            .properties(PROPS)
            .listeners(new ApplicationPidFileWriter())
            .run(args);
    }
}
