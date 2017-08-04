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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.val;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.security.core.SpringSecurityCoreVersion;

import static java.util.Collections.unmodifiableMap;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Definition der Properties f&uuml;r den Microservice.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
public final class Settings {
    public static final String DEV_PROFILE = "dev";
    public static final String DEBUG_EVENTS_PROFILE = "debugEvents";

    private static final String VERSION = "1.0";
    private static final int EUREKA_PORT = 8761;

    public static final Map<String, Object> PROPS;
    static {
        final val packageName = Settings.class.getPackage().getName();
        final val parentPackageName =
            packageName.substring(0, packageName.lastIndexOf('.'));
        final val appName =
            parentPackageName.substring(parentPackageName.lastIndexOf('.') + 1);
        getLogger().info("Name des Microservice: {}", appName);

        // FIXME Java 9: Map.of
        final val map = new ConcurrentHashMap<String, Object>();
        map.put("spring.application.name", appName);
        map.put("spring.application.version", VERSION);
        map.put("spring.profiles.default", "prod");
        
        // map.put("server.error.whitelabel.enabled", false)
        // http://www.dineshonjava.com/2016/08/...
        // ...spring-boot-actuator-complete-guide.html
        map.put("management.context-path", "/admin");
        map.put("management.security.enabled", false);
        map.put("endpoints.shutdown.enabled", true);
        map.put("endpoints.health.sensitive", false);

        // Eureka-Server lokalisieren
        // https://github.com/spring-cloud/spring-cloud-netflix/blob/master/...
        // ...spring-cloud-netflix-eureka-client/src/main/java/org/...
        // ...springframework/cloud/netflix/eureka/EurekaClientConfigBean.java
        //map.put("eureka.client.securePortEnabled", true)
        map.put("eureka.client.serviceUrl.defaultZone",
                "http://localhost:" + EUREKA_PORT + "/eureka/");

        //map.put("eureka.instance.securePort",
        //        "${server.httpsPort}")
        //map.put("eureka.instance.securePortEnabled", true)
        //map.put("eureka.instance.nonSecurePortEnabled", false)
        //map.put("eureka.instance.metadataMap.instanceId",
        //        "${vcap.application.instance_id:${spring.application.name}:${spring.application.instance_id:${server.securePort}}}")
        map.put("eureka.instance.statusPageUrl",
                "https://${eureka.hostname}/${management.context-path}/info");
        map.put("eureka.instance.healthCheckUrl",
                "https://${eureka.hostname}/${management.context-path}/health");
        // https://github.com/spring-cloud/spring-cloud-netflix/blob/master/...
        // ...spring-cloud-netflix-eureka-client/src/main/java/org/...
        // ...springframework/cloud/netflix/eureka/EurekaInstanceConfigBean.java
        // Evtl. generierter Rechnername bei z.B. Docker
        map.put("eureka.instance.preferIpAddress", true);

        // SpringFox fuer Swagger
        // https://github.com/spring-cloud/spring-cloud-netflix/issues/1398
        // map.put("eureka.instance.appName", "${spring.application.name}")

        map.put("feign.hystrix.enabled", true);
        map.put("feign.compression.request.enabled", true);
        final val applicationJson = APPLICATION_JSON.getType();
        map.put("feign.compression.request.mime-types", applicationJson);
        map.put("feign.compression.response.enabled", true);
        map.put("feign.compression.response.mime-types", applicationJson);

        map.put("spring.devtools.restart.trigger-file=", "/restart.txt");
        PROPS = unmodifiableMap(map);
    }
    
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String BANNER_STR =
        "       __                                    _____"
        + NEWLINE
        + "      / /_  _____  _________ ____  ____     /__  /"
        + NEWLINE
        + " __  / / / / / _ \\/ ___/ __ `/ _ \\/ __ \\      / /"
        + NEWLINE
        + "/ /_/ / /_/ /  __/ /  / /_/ /  __/ / / /     / /___"
        + NEWLINE
        + "\\____/\\__,_/\\___/_/   \\__, /\\___/_/ /_/     /____(_)"
        + NEWLINE
        + "                     /____/"
        + NEWLINE + NEWLINE
        + "(C) Juergen Zimmermann, Hochschule Karlsruhe"
        + NEWLINE
        + "Version " + VERSION
        + NEWLINE
        + "Spring Boot      " + SpringBootVersion.getVersion()
        + NEWLINE
        + "Spring Security  " + SpringSecurityCoreVersion.getVersion()
        + NEWLINE
        + "Spring Framework " + SpringVersion.getVersion()
        + NEWLINE
        + "JDK              " + System.getProperty("java.version")
        + NEWLINE;
    public static final Banner BANNER =
        (environment, sourceClass, out) -> out.println(BANNER_STR);

    private Settings() {
        throw new UnsupportedOperationException(
            "Das ist eine Utility-Klasse, die nicht instantiiert werden kann");
    }
}
