@ECHO OFF

REM
REM
REM This program is free software: you can redistribute it and/or modify
REM it under the terms of the GNU General Public License as published by
REM the Free Software Foundation, either version 3 of the License, or
REM (at your option) any later version.
REM
REM This program is distributed in the hope that it will be useful,
REM but WITHOUT ANY WARRANTY; without even the implied warranty of
REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
REM GNU General Public License for more details.
REM
REM You should have received a copy of the GNU General Public License
REM along with this program.  If not, see <http://www.gnu.org/licenses/>.

REM Aufruf:   bestellung [start|stop|shutdown]

TITLE %0

REM ~ entfernt "" um die Aufrufargumente
if "%~1" == "" (
    goto START
) else if "%~1" == "start" (
    goto START
) else if "%~1" == "stop" (
    goto SHUTDOWN
) else if "%~1" == "shutdown" (
    goto SHUTDOWN
)

:START
SETLOCAL
SET TRUSTSTORE=-Djavax.net.ssl.trustStore=src/test/resources/truststore.p12
SET TRUSTSTORE_PASSWORD=-Djavax.net.ssl.trustStorePassword=zimmermann
CALL gradlew --build-cache %TRUSTSTORE% %TRUSTSTORE_PASSWORD%
ENDLOCAL
GOTO :EOF

:SHUTDOWN
SETLOCAL

REM SET SCHEMA=https
SET SCHEMA=http

REM SET PORT=8444
SET PORT=8082

REM SET VERBOSE=
SET VERBOSE=-v

CALL C:\Zimmermann\Git\mingw64\bin\curl %VERBOSE% -d '' -u admin:p --tlsv1.2 -k %SCHEMA%://localhost:%PORT%/admin/shutdown
ENDLOCAL
