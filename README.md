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

Dentro de um mundo, use `/mmg games_hub` para abrir o menu paginado, que agora oferece **50 minijogos**. O comando é registrado também no cliente para funcionar mesmo em servidores que não tenham o mod instalado; o mod deve estar instalado no seu cliente.

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

Esta atualização também adiciona vinte jogos:

- **Jogo dos 15**: ordene as peças deslizando-as para o espaço vazio.
- **Gomoku**: faça cinco em linha contra a IA.
- **Anagrama**: reorganize letras para descobrir palavras.
- **Maior ou Menor**: adivinhe se a próxima carta será maior ou menor.
- **Duelo de Dados**: dispute rodadas de dados contra o computador.
- **Pênaltis**: escolha a direção do chute e tente marcar.
- **Caçador de Alvos**: acerte alvos clicáveis antes de o tempo acabar.
- **Sapo na Estrada**: atravesse as pistas desviando dos carros.
- **Mini Golfe**: ajuste direção e força para colocar a bola no buraco.
- **Memória Numérica**: memorize e repita sequências de números.
- **Combinação de Cores**: diga se o nome da cor combina com a tinta.
- **Pegue as Estrelas**: mova a cesta para coletar estrelas.
- **Pare a Barra**: pare o marcador dentro da faixa de precisão.
- **Caça ao Tesouro**: encontre o tesouro escondido com dicas de proximidade.
- **Corrida de Faixas**: troque de faixa para escapar dos obstáculos.
- **Arremesso ao Cesto**: acerte o tempo do arremesso.
- **Salto de Obstáculos**: pressione Espaço no momento certo.
- **Sequência de Setas**: memorize e repita a sequência indicada.
- **Verdadeiro ou Falso**: responda a dez contas rápidas.
- **Guerra de Cartas**: vire cartas e vença a IA com a maior.

Também foram ajustados os tamanhos dos tabuleiros e controles de **Tetris**, **Campo Minado**, **Jogo da Memória** e **Acerte a Toupeira** para telas menores; a toupeira aparece desde o início e não some entre acertos. O hub usa páginas para organizar os jogos e o workflow do GitHub Actions compila o mod e publica o `.jar` como artefato da execução.
