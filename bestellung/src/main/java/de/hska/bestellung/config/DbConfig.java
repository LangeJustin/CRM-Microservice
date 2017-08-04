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

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;

import com.mongodb.MongoClientURI;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config
       .EnableMongoRepositories;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static java.util.Collections.singleton;

/**
 * Spring-Konfiguration f&uuml;r den Zugriff auf MongoDB
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Configuration
@EnableMongoRepositories(basePackages = {
    "de.hska.*.service"
})
@EnableMongoAuditing
@Log4j2
// public wegen Repository-Tests
public class DbConfig extends AbstractMongoConfiguration {
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
    
    @SuppressWarnings("WeakerAccess")
    DbConfig(@Value("${db.name:hskadb}") String dbname,
             @Value("${db.host:localhost}") String dbhost,
             @Value("${db.username:admin}") String username,
             @Value("${db.password}") String password,
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
}
