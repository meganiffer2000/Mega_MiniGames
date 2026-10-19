package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Reversi: cerque as peças da CPU para virá-las e dominar o tabuleiro. */
public final class ReversiScreen extends Screen {
    private static final int SIDE = 8;
    private static final int EMPTY = 0;
    private static final int PLAYER = 1;
    private static final int CPU = 2;
    private static final int[][] DIRS = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

    private final int[][] board = new int[SIDE][SIDE];
    private int cell;
    private int left;
    private int top;
    private int turn = PLAYER;
    private int aiDelay;
    private int ticks;
    private int lastRow = -1;
    private int lastColumn = -1;
    private int lastTicks;
    private boolean gameOver;
    private boolean playerWon;

    public ReversiScreen() {
        super(Text.literal("Reversi"));
    }

    @Override
    protected void init() {
        cell = Math.max(24, Math.min(46, Math.min((width - 52) / SIDE, (height - 148) / SIDE)));
        left = (width - SIDE * cell) / 2;
        top = Math.max(80, (height - SIDE * cell - 104) / 2 + 26);
        int buttonY = Math.min(height - 24, top + SIDE * cell + 16);
        int buttonWidth = Math.min(112, Math.max(74, (width - 40) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Nova partida"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        newGame();
    }

    private void newGame() {
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                board[row][column] = EMPTY;
            }
        }
        board[3][3] = CPU;
        board[4][4] = CPU;
        board[3][4] = PLAYER;
        board[4][3] = PLAYER;
        turn = PLAYER;
        aiDelay = 0;
        lastRow = -1;
        lastColumn = -1;
        gameOver = false;
        playerWon = false;
    }

    @Override
    public void tick() {
        ticks++;
        if (lastTicks > 0) lastTicks--;
        if (gameOver) return;
        if (turn == CPU) {
            if (legalMoves(CPU).isEmpty()) {
                if (legalMoves(PLAYER).isEmpty()) finish();
                else turn = PLAYER;
                return;
            }
            aiDelay++;
            if (aiDelay >= 14) {
                aiDelay = 0;
                int[] move = chooseCpuMove();
                if (move != null) place(move[0], move[1], CPU);
                turn = PLAYER;
                if (legalMoves(PLAYER).isEmpty()) {
                    if (legalMoves(CPU).isEmpty()) finish();
                    else turn = CPU;
                }
            }
        }
    }

    private void finish() {
        gameOver = true;
        playerWon = count(PLAYER) > count(CPU);
    }

    private int count(int color) {
        int total = 0;
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                if (board[row][column] == color) total++;
            }
        }
        return total;
    }

    private List<int[]> legalMoves(int color) {
        List<int[]> moves = new ArrayList<>();
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                if (board[row][column] == EMPTY && flips(row, column, color).size() > 0) {
                    moves.add(new int[]{row, column});
                }
            }
        }
        return moves;
    }

    private List<int[]> flips(int row, int column, int color) {
        List<int[]> result = new ArrayList<>();
        if (row < 0 || column < 0 || row >= SIDE || column >= SIDE) return result;
        if (board[row][column] != EMPTY) return result;
        int rival = color == PLAYER ? CPU : PLAYER;
        for (int[] dir : DIRS) {
            List<int[]> line = new ArrayList<>();
            int currentRow = row + dir[0];
            int currentColumn = column + dir[1];
            while (currentRow >= 0 && currentColumn >= 0 && currentRow < SIDE && currentColumn < SIDE
                    && board[currentRow][currentColumn] == rival) {
                line.add(new int[]{currentRow, currentColumn});
                currentRow += dir[0];
                currentColumn += dir[1];
            }
            if (!line.isEmpty() && currentRow >= 0 && currentColumn >= 0 && currentRow < SIDE && currentColumn < SIDE
                    && board[currentRow][currentColumn] == color) {
                result.addAll(line);
            }
        }
        return result;
    }

    private boolean place(int row, int column, int color) {
        List<int[]> captured = flips(row, column, color);
        if (captured.isEmpty()) return false;
        board[row][column] = color;
        for (int[] tile : captured) {
            board[tile[0]][tile[1]] = color;
        }
        lastRow = row;
        lastColumn = column;
        lastTicks = 16;
        if (legalMoves(PLAYER).isEmpty() && legalMoves(CPU).isEmpty()) finish();
        return true;
    }

    private int[] chooseCpuMove() {
        List<int[]> moves = legalMoves(CPU);
        if (moves.isEmpty()) return null;
        int[] best = moves.get(0);
        int bestScore = Integer.MIN_VALUE;
        for (int[] move : moves) {
            int score = flips(move[0], move[1], CPU).size();
            boolean corner = (move[0] == 0 || move[0] == SIDE - 1) && (move[1] == 0 || move[1] == SIDE - 1);
            boolean edge = move[0] == 0 || move[1] == 0 || move[0] == SIDE - 1 || move[1] == SIDE - 1;
            score += corner ? 14 : edge ? 4 : 0;
            score += ThreadLocalRandom.current().nextInt(3);
            if (score > bestScore) {
                bestScore = score;
                best = move;
            }
        }
        return best;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!gameOver && turn == PLAYER && button == 0) {
            int column = (int) ((mouseX - left) / cell);
            int row = (int) ((mouseY - top) / cell);
            if (row >= 0 && column >= 0 && row < SIDE && column < SIDE && place(row, column, PLAYER)) {
                turn = CPU;
                aiDelay = 0;
                if (legalMoves(CPU).isEmpty()) {
                    if (legalMoves(PLAYER).isEmpty()) finish();
                    else turn = PLAYER;
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.LIME);
        String status = gameOver
                ? (playerWon ? "Você dominou o tabuleiro!" : count(PLAYER) == count(CPU) ? "Empate!" : "A CPU venceu")
                : turn == PLAYER ? "Sua vez  •  clique em uma casa destacada" : "A CPU está pensando...";
        ArcadeTheme.status(context, textRenderer, Text.literal(status), width / 2, 32,
                gameOver ? (playerWon ? ArcadeTheme.SUCCESS : ArcadeTheme.CORAL)
                        : turn == PLAYER ? ArcadeTheme.MUTED : ArcadeTheme.GOLD);

        int boardSize = SIDE * cell;
        ArcadeTheme.rounded(context, left - 8, top - 8, left + boardSize + 8, top + boardSize + 8, 14, 0x55000000);
        ArcadeTheme.rounded(context, left - 7, top - 7, left + boardSize + 7, top + boardSize + 7, 13, 0x6612C08A);
        context.fillGradient(left - 6, top - 6, left + boardSize + 6, top + boardSize + 6, 0xFF1B8A62, 0xFF0F5C42);
        ArcadeTheme.pill(context, textRenderer, "VOCÊ " + count(PLAYER), left, top - 24, ArcadeTheme.LIME);
        ArcadeTheme.pillRight(context, textRenderer, "CPU " + count(CPU), left + boardSize, top - 24, ArcadeTheme.CORAL);

        for (int row = 0; row <= SIDE; row++) {
            context.fill(left, top + row * cell, left + boardSize, top + row * cell + 1, 0x33000000);
            context.fill(left + row * cell, top, left + row * cell + 1, top + boardSize, 0x33000000);
        }
        for (int[] star : new int[][]{{2, 2}, {2, 6}, {6, 2}, {6, 6}}) {
            ArcadeTheme.disc(context, left + star[1] * cell + cell / 2, top + star[0] * cell + cell / 2,
                    Math.max(1, cell / 12), 0x55FFFFFF);
        }

        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int centerX = left + column * cell + cell / 2;
                int centerY = top + row * cell + cell / 2;
                if (board[row][column] != EMPTY) {
                    drawDisc(context, centerX, centerY, board[row][column] == PLAYER, cell);
                }
            }
        }
        if (!gameOver && turn == PLAYER) {
            int pulse = (int) (Math.sin(ticks * 0.18) * 2) + 2;
            for (int[] move : legalMoves(PLAYER)) {
                int centerX = left + move[1] * cell + cell / 2;
                int centerY = top + move[0] * cell + cell / 2;
                ArcadeTheme.glow(context, centerX, centerY, Math.max(4, cell / 5 + pulse), 0x66FFD66B);
                ArcadeTheme.disc(context, centerX, centerY, Math.max(2, cell / 9 + 1), 0xCCFFD66B);
            }
        }
        if (lastTicks > 0 && lastRow >= 0) {
            int centerX = left + lastColumn * cell + cell / 2;
            int centerY = top + lastRow * cell + cell / 2;
            ArcadeTheme.ring(context, centerX, centerY, cell / 2 - 2, 2,
                    ArcadeTheme.alpha(ArcadeTheme.GOLD, Math.min(220, lastTicks * 14)));
        }
        if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, playerWon ? "VOCÊ VENCEU!" : "FIM DE PARTIDA",
                    count(PLAYER) + " × " + count(CPU), width / 2, top + boardSize / 2,
                    Math.min(width - 24, boardSize + 30), playerWon ? ArcadeTheme.SUCCESS : ArcadeTheme.CORAL);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawDisc(DrawContext context, int centerX, int centerY, boolean player, int size) {
        int radius = Math.max(5, size / 2 - 3);
        ArcadeTheme.disc(context, centerX, centerY + 1, radius, 0x55000000);
        if (player) {
            ArcadeTheme.disc(context, centerX, centerY, radius, 0xFF0C1420);
            ArcadeTheme.disc(context, centerX - 1, centerY - 1, radius - 2, 0xFF2C3B52);
            ArcadeTheme.disc(context, centerX - radius / 3, centerY - radius / 3, Math.max(1, radius / 4),
                    0x88A9C2E0);
        } else {
            ArcadeTheme.disc(context, centerX, centerY, radius, 0xFFC8D3E0);
            ArcadeTheme.disc(context, centerX - 1, centerY - 1, radius - 2, 0xFFFFFFFF);
            ArcadeTheme.disc(context, centerX + radius / 4, centerY + radius / 4, Math.max(1, radius / 4),
                    0x66A9B8CC);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            newGame();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
