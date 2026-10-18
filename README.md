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

Dentro de um mundo, use `/mmg games_hub` para abrir o menu paginado, que agora oferece **30 minijogos**. O comando é registrado também no cliente para funcionar mesmo em servidores que não tenham o mod instalado; o mod deve estar instalado no seu cliente.

Nesta atualização foram adicionados dez jogos:

- **Blackjack**: compre cartas ou pare antes de passar de 21.
- **Forca**: descubra a palavra em até sete erros.
- **Pedra, Papel e Tesoura**: dispute uma série contra o computador.
- **Labirinto**: encontre a saída de um mapa gerado a cada partida.
- **Mini Sudoku**: complete o tabuleiro 4×4 com as teclas 1–4.
- **Caça-Moedas**: colete moedas antes de o tempo acabar.
- **Digitação Rápida**: digite o maior número de palavras em um minuto.
- **Batalha Naval**: localize o navio escondido antes de acabarem os tiros.
- **Pular Corda**: pressione Espaço no momento certo.
- **Pescaria**: lance a linha e recolha quando o peixe morder.

Também foram ajustados os tamanhos dos tabuleiros e controles de **Tetris**, **Campo Minado**, **Jogo da Memória** e **Acerte a Toupeira** para telas menores; a toupeira aparece desde o início e não some entre acertos. Outros dez jogos adicionados anteriormente são **Invasores Espaciais**, **Damas**, **Termo**, **Desafio de Matemática**, **Teste de Reflexo**, **Tetris**, **Quebra-Blocos**, **2048**, **Jogo da Velha** e **Ligue 4**. O hub usa páginas para organizar os jogos e o workflow do GitHub Actions compila o mod e publica o `.jar` como artefato da execução.
