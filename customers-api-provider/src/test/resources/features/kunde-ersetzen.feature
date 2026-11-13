# language: de
Funktionalität: Kunden ersetzen

  Szenario: Alle Daten werden ersetzt, nicht angegebene optionale Daten entfernt
    Angenommen es existiert ein Kunde mit dem Namen "Tom Mayer", dem Geburtsdatum "1990-01-01" und dem Status "active"
    Und die Kundendaten enthalten den Namen "Tom Müller"
    Und die Kundendaten enthalten den Status "disabled"
    Wenn ich den Kunden durch die Kundendaten ersetze
    Dann wird der Kunde ersetzt
    Wenn ich den Kunden abrufe
    Dann erhalte ich den Kunden
    Und die ID des Kunden ist "{id}"
    Und der Name des Kunden ist "Tom Müller"
    Und das Geburtsdatum des Kunden ist nicht gesetzt
    Und der Status des Kunden ist "disabled"

  Szenario: Kunde existiert nicht
    Angenommen es existiert ein gelöschter Kunde
    Und die Kundendaten enthalten den Namen "Tom Mayer"
    Wenn ich den Kunden durch die Kundendaten ersetze
    Dann wird der Kunde nicht gefunden

  Szenario: Ungültige ID
    Angenommen die Kundendaten enthalten den Namen "Tom Mayer"
    Wenn ich den Kunden mit der ID "gelbekatze" durch die Kundendaten ersetze
    Dann wird die Anfrage als ungültig abgelehnt

  Szenariogrundriss: Ungültige Kundendaten – <fall>
    Angenommen es existiert ein Kunde mit dem Namen "Tom Mayer"
    Und die Kundendaten enthalten den Namen "Tom Mayer"
    Und die Kundendaten enthalten das Geburtsdatum "1990-01-01"
    Und die Kundendaten <abweichung>
    Wenn ich den Kunden durch die Kundendaten ersetze
    Dann wird die Anfrage als ungültig abgelehnt

    Beispiele:
      | fall       | abweichung              |
      | ohne Namen | enthalten keinen Namen  |
      | mit ID     | enthalten die ID "{id}" |
