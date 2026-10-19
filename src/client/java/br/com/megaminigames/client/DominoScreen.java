package br.com.megaminigames.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Dominó: encaixe as peças nas pontas da mesa e esvazie a mão antes da CPU. */
public final class DominoScreen extends ArcadeScreen {
    private static final int HAND_SIZE = 7;
    private static final int PIP = 0xFF22334A;

    private final List<int[]> playerHand = new ArrayList<>();
    private final List<int[]> cpuHand = new ArrayList<>();
    private final Deque<int[]> boneyard = new ArrayDeque<>();
    private final Deque<int[]> board = new ArrayDeque<>();
    private int playerScore;
    private int cpuScore;
    private int turn;
    private int aiDelay;
    private int autoDrawDelay;
    private int ticks;
    private boolean roundOver;
    private boolean playerWonRound;
    private int roundWinner;
    private String message = "Clique em uma peça da sua mão";
    private int messageTicks;
    private int boardTileWidth;
    private int boardTileHeight;
    private int boardLeft;
    private int boardY;
    private int handTileWidth;
    private int handTileHeight;
    private int handLeft;
    private int handY;

    public DominoScreen() {
        super("Dominó");
    }

    @Override
    protected void init() {
        handTileWidth = Math.max(24, Math.min(38, (width - 40) / HAND_SIZE));
        handTileHeight = Math.max(14, handTileWidth / 2 + 3);
        handLeft = (width - handTileWidth * HAND_SIZE) / 2;
        handY = height - 68;
        boardTileHeight = Math.max(22, Math.min(34, handTileHeight + 10));
        boardY = 82;
        boardLeft = 16;
        addFooterButtons(height - 26, this::restartMatch);
        newGame();
    }

    private void newGame() {
        playerScore = 0;
        cpuScore = 0;
        roundOver = false;
        turn = 0;
        deal();
    }

    private void restartMatch() {
        playerScore = 0;
        cpuScore = 0;
        deal();
    }

    private void deal() {
        List<int[]> deck = new ArrayList<>();
        for (int a = 0; a <= 6; a++) {
            for (int b = a; b <= 6; b++) {
                deck.add(new int[]{a, b});
            }
        }
        Collections.shuffle(deck, ThreadLocalRandom.current());
        playerHand.clear();
        cpuHand.clear();
        boneyard.clear();
        board.clear();
        for (int index = 0; index < HAND_SIZE; index++) {
            playerHand.add(deck.remove(0));
            cpuHand.add(deck.remove(0));
        }
        boneyard.addAll(deck);
        roundOver = false;
        circle:
        for (int index = 0; index < playerHand.size(); index++) {
            if (playerHand.get(index)[0] == playerHand.get(index)[1]) {
                Collections.swap(playerHand, index, 0);
                break circle;
            }
        }
        turn = 0;
        aiDelay = 0;
        autoDrawDelay = 12;
        message = "Sua vez — clique em uma peça";
        messageTicks = 0;
    }

    private int leftEnd() {
        return board.isEmpty() ? -1 : board.getFirst()[0];
    }

    private int rightEnd() {
        return board.isEmpty() ? -1 : board.getLast()[1];
    }

    private boolean playable(int[] tile) {
        if (board.isEmpty()) return true;
        return tile[0] == leftEnd() || tile[1] == leftEnd()
                || tile[0] == rightEnd() || tile[1] == rightEnd();
    }

    private boolean hasPlayable(List<int[]> hand) {
        for (int[] tile : hand) {
            if (playable(tile)) return true;
        }
        return false;
    }

    private boolean playTile(List<int[]> hand, int[] tile) {
        if (!playable(tile)) return false;
        hand.remove(tile);
        if (board.isEmpty()) {
            board.addLast(new int[]{tile[0], tile[1]});
        } else {
            boolean matchesLeft = tile[0] == leftEnd() || tile[1] == leftEnd();
            boolean matchesRight = tile[0] == rightEnd() || tile[1] == rightEnd();
            if (matchesRight || !matchesLeft) {
                int leftValue = tile[0] == rightEnd() ? tile[0] : tile[1];
                int rightValue = tile[0] == rightEnd() ? tile[1] : tile[0];
                board.addLast(new int[]{leftValue, rightValue});
            } else {
                int rightValue = tile[0] == leftEnd() ? tile[0] : tile[1];
                int leftValue = tile[0] == leftEnd() ? tile[1] : tile[0];
                board.addFirst(new int[]{leftValue, rightValue});
            }
        }
        checkRoundEnd();
        return true;
    }

    private int pipSum(List<int[]> hand) {
        int total = 0;
        for (int[] tile : hand) total += tile[0] + tile[1];
        return total;
    }

    private void checkRoundEnd() {
        if (roundOver) return;
        if (playerHand.isEmpty()) {
            endRound(0, "Você bateu! +" + pipSum(cpuHand) + " pontos");
        } else if (cpuHand.isEmpty()) {
            endRound(1, "A CPU bateu. +" + pipSum(playerHand) + " pontos para ela");
        } else if (boneyard.isEmpty() && !hasPlayable(playerHand) && !hasPlayable(cpuHand)) {
            int player = pipSum(playerHand);
            int cpu = pipSum(cpuHand);
            if (player < cpu) {
                endRound(0, "Jogo fechado! Você tinha menos pontos");
            } else if (cpu < player) {
                endRound(1, "Jogo fechado! A CPU tinha menos pontos");
            } else {
                endRound(2, "Jogo fechado e empatado");
            }
        }
    }

    private void endRound(int winner, String text) {
        roundOver = true;
        roundWinner = winner;
        playerWonRound = winner == 0;
        if (winner == 0) {
            playerScore += pipSum(cpuHand);
        } else if (winner == 1) {
            cpuScore += pipSum(playerHand);
        }
        message = text;
        messageTicks = 0;
    }

    private void passTurn() {
        turn = turn == 0 ? 1 : 0;
        aiDelay = 0;
        autoDrawDelay = 12;
        if (turn == 0) {
            message = "Sua vez — clique em uma peça";
            messageTicks = 0;
        }
    }

    @Override
    public void tick() {
        ticks++;
        if (messageTicks > 0) messageTicks--;
        if (roundOver) return;
        if (turn == 0) {
            if (hasPlayable(playerHand)) return;
            if (autoDrawDelay > 0) {
                autoDrawDelay--;
                return;
            }
            if (!boneyard.isEmpty()) {
                playerHand.add(boneyard.removeFirst());
                autoDrawDelay = 10;
                message = "Você comprou uma peça";
                messageTicks = 30;
                if (!hasPlayable(playerHand) && boneyard.isEmpty()) {
                    message = "Sem jogada possível — vez da CPU";
                    messageTicks = 40;
                    passTurn();
                    checkRoundEnd();
                }
            } else {
                message = "Sem jogada possível — vez da CPU";
                messageTicks = 40;
                passTurn();
                checkRoundEnd();
            }
            return;
        }
        if (aiDelay < 20) {
            aiDelay++;
            return;
        }
        cpuTurn();
    }

    private void cpuTurn() {
        aiDelay = 0;
        int[] chosen = null;
        for (int[] tile : cpuHand) {
            if (!playable(tile)) continue;
            if (chosen == null || tile[0] + tile[1] > chosen[0] + chosen[1]) {
                chosen = tile;
            }
        }
        if (chosen != null) {
            playTile(cpuHand, chosen);
            if (!roundOver) {
                turn = 0;
                autoDrawDelay = 12;
                message = "A CPU jogou. Sua vez!";
                messageTicks = 30;
            }
            return;
        }
        if (!boneyard.isEmpty()) {
            cpuHand.add(boneyard.removeFirst());
            aiDelay = 8;
            return;
        }
        message = "A CPU passou — sua vez";
        messageTicks = 40;
        passTurn();
        checkRoundEnd();
    }

    private int tileAt(double mouseX, double mouseY) {
        if (mouseY < handY - 4 || mouseY > handY + handTileHeight + 4) return -1;
        for (int index = 0; index < playerHand.size(); index++) {
            double x = handLeft + index * (handTileWidth + 4);
            if (mouseX >= x && mouseX <= x + handTileWidth) return index;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !roundOver && turn == 0) {
            int index = tileAt(mouseX, mouseY);
            if (index >= 0 && index < playerHand.size()) {
                int[] tile = playerHand.get(index);
                if (playable(tile)) {
                    playTile(playerHand, tile);
                    if (!roundOver) {
                        turn = 1;
                        aiDelay = 0;
                        message = "Boa! A CPU está pensando...";
                        messageTicks = 20;
                    }
                } else {
                    message = "Essa peça não encaixa em nenhuma ponta";
                    messageTicks = 45;
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            restartMatch();
            return true;
        }
        if (roundOver && (keyCode == 32 || keyCode == 257 || keyCode == 335)) {
            deal();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.LIME);
        ArcadeTheme.status(context, textRenderer, Text.literal(message), width / 2, 32,
                roundOver ? (playerWonRound ? ArcadeTheme.SUCCESS : ArcadeTheme.GOLD) : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "VOCÊ " + playerScore, 12, 47, ArcadeTheme.LIME);
        ArcadeTheme.pillRight(context, textRenderer, "CPU " + cpuScore, width - 12, 47, ArcadeTheme.CORAL);

        int tableWidth = width - 24;
        int tableTop = 66;
        int tableBottom = handY - 14;
        ArcadeTheme.rounded(context, 12, tableTop, width - 12, tableBottom, 12, 0x55000000);
        ArcadeTheme.rounded(context, 11, tableTop - 1, width - 11, tableBottom + 1, 12, 0x6641C98B);
        ArcadeTheme.rounded(context, 13, tableTop + 1, width - 13, tableBottom - 1, 11, 0xF00C2C24);
        context.fillGradient(14, tableTop + 2, width - 14, tableBottom - 2, 0xFF124A3B, 0xFF0A2C25);

        int count = Math.max(1, board.size());
        boardTileWidth = Math.max(10, Math.min(34, (tableWidth - 20) / count));
        String pile = "MONTE " + boneyard.size();
        ArcadeTheme.pill(context, textRenderer, pile, (width - textRenderer.getWidth(pile) - 18) / 2,
                tableTop + 4, ArcadeTheme.TEAL);
        if (board.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("A mesa está vazia — comece por qualquer peça"),
                    width / 2, boardY + boardTileHeight / 2 - 4, 0x88FFFFFF);
        } else {
            int totalWidth = board.size() * (boardTileWidth + 2) - 2;
            int startX = (width - totalWidth) / 2;
            int tileIndex = 0;
            for (int[] tile : board) {
                int x = startX + tileIndex * (boardTileWidth + 2);
                drawTile(context, x, boardY, boardTileWidth, boardTileHeight, tile[0], tile[1], false);
                tileIndex++;
            }
            int leftX = startX;
            int rightX = startX + totalWidth - boardTileWidth;
            ArcadeTheme.disc(context, leftX - 6, boardY + boardTileHeight / 2, 2, ArcadeTheme.GOLD);
            ArcadeTheme.disc(context, rightX + boardTileWidth + 6, boardY + boardTileHeight / 2, 2,
                    ArcadeTheme.GOLD);
        }

        int handWidth = Math.min(width - 24, playerHand.size() * (handTileWidth + 4) - 4);
        handLeft = (width - handWidth) / 2;
        int handCenterY = handY + handTileHeight / 2;
        ArcadeTheme.pill(context, textRenderer, "SUA MÃO", 14, handY - 14, ArcadeTheme.GOLD);
        ArcadeTheme.pillRight(context, textRenderer, "CPU: " + cpuHand.size() + " peça(s)", width - 14,
                handY - 14, ArcadeTheme.CORAL);
        for (int index = 0; index < playerHand.size(); index++) {
            int[] tile = playerHand.get(index);
            int x = handLeft + index * (handTileWidth + 4);
            boolean canPlay = !roundOver && turn == 0 && playable(tile);
            boolean hovered = mouseX >= x && mouseX <= x + handTileWidth && mouseY >= handY
                    && mouseY <= handY + handTileHeight;
            if (canPlay) {
                int glow = 0x33FFFFFF + (hovered ? 0x22 : 0);
                ArcadeTheme.glow(context, x + handTileWidth / 2, handCenterY, handTileWidth / 2 + 3, glow);
            }
            if (hovered && canPlay) {
                drawTile(context, x, handY - 4, handTileWidth, handTileHeight, tile[0], tile[1], true);
            } else {
                drawTile(context, x, handY, handTileWidth, handTileHeight, tile[0], tile[1], !canPlay);
            }
        }

        if (roundOver) {
            ArcadeTheme.banner(context, textRenderer,
                    roundWinner == 0 ? "VOCÊ GANHOU A RODADA" : roundWinner == 1 ? "CPU GANHOU A RODADA" : "EMPATE",
                    "Enter para a próxima rodada", width / 2, (tableTop + tableBottom) / 2,
                    Math.min(width - 20, 300),
                    roundWinner == 0 ? ArcadeTheme.SUCCESS : roundWinner == 2 ? ArcadeTheme.GOLD : ArcadeTheme.CORAL);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawTile(DrawContext context, int x, int y, int width, int height, int left, int right,
                          boolean dimmed) {
        int ivory = dimmed ? 0xFF9AA6B4 : 0xFFF3F0E4;
        int shadow = dimmed ? 0xFF54606E : 0xFFB8B3A2;
        ArcadeTheme.rounded(context, x + 1, y + 2, x + width + 1, y + height + 2, 4, 0x55000000);
        ArcadeTheme.rounded(context, x, y, x + width, y + height, 4, shadow);
        ArcadeTheme.rounded(context, x, y, x + width, y + height - 1, 4, ivory);
        context.fillGradient(x + 1, y + 1, x + width - 1, y + Math.max(2, height / 3), 0x55FFFFFF, 0x00FFFFFF);
        int divider = x + width / 2;
        context.fill(divider, y + 2, divider + 1, y + height - 2, dimmed ? 0xFF6C7784 : 0xFF9E9785);
        if (width >= 18) {
            int radius = Math.max(1, height / 12);
            drawPips(context, x + 1, y, divider - x - 2, height, left, radius, dimmed ? 0xFF59636E : PIP);
            drawPips(context, divider + 2, y, x + width - divider - 3, height, right, radius,
                    dimmed ? 0xFF59636E : PIP);
        } else {
            ArcadeTheme.disc(context, divider - 3, y + height / 2, 1, 0xFF59636E);
            ArcadeTheme.disc(context, divider + 3, y + height / 2, 1, 0xFF59636E);
        }
    }

    private void drawPips(DrawContext context, int x, int y, int width, int height, int value, int radius,
                          int color) {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int dx = Math.max(2, width / 4);
        int dy = Math.max(2, height / 4);
        switch (value) {
            case 1 -> ArcadeTheme.disc(context, centerX, centerY, radius, color);
            case 2 -> {
                ArcadeTheme.disc(context, centerX - dx, centerY - dy, radius, color);
                ArcadeTheme.disc(context, centerX + dx, centerY + dy, radius, color);
            }
            case 3 -> {
                ArcadeTheme.disc(context, centerX - dx, centerY - dy, radius, color);
                ArcadeTheme.disc(context, centerX, centerY, radius, color);
                ArcadeTheme.disc(context, centerX + dx, centerY + dy, radius, color);
            }
            case 4 -> {
                ArcadeTheme.disc(context, centerX - dx, centerY - dy, radius, color);
                ArcadeTheme.disc(context, centerX + dx, centerY - dy, radius, color);
                ArcadeTheme.disc(context, centerX - dx, centerY + dy, radius, color);
                ArcadeTheme.disc(context, centerX + dx, centerY + dy, radius, color);
            }
            case 5 -> {
                ArcadeTheme.disc(context, centerX - dx, centerY - dy, radius, color);
                ArcadeTheme.disc(context, centerX + dx, centerY - dy, radius, color);
                ArcadeTheme.disc(context, centerX, centerY, radius, color);
                ArcadeTheme.disc(context, centerX - dx, centerY + dy, radius, color);
                ArcadeTheme.disc(context, centerX + dx, centerY + dy, radius, color);
            }
            case 6 -> {
                ArcadeTheme.disc(context, centerX - dx, centerY - dy, radius, color);
                ArcadeTheme.disc(context, centerX + dx, centerY - dy, radius, color);
                ArcadeTheme.disc(context, centerX - dx, centerY, radius, color);
                ArcadeTheme.disc(context, centerX + dx, centerY, radius, color);
                ArcadeTheme.disc(context, centerX - dx, centerY + dy, radius, color);
                ArcadeTheme.disc(context, centerX + dx, centerY + dy, radius, color);
            }
            default -> {
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
