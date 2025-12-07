# Platform Game Library

## Einbinden

Fügen Sie folgende Dependency in die pom.xml Ihres Maven-Projekts ein:

```xml

<dependency>
    <groupId>de.aschallenberg</groupId>
    <artifactId>platform-game-server</artifactId>
    <version>1.0.0</version>
</dependency>
```

Beachten Sie, dass die Library Java 21 verwendet.

## Anfangen das Spiel zu schreiben

Sie benötigen primär drei Dinge, um ein Spiel zu schreiben: eine main-Methode, eine (Haupt-)Klasse für Ihr Spiel und eine Konfigurationsdatei.

## config-Datei

Die config-Datei müssen Sie als `config.properties` in `src/main/resources` anlegen. Sie muss Folgendes beinhalten:

```
platform.host=<IP-Adresse der Plattform, zu der Sie sich verbinden möchten>
platform.port=<Port der Plattform, zu der Sie sich verbinden möchten>
platform.game.token=<Der Game-Implementation-Token für das Spiel, das Sie implementieren möchten>
```

## (Haupt-)Klasse
Sie müssen eine Klasse für Ihr Spiel anlegen, die von `de.aschallenberg.gamelibrary.game.Game` erbt. Sie werden einige Methoden implementieren müssen. Diese stellen die Schnittstelle zur Plattform und zu den Bots dar, die auf Ihren Spielservern spielen werden.

## main-Methode



Ihr Programm benötigt eine `main`-Methode. Es wird empfohlen, diese in einer eigenen Klasse `Main` anzulegen. Sie können sie jedoch auch an anderer Stelle implementieren. Zunächst muss die Konfigurationsdatei geladen werden. Dies geschieht mit:
```java
ConfigLoader.load(args);
```
Wichtig ist auch, dass Sie Ihre Hauptklasse registrieren. Das machen Sie mit:
```java
GameRegisty.setGameClass(<Deine Hauptklasse>.class);
```
**Danach** müssen Sie die Library dazu auffordern, eine WebSocket-Verbindung zur Plattform aufzubauen. Dies funktioniert mit dem Befehl:
```java
WebSocketInitiator.initConnection();
```
Ihre `main`-Methode sollte jetzt in etwa so aussehen:

```java
public static void main(String[] args) {
	ConfigLoader.load(args);
	GameRegisty.setGameClass(<Deine Hauptklasse>.class);
	WebSocketInitiator.initConnection();
}
```

Wenn Sie alle drei Komponenten haben und diese korrekt konfiguriert sind, sollte das Spiel beim Starten eine WebSocket-Verbindung zur Plattform aufbauen und sich dort registrieren. Sie sollten dann in der Konsole etwas sehen wie
„Successfully registered“. Das Spiel bzw. die Module und Versionen davon, die Sie unterstützen, haben jetzt eine Instanz mehr online und es wird automatisch zum Spielen bereitgestellt.
Jedoch wird Ihre Spiel-Implementierung noch zu Fehlern führen, da Sie diese bis jetzt noch nicht implementiert haben.

## Implementierung

Die von `Game` überschriebenen Methoden müssen Sie nun implementieren. Achten Sie darauf, dass Sie Ein- und Ausgabe-Klassen (also die Typen von Objekten, die Sie erwarten können und die Sie versenden) genau auf die Voraussetzungen des Spiels abstimmen. Andernfalls wird Ihr Code zu Fehlern führen.
