package br.com.megaminigames.client;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

/** Hub em grade de capas clicáveis, com filtros por categoria e visual neon arcade. */
public final class GamesHubScreen extends Screen {
    private static final List<Game> GAMES = List.of(
            new Game("Pizzaria Expressa", PizzeriaScreen::new, Category.MANAGEMENT),
            new Game("Padaria da Esquina", PadariaScreen::new, Category.MANAGEMENT),
            new Game("Sorveteria Gelada", SorveteriaScreen::new, Category.MANAGEMENT),
            new Game("Mercado do Bairro", () -> TycoonScreen.of(TycoonScreen.Kind.MERCADO), Category.MANAGEMENT),
            new Game("Cinema Pipoca", () -> TycoonScreen.of(TycoonScreen.Kind.CINEMA), Category.MANAGEMENT),
            new Game("Lanchonete Turbo", () -> TycoonScreen.of(TycoonScreen.Kind.LANCHONETE), Category.MANAGEMENT),
            new Game("Floricultura Bella", () -> TycoonScreen.of(TycoonScreen.Kind.FLORICULTURA), Category.MANAGEMENT),
            new Game("Farmácia 24h", () -> TycoonScreen.of(TycoonScreen.Kind.FARMACIA), Category.MANAGEMENT),
            new Game("Sushi Rápido", () -> TycoonScreen.of(TycoonScreen.Kind.SUSHI), Category.MANAGEMENT),
            new Game("Loja de Brinquedos", () -> TycoonScreen.of(TycoonScreen.Kind.BRINQUEDOS), Category.MANAGEMENT),
            new Game("Fábrica de Robôs", () -> TycoonScreen.of(TycoonScreen.Kind.FABRICA), Category.MANAGEMENT),
            new Game("Porto Container", () -> TycoonScreen.of(TycoonScreen.Kind.PORTO), Category.MANAGEMENT),
            new Game("Fazenda Solar", () -> TycoonScreen.of(TycoonScreen.Kind.SOLAR), Category.MANAGEMENT),
            new Game("Estufa Hidropônica", () -> TycoonScreen.of(TycoonScreen.Kind.ESTUFA), Category.MANAGEMENT),
            new Game("Apiário Doce Mel", () -> TycoonScreen.of(TycoonScreen.Kind.APIARIO), Category.MANAGEMENT),
            new Game("Mineradora Profunda", () -> TycoonScreen.of(TycoonScreen.Kind.MINERADORA), Category.MANAGEMENT),
            new Game("Ferrovia Central", () -> TycoonScreen.of(TycoonScreen.Kind.FERROVIA), Category.MANAGEMENT),
            new Game("Fábrica de Sucos", () -> TycoonScreen.of(TycoonScreen.Kind.SUCOS), Category.MANAGEMENT),
            new Game("Gráfica Expressa", () -> TycoonScreen.of(TycoonScreen.Kind.GRAFICA), Category.MANAGEMENT),
            new Game("Cerâmica Vila", () -> TycoonScreen.of(TycoonScreen.Kind.CERAMICA), Category.MANAGEMENT),
            new Game("Hospital Central", () -> TycoonScreen.of(TycoonScreen.Kind.HOSPITAL), Category.MANAGEMENT),
            new Game("Banco Seguro", () -> TycoonScreen.of(TycoonScreen.Kind.BANCO), Category.MANAGEMENT),
            new Game("Redação do Jornal", () -> TycoonScreen.of(TycoonScreen.Kind.JORNAL), Category.MANAGEMENT),
            new Game("Aeroporto Internacional", () -> TycoonScreen.of(TycoonScreen.Kind.AEROPORTO), Category.MANAGEMENT),
            new Game("Hotel Estrela", () -> TycoonScreen.of(TycoonScreen.Kind.HOTEL), Category.MANAGEMENT),
            new Game("Zoológico Urbano", () -> TycoonScreen.of(TycoonScreen.Kind.ZOOLOGICO), Category.MANAGEMENT),
            new Game("Pinball Neon", PinballScreen::new, Category.ACTION),
            new Game("Batalha de Tanques", TankScreen::new, Category.ACTION),
            new Game("Nonograma", NonogramScreen::new, Category.PUZZLE),
            new Game("Hóquei de Ar", AirHockeyScreen::new, Category.SIMULATION),
            new Game("Trânsito Travado", RushHourScreen::new, Category.PUZZLE),
            new Game("Empurra-Caixas", SokobanScreen::new, Category.PUZZLE),
            new Game("Senha Secreta", MastermindScreen::new, Category.PUZZLE),
            new Game("Bomba!", BombermanScreen::new, Category.ACTION),
            new Game("Salto nas Nuvens", DoodleJumpScreen::new, Category.ACTION),
            new Game("Batida Perfeita", RhythmScreen::new, Category.ACTION),
            new Game("Dominó", DominoScreen::new, Category.PUZZLE),
            new Game("Paciência", KlondikeScreen::new, Category.PUZZLE),
            new Game("Sinuca", PoolScreen::new, Category.SIMULATION),
            new Game("Duelo de Heróis", HeroDuelScreen::new, Category.ADVENTURE),
            new Game("Mini Plataforma", PlatformerScreen::new, Category.ACTION),
            new Game("Come-Come", PacManScreen::new, Category.ACTION),
            new Game("Asteroides", AsteroidsScreen::new, Category.ACTION),
            new Game("Boliche", BowlingScreen::new, Category.SIMULATION),
            new Game("Reversi", ReversiScreen::new, Category.PUZZLE),
            new Game("Torre de Hanói", HanoiScreen::new, Category.PUZZLE),
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
            new Game("Pouso de Precisão", LandingScreen::new, Category.SIMULATION),
            new Game("Meu Bichinho", PetScreen::new, Category.SIMULATION),
            new Game("Órbita Estável", OrbitScreen::new, Category.SIMULATION),
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
            new Game("Mapa do Corsário", () -> new CategoryArcadeScreen(CategoryArcadeScreen.Game.PIRATE), Category.ADVENTURE),
            new Game("Livraria em Dia", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.BOOKSHOP), Category.MANAGEMENT),
            new Game("Estação de Recursos", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.STATION), Category.MANAGEMENT),
            new Game("Abrigo Animal", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.SHELTER), Category.MANAGEMENT),
            new Game("Rali de Montanha", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.RALLY), Category.SIMULATION),
            new Game("Mergulho Azul", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.DIVE), Category.SIMULATION),
            new Game("Balão Meteorológico", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.WEATHER_BALLOON), Category.SIMULATION),
            new Game("Duelo de Lasers", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.LASER_DUEL), Category.ACTION),
            new Game("Chuva de Meteoros", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.METEORS), Category.ACTION),
            new Game("Arco Guardião", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.ARCHER), Category.ACTION),
            new Game("Caverna do Cristal", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.CAVE), Category.ADVENTURE),
            new Game("Templo em Fuga", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.TEMPLE), Category.ADVENTURE),
            new Game("Expedição na Neve", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.SNOW), Category.ADVENTURE),
            new Game("Mosaico 3×3", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.MOSAIC), Category.PUZZLE),
            new Game("Conserte os Canos", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.PIPES), Category.PUZZLE),
            new Game("Palavra Ponte", () -> new VarietyArcadeScreen(VarietyArcadeScreen.Game.WORD_BRIDGE), Category.PUZZLE)
    );

    private enum Category {
        ALL("mega_minigames.category.all", "Todos", ArcadeTheme.GOLD),
        MANAGEMENT("mega_minigames.category.management", "Gerenciamento", ArcadeTheme.LIME),
        SIMULATION("mega_minigames.category.simulation", "Simulação", ArcadeTheme.SKY),
        ACTION("mega_minigames.category.action", "Ação", ArcadeTheme.MAGENTA),
        ADVENTURE("mega_minigames.category.adventure", "Aventura", ArcadeTheme.CORAL),
        PUZZLE("mega_minigames.category.puzzle", "Quebra-cabeça", ArcadeTheme.PURPLE);

        private final String key;
        private final String label;
        private final int color;

        Category(String key, String label, int color) {
            this.key = key;
            this.label = label;
            this.color = color;
        }

        private String label() {
            return I18n.translate(key);
        }
    }

    private final Category category;
    private final int[] chipX = new int[Category.values().length];
    private int page;
    private int columns;
    private int chipColumns;
    private int chipRows;
    private int chipWidth;
    private int chipTop;
    private int chipHeight;
    private int rows;
    private int pageSize;
    private int pageCount;
    private int cardWidth;
    private int cardHeight;
    private int cardGap;
    private int gridTop;
    private int sidePadding;
    private int heroX1;
    private int heroY1;
    private int heroX2;
    private int heroY2;

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
        if (containsAny(n, "snake", "pong", "flappy", "quebra-blocos", "desvie", "invasores", "toupeira", "reflexo", "estrelas", "arremesso", "jetpack", "trilho", "corrida de faixas", "salto de obstáculos", "pular corda", "estilingue", "corte de frutas", "sobrevivente", "corte da corda", "azulejos", "robôs", "arco de precisão", "castelo", "duelo relâmpago", "fantasmas", "arena", "salto geométrico")) return Category.ACTION;
        if (containsAny(n, "labirinto", "sapo", "tesouro", "escavador", "escalada", "ruínas", "ilha misteriosa", "mina", "relíquias", "corsário", "resgate dos mineiros")) return Category.ADVENTURE;
        return Category.PUZZLE;
    }

    private static boolean containsAny(String value, String... fragments) {
        for (String fragment : fragments) if (value.contains(fragment)) return true;
        return false;
    }

    @Override
    protected void init() {
        sidePadding = width >= 480 ? 14 : 8;
        heroX1 = sidePadding;
        heroX2 = width - sidePadding;
        heroY1 = Math.max(6, height / 60);
        heroY2 = Math.min(64, Math.max(46, height / 5));
        columns = width >= 860 ? 5 : width >= 660 ? 4 : width >= 430 ? 3 : 2;
        chipColumns = width >= 700 ? 6 : width >= 340 ? 3 : 2;
        chipRows = (Category.values().length + chipColumns - 1) / chipColumns;
        chipTop = heroY2 + 8;
        chipHeight = 18;
        int chipGap = 6;
        chipWidth = Math.max(58, (width - sidePadding * 2 - chipGap * (chipColumns - 1)) / chipColumns);
        for (int i = 0; i < Category.values().length; i++) {
            chipX[i] = sidePadding + (i % chipColumns) * (chipWidth + chipGap);
        }
        gridTop = chipTop + chipRows * (chipHeight + 4) + 4;
        cardGap = width >= 620 ? 8 : 6;
        cardWidth = Math.max(64, (width - sidePadding * 2 - cardGap * (columns - 1)) / columns);
        int footerTop = height - 44;
        int availableHeight = Math.max(52, footerTop - gridTop);
        rows = Math.max(1, Math.min(4, (availableHeight + cardGap) / (72 + cardGap)));
        cardHeight = Math.max(50, Math.min(96, (availableHeight - cardGap * (rows - 1)) / rows));
        pageSize = columns * rows;
        List<Game> visible = visibleGames();
        pageCount = Math.max(1, (visible.size() + pageSize - 1) / pageSize);
        page = Math.min(page, pageCount - 1);

        int controlsY = height - 26;
        int buttonWidth = Math.min(94, Math.max(58, (width - sidePadding * 2 - 24) / 3));
        ButtonWidget previous = ArcadeTheme.button(sidePadding, controlsY, buttonWidth, 20,
                Text.translatable("mega_minigames.hub.previous"), () -> client.setScreen(new GamesHubScreen(category, Math.max(0, page - 1))));
        previous.active = page > 0;
        addDrawableChild(previous);
        addDrawableChild(ArcadeTheme.button((width - buttonWidth) / 2, controlsY, buttonWidth, 20,
                Text.translatable("mega_minigames.hub.close"), () -> client.setScreen(null)));
        ButtonWidget next = ArcadeTheme.button(width - sidePadding - buttonWidth, controlsY, buttonWidth, 20,
                Text.translatable("mega_minigames.hub.next"), () -> client.setScreen(new GamesHubScreen(category, Math.min(pageCount - 1, page + 1))));
        next.active = page < pageCount - 1;
        addDrawableChild(next);

        if (width >= 600) {
            addDrawableChild(ArcadeTheme.button(heroX2 - 104, heroY2 - 26, 96, 18,
                    Text.translatable("mega_minigames.hub.random"), this::openRandomGame));
        }
    }

    /** Sorteia um jogo do catálogo completo (usado pelo Console Retrô ao agachar). */
    public static Screen randomGame() {
        return GAMES.get(ThreadLocalRandom.current().nextInt(GAMES.size())).screenFactory.get();
    }

    private void openRandomGame() {
        List<Game> visible = visibleGames();
        if (visible.isEmpty()) return;
        Game game = visible.get(ThreadLocalRandom.current().nextInt(visible.size()));
        client.setScreen(game.screenFactory.get());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        drawHero(context, mouseX, mouseY);
        drawCategoryChips(context, mouseX, mouseY);

        List<Game> visible = visibleGames();
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
        drawPager(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawHero(DrawContext context, int mouseX, int mouseY) {
        int accent = category.color;
        ArcadeTheme.rounded(context, heroX1 + 2, heroY1 + 3, heroX2 + 2, heroY2 + 3, 10, 0x55000000);
        ArcadeTheme.rounded(context, heroX1, heroY1, heroX2, heroY2, 10, ArcadeTheme.alpha(ArcadeTheme.mix(ArcadeTheme.TEAL, accent, 0.5F), 0x66));
        ArcadeTheme.rounded(context, heroX1 + 1, heroY1 + 1, heroX2 - 1, heroY2 - 1, 9, 0xF00D1A2C);
        ArcadeTheme.rounded(context, heroX1 + 2, heroY1 + 2, heroX2 - 2, heroY1 + (heroY2 - heroY1) / 2, 8, 0x10FFFFFF);
        ArcadeTheme.glow(context, heroX1 + 32, heroY1 + (heroY2 - heroY1) / 2, 30, ArcadeTheme.TEAL);

        int logoSize = Math.min(44, heroY2 - heroY1 - 10);
        drawArcadeLogo(context, heroX1 + 8, heroY1 + (heroY2 - heroY1 - logoSize) / 2, logoSize);

        int textX = heroX1 + 60;
        int textRight = heroX2 - (width >= 600 ? 112 : 10);
        context.drawTextWithShadow(textRenderer, Text.literal("MEGA MINIGAMES"), textX, heroY1 + 10, 0xFFFFE9A8);
        String subtitle = I18n.translate("mega_minigames.hub.subtitle", visibleGames().size());
        context.drawTextWithShadow(textRenderer, Text.literal(textRenderer.trimToWidth(subtitle, Math.max(30, textRight - textX))),
                textX, heroY1 + 23, ArcadeTheme.MUTED);
        if (heroY2 - heroY1 >= 52) {
            String hint = I18n.translate("mega_minigames.hub.hint");
            context.drawTextWithShadow(textRenderer, Text.literal(textRenderer.trimToWidth(hint, Math.max(30, textRight - textX))),
                    textX, heroY1 + 35, 0xFF7FA6C9);
        }

        if (width >= 520) {
            String pageLabel = I18n.translate("mega_minigames.hub.page", page + 1, pageCount);
            int pillWidth = textRenderer.getWidth(pageLabel) + 16;
            ArcadeTheme.rounded(context, heroX2 - pillWidth - 8, heroY1 + 6, heroX2 - 8, heroY1 + 22, 7, 0xD90B1626);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(pageLabel), heroX2 - pillWidth / 2 - 8, heroY1 + 9, ArcadeTheme.GOLD);
        }
    }

    private void drawArcadeLogo(DrawContext context, int x, int y, int size) {
        int u = Math.max(1, size / 16);
        int width = 14 * u;
        int left = x + (size - width) / 2;
        int top = y + Math.max(0, (size - 15 * u) / 2);
        context.fill(left + 2 * u, top + 15 * u, left + 12 * u, top + 16 * u, 0x66000000);
        ArcadeTheme.rounded(context, left + 4 * u, top + 2 * u, left + 12 * u, top + 14 * u, 2 * u, 0xFF28B3A6);
        context.fill(left + 5 * u, top + 13 * u, left + 6 * u, top + 15 * u, 0xFF1E857C);
        context.fill(left + 10 * u, top + 13 * u, left + 11 * u, top + 15 * u, 0xFF1E857C);
        ArcadeTheme.rounded(context, left + 5 * u, top + 3 * u, left + 11 * u, top + 5 * u, u, ArcadeTheme.LIME);
        context.fill(left + 5 * u, top + 5 * u, left + 11 * u, top + 9 * u, 0xFF08111F);
        ArcadeTheme.star(context, left + 8 * u, top + 7 * u, Math.max(1, u), ArcadeTheme.GOLD);
        context.fill(left + 4 * u, top + 9 * u, left + 12 * u, top + 11 * u, 0xFF1E857C);
        context.fill(left + 8 * u, top + 9 * u, left + 9 * u, top + 10 * u, ArcadeTheme.MAGENTA);
        context.fill(left + 10 * u, top + 9 * u, left + 11 * u, top + 10 * u, ArcadeTheme.GOLD);
        context.fillGradient(left + 5 * u, top + 2 * u, left + 11 * u, top + 4 * u, 0x33B9F26B, 0x00B9F26B);
    }

    private void drawCategoryChips(DrawContext context, int mouseX, int mouseY) {
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            int x = chipX[i];
            int y = chipTop + (i / chipColumns) * (chipHeight + 4);
            boolean selected = categories[i] == category;
            boolean hovered = mouseX >= x && mouseX < x + chipWidth && mouseY >= y && mouseY < y + chipHeight;
            int accent = categories[i].color;
            if (selected) {
                ArcadeTheme.rounded(context, x, y, x + chipWidth, y + chipHeight, 9, accent);
                ArcadeTheme.rounded(context, x + 1, y + 1, x + chipWidth - 1, y + 7, 8, 0x33FFFFFF);
                context.fill(x + 6, y + 7, x + 9, y + 11, 0xFF0A1422);
                context.drawTextWithShadow(textRenderer, Text.literal(textRenderer.trimToWidth(categories[i].label(), chipWidth - 20)),
                        x + 13, y + 5, 0xFF08121F);
            } else {
                ArcadeTheme.rounded(context, x, y, x + chipWidth, y + chipHeight, 9,
                        hovered ? 0xCC22344E : 0xCC111E30);
                ArcadeTheme.rounded(context, x + 1, y + 1, x + chipWidth - 1, y + chipHeight - 1, 8, 0x10FFFFFF);
                context.fill(x + 6, y + 7, x + 9, y + 11, accent);
                context.drawTextWithShadow(textRenderer, Text.literal(textRenderer.trimToWidth(categories[i].label(), chipWidth - 20)),
                        x + 13, y + 5, hovered ? ArcadeTheme.TEXT : ArcadeTheme.MUTED);
            }
        }
    }

    private void drawCard(DrawContext context, Game game, int index, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + cardWidth && mouseY >= y && mouseY < y + cardHeight;
        int accent = ArcadeTheme.mix(game.category.color, ArcadeTheme.accentFor(game.name), 0.4F);
        int top = hovered ? y - 1 : y;
        int radius = 8;
        ArcadeTheme.rounded(context, x + 2, top + 4, x + cardWidth + 2, top + cardHeight + 4, radius, 0x55000000);
        if (hovered) {
            ArcadeTheme.rounded(context, x - 1, top - 1, x + cardWidth + 1, top + cardHeight + 1, radius + 1, accent);
        }
        ArcadeTheme.rounded(context, x, top, x + cardWidth, top + cardHeight, radius,
                hovered ? ArcadeTheme.alpha(accent, 0xEE) : 0xCC26384F);
        ArcadeTheme.rounded(context, x + 1, top + 1, x + cardWidth - 1, top + cardHeight - 1, radius - 1, 0xF00C1727);

        int coverHeight = Math.max(26, cardHeight - 26);
        ArcadeTheme.rounded(context, x + 2, top + 2, x + cardWidth - 2, top + coverHeight, radius - 2,
                ArcadeTheme.mix(accent, 0xFF0A1422, 0.62F));
        ArcadeTheme.rounded(context, x + 2, top + 2, x + cardWidth - 2, top + coverHeight / 2 + 2, radius - 2,
                ArcadeTheme.alpha(ArcadeTheme.lighten(accent, 34), 0x5A));
        for (int line = top + 4; line < top + coverHeight - 1; line += 3) {
            context.fill(x + 3, line, x + cardWidth - 3, line + 1, 0x12000000);
        }
        int iconSize = Math.max(8, Math.min(26, coverHeight / 2 - 2));
        drawGameArt(context, game.name, x + cardWidth / 2, top + 2 + (coverHeight - 2) / 2, iconSize,
                ArcadeTheme.lighten(accent, 60));
        context.drawTextWithShadow(textRenderer, Text.literal(String.format("%02d", index + 1)), x + 6, top + 5, 0x88FFFFFF);
        if (index < 5) {
            String badge = I18n.translate("mega_minigames.hub.new");
            int badgeWidth = textRenderer.getWidth(badge) + 8;
            int badgeX = x + cardWidth - badgeWidth - 4;
            ArcadeTheme.rounded(context, badgeX, top + 4, badgeX + badgeWidth, top + 14, 5, 0xEEFFD66B);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(badge), badgeX + badgeWidth / 2,
                    top + 6, 0xFF231703);
        }
        if (hovered) {
            int badgeX = x + cardWidth - 12;
            int badgeY = top + 11;
            ArcadeTheme.disc(context, badgeX, badgeY, 7, ArcadeTheme.GOLD);
            context.fill(badgeX - 1, badgeY - 3, badgeX, badgeY + 4, 0xFF0A1422);
            context.fill(badgeX, badgeY - 2, badgeX + 1, badgeY + 3, 0xFF0A1422);
            context.fill(badgeX + 1, badgeY - 1, badgeX + 2, badgeY + 2, 0xFF0A1422);
        }

        context.fill(x + 2, top + coverHeight, x + cardWidth - 2, top + cardHeight - 2, 0xF0091220);
        int textX = x + 7;
        int maxWidth = cardWidth - 14;
        String name = textRenderer.trimToWidth(game.name, maxWidth);
        context.drawTextWithShadow(textRenderer, Text.literal(name), textX, top + coverHeight + 4,
                hovered ? 0xFFFFFFFF : ArcadeTheme.TEXT);
        if (cardHeight >= 68) {
            String tag = hovered ? I18n.translate("mega_minigames.hub.open")
                    : game.category.label().toUpperCase(java.util.Locale.ROOT);
            context.drawTextWithShadow(textRenderer, Text.literal(textRenderer.trimToWidth(tag, maxWidth)), textX,
                    top + cardHeight - 15, hovered ? ArcadeTheme.GOLD : ArcadeTheme.alpha(accent, 0xFF));
        }
        context.fill(x + 7, top + coverHeight + 2, x + Math.min(cardWidth - 7, 27), top + coverHeight + 3,
                hovered ? ArcadeTheme.GOLD : accent);
    }


    /** Desenha uma pequena ilustração específica para cada jogo na capa do cartão. */
    private void drawGameArt(DrawContext context, String rawName, int centerX, int centerY, int size, int color) {
        String name = rawName.toLowerCase(java.util.Locale.ROOT);
        int half = Math.max(4, size / 2);
        int dark = ArcadeTheme.darken(color, 70);
        int light = ArcadeTheme.lighten(color, 55);
        if (drawTycoonArt(context, name, centerX, centerY, half, color, light)) {
            return;
        }
        if (name.contains("pinball") || name.contains("fliperama")) {
            ArcadeTheme.rounded(context, centerX - half + 2, centerY - half, centerX + half - 2,
                    centerY + half, 3, 0xFF2B1B52);
            ArcadeTheme.ring(context, centerX - half / 3, centerY - half / 3, 4, 1, ArcadeTheme.MAGENTA);
            ArcadeTheme.ring(context, centerX + half / 3, centerY - half / 3, 4, 1, ArcadeTheme.LIME);
            ArcadeTheme.disc(context, centerX, centerY, 5, 0xFFFFE9A8);
            context.fill(centerX - half / 2, centerY + half / 2, centerX - 2, centerY + half / 2 + 2,
                    ArcadeTheme.GOLD);
            context.fill(centerX + 2, centerY + half / 2, centerX + half / 2, centerY + half / 2 + 2,
                    ArcadeTheme.GOLD);
        } else if (name.contains("tanques") || name.contains("tanque")) {
            ArcadeTheme.rounded(context, centerX - half, centerY, centerX + half, centerY + half, 3,
                    0xFF5C6B80);
            ArcadeTheme.rounded(context, centerX - half + 2, centerY + 2, centerX + half - 2, centerY + half - 2,
                    2, 0xFF8B4A35);
            ArcadeTheme.rounded(context, centerX - half / 2, centerY - 2, centerX + half / 2, centerY + 12, 3,
                    ArcadeTheme.LIME);
            context.fill(centerX - 2, centerY - half, centerX + 2, centerY + 2,
                    ArcadeTheme.lighten(ArcadeTheme.LIME, 30));
            ArcadeTheme.disc(context, centerX, centerY + 5, 3, ArcadeTheme.darken(ArcadeTheme.LIME, 40));
        } else if (name.contains("nonograma")) {
            int block = Math.max(4, half / 2);
            for (int row = 0; row < 4; row++) {
                for (int column = 0; column < 4; column++) {
                    int x = centerX - half + column * block;
                    int y = centerY - half + row * block;
                    context.fill(x, y, x + block - 1, y + block - 1,
                            (row + column) % 3 == 0 ? ArcadeTheme.PURPLE : 0xFF1A2438);
                }
            }
        } else if (name.contains("hóquei") || name.contains("hoquei") || name.contains("air hockey")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half, centerX + half, centerY + half, 4,
                    0xFF153C55);
            context.fill(centerX - half, centerY, centerX + half, centerY + 1, 0x66FFFFFF);
            ArcadeTheme.ring(context, centerX, centerY, half / 2, 1, 0x44FFFFFF);
            ArcadeTheme.disc(context, centerX, centerY + half / 3, 3, ArcadeTheme.SKY);
            ArcadeTheme.disc(context, centerX - 2, centerY - half / 3, 3, ArcadeTheme.CORAL);
            ArcadeTheme.disc(context, centerX + 4, centerY - 2, 2, ArcadeTheme.GOLD);
        } else if (name.contains("trânsito") || name.contains("transito") || name.contains("rush hour")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - 2, centerX + half - 4, centerY + 7, 2,
                    0xFFE05263);
            ArcadeTheme.rounded(context, centerX - 2, centerY - half, centerX + 6, centerY - 4, 2,
                    0xFF4FB3D9);
            ArcadeTheme.rounded(context, centerX - half + 2, centerY + half - 6, centerX - 4, centerY + half, 2,
                    0xFF9AAFC7);
            context.fill(centerX + half - 2, centerY - 1, centerX + half, centerY + 6, ArcadeTheme.LIME);
            ArcadeTheme.ring(context, centerX - half / 3, centerY + 2, 3, 1, 0x88FFFFFF);
        } else if (name.contains("empurra-caixas") || name.contains("sokoban")) {
            ArcadeTheme.rounded(context, centerX - half + 2, centerY - half + 2, centerX + half - 2,
                    centerY + half - 2, 2, 0xFFC58B4E);
            ArcadeTheme.rounded(context, centerX - half + 3, centerY - half + 3, centerX + half - 3,
                    centerY + half - 3, 1, 0xFFE0A968);
            context.fill(centerX - half + 4, centerY - half + 4, centerX + half - 4,
                    centerY - half + 5, 0xFF8A5A2E);
            context.fill(centerX - half + 4, centerY + half - 5, centerX + half - 4,
                    centerY + half - 4, 0xFF8A5A2E);
            ArcadeTheme.ring(context, centerX, centerY + half + 4, 3, 1, ArcadeTheme.GOLD);
        } else if (name.contains("senha secreta") || name.contains("mastermind")) {
            for (int index = 0; index < 4; index++) {
                ArcadeTheme.disc(context, centerX - half + index * (half * 2 / 4), centerY - 3, 3,
                        index % 2 == 0 ? ArcadeTheme.CORAL : ArcadeTheme.SKY);
            }
            ArcadeTheme.disc(context, centerX - half / 2, centerY + half / 2, 2, 0xFFFFFFFF);
            ArcadeTheme.disc(context, centerX + half / 2, centerY + half / 2, 2, 0xFFFFFFFF);
            ArcadeTheme.ring(context, centerX + half / 2, centerY + half / 2, 2, 1, ArcadeTheme.GOLD);
        } else if (name.contains("bomba!")) {
            ArcadeTheme.disc(context, centerX, centerY + half / 3, half - 2, 0xFF1B2230);
            ArcadeTheme.disc(context, centerX - half / 4, centerY + half / 4, 1, 0x88FFFFFF);
            context.fill(centerX, centerY - half, centerX + 1, centerY, 0xFFB98A5A);
            ArcadeTheme.glow(context, centerX, centerY - half, 4, 0xAAFFD66B);
            ArcadeTheme.disc(context, centerX, centerY - half, 1, 0xFFFFF3C4);
        } else if (name.contains("salto nas nuvens") || name.contains("doodle")) {
            ArcadeTheme.rounded(context, centerX - half, centerY + half / 2, centerX + half,
                    centerY + half / 2 + 3, 1, ArcadeTheme.LIME);
            ArcadeTheme.rounded(context, centerX - half / 3, centerY - half / 2, centerX + half / 3,
                    centerY + half / 2, 3, ArcadeTheme.LIME);
            ArcadeTheme.disc(context, centerX - 2, centerY - half / 4, 1, 0xFFFFFFFF);
            ArcadeTheme.disc(context, centerX + 2, centerY - half / 4, 1, 0xFFFFFFFF);
            context.fill(centerX - half - 2, centerY - half - 4, centerX, centerY - half - 2, ArcadeTheme.GOLD);
        } else if (name.contains("batida perfeita") || name.contains("ritmo")) {
            for (int lane = 0; lane < 4; lane++) {
                int laneX = centerX - half + lane * (half * 2 / 4);
                context.fill(laneX, centerY - half, laneX + 2, centerY + half, 0x22FFFFFF);
                context.fill(laneX, centerY + half - 4 - lane * 2, laneX + 2, centerY + half - 1,
                        lane == 1 ? ArcadeTheme.GOLD : lane == 3 ? ArcadeTheme.MAGENTA : ArcadeTheme.TEAL);
            }
        } else if (name.contains("dominó") || name.contains("domino")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half / 2, centerX + half,
                    centerY + half / 2, 2, 0xFFF3F0E4);
            context.fill(centerX, centerY - half / 2 + 1, centerX + 1, centerY + half / 2 - 1, 0xFF9E9785);
            ArcadeTheme.disc(context, centerX - half / 2, centerY - half / 4, 1, 0xFF22334A);
            ArcadeTheme.disc(context, centerX - half / 2, centerY + half / 4, 1, 0xFF22334A);
            ArcadeTheme.disc(context, centerX + half / 2, centerY, 1, 0xFF22334A);
        } else if (name.contains("paciência") || name.contains("paciencia") || name.contains("klondike")) {
            ArcadeTheme.playingCard(context, textRenderer, centerX - half, centerY - half,
                    half + 2, half * 2, "A", 0, false);
            ArcadeTheme.playingCard(context, textRenderer, centerX - half / 2, centerY - half + 4,
                    half + 2, half * 2, "K", 3, false);
        } else if (name.contains("sinuca") || name.contains("bilhar")) {
            ArcadeTheme.disc(context, centerX, centerY + half / 4, half - 2, 0xFF17734A);
            ArcadeTheme.disc(context, centerX - half / 3, centerY + half / 3, 1, 0x66FFFFFF);
            for (int step = 0; step < half; step += 3) {
                context.fill(centerX + 2 + step, centerY - half - 2 + step, centerX + 4 + step,
                        centerY - half + step, 0xFFE5B984);
            }
        } else if (name.contains("duelo de heróis") || name.contains("heroi")) {
            context.fill(centerX - 4, centerY - half, centerX - 2, centerY + half, 0xFFD9E6F5);
            context.fill(centerX - half / 2, centerY - 2, centerX + half / 4, centerY, ArcadeTheme.GOLD);
            ArcadeTheme.disc(context, centerX, centerY + 2, 2, ArcadeTheme.GOLD);
            ArcadeTheme.rounded(context, centerX + 1, centerY - half / 2, centerX + half,
                    centerY + half / 2, 3, 0xFF3E6FA3);
            context.fill(centerX + half / 3, centerY - half / 4, centerX + half / 3 + 1,
                    centerY + half / 4, 0xFFD9E6F5);
        } else if (name.contains("mini plataforma") || name.contains("plataforma")) {
            context.fill(centerX - half, centerY + half / 2, centerX + half, centerY + half, 0xFF6E4E2C);
            context.fill(centerX - half, centerY + half / 2, centerX + half, centerY + half / 2 + 2,
                    0xFF4FA34A);
            ArcadeTheme.disc(context, centerX, centerY - half / 2, 4, ArcadeTheme.GOLD);
            ArcadeTheme.ring(context, centerX, centerY - half / 2, 4, 1, 0xFFFFF3C4);
        } else if (name.contains("come-come") || name.contains("pac-man")) {
            ArcadeTheme.disc(context, centerX, centerY, half, ArcadeTheme.GOLD);
            int[] mouthX = {centerX, centerX + half + 2, centerX + half + 2};
            int[] mouthY = {centerY, centerY - half / 2, centerY + half / 2};
            ArcadeTheme.polygon(context, mouthX, mouthY, ArcadeTheme.PANEL_DEEP);
            ArcadeTheme.disc(context, centerX - half / 4, centerY - half / 2, 1, 0xFF201A05);
            for (int index = 0; index < 3; index++) {
                ArcadeTheme.disc(context, centerX - half - 3 - index * 4, centerY + half / 3, 1, 0x88FFF0C0);
            }
        } else if (name.contains("asteroides")) {
            int[] rockX = {centerX - half, centerX - half / 2, centerX + half / 2, centerX + half,
                    centerX + half / 3, centerX - half / 2};
            int[] rockY = {centerY, centerY - half, centerY - half / 2, centerY + half / 3,
                    centerY + half, centerY + half / 3};
            ArcadeTheme.polygon(context, rockX, rockY, ArcadeTheme.darken(color, 40));
            ArcadeTheme.disc(context, centerX - half / 3, centerY, 2, ArcadeTheme.PANEL_DEEP);
            ArcadeTheme.disc(context, centerX + half / 4, centerY + half / 3, 1, ArcadeTheme.PANEL_DEEP);
            context.fill(centerX + half + 2, centerY - half - 3, centerX + half + 3, centerY - 2, ArcadeTheme.SKY);
        } else if (name.contains("reversi")) {
            ArcadeTheme.disc(context, centerX - half / 2, centerY - half / 2, half / 2, 0xFF0C1420);
            ArcadeTheme.disc(context, centerX - half / 2 - 1, centerY - half / 2 - 1, Math.max(1, half / 4),
                    0x88A9C2E0);
            ArcadeTheme.disc(context, centerX + half / 2, centerY - half / 2, half / 2, 0xFFFFFFFF);
            ArcadeTheme.disc(context, centerX - half / 2, centerY + half / 2, half / 2, 0xFFFFFFFF);
            ArcadeTheme.disc(context, centerX + half / 2, centerY + half / 2, half / 2, 0xFF0C1420);
        } else if (name.contains("hanói") || name.contains("hanoi")) {
            for (int index = 0; index < 3; index++) {
                int barWidth = half - index * (half / 3);
                int barY = centerY + half - index * (half / 2) - 3;
                ArcadeTheme.rounded(context, centerX - barWidth, barY, centerX + barWidth, barY + half / 3, 2,
                        index == 0 ? ArcadeTheme.CORAL : index == 1 ? ArcadeTheme.GOLD : ArcadeTheme.LIME);
            }
            context.fill(centerX - 1, centerY - half, centerX + 1, centerY + half, ArcadeTheme.BORDER);
        } else if (name.contains("boliche")) {
            ArcadeTheme.disc(context, centerX - half / 3, centerY + half / 3, half - 1, 0xFF16233A);
            ArcadeTheme.disc(context, centerX - half / 3 - 1, centerY + half / 3 - 1, half - 3, 0xFF3E5A86);
            for (int index = 0; index < 3; index++) {
                ArcadeTheme.disc(context, centerX - half / 3 - 2 + index * 3, centerY + half / 4, 1, 0xFF08111E);
            }
            context.fill(centerX + half - 2, centerY - half, centerX + half, centerY - 2, 0xFFF7FAFF);
            context.fill(centerX + half - 3, centerY - half / 3, centerX + half + 1, centerY - half / 3 + 2, 0xFFE0465C);
        } else if (name.contains("snake") || name.contains("cobrinha")) {
            for (int index = 0; index < 4; index++) {
                int segmentX = centerX - half + index * (half / 2);
                int segmentY = centerY + (index % 2 == 0 ? half / 3 : -half / 3);
                ArcadeTheme.rounded(context, segmentX - 2, segmentY - 2, segmentX + 2, segmentY + 2, 1, color);
            }
            ArcadeTheme.disc(context, centerX + half - 1, centerY - half / 2, 2, ArcadeTheme.GOLD);
        } else if (name.contains("tetris")) {
            context.fill(centerX - half, centerY, centerX - 1, centerY + half, color);
            context.fill(centerX - 1, centerY, centerX + half, centerY + 3, color);
            context.fill(centerX + 1, centerY + 3, centerX + half, centerY + half, light);
            context.fill(centerX - half + 1, centerY + 1, centerX - half + 3, centerY + 3, 0x66FFFFFF);
        } else if (name.contains("2048")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half, centerX + half, centerY + half, 2, dark);
            ArcadeTheme.rounded(context, centerX - half + 1, centerY - half + 1, centerX - 1, centerY - 1, 2, color);
            ArcadeTheme.rounded(context, centerX + 1, centerY - half + 1, centerX + half - 1, centerY - 1, 2, light);
            ArcadeTheme.rounded(context, centerX - half + 1, centerY + 1, centerX - 1, centerY + half - 1, 2, light);
            ArcadeTheme.rounded(context, centerX + 1, centerY + 1, centerX + half - 1, centerY + half - 1, 2, color);
        } else if (name.contains("termo") || name.contains("wordle") || name.contains("forca")
                || name.contains("digitação") || name.contains("anagrama")) {
            for (int index = 0; index < 3; index++) {
                int tileX = centerX - half + index * (size / 3);
                ArcadeTheme.rounded(context, tileX, centerY - half / 2, tileX + size / 4, centerY + half / 2, 1,
                        index == 1 ? ArcadeTheme.GOLD : color);
            }
        } else if (name.contains("pong")) {
            context.fill(centerX - half, centerY - half, centerX - half + 2, centerY + half, color);
            context.fill(centerX + half - 2, centerY - half, centerX + half, centerY + half, color);
            ArcadeTheme.disc(context, centerX, centerY, 2, ArcadeTheme.GOLD);
        } else if (name.contains("quebra-blocos") || name.contains("blocos")) {
            for (int row = 0; row < 2; row++) {
                for (int column = 0; column < 3; column++) {
                    context.fill(centerX - half + column * (size / 3), centerY - half / 2 + row * (size / 3),
                            centerX - half + column * (size / 3) + size / 4, centerY - half / 2 + row * (size / 3) + 3,
                            row == 0 ? color : light);
                }
            }
        } else if (name.contains("flappy") || name.contains("avião") || name.contains("pássaro")) {
            ArcadeTheme.disc(context, centerX - half / 3, centerY, half / 2, ArcadeTheme.GOLD);
            context.fill(centerX + half / 3, centerY - 1, centerX + half, centerY + 1, ArcadeTheme.CORAL);
            context.fill(centerX + half - 4, centerY - half, centerX + half, centerY + half, ArcadeTheme.LIME);
        } else if (name.contains("minado") || name.contains("mina")) {
            ArcadeTheme.disc(context, centerX, centerY, half - 2, color);
            for (int index = 0; index < 4; index++) {
                int dx = index < 2 ? -1 : 1;
                int dy = index % 2 == 0 ? -1 : 1;
                context.fill(centerX + dx * (half - 2) - 1, centerY + dy * (half - 2) - 1,
                        centerX + dx * (half - 2) + 2, centerY + dy * (half - 2) + 2, light);
            }
            ArcadeTheme.disc(context, centerX - half / 4, centerY - half / 4, 1, 0xFF0A1422);
        } else if (name.contains("memória")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half / 2, centerX + 1, centerY + half, 2, dark);
            context.fill(centerX - half + 1, centerY - half / 2 + 1, centerX, centerY + half - 1, color);
            ArcadeTheme.rounded(context, centerX - 1, centerY - half, centerX + half, centerY + half / 2, 2, dark);
            context.fill(centerX, centerY - half + 1, centerX + half - 1, centerY + half / 2 - 1, light);
        } else if (name.contains("simon")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half, centerX - 1, centerY - 1, 2, color);
            ArcadeTheme.rounded(context, centerX + 1, centerY - half, centerX + half, centerY - 1, 2, light);
            ArcadeTheme.rounded(context, centerX - half, centerY + 1, centerX - 1, centerY + half, 2, ArcadeTheme.GOLD);
            ArcadeTheme.rounded(context, centerX + 1, centerY + 1, centerX + half, centerY + half, 2,
                    ArcadeTheme.MAGENTA);
        } else if (name.contains("velha")) {
            context.fill(centerX - half, centerY - 1, centerX + half, centerY + 1, color);
            context.fill(centerX - 1, centerY - half, centerX + 1, centerY + half, color);
            ArcadeTheme.drawX(context, centerX - half / 2, centerY - half / 2, Math.max(1, half / 3), 1, light);
            ArcadeTheme.drawO(context, centerX + half / 2, centerY + half / 2, Math.max(1, half / 3), 1,
                    ArcadeTheme.GOLD);
        } else if (name.contains("ligue 4") || name.contains("quatro")) {
            for (int index = 0; index < 4; index++) {
                ArcadeTheme.disc(context, centerX - half + index * (size / 4), centerY + (-1 + index) * 2,
                        Math.max(1, half / 4), index < 2 ? ArcadeTheme.CORAL : ArcadeTheme.GOLD);
            }
        } else if (name.contains("damas") || name.contains("xadrez")) {
            for (int row = 0; row < 2; row++) {
                for (int column = 0; column < 2; column++) {
                    context.fill(centerX - half + column * half, centerY - half + row * half,
                            centerX + column * half, centerY + row * half, (row + column) % 2 == 0 ? color : dark);
                }
            }
            ArcadeTheme.disc(context, centerX - half / 2, centerY - half / 2, Math.max(1, half / 3), ArcadeTheme.GOLD);
        } else if (name.contains("sudoku")) {
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    context.fill(centerX - half + column * (size / 3), centerY - half + row * (size / 3),
                            centerX - half + column * (size / 3) + 2, centerY - half + row * (size / 3) + 2,
                            (row + column) % 2 == 0 ? color : light);
                }
            }
        } else if (name.contains("batalha")) {
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    context.fill(centerX - half + column * (size / 3), centerY - half + row * (size / 3),
                            centerX - half + column * (size / 3) + 2, centerY - half + row * (size / 3) + 2, color);
                }
            }
            ArcadeTheme.drawX(context, centerX, centerY, Math.max(2, half / 2), 1, ArcadeTheme.CORAL);
        } else if (name.contains("blackjack") || name.contains("cartas") || name.contains("pôquer")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half, centerX + 1, centerY + half, 2, 0xFFF7F2E6);
            ArcadeTheme.rounded(context, centerX - 1, centerY - half, centerX + half, centerY + half, 2, 0xFFE8E3D6);
            context.fill(centerX + half - 4, centerY - half + 2, centerX + half - 1, centerY - half + 3, ArcadeTheme.CORAL);
            ArcadeTheme.disc(context, centerX + half / 2, centerY + 1, Math.max(1, half / 3), ArcadeTheme.CORAL);
        } else if (name.contains("labirinto")) {
            context.fill(centerX - half, centerY - half, centerX - half + 2, centerY + half, color);
            context.fill(centerX - half, centerY - half, centerX + half, centerY - half + 2, color);
            context.fill(centerX - half, centerY + 2, centerX, centerY + 4, color);
            context.fill(centerX + half - 3, centerY - half, centerX + half - 1, centerY + 2, color);
            context.fill(centerX + 2, centerY + 2, centerX + half, centerY + 4, light);
        } else if (name.contains("matemática")) {
            context.fill(centerX - half, centerY - 1, centerX - 2, centerY + 1, color);
            context.fill(centerX - half / 2 - 1, centerY - half / 2, centerX - half / 2 + 1, centerY + half / 2, color);
            context.fill(centerX + 1, centerY - 1, centerX + half, centerY + 1, light);
            context.fill(centerX + half / 2, centerY - half / 2, centerX + half / 2 + 2, centerY + half / 2, light);
        } else if (name.contains("reflexo") || name.contains("relâmpago") || name.contains("reação")) {
            for (int index = 0; index < half; index++) {
                context.fill(centerX + index / 2 - 2, centerY - half + index, centerX + index / 2 + 1,
                        centerY - half + index + 2, index < half / 2 ? ArcadeTheme.GOLD : color);
            }
            context.fill(centerX - 2, centerY - 2, centerX + 3, centerY + 2, ArcadeTheme.GOLD);
        } else if (name.contains("pescaria")) {
            ArcadeTheme.disc(context, centerX - 1, centerY, half - 1, color);
            context.fill(centerX - half, centerY - 2, centerX - half + 3, centerY + 2, dark);
            ArcadeTheme.disc(context, centerX + 1, centerY - 1, 1, 0xFF0A1422);
            context.fill(centerX + 2, centerY - half + 1, centerX + 4, centerY - half + 3, ArcadeTheme.SKY);
        } else if (name.contains("dados") || name.contains("dado")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half, centerX + half, centerY + half, 2, 0xFFF2F6FB);
            ArcadeTheme.disc(context, centerX - half / 2, centerY - half / 2, 1, 0xFF1B2430);
            ArcadeTheme.disc(context, centerX + half / 2, centerY + half / 2, 1, 0xFF1B2430);
            ArcadeTheme.disc(context, centerX, centerY, 1, 0xFF1B2430);
        } else if (name.contains("torre") || name.contains("castelo") || name.contains("escada")) {
            for (int index = 0; index < 3; index++) {
                context.fill(centerX - half + index, centerY + half - index * 3,
                        centerX + half - index, centerY + half - index * 3 + 2, index == 2 ? ArcadeTheme.GOLD : color);
            }
        } else if (name.contains("corrida") || name.contains("trilho") || name.contains("metrô")
                || name.contains("faixas")) {
            context.fill(centerX - half, centerY + half / 2, centerX + half, centerY + half / 2 + 2, color);
            context.fill(centerX - half / 3, centerY - half / 2, centerX + half / 3, centerY + half / 2, light);
            ArcadeTheme.disc(context, centerX - half / 2, centerY + half / 2 + 2, 1, dark);
            ArcadeTheme.disc(context, centerX + half / 2, centerY + half / 2 + 2, 1, dark);
        } else if (name.contains("invasores") || name.contains("nave") || name.contains("espaço")) {
            context.fill(centerX - 2, centerY - half, centerX + 2, centerY + half / 2, color);
            context.fill(centerX - half, centerY + half / 2, centerX + half, centerY + half / 2 + 2, light);
            ArcadeTheme.disc(context, centerX, centerY + half / 3, 1, ArcadeTheme.GOLD);
        } else if (name.contains("estrela")) {
            ArcadeTheme.star(context, centerX, centerY, half, ArcadeTheme.GOLD);
        } else if (name.contains("bolha")) {
            ArcadeTheme.ring(context, centerX - half / 3, centerY, Math.max(2, half / 2), 1, light);
            ArcadeTheme.ring(context, centerX + half / 2, centerY - half / 3, Math.max(1, half / 3), 1, color);
        } else if (name.contains("fruta") || name.contains("doces") || name.contains("candy")) {
            ArcadeTheme.disc(context, centerX, centerY + 1, half - 1, ArcadeTheme.CORAL);
            context.fill(centerX - 1, centerY - half, centerX + 2, centerY - half + 3, ArcadeTheme.LIME);
        } else if (name.contains("fazenda") || name.contains("jardim") || name.contains("floresta")) {
            context.fill(centerX - 1, centerY - half / 2, centerX + 1, centerY + half, ArcadeTheme.LIME);
            context.fill(centerX - half / 2, centerY - half / 2, centerX, centerY - half / 2 + 3, ArcadeTheme.LIME);
            context.fill(centerX, centerY - half / 3, centerX + half / 2, centerY - half / 3 + 3, ArcadeTheme.LIME);
        } else if (name.contains("café") || name.contains("biscoito") || name.contains("cozinha")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half / 3, centerX + half / 2, centerY + half, 2,
                    0xFFF2E8D8);
            ArcadeTheme.ring(context, centerX + half / 2, centerY + half / 4, Math.max(2, half / 3), 2,
                    0xFFF2E8D8);
        } else if (name.contains("aquário")) {
            ArcadeTheme.ring(context, centerX, centerY, half - 1, 1, light);
            ArcadeTheme.disc(context, centerX - half / 3, centerY, 1, ArcadeTheme.GOLD);
            ArcadeTheme.disc(context, centerX + half / 3, centerY - half / 3, 1, color);
        } else if (name.contains("robô")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half / 2, centerX + half, centerY + half, 2, color);
            ArcadeTheme.disc(context, centerX - half / 3, centerY, 1, 0xFF0A1422);
            ArcadeTheme.disc(context, centerX + half / 3, centerY, 1, 0xFF0A1422);
            context.fill(centerX - 2, centerY - half, centerX + 2, centerY - half / 2, light);
        } else if (name.contains("fantasma")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half, centerX + half, centerY + half - 2, 3, light);
            ArcadeTheme.disc(context, centerX - half / 3, centerY - 1, 1, 0xFF0A1422);
            ArcadeTheme.disc(context, centerX + half / 3, centerY - 1, 1, 0xFF0A1422);
        } else if (name.contains("laser") || name.contains("fronteira")) {
            context.fill(centerX - half, centerY - 1, centerX + half, centerY + 1, color);
            context.fill(centerX - half, centerY - 1, centerX - half / 2, centerY + 1, light);
            ArcadeTheme.disc(context, centerX + half / 2, centerY, Math.max(1, half / 4), ArcadeTheme.CORAL);
        } else if (name.contains("arco") || name.contains("arqueiro") || name.contains("pênaltis")) {
            ArcadeTheme.ring(context, centerX - half / 3, centerY, Math.max(2, half / 2), 1, color);
            context.fill(centerX - half / 2, centerY - 1, centerX + half, centerY + 1, light);
        } else {
            drawCategoryGlyph(context, categoryFor(rawName), centerX, centerY, size, color);
        }
    }

    /** Ícones compactos para os 26 negócios da central de gestão. */
    private boolean drawTycoonArt(DrawContext context, String name, int centerX, int centerY, int half,
                                  int color, int light) {
        if (name.contains("pizzaria")) {
            ArcadeTheme.polygon(context, new int[] {centerX - half, centerX + half, centerX},
                    new int[] {centerY - half, centerY + half / 2, centerY + half}, ArcadeTheme.GOLD);
            context.fill(centerX - half + 2, centerY + 2, centerX + half - 2, centerY + 4, 0xFFD9653F);
            ArcadeTheme.disc(context, centerX - half / 3, centerY, 1, 0xFFB33A2B);
            ArcadeTheme.disc(context, centerX + half / 3, centerY - 2, 1, 0xFFB33A2B);
        } else if (name.contains("padaria")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half / 2, centerX + half,
                    centerY + half / 2, 4, 0xFFD9A15C);
            context.fill(centerX - half + 3, centerY - half / 4, centerX + half - 3, centerY, 0xFFF2C98A);
            context.fill(centerX - 1, centerY - half / 2, centerX + 1, centerY + half / 2, 0xFFB47A3C);
        } else if (name.contains("sorveteria")) {
            ArcadeTheme.polygon(context, new int[] {centerX - half / 2, centerX + half / 2, centerX},
                    new int[] {centerY - 2, centerY - 2, centerY + half}, 0xFFD9A15C);
            ArcadeTheme.disc(context, centerX, centerY - half / 2, half / 2 + 1, 0xFFFFC1D9);
            ArcadeTheme.disc(context, centerX - 2, centerY - half, 1, 0xFFFFFFFF);
        } else if (name.contains("mercado")) {
            ArcadeTheme.rounded(context, centerX - half / 2, centerY - half / 2, centerX + half / 2,
                    centerY + half, 3, ArcadeTheme.LIME);
            context.fill(centerX - 2, centerY - half / 2 - 3, centerX + 2, centerY - half / 2, light);
            ArcadeTheme.ring(context, centerX, centerY, half / 2, 1, 0xFF0A1422);
        } else if (name.contains("cinema")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half / 2, centerX + half,
                    centerY + half / 2, 2, 0xFF2A3345);
            for (int i = 0; i < 4; i++) {
                ArcadeTheme.rounded(context, centerX - half + i * (half / 2), centerY - half / 2,
                        centerX - half + i * (half / 2) + half / 4, centerY - half / 2 + 3, 1, light);
            }
            ArcadeTheme.polygon(context, new int[] {centerX - 2, centerX + 4, centerX - 2},
                    new int[] {centerY - 1, centerY + 2, centerY + 5}, ArcadeTheme.GOLD);
        } else if (name.contains("lanchonete")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - 1, centerX + half, centerY + half / 2, 4,
                    0xFFD9A15C);
            context.fill(centerX - half, centerY - 3, centerX + half, centerY - 1, ArcadeTheme.LIME);
            ArcadeTheme.rounded(context, centerX - half, centerY - half / 2, centerX + half, centerY - 4, 3,
                    ArcadeTheme.CORAL);
        } else if (name.contains("floricultura")) {
            for (int i = 0; i < 4; i++) {
                int dx = i == 0 ? -3 : i == 1 ? 3 : 0;
                int dy = i == 2 ? -3 : i == 3 ? 3 : 0;
                ArcadeTheme.disc(context, centerX + dx, centerY + dy, 2, ArcadeTheme.MAGENTA);
            }
            ArcadeTheme.disc(context, centerX, centerY, 2, ArcadeTheme.GOLD);
            context.fill(centerX, centerY + 2, centerX + 1, centerY + half, ArcadeTheme.LIME);
        } else if (name.contains("farmácia") || name.contains("farmacia") || name.contains("hospital")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half, centerX + half,
                    centerY + half, 3, 0xFFF2F7FF);
            context.fill(centerX - 2, centerY - half + 3, centerX + 2, centerY + half - 3, ArcadeTheme.DANGER);
            context.fill(centerX - half + 3, centerY - 2, centerX + half - 3, centerY + 2, ArcadeTheme.DANGER);
        } else if (name.contains("sushi")) {
            ArcadeTheme.rounded(context, centerX - half + 1, centerY + 1, centerX + half - 1,
                    centerY + half, 3, 0xFFF2F7FF);
            ArcadeTheme.rounded(context, centerX - half, centerY - half / 2, centerX + half, centerY + 3, 3,
                    ArcadeTheme.CORAL);
            context.fill(centerX - half + 2, centerY - half / 2 + 2, centerX + half - 2, centerY - half / 2 + 3,
                    0x66FFFFFF);
        } else if (name.contains("brinquedos")) {
            ArcadeTheme.disc(context, centerX, centerY, half - 1, ArcadeTheme.GOLD);
            context.fill(centerX - half + 1, centerY, centerX + half - 1, centerY + 1, 0xFFB33A2B);
            ArcadeTheme.disc(context, centerX, centerY, 2, 0xFFF2F7FF);
        } else if (name.contains("sucos")) {
            ArcadeTheme.rounded(context, centerX - 3, centerY - 1, centerX + 3, centerY + half, 2,
                    ArcadeTheme.LIME);
            context.fill(centerX - 3, centerY - 3, centerX + 3, centerY - 1, ArcadeTheme.GOLD);
            context.fill(centerX + 1, centerY - half, centerX + 2, centerY - 2, ArcadeTheme.CORAL);
        } else if (name.contains("fábrica") || name.contains("fabrica")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - 1, centerX + half, centerY + half, 2,
                    ArcadeTheme.SKY);
            context.fill(centerX - half + 2, centerY - half, centerX - half + 4, centerY - 1, light);
            context.fill(centerX - 2, centerY - half + 2, centerX, centerY - 1, light);
            ArcadeTheme.ring(context, centerX + half / 3, centerY + half / 3, 3, 1, 0xFF0A1422);
        } else if (name.contains("porto")) {
            ArcadeTheme.rounded(context, centerX - half, centerY, centerX, centerY + half / 2, 1, ArcadeTheme.CORAL);
            ArcadeTheme.rounded(context, centerX, centerY, centerX + half, centerY + half / 2, 1, ArcadeTheme.SKY);
            ArcadeTheme.rounded(context, centerX - half / 2, centerY - half / 2, centerX + half / 2, centerY - 2,
                    1, ArcadeTheme.GOLD);
            context.fill(centerX - half, centerY + half / 2 + 1, centerX + half, centerY + half / 2 + 3,
                    ArcadeTheme.SKY);
        } else if (name.contains("solar")) {
            ArcadeTheme.disc(context, centerX, centerY - half / 3, half / 3, ArcadeTheme.GOLD);
            ArcadeTheme.rounded(context, centerX - half, centerY + 1, centerX + half, centerY + half, 1,
                    ArcadeTheme.SKY);
            for (int i = 1; i < 3; i++) {
                context.fill(centerX - half + i * (half * 2 / 3), centerY + 2, centerX - half + i * (half * 2 / 3) + 1,
                        centerY + half, 0x88000000);
            }
        } else if (name.contains("estufa")) {
            ArcadeTheme.polygon(context, new int[] {centerX - half, centerX + half, centerX},
                    new int[] {centerY, centerY, centerY - half}, 0xAA9BE8C4);
            context.fill(centerX - half + 1, centerY, centerX + half - 1, centerY + half, 0x889BE8C4);
            context.fill(centerX - 1, centerY, centerX + 1, centerY + half, ArcadeTheme.LIME);
        } else if (name.contains("apiário") || name.contains("apiario")) {
            ArcadeTheme.rounded(context, centerX - half / 2, centerY - half / 2, centerX + half / 2,
                    centerY + half / 2, 2, 0xFFE5B984);
            for (int i = -1; i <= 1; i++) {
                context.fill(centerX - half / 2, centerY + i * 3, centerX + half / 2, centerY + i * 3 + 1,
                        0xFF8A5A2E);
            }
            ArcadeTheme.disc(context, centerX + half / 2 + 2, centerY - half / 2, 1, ArcadeTheme.GOLD);
        } else if (name.contains("mineradora")) {
            context.fill(centerX - half, centerY - 2, centerX + half, centerY + 1, 0xFF9AAFC7);
            context.fill(centerX - 1, centerY - half, centerX + 2, centerY + half, 0xFFB98A5A);
            ArcadeTheme.star(context, centerX, centerY, 3, ArcadeTheme.GOLD);
        } else if (name.contains("ferrovia")) {
            context.fill(centerX - half, centerY - 3, centerX + half, centerY - 2, 0xFF9AAFC7);
            context.fill(centerX - half, centerY + 2, centerX + half, centerY + 3, 0xFF9AAFC7);
            for (int i = 0; i < 4; i++) {
                context.fill(centerX - half + i * (half / 2), centerY - 4, centerX - half + i * (half / 2) + 1,
                        centerY + 4, 0xFF6E4E2C);
            }
        } else if (name.contains("gráfica") || name.contains("grafica") || name.contains("jornal")) {
            ArcadeTheme.rounded(context, centerX - half, centerY - half / 2, centerX + half,
                    centerY + half / 2, 1, 0xFFF2F7FF);
            for (int i = 0; i < 3; i++) {
                context.fill(centerX - half + 2, centerY - half / 4 + i * 2, centerX + half - 2,
                        centerY - half / 4 + i * 2 + 1, 0xFF4A5769);
            }
            context.fill(centerX - half + 2, centerY - half / 2 + 2, centerX - 1, centerY - half / 4 + 1,
                    ArcadeTheme.CORAL);
        } else if (name.contains("cerâmica") || name.contains("ceramica")) {
            ArcadeTheme.rounded(context, centerX - half / 2, centerY, centerX + half / 2, centerY + half, 3,
                    0xFFD98C5F);
            ArcadeTheme.rounded(context, centerX - 2, centerY - half / 2, centerX + 2, centerY + 1, 2,
                    0xFFD98C5F);
            context.fill(centerX - half / 2 + 1, centerY + 3, centerX + half / 2 - 1, centerY + 4,
                    0xFFA8623C);
        } else if (name.contains("banco")) {
            ArcadeTheme.polygon(context, new int[] {centerX - half, centerX + half, centerX},
                    new int[] {centerY - half / 2, centerY - half / 2, centerY - half}, 0xFFF2F7FF);
            context.fill(centerX - half, centerY - half / 2, centerX + half, centerY - half / 2 + 2, 0xFFF2F7FF);
            for (int i = 0; i < 3; i++) {
                context.fill(centerX - half + 2 + i * (half / 2), centerY - half / 2 + 3,
                        centerX - half + 3 + i * (half / 2), centerY + half / 2, 0xFFD9C07A);
            }
            context.fill(centerX - half, centerY + half / 2, centerX + half, centerY + half / 2 + 2,
                    0xFFF2F7FF);
        } else if (name.contains("aeroporto") || name.contains("avião")) {
            context.fill(centerX - half, centerY - 1, centerX + half, centerY + 1, 0xFFF2F7FF);
            context.fill(centerX - 2, centerY - half, centerX, centerY + half, 0xFFF2F7FF);
            ArcadeTheme.disc(context, centerX, centerY, 2, ArcadeTheme.SKY);
        } else if (name.contains("hotel")) {
            context.fill(centerX - half, centerY, centerX + half, centerY + 3, 0xFFF2F7FF);
            context.fill(centerX - half, centerY - half / 2, centerX - half + 2, centerY + 3, 0xFFF2F7FF);
            ArcadeTheme.rounded(context, centerX - half + 2, centerY - 2, centerX + half, centerY, 2,
                    ArcadeTheme.SKY);
            ArcadeTheme.disc(context, centerX, centerY - half / 2, 3, ArcadeTheme.GOLD);
        } else if (name.contains("zoológico") || name.contains("zoologico")) {
            ArcadeTheme.disc(context, centerX, centerY + 2, half - 2, ArcadeTheme.CORAL);
            ArcadeTheme.disc(context, centerX - half + 2, centerY - 2, 2, ArcadeTheme.CORAL);
            ArcadeTheme.disc(context, centerX + half - 2, centerY - 2, 2, ArcadeTheme.CORAL);
            ArcadeTheme.disc(context, centerX, centerY - half + 1, 2, ArcadeTheme.CORAL);
        } else {
            return false;
        }
        return true;
    }

    private void drawCategoryGlyph(DrawContext context, Category category, int centerX, int centerY, int size, int color) {
        int half = Math.max(3, size / 2);
        switch (category) {
            case MANAGEMENT -> {
                ArcadeTheme.disc(context, centerX, centerY, half, color);
                ArcadeTheme.disc(context, centerX, centerY, Math.max(1, half / 3), 0xFF0C1727);
                for (int i = 0; i < 4; i++) {
                    int dx = i < 2 ? -1 : 1;
                    int dy = i % 2 == 0 ? -1 : 1;
                    context.fill(centerX + dx * half - 1, centerY + dy * half - 1,
                            centerX + dx * half + 2, centerY + dy * half + 2, color);
                }
            }
            case SIMULATION -> {
                ArcadeTheme.ring(context, centerX, centerY, half, 1, color);
                ArcadeTheme.disc(context, centerX, centerY, Math.max(2, half / 2), color);
                context.fill(centerX - half, centerY - 1, centerX - half + 2, centerY + 1, color);
                context.fill(centerX + half - 1, centerY - half, centerX + half + 1, centerY - half + 2, color);
            }
            case ACTION -> {
                for (int i = 0; i < size; i++) {
                    int x = centerX + (i - size / 2) / 2;
                    int width = Math.max(1, size / 4);
                    context.fill(x - width, centerY - size / 2 + i, x + width, centerY - size / 2 + i + 1, color);
                }
            }
            case ADVENTURE -> {
                for (int i = 0; i <= half; i++) {
                    int spread = i;
                    context.fill(centerX - half - 2 + spread / 2, centerY + half - i,
                            centerX + 2, centerY + half - i + 1, color);
                    context.fill(centerX - 2, centerY + half - i,
                            centerX + half + 2 - spread / 2, centerY + half - i + 1, color);
                }
                context.fill(centerX - 1, centerY - half, centerX + 2, centerY + half, color);
            }
            case PUZZLE -> {
                int tile = Math.max(2, half - 1);
                context.fill(centerX - tile - 1, centerY - tile - 1, centerX - 1, centerY - 1, color);
                context.fill(centerX + 1, centerY - tile - 1, centerX + tile + 1, centerY - 1, color);
                context.fill(centerX - tile - 1, centerY + 1, centerX - 1, centerY + tile + 1, color);
                context.fill(centerX + 2, centerY + 2, centerX + tile + 2, centerY + tile + 2, 0xFF0C1727);
            }
            default -> ArcadeTheme.star(context, centerX, centerY, half, color);
        }
    }

    private void drawPager(DrawContext context) {
        if (pageCount <= 1) return;
        int y = height - 38;
        if (pageCount <= 12) {
            int spacing = 11;
            int total = (pageCount - 1) * spacing;
            for (int i = 0; i < pageCount; i++) {
                int cx = width / 2 - total / 2 + i * spacing;
                if (i == page) {
                    ArcadeTheme.rounded(context, cx - 5, y - 2, cx + 5, y + 2, 2, ArcadeTheme.GOLD);
                } else {
                    ArcadeTheme.disc(context, cx, y, 2, 0xFF34485F);
                }
            }
        } else {
            String label = I18n.translate("mega_minigames.hub.page", page + 1, pageCount);
            int pillWidth = textRenderer.getWidth(label) + 16;
            ArcadeTheme.rounded(context, width / 2 - pillWidth / 2, y - 7, width / 2 + pillWidth / 2, y + 7, 7, 0xCC0B1626);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), width / 2, y - 4, ArcadeTheme.MUTED);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            int x = chipX[i];
            int y = chipTop + (i / chipColumns) * (chipHeight + 4);
            if (mouseX >= x && mouseX < x + chipWidth && mouseY >= y && mouseY < y + chipHeight) {
                client.setScreen(new GamesHubScreen(categories[i], 0));
                return true;
            }
        }
        if (pageCount > 1 && pageCount <= 12) {
            int spacing = 11;
            int total = (pageCount - 1) * spacing;
            int y = height - 38;
            for (int i = 0; i < pageCount; i++) {
                int cx = width / 2 - total / 2 + i * spacing;
                if (mouseX >= cx - 5 && mouseX < cx + 5 && mouseY >= y - 4 && mouseY < y + 4) {
                    if (i != page) client.setScreen(new GamesHubScreen(category, i));
                    return true;
                }
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
            if (mouseX >= x && mouseX < x + cardWidth && mouseY >= y - 1 && mouseY < y + cardHeight + 1) {
                client.setScreen(visible.get(index).screenFactory.get());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount > 0 && page > 0) {
            client.setScreen(new GamesHubScreen(category, page - 1));
            return true;
        }
        if (amount < 0 && page < pageCount - 1) {
            client.setScreen(new GamesHubScreen(category, page + 1));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
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
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_R) {
            openRandomGame();
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
