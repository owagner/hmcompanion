HMCompanion V0.22
=================
Geschrieben von Oliver Wagner <owagner@vapor.com>

1. Was ist HMCompanion?
-----------------------
HMCompanion ist eine Middleware, welche ich gebaut habe, um mein Homematic-System via CCU besser
in meine Hausautomation integrieren zu können. Es handelt sich um einen Server-Prozess, der
auf der einen Seite mit der CCU kommuniziert und auf anderen Seite mittels einfacher 
TCP-Textkommandos ansteuerbar ist. Dies ermöglicht eine sehr einfache Integration in
PHP, Bash-Scripte etc. Desweiteren arbeitet HMCompanion auch als sehr effizienter Cache
für den Status aller Channel auf der CCU, so daß z.B. für Visualisierung auf diese
Daten ohne Verzögerung zugegriffen werden kann.

HMCompanion ist in Java geschrieben und funktioniert auf jeder Plattform mit mindestens
einer Java-1.5-JRE (also NICHT direkt auf der CCU selbst)


2. Grobe Struktur der CCU-Software
-----------------------------------
Um die Funktionsweise von HMCompanion zu verstehen, ist es sinnvoll, einen groben Überblick
über die Struktur der CCU-Software zu haben.

Die CCU-Software besteht aus mehreren Schichten:

Auf der untersten Ebene liegen drei Daemon-Prozesse, welche die Kommunikation mit den 
Hardwarekomponenten vornehmen:

rfd    -- mit den BidCoS-Funk-Komponenten (direkt oder über HM-CFG-LAN) (Port 2001)
hs485d -- mit den HomeMatic-Wired-Komponenten (Port 2000)
pfmd   -- mit der Hardware der CCU selbst (Port 2002)

Der CUxD ist, wenn installiert, ein ebensolcher Daemon (Port 8701)

Diese Prozesse besitzen jeweils eine xmlrpc-Schnittstelle, über welche sie angesteuert werden
können und über welche Ereignisse (z.B. Status-Änderungen) gemeldet werden. Auf dieser Ebene existieren
Geräte und Kanäle nur als Adressen.

       +--------+
       | WebUI  |
       +--------+
           | JSON-RPC
       +---------+
       |Webserver|
       +---------+
           | HMScript (und xmlrpc)       
       +--------+
       |ReGa HSS|
       +--------+________
      / xml |    \       \
     /  rpc |     \       \
+---+   +-------+  +----+  +----+
|rfd|   |hs485d |  |pfmd|  |CuXD|
+---|   +-------+  +----+  +----+
 | (TCP/IP)
+----------+
|HM-CFG-LAN|
+----------+

Über diesen Prozessen liegt die von EQ-3 als "Logikschicht" bezeichnete Ebene (ReGa, von "Residential Gateway").
Diese managt die Konfiguration der Hardwarekomponenten, führt WebUI-Programme aus, handhabt die Abarbeitung 
von HMScript und ähnliches. Auf dieser Ebene werden auch die Namen und Bezeichnungen der Geräte verwaltet
(in der "homematic.regadom"-Datenbank).

Diese Logikschicht wiederum besitzt zwei Schnittstellen: Zum einen die Möglichkeit, über eine TCL-Bibliothek
HMScript-Befehle auszuführen zu lassen, zum anderen ist über einen Webserver damit ein JSON-RPC-API
realisiert, welches wiederum von der WebUI verwendet wird. Das WebUI wiederum ist eine browserseitige
AJAX-Applikation, welche die Daten der ReGa visualisiert und die ReGa (via JSON-RPC) ansteuert. 

Die JSON-API-Befehle kann man mittels http://homematic-ip/api/homematic.cgi einsehen. Wer telnet-Zugang auf
die CCU hat, kann den TCL-Quelltext der einzelnen über das API verfügbaren Methoden im Verzeichnis /www/api/methods
einsehen. Diese lassen auch interessante Rückschlüsse auf interne HMScript- und xmlrpc-Aufrufe zu.

Eine Dokumentation der XML-RPC-Schnittstelle bfindet sich auf der Hersteller-Website unter
http://www.eq-3.de/software.html


3. Kommunikation HMCompanion und CCU
------------------------------------
HMCompanion kommuniziert auf zwei Ebenen mit der CCU:

a) Es benutzt xmlrpc, um direkt Befehle an rfd, hs485, pfmd und -- falls installiert, CUxD -- zu schicken.
a.1) Es meldet sich mittels des xmlrpc-Befehls "init" als zusätzliche Logikschicht an den Daemonen an, um 
von diesen über Ereignisse informiert zu werden. Dies macht die Installation von tcpdump o.ä. auf der CCU 
selbst unnötig.

Die xmlrpc-Kommunikation geht über die entsprechenden Schnittstellen der drei Serverdienste. Es wird das
proprietäre binärkodierte Format der CCU-Komponenten verwendet.

b) Es benutzt HMScript, um von der CCU eine Liste aller Geräte mit Namen zu erhalten, damit diese dann
mittels GET, SET etc. per Namen angesprochen werden können.  

HMScript wird mittels eines HTTP POST an die URL http://<homematic>:8181/tclrega.exe ausgeführt.


4. Benutzung von HMCompanion
----------------------------
HMCompanion wird mit folgendem Befehl gestartet:

java -jar hmcompanion.jar <ip oder hostname der CCU> -server <optionaler Port, ansonsten 6770> <optionales password>

HMCompanion registriert sich dann zuerst per xmlrpc-API bei den vier Daemon-Prozessen, um Events zu erhalten,
fordert danach per HMScript die ReGa-Geräteliste an und wartet dann auf TCP-Verbindungen zum
angegebenen Port.  

Zum Beispiel mittels 

	telnet localhost 6770
	
kann man sich dann mit dem HMCompanion verbinden und Befehle abschicken. Mittels "HELP" erhält man
eine Auflistung aller verfügbaren Befehle. Ein Beispiel: Aktuellen Status eines Channel-Attributes abrufen:

GET Wetterstation

Attribut setzen:

SET "Licht Kellerflur" STATE on

Bei SET/GET können jeweils sowohl die BidCoS-Adressen der Channel als auch die im WebUI zugewiesene
Namen verwendet werden. Bei Set/SetParam können auch Wildcards verwendet werden, z.B.

SET "*Licht*" STATE on

setzt alle Komponenten mit Namen, die "Licht" enthalten, auf ein. Das Auslesen von Attributen von
mehreren Geräten ist mittels des Befehls "MGET" möglich.

Da in den xmlrpc-Requests die Datentypen der Parameter eine Rolle spielen, rät HMCompanion anhand des
Formats:

  on/true, off/false -> Boolean
  Integer-Zahl (nur 0-9) -> Integer
  Dezimalzahl (d.h. mit Dezimalpunkt) -> Dezimalzahl
  alles andere -> String
  
Dies bedeutet, dass man Parameter, die als Dezimalzahl erwartet werden (z.B. LEVEL), immer auch als
Dezimalzahl übergeben muss, also "0.0" statt "0" und "1.0" statt "1", ansonsten wird der Request vom
jeweiligen Daemon ignoriert.

HMCompanion speichert den aktuellen Stand aller Channel-Attribute beim Beenden in der Datei "hmc.cache"
und lädt diesen beim Start wieder. Dies dient insbesondere für Geräte, die nur alle 24h o.ä. ein Update
schicken.

Wird HMCompanion auf einem Server-System betrieben, macht es Sinn, die Speicherzuteilung der JavaVM
mittels "-Xmx64M" auf 64MB zu beschränken:

	java -Xmx64M -jar hmcompanion.jar <ip oder hostname der CCU> -server

Hat ein System eine aktivierte, aber unvollständige IPv6-Konfiguration, kann es nötig sein, den Parameter 
"-Djava.net.preferIPv4Stack=true" zu setzen, um zu verhindern, dass eine fehlerhafte Callback-Adresse
ermittelt wird.  


5. Events
---------
Es besteht die Möglichkeit, Events zu definieren, um beim Auftreten von bestimmten Zuständsänderungen Scripte
auszuführen. Diese werden in der Form

  Kanalname_oder_adresse.Attribut.Wert=script
  
definiert, zum Beispiel

  BidCoS-Wir:10.PRESS_SHORT.true=/pfad/zum/skript.sh
  
Diese Events können entweder als Property beim Start von HMC mitgegeben (-DBidCoS-Wir:10.PRESS_SHORT.true=/pfad/zum/skript.sh)
oder in der optionalen Datei "hmcompanion.cfg" definiert werden.


6. Interface zu Graphite/Carbon
-------------------------------
HMC kann sämtliche Statusänderungen auch als Metric an einen carbon-Server des Graphite-Pakets (http://graphite.wikidot.com/)
schicken. Hier muss die Netzwerkadresse des Carbon-Servers mittels der Property

-Dhmc.carbon.host=<hostname oder IP>

festgelegt werden. Die Namen der Metriken werden aus dem ReGaHSS-Gerätenamen und dem Attributnamen gebildet, mit dem
zusätzlichen Präfix, welches per "-Dhmc.carbon.prefix=" festgelegt ist (Default ist "hm."). Dabei wird alles in
Kleinbuchstaben verwandelt und alle unzulässigen Zeichen werden in "_" umgewandelt. Boolsche Werte werden dabei in numerisch
0 bzw. 1 umgesetzt. 

Beispiel: Das Attribut "RAINING" des Gerätes "Wetterstation Aussen" ergibt den Metriknamen

hm.wetterstation_aussen.raining 


7. History
----------
V0.2 - erste funktionale Version

V0.3 - verwendet XMLRPC-BIN auch für eingehende Requests, kein Apache ws-xmlrpc mehr nötig
     - Dump-Format für strukturierte Antworten verbessert
     - Serialization-Fehler beim Cache-Schreiben behoben
     - Neues Kommando "CGET", um alle Attribute eines Kanals in einem für Cacti lesbarem Format
       auszugeben

V0.4 - SET funktioniert nun auch wieder mit reinen BidCoS-Adressen anstatt nur mit ReGa-Namen
     - neuer Befehl "SETPARAM", um einen einzelnen Konfigurationsparameter via der xmlrpc-Methode
       "putParamset" zu setzen. Beispiel z.B. zum Setzen eines Temperaturreglers auf Automatik:
       SETPARAM GEQ00xxxxx:2 MASTER MODE_TEMPERATUR_REGULATOR 1

V0.5 - SET und SETPARAM akzeptieren nun Wildcards (*) für Channelnamen. Heissen z.B. alle
       Thermostat-Steuerkanäle "Thermostat <Raum> 1", kann man mit
       SETPARAM "Thermostat * 1" MASTER MODE_TEMPERATUR_REGULATOR 1
       alle gleichzeitig auf Modus "Auto" setzen.

V0.6 - Watchdog: Wird nun mehr als 180s kein Callback empfangen, werden die init-Requests erneut
       geschickt (z.B. nach einem Reboot der CCU)
     - Neuer Befehl "HMSet <variable> <wert>" als Shortcut zum Setzen einer HMScript-Variable
     - Neuer Befehl "HMGet <variable1> <variable2...>" als Shortcut zum Lesen einer HMScript-Variable
     - Neuer Befehl "HMRun <programm1> <programm2...>" als Shortcut zum Starten von HMScript-Programmen

V0.7 - Mittels "HMGet -timestamp variable" (Textform) oder "HMGet -timestampts variable" (Sekunden seit 1970)
       kann nur die Last-Modification-Timestamp einer Variable abgefragt werden
     - "HMSet" benutzt nun .State(v) statt .Variable(v), damit eventuelle Events auf den Systemvariablen ausgelöst werden
     - Neues Kommando MGET um ein Attribut von einer Menge von Geräten abzufragen, z.B. "MGET PIR* motion". 
       Das Ausgabeformat ist "Gerätename:Wert"

V0.8 - Hinweis auf "undokumentierte Schnittstellen" entfernt, da nun durch EQ-3 offengelegt
     - Beim Beenden von HMC wird nun ein de-init an die xmlrpc-Server geschickt
     - Neuer Befehl "STATS" um Statistiken über von den xmlrpc-Servern erhaltene Nachrichten im Cacti-Format zu bekommen
     - QUIT hat nun eine Option "-exit", mit der HMC beendet werden kann
     
V0.9 - Clientverbindungen setzen nun SO_KEEPALIVE
     - REQ unterstützt nun auch die Angabe von Arrays und Strukturen mit der [ Array ] bzw. { key1 value1 key2 value2 }
       Notation
     - GUI-Betriebsmodus zum Setzen der Interface/Roaming-Parameter (einfach ohne Parameter starten)
       
V0.10 - GUI-Modus: Button zum schnellen Refresh der RSSI-Informationen hinzugefügt
      - GUI-Modus: Schnittstellen werden nun in der CCU-Reihenfolge angezeigt, mit dem Default-Interface zuerst
      
V0.11 - GUI-Modus: Buttons zum Setzen aller Devices auf Roaming Off, auf Default == Interface mit dem besten RSSI-Wert
        und Roaming On für alle Fernbedienungen
        
V0.12 - Synchronisationsproblem beim Request-Handling konnte dazu führen, das mehrere schnell hintereinander
        abgesendete "SET" (o.ä.)-Befehle zu einem Deadlock führten
      - Server-Sockets werden nun alle mit SO_REUSEADDR generiert, um schnellen Neustart möglich zu machen
      
V0.13 - Client-Verbindungen benutzen nun unabhängig vom Systemzeichensatz immer UTF-8
        
V0.14 - Optionales Authentifizerungstoken für Client-Verbindungen: Wird HMC mit "homematicip -server 6770 <password>" gestartet,
        werden weitere Befehle nur nach Authentifizerung mit "AUTH <password>" angenommen.
        
V0.15 - Möglichkeit, die lokale IP-Adresse für XML-RPC-Callbacks explizit zu setzen, falls diese falsch ermittelt wird
        (-Dhmc.localhost=192.168.47.11)
         
V0.16 - Neue Option "-state" beim HMGet, um Alarmvariablen korrekt abzufragen (mit obj.State() statt obj.Variable())
              
V0.17 - RSSI GUI: Ist eine Interface-Description gesetzt (geht auf der CCU nur durch manuelle Ergänzung der rfd.conf durch Description=),
        wird diese im Spaltenkopf angezeigt
      - RSSI GUI: Der Checkboxbereich ist nun grün für das Interface, welches die beste Empfangs-RSSI-Werte für das Gerät hat
      - RSSI GUI: Ist das ausgewählte Default-Interface NICHT das mit dem besten RSSI-Empfangs-Wert, wird der Checkbox-Bereich rot dargestellt
      - RSSI GUI: Hinter dem Gerätetyp steht nun die Firmware-Revision

V0.18 - unterstützt nun auch CUxD (ab V0.564)
      - Exec-Patch von "sircus" integriert
      - Properties können nun in einer Datei "hmcompanion.cfg" definiert werden
      
V0.19 - experimentelle Funktion zum Setzen von Attributen im GUI-Modus

V0.20 - Versionen ab 0.18 starteten nicht mehr korrekt, wenn kein CUxD auf der CCU installiert war.

V0.21 - Interface zu Graphite/Carbon hinzugefügt

V0.22 - Fix für CCU2 (implement nun system.listMethods)


    
                                
