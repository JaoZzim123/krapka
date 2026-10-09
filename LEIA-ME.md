# Krapka (Крапка): app

Ferramenta de dithering (pontilhado de 1 bit) criada para as artes do Grupo Folclórico JETTIA. Krapka (Крапка) quer dizer "ponto" em ucraniano. É um app web instalável (PWA): depois de publicado, instala no computador e no celular, funciona sem internet e baixa os arquivos direto.

## O que tem nesta pasta

| Arquivo | Para que serve |
|---|---|
| `index.html` | O app inteiro (interface e processamento) |
| `privacidade.html` | Política de privacidade (exigida pela Play Store) |
| `sw.js` | Guarda o app no aparelho para funcionar offline |
| `manifest.webmanifest` | Nome, cores e ícones para a instalação |
| `icons/` | Ícones do app |

## Publicar no GitHub Pages (grátis)

1. Entre em github.com e crie um repositório novo, por exemplo `krapka`, marcado como **Public**.
2. Na página do repositório, clique em **Add file > Upload files** e arraste **o conteúdo** desta pasta (`index.html`, `sw.js`, `manifest.webmanifest` e a pasta `icons`). Clique em **Commit changes**.
3. Vá em **Settings > Pages**. Em *Branch*, escolha `main` e a pasta `/ (root)`, e clique em **Save**.
4. Em um ou dois minutos o app fica no ar em `https://SEU-USUARIO.github.io/krapka/`.

## Instalar

- **Computador (Chrome ou Edge):** abra o endereço e clique em **Instalar app** no topo da página, ou no ícone de instalar na barra de endereço.
- **Android (Chrome):** menu ⋮ > **Instalar app** (ou "Adicionar à tela inicial").
- **iPhone (Safari):** botão de compartilhar > **Adicionar à Tela de Início**.

## Recorte com IA

O botão **Recortar a pessoa com IA** usa o modelo MODNet (licença Apache 2.0), que roda no próprio aparelho: a foto não é enviada para lugar nenhum. Na primeira vez ele baixa cerca de 25 MB e depois fica guardado. O MODNet foi treinado para recortar pessoas; em objetos ou estátuas o recorte pode falhar, e aí vale recortar no Photoshop e enviar o PNG.

## Testar no próprio computador antes de publicar

O app precisa ser aberto por um endereço `http://localhost` (abrir o arquivo com dois cliques funciona, mas sem a parte offline). Com o Python instalado, dentro desta pasta:

```
python -m http.server 8080
```

e abra `http://localhost:8080`.

## Atualizar depois

Substitua os arquivos no repositório e troque o número em `sw.js` (`krapka-v3` para `v4`, e assim por diante), para os aparelhos baixarem a versão nova.
