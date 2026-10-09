// Krapka: guarda o app no aparelho para funcionar sem internet.
// Ao publicar uma versão nova, troque o número abaixo para os aparelhos baixarem a atualização.
const VERSION = 'krapka-v4';
const CDN_CACHE = 'krapka-cdn';
const SHELL = [
  './',
  'index.html',
  'privacidade.html',
  'manifest.webmanifest',
  'icons/icon-192.png',
  'icons/icon-512.png',
  'icons/icon-maskable-512.png',
  'icons/apple-touch-icon.png',
  'fonts/fonts.css',
  'fonts/archivo-latin-400.woff2',
  'fonts/archivo-latin-700.woff2',
  'fonts/ibm-plex-mono-latin-400.woff2',
  'fonts/ibm-plex-mono-latin-600.woff2',
  'fonts/ibm-plex-mono-cyrillic-400.woff2'
];
// biblioteca do recorte com IA (o modelo em si fica guardado pela própria biblioteca)
const CDN_HOSTS = ['cdn.jsdelivr.net'];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(VERSION).then((c) => c.addAll(SHELL)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== VERSION && k !== CDN_CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);

  // páginas: tenta a rede primeiro (para pegar atualizações), cai no cache sem internet
  if (req.mode === 'navigate') {
    e.respondWith(
      fetch(req)
        .then((res) => { const copy = res.clone(); caches.open(VERSION).then((c) => c.put('index.html', copy)); return res; })
        .catch(() => caches.match('index.html'))
    );
    return;
  }

  // arquivos do próprio app: cache primeiro
  if (url.origin === self.location.origin) {
    e.respondWith(caches.match(req).then((hit) => hit || fetch(req)));
    return;
  }

  // biblioteca: usa o cache e atualiza em segundo plano
  if (CDN_HOSTS.includes(url.hostname)) {
    e.respondWith(
      caches.open(CDN_CACHE).then(async (c) => {
        const hit = await c.match(req);
        const net = fetch(req).then((res) => { if (res && (res.ok || res.type === 'opaque')) c.put(req, res.clone()); return res; }).catch(() => hit);
        return hit || net;
      })
    );
  }
});
