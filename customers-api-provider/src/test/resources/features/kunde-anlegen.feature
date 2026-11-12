# language: de
Funktionalität: Kunden anlegen

  Szenario: Gültigen Kunden speichern
    Angenommen die Kundendaten enthalten den Namen "Tom Mayer"
    Und die Kundendaten enthalten das Geburtsdatum "1990-01-01"
    Und die Kundendaten enthalten den Status "active"
    Wenn ich einen Kunden mit den Kundendaten speichere
    Dann wird der Kunde angelegt
    Und die Antwort ist im JSON-Format
    Und der Name des Kunden ist "Tom Mayer"
    Und das Geburtsdatum des Kunden ist "1990-01-01"
    Und der Status des Kunden ist "active"
    Und der Kunde hat eine ID
    Und die Antwort verweist auf den neuen Kunden

  Szenario: Kunde ohne Status wird als aktiv angelegt
    Angenommen die Kundendaten enthalten den Namen "Tom Mayer"
    Wenn ich einen Kunden mit den Kundendaten speichere
    Dann wird der Kunde angelegt
    Und der Status des Kunden ist "active"

  Szenario: XML wird als Datenformat nicht unterstützt
    Angenommen die Kundendaten enthalten den Namen "Tom Mayer"
    Und die Kundendaten enthalten das Geburtsdatum "1990-01-01"
    Und die Kundendaten enthalten den Status "active"
    Wenn ich einen Kunden mit den Kundendaten im XML-Format speichere
    Dann wird das Datenformat nicht unterstützt

  Szenario: XML wird als Antwortformat nicht unterstützt
    Angenommen die Kundendaten enthalten den Namen "Tom Mayer"
    Und die Kundendaten enthalten das Geburtsdatum "1990-01-01"
    Und die Kundendaten enthalten den Status "active"
    Wenn ich einen Kunden mit den Kundendaten speichere und die Antwort im XML-Format erwarte
    Dann wird das Antwortformat nicht unterstützt

  Szenario: Kunde ohne Daten
    Wenn ich einen Kunden ohne Daten speichere
    Dann wird die Anfrage als ungültig abgelehnt

  Szenario: Fehlerhaft formatierte Daten
    Wenn ich einen Kunden mit fehlerhaft formatierten Daten speichere
    Dann wird die Anfrage als ungültig abgelehnt

  Szenariogrundriss: Ungültiger Kunde – <fall>
    Angenommen die Kundendaten enthalten den Namen "Tom Mayer"
    Und die Kundendaten <abweichung>
    Wenn ich einen Kunden mit den Kundendaten speichere
    Dann wird die Anfrage als ungültig abgelehnt

    Beispiele:
      | fall                    | abweichung                                              |
      | ohne Namen              | enthalten keinen Namen                                  |
      | Name zu lang            | enthalten den Namen "{Text mit 101 Zeichen}"            |
      | ungültiges Geburtsdatum | enthalten das Geburtsdatum "gestern"                    |
      | Geburtsdatum heute      | enthalten das Geburtsdatum "{heute}"                    |
      | Geburtsdatum morgen     | enthalten das Geburtsdatum "{morgen}"                   |
      | unbekannter Status      | enthalten den Status "verschollen"                      |
      | mit ID                  | enthalten die ID "00000000-0000-0000-0000-000000000001" |
      | unbekanntes Attribut    | enthalten die Lieblingsfarbe "blau"                     |
