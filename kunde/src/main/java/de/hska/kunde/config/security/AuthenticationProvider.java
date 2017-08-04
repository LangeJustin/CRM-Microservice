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
package de.hska.kunde.config.security;

import de.hska.kunde.config.security.AuthService.NotAuthenticatedException;
import org.springframework.security.authentication
       .UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao
       .AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * Provider-Klasse f&uuml;r Spring Security, um Benutzerkennungen aus MongoDB
 * zu verwalten.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Service
public class AuthenticationProvider
             extends AbstractUserDetailsAuthenticationProvider {
    private final AuthService service;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Konstruktor mit injizierten Objekten
     * @param service Der AccountService, um Accounts zu verwalten.
     * @param passwordEncoder Injiziertes Objekt, um mit bcrypt zu
     *                        verschl&uuml;sseln
     */
    public AuthenticationProvider(AuthService service,
                                  BCryptPasswordEncoder passwordEncoder) {
        this.service = service;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * &Uuml;berpr&uuml;fung vom eingegebenen Username
     * @param username Eingegebener Username
     * @param authentication Objekt mit dem eingegebenen Username und Password
     * @return Objekt mit Username und Password aus der MongoDB
     */
    @Override
    protected UserDetails retrieveUser(String username,
                           UsernamePasswordAuthenticationToken authentication) {
        return service.loadUserByUsername(username);
    }
    
    /**
     * &Uuml;berpr&uuml;fung vom eingegebenen Password
     * @param userDetails Objekt mit Username und Password aus der MongoDB
     * @param authentication Objekt mit dem eingegebenen Username und Password
     * @throws AuthenticationException bei falschem Passwort
     */
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                           UsernamePasswordAuthenticationToken authentication) {
        final CharSequence credentials = (CharSequence)
                                         authentication.getCredentials();
        if (!passwordEncoder.matches(credentials, userDetails.getPassword())) {
            throw new NotAuthenticatedException();
        }
    }
}
