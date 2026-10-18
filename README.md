# Mega MiniGames

Base inicial de um mod para **Minecraft 1.20.1**, usando **Fabric** e **Java 17**. A estrutura está pronta para receber os minijogos que serão definidos depois.

## Requisitos

- JDK 17
- Gradle 8.6 ou compatível com Fabric Loom 1.6
- Minecraft 1.20.1

## Compilar

Na raiz do projeto, execute:

```sh
./gradlew build
```

O arquivo compilado será criado em `build/libs/`. Para iniciar uma instância de desenvolvimento com o cliente, use:

```sh
./gradlew runClient
```

## Estrutura inicial

- `src/main/java/br/com/megaminigames/MegaMiniGames.java`: ponto de entrada do mod.
- `src/main/resources/fabric.mod.json`: metadados e dependências Fabric.
- `build.gradle`, `settings.gradle` e `gradle.properties`: configuração do Gradle, Loom e versões.

- **ID do mod:** `mega_minigames`
- **Versão alvo:** Minecraft 1.20.1
- **Loader:** Fabric

## Comando e primeiro jogo

Dentro de um mundo, use `/mmg games_hub` para abrir o menu paginado, que agora oferece quinze minijogos. O comando é registrado também no cliente para funcionar mesmo em servidores que não tenham o mod instalado; o mod deve estar instalado no seu cliente.

- **Flappy Bird**, **Campo Minado**, **Snake**, **Pong**, **Jogo da Memória** e **Simon**.
- **Tetris**: encaixe as peças, gire-as e complete linhas.
- **Quebra-Blocos**: mova a raquete, lance a bola e destrua os tijolos.
- **2048**: use as setas para unir peças iguais.
- **Jogo da Velha** e **Ligue 4**: desafie a IA.
- **Luzes**: apague todas as casas; cada jogada também alterna as vizinhas.
- **Acerte a Toupeira**: clique na toupeira antes que o tempo acabe.
- **Adivinhe o Número**: use os números e Enter para adivinhar de 1 a 100.
- **Desvie!**: use ←/→ ou A/D para escapar dos blocos que caem.

O hub usa páginas para organizar os jogos. Os jogos têm instruções na própria tela e botão para voltar ao menu. O workflow do GitHub Actions compila o mod e publica o `.jar` como artefato da execução.
