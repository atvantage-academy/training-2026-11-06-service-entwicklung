/* =============================================================================
   Stände umschalten – ATVANTAGE Academy
   -----------------------------------------------------------------------------
   `specs.json` wird von der Pages-Pipeline erzeugt: zuerst der aktuelle Stand
   aus `main`, danach jeder gemergte PR, der die `openapi.yaml` geändert hat.
   Diese Datei baut daraus das Auswahlfeld in der Leiste und lädt den gewählten
   Stand in die Swagger UI – ohne Seitenwechsel.

   Der gewählte Stand steht in der Adresse (`?spec=pr-3`). Deshalb lässt sich
   ein einzelner Stand verlinken. Ohne Angabe ist `main` vorausgewählt.
   ============================================================================= */

(function () {
  'use strict';

  var select = document.getElementById('stand-select');
  var note = document.getElementById('stand-note');

  var SWAGGER_OPTIONS = {
    dom_id: '#swagger-ui',
    deepLinking: true,
    docExpansion: 'list',
    defaultModelsExpandDepth: 1,
    // Hinter den Adressen steht kein Server – „Try it out“ würde ins Leere
    // laufen.
    tryItOutEnabled: false,
    supportedSubmitMethods: [],
    presets: [SwaggerUIBundle.presets.apis],
    plugins: [SwaggerUIBundle.plugins.DownloadUrl],
    // BaseLayout statt StandaloneLayout: Die Leiste oben ist unsere eigene.
    layout: 'BaseLayout'
  };

  function fail(message) {
    note.textContent = message;
    select.innerHTML = '';
    select.disabled = true;
  }

  function renderNote(spec) {
    note.innerHTML = '';

    var text = document.createElement('span');
    text.className = 'stand-note__text';
    text.textContent = spec.summary || '';
    note.appendChild(text);

    if (spec.link) {
      var link = document.createElement('a');
      link.href = spec.link;
      link.rel = 'noopener';
      link.textContent = spec.linkText || spec.id;
      note.appendChild(link);
      note.appendChild(document.createTextNode(' · '));
    }

    var yamlLink = document.createElement('a');
    yamlLink.href = spec.url;
    yamlLink.textContent = 'openapi.yaml';
    note.appendChild(yamlLink);
  }

  function show(spec, isFirst) {
    renderNote(spec);
    document.title = spec.label + ' · Kundenverwaltung API – ATVANTAGE Academy';

    if (isFirst) {
      window.ui = SwaggerUIBundle(Object.assign({ url: spec.url }, SWAGGER_OPTIONS));
      return;
    }

    // Beschreibung im laufenden Betrieb austauschen. Geht das schief – etwa
    // weil eine künftige Version diese Aktionen umbenennt –, lädt die Seite
    // neu; das Ergebnis ist dasselbe, nur unruhiger.
    try {
      window.ui.specActions.updateUrl(spec.url);
      window.ui.specActions.download(spec.url);
    } catch (error) {
      window.location.search = '?spec=' + encodeURIComponent(spec.id);
    }
  }

  fetch('specs.json', { cache: 'no-cache' })
    .then(function (response) {
      if (!response.ok) { throw new Error('specs.json: HTTP ' + response.status); }
      return response.json();
    })
    .then(function (data) {
      var specs = (data && data.specs) || [];

      if (specs.length === 0) {
        fail('Es wurde kein Stand gefunden.');
        return;
      }

      // Der erste Eintrag ist `main` und damit vorausgewählt.
      var wanted = new URLSearchParams(window.location.search).get('spec');
      var current = specs.filter(function (s) { return s.id === wanted; })[0] || specs[0];

      select.innerHTML = '';
      specs.forEach(function (spec) {
        var option = document.createElement('option');
        option.value = spec.id;
        option.textContent = spec.label;
        option.selected = spec === current;
        select.appendChild(option);
      });

      // Ist nur ein Stand da, wäre die Auswahl eine Attrappe.
      select.disabled = specs.length < 2;

      select.addEventListener('change', function () {
        var chosen = specs.filter(function (s) { return s.id === select.value; })[0];
        if (!chosen) { return; }
        var url = new URL(window.location.href);
        url.searchParams.set('spec', chosen.id);
        url.hash = '';
        window.history.replaceState(null, '', url);
        show(chosen, false);
      });

      show(current, true);
    })
    .catch(function (error) {
      fail('Die Liste der Stände ließ sich nicht laden: ' + error.message);
    });
})();
