# PT-BR

### Mega MiniGames

![Logo Mega MiniGames](assets/modrinth-icon.png)

Mod de minijogos para **Minecraft 1.20.1**, usando **Fabric** e **Java 17**. O catálogo tem **166 minijogos** navegáveis em um hub com cartuchos ilustrados, filtros por categoria e interface arcade neon.

- **Versão atual:** `0.0.4-beta`
- **Dependências:** Fabric API 0.92.2
- **Versão alvo:** Minecraft 1.20.1
- **Loader:** Fabric

## Como abrir

Segure o item **Console Retrô** e use-o com o botão direito. Ele aparece na aba **Ferramentas** do Criativo e também pode ser fabricado com 5 barras de ferro, 2 redstones, 1 painel de vidro e 1 botão de pedra.

No hub, use as **setas ←/→** ou a **roda do mouse** para trocar de página, **R** para abrir um jogo aleatório e **ESC** para fechar.

📖 A lista completa dos 166 jogos, com descrição e controles, está em **[CATALOGO.md](CATALOGO.md)**.

## Pizzaria Expressa — jogo de gestão em pixel art

A **Pizzaria Expressa** ganhou tela própria e virou uma simulação de verdade: o **Steve** atende **Villagers** atrás do balcão, numa pizzaria pixel art com câmera lateral que segue o personagem. Não tem game over — o ritmo é seu.

- **Mundo em pixels:** o cenário é gerado aos poucos (poucos blocos por quadro) e os pixels já desenhados são guardados ao sair; ao voltar, os pedaços são recarregados conforme o Steve caminha.
- **Loja do outro lado da rua:** 12 ingredientes à venda (clique compra 1, **Shift + clique** compra 5).
- **Bancada 3×3 (crafttable):** arraste ingredientes da despensa para a grade e teste combinações. **As receitas são secretas** — um sabor só aparece no cardápio depois de ser montado pela primeira vez. A pizza de **queijo** já vem liberada; são **10 sabores**.
- **Fornalha:** a pizza montada vai para o forno e fica pronta sozinha. Começa com 1 forno e dá para chegar a 5.
- **Clientes:** Villagers normais e 6 **especiais** (pagam 50% a mais). Cada venda dá dinheiro e **XP**; clientes especiais podem deixar **colecionáveis**.
- **3 abas no topo:** **Melhorias** (forno, pizzaria e Steve), **Coleção** (8 itens deixados por clientes) e **Equipe** (3 garçons e 3 chefes, cada um com nível mínimo, preço e velocidade próprios — os chefes fazem pizzas sozinhos).
- **Controles:** setas ←/→ ou **A/D** para andar, **E** para interagir, **1 a 0** para servir pelo cardápio, clique para servir e comprar, **ESC** para sair.

## Padaria da Esquina e Sorveteria Gelada

As duas lojas seguem o motor da Pizzaria (mesma câmera, mesma bancada 3×3 com receitas secretas, mesmas abas), mas cada uma tem **mecânica própria**:

- **Padaria da Esquina:** **12 ingredientes** e **10 receitas** (pão francês, croissant, sonho, pão de queijo, torta de frango...). Cada fornada fica **quentinha por 75 segundos** — depois o cliente paga **35% menos**. O forno é de tijolos com cúpula e a parede tem prateleiras de pão.
- **Sorveteria Gelada:** **12 ingredientes** e **10 sorvetes** (do baunilha à taça especial), com freezer de vidro, batedeira e piso xadrez. A cada ~75 s vem uma **onda de calor**: mais clientes, menos paciência e **+25% nos preços** por 30 segundos.
- **Promoção do dia (as três lojas):** a cada 2 minutos um sabor conhecido passa a pagar **50% a mais** — ele ganha uma estrela no cardápio.

## Simulação com física própria

Três jogos de **Simulação** ganharam telas próprias, com física e progressão de verdade:

- **Pouso de Precisão:** um módulo lunar desce sobre um mundo de 2.200 px com terreno gerado na hora, **três plataformas** (x2, x3 e x4) e **5 fases**. A gravidade aumenta a cada fase, o vento muda de direção e aparece no painel, o combustível é finito e o módulo explode se tocar o solo rápido demais. São **3 vidas**, sombra e linha de mira para ajudar, e a pontuação depende do combustível restante e do multiplicador da plataforma.
- **Meu Bichinho:** quarto em pixel art com ciclo de dia e noite. O bichinho tem **fome, higiene, energia e alegria**, e cada cuidado rende moedas. Dá para fazer **carinho** clicando nele, comprar **3 melhorias** (cama, brinquedo e banheira, até o nível 3), jogar bola e colocar para dormir. O bichinho sobe de nível e rende mais moedas — e **não existe game over**.
- **Órbita Estável:** gravidade 1/r², inércia, arrasto e propulsão com combustível. Mantenha o módulo na **faixa verde** de uma órbita-alvo que muda a cada fase, desvie de **destroços**, colete **células de combustível** e complete **5 fases** com 3 vidas. O HUD mostra combustível, raio da órbita, velocidade e estabilidade.

## Central de gestão (23 negócios)

Os 23 negócios usam três motores de jogo diferentes — e cada um tem metas, clientes e decisões próprias.

**Atendimento (fila e preparo por teclas 1–3):** Mercado do Bairro, Cinema Pipoca, Lanchonete Turbo, Floricultura Bella, Farmácia 24h, Sushi Rápido e Loja de Brinquedos.

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

## Traduções

| Idioma | Item | Hub | Avanço |
| --- | --- | --- | --- |
| Português (Brasil) | Console Retrô | ✔ | ✔ |
| English (US) | Retro Console | ✔ | ✔ |
| Español | Consola Retro | ✔ | ✔ |

## Baixar ou compilar o jar

Cada push compila o mod e publica o `.jar` em **Releases** (tag `v0.0.4-beta`), pronto para colocar na pasta `mods` junto com o **Fabric Loader** e a **Fabric API 0.92.2**.

Para compilar localmente, use **JDK 17** e execute:

```bash
./gradlew build
```

O resultado fica em `build/libs/mega-minigames-0.0.4-beta.jar`.

## Versionamento

O nome da versão é definido **manualmente** em `gradle.properties` (`mod_version`). Quando uma nova versão for combinada, basta editar esse valor, atualizar o README e enviar o commit: o workflow usa o mesmo valor para nomear o arquivo e a tag da release.

# EN

### Mega MiniGames

![Logo Mega MiniGames](assets/modrinth-icon.png)

A minigame mod for **Minecraft 1.20.1**, built using **Fabric** and **Java 17**. The catalog offers **166 minigames** in a hub with illustrated cartridges, category filters and a neon arcade interface.

- **Current version:** `0.0.4-beta`
- **Dependencies:** Fabric API 0.92.2
- **Target version:** Minecraft 1.20.1
- **Loader:** Fabric

## Opening the hub

Hold the **Retro Console** item and right-click. It can be found in the **Tools** tab in Creative mode and can also be crafted using 5 iron ingots, 2 redstone dust, 1 glass pane, and 1 stone button.

In the hub, use **←/→** or the **mouse wheel** to change pages, **R** to open a random game and **ESC** to close.

📖 The full list of all 166 games, with descriptions and controls, is in **[CATALOGO.md](CATALOGO.md)**.

## Pizzeria Express — a pixel-art management game

**Pizzeria Express** got its own screen and became a real simulation: **Steve** serves **Villagers** behind the counter of a pixel-art pizzeria, with a side camera that follows the character. There is no game over — you set the pace.

- **Pixel world:** the scenery is drawn gradually (a few chunks per frame) and the generated pixels are saved when you leave; on your way back they reload as Steve walks.
- **Shop across the street:** 12 ingredients for sale (click buys 1, **Shift + click** buys 5).
- **3×3 workbench (crafting table):** move ingredients from the pantry into the grid and try combinations. **Recipes are secret** — a flavour only shows up on the menu after you make it for the first time. **Cheese** pizza is unlocked from the start; there are **10 flavours**.
- **Oven:** a built pizza goes in and bakes by itself. You start with 1 oven and can reach 5.
- **Customers:** regular Villagers plus 6 **specials** (they pay 50% more). Every sale gives money and **XP**; specials may leave **collectibles**.
- **3 tabs at the top:** **Upgrades** (oven, pizzeria and Steve), **Collection** (8 items left by customers) and **Team** (3 waiters and 3 chefs, each with their own level requirement, price and speed — chefs make pizzas on their own).
- **Controls:** ←/→ or **A/D** to walk, **E** to interact, **1 to 0** to serve from the menu, click to serve and buy, **ESC** to leave.

## Corner Bakery and Ice Cream Shop

Both shops run on the Pizzeria engine (same camera, same 3×3 workbench with secret recipes, same tabs), but each one has its **own mechanic**:

- **Corner Bakery:** **12 ingredients** and **10 recipes** (baguette, croissant, cream bun, cheese bread, chicken pie...). Every batch stays **warm for 75 seconds** — after that customers pay **35% less**. The brick oven has a dome and the wall is lined with bread shelves.
- **Ice Cream Shop:** **12 ingredients** and **10 ice creams** (from vanilla to the special sundae), with a glass freezer, a mixer and a checkered floor. Every ~75 s a **heat wave** hits: more customers, less patience and **+25% on prices** for 30 seconds.
- **Daily special (all three shops):** every 2 minutes one known flavour pays **50% more** — it gets a star on the menu.

## Simulation with real physics

Three **Simulation** games now have their own screens, with real physics and progression:

- **Precision Landing:** a lunar module descends over a 2,200 px world with generated terrain, **three pads** (x2, x3 and x4) and **5 stages**. Gravity grows every stage, the wind shifts and is shown on the panel, fuel is limited and the module explodes if it hits the ground too fast. You get **3 lives**, a shadow and a guide line, and the score depends on the remaining fuel and the pad multiplier.
- **My Little Pet:** a pixel-art room with a day/night cycle. The pet has **hunger, hygiene, energy and joy**, and every care action earns coins. You can **pet it** by clicking, buy **3 upgrades** (bed, toy and bathtub, up to level 3), play ball and let it sleep. The pet levels up and earns more coins — and there is **no game over**.
- **Stable Orbit:** 1/r² gravity, inertia, drag and fuelled thrust. Keep the module inside the **green band** of a target orbit that changes every stage, dodge **debris**, collect **fuel cells** and clear **5 stages** with 3 lives. The HUD shows fuel, orbit radius, speed and stability.

## Management center (23 businesses)

The 23 businesses run on three different play engines — each with its own goals, customers and decisions.

**Service rush (queue and prep with keys 1–3):** Corner Market, Popcorn Cinema, Turbo Diner, Flower Shop, 24h Pharmacy, Sushi Bar and Toy Store.

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

## Translations

| Language | Item | Hub | Advancement |
| --- | --- | --- | --- |
| Portuguese (Brazil) | Console Retrô | ✔ | ✔ |
| English (US) | Retro Console | ✔ | ✔ |
| Spanish | Consola Retro | ✔ | ✔ |

## Downloading or building the jar

Every push builds the mod and publishes the `.jar` under **Releases** (tag `v0.0.4-beta`), ready to drop into the `mods` folder with **Fabric Loader** and **Fabric API 0.92.2**.

To build locally with **JDK 17**:

```bash
./gradlew build
```

The result is written to `build/libs/mega-minigames-0.0.4-beta.jar`.

## Versioning

The version name is set **manually** in `gradle.properties` (`mod_version`). When a new version is agreed, edit that value, update the README and push the commit: the workflow uses the same value for the jar name and the release tag.
