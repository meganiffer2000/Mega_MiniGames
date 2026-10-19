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

/** Adapta mecânicas arcade conhecidas em minijogos originais, sem assets de terceiros. */
public final class InspiredArcadeScreen extends Screen {
    public enum Game {
        JETPACK("Jetpack Runner"), COOKIE("Fábrica de Biscoitos"), KITCHEN("Cozinha Caótica"),
        PAPER_PLANE("Avião de Papel"), HELIX("Queda Helicoidal"), BLACK_TILE("Toque nos Azulejos"),
        SUBWAY("Corrida no Trilho"), DIGGER("Escavador"), SLINGSHOT("Estilingue de Pássaros"),
        MARBLE("Orbes em Linha"), FRUIT_SLICE("Corte de Frutas"), SURVIVOR("Sobrevivente Noturno"),
        CUT_ROPE("Corte da Corda"), CANDY_MATCH("Combinação de Doces"), WHEEL("Corrida Maluca"),
        DECK_CLIMB("Torre de Cartas"), MOUNTAIN("Escalada da Montanha"), BALLOON_TD("Defesa de Balões"),
        GARDEN_TD("Defesa do Jardim"), LEMMINGS("Resgate dos Mineiros"), GEOMETRY("Salto Geométrico"),
        MINI_METRO("Mini Metrô"), BORDER("Controle de Fronteira"), POKER("Pôquer Roguelike");
        private final String title;
        Game(String title) { this.title = title; }
    }

    private static final int[] COLORS = {0xFFE65B5B, 0xFF54A9EC, 0xFF59BD75, 0xFFF0C34F, 0xFFB979E8, 0xFFFF914D};
    private static final String[] INGREDIENTS = {"PÃO", "QUEIJO", "ALFACE", "TOMATE", "CARNE", "MASSA"};
    private static final String[] NAMES = {"Lia", "Caio", "Mina", "Theo", "Bia", "Ravi", "Nina", "Ivo"};
    private final Game game;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final int[][] board = new int[10][20];
    private final int[] rows = new int[6];
    private final int[] cards = new int[5];
    private final List<Mob> mobs = new ArrayList<>();
    private String message = "";
    private String order = "";
    private String applicant = "";
    private int score, round, ticks, timer, phase, misses, health;
    private int x, y, vx, vy, lane, altitude, fuel, combo, currency;
    private int targetX, targetY, aimX, aimY, towerX, towerY;
    private int ropeCut, candyY, towerHp, enemyHp, playerHp, shield;
    private int ingredientIndex, wheelLean, subwayStation, metroLine, fruitKind;
    private int answerCorrect, passengerNeed, passengerHave;
    private boolean gameOver, won, initialized, jumping, dragging, documentValid;

    public InspiredArcadeScreen(Game game) {
        super(Text.literal(game.title));
        this.game = game;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Reiniciar (R)"), button -> resetGame())
                .dimensions(width / 2 - 116, height - 25, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao hub"), button -> client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, height - 25, 112, 20).build());
        if (!initialized) { resetGame(); initialized = true; }
    }

    private int random(int bound) { return random.nextInt(bound); }

    private void resetGame() {
        for (int[] row : board) Arrays.fill(row, 0);
        Arrays.fill(rows, 0);
        mobs.clear();
        score = round = ticks = phase = misses = combo = currency = 0;
        health = playerHp = 3;
        timer = 0;
        x = width / 2; y = height / 2; vx = vy = 0; lane = 1; altitude = 2; fuel = 100;
        targetX = targetY = aimX = aimY = 0;
        jumping = dragging = false; gameOver = won = false;
        switch (game) {
            case JETPACK -> { timer = 900; message = "Segure Espaço para voar; solte para descer. Pegue moedas e evite faíscas."; }
            case COOKIE -> { currency = 0; score = 0; message = "Clique no biscoito para ganhar migalhas; compre melhorias à direita."; }
            case KITCHEN -> { timer = 900; newOrder(); message = "Monte o pedido clicando nos ingredientes na ordem exibida."; }
            case PAPER_PLANE -> { timer = 900; x = width + 10; y = height / 2; targetY = random.nextInt(100, Math.max(101, height - 100)); message = "Espaço dá impulso. Passe pelos anéis sem bater."; }
            case HELIX -> { lane = 0; round = 0; message = "Gire com ←/→ para alinhar a abertura antes de cada queda."; }
            case BLACK_TILE -> { for (int i = 0; i < rows.length; i++) rows[i] = random(4); message = "Clique no azulejo escuro da linha de baixo. Nunca toque nos claros."; }
            case SUBWAY -> { timer = 900; lane = 1; message = "←/→ muda de trilho; ↑ pula e ↓ rola. Desvie dos trens."; }
            case DIGGER -> setupDigger();
            case SLINGSHOT -> { targetX = width / 2 + 115; targetY = height / 2 - 15; aimX = width / 2 - 95; aimY = height / 2 + 30; towerHp = 3; message = "Clique para mirar e pressione Espaço para lançar a pedra."; }
            case MARBLE -> setupMarbles();
            case FRUIT_SLICE -> { timer = 600; spawnFruit(); message = "Clique nas frutas que aparecem; evite as bombas escuras."; }
            case SURVIVOR -> { timer = 1200; x = width / 2; y = height / 2; playerHp = 3; spawnEnemies(); message = "Mova-se com WASD/setas. Seu ataque é automático."; }
            case CUT_ROPE -> { ropeCut = 0; candyY = height / 2 - 20; targetX = width / 2; targetY = height / 2 + 48; message = "Clique na corda para soltar o doce e acertar a estrela."; }
            case CANDY_MATCH -> { setupCandyBoard(); message = "Troque doces vizinhos e faça combinações de três ou mais."; }
            case WHEEL -> { timer = 900; wheelLean = 0; message = "Equilibre a bicicleta com ←/→; Espaço salta os obstáculos."; }
            case DECK_CLIMB -> { playerHp = 24; enemyHp = 18; shield = 0; drawCards(); message = "Escolha uma carta: golpe, defesa ou cura."; }
            case MOUNTAIN -> { x = 2; y = 5; timer = 0; message = "←/→ corre, Espaço pula. Alcance a bandeira no alto."; }
            case BALLOON_TD -> setupBalloonDefense();
            case GARDEN_TD -> setupGarden();
            case LEMMINGS -> { round = 8; lane = 0; message = "Clique em uma casa para construir uma ponte para os mineiros."; }
            case GEOMETRY -> { timer = 900; x = 0; jumping = false; message = "Salte com Espaço no ritmo para passar os espinhos."; }
            case MINI_METRO -> setupMetro();
            case BORDER -> newApplicant();
            case POKER -> { drawPokerHand(); message = "Jogue mãos de cinco cartas e alcance 100 pontos em até oito rodadas."; }
        }
    }

    private void newOrder() {
        String[] recipe = {INGREDIENTS[random(INGREDIENTS.length)], INGREDIENTS[random(INGREDIENTS.length)], INGREDIENTS[random(INGREDIENTS.length)]};
        order = String.join(" → ", recipe);
        answerCorrect = 0;
        ingredientIndex = 0;
    }

    private void setupDigger() {
        for (int r = 0; r < 7; r++) for (int c = 0; c < 8; c++) board[r][c] = 1;
        board[0][0] = 0; board[6][7] = 3;
        for (int i = 0; i < 5; i++) { int er = random(1, 6), ec = random(0, 7); if (board[er][ec] == 1) board[er][ec] = 2; }
        message = "Escave até o cristal azul; clique em terra adjacente ao túnel.";
    }

    private int random(int min, int max) { return random.nextInt(min, max + 1); }

    private void setupMarbles() {
        for (int i = 0; i < 14; i++) board[0][i] = random(4) + 1;
        message = "Dispare uma esfera colorida clicando em uma das quatro cores.";
    }

    private void spawnFruit() {
        targetX = random.nextInt(45, Math.max(46, width - 45));
        targetY = random.nextInt(height / 2 - 90, height / 2 + 90);
        fruitKind = random(5);
    }

    private void spawnEnemies() {
        mobs.clear();
        for (int i = 0; i < 4; i++) mobs.add(new Mob(random.nextInt(20, Math.max(21, width - 20)), random.nextInt(70, Math.max(71, height - 70)), 1));
    }

    private void setupCandyBoard() {
        for (int r = 0; r < 6; r++) for (int c = 0; c < 6; c++) board[r][c] = random(COLORS.length);
    }

    private void drawCards() {
        for (int i = 0; i < cards.length; i++) cards[i] = random.nextInt(1, 11);
    }

    private void setupBalloonDefense() {
        timer = 1500; towerHp = 0; score = 0;
        message = "Clique para posicionar torres na grade. Elas estouram balões automaticamente.";
    }

    private void setupGarden() {
        timer = 1500; score = 0; health = 5; towerHp = 0;
        message = "Plante defensores clicando nas casas; não deixe os monstros chegarem à esquerda.";
    }

    private void setupMetro() {
        timer = 1200; subwayStation = -1; metroLine = 0; passengerNeed = 0; passengerHave = 0;
        message = "Conecte estações com cliques: escolha duas estações para formar uma linha.";
    }

    private void newApplicant() {
        applicant = NAMES[random(NAMES.length)];
        documentValid = random(2) == 1;
        answerCorrect = documentValid ? 2027 : 2025;
        message = "Confira validade e carimbo: aprove apenas documentos válidos.";
    }

    private void drawPokerHand() {
        for (int i = 0; i < 5; i++) cards[i] = random.nextInt(2, 15);
    }

    private void updateGame() {
        ticks++;
        switch (game) {
            case JETPACK -> updateJetpack();
            case COOKIE, POKER -> { if (game == Game.POKER && ticks % 100 == 0) drawPokerHand(); }
            case KITCHEN, PAPER_PLANE, SUBWAY, FRUIT_SLICE, SURVIVOR, WHEEL, GEOMETRY, BALLOON_TD, GARDEN_TD, MINI_METRO -> {
                if (timer > 0 && --timer == 0) finishByTimer();
                if (!gameOver) {
                    if (game == Game.PAPER_PLANE) updatePaperPlane();
                    else if (game == Game.SUBWAY) updateSubway();
                    else if (game == Game.FRUIT_SLICE && ticks % 28 == 0) spawnFruit();
                    else if (game == Game.SURVIVOR) updateSurvivor();
                    else if (game == Game.WHEEL) updateWheel();
                    else if (game == Game.GEOMETRY) updateGeometry();
                    else if (game == Game.BALLOON_TD) updateBalloonDefense();
                    else if (game == Game.GARDEN_TD) updateGarden();
                    else if (game == Game.MINI_METRO) updateMetro();
                }
            }
            case HELIX -> { if (ticks % 36 == 0) helixDrop(); }
            case BLACK_TILE -> { }
            case DIGGER -> { if (ticks % 30 == 0) diggerEnemiesMove(); }
            case SLINGSHOT -> updateProjectile();
            case MARBLE -> { }
            case CUT_ROPE -> updateCandy();
            case CANDY_MATCH -> { }
            case DECK_CLIMB -> { }
            case MOUNTAIN -> { }
            case LEMMINGS -> updateLemmings();
            case BORDER -> { }
        }
    }

    private void finishByTimer() {
        gameOver = true;
        switch (game) {
            case JETPACK -> message = "Combustível acabou! Moedas: " + score + ".";
            case KITCHEN -> message = "Turno encerrado: " + score + " pedidos servidos.";
            case PAPER_PLANE -> message = "Voo concluído! Pontos: " + score + ".";
            case SUBWAY -> message = "Corrida encerrada! Distância: " + score + ".";
            case FRUIT_SLICE -> message = "Fim! Frutas cortadas: " + score + ".";
            case SURVIVOR -> message = "Você sobreviveu! Inimigos derrotados: " + score + ".";
            case WHEEL -> message = "Corrida concluída com " + score + " metros!";
            case GEOMETRY -> message = "Fim da pista: pontuação " + score + ".";
            case BALLOON_TD -> message = "Onda completa! Balões estourados: " + score + ".";
            case GARDEN_TD -> message = "Jardim protegido! Pontos: " + score + ".";
            case MINI_METRO -> message = "Fim do expediente. Passageiros entregues: " + score + ".";
            case POKER -> message = "Rodada encerrada. Pontos: " + score + ".";
            default -> message = "Tempo encerrado!";
        }
    }

    private void updateJetpack() {
        if (timer > 0 && --timer == 0) { gameOver = true; message = "Combustível acabou! Moedas: " + score + "."; }
        if (ticks % 8 != 0 || gameOver) return;
        if (jumping && fuel > 0) { altitude = Math.max(0, altitude - 1); fuel--; }
        else altitude = Math.min(4, altitude + 1);
        if (random(100) < 42) mobs.add(new Mob(width + 10, random.nextInt(100, Math.max(101, height - 60)), random(2)));
        for (Mob m : mobs) m.x -= 6;
        mobs.removeIf(m -> m.x < -10);
        for (Mob m : mobs) if (Math.abs(m.x - width / 2) < 14 && Math.abs(m.y - (height / 2 + (altitude - 2) * 24)) < 18) {
            if (m.type == 1) { score++; currency++; } else { gameOver = true; message = "Você atingiu uma barreira! Moedas: " + score; }
            m.x = -100;
        }
        mobs.removeIf(m -> m.x < -50);
        if (fuel < 100 && ticks % 30 == 0) fuel++;
    }

    private void updatePaperPlane() {
        if (ticks % 5 == 0) {
            x -= 5;
            y += vy;
            vy = Math.min(5, vy + 1);
            if (y < 60 || y > height - 55) { gameOver = true; message = "O avião saiu da rota! Aros atravessados: " + score; return; }
            if (x < -10) { x = width + 10; score++; targetY = random.nextInt(100, Math.max(101, height - 80)); }
            if (Math.abs(x - width / 2) < 12 && Math.abs(y - targetY) > 45) { gameOver = true; message = "A asa bateu no aro! Aros atravessados: " + score; }
        }
    }

    private void updateSubway() {
        if (ticks % 9 == 0) {
            for (int r = 8; r > 0; r--) System.arraycopy(board[r - 1], 0, board[r], 0, 3);
            Arrays.fill(board[0], 0);
            if (random(100) < 72) board[0][random(3)] = 1;
            score++;
            if (board[7][lane] == 1 && phase == 0) { gameOver = true; message = "Trombou! Distância: " + score + "."; }
            if (phase > 0) phase--;
        }
        if (jumping && phase == 0) { jumping = false; }
    }

    private void updateSurvivor() {
        if (ticks % 18 == 0 && mobs.size() < 8) mobs.add(new Mob(random.nextInt(10, Math.max(11, width - 10)), random.nextInt(65, Math.max(66, height - 60)), 2));
        if (ticks % 25 == 0 && !mobs.isEmpty()) {
            Mob nearest = mobs.get(0); double best = Double.MAX_VALUE;
            for (Mob m : mobs) { double d = Math.hypot(m.x - x, m.y - y); if (d < best) { best = d; nearest = m; } }
            nearest.hp--;
            if (nearest.hp <= 0) { mobs.remove(nearest); score++; }
        }
        for (Mob m : mobs) {
            m.x += Integer.compare(x, m.x); m.y += Integer.compare(y, m.y);
            if (Math.abs(m.x - x) < 10 && Math.abs(m.y - y) < 10) { playerHp--; m.x = random.nextInt(0, width); m.y = 60; if (playerHp <= 0) { gameOver = true; message = "Cercado! Inimigos derrotados: " + score; } }
        }
    }

    private void updateWheel() {
        if (ticks % 12 == 0) {
            score++;
            if (random(100) < 12) wheelLean += random(2) == 0 ? 1 : -1;
            wheelLean += Integer.compare(0, wheelLean);
            if (Math.abs(wheelLean) > 4) { gameOver = true; message = "Você perdeu o equilíbrio após " + score + " metros."; }
            if (jumping) jumping = false;
        }
    }

    private void updateGeometry() {
        if (ticks % 11 == 0) {
            score++;
            if (phase > 0) phase--;
            if (phase == 0 && random(100) < 28) { gameOver = true; message = "Bateu no espinho! Pontuação: " + score; }
            if (jumping && phase == 0) jumping = false;
        }
    }

    private void updateBalloonDefense() {
        if (ticks % 16 != 0) return;
        if (random(100) < 80) mobs.add(new Mob(16, height / 2 - 54 + random(6) * 18, 1));
        for (Mob m : mobs) m.x += 5;
        for (Mob m : mobs) {
            for (int r = 0; r < 5; r++) for (int c = 0; c < 8; c++) if (board[r][c] == 1 && Math.abs((30 + c * 58) - m.x) < 70) { m.hp--; break; }
            if (m.hp <= 0) { score++; m.x = width + 100; }
            else if (m.x > width - 25) { health--; m.x = width + 100; if (health <= 0) { gameOver = true; message = "Os balões passaram! Você estourou " + score + "."; } }
        }
        mobs.removeIf(m -> m.x > width + 50);
    }

    private void updateGarden() {
        if (ticks % 20 != 0) return;
        if (random(100) < 45) mobs.add(new Mob(width - 25, 100 + random(5) * 48, 2));
        for (Mob m : mobs) {
            int row = Math.max(0, Math.min(4, (m.y - 100) / 48));
            for (int c = 0; c < 6; c++) if (board[row][c] == 1 && Math.abs((40 + c * 64) - m.x) < 120) { m.hp--; break; }
            if (m.hp <= 0) { score++; m.x = -50; } else m.x -= 4;
            if (m.x < 25) { health--; m.x = -50; if (health <= 0) { gameOver = true; message = "O jardim foi invadido! Pontos: " + score; } }
        }
        mobs.removeIf(m -> m.x < -40);
    }

    private void updateMetro() {
        if (ticks % 35 == 0) {
            passengerNeed = random(3);
            passengerHave++;
            if (passengerHave >= 3) { score++; passengerHave = 0; }
        }
    }

    private void updateProjectile() {
        if (phase != 1 || gameOver) return;
        x += vx; y += vy; vy++;
        if (Math.hypot(x - targetX, y - targetY) < 24) { score++; towerHp--; phase = 0; x = aimX; y = aimY; if (towerHp <= 0) { gameOver = won = true; message = "Alvo derrubado!"; } else message = "Acerto! Restam " + towerHp + " caixas."; }
        else if (x > width || y > height || x < 0) { phase = 0; message = "Errou! Ajuste a mira e tente novamente."; }
    }

    private void updateCandy() {
        if (ropeCut == 1 && candyY < height - 65) {
            candyY += 4;
            if (Math.abs((width / 2) - targetX) < 30 && Math.abs(candyY - targetY) < 18) { score = 1; gameOver = won = true; message = "O doce alcançou a estrela!"; }
        } else if (ropeCut == 1 && candyY >= height - 65) { gameOver = true; message = "O doce caiu longe da estrela."; }
    }

    private void diggerEnemiesMove() {
        for (int r = 0; r < 7; r++) for (int c = 0; c < 8; c++) if (board[r][c] == 2 && random(4) == 0) {
            int nr = Math.max(0, r - 1), nc = Math.max(0, Math.min(7, c - random(2)));
            board[r][c] = 0; if (board[nr][nc] == 0) board[nr][nc] = 2; else board[r][c] = 2;
            if (nr == 0 && nc == 0) { health--; if (health <= 0) { gameOver = true; message = "Os escavadores te alcançaram."; } }
        }
    }

    private void updateLemmings() {
        if (ticks % 8 != 0) return;
        lane++;
        if (lane >= 12) { score += round; round = 0; lane = 0; if (score >= 8) { gameOver = won = true; message = "Todos os mineiros chegaram em segurança!"; } }
        if (lane == 6 && board[0][6] == 0) { round--; lane = 0; misses++; if (round <= 0) { gameOver = true; message = "Os mineiros caíram na fenda. Resgatados: " + score; } }
    }

    private void helixDrop() {
        if (lane == phase) { score++; round++; phase = random(8); if (round >= 10) { gameOver = won = true; message = "Você desceu os dez andares!"; } }
        else { gameOver = true; message = "A bola bateu na plataforma. Andares: " + score; }
    }

    @Override
    public void tick() { if (initialized && !gameOver) updateGame(); }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        renderBackground(c);
        c.fill(0, 0, width, height, 0xD00A1220);
        c.fill(12, 8, width - 12, height - 34, 0xA91B2D40);
        c.drawCenteredTextWithShadow(textRenderer, title, width / 2, 13, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, message, width / 2, 32, gameOver ? 0xFFFFD66A : 0xFFD9E8F5);
        switch (game) {
            case JETPACK -> renderJetpack(c);
            case COOKIE -> renderCookie(c);
            case KITCHEN -> renderKitchen(c);
            case PAPER_PLANE -> renderPaperPlane(c);
            case HELIX -> renderHelix(c);
            case BLACK_TILE -> renderBlackTiles(c);
            case SUBWAY -> renderSubway(c);
            case DIGGER -> renderDigger(c);
            case SLINGSHOT -> renderSlingshot(c);
            case MARBLE -> renderMarbles(c);
            case FRUIT_SLICE -> renderFruit(c);
            case SURVIVOR -> renderSurvivor(c);
            case CUT_ROPE -> renderRope(c);
            case CANDY_MATCH -> renderCandy(c);
            case WHEEL -> renderWheel(c);
            case DECK_CLIMB -> renderDeck(c);
            case MOUNTAIN -> renderMountain(c);
            case BALLOON_TD -> renderBalloonDefense(c);
            case GARDEN_TD -> renderGarden(c);
            case LEMMINGS -> renderLemmings(c);
            case GEOMETRY -> renderGeometry(c);
            case MINI_METRO -> renderMetro(c);
            case BORDER -> renderBorder(c);
            case POKER -> renderPoker(c);
        }
        if (gameOver) c.drawCenteredTextWithShadow(textRenderer, Text.literal("R para tentar de novo"), width / 2, height - 43, 0xFFFFD66A);
        super.render(c, mouseX, mouseY, delta);
    }

    private void renderJetpack(DrawContext c) {
        int gy = height / 2 + (altitude - 2) * 24;
        c.fill(30, height / 2 + 70, width - 30, height / 2 + 76, 0xFF6C8293);
        c.fill(width / 2 - 10, gy - 10, width / 2 + 10, gy + 10, 0xFF4CC7E5);
        c.fill(width / 2 + 9, gy - 3, width / 2 + 20, gy + 4, jumping ? 0xFFFF913D : 0xFF647789);
        for (Mob m : mobs) if (m.x > 0) disc(c, m.x, m.y, 9, m.type == 1 ? 0xFFFFD34F : 0xFFDF5662);
        c.drawTextWithShadow(textRenderer, Text.literal("Moedas: " + score + "   Combustível: " + fuel), 25, 55, 0xFFFFFFFF);
    }

    private void renderCookie(DrawContext c) {
        disc(c, width / 2 - 58, height / 2, 57, 0xFFBE783B);
        disc(c, width / 2 - 58, height / 2, 49, 0xFFD89A57);
        for (int i = 0; i < 8; i++) disc(c, width / 2 - 82 + i % 4 * 15, height / 2 - 26 + i / 4 * 30, 4, 0xFF583522);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("BISCOITOS: " + score), width / 2, height / 2 + 75, 0xFFFFE59A);
        c.fill(width - 150, height / 2 - 40, width - 24, height / 2 + 12, 0xFF397356);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("COMPRAR +1/CLIQUE"), width - 87, height / 2 - 20, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Custo: " + (currency + 10)), width - 87, height / 2, 0xFFD8F1D8);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Melhorias: " + currency / 10), width - 87, height / 2 + 30, 0xFFFFFFFF);
    }

    private void renderKitchen(DrawContext c) {
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("PEDIDO: " + order), width / 2, height / 2 - 58, 0xFFFFD36A);
        for (int i = 0; i < INGREDIENTS.length; i++) {
            int bx = width / 2 - 180 + i % 3 * 120, by = height / 2 - 20 + i / 3 * 60;
            box(c, bx, by, 105, 42, COLORS[i % COLORS.length], INGREDIENTS[i]);
        }
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Pedidos servidos: " + score + " • Etapa: " + ingredientIndex + "/3"), width / 2, height / 2 + 115, 0xFFFFFFFF);
    }

    private void renderPaperPlane(DrawContext c) {
        c.fill(0, height / 2 + 80, width, height / 2 + 84, 0xFF70859A);
        int py = Math.max(65, Math.min(height - 65, y));
        c.fill(x - 14, py - 5, x + 12, py + 5, 0xFFEAF5FF);
        c.fill(x - 4, py - 16, x + 13, py + 2, 0xFF91C7E7);
        c.fill(width / 2 - 25, targetY - 5, width / 2 + 25, targetY + 5, 0xFF65D7A2);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Aros atravessados: " + score), width / 2, 58, 0xFFFFFFFF);
    }

    private void renderHelix(DrawContext c) {
        for (int i = 0; i < 5; i++) {
            int yy = height / 2 - 80 + i * 34;
            c.fill(width / 2 - 105, yy, width / 2 + 105, yy + 10, 0xFF586A89);
            c.fill(width / 2 - 12 + ((phase + i) % 4) * 17, yy - 2, width / 2 + 28 + ((phase + i) % 4) * 17, yy + 12, 0xFF0D1826);
        }
        disc(c, width / 2 + (lane - 3) * 25, height / 2 - 60 + (round % 5) * 34, 12, 0xFFFFD34D);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Andares: " + score + " • Abertura: " + phase + " • Gire ←/→"), width / 2, height / 2 + 112, 0xFFFFFFFF);
    }

    private void renderBlackTiles(DrawContext c) {
        int size = Math.min(48, Math.min((width - 60) / 4, (height - 105) / 5));
        int x0 = (width - size * 4) / 2, y0 = height / 2 - size * 2;
        for (int r = 0; r < 5; r++) for (int col = 0; col < 4; col++) {
            int xx = x0 + col * size, yy = y0 + r * size;
            boolean black = rows[r] == col;
            c.fill(xx + 2, yy + 2, xx + size - 3, yy + size - 3, black ? 0xFF223044 : 0xFFF1F4F7);
        }
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Azulejos corretos: " + score), width / 2, y0 + size * 5 + 8, 0xFFFFFFFF);
    }

    private void renderSubway(DrawContext c) {
        for (int col = 0; col < 3; col++) c.fill(width / 2 - 95 + col * 64, 70, width / 2 - 90 + col * 64, height - 55, 0xFF70879A);
        for (int r = 0; r < 8; r++) for (int col = 0; col < 3; col++) if (board[r][col] == 1) box(c, width / 2 - 112 + col * 64, 80 + r * 38, 38, 27, 0xFFD25252, "■");
        int px = width / 2 - 100 + lane * 64, py = height - 86 - (jumping ? 30 : 0);
        box(c, px, py, 30, 34, 0xFF48B9DC, "●");
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Distância: " + score + "   •   Vida: " + health), width / 2, 57, 0xFFFFFFFF);
    }

    private void renderDigger(DrawContext c) {
        int size = Math.min(44, Math.min((width - 60) / 8, (height - 120) / 7));
        int x0 = (width - 8 * size) / 2, y0 = height / 2 - 7 * size / 2;
        for (int r = 0; r < 7; r++) for (int col = 0; col < 8; col++) {
            int val = board[r][col], bx = x0 + col * size, by = y0 + r * size;
            int color = val == 1 ? 0xFF785536 : val == 2 ? 0xFFB75B4B : val == 3 ? 0xFF4DC9E8 : 0xFF334B4C;
            c.fill(bx + 2, by + 2, bx + size - 2, by + size - 2, color);
            String mark = val == 1 ? "■" : val == 2 ? "M" : val == 3 ? "◆" : "";
            if (!mark.isEmpty()) c.drawCenteredTextWithShadow(textRenderer, Text.literal(mark), bx + size / 2, by + size / 2 - 4, 0xFFFFFFFF);
        }
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Cristal: azul   •   Vidas: " + health), width / 2, y0 + 7 * size + 4, 0xFFFFFFFF);
    }

    private void renderSlingshot(DrawContext c) {
        c.fill(25, height / 2 + 75, width - 25, height / 2 + 80, 0xFF806D56);
        c.fill(width / 2 - 105, height / 2 - 8, width / 2 - 98, height / 2 + 60, 0xFF9E6038);
        c.fill(targetX - 22, targetY - 32, targetX + 24, targetY + 32, 0xFFBB7651);
        c.fill(targetX - 12, targetY - 22, targetX + 14, targetY + 22, 0xFFE6B26D);
        disc(c, phase == 1 ? x : aimX, phase == 1 ? y : aimY, 8, 0xFF5C5550);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Caixas restantes: " + towerHp + " • clique para mirar, Espaço para lançar"), width / 2, height / 2 - 78, 0xFFFFFFFF);
    }

    private void renderMarbles(DrawContext c) {
        int cell = Math.min(36, (width - 50) / 14), x0 = (width - cell * 14) / 2, y0 = height / 2 - 18;
        for (int i = 0; i < 14; i++) if (board[0][i] > 0) disc(c, x0 + i * cell + cell / 2, y0, cell / 3, COLORS[board[0][i] - 1]);
        for (int i = 0; i < 4; i++) box(c, width / 2 - 116 + i * 60, height / 2 + 47, 48, 40, COLORS[i], "●");
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Orbes removidos: " + score + " • clique numa cor para disparar"), width / 2, height / 2 - 78, 0xFFFFFFFF);
    }

    private void renderFruit(DrawContext c) {
        disc(c, targetX, targetY, 21, fruitKind == 4 ? 0xFF30323A : COLORS[fruitKind]);
        if (fruitKind != 4) disc(c, targetX - 5, targetY - 6, 4, 0xFFFFE3A2);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Frutas: " + score + " • Erros: " + misses + " • " + timer / 20 + "s"), width / 2, 65, 0xFFFFFFFF);
    }

    private void renderSurvivor(DrawContext c) {
        for (Mob m : mobs) disc(c, m.x, m.y, 10, 0xFFB74D67);
        disc(c, x, y, 12, 0xFF5DBCE3);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Vida: " + playerHp + " • Abatidos: " + score + " • WASD/setas"), width / 2, 62, 0xFFFFFFFF);
    }

    private void renderRope(DrawContext c) {
        int hookX = width / 2, hookY = height / 2 - 85;
        if (ropeCut == 0) c.fill(hookX - 2, hookY, hookX + 2, candyY, 0xFFE6E6D5);
        disc(c, hookX, candyY, 17, 0xFFE35C50);
        disc(c, targetX, targetY, 9, 0xFFFFD94A);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Estrela dourada"), width / 2, height / 2 + 105, 0xFFFFFFFF);
    }

    private void renderCandy(DrawContext c) {
        int size = Math.min(48, Math.min((width - 70) / 6, (height - 110) / 6));
        int x0 = (width - 6 * size) / 2, y0 = height / 2 - 3 * size;
        for (int r = 0; r < 6; r++) for (int col = 0; col < 6; col++) disc(c, x0 + col * size + size / 2, y0 + r * size + size / 2, size / 3, COLORS[board[r][col]]);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Pontos: " + score + " • clique em dois doces vizinhos"), width / 2, y0 + size * 6 + 5, 0xFFFFFFFF);
    }

    private void renderWheel(DrawContext c) {
        int cx = width / 2, cy = height / 2 + 40;
        c.fill(25, cy + 32, width - 25, cy + 37, 0xFF806F58);
        disc(c, cx - 35, cy + 23, 15, 0xFF242B35); disc(c, cx + 38, cy + 23, 15, 0xFF242B35);
        c.fill(cx - 35, cy - 15, cx + 38, cy + 25, 0xFF4BAED3);
        c.fill(cx - 25, cy - 40 - wheelLean * 5, cx - 20, cy - 5, 0xFFBEE7F7);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Equilíbrio: " + wheelLean + " • Distância: " + score), cx, cy - 95, 0xFFFFFFFF);
    }

    private void renderDeck(DrawContext c) {
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("HERÓI " + playerHp + " ♥     INIMIGO " + enemyHp + " ♦     Escudo " + shield), width / 2, height / 2 - 80, 0xFFFFFFFF);
        String[] labels = {"GOLPE 6", "DEFESA 5", "CURA 4"};
        for (int i = 0; i < 3; i++) box(c, width / 2 - 165 + i * 112, height / 2 - 15, 100, 88, COLORS[i], labels[i]);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Turno " + (round + 1)), width / 2, height / 2 + 95, 0xFFFFFFFF);
    }

    private void renderMountain(DrawContext c) {
        int ground = height / 2 + 70;
        for (int i = 0; i < 8; i++) c.fill(30 + i * 50, ground - (i % 3) * 22, 80 + i * 50, ground + 30, 0xFF67584A);
        c.fill(30 + x * 45, ground - y * 20 - 18, 48 + x * 45, ground - y * 20, 0xFF59B9DF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Bandeira no cume →"), width - 85, ground - 100, 0xFFFFD564);
    }

    private void renderBalloonDefense(DrawContext c) {
        for (int col = 0; col < 8; col++) c.fill(30 + col * 58, height / 2 - 8, 80 + col * 58, height / 2 + 9, 0xFF3E4B53);
        for (int r = 0; r < 5; r++) for (int col = 0; col < 8; col++) if (board[r][col] == 1) disc(c, 30 + col * 58, 76 + r * 38, 13, 0xFF56BB74);
        for (Mob m : mobs) disc(c, m.x, m.y, 9, 0xFFED5C62);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Torres: " + towerHp + " • Balões: " + score + " • Saídas restantes: " + health), width / 2, height - 57, 0xFFFFFFFF);
    }

    private void renderGarden(DrawContext c) {
        for (int r = 0; r < 5; r++) for (int col = 0; col < 6; col++) {
            int xx = 35 + col * 64, yy = 78 + r * 48;
            c.fill(xx, yy, xx + 56, yy + 40, 0xFF405B42);
            if (board[r][col] == 1) disc(c, xx + 28, yy + 20, 13, 0xFF74D077);
        }
        for (Mob m : mobs) disc(c, m.x, m.y, 13, 0xFF9B698A);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Defensores: " + towerHp + " • Invasores vencidos: " + score + " • Portão: " + health), width / 2, height - 56, 0xFFFFFFFF);
    }

    private void renderLemmings(DrawContext c) {
        int ground = height / 2 + 35;
        c.fill(35, ground, width - 35, ground + 14, 0xFF6D5A48);
        c.fill(width / 2 - 30, ground, width / 2 + 30, ground + 17, board[0][6] == 1 ? 0xFF5B9F63 : 0xFF172332);
        for (int i = 0; i < Math.max(0, round); i++) disc(c, 45 + ((lane * 14 + i * 39) % (width - 100)), ground - 10, 7, 0xFF6BC9E8);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Resgatados: " + score + " • Mineiros na pista: " + round), width / 2, ground + 40, 0xFFFFFFFF);
    }

    private void renderGeometry(DrawContext c) {
        int ground = height / 2 + 42;
        c.fill(25, ground, width - 25, ground + 5, 0xFF697C89);
        int spikeX = width / 2 + (10 - (ticks / 11) % 10) * 24;
        c.fill(spikeX, ground - 24, spikeX + 24, ground, 0xFFE75B65);
        c.fill(width / 2 - 90, ground - (jumping ? 40 : 25), width / 2 - 65, ground, 0xFF59B8E1);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Pontos: " + score + " • Espaço salta"), width / 2, ground + 20, 0xFFFFFFFF);
    }

    private void renderMetro(DrawContext c) {
        int[][] stations = {{width / 2 - 120, height / 2 - 50}, {width / 2 - 20, height / 2 - 80}, {width / 2 + 100, height / 2 - 35}, {width / 2 - 80, height / 2 + 65}, {width / 2 + 50, height / 2 + 75}};
        for (int i = 0; i < stations.length; i++) if (board[1][i] > 0) drawLine(c, stations[i][0], stations[i][1], stations[board[1][i] - 1][0], stations[board[1][i] - 1][1], 0xFF50C1CD);
        for (int i = 0; i < stations.length; i++) disc(c, stations[i][0], stations[i][1], 12, i == passengerNeed ? 0xFFFFC95C : 0xFFECF4F7);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Passageiros entregues: " + score + " • Clique em estações para conectar"), width / 2, height - 55, 0xFFFFFFFF);
    }

    private void renderBorder(DrawContext c) {
        c.fill(width / 2 - 92, height / 2 - 82, width / 2 + 92, height / 2 + 72, 0xFFE9E2CC);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("DOCUMENTO DE " + applicant.toUpperCase(java.util.Locale.ROOT)), width / 2, height / 2 - 68, 0xFF283849);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Válido até: " + answerCorrect), width / 2, height / 2 - 28, 0xFF283849);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("CARIMBO: " + (documentValid ? "AUTORIZADO" : "EXPIRADO")), width / 2, height / 2 + 4, documentValid ? 0xFF398855 : 0xFFB84242);
        box(c, width / 2 - 150, height / 2 + 88, 130, 42, 0xFF459362, "APROVAR");
        box(c, width / 2 + 20, height / 2 + 88, 130, 42, 0xFFB94D4D, "RECUSAR");
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Casos corretos: " + score), width / 2, height / 2 + 142, 0xFFFFFFFF);
    }

    private void renderPoker(DrawContext c) {
        for (int i = 0; i < 5; i++) {
            int bx = width / 2 - 142 + i * 58;
            c.fill(bx, height / 2 - 40, bx + 48, height / 2 + 26, 0xFFF2ECDC);
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(cardRank(cards[i])), bx + 24, height / 2 - 10, cards[i] % 2 == 0 ? 0xFFCC4A53 : 0xFF26394E);
        }
        box(c, width / 2 - 80, height / 2 + 52, 160, 40, 0xFF497BA7, "JOGAR MÃO");
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Pontuação: " + score + " • Meta: 100"), width / 2, height / 2 + 110, 0xFFFFFFFF);
    }

    private String cardRank(int value) { return value <= 10 ? String.valueOf(value) : value == 11 ? "J" : value == 12 ? "Q" : value == 13 ? "K" : "A"; }
    private void box(DrawContext c, int x, int y, int w, int h, int color, String label) { c.fill(x, y, x + w, y + h, color); c.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + w / 2, y + h / 2 - 4, 0xFFFFFFFF); }
    private void disc(DrawContext c, int cx, int cy, int radius, int color) { for (int dy = -radius; dy <= radius; dy++) { int dx = (int)Math.sqrt(radius * radius - dy * dy); c.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color); } }
    private void drawLine(DrawContext c, int x1, int y1, int x2, int y2, int color) { int steps = Math.max(1, Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1))); for (int i = 0; i <= steps; i++) { int px = x1 + (x2 - x1) * i / steps, py = y1 + (y2 - y1) * i / steps; c.fill(px - 2, py - 2, px + 3, py + 3, color); } }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        if (button != 0 || gameOver) return false;
        switch (game) {
            case COOKIE -> clickCookie(mx, my);
            case KITCHEN -> clickIngredient(mx, my);
            case BLACK_TILE -> clickBlackTile(mx, my);
            case DIGGER -> clickDirt(mx, my);
            case SLINGSHOT -> { aimX = (int)mx; aimY = (int)my; }
            case MARBLE -> fireMarble(mx, my);
            case FRUIT_SLICE -> { if (Math.hypot(mx - targetX, my - targetY) < 28) { if (fruitKind == 4) { misses++; score = Math.max(0, score - 2); } else score++; spawnFruit(); } else misses++; }
            case CUT_ROPE -> { if (ropeCut == 0 && mx > width / 2 - 12 && mx < width / 2 + 12 && my > height / 2 - 85 && my < candyY) ropeCut = 1; }
            case CANDY_MATCH -> clickCandy(mx, my);
            case DECK_CLIMB -> playCard(mx, my);
            case MOUNTAIN -> { if (y < 0 && x >= 6) { score++; gameOver = won = true; message = "Você alcançou o cume!"; } }
            case BALLOON_TD -> placeTower(mx, my, false);
            case GARDEN_TD -> placeTower(mx, my, true);
            case LEMMINGS -> { if (mx > width / 2 - 38 && mx < width / 2 + 38) board[0][6] = 1; }
            case MINI_METRO -> clickStation(mx, my);
            case BORDER -> decideDocument(mx < width / 2);
            case POKER -> { if (my > height / 2 + 45 && my < height / 2 + 105) playPokerHand(); }
            default -> { }
        }
        return true;
    }

    private void clickCookie(double mx, double my) {
        if (Math.hypot(mx - (width / 2 - 58), my - height / 2) < 60) { score += 1 + currency / 10; return; }
        if (mx > width - 155 && my > height / 2 - 45 && my < height / 2 + 20 && score >= currency + 10) { score -= currency + 10; currency += 10; message = "Melhoria comprada! Cada clique vale " + (1 + currency / 10) + "."; }
    }

    private void clickIngredient(double mx, double my) {
        for (int i = 0; i < INGREDIENTS.length; i++) {
            int bx = width / 2 - 180 + i % 3 * 120, by = height / 2 - 20 + i / 3 * 60;
            if (mx >= bx && mx <= bx + 105 && my >= by && my <= by + 42) {
                String expected = order.split(" → ")[ingredientIndex];
                if (INGREDIENTS[i].equals(expected)) { if (++ingredientIndex == 3) { score++; round++; if (round >= 8) { gameOver = won = true; message = "Turno perfeito: 8 pedidos servidos!"; } else { newOrder(); message = "Pedido servido! Próximo!"; } } }
                else { misses++; ingredientIndex = 0; message = "Ingrediente errado! Recomece este pedido."; }
                return;
            }
        }
    }

    private void clickBlackTile(double mx, double my) {
        int size = Math.min(48, Math.min((width - 60) / 4, (height - 105) / 5));
        int x0 = (width - size * 4) / 2, y0 = height / 2 - size * 2;
        int row = (int)((my - y0) / size), col = (int)((mx - x0) / size);
        if (row == 4 && col >= 0 && col < 4 && mx >= x0 && rows[4] == col) {
            score++; for (int i = 5; i > 0; i--) rows[i] = rows[i - 1]; rows[0] = random(4);
        } else if (row == 4 && col >= 0 && col < 4) { gameOver = true; message = "Você tocou no azulejo claro. Pontuação: " + score; }
    }

    private void clickDirt(double mx, double my) {
        int size = Math.min(44, Math.min((width - 60) / 8, (height - 120) / 7));
        int col = (int)((mx - (width - 8 * size) / 2) / size), row = (int)((my - (height / 2 - 7 * size / 2)) / size);
        if (row < 0 || row >= 7 || col < 0 || col >= 8) return;
        if (board[row][col] == 3) { score = 1; gameOver = won = true; message = "Cristal encontrado!"; }
        else if (board[row][col] == 2) { health--; board[row][col] = 0; if (health <= 0) { gameOver = true; message = "Os monstros te cercaram."; } }
        else if (board[row][col] == 1) { board[row][col] = 0; score++; }
    }

    private void fireMarble(double mx, double my) {
        int color = -1;
        for (int i = 0; i < 4; i++) if (mx >= width / 2 - 116 + i * 60 && mx <= width / 2 - 68 + i * 60 && my >= height / 2 + 40) color = i + 1;
        if (color < 0) return;
        int empty = -1; for (int i = 13; i >= 0; i--) if (board[0][i] == 0) empty = i;
        if (empty < 0) { gameOver = true; message = "A fileira encheu! Orbes removidos: " + score; return; }
        board[0][empty] = color;
        for (int i = 0; i <= 11; i++) if (board[0][i] == color && board[0][i + 1] == color && board[0][i + 2] == color) { board[0][i] = board[0][i + 1] = board[0][i + 2] = 0; score += 3; compactMarbles(); return; }
    }

    private void compactMarbles() { int at = 0; for (int i = 0; i < 14; i++) if (board[0][i] > 0) board[0][at++] = board[0][i]; while (at < 14) board[0][at++] = 0; }

    private void clickCandy(double mx, double my) {
        int size = Math.min(48, Math.min((width - 70) / 6, (height - 110) / 6));
        int c = (int)((mx - (width - 6 * size) / 2) / size), r = (int)((my - (height / 2 - 3 * size)) / size);
        if (r < 0 || r >= 6 || c < 0 || c >= 6) return;
        if (phase == 0) { x = c; y = r; phase = 1; }
        else {
            if (Math.abs(x - c) + Math.abs(y - r) == 1) {
                int v = board[y][x]; board[y][x] = board[r][c]; board[r][c] = v;
                int matches = countCandyMatches();
                if (matches > 0) { score += matches * 10; phase = 0; }
                else { v = board[y][x]; board[y][x] = board[r][c]; board[r][c] = v; phase = 0; message = "Essa troca não forma uma combinação."; }
            } else { x = c; y = r; }
        }
    }

    private int countCandyMatches() {
        boolean[][] clear = new boolean[6][6]; int count = 0;
        for (int r = 0; r < 6; r++) for (int c = 0; c < 6; c++) {
            if (c + 2 < 6 && board[r][c] == board[r][c + 1] && board[r][c] == board[r][c + 2]) for (int k = c; k < 6 && board[r][k] == board[r][c]; k++) clear[r][k] = true;
            if (r + 2 < 6 && board[r][c] == board[r + 1][c] && board[r][c] == board[r + 2][c]) for (int k = r; k < 6 && board[k][c] == board[r][c]; k++) clear[k][c] = true;
        }
        for (int r = 0; r < 6; r++) for (int c = 0; c < 6; c++) if (clear[r][c]) { board[r][c] = random(COLORS.length); count++; }
        return count;
    }

    private void playCard(double mx, double my) {
        if (my < height / 2 - 15 || my > height / 2 + 73) return;
        int card = (int)((mx - (width / 2 - 165)) / 112);
        if (card < 0 || card > 2) return;
        if (card == 0) enemyHp -= 6;
        else if (card == 1) shield += 5;
        else playerHp = Math.min(24, playerHp + 4);
        round++;
        if (enemyHp <= 0) { score++; enemyHp = 18 + score * 3; playerHp = Math.min(24, playerHp + 3); if (score >= 3) { gameOver = won = true; message = "Você venceu três combates!"; } }
        int damage = Math.max(0, 5 - shield); shield = Math.max(0, shield - 5); playerHp -= damage;
        if (playerHp <= 0) { gameOver = true; message = "A expedição terminou após " + score + " vitórias."; }
        drawCards();
    }

    private void placeTower(double mx, double my, boolean garden) {
        int col = garden ? (int)((mx - 35) / 64) : (int)((mx - 15) / 58);
        int row = garden ? (int)((my - 78) / 48) : (int)((my - 60) / 38);
        int maxCol = garden ? 5 : 7, maxRow = garden ? 4 : 4;
        if (col < 0 || col > maxCol || row < 0 || row > maxRow) return;
        if (board[row][col] == 0) { board[row][col] = 1; towerHp++; }
    }

    private void clickStation(double mx, double my) {
        int[][] p = metroStations(); int nearest = -1; double dist = 28;
        for (int i = 0; i < p.length; i++) { double d = Math.hypot(mx - p[i][0], my - p[i][1]); if (d < dist) { nearest = i; dist = d; } }
        if (nearest >= 0) {
            if (subwayStation < 0) subwayStation = nearest;
            else { board[1][subwayStation] = nearest + 1; board[1][nearest] = subwayStation + 1; subwayStation = -1; metroLine++; if (metroLine >= 4) { score++; metroLine = 0; } }
        }
    }

    private int[][] metroStations() { return new int[][]{{width / 2 - 120,height / 2 - 50},{width / 2 - 20,height / 2 - 80},{width / 2 + 100,height / 2 - 35},{width / 2 - 80,height / 2 + 65},{width / 2 + 50,height / 2 + 75}}; }

    private void decideDocument(boolean approve) {
        if (approve == documentValid) score++; else misses++;
        round++;
        if (round >= 10) { gameOver = true; won = score >= 8; message = "Turno encerrado: " + score + "/10 decisões corretas."; }
        else newApplicant();
    }

    private void playPokerHand() {
        int[] counts = new int[15]; for (int card : cards) counts[card]++;
        int pairs = 0, triple = 0, four = 0;
        for (int n : counts) { if (n == 2) pairs++; if (n == 3) triple++; if (n == 4) four++; }
        int points = four > 0 ? 45 : triple > 0 && pairs > 0 ? 35 : triple > 0 ? 25 : pairs >= 2 ? 20 : pairs == 1 ? 10 : 3;
        score += points; round++;
        if (score >= 100 || round >= 8) { gameOver = true; won = score >= 100; message = won ? "Meta de 100 pontos alcançada!" : "Rodadas acabaram: " + score + "/100 pontos."; }
        else drawPokerHand();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) { resetGame(); return true; }
        if (gameOver) return super.keyPressed(keyCode, scanCode, modifiers);
        if (game == Game.JETPACK) { if (keyCode == GLFW.GLFW_KEY_SPACE) { jumping = true; return true; } }
        if (game == Game.SUBWAY) {
            if (keyCode == GLFW.GLFW_KEY_LEFT) lane = Math.max(0, lane - 1);
            else if (keyCode == GLFW.GLFW_KEY_RIGHT) lane = Math.min(2, lane + 1);
            else if (keyCode == GLFW.GLFW_KEY_UP && phase == 0) { jumping = true; phase = 5; }
            else if (keyCode == GLFW.GLFW_KEY_DOWN && phase == 0) phase = 3;
            else return super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (game == Game.PAPER_PLANE && keyCode == GLFW.GLFW_KEY_SPACE) { vy = -5; return true; }
        if (game == Game.HELIX) { if (keyCode == GLFW.GLFW_KEY_LEFT) lane = (lane + 7) % 8; else if (keyCode == GLFW.GLFW_KEY_RIGHT) lane = (lane + 1) % 8; else return super.keyPressed(keyCode, scanCode, modifiers); return true; }
        if (game == Game.SLINGSHOT && keyCode == GLFW.GLFW_KEY_SPACE && phase == 0) { x = width / 2 - 95; y = height / 2 + 30; vx = Math.max(2, (x - aimX) / 8); vy = Math.min(-2, (y - aimY) / 8); phase = 1; return true; }
        if (game == Game.SURVIVOR) {
            if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) x = Math.max(20, x - 14);
            else if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) x = Math.min(width - 20, x + 14);
            else if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_W) y = Math.max(60, y - 14);
            else if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_S) y = Math.min(height - 50, y + 14);
            else return super.keyPressed(keyCode, scanCode, modifiers); return true;
        }
        if (game == Game.WHEEL) { if (keyCode == GLFW.GLFW_KEY_LEFT) wheelLean--; else if (keyCode == GLFW.GLFW_KEY_RIGHT) wheelLean++; else if (keyCode == GLFW.GLFW_KEY_SPACE) jumping = true; else return super.keyPressed(keyCode, scanCode, modifiers); return true; }
        if (game == Game.MOUNTAIN) {
            if (keyCode == GLFW.GLFW_KEY_LEFT) x = Math.max(0, x - 1);
            else if (keyCode == GLFW.GLFW_KEY_RIGHT) x = Math.min(8, x + 1);
            else if (keyCode == GLFW.GLFW_KEY_SPACE) { y = Math.min(8, y + 2); if (y >= 8 && x >= 6) { score = 1; gameOver = won = true; message = "Você alcançou o cume!"; } return true; }
            else return super.keyPressed(keyCode, scanCode, modifiers); return true;
        }
        if (game == Game.GEOMETRY && keyCode == GLFW.GLFW_KEY_SPACE && phase == 0) { jumping = true; phase = 4; return true; }
        if (game == Game.CUT_ROPE && keyCode == GLFW.GLFW_KEY_SPACE && ropeCut == 0) { ropeCut = 1; return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (game == Game.JETPACK && keyCode == GLFW.GLFW_KEY_SPACE) { jumping = false; return true; }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    private static final class Mob {
        int x, y, hp, type;
        Mob(int x, int y, int type) { this.x = x; this.y = y; this.type = type; this.hp = type == 1 ? 1 : 2; }
    }
}
