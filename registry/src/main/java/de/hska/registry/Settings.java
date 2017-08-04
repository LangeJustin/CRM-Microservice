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

import lombok.val;
import org.springframework.boot.Banner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableMap;

/**
 * Einstellungen bzw. Properties
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
final class Settings {
    private static final int PORT = 8761;
    static final String DEV_PROFILE = "dev";

    private static final String NEWLINE = System.getProperty("line.separator");
    static final Banner BANNER = (environment, sourceClass, out) ->
        out.println("    ______                __"
            + NEWLINE
            + "   / ____/_  __________  / /______ _"
            + NEWLINE
            + "  / __/ / / / / ___/ _ \\/ //_/ __ `/"
            + NEWLINE
            + " / /___/ /_/ / /  /  __/ ,< / /_/ /"
            + NEWLINE
            + "/_____/\\__,_/_/   \\___/_/|_|\\__,_/"
            + NEWLINE
        );

    static final Map<String, Object> PROPS;
    static {
        final val packageName = Registry.class.getPackage().getName();
        final val appName =
            packageName.substring(packageName.lastIndexOf('.') + 1);

        // FIXME Java 9: Map.of
        final val map = new ConcurrentHashMap<String, Object>();
        map.put("server.port", PORT);
        map.put("error.whitelabel.enabled", false);
        map.put("spring.application.name", appName);
        map.put("logging.path", "build");

        // http://www.dineshonjava.com/2016/08/...
        // ...spring-boot-actuator-complete-guide.html
        map.put("management.context-path", "/admin");
        map.put("management.security.enabled", false);
        map.put("endpoints.shutdown.enabled", true);

        // https://github.com/spring-cloud/spring-cloud-netflix/blob/...
        // ...master/spring-cloud-netflix-eureka-client/src/main/java/...
        // ...org/springframework/cloud/netflix/eureka/...
        // ...EurekaInstanceConfigBean.java
        map.put("eureka.instance.hostname", "localhost");
        //map.put("eureka.instance.securePort", "${server.port}")
        //map.put("eureka.instance.securePortEnabled", true)
        //map.put("eureka.instance.nonSecurePortEnabled", false)
        //map.put("eureka.instance.secureVirtualHostName",
        //        "${spring.application.name}")
        map.put("eureka.instance.homePageUrl",
                "https://${eureka.instance.hostname}:${server.port}/");
        map.put("eureka.instance.statusPageUrl",
                "https://${eureka.instance.hostname}:${server.port}/admin/info");
        //map.put("eureka.instance.metadataMap.hostname",
        //        "${eureka.instance.hostname}")
        //map.put("eureka.instance.metadataMap.securePort", "${server.port}")

        // Eureka-Server nicht bei sich selbst registrieren
        // https://github.com/spring-cloud/spring-cloud-netflix/blob/...
        // ...master/spring-cloud-netflix-eureka-client/src/main/java/...
        // ...org/springframework/cloud/netflix/eureka/...
        // ...EurekaClientConfigBean.java
        map.put("eureka.client.registerWithEureka", false);
        map.put("eureka.client.fetchRegistry", false);
        map.put("eureka.client.server.waitTimeInMsWhenSyncEmpty", 0);
        map.put("eureka.client.serviceUrl.defaultZone",
            "http://${eureka.instance.hostname}:${server.port}/eureka/");
        map.put("eureka.datacenter", "default");
        map.put("eureka.environment", "test");

        // Dashboard nutzt Freemarker
        map.put("spring.thymeleaf.enabled", false);

        // map.put("security.basic.enabled", false);
        // map.put("security.user.name", "admin");
        // map.put("security.user.password", "p");

        PROPS = unmodifiableMap(map);
    }

    private Settings() {
        throw new UnsupportedOperationException(
            "Das ist eine Utility-Klasse, die nicht instantiiert werden kann");
    }
}
