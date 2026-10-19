package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Quinze desafios variados de gerenciamento, simulação, ação, aventura e lógica. */
public final class VarietyArcadeScreen extends Screen {
    public enum Game {
        BOOKSHOP("Livraria em Dia"), STATION("Estação de Recursos"), SHELTER("Abrigo Animal"),
        RALLY("Rali de Montanha"), DIVE("Mergulho Azul"), WEATHER_BALLOON("Balão Meteorológico"),
        LASER_DUEL("Duelo de Lasers"), METEORS("Chuva de Meteoros"), ARCHER("Arco Guardião"),
        CAVE("Caverna do Cristal"), TEMPLE("Templo em Fuga"), SNOW("Expedição na Neve"),
        MOSAIC("Mosaico 3×3"), PIPES("Conserte os Canos"), WORD_BRIDGE("Palavra Ponte");
        private final String title;
        Game(String title) { this.title = title; }
    }

    private static final int[] COLORS = {0xFF59BD78, 0xFF4C9CD6, 0xFFF2C653, 0xFFE66464, 0xFF9B71CE, 0xFFFF9452};
    private static final String[] BOOKS = {"AVENTURA", "CIÊNCIA", "HISTÓRIA", "FANTASIA"};
    private static final String[] BRIDGES = {"GATO", "FATA", "MATA", "RATO", "MATO"};
    private static final String[] BRIDGE_START = {"PATO", "FITA", "MATO", "MATO", "PATO"};
    private static final String[] BRIDGE_END = {"GALO", "FADA", "META", "GATO", "MATA"};
    private final Game game;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final int[][] board = new int[8][8];
    private final int[] stats = new int[5];
    private final List<Entity> entities = new ArrayList<>();
    private String message = "";
    private String input = "";
    private int score, round, ticks, timer, phase, misses, x, y, lane;
    private int fuel, ammo, target, resource, oxygen, altitude, wind, progress, keyCount;
    private int selectedX, selectedY, targetX, targetY, bridgeIndex;
    private static int bestManagement;
    private boolean gameOver, won, initialized, jumping;

    public VarietyArcadeScreen(Game game) {
        super(Text.literal(game.title));
        this.game = game;
    }

    @Override
    protected void init() {
        addDrawableChild(ArcadeTheme.button(width / 2 - 116, height - 25, 112, 20, Text.literal("Reiniciar (R)"), () -> resetGame()));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, height - 25, 112, 20, Text.literal("Voltar ao hub"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) { resetGame(); initialized = true; }
    }

    private int rand(int bound) { return random.nextInt(bound); }
    private int rand(int min, int max) { return random.nextInt(min, max + 1); }

    private void resetGame() {
        for (int[] row : board) Arrays.fill(row, 0);
        Arrays.fill(stats, 0); entities.clear();
        score = round = ticks = phase = misses = fuel = ammo = resource = oxygen = progress = keyCount = 0;
        timer = 0; x = y = lane = 0; target = targetX = targetY = altitude = wind = selectedX = selectedY = 0;
        input = ""; jumping = gameOver = won = false;
        switch (game) {
            case BOOKSHOP -> { timer = 1000; resource = 18; nextBookOrder(); message = "Atenda o gênero solicitado antes que o cliente vá embora."; }
            case STATION -> { timer = 1200; stats[0] = stats[1] = stats[2] = 10; message = "Reponha os recursos para manter a estação funcionando."; }
            case SHELTER -> { timer = 1200; stats[0] = stats[1] = stats[2] = 70; message = "Cuide de três animais: água, comida e carinho."; }
            case RALLY -> { timer = 900; lane = 1; message = "Use ←/→ para desviar das pedras na pista."; }
            case DIVE -> { timer = 1000; oxygen = 100; x = width / 2; y = height / 2; spawnPearls(); message = "Setas movem o mergulhador. Colete pérolas e volte à superfície."; }
            case WEATHER_BALLOON -> { timer = 900; altitude = 2; progress = 0; wind = 0; message = "Ajuste a altitude com ↑/↓ para atravessar os anéis de vento."; }
            case LASER_DUEL -> { timer = 900; lane = 1; spawnFoes(3); message = "←/→ muda de faixa; Espaço dispara o laser."; }
            case METEORS -> { timer = 900; x = width / 2; y = height - 65; spawnFoes(5); message = "Desvie com as setas e dispare com Espaço."; }
            case ARCHER -> { timer = 800; ammo = 12; spawnTarget(); message = "Clique nos alvos antes que escapem. Doze flechas."; }
            case CAVE -> setupCave();
            case TEMPLE -> { timer = 0; progress = 0; message = "Corra para a saída e pressione Espaço para saltar os fossos."; }
            case SNOW -> setupSnow();
            case MOSAIC -> setupMosaic();
            case PIPES -> setupPipes();
            case WORD_BRIDGE -> { bridgeIndex = 0; message = "Digite a palavra que liga o início ao destino, mudando uma letra."; }
        }
    }

    private void nextBookOrder() { target = rand(BOOKS.length); }
    private void spawnPearls() { entities.clear(); for (int i = 0; i < 6; i++) entities.add(new Entity(rand(40, Math.max(41, width - 40)), rand(100, Math.max(101, height - 80)), 1)); }
    private void spawnFoes(int count) { entities.clear(); for (int i = 0; i < count; i++) entities.add(new Entity(rand(35, Math.max(36, width - 35)), rand(55, Math.max(56, height / 2)), 2)); }
    private void spawnTarget() { entities.clear(); entities.add(new Entity(rand(60, Math.max(61, width - 60)), rand(90, Math.max(91, height - 100)), 1)); }

    private void setupCave() {
        x = 0; y = 0; keyCount = 0;
        for (int r = 0; r < 7; r++) for (int c = 0; c < 7; c++) if (rand(100) < 18) board[r][c] = 1;
        for (int c = 0; c < 7; c++) board[0][c] = 0;
        for (int r = 0; r < 7; r++) board[r][6] = 0;
        for (int r = 0; r <= 2; r++) board[r][4] = 0;
        for (int c = 4; c <= 6; c++) board[2][c] = 0;
        board[2][4] = 2; board[6][6] = 3;
        message = "Explore a caverna, pegue o cristal e alcance a saída.";
    }

    private void setupSnow() {
        x = 0; y = 5; fuel = 30;
        for (int r = 0; r < 6; r++) for (int c = 0; c < 6; c++) if (rand(100) < 17) board[r][c] = 1;
        for (int c = 0; c < 6; c++) board[5][c] = 0;
        for (int r = 0; r < 6; r++) board[r][5] = 0;
        for (int c = 0; c <= 5; c++) board[0][c] = 0;
        for (int r = 0; r <= 1; r++) board[r][2] = 0;
        for (int c = 2; c <= 4; c++) board[1][c] = 0;
        for (int r = 1; r <= 3; r++) board[r][4] = 0;
        board[1][2] = 2; board[3][4] = 2; board[0][5] = 3;
        message = "Encontre dois sobreviventes e chegue ao farol antes do frio.";
    }

    private void setupMosaic() {
        int n = 1;
        for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++) board[r][c] = n++ % 9;
        for (int i = 0; i < 90; i++) {
            int blank = blankIndex(), r = blank / 3, c = blank % 3, dir = rand(4);
            int nr = r + (dir == 0 ? -1 : dir == 1 ? 1 : 0), nc = c + (dir == 2 ? -1 : dir == 3 ? 1 : 0);
            if (nr >= 0 && nr < 3 && nc >= 0 && nc < 3) { board[r][c] = board[nr][nc]; board[nr][nc] = 0; }
        }
        message = "Ordene os números deslizando peças para o espaço vazio.";
    }

    private int blankIndex() { for (int i = 0; i < 9; i++) if (board[i / 3][i % 3] == 0) return i; return 8; }

    private void setupPipes() {
        for (int c = 0; c < 4; c++) board[0][c] = rand(2);
        for (int r = 1; r < 4; r++) board[r][3] = rand(2);
        message = "Gire os canos do caminho superior e da coluna direita para ligar a fonte à saída.";
    }

    @Override
    public void tick() {
        if (!initialized || gameOver) return;
        ticks++;
        switch (game) {
            case BOOKSHOP -> { if (ticks % 110 == 0) { misses++; nextBookOrder(); if (misses >= 5) lose("Clientes foram embora. Livros vendidos: " + score); } tickTimer(); }
            case STATION -> tickStation();
            case SHELTER -> tickShelter();
            case RALLY -> tickRally();
            case DIVE -> tickDive();
            case WEATHER_BALLOON -> tickWeatherBalloon();
            case LASER_DUEL -> tickLaserDuel();
            case METEORS -> tickMeteors();
            case ARCHER -> { if (ticks % 55 == 0) spawnTarget(); tickTimer(); }
            case TEMPLE -> tickTemple();
            case WORD_BRIDGE -> { }
            case CAVE, SNOW, MOSAIC, PIPES -> { }
        }
    }

    private void tickTimer() { if (timer > 0 && --timer == 0) { gameOver = true; won = score > 0; message = "Tempo encerrado. Pontuação: " + score + "."; } }

    private void tickStation() {
        if (ticks % 50 == 0) {
            for (int i = 0; i < 3; i++) stats[i]--;
            if (stats[0] < 0 || stats[1] < 0 || stats[2] < 0) { lose("A estação ficou sem suprimentos. Ciclos: " + score); return; }
            score++;
        }
        tickTimer();
    }

    private void tickShelter() {
        if (ticks % 35 == 0) for (int i = 0; i < 3; i++) stats[i]--;
        if (stats[0] <= 0 || stats[1] <= 0 || stats[2] <= 0) { lose("Um animal precisa de atenção! Dias: " + score); return; }
        if (ticks % 50 == 0) score++;
        tickTimer();
    }

    private void tickRally() {
        if (ticks % 10 == 0) {
            for (int r = 7; r > 0; r--) System.arraycopy(board[r - 1], 0, board[r], 0, 3);
            Arrays.fill(board[0], 0);
            if (rand(100) < 70) board[0][rand(3)] = 1;
            score++;
            if (board[7][lane] == 1) lose("A batida encerrou o rali. Distância: " + score);
        }
        tickTimer();
    }

    private void tickDive() {
        oxygen--;
        if (y < 105) oxygen = Math.min(100, oxygen + 2);
        if (ticks % 18 == 0) for (Entity pearl : entities) pearl.x += rand(3) - 1;
        if (oxygen <= 0) lose("Oxigênio esgotado. Pérolas coletadas: " + score);
        else tickTimer();
    }

    private void tickWeatherBalloon() {
        if (ticks % 28 == 0) {
            progress++;
            wind = rand(3) - 1;
            altitude += wind;
            if (altitude < 1 || altitude > 4) lose("O balão saiu da rota.");
            else if (progress >= 8) { gameOver = won = true; score = progress; message = "O balão chegou ao ponto de observação!"; }
            else if (rand(5) == 0 && Math.abs(altitude - 2) > 1) lose("Uma rajada tirou o balão da rota.");
        }
        tickTimer();
    }

    private void tickLaserDuel() {
        if (ticks % 24 == 0 && entities.size() < 8) entities.add(new Entity(rand(40, Math.max(41, width - 40)), 55, 2));
        for (Entity e : entities) e.y += 3;
        for (Entity e : entities) if (e.y > height - 75) { e.y = height + 100; misses++; }
        entities.removeIf(e -> e.y > height + 50);
        if (misses >= 4) lose("Os drones dominaram a arena. Abatidos: " + score);
        tickTimer();
    }

    private void tickMeteors() {
        if (ticks % 18 == 0 && entities.size() < 10) entities.add(new Entity(rand(25, Math.max(26, width - 25)), 45, 3));
        for (Entity e : entities) { e.y += 3; if (Math.abs(e.x - x) < 16 && Math.abs(e.y - y) < 18) { misses++; e.y = height + 100; } }
        entities.removeIf(e -> e.y > height + 50);
        if (misses >= 3) lose("A nave foi atingida três vezes. Meteoros destruídos: " + score);
        tickTimer();
    }

    private void tickTemple() {
        if (ticks % 16 == 0) {
            progress++;
            if ((progress == 3 || progress == 6 || progress == 9) && !jumping) { lose("Você caiu em um fosso. Progresso: " + progress); return; }
            jumping = false;
            if (progress >= 12) { gameOver = won = true; score = progress; message = "Você escapou do templo!"; }
        }
    }

    private void lose(String text) { gameOver = true; won = false; message = text; }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
ArcadeTheme.background(c, width, height);
        int accent = ArcadeTheme.accentFor(title);
        c.fillGradient(0, 0, width, 64, ArcadeTheme.alpha(accent, 30), 0x00000000);
        ArcadeTheme.rounded(c, 8, 3, width - 8, height - 25, 14, ArcadeTheme.alpha(accent, 110));
        ArcadeTheme.rounded(c, 9, 4, width - 9, height - 26, 13, 0xFF07101C);
        ArcadeTheme.rounded(c, 11, 6, width - 11, height - 28, 11, 0xFF0E1B2C);
        ArcadeTheme.rounded(c, 12, 7, width - 12, height - 29, 10, 0xFF122134);
        c.fillGradient(15, 44, width - 15, 116, ArcadeTheme.alpha(accent, 16), 0x00000000);
        ArcadeTheme.header(c, textRenderer, title, width / 2, 12, accent);
        ArcadeTheme.status(c, textRenderer, Text.literal(message), width / 2, 31,
                gameOver ? ArcadeTheme.GOLD : ArcadeTheme.TEXT);
        if (managementMode()) {
            int goal = managementGoal();
            float goalValue = Math.min(1.0F, score / (float) goal);
            ArcadeTheme.progress(c, 16, 41, width - 32, 3, goalValue,
                    goalValue >= 1.0F ? ArcadeTheme.SUCCESS : accent);
        }
        switch (game) {
            case BOOKSHOP -> renderBookshop(c);
            case STATION -> renderStation(c);
            case SHELTER -> renderShelter(c);
            case RALLY -> renderRally(c);
            case DIVE -> renderDive(c);
            case WEATHER_BALLOON -> renderBalloon(c);
            case LASER_DUEL -> renderLaser(c);
            case METEORS -> renderMeteors(c);
            case ARCHER -> renderArcher(c);
            case CAVE -> renderMap(c, 7, "CRISTAIS " + keyCount + "/1");
            case TEMPLE -> renderTemple(c);
            case SNOW -> renderMap(c, 6, "SOBREVIVENTES " + keyCount + "/2 • CALOR " + fuel);
            case MOSAIC -> renderMosaic(c);
            case PIPES -> renderPipes(c);
            case WORD_BRIDGE -> renderWordBridge(c);
        }
        if (gameOver) {
            bestManagement = Math.max(bestManagement, score);
            ArcadeTheme.banner(c, textRenderer, won ? "VOCÊ VENCEU!" : "FIM DE JOGO",
                    managementMode()
                            ? "Meta " + Math.min(score, managementGoal()) + "/" + managementGoal()
                                    + "  •  " + managementStars() + " estrelas  •  R tenta de novo"
                            : "R para jogar novamente",
                    width / 2, 62 + (height - 102) / 2, Math.min(width - 20, 300),
                    won ? ArcadeTheme.SUCCESS : ArcadeTheme.GOLD);
        }
        if (width >= 460) {
            ArcadeTheme.pill(c, textRenderer, "PONTOS  " + score, 16, height - 22, accent);
            ArcadeTheme.pillRight(c, textRenderer,
                    managementMode() ? "RECORDE  " + bestManagement : "RODADA  " + round,
                    width - 16, height - 22, ArcadeTheme.GOLD);
        }
        super.render(c, mouseX, mouseY, delta);
    }

    private boolean managementMode() {
        return game == Game.BOOKSHOP || game == Game.STATION || game == Game.SHELTER;
    }

    private int managementGoal() {
        return 12;
    }

    private int managementStars() {
        return Math.max(1, Math.min(5, 1 + score / 3));
    }

    private void renderBookshop(DrawContext c) {
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("PEDIDO: " + BOOKS[target]), width / 2, height / 2 - 62, 0xFFFFD76A);
        for (int i = 0; i < 4; i++) box(c, width / 2 - 192 + i * 96, height / 2 - 10, 88, 72, COLORS[i], BOOKS[i]);
        drawText(c, "Vendidos " + score + " • Estoque " + resource + " • Clientes perdidos " + misses, width / 2, height / 2 + 82);
    }

    private void renderStation(DrawContext c) {
        String[] names = {"COMIDA", "OXIGÊNIO", "ENERGIA"};
        for (int i = 0; i < 3; i++) { int bx = width / 2 - 164 + i * 112; box(c, bx, height / 2 - 38, 100, 62, COLORS[i], names[i] + " " + stats[i]); box(c, bx, height / 2 + 38, 100, 34, 0xFF3D6587, "+ ESTOQUE"); }
        drawText(c, "Ciclos mantidos: " + score + " • Tempo " + timer / 20 + "s", width / 2, height / 2 + 95);
    }

    private void renderShelter(DrawContext c) {
        for (int i = 0; i < 3; i++) { int cx = width / 2 - 100 + i * 100; disc(c, cx, height / 2 - 10, 28, COLORS[i]); drawText(c, "♥", cx, height / 2 - 15); drawText(c, "Cuidado " + stats[i], cx, height / 2 + 30); }
        String[] labels = {"ALIMENTAR", "BRINCAR", "LIMPAR"};
        for (int i = 0; i < 3; i++) box(c, width / 2 - 160 + i * 110, height / 2 + 66, 100, 36, COLORS[i], labels[i]);
        drawText(c, "Dias de abrigo: " + score + " • Tempo " + timer / 20 + "s", width / 2, 58);
    }

    private void renderRally(DrawContext c) {
        for (int i = 0; i < 3; i++) c.fill(width / 2 - 92 + i * 62, 66, width / 2 - 34 + i * 62, height - 55, 0xFF394A59);
        for (int r = 0; r < 8; r++) for (int col = 0; col < 3; col++) if (board[r][col] == 1) disc(c, width / 2 - 63 + col * 62, 75 + r * 38, 12, 0xFF9A7C61);
        box(c, width / 2 - 76 + lane * 62, height - 88, 28, 35, 0xFF5AC4E1, "■");
        drawText(c, "Distância " + score + " • Tempo " + timer / 20 + "s", width / 2, 55);
    }

    private void renderDive(DrawContext c) {
        c.fill(28, 65, width - 28, height - 52, 0xFF246A91);
        for (Entity pearl : entities) disc(c, pearl.x, pearl.y, 8, 0xFFE8F5F5);
        disc(c, x, y, 13, 0xFF5CC5DB); c.fill(30, 56, 30 + oxygen * (width - 60) / 100, 63, 0xFF61D99B);
        drawText(c, "Pérolas " + score + " • Oxigênio " + oxygen + " • suba para respirar", width / 2, height - 45);
    }

    private void renderBalloon(DrawContext c) {
        int yy = height / 2 - (altitude - 2) * 34;
        c.fill(30, height / 2 + 100, width - 30, height / 2 + 104, 0xFF697888);
        for (int i = 0; i < 4; i++) { int gx = 125 + i * 135; c.fill(gx, 90 + (i % 2) * 35, gx + 7, 165 + (i % 2) * 35, 0xFF8FA1B2); }
        disc(c, 70 + progress * (width - 145) / 8, yy, 17, 0xFFE56F6F);
        drawText(c, "Pontos de rota " + progress + "/8 • Altitude " + altitude + " • Vento " + wind, width / 2, 58);
    }

    private void renderLaser(DrawContext c) {
        for (Entity e : entities) box(c, e.x - 12, e.y - 10, 24, 20, 0xFFE05469, "◆");
        for (int i = 0; i < 3; i++) c.fill(width / 2 - 72 + i * 48, height - 95, width / 2 - 28 + i * 48, height - 52, lane == i ? 0xFF56C6E3 : 0xFF425466);
        drawText(c, "Drones abatidos " + score + " • Escaparam " + misses, width / 2, 58);
    }

    private void renderMeteors(DrawContext c) { for (Entity e : entities) disc(c, e.x, e.y, 12, 0xFF9B795F); disc(c, x, y, 14, 0xFF63C7E2); drawText(c, "Meteoros destruídos " + score + " • Escudos " + (3 - misses), width / 2, 58); }
    private void renderArcher(DrawContext c) { for (Entity e : entities) { disc(c, e.x, e.y, 19, 0xFFDB5B5B); disc(c, e.x, e.y, 10, 0xFFF2D365); disc(c, e.x, e.y, 4, 0xFFEAF3F5); } drawText(c, "Alvos " + score + " • Flechas " + ammo, width / 2, 58); }

    private void renderMap(DrawContext c, int side, String status) {
        int size = Math.min(44, Math.min((width - 70) / side, (height - 125) / side)); int x0 = (width - size * side) / 2, y0 = height / 2 - size * side / 2;
        for (int r = 0; r < side; r++) for (int col = 0; col < side; col++) {
            int v = board[r][col], bx = x0 + col * size, by = y0 + r * size;
            c.fill(bx + 2, by + 2, bx + size - 2, by + size - 2, v == 1 ? 0xFF424653 : v == 2 ? 0xFFFFD25A : v == 3 ? 0xFF4BBF9C : 0xFF536B4F);
            if (x == col && y == r) disc(c, bx + size / 2, by + size / 2, size / 4, 0xFF64C8E5);
        }
        drawText(c, status + " • Setas para explorar", width / 2, y0 + size * side + 8);
    }

    private void renderTemple(DrawContext c) {
        int ground = height / 2 + 55; c.fill(24, ground, width - 24, ground + 8, 0xFF886846);
        for (int obstacle : new int[]{3, 6, 9}) { int ox = 45 + obstacle * (width - 90) / 12; c.fill(ox, ground, ox + 28, ground + 15, 0xFF18212C); }
        disc(c, 40 + progress * (width - 80) / 12, ground - (jumping ? 44 : 12), 12, 0xFF66C4E5);
        drawText(c, "Progresso " + progress + "/12 • Espaço salta", width / 2, 58);
    }

    private void renderMosaic(DrawContext c) {
        int size = Math.min(74, Math.min((width - 60) / 3, (height - 120) / 3)); int x0 = (width - size * 3) / 2, y0 = height / 2 - size * 3 / 2;
        for (int r = 0; r < 3; r++) for (int col = 0; col < 3; col++) { int v = board[r][col], bx = x0 + col * size, by = y0 + r * size; c.fill(bx + 2, by + 2, bx + size - 2, by + size - 2, v == 0 ? 0xFF202C3B : COLORS[v % COLORS.length]); if (v > 0) c.drawCenteredTextWithShadow(textRenderer, Text.literal(String.valueOf(v)), bx + size / 2, by + size / 2 - 4, 0xFFFFFFFF); }
        drawText(c, "Movimentos: " + score, width / 2, y0 + size * 3 + 6);
    }

    private void renderPipes(DrawContext c) {
        int size = Math.min(62, Math.min((width - 80) / 4, (height - 120) / 4)); int x0 = width / 2 - size * 2, y0 = height / 2 - size * 2;
        for (int r = 0; r < 4; r++) for (int col = 0; col < 4; col++) { int bx = x0 + col * size, by = y0 + r * size; c.fill(bx + 2, by + 2, bx + size - 2, by + size - 2, 0xFF35485D); if (r == 0 && col < 4 || col == 3 && r > 0) { boolean active = r == 0 ? board[0][col] == 1 : board[r][3] == 0; c.fill(bx + size / 2 - 4, by + 5, bx + size / 2 + 4, by + size - 5, active ? 0xFF60D5C0 : 0xFF7D8A98); c.fill(bx + 5, by + size / 2 - 4, bx + size - 5, by + size / 2 + 4, active ? 0xFF60D5C0 : 0xFF7D8A98); } }
        drawText(c, "Clique nos canos do caminho para girá-los • Conexões " + score, width / 2, y0 + size * 4 + 8);
    }

    private void renderWordBridge(DrawContext c) {
        drawText(c, BRIDGE_START[bridgeIndex] + "   →   ?   →   " + BRIDGE_END[bridgeIndex], width / 2, height / 2 - 35);
        c.fill(width / 2 - 90, height / 2 - 2, width / 2 + 90, height / 2 + 42, 0xFF344B61);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(input.isEmpty() ? "DIGITE A PALAVRA" : input), width / 2, height / 2 + 14, 0xFFFFD86A);
        drawText(c, "Pontes corretas: " + score + "/5 • Enter confirma", width / 2, height / 2 + 64);
    }

    private void box(DrawContext c, int bx, int by, int w, int h, int color, String label) { c.fill(bx, by, bx + w, by + h, color); c.drawCenteredTextWithShadow(textRenderer, Text.literal(label), bx + w / 2, by + h / 2 - 4, 0xFFFFFFFF); }
    private void drawText(DrawContext c, String label, int cx, int cy) { c.drawCenteredTextWithShadow(textRenderer, Text.literal(label), cx, cy, 0xFFFFFFFF); }
    private void disc(DrawContext c, int cx, int cy, int rad, int color) { for (int dy = -rad; dy <= rad; dy++) { int dx = (int)Math.sqrt(rad * rad - dy * dy); c.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color); } }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        if (button != 0 || gameOver) return false;
        switch (game) {
            case BOOKSHOP -> sellBook(mx, my);
            case STATION -> refillStation(mx, my);
            case SHELTER -> careForPet(mx, my);
            case DIVE -> collectPearl(mx, my);
            case ARCHER -> shootTarget(mx, my);
            case CAVE, SNOW -> clickMap(mx, my);
            case MOSAIC -> slideTile(mx, my);
            case PIPES -> rotatePipe(mx, my);
            default -> { }
        }
        return true;
    }

    private void sellBook(double mx, double my) {
        int index = (int)((mx - (width / 2 - 192)) / 96); if (my < height / 2 - 10 || my > height / 2 + 62 || index < 0 || index >= 4) return;
        if (resource <= 0) { lose("As prateleiras ficaram vazias."); return; }
        resource--;
        if (index == target) { score++; nextBookOrder(); } else { misses++; nextBookOrder(); if (misses >= 5) lose("Muitos pedidos errados. Livros vendidos: " + score); }
    }

    private void refillStation(double mx, double my) {
        if (my < height / 2 + 32 || my > height / 2 + 78) return;
        int i = (int)((mx - (width / 2 - 164)) / 112); if (i < 0 || i >= 3) return;
        stats[i] = Math.min(20, stats[i] + 5);
    }

    private void careForPet(double mx, double my) {
        if (my < height / 2 + 58) return;
        int i = (int)((mx - (width / 2 - 160)) / 110); if (i >= 0 && i < 3) stats[i] = Math.min(100, stats[i] + 28);
    }

    private void collectPearl(double mx, double my) {
        for (int i = 0; i < entities.size(); i++) if (Math.hypot(mx - entities.get(i).x, my - entities.get(i).y) < 20) { entities.remove(i); score++; return; }
    }

    private void shootTarget(double mx, double my) {
        if (ammo <= 0) return;
        ammo--;
        if (!entities.isEmpty() && Math.hypot(mx - entities.get(0).x, my - entities.get(0).y) < 25) { score++; spawnTarget(); }
        else misses++;
        if (ammo == 0) { gameOver = true; won = score >= 6; message = "Precisão: " + score + "/12."; }
    }

    private void clickMap(double mx, double my) {
        int side = game == Game.CAVE ? 7 : 6, size = Math.min(44, Math.min((width - 70) / side, (height - 125) / side));
        int col = (int)((mx - (width - size * side) / 2) / size), row = (int)((my - (height / 2 - size * side / 2)) / size);
        moveMap(col - x, row - y, side);
    }

    private void moveMap(int dx, int dy, int side) {
        int nx = Math.max(0, Math.min(side - 1, x + dx)), ny = Math.max(0, Math.min(side - 1, y + dy));
        if (Math.abs(nx - x) + Math.abs(ny - y) != 1 || board[ny][nx] == 1) return;
        x = nx; y = ny;
        if (game == Game.CAVE && board[y][x] == 2) { keyCount = 1; board[y][x] = 0; }
        else if (game == Game.CAVE && board[y][x] == 3) { if (keyCount > 0) win("Cristal coletado! Você encontrou a saída da caverna."); else message = "A saída está selada: encontre o cristal azul."; }
        else if (game == Game.SNOW && board[y][x] == 2) { keyCount++; board[y][x] = 0; fuel--; }
        else if (game == Game.SNOW && board[y][x] == 3) { if (keyCount >= 2) win("Os dois exploradores chegaram ao farol!"); else message = "Ainda falta encontrar alguém na neve."; }
        if (game == Game.SNOW && --fuel <= 0 && !gameOver) lose("A equipe ficou sem calor antes do resgate.");
    }

    private void slideTile(double mx, double my) {
        int size = Math.min(74, Math.min((width - 60) / 3, (height - 120) / 3)), x0 = (width - size * 3) / 2, y0 = height / 2 - size * 3 / 2;
        int col = (int)((mx - x0) / size), row = (int)((my - y0) / size);
        if (row < 0 || row >= 3 || col < 0 || col >= 3) return;
        int blank = blankIndex(), br = blank / 3, bc = blank % 3;
        if (Math.abs(br - row) + Math.abs(bc - col) == 1) { board[br][bc] = board[row][col]; board[row][col] = 0; score++; if (solvedMosaic()) win("Mosaico completo em " + score + " movimentos!"); }
    }

    private boolean solvedMosaic() { for (int i = 0; i < 8; i++) if (board[i / 3][i % 3] != i + 1) return false; return board[2][2] == 0; }

    private void rotatePipe(double mx, double my) {
        int size = Math.min(62, Math.min((width - 80) / 4, (height - 120) / 4)), x0 = width / 2 - size * 2, y0 = height / 2 - size * 2;
        int col = (int)((mx - x0) / size), row = (int)((my - y0) / size);
        if (row < 0 || row >= 4 || col < 0 || col >= 4) return;
        if (row == 0 && col < 4) board[0][col] = 1 - board[0][col];
        else if (col == 3 && row > 0) board[row][3] = 1 - board[row][3];
        else return;
        score++;
        boolean connected = true;
        for (int c = 0; c < 4; c++) if (board[0][c] != 1) connected = false;
        for (int r = 1; r < 4; r++) if (board[r][3] != 0) connected = false;
        if (connected) win("Água chegou à saída! Canos conectados.");
    }

    private void win(String text) { gameOver = won = true; score = Math.max(1, score); message = text; }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) { resetGame(); return true; }
        if (gameOver) return super.keyPressed(keyCode, scanCode, modifiers);
        if (game == Game.STATION) {
            if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_3) stats[keyCode - GLFW.GLFW_KEY_1] += 5;
            else return super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (game == Game.DIVE) {
            if (keyCode == GLFW.GLFW_KEY_LEFT) x = Math.max(35, x - 14);
            else if (keyCode == GLFW.GLFW_KEY_RIGHT) x = Math.min(width - 35, x + 14);
            else if (keyCode == GLFW.GLFW_KEY_UP) y = Math.max(75, y - 14);
            else if (keyCode == GLFW.GLFW_KEY_DOWN) y = Math.min(height - 60, y + 14);
            else return super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (game == Game.WEATHER_BALLOON && (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN)) { altitude = Math.max(0, Math.min(5, altitude + (keyCode == GLFW.GLFW_KEY_UP ? 1 : -1))); return true; }
        if (game == Game.RALLY && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)) { lane = Math.max(0, Math.min(2, lane + (keyCode == GLFW.GLFW_KEY_LEFT ? -1 : 1))); return true; }
        if (game == Game.LASER_DUEL) {
            if (keyCode == GLFW.GLFW_KEY_LEFT) lane = Math.max(0, lane - 1);
            else if (keyCode == GLFW.GLFW_KEY_RIGHT) lane = Math.min(2, lane + 1);
            else if (keyCode == GLFW.GLFW_KEY_SPACE) { fireLaser(); return true; }
            else return super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (game == Game.METEORS) {
            if (keyCode == GLFW.GLFW_KEY_LEFT) x = Math.max(20, x - 16);
            else if (keyCode == GLFW.GLFW_KEY_RIGHT) x = Math.min(width - 20, x + 16);
            else if (keyCode == GLFW.GLFW_KEY_UP) y = Math.max(60, y - 16);
            else if (keyCode == GLFW.GLFW_KEY_DOWN) y = Math.min(height - 45, y + 16);
            else if (keyCode == GLFW.GLFW_KEY_SPACE) { fireMeteor(); return true; }
            else return super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (game == Game.TEMPLE && keyCode == GLFW.GLFW_KEY_SPACE) { jumping = true; return true; }
        if (game == Game.CAVE || game == Game.SNOW) {
            int dx = keyCode == GLFW.GLFW_KEY_LEFT ? -1 : keyCode == GLFW.GLFW_KEY_RIGHT ? 1 : 0;
            int dy = keyCode == GLFW.GLFW_KEY_UP ? -1 : keyCode == GLFW.GLFW_KEY_DOWN ? 1 : 0;
            if (dx != 0 || dy != 0) { moveMap(dx, dy, game == Game.CAVE ? 7 : 6); return true; }
        }
        if (game == Game.WORD_BRIDGE) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) { checkBridge(); return true; }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !input.isEmpty()) { input = input.substring(0, input.length() - 1); return true; }
            if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z && input.length() < 4) { input += (char)('A' + keyCode - GLFW.GLFW_KEY_A); return true; }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void fireLaser() {
        for (int i = 0; i < entities.size(); i++) if (Math.abs(entities.get(i).x - (width / 2 - 48 + lane * 48)) < 26) { entities.remove(i); score++; return; }
        misses++;
    }

    private void fireMeteor() {
        Entity nearest = null; double dist = 180;
        for (Entity e : entities) { double d = Math.hypot(e.x - x, e.y - y); if (d < dist) { nearest = e; dist = d; } }
        if (nearest != null) { entities.remove(nearest); score++; }
    }

    private void checkBridge() {
        if (input.equals(BRIDGES[bridgeIndex])) {
            score++; round++; input = "";
            if (round >= 5) win("Todas as pontes de palavras foram resolvidas!");
            else { bridgeIndex = round; message = "Correto! Agora resolva a próxima ligação."; }
        } else { misses++; input = ""; message = "Essa palavra não liga os dois extremos. Tente outra."; if (misses >= 5) lose("As pontes desabaram. Resolvidas: " + score); }
    }

    @Override
    public boolean shouldPause() { return false; }

    private static final class Entity {
        int x, y, type, direction = 1;
        Entity(int x, int y, int type) { this.x = x; this.y = y; this.type = type; }
    }
}
