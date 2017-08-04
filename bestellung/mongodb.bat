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
REM along with this program.  If not, see <http://www.gnu.org/licenses/>

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
mongod --config C:\Zimmermann\mongodb\config.yml
goto EOF

:SHUTDOWN
SETLOCAL

SET AUTH=-u admin -p p --authenticationDatabase admin --norc admin
SET CERT_FILE=C:\Zimmermann\mongodb\mongodb.pem
SET CERT=--ssl --host localhost --sslAllowInvalidCertificates --sslPEMKeyFile %CERT_FILE%

mongo --eval db.shutdownServer() %AUTH% %CERT%

ENDLOCAL

:EOF
