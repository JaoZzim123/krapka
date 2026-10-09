# Krapka — Dithering & Pixel Art (Крапка)

Ferramenta de dithering (pontilhado de 1 bit) criada para as artes do Grupo Folclórico JETTIA. Krapka (Крапка) quer dizer "ponto" em ucraniano. É um app web instalável (PWA): depois de publicado, instala no computador e no celular, funciona sem internet e baixa os arquivos direto.

## O que tem nesta pasta

| Arquivo | Para que serve |
|---|---|
| `index.html` | O app inteiro (interface e processamento) |
| `privacidade.html` | Política de privacidade (exigida pela Play Store) |
| `sw.js` | Guarda o app no aparelho para funcionar offline |
| `manifest.webmanifest` | Nome, cores e ícones para a instalação |
| `icons/` | Ícones do app |
| `fonts/` | Fontes (Archivo e IBM Plex Mono), servidas pelo próprio app |

## Publicar no GitHub Pages (grátis)

1. Entre em github.com e crie um repositório novo, por exemplo `krapka`, marcado como **Public**.
2. Na página do repositório, clique em **Add file > Upload files** e arraste **o conteúdo** desta pasta (`index.html`, `privacidade.html`, `sw.js`, `manifest.webmanifest` e as pastas `icons` e `fonts`). Clique em **Commit changes**.
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

Substitua os arquivos no repositório e troque o número em `sw.js` (`krapka-v5` para `v6`, e assim por diante), para os aparelhos baixarem a versão nova.

## App Android (APK) independente do site

A pasta `android/` é um app Android que leva o Krapka inteiro dentro do APK: abre sem internet e não acessa o site. Só o recorte com IA usa a internet, para baixar o modelo uma vez.

- **Compilação:** a cada mudança no app (ou em **Actions > APK Android > Run workflow**), o GitHub compila e publica o `.apk` e o `.aab` em **Releases**. O GitHub só compila; o app instalado não depende dele.
- **Instalar no celular:** baixe o `.apk` da versão mais recente em Releases e abra no Android (10 ou mais novo).
- **Arquivos salvos:** PNG em *Imagens/Krapka* (aparece na galeria); ZIP de camadas em *Downloads/Krapka*.
- **Abrir fotos pelo Krapka:** na galeria, *Compartilhar > Krapka* abre a foto direto no app.
- **Play Store:** envie o `.aab`. O nome do pacote é `com.jaozzim123.krapka`.

### Chave de assinatura (Secrets)

Sem chave cadastrada, o APK sai com uma chave de teste diferente a cada compilação, e cada versão nova precisa desinstalar a anterior. Com a chave, todas as versões se atualizam por cima e o `.aab` serve para a Play Store. Em **Settings > Secrets and variables > Actions > New repository secret**:

| Nome | Valor |
|---|---|
| `KRAPKA_KEYSTORE_BASE64` | o arquivo `.keystore` convertido em base64 |
| `KRAPKA_KEY_ALIAS` | o apelido (alias) da chave |
| `KRAPKA_KEY_PASSWORD` | a senha da chave (key password) |
| `KRAPKA_STORE_PASSWORD` | a senha do arquivo (store password), só se for diferente da senha da chave |

Para converter o `.keystore` em base64 no Windows (PowerShell), o resultado já vai para a área de transferência:

```
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\caminho\signing.keystore")) | Set-Clipboard
```

Nunca coloque o arquivo `.keystore` nem as senhas no repositório: guarde em dois lugares seguros. Sem eles não dá para atualizar o app na Play Store.
