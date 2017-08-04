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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.kunde.config.security.Account;
import de.hska.kunde.entity.Kunde;
import java.io.IOException;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.val;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.repository.init
       .Jackson2RepositoryPopulatorFactoryBean;
import org.springframework.data.repository.init.ResourceReaderRepositoryPopulator;

import static de.hska.kunde.config.Settings.DEV_PROFILE;
import static java.util.Arrays.asList;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

@SuppressFBWarnings("MS_OOI_PKGPROTECT")
@SuppressWarnings("squid:S1214")
public interface RepositoryPopulator {
    // FIXME Java 9: List.of
    @SuppressWarnings("squid:S2386")
    List<Class<?>> AGGREGATE_CLASSES = asList(
        Kunde.class,
        Account.class
    );

    /**
     * Bean zum Befuellen der DB aus der JSON-Datei src/main/resources/data.json
     * @return RepositoryPopulatorFactoryBean mittels Jackson 2
     */
    @Bean
    @Description("Testdaten fuer Kunden laden")
    @Profile(DEV_PROFILE)
    default AbstractFactoryBean<ResourceReaderRepositoryPopulator>
    populatorFactory(MongoTemplate mongoTemplate,
                     ObjectMapper jsonMapper,
                     ResourcePatternResolver resourceResolver,
                     GridFsTemplate gridFsTemplate) {
        final val populator = new Jackson2RepositoryPopulatorFactoryBean();
        populator.setMapper(jsonMapper);

        // alle hochgeladenen multimedialen Dateien loeschen
        gridFsTemplate.delete(null);
        
        // Dokumente in den Collections loeschen
        AGGREGATE_CLASSES.stream()
                         .filter(mongoTemplate::collectionExists)
                         .forEach(mongoTemplate::dropCollection);
        
        try {
            populator.setResources(
                resourceResolver.getResources(CLASSPATH_URL_PREFIX
                                              + "data.json"));
            // populator.afterPropertiesSet()
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        
        return populator;
    }
}
