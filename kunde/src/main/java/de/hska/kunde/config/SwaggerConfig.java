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

import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;

import static org.apache.logging.log4j.LogManager.getLogger;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

// In Anlehnung an
// http://www.baeldung.com/swagger-2-documentation-for-spring-rest-api
// Kein Interface, damit Swagger nachtraeglich einfach aktiviert werden kann, so
// dass dann aber in den Tests bei RestTemplate keine Atom-Links mehr ankommen.
interface SwaggerConfig {
    /**
     * Bean bereitstellen, damit es eine Swagger-Dokumentation zum REST-API
     * gibt. Die Swagger-Dokumentation wird mittels Springfox bereitgestellt.
     * Die Spring-Controller liegen unterhalb des "Parent Package"
     * z.B. Package de.hska.kunde.config.SwaggerConfig --> de.hska.kunde.
     * Damit keine Anpassung an andere Microservices notwendig ist, wird
     * nochmals das Parent-Package ermittelt, d.h. "de.hska"
     * @return Docket-Objekt gem&auml;&szlig; Springfox.
     */
    @Bean
    @Description("Swagger fuer die Dokumentation des REST-API")
    default Docket api() {
        final val pkg = SwaggerConfig.class.getPackage().getName();
        final val parentPkg = pkg.substring(0, pkg.lastIndexOf('.'));
        final val basePkg =
            parentPkg.substring(0, parentPkg.lastIndexOf('.'));
        getLogger().debug("Base Package: {}", basePkg);
        
        return new Docket(SWAGGER_2)
            // .directModelSubstitute(LocalDate.class, java.sql.Date.class)
            // .directModelSubstitute(LocalDateTime.class, java.util.Date.class)
            .select()
            .apis(RequestHandlerSelectors.basePackage(basePkg))
            // .apis(RequestHandlerSelectors.any())
            // .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo());
    }
    
    /**
     * Die allgemeinen Informationen zum REST-API werden bereitgestellt, damit
     * sie im Docket-Objekt verf&uuml;gbar sind.
     * @return Objekt der Klasse ApiInfo
     */
    // FIXME Java 9: private statt default
    default ApiInfo apiInfo() {
        return new ApiInfo(
            "REST API fuer die Verwaltung von Kunden",
            "Beispiel fuer einen Microservice",
            "Version 1.0",
            "Support innerhalb der Uebungen und Sprechstunden",
            new Contact("Juergen Zimmermann", "http://www.HS-Karlsruhe.de",
                        "Juergen.Zimmermann (at) HS-Karlsruhe.de"),
            "GPL v3",
            "http://www.gnu.org/licenses");
    }
}
