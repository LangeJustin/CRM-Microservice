# Hinweise zum Programmierbeispiel

<Juergen.Zimmermann@HS-Karlsruhe.de>

> Diese Datei ist in Markdown geschrieben und kann z.B. mit IntelliJ IDEA
> oder NetBeans gelesen werden. Näheres zu Markdown gibt es in einem
> [Wiki](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet)

## Editieren und Initialisieren

Ein Config-Server kann Properties aus einem Git-Reppository bereitstellen.
Diese Properties werden je Microservice in einer Properties-Datei definiert.
YML-Dateien sind leider nicht möglich.

Im  Unterverzeichnis `git-repo` muss man die Dateien `kunde-dev.properties` und
`bestellung-dev.properties` (für Beispiel 3) editieren und dort jeweils beim
Schlüssel `uri` den Pfad korrekt setzen.

Jetzt muss man in einer Eingabeaufforderung die nachfolgenden Git-Kommandos
aufrufen, damit ein Git-Repository im Unterverzeichnis `git-repo` initialisiert
wird.

```
    cd git-repo
    git init
    git add .
    git commit -m "Initiale Version der Properties-Dateien"
    cd ..
```

Das Git-Repository kann nun Properties für die Microservices _kunde_ und
_bestellung_ (Beispiel 3) bereitstellen.

## Übersetzung und Start des Config-Servers

In einer Eingabeaufforderung `config-server` aufrufen.

## Überprüfung der bereitgestellten Properties

In einem Webbrowser `http://localhost:8888/kunde/dev` aufrufen, um die
Properties auszugeben, die für den Microservice _kunde_ mit dem Profile _dev_
bereitgestellt werden.

Es können auch folgende URIs verwendet werden:

* `http://localhost:8888/kunde-dev.yml` für die Ausgabe der Properties im Format
   YML oder
* `http://localhost:8888/kunde/dev/master` für die Ausgabe  der Properties aus
  dem Default-Branch _master_ des Git-Repositories. Dass es sich um den Branch
  _master_ handelt, sieht man dadurch, dass bei `label` jetzt der Wert `master`
  steht; beim Request mit `http://localhost:8888/kunde/dev` stand hier `null`.
  Das Git-Repository im Unterverzeichnis `git-repo` hat übrigens nur den Branch
  _master_.

## Microservice _kunde_ starten

Den Microservice _kunde_ starten: siehe zugehörige Datei `ReadMe.md`.

## Properties beim Microservice _kunde_ überprüfen

Mit der URI `http://localhost:8081/admin/env` kann überprüft werden, ob der
Microservice _kunde_ die Properties vom Config-Server korrekt ausliest. Der
Response wird mit dem MIME-Type `application/vnd.spring-boot.actuator.v1+json`
zurückgegeben, welcher von einem Webbrowser i.a. nicht verstanden wird.

Man kann z.B. den _REST Client_ von _IntelliJ IDEA_ benutzen, der über
`Tools > Test RESTful Web Service` aktiviert werden kann:

* HTTP method: `GET`
* Host/port: `http://localhost:8081`
* Path: `/admin/env`

Die Ausgabe kann mit den beiden Icons _View as JSON_ und _Reformat response_
gut lesbar dargestellt werden. Die vom Config-Server bereitgestellten Properties
sind bei
`"configService:file:///C:/Users/.../IdeaProjects/config-server/git-repo/kunde-dev.properties"`
zu finden.

## Properties für einen eigenen Microservice

Im Unterverzeichnis `git-repo` eine Properties-Datei, z.B. `buch-dev.properties`
anlegen, falls der eigene Microservice _buch_ heißt. Danach in einer
Eingabeaufforderung die folgenden Git-Kommandos eingeben:

```
    cd git-repo
    git add .
    git commit -m "Properties fuer den eigenen Microservice"
    cd ..
```
