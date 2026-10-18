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

Dentro de um mundo, use `/mmg games_hub` para abrir o menu. O hub já oferece dois minijogos:

- **Flappy Bird**: clique, barra de espaço ou seta para cima para voar. `R` reinicia a partida; os botões na parte inferior reiniciam ou voltam ao menu.
- **Campo Minado**: clique esquerdo revela uma casa e clique direito coloca/remove uma bandeira. A primeira casa revelada é sempre segura. Use **Novo jogo** para reiniciar.

Os comandos do mod começam com `/mmg`, e novos jogos serão adicionados ao hub aos poucos. O workflow do GitHub Actions compila e publica o `.jar` como artefato da execução.
