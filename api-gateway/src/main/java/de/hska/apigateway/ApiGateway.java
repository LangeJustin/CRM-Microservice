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
package de.hska.apigateway;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

import static de.hska.apigateway.Settings.BANNER;
import static de.hska.apigateway.Settings.DEV_PROFILE;
import static de.hska.apigateway.Settings.PROPS;

/**
 * Start des API-Gateways
 */
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
@SuppressWarnings({
    "checkstyle:HideUtilityClassConstructor",
    "PMD.UseUtilityClass",
    "SpringFacetCodeInspection"
})
public class ApiGateway {
    ApiGateway() {
        // Leerer Konstruktor gemaess Spring Boot
    }

    /**
     * Start des API-Gateways
     * @param args Zus&auml;tzliche Argumente f&uuml;r das API-Gateway
     */
    @SuppressWarnings("checkstyle:UncommentedMain")
    public static void main(String[] args) {
        new SpringApplicationBuilder(ApiGateway.class)
            .banner(BANNER)
            .profiles(DEV_PROFILE)
            .listeners(new ApplicationPidFileWriter())
            .properties(PROPS)
            .run(args);
    }
}
