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
package de.hska.kunde.config.dev;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.event.LoggingEventListener;

import static de.hska.kunde.config.Settings.DEBUG_EVENTS_PROFILE;

public interface MongoMappingEventsListener {
    /**
     * MongoMappingEvents im Kontext von ApplicationContextEvent von Spring
     * protokollieren
     * @return Listener f&uuml;r die zu protokollierenden Events
     */
    @Bean
    @Description("MongoMappingEvents protokollieren")
    @Profile(DEBUG_EVENTS_PROFILE)
    default LoggingEventListener mappingEventsListener() {
        return new LoggingEventListener();
    }
}
