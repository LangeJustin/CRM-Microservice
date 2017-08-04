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

import lombok.val;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Service-Klasse, um Accounts zu suchen.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Service
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class AuthService implements UserDetailsService {
    private final AccountRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;
    
    AuthService(AccountRepository repo,
                BCryptPasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Zu einem gegebenen Username wird der zugeh&ouml;rige Account gesucht.
     * @param username Username des gesuchten Accounts
     * @return Der gesuchte Account
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        return repo.findByUsername(username)
                   .orElseThrow(NotAuthenticatedException::new);
    }

    /**
     * Einen neuen Account anlegen
     * @param kundeAccount Der neue Account
     */    
    public void save(de.hska.kunde.entity.Account kundeAccount) {
        repo.findByUsername(kundeAccount.getUsername())
            .ifPresent(account -> {
            throw new UsernameExistsException(kundeAccount.getUsername());
        });

        // Die Account-Informationen des Kunden in Account-Informationen
        // fuer die Security-Komponente transformieren
        final val password = kundeAccount.getPassword();
        final val passwordEncoded = passwordEncoder.encode(password);
        final val rollen = kundeAccount.getRollen()
                                       .stream()
                                       .map(SimpleGrantedAuthority::new)
                                       .collect(toList());
        final val account = new Account(null, kundeAccount.getUsername(),
                                        passwordEncoded, rollen);

        repo.save(account);
    }
    
    /**
     * Zu werfende Exception, falls der Benutzername oder das Passwort nicht
     * korrekt ist. Diese Exception ist mit dem HTTP-Statuscode 401 gekoppelt.
     */
    @ResponseStatus(value = UNAUTHORIZED)
    static class NotAuthenticatedException extends UsernameNotFoundException {
        private static final long serialVersionUID = 1L;
        /**
         * Konstruktor, der aus Sicherheitsgr&uuml;nden keine Fehlermeldung
         * kapselt.
         */
        NotAuthenticatedException() {
            super("");
        }
    }
}
