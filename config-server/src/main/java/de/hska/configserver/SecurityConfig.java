/*
 * Copyright (C) 2017 Juergen Zimmermann, Hochschule Karlsruhe
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

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.Locale;

/**
 * Security-Konfiguration
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Configuration
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String REALM;
    
    static {
        // Name der REALM = Name des Parent-Package in Grossbuchstaben,
        // z.B. CONFIGSERVER
        final val packageName = SecurityConfig.class.getPackage().getName();
        final val parentPackageName =
            packageName.substring(0, packageName.lastIndexOf('.'));
        REALM = parentPackageName.substring(parentPackageName.lastIndexOf('.')
                                            + 1)
                                 .toUpperCase(Locale.getDefault());
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().permitAll()
                .antMatchers("/admin/**").permitAll()
            .and()
            .httpBasic().realmName(REALM)
            .and()
            .csrf().disable()
            .headers().frameOptions().disable();
    }
    
    /**
     * Einen User "admin" mit Passwort "p" bereitstellen.
     * @param auth Injiziertes Objekt der Klasse AuthenticationManagerBuilder
     * @throws Exception allgemeine Exception gem&auml;&szlig; der Basis-Klasse
     *         WebSecurityConfigurerAdapter
     */
    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth)
                throws Exception {
        auth.inMemoryAuthentication()
                .withUser("admin")
                .password("p")
                .roles("ADMIN", "ACTUATOR");
    }
}
