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

import de.hska.kunde.config.security.AuthService;
import de.hska.kunde.config.security.AuthenticationProvider;
import lombok.val;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders
       .AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration
       .WebSecurityConfigurerAdapter;

import java.util.Locale;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;

/**
 * Security-Konfiguration mit MongoDB f&uuml;r Username, Password und Rolle.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Configuration
// @EnableWebSecurity bereits durch @SpringBootApplication
// @Secured wird jetzt bei @Controller und @Service beruecksichtigt
@EnableGlobalMethodSecurity(securedEnabled = true)
class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String REALM;
    
    private static final String ADMIN = "ADMIN";
    private static final String KUNDE = "KUNDE";
    private static final String KUNDE_PATH = "/kunde";
    private static final String AUTH_PATH = "/auth";
    
    static {
        // Name der REALM = Name des Parent-Package in Grossbuchstaben,
        // z.B. KUNDE
        final val pkg = SecurityConfig.class.getPackage().getName();
        final val parentPkg = pkg.substring(0, pkg.lastIndexOf('.'));
        REALM = parentPkg
                .substring(parentPkg.lastIndexOf('.') + 1)
                .toUpperCase(Locale.getDefault());
    }
    
    @SuppressWarnings({"checkstyle:IllegalCatch",
            "PMD.AvoidCatchingGenericException", "WeakerAccess"})
    SecurityConfig(AuthService authService,
                   AuthenticationManagerBuilder auth,
                   AuthenticationProvider authenticationProvider) {
        // Authentifizierung durch den injizierten Provider mittels einer
        // MongoDB. Fuer die Produktion ein Profil ergaenzen fuer SSO
        // (mit Keycloak oder Stormpath) oder OAuth2 oder ...
        // http://www.baeldung.com/spring-security-oauth-jwt
        try {
            auth.userDetailsService(authService);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        auth.authenticationProvider(authenticationProvider);
    }
    
    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected void configure(HttpSecurity http) throws Exception {
        http
            // Zugriff nur ueber https und http wird zu https umgeleitet:
            // .requiresChannel().anyRequest().requiresSecure()
            // .and()
            // .portMapper().http(HTTP_PORT).mapsTo(httpsPort)
            // .and()
            .authorizeRequests()
                .antMatchers(POST, KUNDE_PATH).permitAll()
                .antMatchers(GET, KUNDE_PATH).hasRole(ADMIN)
                .antMatchers(GET, KUNDE_PATH + "/*").hasRole(ADMIN)
                .antMatchers(PATCH, KUNDE_PATH).hasRole(KUNDE)
                .antMatchers(DELETE, KUNDE_PATH + "/*")
                    .access("hasRole('" + ADMIN + "') and "
                            + "hasRole('" + KUNDE + "')")
                .antMatchers(AUTH_PATH + "/**").authenticated()
                .antMatchers("/admin/**").permitAll()
            .and()
            .httpBasic().realmName(REALM)
            .and()
            .csrf().disable()
            .headers().frameOptions().disable();
    }
}
