# Platform Game Library

## Einbinden

Füge folgende dependency in die pom.xml deines Maven Projekts hinzu:

```xml

<dependency>
    <groupId>de.aschallenberg</groupId>
    <artifactId>platform-game-server</artifactId>
    <version>1.0.0</version>
</dependency>
```

Beachte, dass die Library Java 21 benutzt.

## Anfangen das Spiel zu schreiben

Du brauchst primär drei Dinge um ein Spiel zu schreiben: eine main-Methode, eine (Haupt-)Klasse für dein Spiel und eine
config-Datei.

## config-Datei

Die config-Datei musst du als `config.properties` in `src/main/resources` anlegen. Sie muss folgendes beinhalten:

```
platform.host=<IP-Adresse der Platform zu der du dich verbinden möchtest>
platform.port=<Port der Plattform zu der du dich verbinden möchtest>
platform.game.token=<Der Game-Implementation-Token, für das Spiel, was du implementieren möchtest>
```

## (Haupt-)Klasse

Du musst eine Klasse für deinen Bot anlegen, die von `de.aschallenberg.gamelibrary.game.Game` erben muss. Du wirst
einige Methoden implementieren müssen. Sie sind die Schnittstelle zur Plattform und zu den Bots, die auf deinen
Spielservern spielen werden.

## main-Methode

Dein Programm braucht eine main-Methode. Ich empfehle, diese in einer eigenen Klasse `Main` anzulegen, du kannst sie
aber auch überall anders implementieren. Wichtig ist, dass du deine Hauptklasse registrierst. Das machst du mit
`GameRegisty.setGameClass(<Deine Hauptklasse>.class)`. **Danach** musst du die Library dazu auffordern, eine WebSocket-
Verbindung zur Plattform aufzubauen. Dies funktioniert mit dem Befehl `WebSocketInitiator.initConnection()`.
Deine main- Methode sollte jetzt in etwa so aussehen:

```java
public static void main(String[] args) {
	GameRegisty.setGameClass( < Deine Hauptklasse >.class);
	WebSocketInitiator.initConnection();
}
```

Wenn du alle drei Komponenten hast und korrekt konfiguriert hast, dann sollte das Spiel beim Starten eine WebSocket-
Verbindung zur Plattform aufbauen und sich dort registrieren. Du solltest dann in der Konsole etwas sehen wie
"Successfully registered". Das Spiel bzw, die Module und Versionen davon, die du unterstützt, haben jetzt eine Instanz
mehr online und du es wird automatisch zum Spielen bereitgestellt. Jedoch wird deine Spiel-Implementierung noch zu
Fehlern führen, da du sie bis jetzt noch nicht implementiert hast.

## Implementierung

Die von `Game` überschriebenen Methoden musst du jetzt implementieren. Achte darauf, dass du Ein- und Ausgabe-Klassen
(also die Typen von Objekten, die du erwarten kannst und die du versendest) genau auf die Voraussetzungen des Spiels
abstimmst. Ansonsten wird dein Code zu Fehlern führen.