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
package de.hska.mailserver;

// https://github.com/voodoodyne/subethasmtp/blob/master/SimpleExample.md

/*
    telnet localhost 25000
    mail from: sender@test.de
    rcpt to: empfaenger@test.de
    data
    blabla
    .
    quit
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.SMTPServer;

import static java.lang.System.out;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.joining;

public class Mailserver {
    private static final int PORT = 25000;
    
    private Mailserver() {
        // Main-Klasse fuer Mailserver
    }

    @SuppressWarnings("checkstyle:UncommentedMain")
    public static void main(String[] args) {
        final MessageHandlerFactory factory = new MessageHandlerFactoryImpl();
        final SMTPServer smtpServer = new SMTPServer(factory);
        smtpServer.setHostName("localhost");
        smtpServer.setPort(PORT);
        smtpServer.setSoftwareName(Mailserver.class
                                             .getPackage()
                                             .getSpecificationVendor()
                                   + ", "
                                   + Mailserver.class
                                               .getPackage()
                                               .getSpecificationVersion());
        smtpServer.start();
    }
    
    static class MessageHandlerFactoryImpl implements MessageHandlerFactory {
        @Override
        public MessageHandler create(MessageContext ctx) {
            return new MessageHandlerImpl(ctx);
        }
    }
    
    static class MessageHandlerImpl implements MessageHandler {
        @SuppressWarnings("PMD.UnusedFormalParameter")
        MessageHandlerImpl(@SuppressWarnings("unused") MessageContext ctx) {
            // nichts zu initialisieren
        }

        @Override
        public void from(String from) throws RejectException {
            // nichts notwendig bzgl. Absender
        }

        @Override
        public void recipient(String recipient) throws RejectException {
            // nichts notwendig bzgl. Empfaenger
        }

        @Override
        public void data(InputStream data) throws IOException {
            out.println("MAIL BEGIN >>>");
            out.println(inputStreamToString(data));
            out.println("<<< MAIL END\n");
        }

        @Override
        public void done() {
            // ggf. Abschlussmassnahmen
        }

        private String inputStreamToString(InputStream is) throws IOException {
            try (BufferedReader buffer =
                    new BufferedReader(
                        new InputStreamReader(is, defaultCharset()))) {
                return buffer.lines().collect(joining("\n"));
            }
        }
    }
}
