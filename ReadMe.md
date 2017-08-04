### **shopKunde**



### KundeController

![kundeController](https://github.com/LangeJustin/KundenverwaltungMicroservice/blob/master/modellierung/shopKunde.PNG)



### KundeService![kundeService](C:\Users\Justin\IdeaProjects\Kundenverwaltung -Microservices\modellierung\kundeService.PNG)



### KundeResource

![kundeResource](C:\Users\Justin\IdeaProjects\Kundenverwaltung -Microservices\modellierung\kundeResource.PNG)

### Benutzerpolicy (Use-Case-Diagramm)

![benutzerPolicy](C:\Users\Justin\IdeaProjects\Kundenverwaltung -Microservices\modellierung\benutzerPolicy.PNG)



## Vorbereitung für den Start der Server

### Internet-Verbindung

Eine _Internet-Verbindung_ muss vorhanden sein, damit die eigenen Microservices
über die IP-Adresse des Rechners aufgerufen werden können. Ansonsten würden die
Rechnernamen verwendet werden, wozu ein DNS-Server benötigt wird.

Bereits vorhandene Tests in JUnit 5 muss dem Projekt entfernt werden!!!

### IP-Adresse und hosts

Die IP-Adresse wird über das Kommando `ipconfig` ermittelt und liefert z.B.
folgende Ausgabe:

```TXT
    C:\>ipconfig

    Windows-IP-Konfiguration

    Ethernet-Adapter Ethernet:

       ...
       IPv4-Adresse  . . . . . . . . . . : 193.196.84.110
       ...
```

Die IP-Adresse muss dann in `C:\Windows\System32\drivers\etc\hosts` am
Dateiende eingetragen und abgespeichert werden. Dazu muss man
Administrator-Berechtigung haben.

```TXT
    193.196.84.110 localhost
```

### Proxy-Einstellung für Gradle

Die Proxy-Einstellung in gradle.properties muss richtig gesetzt sein. Dabei
muss die eigene IP-Adresse bei den Ausnahmen ("nonProxyHosts") eingetragen
sein, wozu man typischerweise Wildcards benutzt.

## Überblick: Start der Server (REIHENFOLGE!)

* MongoDB
* Mailserver
* Service Registry
* Config-Server
* API-Gateway
* Circuit Breaker Dashboard (_optional_)
* kunde
* bestellung

Die Server (außer MongoDB) sind jeweils in einem eigenen Gradle-Projekt.

## MongoDB starten und beenden

Durch Aufruf der .bat-Datei:

````CMD
    mongodb
````

bzw.

````CMD
    mongodb stop
````

## IntelliJ IDEA als Datenbankbrowser

Das Teilfenster _Mongo Explorer_ aktivieren und einen Doppelklick auf den
Eintrag `localhost` machen. Jetzt sieht man die Datenbank `hska` und kann
zu den Collections dieser Datenbank navigieren.

Eine Collection kann man wiederum durch einen Doppelklick inspizieren und
kann dabei die Ansicht zwischen _Tree_ und _Table_ variieren.

## Config-Server starten

Siehe `ReadMe.md` im Beispiel `config-server`.

## Übersetzung und Ausführung

### Start des Servers

In einer Eingabeaufforderung wird der Server mit der Möglichkeit für einen
_Restart_ gestartet, falls es geänderte Dateien gibt:

```CMD
    kunde
```

### Kontinuierliches Monitoring von Dateiänderungen

In einer zweiten Eingabeaufforderung überwachen, ob es Änderungen gibt, so dass
die Dateien für den Server neu bereitgestellt werden müssen; dazu gehören die
übersetzten .class-Dateien und auch Konfigurationsdateien. Damit nicht bei jeder
Änderung der Server neu gestartet wird und man ständig warten muss, gibt es eine
"Trigger-Datei". Wenn die Datei `restart.txt` im Verzeichnis
`src\main\resources` geändert wird, dann wird ein _Neustart des Servers_
ausgelöst und nur dann.

Die Eingabeaufforderung, um kontinuierlich geänderte Dateien für den Server
bereitzustellen, kann auch innerhalb der IDE geöffnet werden (z.B. als
_Terminal_ bei IntelliJ).

```CMD
    gradlew classes -t --build-cache
```

### Properties beim gestarteten Microservice _kunde_ überprüfen

Mit der URI `https://localhost:8444/admin/env` kann überprüft werden, ob der
Microservice _kunde_ die Properties vom Config-Server korrekt ausliest. Der
Response wird mit dem MIME-Type `application/vnd.spring-boot.actuator.v1+json`
zurückgegeben, welcher von einem Webbrowser i.a. nicht verstanden wird.

Man kann z.B. den _REST Client_ von _IntelliJ IDEA_ benutzen, der über
`Tools > Test RESTful Web Service` aktiviert werden kann:

* HTTP method: `GET`
* Host/port: `https://localhost:8444`
* Path: `/admin/env`

Die Ausgabe kann mit den beiden Icons _View as JSON_ und _Reformat response_
gut lesbar dargestellt werden. Die vom Config-Server bereitgestellten Properties
sind bei
`"configService:file:///C:/Users/.../IdeaProjects/config-server/git-repo/kunde-dev.properties"`
zu finden.

Analog können bei Microservice `bestellung` die Properties überprüft werden:

* Der Port ist von `8444` auf `8445` zu ändern.
* Bei `"configService:file:///C:/Users/...` steht `bestellung-dev.properties`

### Registrierung bei _Service Registry_ überprüfen

````URI
    http://localhost:8761/eureka/apps/kunde
````

### Herunterfahren in einer eigenen Eingabeaufforderung

```CMD
    kunde stop
```

### Tests

```CMD
    gradlew test --build-cache
```

### Codeanalyse durch FindBugs und CheckStyle einschl. Tests

```CMD
    gradlew check --build-cache
```

Die Tests können durch die Option `-x junitPlatformTest` ausgelassen ("eXclude")
werden.

### Codeanalyse durch SonarQube einschl. Tests

```CMD
    gradlew sonarqube --build-cache
```

Dazu muss der _Sonar-Server_ gestartet sein. Die Tests können durch die Option
`-x junitPlatformTest` ausgelassen ("eXclude") werden.

## Beispielhafte URI für einen _GET_-Request

```URI
    https://localhost:8444/000000000000000000000001
```

Dabei muss man BASIC-Authentifizierung mit z.B. Username `admin` und Passwort
`p` verwenden.

Um unnötigen Datentransfer zu vermeiden, kann der Header `If-None-Match` mit einer
Versionsnummer (z.B. `0`) als Wert verwendet werden. Falls die angeforderte Ressource
genau diese Versionsnummer hat, wird der Statuscode `Not Modified` bzw. `304` zurückgeliefert

Wenn das _API-Gateway_ gestartet ist, kann der Microservice _kunde_ auch darüber
aufgerufen werden, d.h. mit `https://localhost:8443/kunde`.

Mit nachfolgender URI kann man einen GET-Request absetzen, wobei Eureka und
OpenFeign genutzt wird:

```URI
    https://localhost:8445/100000000000000000000001
```

Mit nachfolgender URI kann man einen GET-Request absetzen, wobei Eureka,
OpenFeign und Hystrix genutzt wird:

```URI
    https://localhost:8445/bestellung/100000000000000000000010
```

## Beispiel für einen _POST_-Request

### URI für einen POST-Request

```URI
    https://localhost:8444
```

### Datensatz für einen POST-Request

```json
    {
        "nachname": "Test",
        "email": "theo@test.de",
        "newsletter": true,
        "geburtsdatum": [
            1918,
            1,
            31
        ],
        "waehrung": "EUR",
        "homepage": "https://www.test.de",
        "geschlecht": "W",
        "familienstand": "L",
        "interessen": [
            "R",
            "L"
        ],
        "adresse": {
            "plz": "12345",
            "ort": "Testort"
        },
        "account": {
            "username": "test",
            "password": "p"
        }
    }
```

### MIME-Type für einen POST-Request

Im _REST Client_ bei _Request_ und _Headers_ durch *+* den Eintrag `Content-Type` mit
dem Wert `application/json` setzen.

## Beispiel für einen _PATCH_-Request

### URI für einen PATCH-Request

```URI
    https://localhost:8444/000000000000000000000001
```

### Datensatz für einen PATCH-Request

```json
    [
        { "op": "replace", "path": "/nachname", "value": "Neuername" },
        { "op": "replace", "path": "/email", "value": "new.email@test.de" },
        { "op": "add", "path": "/interessen", "value": "R" },
        { "op": "remove", "path": "/interessen", "value": "L" }
    ]
```

Dazu _muss_ der Header `If-Match` mit der Versionsnummer des Kunden (z.B. `0`)
als Wert gesetzt werden.

### MIME-Type für einen PATCH-Request

Im _Headers_ bei `Content-Type` den MIME-Type `application/json-patch+json` setzen.

## Beispiel für einen _PUT_-Request mit einer multimedialen Datei

### RESTclient aufrufen

`gradlew restclient` aufrufen

### URI für einen PUT-Request

```URI
    https://localhost:8444/000000000000000000000001
```

### Auswahl der hochzuladenden Datei

Beim Karteireiter `Body` den Eintrag `Multipart Body` auswählen. Danach beim
Label `Add Part:` auf den Button `File` klicken und folgende Einstellungen
vornehmen:

* _Name_: `file`
* _File_: anklicken und die hochzuladende Datei auswählen, z.B. in
  `src\test\resources\rest` gibt es PNG-, JPG- und MP4-Dateien. Dabei erkennt
  RESTclient, welchen MIME-Type die ausgewählte Datei hat und schlägt den
  entsprechenden Eintrag für _Content type_ vor.

Danach auf `Add & Close` klicken und den PUT-Request schließlich abschicken. Der
Statuscode muss dann `200 OK` sein.

Nun kann man mit _MongoChef_ in der Datenbank `hska` in der Collection
`fs.files` die hochgeladene Datei mit dem Default-Viewer inspizieren.

Außerdem kann man in einem Webbrowser die hochgeladene Datei über
die URL `https://localhost:8444/000000000000000000000001/media`
herunterladen.

## Dashboard für Service Registry (Eureka)

```URI
    http://localhost:8761
```

## Anzeige im Circuit Breaker Dashboard (FEHLT)

```URI
    http://localhost:8762
```

Im Dashboard die URI für den zu beobachtenden Microservice eingeben, z.B.:

```URI
    http://admin:p@localhost:8081/admin/hystrix.stream
```

Hier wird BASIC-Authentifizierung mit dem Benutzernamen 'admin' und mit dem
Passwort 'p' verwendet.

### Beachte

* Erst **nach dem ersten Request** des zu beobachtenden Microservice ist eine
  Anzeige zu sehen.
* Mit dem Microservice wird über _HTTP_, und nicht über _HTTPS_ kommuniziert,
  weil man sonst für _Hystrix_ noch einen _Truststore_ konfigurieren müsste.
  Das würde den Umfang der Übungen sprengen und gehört in Vorlesungen mit den
  Schwerpunkten "IT-Sicherheit" und "Automatisierung von Geschäftsprozessen".

## Swagger

```URI
    https://localhost:8444/swagger-ui.html
```

## Vorhandene Mappings auflisten

D.h. welche Zuordnung gibt es zwischen URIs bzw. Pfaden, HTTP-Methoden und
Java-Methoden?

```URI
    https://localhost:8444/admin/mappings
```

## Vorhandene Spring-Beans auflisten

```URI
    https://localhost:8444/admin/beans
```

## Monitoring mit JConsole

In einer Eingabeaufforderung:

```CMD
    jconsole
```

Navigation, z.B.:

```TXT
    kunde.jar > MBeans > org.springframework.boot > Endpoint > beansEndpoint > Operations > getData()
```

Analog mit `requestMappingsEndpoint` statt `beansEndpoint`.

## Ausführen der JAR-Datei in einer Eingabeaufforderung

```CMD
    java -Xbootclasspath/p:alpn-boot.jar -jar build/libs/kunde.jar --spring.profiles.active=dev
```
