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

import feign.Logger.Level;
import feign.Request.Options;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import static feign.Logger.Level.FULL;

/**
 * Konfiguration des Feign-Clients.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Configuration
interface FeignConfig {
    @Bean
    @Description("BASIC-Authentifizierung beim Microservice kunden")
    default RequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor("admin", "p");
    }
  
    @Bean
    @Description("Log-Level fuer den Feign-Client")
    @SuppressWarnings("SameReturnValue")
    default Level feignLogger() {
        return FULL;
    }
    
    @Bean
    @Description("Timeout-Optionen fuer den Feign-Client")
    default Options options() {
        // Defaultwerte:
        final val connectTimeoutMillis = 10_000;
        final val readTimeoutMillis = 60_000;

        return new Options(connectTimeoutMillis, readTimeoutMillis);
    }
}
