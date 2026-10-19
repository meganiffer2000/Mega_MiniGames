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

/** Vinte e um jogos novos organizados em quatro famílias de mecânicas. */
public final class CategoryArcadeScreen extends Screen {
    public enum Game {
        FARM("Fazenda Próspera", "Gerenciamento"), CAFE("Café da Praça", "Gerenciamento"),
        TRAFFIC("Central de Trânsito", "Gerenciamento"), AQUARIUM("Aquário Vivo", "Gerenciamento"),
        COLONY("Colônia Espacial", "Gerenciamento"),
        FLIGHT("Pouso de Precisão", "Simulação"), PET("Meu Bichinho", "Simulação"),
        ORBIT("Órbita Estável", "Simulação"), WEATHER("Clima e Colheita", "Simulação"),
        ECOSYSTEM("Equilíbrio Natural", "Simulação"),
        ROBOT("Arena de Robôs", "Ação"), ARCHERY("Arco de Precisão", "Ação"),
        CASTLE("Defenda o Castelo", "Ação"), NINJA("Duelo Relâmpago", "Ação"),
        GHOST("Caça-Fantasmas", "Ação"), ARENA("Arena de Sobrevivência", "Ação"),
        RUINS("Ruínas Perdidas", "Aventura"), ISLAND("Ilha Misteriosa", "Aventura"),
        MINE_ESCAPE("Fuga da Mina", "Aventura"), FOREST("Relíquias da Floresta", "Aventura"),
        PIRATE("Mapa do Corsário", "Aventura");
        private final String title;
        private final String category;
        Game(String title, String category) { this.title = title; this.category = category; }
    }

    private static final int[] COLORS = {0xFF59B56D, 0xFF59AADB, 0xFFFFCC55, 0xFFE96565, 0xFFA177D5, 0xFFDA8750};
    private final Game game;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final int[][] board = new int[8][8];
    private final int[] stats = new int[5];
    private final List<Target> targets = new ArrayList<>();
    private String message = "";
    private int score, round, ticks, timer, phase, misses, x, y, altitude, speed;
    private int resourceA, resourceB, resourceC, lane, targetLane, ammo, fuel, keyCount;
    private int selectedAction, weather, temperature, population, predatorCount, preyCount;
    private boolean gameOver, won, initialized, jumping;

    public CategoryArcadeScreen(Game game) {
        super(Text.literal(game.title));
        this.game = game;
    }

    @Override
    protected void init() {
        addDrawableChild(ArcadeTheme.button(width / 2 - 116, height - 25, 112, 20, Text.literal("Reiniciar (R)"), () -> resetGame()));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, height - 25, 112, 20, Text.literal("Voltar ao hub"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) { resetGame(); initialized = true; }
    }

    private int rand(int max) { return random.nextInt(max); }
    private int rand(int min, int max) { return random.nextInt(min, max + 1); }

    private void resetGame() {
        for (int[] row : board) Arrays.fill(row, 0);
        Arrays.fill(stats, 0);
        targets.clear();
        score = round = ticks = phase = misses = keyCount = resourceA = resourceB = resourceC = 0;
        timer = 0; x = y = 0; altitude = 4; speed = 4; lane = 1; targetLane = 1;
        fuel = 100; ammo = 10; selectedAction = 0; gameOver = won = jumping = false;
        population = 5; predatorCount = 2; preyCount = 8; weather = 2; temperature = 2;
        switch (game) {
            case FARM -> { timer = 1200; resourceA = 12; message = "Plante, espere crescer e colha antes do fim da estação."; }
            case CAFE -> { timer = 900; resourceA = 10; nextDrink(); message = "Sirva pedidos clicando na bebida pedida."; }
            case TRAFFIC -> { timer = 900; Arrays.fill(stats, 0); message = "Clique nos semáforos para liberar carros sem lotar as filas."; }
            case AQUARIUM -> { timer = 900; stats[0] = stats[1] = stats[2] = 80; message = "Alimente, limpe e brinque para manter o aquário saudável."; }
            case COLONY -> { timer = 1200; resourceA = 8; resourceB = 8; resourceC = 8; message = "Administre comida, água e energia para sustentar a colônia."; }
            case FLIGHT -> { timer = 900; altitude = 5; speed = 5; x = 0; message = "↑/↓ controla altitude; ←/→ ajusta velocidade. Pouse na faixa verde."; }
            case PET -> { timer = 1200; stats[0] = stats[1] = stats[2] = 75; message = "Cuide do bichinho: alimente, brinque e limpe."; }
            case ORBIT -> { timer = 1200; altitude = 3; message = "↑/↓ ajusta a altitude orbital. Mantenha a órbita estável."; }
            case WEATHER -> { timer = 1200; temperature = 2; weather = 2; resourceA = 35; message = "Ajuste sol e chuva para manter a plantação viva."; }
            case ECOSYSTEM -> { timer = 1200; preyCount = 8; predatorCount = 2; resourceA = 5; message = "Equilibre presas e predadores com alimento e proteção."; }
            case ROBOT -> { timer = 900; x = width / 2; y = height - 90; spawnTargets(4, 1); message = "←/→ movimenta o robô; Espaço dispara."; }
            case ARCHERY -> { ammo = 10; spawnTargets(1, 2); message = "Clique no alvo móvel para acertar. Você tem dez flechas."; }
            case CASTLE -> { timer = 900; spawnTargets(4, 3); message = "Clique nos invasores antes que cheguem ao castelo."; }
            case NINJA -> { timer = 600; targetLane = rand(4); message = "Clique no símbolo indicado antes que ele mude de posição."; }
            case GHOST -> { timer = 900; spawnTargets(5, 4); message = "Clique nos fantasmas; fuja das sombras escuras."; }
            case ARENA -> { timer = 1200; x = width / 2; y = height / 2; spawnTargets(5, 5); message = "Sobreviva movendo-se com WASD ou as setas."; }
            case RUINS -> setupRuins();
            case ISLAND -> setupIsland();
            case MINE_ESCAPE -> { x = 0; y = 4; timer = 0; message = "←/→ avança; Espaço salta sobre os buracos."; }
            case FOREST -> setupForest();
            case PIRATE -> setupPirate();
        }
    }

    private void nextDrink() { targetLane = rand(3); }

    private void spawnTargets(int count, int type) {
        targets.clear();
        for (int i = 0; i < count; i++) targets.add(new Target(rand(70, Math.max(71, width - 70)), rand(90, Math.max(91, height - 100)), type));
    }

    private void setupRuins() {
        timer = 0; x = 0; y = 0; keyCount = 0;
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) if (rand(100) < 20) board[r][c] = 1;
        for (int i = 0; i < 8; i++) { board[0][i] = 0; board[i][7] = 0; }
        for (int r = 0; r <= 3; r++) board[r][4] = 0;
        for (int c = 4; c < 8; c++) board[3][c] = 0;
        for (int r = 3; r < 8; r++) board[r][7] = 0;
        board[0][0] = 0; board[3][4] = 2; board[7][7] = 3;
        message = "Explore as ruínas, pegue a chave dourada e alcance a saída.";
    }

    private void setupIsland() {
        timer = 0; x = 0; y = 0; resourceA = 0;
        for (int r = 0; r < 6; r++) for (int c = 0; c < 6; c++) board[r][c] = rand(5) == 0 ? 1 : 0;
        for (int c = 0; c <= 4; c++) board[0][c] = 0;
        for (int r = 0; r <= 2; r++) board[r][4] = 0;
        for (int c = 2; c <= 4; c++) board[2][c] = 0;
        for (int r = 2; r <= 4; r++) board[r][2] = 0;
        for (int c = 1; c <= 5; c++) board[4][c] = 0;
        for (int r = 0; r <= 5; r++) board[r][5] = 0;
        for (int c = 0; c <= 5; c++) board[5][c] = 0;
        board[0][0] = 0; board[5][5] = 3; board[2][2] = 2; board[4][1] = 2; board[1][4] = 2;
        message = "Vasculhe a ilha por três suprimentos e encontre a embarcação.";
    }

    private void setupForest() {
        timer = 0; x = 0; y = 5; keyCount = 0;
        for (int r = 0; r < 6; r++) for (int c = 0; c < 6; c++) if (rand(100) < 14) board[r][c] = 1;
        for (int c = 0; c < 6; c++) board[5][c] = 0;
        for (int r = 0; r < 6; r++) board[r][5] = 0;
        for (int c = 1; c <= 5; c++) board[0][c] = 0;
        for (int c = 1; c <= 4; c++) board[1][c] = 0;
        for (int r = 1; r <= 4; r++) board[r][4] = 0;
        for (int c = 2; c <= 4; c++) board[4][c] = 0;
        for (int r = 1; r < 5; r++) board[r][2] = 0;
        board[1][1] = 2; board[2][4] = 2; board[4][2] = 2; board[0][5] = 3;
        message = "Colete três relíquias e chegue ao portal no canto superior direito.";
    }

    private void setupPirate() {
        timer = 0; x = 0; y = 0; fuel = 18; keyCount = 0;
        board[1][3] = 2; board[4][2] = 2; board[5][5] = 3;
        message = "Navegue até as duas partes do mapa e depois até o X do tesouro.";
    }

    @Override
    public void tick() {
        if (!initialized || gameOver) return;
        ticks++;
        switch (game) {
            case FARM -> tickFarm();
            case CAFE -> { tickTimed(); if (ticks % 70 == 0 && !gameOver) { misses++; nextDrink(); if (misses >= 5) lose("Clientes impacientes foram embora."); } }
            case TRAFFIC -> tickTraffic();
            case AQUARIUM -> tickAquarium();
            case COLONY -> tickColony();
            case FLIGHT -> tickFlight();
            case PET -> tickPet();
            case ORBIT -> tickOrbit();
            case WEATHER -> tickWeather();
            case ECOSYSTEM -> tickEcosystem();
            case ROBOT -> tickRobot();
            case ARCHERY -> tickArchery();
            case CASTLE -> tickCastle();
            case NINJA -> tickNinja();
            case GHOST -> tickGhost();
            case ARENA -> tickArena();
            case RUINS, ISLAND, MINE_ESCAPE, FOREST, PIRATE -> { if (game == Game.MINE_ESCAPE) tickMine(); }
        }
    }

    private void tickFarm() {
        if (ticks % 90 == 0) {
            for (int r = 0; r < 3; r++) for (int c = 0; c < 5; c++) if (board[r][c] == 1) board[r][c] = 2;
        }
        tickTimed();
    }

    private void tickTimed() { if (timer > 0 && --timer == 0) { gameOver = true; won = score > 0; message = "Turno encerrado. Pontos: " + score + "."; } }

    private void tickTraffic() {
        if (ticks % 24 == 0) stats[rand(3)]++;
        if (ticks % 5 == 0) for (int i = 0; i < 3; i++) if (board[0][i] == 1 && stats[i] > 0) { stats[i]--; score++; }
        for (int i = 0; i < 3; i++) if (stats[i] >= 6) lose("Uma fila ficou longa demais. Carros liberados: " + score);
        if (!gameOver && timer > 0 && --timer == 0) { gameOver = true; won = score >= 25; message = "Trânsito encerrado: " + score + " carros liberados."; }
    }

    private void tickAquarium() {
        if (ticks % 24 == 0) { stats[0]--; stats[1]--; stats[2]--; }
        if (stats[0] <= 0 || stats[1] <= 0 || stats[2] <= 0) { lose("O aquário ficou desequilibrado. Dias cuidados: " + score); return; }
        if (ticks % 40 == 0) score++;
        tickTimed();
    }

    private void tickColony() {
        if (ticks % 60 == 0) {
            resourceA--; resourceB--; resourceC--;
            if (resourceA < 0 || resourceB < 0 || resourceC < 0) { lose("A colônia ficou sem recursos. Ciclos: " + score); return; }
            population++;
            score++;
        }
        if (timer > 0 && --timer == 0) { gameOver = true; won = score >= 10; message = "Colônia: " + population + " habitantes após " + score + " ciclos."; }
    }

    private void tickFlight() {
        if (ticks % 8 == 0) {
            x += speed;
            if (x >= width - 140) {
                gameOver = true;
                won = altitude >= 2 && altitude <= 3 && speed >= 3 && speed <= 5;
                message = won ? "Pouso suave!" : "Pouso fora da faixa. Ajuste altitude e velocidade.";
            }
        }
        if (timer > 0 && --timer == 0 && !gameOver) lose("Combustível esgotado antes da pista.");
    }

    private void tickPet() {
        if (ticks % 28 == 0) { stats[0]--; stats[1]--; stats[2]--; }
        if (stats[0] <= 0 || stats[1] <= 0 || stats[2] <= 0) { lose("Seu bichinho ficou triste. Dias cuidados: " + score); return; }
        if (ticks % 40 == 0) score++;
        tickTimed();
    }

    private void tickOrbit() {
        if (ticks % 10 == 0) {
            if (altitude < 1 || altitude > 5) { lose("A nave saiu da órbita."); return; }
            if (altitude == 3) score++;
            else if (ticks % 30 == 0) altitude += Integer.compare(3, altitude);
        }
        tickTimed();
    }

    private void tickWeather() {
        if (ticks % 40 == 0) {
            int delta = 3 - Math.abs(weather - 2) - Math.abs(temperature - 2);
            resourceA += delta;
            if (resourceA <= 0) { lose("A plantação secou."); return; }
            score++;
            if (resourceA > 90) resourceA = 90;
        }
        tickTimed();
    }

    private void tickEcosystem() {
        if (ticks % 55 == 0) {
            preyCount += resourceA > 0 ? 2 : -1;
            resourceA = Math.max(0, resourceA - 1);
            predatorCount += preyCount > predatorCount * 3 ? 1 : -1;
            predatorCount = Math.max(0, predatorCount);
            score++;
            if (preyCount <= 0 || predatorCount >= preyCount) { lose("O ecossistema perdeu o equilíbrio após " + score + " ciclos."); return; }
        }
        tickTimed();
    }

    private void tickRobot() {
        if (ticks % 28 == 0 && targets.size() < 7) targets.add(new Target(rand(40, Math.max(41, width - 40)), 70, 1));
        for (Target t : targets) { t.y += 2; if (t.y > height - 65) { misses++; t.y = height + 100; } }
        targets.removeIf(t -> t.y > height + 50);
        if (misses >= 4) lose("Robôs inimigos invadiram a arena. Abatidos: " + score);
        tickTimed();
    }

    private void tickArchery() {
        if (!targets.isEmpty()) {
            Target t = targets.get(0); t.x += t.direction * 3;
            if (t.x < 60 || t.x > width - 60) t.direction *= -1;
        }
    }

    private void tickCastle() {
        if (ticks % 30 == 0 && targets.size() < 8) targets.add(new Target(rand(45, Math.max(46, width - 45)), 60, 3));
        for (Target t : targets) t.y += 2;
        for (Target t : targets) if (t.y > height - 65) { t.y = height + 100; misses++; }
        targets.removeIf(t -> t.y > height + 50);
        if (misses >= 5) lose("O castelo caiu! Invasores derrotados: " + score);
        tickTimed();
    }

    private void tickNinja() {
        if (ticks % 28 == 0) { targetLane = rand(4); round++; if (round >= 12) { gameOver = true; won = score >= 8; message = "Duelo finalizado: " + score + "/12 acertos."; } }
        tickTimed();
    }

    private void tickGhost() {
        if (ticks % 28 == 0 && targets.size() < 7) targets.add(new Target(rand(50, Math.max(51, width - 50)), rand(90, Math.max(91, height - 90)), rand(5) == 0 ? 6 : 4));
        for (Target t : targets) t.life--;
        for (Target t : targets) if (t.life <= 0) { t.x = -100; misses++; }
        targets.removeIf(t -> t.x < 0);
        if (misses >= 5) lose("As sombras escaparam. Fantasmas capturados: " + score);
        tickTimed();
    }

    private void tickArena() {
        if (ticks % 35 == 0 && targets.size() < 8) targets.add(new Target(rand(25, Math.max(26, width - 25)), rand(55, Math.max(56, height - 50)), 5));
        for (Target t : targets) { t.x += Integer.compare(x, t.x); t.y += Integer.compare(y, t.y); if (Math.abs(t.x - x) < 12 && Math.abs(t.y - y) < 12) { misses++; t.x = width + 100; } }
        targets.removeIf(t -> t.x > width + 50);
        if (misses >= 4) lose("A arena ficou cercada. Sobreviventes abatidos: " + score);
        tickTimed();
    }

    private void tickMine() {
        if (ticks % 25 == 0) {
            x++;
            if (x == 4 && !won) { misses++; if (!jumping) { lose("Você caiu em uma fenda."); return; } }
            jumping = false;
            if (x >= 9) { gameOver = won = true; score = 1; message = "Você escapou da mina!"; }
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
        switch (game) {
            case FARM -> renderFarm(c);
            case CAFE -> renderCafe(c);
            case TRAFFIC -> renderTraffic(c);
            case AQUARIUM -> renderAquarium(c);
            case COLONY -> renderColony(c);
            case FLIGHT -> renderFlight(c);
            case PET -> renderPet(c);
            case ORBIT -> renderOrbit(c);
            case WEATHER -> renderWeather(c);
            case ECOSYSTEM -> renderEcosystem(c);
            case ROBOT -> renderRobot(c);
            case ARCHERY -> renderArchery(c);
            case CASTLE -> renderCastle(c);
            case NINJA -> renderNinja(c);
            case GHOST -> renderGhost(c);
            case ARENA -> renderArena(c);
            case RUINS -> renderMap(c, 8, "CHAVE: " + keyCount + "/1", 2);
            case ISLAND -> renderMap(c, 6, "SUPRIMENTOS: " + resourceA + "/3", 3);
            case MINE_ESCAPE -> renderMine(c);
            case FOREST -> renderMap(c, 6, "RELÍQUIAS: " + keyCount + "/3", 3);
            case PIRATE -> renderMap(c, 6, "PARTES DO MAPA: " + keyCount + "/2   •   Combustível " + fuel, 3);
        }
        if (gameOver) ArcadeTheme.status(c, textRenderer, Text.literal("R para jogar novamente"), width / 2, height - 45, ArcadeTheme.GOLD);
        super.render(c, mouseX, mouseY, delta);
    }

    private void renderFarm(DrawContext c) {
        drawGrid(c, 5, 3, 52, 55, (r, col) -> board[r][col] == 0 ? "PLANTAR" : board[r][col] == 1 ? "CRESCE" : "COLHER", (r, col) -> board[r][col] == 0 ? 0xFF53784A : board[r][col] == 1 ? 0xFF9CA94A : 0xFFF0C853);
        drawText(c, "Moedas: " + resourceA + "   •   Colheita: " + score + "   •   Tempo " + timer / 20 + "s", width / 2, height - 54);
    }

    private void renderCafe(DrawContext c) {
        drawText(c, "PEDIDO: " + new String[]{"CAFÉ", "CHÁ", "SUCO"}[targetLane], width / 2, height / 2 - 65);
        String[] drinks = {"CAFÉ", "CHÁ", "SUCO"};
        for (int i = 0; i < 3; i++) box(c, width / 2 - 165 + i * 112, height / 2 - 5, 100, 62, COLORS[i], drinks[i]);
        drawText(c, "Pedidos: " + score + " • Estoque: " + resourceA + " • Erros: " + misses, width / 2, height / 2 + 85);
    }

    private void renderTraffic(DrawContext c) {
        String[] labels = {"NORTE", "CENTRO", "SUL"};
        for (int i = 0; i < 3; i++) {
            int bx = width / 2 - 150 + i * 100;
            box(c, bx, height / 2 - 55, 80, 80, board[0][i] == 1 ? 0xFF43A66B : 0xFFB94C4C, board[0][i] == 1 ? "VERDE" : "VERMELHO");
            drawText(c, labels[i] + "  fila " + stats[i], bx + 40, height / 2 + 36);
        }
        drawText(c, "Carros liberados: " + score + "   •   " + timer / 20 + "s", width / 2, height / 2 + 72);
    }

    private void renderAquarium(DrawContext c) {
        c.fill(width / 2 - 175, height / 2 - 85, width / 2 + 175, height / 2 + 55, 0xFF3586A5);
        for (int i = 0; i < 4; i++) { int fx = width / 2 - 100 + i * 60, fy = height / 2 - 15 + (i % 2) * 20; disc(c, fx, fy, 12, COLORS[i]); c.fill(fx - 18, fy, fx + 4, fy + 2, 0xFFD6EFF2); }
        box(c, width / 2 - 150, height / 2 + 70, 90, 34, 0xFF4AA574, "ALIMENTAR"); box(c, width / 2 - 45, height / 2 + 70, 90, 34, 0xFF4B95B8, "LIMPAR"); box(c, width / 2 + 60, height / 2 + 70, 90, 34, 0xFF9A67BA, "BRINCAR");
        drawText(c, "Fome " + stats[0] + "   Limpeza " + stats[1] + "   Alegria " + stats[2] + "   Dias " + score, width / 2, 58);
    }

    private void renderColony(DrawContext c) {
        String[] labels = {"COMIDA", "ÁGUA", "ENERGIA"}; int[] values = {resourceA, resourceB, resourceC};
        for (int i = 0; i < 3; i++) { box(c, width / 2 - 165 + i * 112, height / 2 - 22, 100, 74, COLORS[i], labels[i] + " " + values[i]); box(c, width / 2 - 165 + i * 112, height / 2 + 66, 100, 30, 0xFF426A88, "+ RECURSO"); }
        drawText(c, "População " + population + " • Ciclos " + score + " • Tempo " + timer / 20 + "s", width / 2, height / 2 - 58);
    }

    private void renderFlight(DrawContext c) {
        c.fill(20, height / 2 + 55, width - 20, height / 2 + 60, 0xFF58875E);
        c.fill(width - 135, height / 2 + 30, width - 38, height / 2 + 78, 0xFFBB9B66);
        int px = 50 + x;
        c.fill(px, height / 2 - altitude * 22, px + 45, height / 2 - altitude * 22 + 10, 0xFF70C3E0);
        drawText(c, "Altitude " + altitude + " • Velocidade " + speed + " • ←/→", width / 2, 62);
    }

    private void renderPet(DrawContext c) {
        disc(c, width / 2, height / 2 - 10, 42, 0xFFD99A69); disc(c, width / 2 - 14, height / 2 - 20, 4, 0xFF24394A); disc(c, width / 2 + 14, height / 2 - 20, 4, 0xFF24394A);
        box(c, width / 2 - 150, height / 2 + 62, 90, 34, 0xFF54A86A, "COMIDA"); box(c, width / 2 - 45, height / 2 + 62, 90, 34, 0xFF518FC5, "BRINCAR"); box(c, width / 2 + 60, height / 2 + 62, 90, 34, 0xFF9B6BBC, "LIMPAR");
        drawText(c, "Fome " + stats[0] + " • Alegria " + stats[1] + " • Higiene " + stats[2] + " • Dias " + score, width / 2, 58);
    }

    private void renderOrbit(DrawContext c) {
        disc(c, width / 2, height / 2, 36, 0xFFFFCD57);
        int radius = 42 + altitude * 18;
        for (int i = 0; i < 12; i++) { double a = (ticks * 0.055) + i * Math.PI / 6; int px = width / 2 + (int)(Math.cos(a) * radius), py = height / 2 + (int)(Math.sin(a) * radius); disc(c, px, py, i == 0 ? 9 : 2, i == 0 ? 0xFF65D5E8 : 0xFF7C93A8); }
        drawText(c, "Altitude orbital " + altitude + " • Órbitas estáveis " + score + " • ↑/↓", width / 2, height / 2 + 105);
    }

    private void renderWeather(DrawContext c) {
        drawText(c, "Temperatura " + temperature + "   Chuva " + weather + "   Vitalidade " + resourceA, width / 2, height / 2 - 85);
        disc(c, width / 2, height / 2 + 2, 35 + Math.min(25, score), 0xFF65B768);
        box(c, width / 2 - 155, height / 2 + 75, 90, 36, 0xFFFFA64D, "MAIS SOL"); box(c, width / 2 - 45, height / 2 + 75, 90, 36, 0xFF548BCE, "MAIS CHUVA"); box(c, width / 2 + 65, height / 2 + 75, 90, 36, 0xFF7C6CB8, "ESFRIAR");
        drawText(c, "Ciclos bons: " + score, width / 2, 58);
    }

    private void renderEcosystem(DrawContext c) {
        drawText(c, "PRESAS " + preyCount + "       PREDADORES " + predatorCount + "       ALIMENTO " + resourceA, width / 2, height / 2 - 62);
        for (int i = 0; i < Math.min(20, preyCount); i++) disc(c, width / 2 - 110 + (i % 10) * 23, height / 2 - 15 + (i / 10) * 24, 6, 0xFF6FC878);
        for (int i = 0; i < Math.min(10, predatorCount); i++) disc(c, width / 2 - 80 + i * 18, height / 2 + 40, 8, 0xFFDF6262);
        box(c, width / 2 - 140, height / 2 + 75, 120, 38, 0xFF63AA64, "PLANTAR ALIMENTO"); box(c, width / 2 + 20, height / 2 + 75, 120, 38, 0xFF5689BD, "PROTEGER PRESAS");
        drawText(c, "Ciclos equilibrados: " + score, width / 2, 58);
    }

    private void renderRobot(DrawContext c) { renderActionArena(c, 0xFF58C6DE, "Robôs abatidos: " + score + " • Espaço dispara"); }
    private void renderArena(DrawContext c) { renderActionArena(c, 0xFFFFD05B, "Ameaças eliminadas: " + score + " • Vida: " + (4 - misses)); }

    private void renderActionArena(DrawContext c, int color, String status) {
        for (Target t : targets) disc(c, t.x, t.y, 12, t.type == 5 ? 0xFFDF5D62 : 0xFFCB6A79);
        disc(c, x, y, 13, color); drawText(c, status, width / 2, 58);
    }

    private void renderArchery(DrawContext c) {
        c.fill(width / 2 - 3, height / 2 + 25, width / 2 + 3, height / 2 + 86, 0xFF9A663E);
        if (!targets.isEmpty()) drawTarget(c, targets.get(0));
        drawText(c, "Acertos " + score + " • Flechas " + ammo, width / 2, 58);
    }

    private void renderCastle(DrawContext c) {
        c.fill(width / 2 - 60, height - 140, width / 2 + 60, height - 62, 0xFFB79259);
        for (Target t : targets) drawTarget(c, t);
        drawText(c, "Castelo • Invasores derrotados " + score + " • Escaparam " + misses, width / 2, 58);
    }

    private void renderNinja(DrawContext c) {
        String[] symbols = {"◈", "◆", "●", "▲"};
        for (int i = 0; i < 4; i++) box(c, width / 2 - 160 + i * 82, height / 2 - 28, 64, 64, COLORS[i], symbols[i]);
        drawText(c, "Alvo atual: " + symbols[targetLane] + "   Acertos " + score + " / 12", width / 2, height / 2 + 65);
    }

    private void renderGhost(DrawContext c) { for (Target t : targets) drawTarget(c, t); drawText(c, "Capturados " + score + " • Escaparam " + misses, width / 2, 58); }

    private void drawTarget(DrawContext c, Target t) {
        int color = t.type == 6 ? 0xFF292D39 : t.type == 3 ? 0xFFE45B62 : t.type == 4 ? 0xFFB479D6 : 0xFF5DC8D7;
        disc(c, t.x, t.y, 17, color); disc(c, t.x - 5, t.y - 4, 3, 0xFFFFFFFF); disc(c, t.x + 5, t.y - 4, 3, 0xFFFFFFFF);
    }

    private void renderMap(DrawContext c, int side, String status, int exitCode) {
        int size = Math.min(48, Math.min((width - 70) / side, (height - 125) / side));
        int x0 = (width - size * side) / 2, y0 = height / 2 - size * side / 2;
        for (int r = 0; r < side; r++) for (int col = 0; col < side; col++) {
            int val = board[r][col]; int bx = x0 + col * size, by = y0 + r * size;
            int color = val == 1 ? 0xFF4A4C56 : val == 2 ? 0xFFFFD35B : val == 3 ? 0xFF5AC5A0 : 0xFF44664E;
            c.fill(bx + 2, by + 2, bx + size - 2, by + size - 2, color);
            if (r == y && col == x) disc(c, bx + size / 2, by + size / 2, size / 4, 0xFF64C9EA);
            else if (val > 1) c.drawCenteredTextWithShadow(textRenderer, Text.literal(val == 2 ? "◆" : "X"), bx + size / 2, by + size / 2 - 4, 0xFF202A36);
        }
        drawText(c, status + "   •   Setas para explorar", width / 2, y0 + side * size + 10);
    }

    private void renderMine(DrawContext c) {
        int ground = height / 2 + 55; c.fill(20, ground, width - 20, ground + 8, 0xFF786148);
        for (int i = 0; i < 9; i++) { int xx = 30 + i * (width - 70) / 9; if (i == 4) c.fill(xx, ground, xx + 30, ground + 18, 0xFF14202B); }
        disc(c, 40 + x * (width - 80) / 9, ground - (jumping ? 42 : 10), 11, 0xFF65BCE2);
        drawText(c, "Espaço pula a fenda • Progresso " + x + "/9", width / 2, 58);
    }

    private void box(DrawContext c, int bx, int by, int w, int h, int color, String label) { c.fill(bx, by, bx + w, by + h, color); c.drawCenteredTextWithShadow(textRenderer, Text.literal(label), bx + w / 2, by + h / 2 - 4, 0xFFFFFFFF); }
    private void drawText(DrawContext c, String text, int cx, int yy) { c.drawCenteredTextWithShadow(textRenderer, Text.literal(text), cx, yy, 0xFFFFFFFF); }
    private void disc(DrawContext c, int cx, int cy, int radius, int color) { for (int dy = -radius; dy <= radius; dy++) { int dx = (int)Math.sqrt(radius * radius - dy * dy); c.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color); } }

    @FunctionalInterface private interface CellLabel { String get(int row, int col); }
    @FunctionalInterface private interface CellColor { int get(int row, int col); }
    private void drawGrid(DrawContext c, int cols, int rowCount, int tile, int top, CellLabel label, CellColor color) {
        int size = Math.min(tile, Math.min((width - 50) / cols, (height - 130) / rowCount));
        int x0 = (width - cols * size) / 2, y0 = height / 2 - rowCount * size / 2;
        for (int r = 0; r < rowCount; r++) for (int col = 0; col < cols; col++) {
            int bx = x0 + col * size, by = y0 + r * size;
            c.fill(bx + 2, by + 2, bx + size - 2, by + size - 2, color.get(r, col));
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(label.get(r, col)), bx + size / 2, by + size / 2 - 4, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        if (button != 0 || gameOver) return false;
        switch (game) {
            case FARM -> clickFarm(mx, my);
            case CAFE -> clickCafe(mx, my);
            case TRAFFIC -> clickTraffic(mx, my);
            case AQUARIUM -> clickCare(mx, my, false);
            case PET -> clickCare(mx, my, true);
            case COLONY -> clickColony(mx, my);
            case ARCHERY -> shootTarget(mx, my);
            case CASTLE -> clickMovingTarget(mx, my, 3);
            case NINJA -> clickNinja(mx, my);
            case GHOST -> clickMovingTarget(mx, my, 4);
            case RUINS, ISLAND, FOREST, PIRATE -> clickMap(mx, my);
            case WEATHER -> clickWeather(mx, my);
            case ECOSYSTEM -> clickEcosystem(mx);
            default -> { }
        }
        return true;
    }

    private void clickFarm(double mx, double my) {
        int size = Math.min(52, Math.min((width - 50) / 5, (height - 130) / 3)); int x0 = (width - size * 5) / 2, y0 = height / 2 - size * 3 / 2;
        int col = (int)((mx - x0) / size), row = (int)((my - y0) / size); if (row < 0 || row >= 3 || col < 0 || col >= 5) return;
        if (board[row][col] == 0 && resourceA > 0) { board[row][col] = 1; resourceA--; }
        else if (board[row][col] == 2) { board[row][col] = 0; resourceA += 3; score++; }
    }

    private void clickCafe(double mx, double my) {
        int choice = (int)((mx - (width / 2 - 165)) / 112);
        if (my < height / 2 - 5 || my > height / 2 + 57 || choice < 0 || choice > 2) return;
        if (resourceA <= 0) { lose("O estoque acabou."); return; }
        resourceA--;
        if (choice == targetLane) { score++; nextDrink(); } else { misses++; if (misses >= 5) lose("Muitos pedidos errados."); else nextDrink(); }
    }

    private void clickTraffic(double mx, double my) {
        if (my < height / 2 - 55 || my > height / 2 + 25) return;
        int lane = (int)((mx - (width / 2 - 150)) / 100); if (lane < 0 || lane >= 3) return;
        board[0][lane] = 1 - board[0][lane];
    }

    private void clickCare(double mx, double my, boolean pet) {
        int action = mx < width / 2 - 50 ? 0 : mx > width / 2 + 50 ? 2 : 1;
        if (my < height / 2 + 55) return;
        if (action == 0) stats[0] = Math.min(100, stats[0] + 28);
        if (action == 1) stats[1] = Math.min(100, stats[1] + 30);
        if (action == 2) stats[2] = Math.min(100, stats[2] + 30);
    }

    private void clickColony(double mx, double my) {
        if (my < height / 2 + 55 || my > height / 2 + 100) return;
        int i = (int)((mx - (width / 2 - 165)) / 112); if (i < 0 || i >= 3) return;
        if (i == 0) resourceA += 3; else if (i == 1) resourceB += 3; else resourceC += 3;
    }

    private void shootTarget(double mx, double my) {
        if (ammo <= 0) { lose("As flechas acabaram. Acertos: " + score); return; }
        ammo--;
        if (!targets.isEmpty() && Math.hypot(mx - targets.get(0).x, my - targets.get(0).y) < 25) { score++; targets.get(0).x = 80 + rand(Math.max(1, width - 160)); }
        else misses++;
        if (ammo == 0) { gameOver = true; won = score >= 6; message = "Precisão: " + score + "/10 flechas."; }
    }

    private void clickMovingTarget(double mx, double my, int type) {
        for (int i = 0; i < targets.size(); i++) {
            Target t = targets.get(i);
            if (Math.hypot(mx - t.x, my - t.y) < 22 && (type == 3 || t.type != 6)) { targets.remove(i); score++; return; }
            if (Math.hypot(mx - t.x, my - t.y) < 22 && type == 4 && t.type == 6) { misses++; score = Math.max(0, score - 2); targets.remove(i); return; }
        }
    }

    private void clickNinja(double mx, double my) {
        if (my < height / 2 - 28 || my > height / 2 + 36) return;
        int index = (int)((mx - (width / 2 - 160)) / 82);
        if (index < 0 || index >= 4) return;
        if (index == targetLane) score++; else misses++;
        if (misses >= 4) lose("Duelo perdido. Acertos: " + score);
    }

    private void clickWeather(double mx, double my) {
        if (my < height / 2 + 65) return;
        if (mx < width / 2 - 55) { temperature = Math.min(4, temperature + 1); weather = Math.max(0, weather - 1); }
        else if (mx > width / 2 + 55) weather = Math.min(4, weather + 1);
        else temperature = Math.max(0, temperature - 1);
    }

    private void clickEcosystem(double mx) { if (mx < width / 2) resourceA = Math.min(8, resourceA + 3); else preyCount = Math.min(20, preyCount + 2); }

    private void clickFarmCell(int r, int c) { }

    private void clickMap(double mx, double my) {
        int side = game == Game.RUINS ? 8 : 6; int size = Math.min(48, Math.min((width - 70) / side, (height - 125) / side));
        int col = (int)((mx - (width - size * side) / 2) / size), row = (int)((my - (height / 2 - size * side / 2)) / size);
        if (row < 0 || row >= side || col < 0 || col >= side) return;
        moveExplorer(col - x, row - y);
    }

    private void moveExplorer(int dx, int dy) {
        int nx = Math.max(0, Math.min((game == Game.RUINS ? 8 : 6) - 1, x + dx));
        int ny = Math.max(0, Math.min((game == Game.RUINS ? 8 : 6) - 1, y + dy));
        if (Math.abs(nx - x) + Math.abs(ny - y) != 1 || board[ny][nx] == 1) return;
        x = nx; y = ny;
        int tile = board[y][x];
        if (game == Game.RUINS && tile == 2) { keyCount = 1; board[y][x] = 0; }
        else if (game == Game.RUINS && tile == 3) { if (keyCount > 0) complete("Você escapou das ruínas com o artefato!"); else message = "A porta está trancada. Procure a chave dourada."; }
        else if (game == Game.ISLAND && tile == 2) { resourceA++; board[y][x] = 0; if (resourceA >= 3) message = "Suprimentos completos! Encontre o barco no X."; }
        else if (game == Game.ISLAND && tile == 3) { if (resourceA >= 3) complete("Você deixou a ilha com os suprimentos!"); else message = "Ainda faltam suprimentos na ilha."; }
        else if (game == Game.FOREST && tile == 2) { keyCount++; board[y][x] = 0; }
        else if (game == Game.FOREST && tile == 3) { if (keyCount >= 3) complete("As relíquias abriram o portal da floresta!"); else message = "O portal precisa de três relíquias."; }
        else if (game == Game.PIRATE && tile == 2) { keyCount++; board[y][x] = 0; fuel--; }
        else if (game == Game.PIRATE && tile == 3) { if (keyCount >= 2) complete("Tesouro encontrado! A expedição venceu."); else message = "Procure as duas partes do mapa."; }
        if (game == Game.PIRATE && --fuel <= 0 && !gameOver) lose("O navio ficou sem provisões.");
    }

    private void complete(String text) { gameOver = won = true; score = Math.max(1, score); message = text; }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) { resetGame(); return true; }
        if (gameOver) return super.keyPressed(keyCode, scanCode, modifiers);
        if (game == Game.COLONY) {
            if (keyCode == GLFW.GLFW_KEY_1) resourceA += 3;
            else if (keyCode == GLFW.GLFW_KEY_2) resourceB += 3;
            else if (keyCode == GLFW.GLFW_KEY_3) resourceC += 3;
            else return super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (game == Game.FLIGHT) {
            if (keyCode == GLFW.GLFW_KEY_UP) altitude = Math.min(6, altitude + 1);
            else if (keyCode == GLFW.GLFW_KEY_DOWN) altitude = Math.max(0, altitude - 1);
            else if (keyCode == GLFW.GLFW_KEY_RIGHT) speed = Math.min(7, speed + 1);
            else if (keyCode == GLFW.GLFW_KEY_LEFT) speed = Math.max(1, speed - 1);
            else return super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (game == Game.ORBIT) { if (keyCode == GLFW.GLFW_KEY_UP) altitude++; else if (keyCode == GLFW.GLFW_KEY_DOWN) altitude--; else return super.keyPressed(keyCode, scanCode, modifiers); return true; }
        if (game == Game.ROBOT && keyCode == GLFW.GLFW_KEY_SPACE) { fireRobot(); return true; }
        if (game == Game.MINE_ESCAPE && keyCode == GLFW.GLFW_KEY_SPACE) { jumping = true; return true; }
        if (game == Game.ARENA && keyCode == GLFW.GLFW_KEY_SPACE) { fireArena(); return true; }
        if (game == Game.ARENA || game == Game.ROBOT) {
            if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) x = Math.max(16, x - 14);
            else if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) x = Math.min(width - 16, x + 14);
            else if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_W) y = Math.max(48, y - 14);
            else if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_S) y = Math.min(height - 45, y + 14);
            else return super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (game == Game.MINE_ESCAPE || game == Game.RUINS || game == Game.ISLAND || game == Game.FOREST || game == Game.PIRATE) {
            int dx = keyCode == GLFW.GLFW_KEY_LEFT ? -1 : keyCode == GLFW.GLFW_KEY_RIGHT ? 1 : 0;
            int dy = keyCode == GLFW.GLFW_KEY_UP ? -1 : keyCode == GLFW.GLFW_KEY_DOWN ? 1 : 0;
            if (dx != 0 || dy != 0) {
                if (game == Game.MINE_ESCAPE) { x = Math.min(9, x + dx); if (x >= 9) complete("Você escapou da mina!"); }
                else moveExplorer(dx, dy);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void fireArena() {
        Target nearest = null; double distance = 105;
        for (Target target : targets) { double d = Math.hypot(target.x - x, target.y - y); if (d < distance) { nearest = target; distance = d; } }
        if (nearest != null) { targets.remove(nearest); score++; }
    }

    private void fireRobot() {
        if (targets.isEmpty()) return;
        Target nearest = targets.get(0); double distance = Double.MAX_VALUE;
        for (Target t : targets) { double d = Math.hypot(t.x - x, t.y - y); if (d < distance) { distance = d; nearest = t; } }
        if (distance < 220) { targets.remove(nearest); score++; }
    }

    @Override
    public boolean shouldPause() { return false; }

    private static final class Target {
        int x, y, type, life, direction = 1;
        Target(int x, int y, int type) { this.x = x; this.y = y; this.type = type; this.life = type == 4 ? 90 : 200; }
    }
}
