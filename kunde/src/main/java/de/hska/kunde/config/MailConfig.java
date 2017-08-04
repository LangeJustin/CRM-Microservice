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

import java.util.Locale;
import java.util.Properties;

import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static java.lang.Boolean.FALSE;

/**
 * Spring-Konfiguration f&uuml;r den SMTP-Zugriff auf einen Mailserver
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">
 *         J&uuml;rgen Zimmermann</a>
 */
interface MailConfig {
    /**
     * Spring-Bean f&uuml;r das Verschicken von Emails.
     * @param host Hostname des Mailservers
     * @param port Port des Mailservers
     * @return Das konfigurierte Objekt, um Emails zu verschicken.
     */
    @Bean
    @Description("JavaMailSender")
    default JavaMailSender javaMailSender(
        @Value("${mail.host:localhost}") String host,
        @Value("${mail.port:25000}") int port) {
        // set-Methoden sind im Interface JavaMailSender nicht deklariert
        final val javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPort(port);

        // https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/...
        // ...package-summary.html
        final val falseStr = FALSE.toString().toLowerCase(Locale.getDefault());
        final val properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", falseStr);
        properties.setProperty("mail.smtp.starttls.enable", falseStr);
        properties.setProperty("mail.debug", falseStr);
        javaMailSender.setJavaMailProperties(properties);

        return javaMailSender;
    }
}
