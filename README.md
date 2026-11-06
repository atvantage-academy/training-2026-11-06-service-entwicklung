# Account Management Service

Im Rahmen des Trainings „Service-Entwicklung“ setzen wir einen Account Management Service um, der Kunden-Accounts verwaltet: Kunden anlegen, suchen, auslesen, ändern, deaktivieren und löschen.

Wir gehen dabei nach **API First** vor: Am Anfang steht die Schnittstelle, beschrieben als OpenAPI. Sie ist der Vertrag, auf den sich Anbieter und Nutzer des Service verlassen können. Die Implementierung als Quarkus-Anwendung folgt im nächsten Schritt.

## API

- **OpenAPI-Beschreibung:** [`openapi.yaml`](openapi.yaml)
- **Swagger UI:** <https://atvantage-academy.github.io/training-2026-11-06-service-entwicklung/>

Die OpenAPI-Beschreibung wird bei jedem Pull Request auf `main` validiert. Nach jedem Push auf `main` wird die Swagger UI neu veröffentlicht.
