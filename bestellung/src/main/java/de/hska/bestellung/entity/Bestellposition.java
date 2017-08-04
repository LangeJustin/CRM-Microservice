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
package de.hska.bestellung.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Daten einer Bestellposition f&uuml;r die Anwendungslogik und zum Abspeichern.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bestellposition {
    @NotNull(message = "{bestellposition.artikelId.notNull}")
    private String artikelId;

    @NotNull(message = "{bestellposition.einzelpreis.notNull}")
    @DecimalMin(value = "0",
                inclusive = false,
                message = "{bestellposition.einzelpreis.DecimalMin}")
    private BigDecimal einzelpreis;

    @Min(value = 1, message = "{bestellposition.anzahl.min}")
    private int anzahl;
}
