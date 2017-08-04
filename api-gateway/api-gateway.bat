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

REM Aufruf:   api-gateway [start|stop|shutdown]

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
CALL gradlew --build-cache
GOTO :EOF

:SHUTDOWN
SETLOCAL

SET SCHEMA=https
REM SET SCHEMA=http

SET PORT=8443
REM SET PORT=8080

SET VERBOSE=
REM SET VERBOSE=-v

CALL C:\Zimmermann\Git\mingw64\bin\curl %VERBOSE% -d '' -u admin:p --tlsv1.2 -k %SCHEMA%://localhost:%PORT%/admin/shutdown
ENDLOCAL
