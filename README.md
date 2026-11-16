# IPTV Hero Pro (Android Nativo)

Player IPTV completo desenvolvido 100% nativamente em **Kotlin**, **Jetpack Compose** e **Media3 ExoPlayer**, projetado para smartphones, tablets e Android TV / TV Box (navegação por D-Pad). Zero dependências de WebView.

## Funcionalidades Implementadas
- **Gerenciamento Multi-Perfis (Room Database)**: Suporte a Xtream Codes, M3U e TXT com validação automática de expiração (`VALID`, `EXPIRING_SOON`, `EXPIRED`) e contagem de itens (Ao Vivo, Filmes e Séries).
- **Integração Xtream Codes (`player_api.php`)**: Autenticação completa, carregamento de categorias e streams, além de carregamento sob demanda de temporadas e episódios (`get_series_info`).
- **Parser M3U & TXT Nativo**: Extração de `#EXTINF`, `group-title`, `tvg-logo` e separação automática entre Ao Vivo, Filmes e Séries.
- **Player Media3 ExoPlayer**: Reprodução nativa de HLS (`.m3u8`), DASH (`.mpd`) e VOD (`.mp4`/`.mkv`) with `FLAG_KEEP_SCREEN_ON` e modo Tela Cheia.
- **Interceptor de Proxy Global**: Redireciona chamadas de API, downloads M3U e segmentos de vídeo através de uma URL de proxy configurável (`https://seu-proxy.com/?url=`).
- **Lista de Heróis (Importação em Lote)**: Download de lista remota de URLs com barra de progresso e console de logs em tempo real.
- **Navegação por Controle Remoto (D-Pad)**: Foco visual em botões, abas, lista de canais e alternância direcional (`Esquerda`/`Direita`) entre Temporadas e Episódios.

## Instruções de Compilação
1. Abra o diretório `IPTVHeroPro` no **Android Studio (Koala ou superior)**.
2. Certifique-se de ter o **JDK 17** configurado em `File > Settings > Build, Execution, Deployment > Build Tools > Gradle`.
3. Aguarde a sincronização do Gradle (`Sync Project with Gradle Files`).
4. Para gerar o APK de debug via terminal:
   ```bash
   ./gradlew assembleDebug
   ```
   O APK será gerado em `app/build/outputs/apk/debug/app-debug.apk`.
