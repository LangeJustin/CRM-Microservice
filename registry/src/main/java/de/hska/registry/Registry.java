/*
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
package de.hska.registry;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import static de.hska.registry.Settings.BANNER;
import static de.hska.registry.Settings.DEV_PROFILE;
import static de.hska.registry.Settings.PROPS;

/**
 * Start des Eureka-Servers.
 * Abfrage mit z.B. http://localhost:8761/eureka/apps/kunde
 */
@EnableEurekaServer
@SpringBootApplication
@SuppressWarnings({
        "checkstyle:HideUtilityClassConstructor",
        "PMD.UseUtilityClass",
        "SpringFacetCodeInspection"
})
public class Registry {
    @SuppressWarnings("WeakerAccess")
    Registry() {
        // Leerer Konstruktor gemaess Spring Boot
    }

    /**
     * Start des Eureka-Servers
     * @param args Zus&auml;tzliche Argumente f&uuml;r den Eureka-Server
     */
    @SuppressWarnings("checkstyle:UncommentedMain")
    public static void main(String[] args) {
        new SpringApplicationBuilder(Registry.class)
            .banner(BANNER)
            .profiles(DEV_PROFILE)
            .listeners(new ApplicationPidFileWriter())
            .properties(PROPS)
            .run(args);
    }
}
