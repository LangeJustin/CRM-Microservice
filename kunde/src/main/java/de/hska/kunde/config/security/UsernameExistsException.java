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

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@ResponseStatus(value = BAD_REQUEST, reason = "Username exists")
class UsernameExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * RuntimeException, dass es den angegebenen Username bereits gibt.
     * @param username Der bereits vorhandene Username.
     */
    UsernameExistsException(String username) {
        super("Der Username " + username + " existiert bereits");
    }
}
