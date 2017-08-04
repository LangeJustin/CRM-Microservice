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

import de.hska.bestellung.entity.Kunde;
import de.hska.bestellung.service.KundeClient;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import static de.hska.bestellung.config.Settings.DEV_PROFILE;
import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Logging f&uuml;r den aufzurufenden Microservice "kunden".
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@SuppressWarnings("squid:S1214")
public interface KundeClientBeispiel {
    String KUNDE_ID = "000000000000000000000001";

    @Bean
    @Qualifier("KundeClientBeispiel")
    @Description("Ausgabe der registrierten Services")
    @Profile(DEV_PROFILE)
    @SuppressWarnings("SpringJavaAutowiringInspection")
    default CommandLineRunner
        kundenClientBeispiel(KundeClient kundeClient,
                             RestTemplateBuilder restTemplateBuilder,
                             DiscoveryClient discoveryClient) {
        final RestTemplate restTemplate =
            restTemplateBuilder.basicAuthorization("admin", "p")
                               .build();

        final val log = getLogger();
        final val services =
            discoveryClient.getInstances(KundeClient.SERVICE_NAME);

        return args -> {
            if (services.isEmpty()) {
                log.error("Keine Instanz fuer {}", KundeClient.SERVICE_NAME);
                return;
            }

            // Client mit RestTemplate (und DiscoveryClient)
            final val kundeUri = services.get(0).getUri().toString();
            log.warn("Kunde-URI: {}", kundeUri);
            //log.warn("RestTemplate fuer Kunde: {}",
            //        () -> restTemplate.getForObject(kundeUri + "/{id}",
            //                Kunde.class,
            //                KUNDE_ID));

            // Client mit OpenFeign
            kundeClient.findById(KUNDE_ID);
            log.warn("Feign fuer Kunde: {}",
                      () -> kundeClient.findById(KUNDE_ID));
        };
    }
}
