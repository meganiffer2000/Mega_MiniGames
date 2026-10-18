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

Dentro de um mundo, use `/mmg games_hub` para abrir o menu paginado, que agora oferece vinte minijogos. O comando é registrado também no cliente para funcionar mesmo em servidores que não tenham o mod instalado; o mod deve estar instalado no seu cliente.

Além de **Flappy Bird**, **Campo Minado**, **Snake**, **Pong**, **Jogo da Memória**, **Simon**, **Tetris**, **Quebra-Blocos**, **2048**, **Jogo da Velha**, **Ligue 4**, **Luzes**, **Acerte a Toupeira**, **Adivinhe o Número** e **Desvie!**, foram adicionados:

- **Invasores Espaciais**: mova a nave e atire com Espaço.
- **Damas**: jogue contra uma IA, com capturas obrigatórias e coroação.
- **Termo**: descubra a palavra de cinco letras em seis tentativas.
- **Desafio de Matemática**: resolva dez contas de soma, subtração e multiplicação.
- **Teste de Reflexo**: espere a tela ficar verde e clique o mais rápido possível.

O hub usa páginas para organizar os jogos. Os jogos têm instruções na própria tela e botão para voltar ao menu. O workflow do GitHub Actions compila o mod e publica o `.jar` como artefato da execução.
