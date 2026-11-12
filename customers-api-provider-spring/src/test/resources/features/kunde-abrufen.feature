# language: de
Funktionalität: Kunden abrufen

  Szenario: Existierenden Kunden abrufen
    Angenommen es existiert ein Kunde mit dem Namen "Tom Mayer", dem Geburtsdatum "1990-01-01" und dem Status "active"
    Wenn ich den Kunden abrufe
    Dann erhalte ich den Kunden
    Und die Antwort ist im JSON-Format
    Und die ID des Kunden ist "{id}"
    Und der Name des Kunden ist "Tom Mayer"
    Und das Geburtsdatum des Kunden ist "1990-01-01"
    Und der Status des Kunden ist "active"

  Szenario: Kunde existiert nicht
    Angenommen es existiert ein gelöschter Kunde
    Wenn ich den Kunden abrufe
    Dann wird der Kunde nicht gefunden

  Szenario: Ungültige ID
    Wenn ich den Kunden mit der ID "gelbekatze" abrufe
    Dann wird die Anfrage als ungültig abgelehnt
