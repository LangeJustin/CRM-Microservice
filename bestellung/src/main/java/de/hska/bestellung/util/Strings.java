/*
 * Copyright (C) 2014 - 2017 Juergen Zimmermann, Hochschule Karlsruhe
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
package de.hska.bestellung.util;

/**
 * Hilfsklasse f&uuml;r Strings
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
public final class Strings {
    private Strings() {
        throw new UnsupportedOperationException(
            "Das ist eine Utility-Klasse, die nicht instantiiert werden kann");
    }

    /**
     * Abfrage, ob ein String null oder der leere String ist.
     * @param s der zu &uuml;berpr&uuml;fende String
     * @return true, falls der zu &uuml;berpr&uuml;fende String null oder leer
     *         ist; false sonst.
     */
    public static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }
    
    /**
     * Abfrage, ob ein String null oder der leere String ist.
     * @param s der zu &uuml;berpr&uuml;fende String
     * @return true, falls der zu &uuml;berpr&uuml;fende String null oder leer
     *         ist; false sonst.
     */
    public static boolean isNotBlank(String s) {
        return s != null && !s.isEmpty();
    }
}
