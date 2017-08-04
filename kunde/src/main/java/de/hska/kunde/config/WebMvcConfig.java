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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import lombok.val;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json
       .MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation
       .WebMvcConfigurerAdapter;

import static com.fasterxml.jackson.core.Version.unknownVersion;
import static java.util.Collections.singletonList;

/**
 * Jackson-Converter, der einen Stream in eine List konvertiert.
 * https://www.airpair.com/java/posts/spring-streams-memory-efficiency
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Configuration
class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>>
                                           converters) {
        converters.add(jackson2HttpMessageConverter());
    }

    // Stream als JSON-Datensatz
    private MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        final val jackson = new MappingJackson2HttpMessageConverter();
        final val om = jackson.getObjectMapper();
        final val streamSerializer =
            new StdSerializer<Stream<?>>(Stream.class, true) {
            @Override
            public void serialize(Stream<?> stream, JsonGenerator jgen,
                                  SerializerProvider provider)
                        throws IOException {
                provider.findValueSerializer(Iterator.class, null)
                        .serialize(stream.iterator(), jgen, provider);
            }
        };
        om.registerModule(new SimpleModule("Streams API", unknownVersion(),
                                           singletonList(streamSerializer)));
        return jackson;
   }
    
    /**
     * LogRequestResponseInterceptor als zus&auml;tzlicher Interceptor bei
     * Spring MVC
     * @param registry Verzeichnis der aktivierten Interceptoren
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestResponseLogInterceptor());
    }
    
    /**
     * CORS f&uuml;r einen Webserver mit z.B. Angular
     * https://spring.io/understanding/CORS
     * https://spring.io/blog/2015/06/08/cors-support-in-spring-framework
     * https://spring.io/guides/gs/rest-service-cors/
     * @param registry CorsRegistry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://localhost")
                .allowedHeaders("origin", "content-type", "accept",
                                "if-none-match", "if-match", "authorization",
                                "x-requested-with")
                .allowedMethods("OPTIONS", "GET", "POST", "PUT", "PATCH",
                                "DELETE")
                .exposedHeaders("ETag", "Location")
                .allowCredentials(true);
    }
}
