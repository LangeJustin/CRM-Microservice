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
package de.hska.bestellung.config;

import de.hska.bestellung.config.dev.KundeClientBeispiel;
import de.hska.bestellung.config.dev.LogAlleBestellungen;
import de.hska.bestellung.config.dev.LogDiscoveryClient;
import de.hska.bestellung.config.dev.MongoMappingEventsListener;
import de.hska.bestellung.config.dev.RepositoryPopulator;
import de.hska.bestellung.service.KundeClient;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableCaching
@EnableDiscoveryClient
@EnableFeignClients(clients = KundeClient.class)
@EnableCircuitBreaker
@EnableSwagger2
class CommonConfig implements EmbeddedServletContainerConfig,
                              FeignConfig,
                              KundeClientBeispiel,
                              LogAlleBestellungen,
                              LogConfig,
                              LogDiscoveryClient,
                              MailConfig,
                              MongoMappingEventsListener,
                              RepositoryPopulator,
                              SwaggerConfig {
}
