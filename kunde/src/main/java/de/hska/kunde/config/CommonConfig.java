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
package de.hska.kunde.config;

import de.hska.kunde.config.dev.LogAlleKunden;
import de.hska.kunde.config.dev.LogBasicAuth;
import de.hska.kunde.config.dev.MongoMappingEventsListener;
import de.hska.kunde.config.dev.RepositoryPopulator;
import de.hska.kunde.config.security.PasswordEncoder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

// * Mehrfachvererbung durch Interfaces mit default-Methoden
//   aus Java 8 fuer die Factory-Methoden mit @Bean
// * @Configuration-Klassen als Einstiegspunkt
// * Mit CGLIB werden @Configuration-Klassen verarbeitet

@Configuration
//@EnableAspectJAutoProxy
//@EnableScheduling
// https://spring.io/blog/2015/06/15/cache-auto-configuration-in-spring-boot-1-3
@EnableCaching
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableSwagger2
@SuppressWarnings("SpringFacetCodeInspection")
class CommonConfig implements EmbeddedServletContainerConfig,
                              LazyInit,
                              LogAlleKunden,
                              LogBasicAuth,
                              LogConfig,
                              MailConfig,
                              MongoMappingEventsListener,
                              PasswordEncoder,
                              RepositoryPopulator,
                              SwaggerConfig {
}
