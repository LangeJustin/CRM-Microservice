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
package de.hska.bestellung.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
@Getter
@ToString
@SuppressWarnings({"unused", "WeakerAccess"})
public class Auditable {
    @Version
    private Integer version;
    
    @CreatedDate
    private LocalDateTime erzeugt;

    @LastModifiedDate
    private LocalDateTime aktualisiert;
    
    @SuppressWarnings("WeakerAccess")
    protected Auditable() {
        // Leerer Konstructor: Basisklasse fuer Entityklassen
    }
}
