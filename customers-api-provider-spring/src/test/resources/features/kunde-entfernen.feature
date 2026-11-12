# language: de
Funktionalität: Kunden löschen

  Szenario: Existierenden Kunden löschen
    Angenommen es existiert ein Kunde mit dem Namen "Tom Mayer"
    Wenn ich den Kunden lösche
    Dann wird der Kunde gelöscht
    Wenn ich den Kunden abrufe
    Dann wird der Kunde nicht gefunden

  Szenario: Kunde existiert nicht
    Angenommen es existiert ein gelöschter Kunde
    Wenn ich den Kunden lösche
    Dann wird der Kunde nicht gefunden

  Szenario: Ungültige ID
    Wenn ich den Kunden mit der ID "gelbekatze" lösche
    Dann wird die Anfrage als ungültig abgelehnt
