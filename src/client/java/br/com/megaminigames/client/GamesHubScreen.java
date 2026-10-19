package br.com.megaminigames.client;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Hub visual em grade de capas clicáveis, adaptável ao tamanho da janela. */
public final class GamesHubScreen extends Screen {
    private static final int[] COVER_COLORS = {
            0xFF257E8A, 0xFF7447B7, 0xFFDB7139, 0xFF287A5C, 0xFFBB4269,
            0xFF3C65A5, 0xFF9A7130, 0xFF397A9B, 0xFF8A4F56, 0xFF526B3B,
            0xFFB34F35, 0xFF5353A1
    };
    private static final List<Game> GAMES = List.of(
            new Game("Flappy Bird", FlappyBirdScreen::new),
            new Game("Campo Minado", MinesweeperScreen::new),
            new Game("Snake", SnakeScreen::new),
            new Game("Pong", PongScreen::new),
            new Game("Jogo da Memória", MemoryScreen::new),
            new Game("Simon", SimonScreen::new),
            new Game("Tetris", TetrisScreen::new),
            new Game("Quebra-Blocos", BreakoutScreen::new),
            new Game("2048", Game2048Screen::new),
            new Game("Jogo da Velha", TicTacToeScreen::new),
            new Game("Ligue 4", ConnectFourScreen::new),
            new Game("Luzes", LightsOutScreen::new),
            new Game("Acerte a Toupeira", WhackAMoleScreen::new),
            new Game("Adivinhe o Número", NumberGuessScreen::new),
            new Game("Desvie!", DodgeScreen::new),
            new Game("Invasores Espaciais", SpaceInvadersScreen::new),
            new Game("Damas", CheckersScreen::new),
            new Game("Termo", WordleScreen::new),
            new Game("Desafio de Matemática", MathQuizScreen::new),
            new Game("Teste de Reflexo", ReactionTestScreen::new),
            new Game("Blackjack", BlackjackScreen::new),
            new Game("Forca", HangmanScreen::new),
            new Game("Pedra, Papel e Tesoura", RockPaperScissorsScreen::new),
            new Game("Labirinto", MazeScreen::new),
            new Game("Mini Sudoku", MiniSudokuScreen::new),
            new Game("Caça-Moedas", CoinCollectorScreen::new),
            new Game("Digitação Rápida", TypingSprintScreen::new),
            new Game("Batalha Naval", MiniBattleshipScreen::new),
            new Game("Pular Corda", JumpRopeScreen::new),
            new Game("Pescaria", FishingScreen::new),
            new Game("Jogo dos 15", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.SLIDING_15)),
            new Game("Gomoku", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.GOMOKU)),
            new Game("Anagrama", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.ANAGRAM)),
            new Game("Maior ou Menor", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.HIGH_LOW)),
            new Game("Duelo de Dados", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.DICE_DUEL)),
            new Game("Pênaltis", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.PENALTIES)),
            new Game("Caçador de Alvos", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.AIM_TRAINER)),
            new Game("Sapo na Estrada", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.FROGGER)),
            new Game("Mini Golfe", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.MINI_GOLF)),
            new Game("Memória Numérica", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.NUMBER_MEMORY)),
            new Game("Combinação de Cores", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.COLOR_MATCH)),
            new Game("Pegue as Estrelas", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.STAR_CATCH)),
            new Game("Pare a Barra", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.STOP_BAR)),
            new Game("Caça ao Tesouro", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.TREASURE_HUNT)),
            new Game("Corrida de Faixas", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.LANE_RACER)),
            new Game("Arremesso ao Cesto", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.BASKET_SHOT)),
            new Game("Salto de Obstáculos", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.JUMP_HURDLE)),
            new Game("Sequência de Setas", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.ARROW_SEQUENCE)),
            new Game("Verdadeiro ou Falso", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.TRUE_FALSE)),
            new Game("Guerra de Cartas", () -> new ExtraArcadeScreen(ExtraArcadeScreen.Game.CARD_WAR)),
            new Game("Caça-Palavras", () -> new BonusArcadeScreen(BonusArcadeScreen.Game.WORD_SEARCH)),
            new Game("Desafio Relâmpago", () -> new BonusArcadeScreen(BonusArcadeScreen.Game.MATH_DASH)),
            new Game("Teste de Reação", () -> new BonusArcadeScreen(BonusArcadeScreen.Game.REACTION)),
            new Game("Copos Mágicos", () -> new BonusArcadeScreen(BonusArcadeScreen.Game.CUP_SHUFFLE)),
            new Game("Desvie dos Lasers", () -> new BonusArcadeScreen(BonusArcadeScreen.Game.LASER_DODGE)),
            new Game("Frutas Coloridas", () -> new BonusArcadeScreen(BonusArcadeScreen.Game.COLOR_CATCH)),
            new Game("Quiz de Bandeiras", () -> new BonusArcadeScreen(BonusArcadeScreen.Game.FLAG_QUIZ)),
            new Game("Toque Rápido", () -> new BonusArcadeScreen(BonusArcadeScreen.Game.TAP_CHALLENGE)),
            new Game("Torre Perfeita", () -> new BonusArcadeScreen(BonusArcadeScreen.Game.TOWER_STACK)),
            new Game("Estoure as Bolhas", () -> new BonusArcadeScreen(BonusArcadeScreen.Game.BUBBLE_POP)),
            new Game("Jetpack Runner", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.JETPACK)),
            new Game("Fábrica de Biscoitos", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.COOKIE)),
            new Game("Cozinha Caótica", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.KITCHEN)),
            new Game("Avião de Papel", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.PAPER_PLANE)),
            new Game("Queda Helicoidal", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.HELIX)),
            new Game("Toque nos Azulejos", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.BLACK_TILE)),
            new Game("Corrida no Trilho", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.SUBWAY)),
            new Game("Escavador", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.DIGGER)),
            new Game("Estilingue de Pássaros", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.SLINGSHOT)),
            new Game("Orbes em Linha", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.MARBLE)),
            new Game("Corte de Frutas", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.FRUIT_SLICE)),
            new Game("Sobrevivente Noturno", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.SURVIVOR)),
            new Game("Corte da Corda", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.CUT_ROPE)),
            new Game("Combinação de Doces", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.CANDY_MATCH)),
            new Game("Corrida Maluca", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.WHEEL)),
            new Game("Torre de Cartas", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.DECK_CLIMB)),
            new Game("Escalada da Montanha", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.MOUNTAIN)),
            new Game("Defesa de Balões", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.BALLOON_TD)),
            new Game("Defesa do Jardim", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.GARDEN_TD)),
            new Game("Resgate dos Mineiros", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.LEMMINGS)),
            new Game("Salto Geométrico", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.GEOMETRY)),
            new Game("Mini Metrô", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.MINI_METRO)),
            new Game("Controle de Fronteira", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.BORDER)),
            new Game("Pôquer Roguelike", () -> new InspiredArcadeScreen(InspiredArcadeScreen.Game.POKER)),
            new Game("Fazenda Próspera", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.FARM), Category.MANAGEMENT),
            new Game("Café da Praça", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.CAFE), Category.MANAGEMENT),
            new Game("Central de Trânsito", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.TRAFFIC), Category.MANAGEMENT),
            new Game("Aquário Vivo", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.AQUARIUM), Category.MANAGEMENT),
            new Game("Colônia Espacial", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.COLONY), Category.MANAGEMENT),
            new Game("Pouso de Precisão", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.FLIGHT), Category.SIMULATION),
            new Game("Meu Bichinho", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.PET), Category.SIMULATION),
            new Game("Órbita Estável", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.ORBIT), Category.SIMULATION),
            new Game("Clima e Colheita", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.WEATHER), Category.SIMULATION),
            new Game("Equilíbrio Natural", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.ECOSYSTEM), Category.SIMULATION),
            new Game("Arena de Robôs", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.ROBOT), Category.ACTION),
            new Game("Arco de Precisão", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.ARCHERY), Category.ACTION),
            new Game("Defenda o Castelo", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.CASTLE), Category.ACTION),
            new Game("Duelo Relâmpago", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.NINJA), Category.ACTION),
            new Game("Caça-Fantasmas", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.GHOST), Category.ACTION),
            new Game("Arena de Sobrevivência", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.ARENA), Category.ACTION),
            new Game("Ruínas Perdidas", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.RUINS), Category.ADVENTURE),
            new Game("Ilha Misteriosa", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.ISLAND), Category.ADVENTURE),
            new Game("Fuga da Mina", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.MINE_ESCAPE), Category.ADVENTURE),
            new Game("Relíquias da Floresta", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.FOREST), Category.ADVENTURE),
            new Game("Mapa do Corsário", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.PIRATE), Category.ADVENTURE)
    );

    private enum Category {
        ALL("Todos"), MANAGEMENT("Gerenciamento"), SIMULATION("Simulação"), ACTION("Ação"), ADVENTURE("Aventura"), PUZZLE("Quebra-cabeça");
        private final String label;
        Category(String label) { this.label = label; }
    }

    private final Category category;
    private int page;
    private int columns;
    private int chipColumns;
    private int chipRows;
    private int chipWidth;
    private int chipTop;
    private int rows;
    private int pageSize;
    private int pageCount;
    private int cardWidth;
    private int cardHeight;
    private int cardGap;
    private int gridTop;

    public GamesHubScreen() {
        this(Category.ALL, 0);
    }

    private GamesHubScreen(Category category, int page) {
        super(Text.literal("Mega MiniGames"));
        this.category = category;
        this.page = page;
    }

    private List<Game> visibleGames() {
        if (category == Category.ALL) return GAMES;
        return GAMES.stream().filter(game -> game.category == category).toList();
    }

    private static Category categoryFor(String name) {
        String n = name.toLowerCase(java.util.Locale.ROOT);
        if (containsAny(n, "fazenda", "café", "biscoito", "cozinha", "aquário", "colônia", "trânsito", "defesa de balões", "defesa do jardim", "mini metrô", "caça-moedas")) return Category.MANAGEMENT;
        if (containsAny(n, "pescaria", "pênaltis", "mini golfe", "corrida maluca", "avião", "órbita", "clima", "equilíbrio natural", "pular corda", "pouso de precisão", "bichinho", "aquário vivo")) return Category.SIMULATION;
        if (containsAny(n, "snake", "pong", "flappy", "tetris", "quebra-blocos", "desvie", "invasores", "toupeira", "reflexo", "estrelas", "arremesso", "jetpack", "trilho", "estilingue", "corte de frutas", "sobrevivente", "corte da corda", "azulejos", "robôs", "arco de precisão", "castelo", "duelo relâmpago", "fantasmas", "arena", "salto geométrico")) return Category.ACTION;
        if (containsAny(n, "labirinto", "sapo", "tesouro", "escavador", "escalada", "ruínas", "ilha misteriosa", "mina", "relíquias", "corsário", "resgate dos mineiros")) return Category.ADVENTURE;
        return Category.PUZZLE;
    }

    private static boolean containsAny(String value, String... fragments) {
        for (String fragment : fragments) if (value.contains(fragment)) return true;
        return false;
    }

    @Override
    protected void init() {
        columns = width >= 780 ? 5 : width >= 620 ? 4 : width >= 420 ? 3 : 2;
        chipColumns = width >= 600 ? Category.values().length : 3;
        chipRows = (Category.values().length + chipColumns - 1) / chipColumns;
        chipTop = 45;
        chipWidth = (width - 32 - 6 * (chipColumns - 1)) / chipColumns;
        gridTop = chipTop + chipRows * 19 + 8;
        cardGap = width >= 620 ? 8 : 6;
        int sidePadding = width >= 420 ? 16 : 8;
        cardWidth = Math.max(72, (width - sidePadding * 2 - cardGap * (columns - 1)) / columns);
        int availableHeight = height - gridTop - 52;
        rows = Math.max(1, Math.min(3, (availableHeight + cardGap) / (72 + cardGap)));
        cardHeight = Math.max(52, Math.min(102, (availableHeight - cardGap * (rows - 1)) / rows));
        pageSize = columns * rows;
        List<Game> visible = visibleGames();
        pageCount = Math.max(1, (visible.size() + pageSize - 1) / pageSize);
        page = Math.min(page, pageCount - 1);

        int controlsY = height - 25;
        int buttonWidth = Math.min(90, Math.max(62, (width - 28) / 3));
        ButtonWidget previous = ButtonWidget.builder(Text.literal("‹ Voltar"), button ->
                        client.setScreen(new GamesHubScreen(category, Math.max(0, page - 1))))
                .dimensions(10, controlsY, buttonWidth, 20).build();
        previous.active = page > 0;
        addDrawableChild(previous);
        addDrawableChild(ButtonWidget.builder(Text.literal("Fechar"), button -> client.setScreen(null))
                .dimensions((width - buttonWidth) / 2, controlsY, buttonWidth, 20).build());
        ButtonWidget next = ButtonWidget.builder(Text.literal("Avançar ›"), button ->
                        client.setScreen(new GamesHubScreen(category, Math.min(pageCount - 1, page + 1))))
                .dimensions(width - buttonWidth - 10, controlsY, buttonWidth, 20).build();
        next.active = page < pageCount - 1;
        addDrawableChild(next);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.fill(0, 0, width, height, 0xC9081020);
        for (int y = 0; y < height; y += 4) {
            int tint = (y / 4 % 2 == 0) ? 0x080A3044 : 0x080B1A36;
            context.fill(0, y, width, Math.min(height, y + 4), tint);
        }

        context.drawTextWithShadow(textRenderer, Text.literal("MEGA ARCADE"), 16, 13, 0xFFFFFFFF);
        List<Game> visible = visibleGames();
        context.drawTextWithShadow(textRenderer, Text.literal(visible.size() + " jogos • escolha uma categoria"), 17, 30, 0xFF9FB7D5);
        String pageLabel = "PÁGINA " + (page + 1) + " / " + pageCount;
        context.drawTextWithShadow(textRenderer, Text.literal(pageLabel),
                width - textRenderer.getWidth(pageLabel) - 17, 17, 0xFFFFD76C);
        drawCategoryChips(context, mouseX, mouseY);

        int first = page * pageSize;
        int end = Math.min(first + pageSize, visible.size());
        for (int index = first; index < end; index++) {
            int slot = index - first;
            int col = slot % columns;
            int row = slot / columns;
            int x = (width - columns * cardWidth - (columns - 1) * cardGap) / 2 + col * (cardWidth + cardGap);
            int y = gridTop + row * (cardHeight + cardGap);
            drawCard(context, visible.get(index), index, x, y, mouseX, mouseY);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawCategoryChips(DrawContext context, int mouseX, int mouseY) {
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            int col = i % chipColumns, row = i / chipColumns;
            int x = 16 + col * (chipWidth + 6), y = chipTop + row * 19;
            boolean selected = categories[i] == category;
            boolean hovered = mouseX >= x && mouseX < x + chipWidth && mouseY >= y && mouseY < y + 17;
            context.fill(x, y, x + chipWidth, y + 17, selected ? 0xFF3E9C9C : hovered ? 0xFF35556D : 0xFF24374A);
            context.fill(x, y + 15, x + chipWidth, y + 17, selected ? 0xFFFFD76C : 0xFF152535);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(categories[i].label), x + chipWidth / 2, y + 4, selected ? 0xFFFFFFFF : 0xFFD3E2EF);
        }
    }

    private void drawCard(DrawContext context, Game game, int index, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + cardWidth && mouseY >= y && mouseY < y + cardHeight;
        int color = COVER_COLORS[index % COVER_COLORS.length];
        int artWidth = Math.min(54, Math.max(32, cardWidth / 3));
        int artLeft = x + cardWidth - artWidth - 6;

        context.fill(x + 2, y + 3, x + cardWidth + 2, y + cardHeight + 3, 0xAA050A14);
        context.fill(x, y, x + cardWidth, y + cardHeight, hovered ? 0xFFFFD76C : 0xFF071320);
        context.fill(x + 2, y + 2, x + cardWidth - 2, y + cardHeight - 2, color);
        context.fill(x + 3, y + 3, x + cardWidth - 3, y + cardHeight / 3, lighten(color));
        context.fill(x + 3, y + cardHeight / 3, artLeft, y + cardHeight - 3, darken(color));

        for (int i = 0; i < 3; i++) {
            int dotX = x + 8 + i * 7;
            context.fill(dotX, y + 7, dotX + 3, y + 10, 0x66FFFFFF);
        }
        drawCoverArt(context, game.name, index, artLeft, y + 8, artWidth, cardHeight - 16, color);

        int textX = x + 9;
        int maxTitleWidth = Math.max(28, artLeft - textX - 5);
        String gameName = textRenderer.trimToWidth(game.name, maxTitleWidth);
        int titleY = y + Math.max(20, cardHeight / 2 - 6);
        context.drawTextWithShadow(textRenderer, Text.literal(gameName), textX, titleY, 0xFFFFFFFF);
        if (cardHeight >= 72) context.drawTextWithShadow(textRenderer, Text.literal(game.category.label.toUpperCase(java.util.Locale.ROOT)), textX, y + cardHeight - 29, 0xFFFFD56A);
        String action = hovered ? "ABRIR AGORA  ›" : "JOGAR  ›";
        context.drawTextWithShadow(textRenderer, Text.literal(action), textX, y + cardHeight - 15,
                hovered ? 0xFFFFE68A : 0xFFB9D7E8);
        if (hovered) {
            context.fill(x + 3, y + cardHeight - 3, x + cardWidth - 3, y + cardHeight - 1, 0xFFFFD76C);
        }
    }

    private void drawCoverArt(DrawContext c, String name, int seed, int x, int y, int w, int h, int base) {
        int cx = x + w / 2;
        int cy = y + h / 2;
        c.fill(x, y, x + w, y + h, 0x66202B47);
        String low = name.toLowerCase(java.util.Locale.ROOT);
        if (low.contains("campo") || low.contains("minado") || low.contains("tetris") || low.contains("sudoku")
                || low.contains("luzes") || low.contains("2048") || low.contains("15") || low.contains("gomoku")
                || low.contains("velha") || low.contains("ligue") || low.contains("blocos") || low.contains("torre")) {
            int size = Math.max(5, Math.min(12, w / 4));
            for (int r = 0; r < 3; r++) for (int col = 0; col < 3; col++) {
                int bx = cx - size - 2 + col * (size + 2);
                int by = cy - size - 2 + r * (size + 2);
                int shade = (r * 3 + col + seed) % 3 == 0 ? 0xFFFFD45F : (r + col) % 2 == 0 ? 0xFF79D8C1 : 0xFFE8F0FF;
                c.fill(bx, by, bx + size, by + size, shade);
                if (size > 8 && (r + col + seed) % 2 == 0) {
                    c.drawCenteredTextWithShadow(textRenderer, Text.literal("" + ((r * 3 + col + seed) % 9 + 1)), bx + size / 2, by + size / 2 - 3, 0xFF26354B);
                }
            }
        } else if (low.contains("carta") || low.contains("blackjack") || low.contains("dados") || low.contains("guerra")) {
            c.fill(cx - 14, cy - 15, cx + 9, cy + 16, 0xFFEAF2F8);
            c.fill(cx - 6, cy - 17, cx + 17, cy + 14, 0xFFFFDF83);
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(seed % 2 == 0 ? "A" : "7"), cx + 5, cy - 5, 0xFF26354B);
            c.fill(cx - 9, cy + 3, cx - 2, cy + 10, 0xFFDB5364);
        } else if (low.contains("alvo") || low.contains("reflex") || low.contains("laser") || low.contains("toupeira") || low.contains("bolha")) {
            int radius = Math.max(8, Math.min(17, w / 3));
            for (int r = radius; r > 0; r -= Math.max(3, radius / 3)) {
                int ring = (r / 3) % 2 == 0 ? 0xFFE8F4FF : 0xFFE75D67;
                for (int dy = -r; dy <= r; dy++) {
                    int dx = (int) Math.sqrt(r * r - dy * dy);
                    c.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, ring);
                }
            }
            c.fill(cx - 1, cy - radius - 4, cx + 1, cy + radius + 5, 0xCCFFFFFF);
            c.fill(cx - radius - 4, cy - 1, cx + radius + 5, cy + 1, 0xCCFFFFFF);
        } else if (low.contains("palavra") || low.contains("termo") || low.contains("anagrama") || low.contains("forca") || low.contains("digita")) {
            String[] letters = {"M", "A", "G"};
            int tile = Math.max(8, Math.min(15, w / 3));
            for (int i = 0; i < 3; i++) {
                int bx = cx - tile - 2 + i * (tile + 2);
                int by = cy - tile / 2 + (i == 1 ? -4 : 3);
                c.fill(bx, by, bx + tile, by + tile, i == 1 ? 0xFF8DCB70 : 0xFFEAF1F8);
                c.drawCenteredTextWithShadow(textRenderer, Text.literal(letters[(i + seed) % 3]), bx + tile / 2, by + tile / 2 - 3, 0xFF26354B);
            }
        } else if (low.contains("snake") || low.contains("sapo") || low.contains("fruta") || low.contains("pesca") || low.contains("estrela")) {
            int unit = Math.max(5, Math.min(10, w / 5));
            for (int i = 0; i < 4; i++) {
                int bx = cx - unit * 2 + i * unit;
                int by = cy + ((i + seed) % 2 == 0 ? -unit : unit / 2);
                c.fill(bx, by, bx + unit - 1, by + unit - 1, i == 3 ? 0xFFFFE46A : 0xFF8EE6A0);
            }
            c.fill(cx + unit, cy - unit - 2, cx + unit + 2, cy - unit, 0xFF182435);
        } else {
            int radius = Math.max(8, Math.min(17, w / 3));
            for (int dy = -radius; dy <= radius; dy++) {
                int dx = (int) Math.sqrt(radius * radius - dy * dy);
                c.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, (dy + seed) % 3 == 0 ? 0xFFFFE58A : 0xFFFBF5D7);
            }
            c.fill(cx - 2, cy - 2, cx + 3, cy + 3, base);
            c.fill(cx + radius / 2, cy - radius / 2, cx + radius / 2 + 3, cy - radius / 2 + 3, 0xFFFFFFFF);
        }
        String mark = name.contains("15") ? "15" : name.contains("2048") ? "2K" : name.substring(0, Math.min(2, name.length())).toUpperCase(java.util.Locale.ROOT);
        c.drawTextWithShadow(textRenderer, Text.literal(mark), x + 3, y + 2, 0xFFFFF5DA);
    }

    private int lighten(int color) {
        int r = Math.min(255, ((color >> 16) & 255) + 24);
        int g = Math.min(255, ((color >> 8) & 255) + 24);
        int b = Math.min(255, (color & 255) + 24);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int darken(int color) {
        int r = ((color >> 16) & 255) * 3 / 5;
        int g = ((color >> 8) & 255) * 3 / 5;
        int b = (color & 255) * 3 / 5;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            int col = i % chipColumns, row = i / chipColumns;
            int x = 16 + col * (chipWidth + 6), y = chipTop + row * 19;
            if (mouseX >= x && mouseX < x + chipWidth && mouseY >= y && mouseY < y + 17) {
                client.setScreen(new GamesHubScreen(categories[i], 0));
                return true;
            }
        }
        List<Game> visible = visibleGames();
        int first = page * pageSize;
        int end = Math.min(first + pageSize, visible.size());
        for (int index = first; index < end; index++) {
            int slot = index - first;
            int col = slot % columns;
            int row = slot / columns;
            int x = (width - columns * cardWidth - (columns - 1) * cardGap) / 2 + col * (cardWidth + cardGap);
            int y = gridTop + row * (cardHeight + cardGap);
            if (mouseX >= x && mouseX < x + cardWidth && mouseY >= y && mouseY < y + cardHeight) {
                client.setScreen(visible.get(index).screenFactory.get());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT && page < pageCount - 1) {
            client.setScreen(new GamesHubScreen(category, page + 1));
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT && page > 0) {
            client.setScreen(new GamesHubScreen(category, page - 1));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private record Game(String name, Supplier<Screen> screenFactory, Category category) {
        private Game(String name, Supplier<Screen> screenFactory) {
            this(name, screenFactory, categoryFor(name));
        }
    }
}
