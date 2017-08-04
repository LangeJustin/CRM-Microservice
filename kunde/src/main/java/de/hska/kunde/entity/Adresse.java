/*
 * Copyright (C) 2013 - 2017 Juergen Zimmermann, Hochschule Karlsruhe
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
package de.hska.kunde.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Adressdaten f&uuml;r die Anwendungslogik und zum Abspeichern.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
//@Getter, @Setter, @EqualsAndHashCode, @ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adresse {
    public static final String PLZ_PATTERN = "\\d{5}";
    public static final String PLZ_MSG = "{adresse.plz}";

    @NotNull(message = "{adresse.plz.notNull}")
    @Pattern(regexp = PLZ_PATTERN, message = PLZ_MSG)
    private String plz;

    @NotNull(message = "{adresse.ort.notNull}")
    private String ort;
}
