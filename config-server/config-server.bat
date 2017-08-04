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

REM Aufruf:   service-registry [start|stop|shutdown]

SETLOCAL

REM SET SCHEMA=https
SET SCHEMA=http

SET PORT=8888

SET VERBOSE=
REM SET VERBOSE=-v

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
CALL gradlew
GOTO :EOF


:SHUTDOWN
CALL C:\Zimmermann\Git\mingw64\bin\curl %VERBOSE% -d '' -u admin:p --tlsv1.2 -k %SCHEMA%://localhost:%PORT%/admin/shutdown

ENDLOCAL
