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

Dentro de um mundo, use `/mmg games_hub` para abrir o menu. O hub oferece seis minijogos:

- **Flappy Bird**: clique, barra de espaço ou seta para cima para voar.
- **Campo Minado**: clique esquerdo revela casas e clique direito coloca/remove bandeiras; a primeira casa revelada é segura.
- **Snake**: setas ou WASD para controlar a cobra; coma a comida sem bater nas paredes ou em si mesma.
- **Pong**: mova a raquete com W/S ou ↑/↓ e marque cinco pontos contra o computador.
- **Jogo da Memória**: encontre os oito pares clicando nas cartas.
- **Simon**: observe e repita a sequência de cores, clicando nos painéis ou usando as teclas 1 a 4.

Os jogos que oferecem reinício também aceitam a tecla R; todos têm botão para voltar ao menu. O workflow do GitHub Actions compila o mod e publica o `.jar` como artefato da execução.
