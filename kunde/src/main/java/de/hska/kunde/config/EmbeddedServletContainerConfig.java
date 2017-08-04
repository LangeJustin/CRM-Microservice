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

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.servlet.MultipartConfigElement;

import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded
       .EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.undertow
       .UndertowBuilderCustomizer;
import org.springframework.boot.context.embedded.undertow
       .UndertowEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation
       .Bean;
import org.springframework.context.annotation.Description;

import static io.undertow.UndertowOptions.ENABLE_HTTP2;
import static java.util.Collections.singleton;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;
import static org.springframework.util.ResourceUtils.getURL;

/**
 * Spring-Konfiguration f&uuml;r den EmbeddedServletContainer
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@SuppressWarnings("squid:S1214")
interface EmbeddedServletContainerConfig {
    String DEFAULT_KEYSTORE_TYPE = "PKCS12";
    String DEFAULT_KEYSTORE_FILENAME = "keystore.p12";
    @SuppressWarnings("squid:S2068")
    String DEFAULT_KEYSTORE_PASSWORD = "zimmermann";
    String DEFAULT_KEYSTORE_ALGORITHM = "SunX509";
    String DEFAULT_TLS_VERSION = "TLSv1.2";
    int DEFAULT_GZIP_PRIORITY = 50;
    String MAX_UPLOAD_SIZE = "10MB";

    /**
     * Spring Bean f&uuml;r die Undertow-Factory mit HTTPS erzeugen
     * @param httpsPort Port f&uuml;r HTTPS
     * @param keystoreType Type des Keystore, z.B. PKCS12
     * @param keystoreFilename Dateiname des Keystore
     * @param keystorePassword Passwort f&uuml;r den Keystore
     * @param keystoreAlg Algorithmus f&uuml;r das Keymanagement, z.B. SunX509
     * @param tlsVersion TLS Version, z.B. TLSv1.2
     * @param gzipPriority Priorit&auml;t bei GZIP-Komprimierung
     * @return Factory f&uuml;r Undertow
     */
    @Bean
    @ConditionalOnProperty("server.httpsPort")
    @Description("Undertow Embedded mit HTTPS")
    default
    EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer(
        @Value("${server.httpsPort}") Integer httpsPort,
        @Value("${container.keystore.type:"
            + DEFAULT_KEYSTORE_TYPE + '}') String keystoreType,
        @Value("${container.keystore.filename:"
            + DEFAULT_KEYSTORE_FILENAME + '}') String keystoreFilename,
        @Value("${container.keystore.password:"
            + DEFAULT_KEYSTORE_PASSWORD + '}') String keystorePassword,
        @Value("${container.keystore.algorithm:"
            + DEFAULT_KEYSTORE_ALGORITHM + '}') String keystoreAlg,
        @Value("${container.tlsVersion:"
            + DEFAULT_TLS_VERSION + '}') String tlsVersion,
        @Value("${container.gzip.priority:"
            + DEFAULT_GZIP_PRIORITY + '}') int gzipPriority) {
        // funktionales Interface
        return container -> {
            if (!(container
                  instanceof UndertowEmbeddedServletContainerFactory)) {
                throw new IllegalArgumentException(
                           "Falsche Klasse: " + container.getClass().getName());
            }
            final UndertowEmbeddedServletContainerFactory factory =
                (UndertowEmbeddedServletContainerFactory) container;
            setHttps(factory, httpsPort, keystoreType, keystoreFilename,
                     keystorePassword, keystoreAlg, tlsVersion, gzipPriority);
        };
    }
    
    /**
     * Hilfsmethode, um HTTPS zu konfigurieren
     * @param factory Factory f&uuml;r Undertow
     * @param httpsPort Port f&uuml;r HTTPS
     * @param keystoreType Type des Keystore, z.B. PKCS12
     * @param keystoreFilename Dateiname des Keystore
     * @param keystorePassword Passwort f&uuml;r den Keystore
     * @param keystoreAlg Algorithmus f&uuml;r das Keymanagement, z.B. SunX509
     * @param tlsVersion TLS Version, z.B. TLSv1.2
     * @param gzipPriority Priorit&auml;t bei GZIP-Komprimierung
     */
    @SuppressWarnings({
        "checkstyle:ParameterNumber",
        "checkstyle:RightCurly",
        "squid:S00107"
    })
    // FIXME Java 9: private statt default
    default void setHttps(UndertowEmbeddedServletContainerFactory factory,
                          int httpsPort, String keystoreType,
                          String keystoreFilename, String keystorePassword,
                          String keystoreAlg, String tlsVersion,
                          int gzipPriority) {
        final KeyStore keystore;
        try {
            keystore = KeyStore.getInstance(keystoreType);
        } catch (KeyStoreException e) {
            throw new IllegalArgumentException(e);
        }

        final SSLContext sslContext;
        try (InputStream stream =
                getURL(CLASSPATH_URL_PREFIX + keystoreFilename)
                .openConnection()
                .getInputStream()) {
            keystore.load(stream, keystorePassword.toCharArray());
            final val keyManagerFactory =
                KeyManagerFactory.getInstance(keystoreAlg);
            keyManagerFactory.init(keystore, keystorePassword.toCharArray());
            sslContext = SSLContext.getInstance(tlsVersion);
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        } catch (FileNotFoundException e) {
            final val log = getLogger();
            log.warn("Kein HTTPS: " + keystoreFilename + " fehlt");
            log.debug("Kein HTTPS: " + keystoreFilename + " fehlt", e);
            return;
        } catch (IOException | NoSuchAlgorithmException
                 | CertificateException | KeyStoreException
                 | UnrecoverableKeyException | KeyManagementException e) {
            throw new IllegalArgumentException(e);
        }

        // io.undertow.Undertow.Builder
        final UndertowBuilderCustomizer customizer = builder -> {
            final val host = "localhost";
            builder.setServerOption(ENABLE_HTTP2, true)
                    // ggf. XnioWorker und ByteBufferPool konfigurieren
                    // TODO Header Content-Security-Policy setzen
                    .addHttpsListener(httpsPort, host, sslContext)
                    .setHandler(compressionHandler(gzipPriority));
        };
        factory.setBuilderCustomizers(singleton(customizer));
    }
    
    /**
     * Hilfsmethode, um die GZIP-Komprimierung zu konfigurieren
     * @param gzipPriority Priorit&auml;t bei GZIP-Komprimierung
     * @return HTTP-Handler mit GZIP-Komprimierung
     */
    // FIXME Java 9: private statt default
    default HttpHandler compressionHandler(int gzipPriority) {
        return new EncodingHandler(
                    new ContentEncodingRepository()
                    .addEncodingHandler("gzip", new GzipEncodingProvider(),
                                        gzipPriority));
    }
    
    /**
     * Spring Bean, um bin&auml;re Dateien hochladen zu k&ouml;nnen
     * @return Das Spring Bean f&uuml;r Multipart-Requests.
     */
    @Bean
    default MultipartConfigElement multipartConfigElement() {
        final val factory = new MultipartConfigFactory();
        factory.setMaxFileSize(MAX_UPLOAD_SIZE);
        factory.setMaxRequestSize(MAX_UPLOAD_SIZE);
        return factory.createMultipartConfig();
    }
}
