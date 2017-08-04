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

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import de.hska.kunde.config.security.Account;
import de.hska.kunde.entity.FamilienstandType;
import de.hska.kunde.entity.GeschlechtType;
import de.hska.kunde.entity.InteresseType;
import java.util.Collection;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config
       .EnableMongoRepositories;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;


/**
 * Spring-Konfiguration f&uuml;r den Zugriff auf MongoDB
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Configuration
@EnableMongoRepositories(basePackages = {
    "de.hska.*.service",
    "de.hska.*.config.security"
})
@EnableMongoAuditing
@Log4j2
// public wegen Repository-Tests
public class DbConfig extends AbstractMongoConfiguration {
    // FIXME Java 9: List.of
    private static final List<Class<?>> CONVERTERS = asList(
        // Enums
        GeschlechtType.ReadConverter.class,
        GeschlechtType.WriteConverter.class,
        FamilienstandType.ReadConverter.class,
        FamilienstandType.WriteConverter.class,
        InteresseType.ReadConverter.class,
        InteresseType.WriteConverter.class,
        
        // Rollen fuer Security
        Account.RoleReadConverter.class,
        Account.RoleWriteConverter.class
    );

    private static final String ENTITY_PKG;
    private static final String APP_NAME;
    
    static {
        final val packageName = DbConfig.class.getPackage().getName();
        final val parentPackageName =
            packageName.substring(0, packageName.lastIndexOf('.'));
        ENTITY_PKG = parentPackageName + ".entity";
        
        APP_NAME =
            parentPackageName.substring(parentPackageName.lastIndexOf('.') + 1);
    }
    
    private final String dbname;
    private final String uri;
    // Connection Pool
    private final int maxConnectionsPerHost;

    DbConfig(@Value("${db.name:hskadb}") String dbname,
             @Value("${db.host:127.0.0.1}") String dbhost,
             @Value("${db.username:admin}") String username,
             @Value("${db.password:p}") String password,
             @Value("${db.authDb:admin}") String authDb,
             @Value("${db.maxConnectionsPerHost:10}")
             int maxConnectionsPerHost) {
        log.info("Datenbank: {}", dbname);
        this.dbname = dbname;
        uri = "mongodb://" + username + ":" + password + "@" + dbhost
              + "/?authSource=" + authDb;
        log.debug("URI fuer MongoDB: {}", uri);
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }
    
    @Override
    protected String getDatabaseName() {
        return dbname;
    }
    
    @Override
    public Mongo mongo() {
        // Host: localhost
        // Port: Default Port 27017
        // ohne Username und ohne Passwort
        // Connections per Host: 100
        final val optionsBuilder = MongoClientOptions.builder()
                                .connectionsPerHost(maxConnectionsPerHost)
                                // http://docs.mlab.com/connecting/#known-issues
                                // .maxConnectionIdleTime(60000)
                                .applicationName(APP_NAME)
                                .description(APP_NAME);
        final val mongoClientUri = new MongoClientURI(uri, optionsBuilder);
        return new MongoClient(mongoClientUri);
    }
    
     @Override
     protected Collection<String> getMappingBasePackages() {
         return singleton(ENTITY_PKG);
     }
    
    @Override
    public CustomConversions customConversions() {
        final val converterList = CONVERTERS.stream().map(converterClass -> {
                final Object newInstance;
                try {
                    newInstance = converterClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
                return newInstance;
            }).collect(toList());
        return new CustomConversions(converterList);
    }
}
