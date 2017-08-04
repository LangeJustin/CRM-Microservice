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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.val;
import org.springframework.boot.Banner;

import static java.util.Collections.unmodifiableMap;

/**
 * Properties f&uuml;r das API-Gateway
 */
final class Settings {
    private static final int PORT = 8443;
    private static final int EUREKA_PORT = 8761;
    static final String DEV_PROFILE = "dev";

    private static final String NEWLINE = System.getProperty("line.separator");
    static final Banner BANNER = (environment, sourceClass, out) ->
        out.println(" _____                        __"
            + NEWLINE
            + "/__  /     __  __   __  __   / /"
            + NEWLINE
            + "  / /     / / / /  / / / /  / /"
            + NEWLINE
            + " / /__   / /_/ /  / /_/ /  / /"
            + NEWLINE
            + "/____/   \\__,_/   \\__,_/  /_/"
            + NEWLINE
        );

    @SuppressWarnings("PMD.AvoidFieldNameMatchingTypeName")
    static final Map<String, Object> PROPS;
    static {
        final val packageName = ApiGateway.class.getPackage().getName();
        final val appName =
            packageName.substring(packageName.lastIndexOf('.') + 1);

        // FIXME Java 9: Map.of
        final val map = new ConcurrentHashMap<String, Object>();
        map.put("server.port", PORT);
        map.put("server.use-forward-headers", true);
        map.put("error.whitelabel.enabled", false);
        map.put("spring.application.name", appName);
        map.put("logging.path", "build");

        // http://www.dineshonjava.com/2016/08/...
        // ...spring-boot-actuator-complete-guide.html
        map.put("management.context-path", "/admin");
        map.put("management.security.enabled", false);
        map.put("endpoints.shutdown.enabled", true);

        // Eureka-Server lokalisieren
        // https://github.com/spring-cloud/spring-cloud-netflix/blob/master/...
        // ...spring-cloud-netflix-eureka-client/src/main/java/org/...
        // ...springframework/cloud/netflix/eureka/EurekaClientConfigBean.java
        // Mit BASIC Auth.: http://user:password@localhost:8761/eureka
        map.put("eureka.client.serviceUrl.defaultZone",
                "http://localhost:" + EUREKA_PORT + "/eureka/");
        map.put("eureka.instance.statusPageUrl",
                "https://${eureka.hostname}/${management.context-path}/info");
        map.put("eureka.instance.healthCheckUrl",
                "https://${eureka.hostname}/${management.context-path}/health");
        // https://github.com/spring-cloud/spring-cloud-netflix/blob/master/...
        // ...spring-cloud-netflix-eureka-client/src/main/java/org/...
        // ...springframework/cloud/netflix/eureka/EurekaInstanceConfigBean.java
        map.put("eureka.instance.preferIpAddress", true);
        // map.put("eureka.instance.securePortEnabled", true)

        PROPS = unmodifiableMap(map);
    }

    private Settings() {
    }
}
