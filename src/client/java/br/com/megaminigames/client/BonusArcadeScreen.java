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

/** Dez desafios extras com capas próprias no hub e controles simples. */
public final class BonusArcadeScreen extends Screen {
    public enum Game {
        WORD_SEARCH("Caça-Palavras"), MATH_DASH("Desafio Relâmpago"), REACTION("Teste de Reação"),
        CUP_SHUFFLE("Copos Mágicos"), LASER_DODGE("Desvie dos Lasers"), COLOR_CATCH("Frutas Coloridas"),
        FLAG_QUIZ("Quiz de Bandeiras"), TAP_CHALLENGE("Toque Rápido"), TOWER_STACK("Torre Perfeita"),
        BUBBLE_POP("Estoure as Bolhas");
        private final String title;
        Game(String title) { this.title = title; }
    }

    private static final String[] WORDS = {"MUNDO", "PRAIA", "FOLHA", "TIGRE", "LIVRO", "PEDRA", "NUVEM", "FESTA"};
    private static final String[] COLORS = {"VERMELHO", "AZUL", "VERDE", "AMARELO"};
    private static final int[] COLOR_VALUES = {0xFFE85656, 0xFF4D91ED, 0xFF54C77D, 0xFFFFD84F};
    private static final Flag[] FLAGS = {
            new Flag("Brasil", 0xFF229E45, 0xFFFFD447, 0xFF2876CB),
            new Flag("França", 0xFF3156A5, 0xFFFFFFFF, 0xFFE64E55),
            new Flag("Japão", 0xFFFFFFFF, 0xFFE54853, 0xFFE54853),
            new Flag("Itália", 0xFF31995F, 0xFFFFFFFF, 0xFFE64F55),
            new Flag("Alemanha", 0xFF252525, 0xFFE04B4B, 0xFFFFD34D),
            new Flag("Argentina", 0xFF76C7E9, 0xFFFFFFFF, 0xFF76C7E9),
            new Flag("Ucrânia", 0xFF4786E3, 0xFFFFD54B, 0xFFFFD54B),
            new Flag("Polônia", 0xFFFFFFFF, 0xFFE34D58, 0xFFE34D58)
    };

    private final Game game;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final int[][] laserGrid = new int[9][5];
    private final int[][] fruitGrid = new int[9][4];
    private final List<Integer> selectedLetters = new ArrayList<>();
    private final int[] cups = new int[3];
    private final int[] choices = new int[4];
    private final Flag[] flagChoices = new Flag[4];
    private final char[][] letters = new char[5][7];
    private String targetWord = "";
    private String message = "";
    private int score, round, phase, ticks, timer, misses;
    private int playerLane, targetColor, bubbleX, bubbleY, bubbleDelay, bubbleSize;
    private int cupTarget, shuffleSteps, reactionTicks, reactionTotal;
    private int numberA, numberB, correctChoice;
    private int towerBaseX, towerBaseWidth, towerBlockX, towerBlockWidth, towerDirection;
    private boolean gameOver, initialized;

    public BonusArcadeScreen(Game game) {
        super(Text.literal(game.title));
        this.game = game;
    }

    @Override
    protected void init() {
        addDrawableChild(ArcadeTheme.button(width / 2 - 116, height - 25, 112, 20, Text.literal("Reiniciar (R)"), () -> resetGame()));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, height - 25, 112, 20, Text.literal("Voltar ao hub"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) {
            resetGame();
            initialized = true;
        }
    }

    private int random(int max) { return random.nextInt(max); }

    private void resetGame() {
        Arrays.fill(laserGrid[0], 0);
        for (int[] row : laserGrid) Arrays.fill(row, 0);
        for (int[] row : fruitGrid) Arrays.fill(row, -1);
        selectedLetters.clear();
        score = round = phase = ticks = misses = 0;
        gameOver = false;
        switch (game) {
            case WORD_SEARCH -> setupWordSearch();
            case MATH_DASH -> { timer = 600; nextMathQuestion(); }
            case REACTION -> startReactionRound();
            case CUP_SHUFFLE -> setupCupRound();
            case LASER_DODGE -> { timer = 1200; playerLane = 2; message = "Desvie com ←/→ e sobreviva o máximo possível."; }
            case COLOR_CATCH -> { timer = 600; playerLane = 1; targetColor = random(4); message = "Pegue só as frutas da cor indicada com ←/→."; }
            case FLAG_QUIZ -> { timer = 900; nextFlagQuestion(); }
            case TAP_CHALLENGE -> { timer = 200; message = "Clique no botão o máximo de vezes em 10 segundos!"; }
            case TOWER_STACK -> setupTower();
            case BUBBLE_POP -> { timer = 600; bubbleDelay = 0; bubbleSize = 24; spawnBubble(); message = "Clique nas bolhas antes que mudem de lugar."; }
        }
    }

    private void setupWordSearch() {
        targetWord = WORDS[random(WORDS.length)];
        for (int row = 0; row < 5; row++) for (int col = 0; col < 7; col++) letters[row][col] = (char) ('A' + random(26));
        int row = random(5), col = random(8 - targetWord.length());
        for (int i = 0; i < targetWord.length(); i++) letters[row][col + i] = targetWord.charAt(i);
        selectedLetters.clear();
        message = "Encontre a palavra escondida na grade. Clique nas letras em ordem.";
    }

    private void nextMathQuestion() {
        numberA = random.nextInt(3, 20);
        numberB = random.nextInt(2, 16);
        correctChoice = numberA + numberB;
        int correctSlot = random(4);
        for (int i = 0; i < 4; i++) {
            if (i == correctSlot) choices[i] = correctChoice;
            else {
                int candidate;
                do candidate = correctChoice + random.nextInt(-8, 9); while (candidate < 0 || candidate == correctChoice || containsChoice(candidate, i));
                choices[i] = candidate;
            }
        }
        message = "Resolva a conta e escolha a resposta antes do tempo acabar.";
    }

    private boolean containsChoice(int value, int end) {
        for (int i = 0; i < end; i++) if (choices[i] == value) return true;
        return false;
    }

    private void answerMath(int index) {
        if (gameOver) return;
        if (choices[index] == correctChoice) score++;
        round++;
        if (round >= 10) {
            gameOver = true;
            message = "Desafio concluído: " + score + "/10 acertos.";
        } else nextMathQuestion();
    }

    private void startReactionRound() {
        phase = 0;
        timer = random.nextInt(45, 110);
        reactionTicks = 0;
        message = "Aguarde o sinal verde...";
    }

    private void reactionClick() {
        if (phase == 0) {
            misses++;
            phase = 2;
            timer = 35;
            message = "Queimou a largada! Aguarde e tente de novo.";
        } else if (phase == 1) {
            reactionTotal += reactionTicks;
            score++;
            round++;
            if (round >= 5) {
                gameOver = true;
                message = "Média: " + (reactionTotal * 50 / 5) + " ms. " + score + "/5 sinais no tempo!";
            } else {
                phase = 2;
                timer = 30;
                message = "Boa! Próxima rodada...";
            }
        }
    }

    private void setupCupRound() {
        Arrays.fill(cups, 0);
        cupTarget = random(3);
        cups[cupTarget] = 1;
        phase = 0;
        timer = 45;
        shuffleSteps = 0;
        message = "Memorize o copo com a estrela.";
    }

    private void swapCups(int a, int b) {
        int value = cups[a]; cups[a] = cups[b]; cups[b] = value;
    }

    private void pickCup(int cup) {
        if (phase != 2 || gameOver) return;
        if (cups[cup] == 1) {
            score++;
            round++;
            if (round >= 5) {
                gameOver = true;
                message = "Impressionante! Você encontrou a estrela nas cinco rodadas.";
            } else {
                setupCupRound();
                message = "Acertou! Memorize a próxima estrela.";
            }
        } else {
            gameOver = true;
            misses++;
            message = "A estrela estava em outro copo. Pontuação: " + score + "/5.";
        }
    }

    private void nextFlagQuestion() {
        Flag target = FLAGS[random(FLAGS.length)];
        flagChoices[0] = target;
        for (int i = 1; i < flagChoices.length; i++) {
            Flag pick;
            do pick = FLAGS[random(FLAGS.length)]; while (containsFlag(pick, i));
            flagChoices[i] = pick;
        }
        for (int i = flagChoices.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Flag temp = flagChoices[i]; flagChoices[i] = flagChoices[j]; flagChoices[j] = temp;
        }
        for (int i = 0; i < flagChoices.length; i++) if (flagChoices[i] == target) correctChoice = i;
        message = "Qual bandeira pertence ao país indicado?";
    }

    private boolean containsFlag(Flag flag, int end) {
        for (int i = 0; i < end; i++) if (flagChoices[i] == flag) return true;
        return false;
    }

    private void answerFlag(int choice) {
        if (gameOver) return;
        if (flagChoices[choice] == flagChoices[correctChoice]) score++;
        round++;
        if (round >= 8) {
            gameOver = true;
            message = "Quiz finalizado: " + score + "/8 países corretos.";
        } else nextFlagQuestion();
    }

    private void setupTower() {
        towerBaseWidth = Math.min(170, width / 3);
        towerBaseX = width / 2 - towerBaseWidth / 2;
        towerBlockWidth = towerBaseWidth;
        towerBlockX = 12;
        towerDirection = 1;
        message = "Pressione Espaço para empilhar o bloco em movimento.";
    }

    private void stopTower() {
        int left = Math.max(towerBaseX, towerBlockX);
        int right = Math.min(towerBaseX + towerBaseWidth, towerBlockX + towerBlockWidth);
        int overlap = right - left;
        if (overlap < 10) {
            gameOver = true;
            message = "A torre caiu! Altura: " + score + " blocos.";
            return;
        }
        towerBaseX = left;
        towerBaseWidth = overlap;
        towerBlockWidth = overlap;
        score++;
        if (score >= 8) {
            gameOver = true;
            message = "Torre perfeita! Você empilhou oito blocos.";
        } else {
            towerBlockX = towerDirection > 0 ? 12 : width - towerBlockWidth - 12;
            message = "Boa! Bloco " + (score + 1) + "/8 — continue.";
        }
    }

    private void spawnBubble() {
        bubbleX = random.nextInt(Math.max(30, width - 30));
        bubbleY = random.nextInt(95, Math.max(96, height - 70));
        bubbleSize = random.nextInt(18, 31);
        bubbleDelay = 45;
    }

    private void tickGame() {
        ticks++;
        switch (game) {
            case MATH_DASH, FLAG_QUIZ, TAP_CHALLENGE, BUBBLE_POP, COLOR_CATCH, LASER_DODGE -> {
                if (timer > 0 && --timer == 0 && !gameOver) {
                    gameOver = true;
                    if (game == Game.TAP_CHALLENGE) message = "Tempo! Você tocou " + score + " vezes.";
                    else if (game == Game.BUBBLE_POP) message = "Tempo! Bolhas estouradas: " + score + ".";
                    else if (game == Game.COLOR_CATCH) message = "Fim de jogo. Frutas corretas: " + score + ".";
                    else if (game == Game.LASER_DODGE) message = "Sobreviveu! Pontos: " + score + ".";
                    else if (game == Game.MATH_DASH) message = "Tempo! Resultado: " + score + "/10 contas.";
                    else message = "Tempo! Resultado: " + score + "/8 países.";
                }
                if (!gameOver && game == Game.LASER_DODGE) tickLaserBoard();
                if (!gameOver && game == Game.COLOR_CATCH) tickFruitBoard();
                if (!gameOver && game == Game.BUBBLE_POP && bubbleDelay > 0 && --bubbleDelay == 0) spawnBubble();
            }
            case REACTION -> {
                if (phase == 0 && timer > 0 && --timer == 0) {
                    phase = 1;
                    reactionTicks = 0;
                    message = "CLIQUE AGORA!";
                } else if (phase == 1) reactionTicks++;
                else if (phase == 2 && timer > 0 && --timer == 0) startReactionRound();
            }
            case CUP_SHUFFLE -> {
                if (phase == 0 && timer > 0 && --timer == 0) { phase = 1; timer = 0; }
                else if (phase == 1 && ticks % 5 == 0) {
                    int a = random(3), b = random(3);
                    if (a == b) b = (b + 1) % 3;
                    swapCups(a, b);
                    if (++shuffleSteps >= 9) { phase = 2; message = "Onde está a estrela? Clique no copo."; }
                }
            }
            case TOWER_STACK -> {
                if (!gameOver) {
                    towerBlockX += towerDirection * 5;
                    if (towerBlockX + towerBlockWidth >= width - 12) { towerBlockX = width - 12 - towerBlockWidth; towerDirection = -1; }
                    if (towerBlockX <= 12) { towerBlockX = 12; towerDirection = 1; }
                }
            }
            case WORD_SEARCH -> { }
        }
    }

    private void tickLaserBoard() {
        if (gameOver || timer <= 0) return;
        if (ticks % 4 == 0) {
            for (int row = laserGrid.length - 1; row > 0; row--) System.arraycopy(laserGrid[row - 1], 0, laserGrid[row], 0, 5);
            Arrays.fill(laserGrid[0], 0);
            if (random(100) < 80) laserGrid[0][random(5)] = 1;
            if (laserGrid[8][playerLane] == 1) {
                gameOver = true;
                message = "O laser atingiu você! Pontuação: " + score + ".";
            } else score++;
        }
    }

    private void tickFruitBoard() {
        if (gameOver || timer <= 0) return;
        if (ticks % 18 == 0) fruitGrid[0][random(4)] = random(4);
        if (ticks % 7 == 0) {
            for (int row = fruitGrid.length - 1; row > 0; row--) System.arraycopy(fruitGrid[row - 1], 0, fruitGrid[row], 0, 4);
            Arrays.fill(fruitGrid[0], -1);
            int fruit = fruitGrid[8][playerLane];
            if (fruit >= 0) {
                if (fruit == targetColor) score++; else misses++;
                targetColor = random(4);
            }
        }
    }

    @Override
    public void tick() {
        if (initialized && !gameOver) tickGame();
    }

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
            case WORD_SEARCH -> renderWordSearch(c);
            case MATH_DASH -> renderMath(c);
            case REACTION -> renderReaction(c);
            case CUP_SHUFFLE -> renderCups(c);
            case LASER_DODGE -> renderLaser(c);
            case COLOR_CATCH -> renderFruit(c);
            case FLAG_QUIZ -> renderFlags(c);
            case TAP_CHALLENGE -> renderTap(c);
            case TOWER_STACK -> renderTower(c);
            case BUBBLE_POP -> renderBubble(c);
        }
        if (gameOver) ArcadeTheme.banner(c, textRenderer, "FIM DE JOGO",
                "R para jogar novamente", width / 2, 62 + (height - 102) / 2,
                Math.min(width - 20, 280), ArcadeTheme.GOLD);
        if (width >= 460) {
            ArcadeTheme.pill(c, textRenderer, "PONTOS  " + score, 16, height - 22, accent);
            ArcadeTheme.pillRight(c, textRenderer, "RODADA  " + round, width - 16, height - 22,
                    ArcadeTheme.GOLD);
        }
        super.render(c, mouseX, mouseY, delta);
    }

    private void renderWordSearch(DrawContext c) {
        int size = Math.min(46, Math.min((width - 40) / 7, (height - 125) / 5));
        int x0 = (width - size * 7) / 2, y0 = Math.max(72, height / 2 - size * 2);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("PALAVRA: " + targetWord + "   •   Encontrada: " + selectedLetters.size() + "/" + targetWord.length()), width / 2, y0 - 17, 0xFFFFD66D);
        for (int r = 0; r < 5; r++) for (int col = 0; col < 7; col++) {
            int index = r * 7 + col, x = x0 + col * size, y = y0 + r * size;
            boolean selected = selectedLetters.contains(index);
            c.fill(x, y, x + size - 3, y + size - 3, selected ? 0xFF4AA873 : 0xFF334B61);
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(String.valueOf(letters[r][col])), x + size / 2 - 1, y + size / 2 - 5, 0xFFFFFFFF);
        }
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Clique nas letras da esquerda para a direita."), width / 2, y0 + 5 * size + 4, 0xFFBBD0E3);
    }

    private void renderMath(DrawContext c) {
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(numberA + " + " + numberB + " = ?"), width / 2, height / 2 - 70, 0xFFFFD96A);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Acertos: " + score + "/10  •  Tempo: " + timer / 20 + "s"), width / 2, height / 2 - 48, 0xFFFFFFFF);
        drawAnswerButtons(c, choices, height / 2 - 5);
    }

    private void drawAnswerButtons(DrawContext c, int[] values, int top) {
        int bw = Math.min(112, (width - 40) / 2), bh = 42, gap = 10;
        int x0 = width / 2 - bw - gap / 2;
        for (int i = 0; i < 4; i++) {
            int x = x0 + (i % 2) * (bw + gap), y = top + (i / 2) * (bh + gap);
            c.fill(x, y, x + bw, y + bh, 0xFF386487);
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(String.valueOf(values[i])), x + bw / 2, y + 15, 0xFFFFFFFF);
        }
    }

    private void renderReaction(DrawContext c) {
        int color = phase == 1 ? 0xFF45C779 : phase == 0 ? 0xFFAA4149 : 0xFF596B80;
        int size = Math.min(190, width / 2);
        c.fill(width / 2 - size / 2, height / 2 - 42, width / 2 + size / 2, height / 2 + 42, color);
        String label = phase == 1 ? "CLIQUE!" : phase == 0 ? "AGUARDE" : "...";
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(label), width / 2, height / 2 - 4, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Rodada " + Math.min(5, round + 1) + "/5  •  Acertos " + score + "/5"), width / 2, height / 2 + 58, 0xFFFFFFFF);
    }

    private void renderCups(DrawContext c) {
        int cupW = Math.min(74, width / 5), gap = Math.min(27, width / 12), start = width / 2 - (cupW * 3 + gap * 2) / 2;
        for (int i = 0; i < 3; i++) {
            int x = start + i * (cupW + gap), y = height / 2 - 16;
            c.fill(x, y, x + cupW, y + 68, 0xFF344E62);
            c.fill(x + 8, y + 7, x + cupW - 8, y + 45, 0xFFDFAB67);
            c.fill(x + 2, y + 42, x + cupW - 2, y + 51, 0xFFB77D45);
            if (phase == 0 && cups[i] == 1) c.drawCenteredTextWithShadow(textRenderer, Text.literal("★"), x + cupW / 2, y + 20, 0xFFFFEF76);
            if (phase == 2) c.drawCenteredTextWithShadow(textRenderer, Text.literal("" + (i + 1)), x + cupW / 2, y + 77, 0xFFFFFFFF);
        }
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Acertos: " + score + "/5"), width / 2, height / 2 + 102, 0xFFFFFFFF);
    }

    private void renderLaser(DrawContext c) {
        int cell = Math.min(42, Math.min((width - 50) / 5, (height - 125) / 9));
        int x0 = width / 2 - cell * 5 / 2, y0 = height / 2 - cell * 9 / 2;
        for (int r = 0; r < 9; r++) for (int col = 0; col < 5; col++) {
            int x = x0 + col * cell, y = y0 + r * cell;
            c.fill(x, y, x + cell - 2, y + cell - 2, laserGrid[r][col] == 1 ? 0xFFDD4965 : 0xFF26394C);
            if (laserGrid[r][col] == 1) c.fill(x + 4, y + cell / 2 - 2, x + cell - 6, y + cell / 2 + 3, 0xFFFFD7E3);
        }
        c.fill(x0 + playerLane * cell + 5, y0 + 8 * cell + 5, x0 + (playerLane + 1) * cell - 7, y0 + 9 * cell - 7, 0xFF48C9E2);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("← / → para mover  •  Pontos: " + score), width / 2, y0 + cell * 9 + 4, 0xFFFFFFFF);
    }

    private void renderFruit(DrawContext c) {
        int cell = Math.min(60, Math.min((width - 50) / 4, (height - 130) / 9));
        int x0 = width / 2 - cell * 2, y0 = height / 2 - cell * 9 / 2;
        for (int r = 0; r < 9; r++) for (int col = 0; col < 4; col++) {
            int x = x0 + col * cell, y = y0 + r * cell;
            c.fill(x, y, x + cell - 3, y + cell - 3, 0xFF2A3A4A);
            if (fruitGrid[r][col] >= 0) disc(c, x + cell / 2, y + cell / 2, Math.max(5, cell / 4), COLOR_VALUES[fruitGrid[r][col]]);
        }
        c.fill(x0 + playerLane * cell + 5, y0 + 8 * cell + 7, x0 + (playerLane + 1) * cell - 8, y0 + 9 * cell - 7, 0xFFECF2F6);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Pegue: " + COLORS[targetColor] + "   •   Pontos: " + score + "   •   Erros: " + misses), width / 2, y0 + cell * 9 + 5, COLOR_VALUES[targetColor]);
    }

    private void renderFlags(DrawContext c) {
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("PAÍS: " + flagChoices[correctChoice].name.toUpperCase(java.util.Locale.ROOT)), width / 2, height / 2 - 90, 0xFFFFD86C);
        int bw = Math.min(145, (width - 48) / 2), bh = 66, gap = 12;
        int x0 = width / 2 - bw - gap / 2, y0 = height / 2 - 55;
        for (int i = 0; i < 4; i++) {
            int x = x0 + (i % 2) * (bw + gap), y = y0 + (i / 2) * (bh + gap);
            c.fill(x, y, x + bw, y + bh, 0xFF35516A);
            drawFlag(c, flagChoices[i], x + 12, y + 12, Math.min(52, bw / 2), 34);
            c.drawTextWithShadow(textRenderer, Text.literal(flagChoices[i].name), x + 68, y + 27, 0xFFFFFFFF);
        }
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Pontos: " + score + "/8  •  " + timer / 20 + "s"), width / 2, y0 + bh * 2 + gap + 4, 0xFFFFFFFF);
    }

    private void drawFlag(DrawContext c, Flag flag, int x, int y, int w, int h) {
        if (flag.name.equals("Japão")) {
            c.fill(x, y, x + w, y + h, 0xFFF7F7F3);
            disc(c, x + w / 2, y + h / 2, Math.max(5, h / 4), 0xFFE54853);
        } else {
            c.fill(x, y, x + w, y + h / 3, flag.first);
            c.fill(x, y + h / 3, x + w, y + 2 * h / 3, flag.second);
            c.fill(x, y + 2 * h / 3, x + w, y + h, flag.third);
        }
        c.fill(x, y, x + w, y + h, 0x22000000);
    }

    private void renderTap(DrawContext c) {
        int bw = Math.min(240, width - 50), bh = 96;
        c.fill(width / 2 - bw / 2, height / 2 - bh / 2, width / 2 + bw / 2, height / 2 + bh / 2, 0xFF37A86B);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("TOQUE AQUI!"), width / 2, height / 2 - 4, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Cliques: " + score + "  •  Tempo: " + timer / 20 + "s"), width / 2, height / 2 + 72, 0xFFFFFFFF);
    }

    private void renderTower(DrawContext c) {
        int ground = height - 74;
        c.fill(20, ground, width - 20, ground + 6, 0xFF846E56);
        for (int i = 0; i < score; i++) {
            int y = ground - (i + 1) * 22;
            c.fill(towerBaseX, y, towerBaseX + towerBaseWidth, y + 20, i % 2 == 0 ? 0xFF42A6BE : 0xFF6979D4);
        }
        if (!gameOver || score < 8) {
            int y = ground - (score + 1) * 22;
            c.fill(towerBlockX, y, towerBlockX + towerBlockWidth, y + 20, 0xFFFFC74F);
        }
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Altura: " + score + "/8  •  Espaço para parar"), width / 2, 58, 0xFFFFFFFF);
    }

    private void renderBubble(DrawContext c) {
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Bolhas: " + score + "   •   Tempo: " + timer / 20 + "s"), width / 2, 60, 0xFFFFFFFF);
        disc(c, bubbleX, bubbleY, bubbleSize, 0xAA52CBEA);
        disc(c, bubbleX - bubbleSize / 3, bubbleY - bubbleSize / 3, Math.max(3, bubbleSize / 4), 0xAAE8FBFF);
        c.fill(bubbleX - bubbleSize - 1, bubbleY - bubbleSize - 1, bubbleX + bubbleSize + 1, bubbleY - bubbleSize + 2, 0xFFB8F4FF);
    }

    private void disc(DrawContext c, int x, int y, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            c.fill(x - dx, y + dy, x + dx + 1, y + dy + 1, color);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (super.mouseClicked(x, y, button)) return true;
        if (button != 0 || gameOver) return false;
        switch (game) {
            case WORD_SEARCH -> clickWordSearch(x, y);
            case MATH_DASH -> clickAnswer(x, y);
            case REACTION -> reactionClick();
            case CUP_SHUFFLE -> clickCup(x, y);
            case FLAG_QUIZ -> clickFlag(x, y);
            case TAP_CHALLENGE -> { if (inTapButton(x, y)) score++; }
            case BUBBLE_POP -> {
                if (bubbleDelay == 0 && Math.hypot(x - bubbleX, y - bubbleY) <= bubbleSize + 6) { score++; spawnBubble(); }
            }
            default -> { }
        }
        return true;
    }

    private void clickWordSearch(double x, double y) {
        int size = Math.min(46, Math.min((width - 40) / 7, (height - 125) / 5));
        int x0 = (width - size * 7) / 2, y0 = Math.max(72, height / 2 - size * 2);
        int col = (int) ((x - x0) / size), row = (int) ((y - y0) / size);
        if (x < x0 || y < y0 || row < 0 || row >= 5 || col < 0 || col >= 7) return;
        int index = row * 7 + col;
        if (selectedLetters.contains(index)) return;
        int expected = selectedLetters.size();
        if (letters[row][col] == targetWord.charAt(expected)) {
            selectedLetters.add(index);
            if (selectedLetters.size() == targetWord.length()) { score = 1; gameOver = true; message = "Palavra encontrada! " + targetWord + "."; }
        } else selectedLetters.clear();
    }

    private void clickAnswer(double x, double y) {
        int bw = Math.min(112, (width - 40) / 2), bh = 42, gap = 10;
        int x0 = width / 2 - bw - gap / 2, top = height / 2 - 5;
        for (int i = 0; i < 4; i++) {
            int bx = x0 + (i % 2) * (bw + gap), by = top + (i / 2) * (bh + gap);
            if (x >= bx && x <= bx + bw && y >= by && y <= by + bh) { answerMath(i); return; }
        }
    }

    private void clickCup(double x, double y) {
        int cupW = Math.min(74, width / 5), gap = Math.min(27, width / 12), start = width / 2 - (cupW * 3 + gap * 2) / 2;
        for (int i = 0; i < 3; i++) {
            int left = start + i * (cupW + gap);
            if (x >= left && x < left + cupW && y >= height / 2 - 20 && y <= height / 2 + 65) pickCup(i);
        }
    }

    private void clickFlag(double x, double y) {
        int bw = Math.min(145, (width - 48) / 2), bh = 66, gap = 12;
        int x0 = width / 2 - bw - gap / 2, y0 = height / 2 - 55;
        for (int i = 0; i < 4; i++) {
            int bx = x0 + (i % 2) * (bw + gap), by = y0 + (i / 2) * (bh + gap);
            if (x >= bx && x <= bx + bw && y >= by && y <= by + bh) { answerFlag(i); return; }
        }
    }

    private boolean inTapButton(double x, double y) {
        int bw = Math.min(240, width - 50), bh = 96;
        return x >= width / 2 - bw / 2 && x <= width / 2 + bw / 2 && y >= height / 2 - bh / 2 && y <= height / 2 + bh / 2;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) { resetGame(); return true; }
        if (gameOver) return super.keyPressed(keyCode, scanCode, modifiers);
        if (game == Game.LASER_DODGE) {
            if (keyCode == GLFW.GLFW_KEY_LEFT) playerLane = Math.max(0, playerLane - 1);
            else if (keyCode == GLFW.GLFW_KEY_RIGHT) playerLane = Math.min(4, playerLane + 1);
            else return super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (game == Game.COLOR_CATCH) {
            if (keyCode == GLFW.GLFW_KEY_LEFT) playerLane = Math.max(0, playerLane - 1);
            else if (keyCode == GLFW.GLFW_KEY_RIGHT) playerLane = Math.min(3, playerLane + 1);
            else return super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        if (game == Game.TOWER_STACK && keyCode == GLFW.GLFW_KEY_SPACE) { stopTower(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    private record Flag(String name, int first, int second, int third) { }
}
