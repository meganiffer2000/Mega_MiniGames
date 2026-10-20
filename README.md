# PT-BR

### Mega MiniGames

![Logo Mega MiniGames](assets/modrinth-icon.png)

Mod de minijogos para **Minecraft 1.20.1**, usando **Fabric** e **Java 17**. O catálogo tem **166 minijogos** navegáveis em um hub com cartuchos ilustrados, filtros por categoria e interface arcade neon.

- **Versão atual:** `0.0.3-beta`
- **Dependências:** Fabric API 0.92.2
- **Versão alvo:** Minecraft 1.20.1
- **Loader:** Fabric

## Como abrir

Segure o item **Console Retrô** e use-o com o botão direito. Ele aparece na aba **Ferramentas** do Criativo e também pode ser fabricado com 5 barras de ferro, 2 redstones, 1 painel de vidro e 1 botão de pedra.

No hub, use as **setas ←/→** ou a **roda do mouse** para trocar de página, **R** para abrir um jogo aleatório e **ESC** para fechar.

📖 A lista completa dos 166 jogos, com descrição e controles, está em **[CATALOGO.md](CATALOGO.md)**.

## Central de gestão (26 novos negócios)

Os 26 negócios usam três motores de jogo diferentes — e cada um tem metas, clientes e decisões próprias.

**Atendimento (fila e preparo por teclas 1–3):** Pizzaria Expressa, Padaria da Esquina, Sorveteria Gelada, Mercado do Bairro, Cinema Pipoca, Lanchonete Turbo, Floricultura Bella, Farmácia 24h, Sushi Rápido e Loja de Brinquedos.

**Produção e mercado (comprar insumo, produzir e vender no preço alto):** Fábrica de Robôs, Porto Container, Fazenda Solar, Estufa Hidropônica, Apiário Doce Mel, Mineradora Profunda, Ferrovia Central, Fábrica de Sucos, Gráfica Expressa e Cerâmica Vila.

**Incidentes (escolher a resposta certa antes do tempo):** Hospital Central, Banco Seguro, Redação do Jornal, Aeroporto Internacional, Hotel Estrela e Zoológico Urbano.

Os jogos anteriores continuam na lista logo abaixo:

- **Pinball Neon:** mesa com três amortecedores, alvos luminosos, slingshots e flippers (A/D).
- **Batalha de Tanques:** labirinto com tijolos quebráveis, inimigos, vidas e três níveis.
- **Nonograma:** quadro 10×10 com pistas de linha/coluna, 3 dicas e contador de erros.
- **Hóquei de Ar:** disco com física, adversário que persegue e partida até 7 gols.
- **Trânsito Travado:** 8 fases de Rush Hour geradas por busca em largura (todas solucionáveis).
- **Empurra-Caixas:** dez níveis de Sokoban com desfazer e nota-par.
- **Senha Secreta:** Mastermind com seis cores, oito tentativas e dicas de posição.
- **Bomba!:** três fases de labirinto com caixotes, bombas em cadeia e monstros.
- **Salto nas Nuvens:** plataformas infinitas, plataformas móveis, frágeis e molas.
- **Batida Perfeita:** ritmo de quatro faixas com janelas de acerto, combo e nota final.
- **Dominó:** partida contra a CPU com compra automática e placar de rodadas.
- **Paciência:** Klondike completo com estoque, descarte, fundações e sete colunas.
- **Sinuca:** nove bolas, seis caçapas, mira com o mouse e tacada com força.
- **Duelo de Heróis:** batalha por turnos com ataque, defesa, poção e golpe especial.
- **Mini Plataforma:** três fases de plataforma com moedas, inimigos e bandeira.

## Console Retrô melhorado

- Textura 32×32 nova, com tela animada, D-pad, botões, LED e alto-falante.
- **Clique direito** abre o hub; **agachado + clique direito** abre um jogo aleatório direto.
- Dica (tooltip) em três idiomas e brilho de encantamento no inventário.
- Avanço (advancement) próprio para quem fabrica o console.

## Traduções

| Idioma | Item | Hub | Avanço |
| --- | --- | --- | --- |
| Português (Brasil) | Console Retrô | ✔ | ✔ |
| English (US) | Retro Console | ✔ | ✔ |
| Español | Consola Retro | ✔ | ✔ |

## Gestão mais clara nos jogos antigos

- **Fazenda Próspera, Café da Praça, Central de Trânsito, Aquário Vivo, Colônia Espacial, Livraria em Dia, Estação de Recursos e Abrigo Animal** ganharam barra de meta, avaliação em estrelas, contador de dias e recorde de sessão.

## Novidades visuais

- Tema arcade neon compartilhado: fundo animado com estrelas, grade, vinheta e varredura CRT, painéis de vidro, pílulas de HUD, barras de progresso e botões com brilho.
- Cartuchos com ilustrações próprias para cada jogo, selo de play no hover e navegação por teclado.
- Banners de vitória, derrota e empate, placares de sessão e recordes em vários jogos.
- Controles na tela: teclados virtuais no **Termo**, **Forca**, **Adivinhe o Número** e **Desafio de Matemática**; cartas desenhadas no **Blackjack** e na **Paciência**; tabuleiros ilustrados em **Damas**, **Ligue 4**, **Memória**, **Sudoku**, **Reversi** e **Dominó**.
- **Tetris** agora tem peça guardada (**C**), queda instantânea (**Espaço**) e painel de nível replanejado.
- **2048** ganhou desfazer (**Z**) e **Campo Minado** ganhou abertura em cadeia com o botão do meio.
- **Snake** troca maçãs por estrelas douradas que valem 3 pontos por tempo limitado.

## Baixar ou compilar o jar

Cada push compila o mod e publica o `.jar` em **Releases** (tag `v0.0.3-beta`), pronto para colocar na pasta `mods` junto com o **Fabric Loader** e a **Fabric API 0.92.2**.

Para compilar localmente, use **JDK 17** e execute:

```bash
./gradlew build
```

O resultado fica em `build/libs/mega-minigames-0.0.3-beta.jar`.

## Versionamento

O nome da versão é definido **manualmente** em `gradle.properties` (`mod_version`). Quando uma nova versão for combinada, basta editar esse valor, atualizar o README e enviar o commit: o workflow usa o mesmo valor para nomear o arquivo e a tag da release.

# EN

### Mega MiniGames

![Logo Mega MiniGames](assets/modrinth-icon.png)

A minigame mod for **Minecraft 1.20.1**, built using **Fabric** and **Java 17**. The catalog offers **166 minigames** in a hub with illustrated cartridges, category filters and a neon arcade interface.

- **Current version:** `0.0.3-beta`
- **Dependencies:** Fabric API 0.92.2
- **Target version:** Minecraft 1.20.1
- **Loader:** Fabric

## Opening the hub

Hold the **Retro Console** item and right-click. It can be found in the **Tools** tab in Creative mode and can also be crafted using 5 iron ingots, 2 redstone dust, 1 glass pane, and 1 stone button.

In the hub, use **←/→** or the **mouse wheel** to change pages, **R** to open a random game and **ESC** to close.

📖 The full list of all 166 games, with descriptions and controls, is in **[CATALOGO.md](CATALOGO.md)**.

## Management center (26 new businesses)

The 26 businesses run on three different play engines — each with its own goals, customers and decisions.

**Service rush (queue and prep with keys 1–3):** Pizzeria, Bakery, Ice Cream Shop, Corner Market, Popcorn Cinema, Turbo Diner, Flower Shop, 24h Pharmacy, Sushi Bar and Toy Store.

**Production and market (buy input, produce, sell at the right price):** Robot Factory, Container Port, Solar Farm, Hydroponic Greenhouse, Honey Apiary, Deep Mine, Central Railway, Juice Factory, Print Shop and Ceramics Studio.

**Incidents (pick the right response before time runs out):** Central Hospital, Safe Bank, Newsroom, International Airport, Star Hotel and City Zoo.

The previous additions are still in the catalog:

- **Pinball Neon:** table with three bumpers, lit targets, slingshots and flippers (A/D).
- **Tank Battle:** maze with breakable bricks, enemy tanks, lives and three stages.
- **Nonogram:** 10×10 board with row/column clues, 3 hints and a mistake counter.
- **Air Hockey:** puck physics, a chasing opponent and a first-to-7 match.
- **Rush Hour:** 8 levels generated with breadth-first search (all solvable).
- **Sokoban:** ten box-pushing levels with undo and a par score.
- **Mastermind:** six colours, eight guesses and position clues.
- **Bomb!:** three maze stages with crates, chain explosions and monsters.
- **Cloud Jump:** endless platforms, moving and fragile platforms plus springs.
- **Perfect Beat:** four-lane rhythm with timing windows, combo and final grade.
- **Dominoes:** match against the CPU with automatic drawing and round scoring.
- **Solitaire:** full Klondike with stock, waste, foundations and seven columns.
- **Pool:** nine balls, six pockets, mouse aiming and charge-based shots.
- **Hero Duel:** turn-based battle with attack, defend, potion and special moves.
- **Mini Platformer:** three platforming stages with coins, enemies and a flag.

## Improved Retro Console

- New 32×32 texture with an animated screen, D-pad, buttons, LED and speaker.
- **Right-click** opens the hub; **sneak + right-click** starts a random game.
- Tooltip in three languages and enchantment glint in the inventory.
- Its own advancement for players who craft the console.

## Translations

| Language | Item | Hub | Advancement |
| --- | --- | --- | --- |
| Portuguese (Brazil) | Console Retrô | ✔ | ✔ |
| English (US) | Retro Console | ✔ | ✔ |
| Spanish | Consola Retro | ✔ | ✔ |

## Clearer management in the older games

- **Farm, Café, Traffic Control, Aquarium, Space Colony, Bookshop, Resource Station and Animal Shelter** gained a goal bar, star rating, day counter and session record.

## Visual changes

- Shared neon arcade theme: animated star/grid background with vignette and CRT sweep, glass panels, HUD pills, progress bars and glossy buttons.
- Cartridges with unique artwork for each game, hover play badge and keyboard navigation.
- Victory, defeat and draw banners, session scores and best records in many games.
- On-screen controls: virtual keyboards in **Wordle**, **Hangman**, **Number Guess** and **Math Quiz**; drawn cards in **Blackjack** and **Solitaire**; illustrated boards in **Checkers**, **Connect Four**, **Memory**, **Sudoku**, **Reversi** and **Dominoes**.
- **Tetris** now has a hold slot (**C**), instant drop (**Space**) and a redesigned level panel.
- **2048** gained undo (**Z**) and **Minesweeper** gained middle-click chording.
- **Snake** swaps apples for golden stars worth 3 points for a limited time.

## Downloading or building the jar

Every push builds the mod and publishes the `.jar` under **Releases** (tag `v0.0.3-beta`), ready to drop into the `mods` folder with **Fabric Loader** and **Fabric API 0.92.2**.

To build locally with **JDK 17**:

```bash
./gradlew build
```

The result is written to `build/libs/mega-minigames-0.0.3-beta.jar`.

## Versioning

The version name is set **manually** in `gradle.properties` (`mod_version`). When a new version is agreed, edit that value, update the README and push the commit: the workflow uses the same value for the jar name and the release tag.
