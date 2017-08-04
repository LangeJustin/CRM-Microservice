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

import java.util.Collection;

import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.toList;

/**
 * Controller-Objekt gem&auml;&szlig; SpringMVC f&uuml;r Requests zu /kunde.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@RestController
@RequestMapping("/auth")
class AuthController {
    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/rollen")
    @SuppressWarnings("unused")
    Collection<String> findEigeneRollen(Authentication authentication) {
        final Account account = (Account) authentication.getPrincipal();
        final val username = account.getUsername();
        return authService.loadUserByUsername(username)
                          .getAuthorities()
                          .stream()
                          .map(GrantedAuthority::getAuthority)
                          .collect(toList());
    }
}
