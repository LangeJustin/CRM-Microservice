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
package de.hska.configserver;

import org.springframework.boot.Banner;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Einstellungen
 */
final class Settings {
    private static final int PORT = 8888;
    static final String DEV_PROFILE = "dev";

    private static final String NEWLINE = System.getProperty("line.separator");
    static final Banner BANNER = (environment, sourceClass, out) ->
        out.println("   ______            _____"
            + NEWLINE
            + "  / ____/___  ____  / __(_)___ _"
            + NEWLINE
            + " / /   / __ \\/ __ \\/ /_/ / __ `/"
            + NEWLINE
            + " \\____/\\____/_/ /_/_/ /_/\\__, /"
            + NEWLINE
            + "                        /____/"
            + NEWLINE
        );

    static final Map<String, Object> PROPS;
    static {
        final String packageName = Settings.class.getPackage().getName();
        final String appName =
            packageName.substring(packageName.lastIndexOf('.') + 1);

        @SuppressWarnings("PMD.UseConcurrentHashMap")
        final Map<String, Object> map = new HashMap<>();
        map.put("server.port", PORT);
        map.put("error.whitelabel.enabled", false);
        map.put("spring.application.name", appName);
        // map.put("spring.config.name", appName);
        map.put("logging.path", "build");
        map.put("endpoints.shutdown.enabled", true);

        // http://www.dineshonjava.com/2016/08/...
        // ...spring-boot-actuator-complete-guide.html
        map.put("management.context-path", "/admin");
        map.put("management.security.enabled", false);

        PROPS = unmodifiableMap(map);
    }

    private Settings() {
        throw new UnsupportedOperationException(
            "Das ist eine Utility-Klasse, die nicht instantiiert werden kann");
    }
}
