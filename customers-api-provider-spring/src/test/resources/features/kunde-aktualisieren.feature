# language: de
Funktionalität: Kunden ändern

  Szenario: Nur der Status wird geändert
    Angenommen es existiert ein Kunde mit dem Namen "Tom Mayer", dem Geburtsdatum "1990-01-01" und dem Status "active"
    Und die Änderung setzt den Status auf "locked"
    Wenn ich den Kunden ändere
    Dann wird der Kunde geändert
    Wenn ich den Kunden abrufe
    Dann erhalte ich den Kunden
    Und der Name des Kunden ist "Tom Mayer"
    Und das Geburtsdatum des Kunden ist "1990-01-01"
    Und der Status des Kunden ist "locked"

  Szenario: Geburtsdatum entfernen
    Angenommen es existiert ein Kunde mit dem Namen "Tom Mayer" und dem Geburtsdatum "1990-01-01"
    Und die Änderung entfernt das Geburtsdatum
    Wenn ich den Kunden ändere
    Dann wird der Kunde geändert
    Wenn ich den Kunden abrufe
    Dann erhalte ich den Kunden
    Und der Name des Kunden ist "Tom Mayer"
    Und das Geburtsdatum des Kunden ist nicht gesetzt

  Szenario: Kunde existiert nicht
    Angenommen es existiert ein gelöschter Kunde
    Und die Änderung setzt den Status auf "locked"
    Wenn ich den Kunden ändere
    Dann wird der Kunde nicht gefunden

  Szenario: Ungültige ID
    Angenommen die Änderung setzt den Status auf "locked"
    Wenn ich den Kunden mit der ID "gelbekatze" ändere
    Dann wird die Anfrage als ungültig abgelehnt

  Szenariogrundriss: Ungültige Änderung ändert nichts – <fall>
    Angenommen es existiert ein Kunde mit dem Namen "Tom Mayer" und dem Status "active"
    Und die Änderung setzt den Namen auf "Tom Müller"
    Und die Änderung <abweichung>
    Wenn ich den Kunden ändere
    Dann wird die Anfrage als ungültig abgelehnt
    Wenn ich den Kunden abrufe
    Dann erhalte ich den Kunden
    Und der Name des Kunden ist "Tom Mayer"
    Und der Status des Kunden ist "active"

    Beispiele:
      | fall               | abweichung                          |
      | unbekannter Status | setzt den Status auf "verschollen"  |
      | Name entfernt      | entfernt den Namen                  |
      | unbekanntes Feld   | setzt die Lieblingsfarbe auf "blau" |

  Szenario: Änderung in nicht unterstütztem Format
    Angenommen es existiert ein Kunde mit dem Namen "Tom Mayer"
    Und die Änderung setzt den Status auf "locked"
    Wenn ich den Kunden mit einer Änderung in einem nicht unterstützten Format ändere
    Dann wird das Datenformat nicht unterstützt
