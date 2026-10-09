// Gera o index.html publicado (site, PWA e APK) a partir da fonte legível em src/krapka.html.
//
//   cd tools && npm ci && node build.mjs
//
// A fonte é a mesma página que roda como artefato no Claude (fontes do Google Fonts). Aqui ela ganha:
// metadados de app instalável, as fontes servidas pelo próprio app (fonts/), a marca de app
// (window.DJ_APP) e o registro do service worker (fora do APK Android), e sai compactada.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { minify } from 'html-minifier-terser';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const src = fs.readFileSync(path.join(ROOT, 'src', 'krapka.html'), 'utf8');

const pick = (re, what) => { const m = src.match(re); if (!m) throw new Error(`não achei ${what} em src/krapka.html`); return m[0]; };
const title = pick(/<title>[\s\S]*?<\/title>/, '<title>');
const fontLink = pick(/<link rel="stylesheet" href="https:\/\/fonts\.googleapis\.com[^>]*>/, 'o link do Google Fonts');
const style = pick(/<style>[\s\S]*?<\/style>/, '<style>');
const body = src.replace(title, '').replace(fontLink, '').replace(style, '').trim();

// as mesmas @font-face de fonts/fonts.css, com o caminho a partir da raiz do site
const fontCss = fs.readFileSync(path.join(ROOT, 'fonts', 'fonts.css'), 'utf8').replaceAll('url(', 'url(fonts/');

const html = `<!doctype html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">
${title}
<meta name="description" content="Krapka — Dithering &amp; Pixel Art: pontilhado de 1 bit, meio-tom e duotone para cartazes, com camadas separadas e recorte automático.">
<meta name="theme-color" content="#15120f">
<link rel="manifest" href="manifest.webmanifest">
<link rel="icon" type="image/png" href="icons/icon-192.png">
<link rel="apple-touch-icon" href="icons/apple-touch-icon.png">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-title" content="Krapka">
<link rel="preload" href="fonts/archivo-latin-400.woff2" as="font" type="font/woff2" crossorigin>
<link rel="preload" href="fonts/ibm-plex-mono-latin-600.woff2" as="font" type="font/woff2" crossorigin>
<style>
${fontCss}
:root { padding-top: env(safe-area-inset-top, 0px); padding-bottom: env(safe-area-inset-bottom, 0px); }
body { margin: 0; }
img { max-width: 100%; }
[hidden] { display: none !important; }
</style>
${style}
<script>window.DJ_APP = true;</script>
</head>
<body>
${body}
<script>
// service worker só no site/PWA; dentro do APK Android (ponte KrapkaNative) o app já vem no aparelho
if ('serviceWorker' in navigator && location.protocol !== 'file:' && !window.KrapkaNative) {
  window.addEventListener('load', () => { navigator.serviceWorker.register('sw.js').catch(() => {}); });
}
</script>
</body>
</html>
`;

const out = await minify(html, {
  collapseWhitespace: true,
  conservativeCollapse: true,
  removeComments: true,
  minifyCSS: true,
  // CORE precisa manter o nome: o Web Worker recebe CORE.toString()
  minifyJS: { mangle: { toplevel: false }, compress: { passes: 2 } },
});
fs.writeFileSync(path.join(ROOT, 'index.html'), out);
console.log(`index.html: ${out.length} bytes (fonte: ${src.length})`);
