/* =============================================================================
   Swagger UI starten – ATVANTAGE Academy
   -----------------------------------------------------------------------------
   Lädt die `openapi.yaml`, die die Pages-Pipeline neben diese Datei legt.
   ============================================================================= */

(function () {
  'use strict';

  window.ui = SwaggerUIBundle({
    url: 'openapi.yaml',
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
  });
})();
