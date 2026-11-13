# language: de
Funktionalität: Kunden suchen

  Szenario: Alle Kunden abrufen
    Wenn ich alle Kunden abrufe
    Dann erhalte ich eine Liste von Kunden
    Und die Antwort ist im JSON-Format

  Szenario: XML wird als Antwortformat nicht unterstützt
    Wenn ich alle Kunden im XML-Format abrufe
    Dann wird das Antwortformat nicht unterstützt

  Szenario: Nach einem Teil des Namens suchen, Groß-/Kleinschreibung egal
    Angenommen es existiert ein Kunde mit dem Namen "Kunde {eindeutig}"
    Wenn ich nach Kunden mit dem Namensteil "{EINDEUTIG}" suche
    Dann erhalte ich eine Liste von Kunden
    Und die Liste enthält den zuletzt angelegten Kunden
    Und alle Namen in der Liste enthalten "{EINDEUTIG}"

  Szenario: Nach Status suchen
    Angenommen es existiert ein Kunde mit dem Namen "Tom Mayer" und dem Status "locked"
    Wenn ich nach Kunden mit dem Status "locked" suche
    Dann erhalte ich eine Liste von Kunden
    Und die Liste enthält den zuletzt angelegten Kunden
    Und alle Kunden in der Liste haben den Status "locked"

  Szenario: Namensteil und Status gelten gemeinsam
    Angenommen es existiert ein Kunde mit dem Namen "Kunde {eindeutig}" und dem Status "active"
    Und es existiert ein Kunde mit dem Namen "Kunde {eindeutig}" und dem Status "locked"
    Wenn ich nach Kunden mit dem Namensteil "Kunde {eindeutig}" und dem Status "locked" suche
    Dann erhalte ich eine Liste von Kunden
    Und die Liste enthält genau 1 Kunden
    Und die Liste enthält den zuletzt angelegten Kunden

  Szenariogrundriss: Ungültige Suche – <fall>
    Wenn ich nach Kunden mit <kriterium> suche
    Dann wird die Anfrage als ungültig abgelehnt

    Beispiele:
      | fall                | kriterium                                  |
      | unbekannter Status  | dem Status "verschollen"                   |
      | leerer Namensteil   | dem Namensteil ""                          |
      | Namensteil zu lang  | dem Namensteil "{Text mit 101 Zeichen}"    |
