package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** 2048: deslize e una peças iguais para alcançar 2048. */
public final class Game2048Screen extends Screen {
    private static final int SIDE = 4;
    private static int bestScore;
    private final int[][] board = new int[SIDE][SIDE];
    private int cell = 64;
    private int left;
    private int top;
    private int score;
    private boolean gameOver;
    private boolean reached2048;
    private boolean initialized;
    private int spawnRow = -1;
    private int spawnColumn = -1;
    private int spawnTicks;
    private int[][] undoBoard;
    private int undoScore;

    public Game2048Screen() {
        super(Text.literal("2048"));
    }

    @Override
    protected void init() {
        cell = Math.max(34, Math.min(70, Math.min((width - 56) / SIDE, (height - 172) / SIDE)));
        left = (width - SIDE * cell) / 2;
        top = Math.max(82, (height - SIDE * cell - 112) / 2 + 18);
        int buttonY = Math.min(height - 26, top + SIDE * cell + 20);
        int buttonWidth = Math.min(118, Math.max(76, (width - 44) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Novo jogo (R)"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) {
            newGame();
        }
    }

    private void newGame() {
        for (int[] row : board) {
            java.util.Arrays.fill(row, 0);
        }
        score = 0;
        gameOver = false;
        reached2048 = false;
        spawnRow = -1;
        spawnTicks = 0;
        undoBoard = null;
        initialized = true;
        spawnTile();
        spawnTile();
    }

    private void spawnTile() {
        List<int[]> empty = new ArrayList<>();
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                if (board[row][column] == 0) {
                    empty.add(new int[]{row, column});
                }
            }
        }
        if (!empty.isEmpty()) {
            int[] cellPosition = empty.get(ThreadLocalRandom.current().nextInt(empty.size()));
            board[cellPosition[0]][cellPosition[1]] = ThreadLocalRandom.current().nextInt(10) == 0 ? 4 : 2;
            spawnRow = cellPosition[0];
            spawnColumn = cellPosition[1];
            spawnTicks = 10;
        }
    }

    private void move(int direction) {
        if (gameOver) {
            return;
        }
        int[][] snapshot = new int[SIDE][SIDE];
        for (int row = 0; row < SIDE; row++) {
            System.arraycopy(board[row], 0, snapshot[row], 0, SIDE);
        }
        int previousScore = score;
        boolean changed = false;
        for (int line = 0; line < SIDE; line++) {
            int[] values = new int[SIDE];
            for (int position = 0; position < SIDE; position++) {
                values[position] = getCell(line, position, direction);
            }
            int[] merged = new int[SIDE];
            int write = 0;
            for (int position = 0; position < SIDE; position++) {
                if (values[position] == 0) {
                    continue;
                }
                if (position + 1 < SIDE && values[position] == values[position + 1]) {
                    merged[write] = values[position] * 2;
                    score += merged[write];
                    if (merged[write] >= 2048) {
                        reached2048 = true;
                    }
                    position++;
                } else {
                    merged[write] = values[position];
                }
                write++;
            }
            for (int position = 0; position < SIDE; position++) {
                if (merged[position] != values[position]) {
                    changed = true;
                }
                setCell(line, position, direction, merged[position]);
            }
        }
        if (changed) {
            undoBoard = snapshot;
            undoScore = previousScore;
            bestScore = Math.max(bestScore, score);
            spawnTile();
            gameOver = !hasMoves();
        }
    }

    private void undo() {
        if (undoBoard == null || gameOver) return;
        for (int row = 0; row < SIDE; row++) {
            System.arraycopy(undoBoard[row], 0, board[row], 0, SIDE);
        }
        score = undoScore;
        undoBoard = null;
        gameOver = false;
        spawnRow = -1;
        spawnTicks = 0;
    }

    private int getCell(int line, int position, int direction) {
        return switch (direction) {
            case 0 -> board[line][position];
            case 1 -> board[line][SIDE - 1 - position];
            case 2 -> board[position][line];
            default -> board[SIDE - 1 - position][line];
        };
    }

    private void setCell(int line, int position, int direction, int value) {
        switch (direction) {
            case 0 -> board[line][position] = value;
            case 1 -> board[line][SIDE - 1 - position] = value;
            case 2 -> board[position][line] = value;
            default -> board[SIDE - 1 - position][line] = value;
        }
    }

    private boolean hasMoves() {
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                if (board[row][column] == 0) {
                    return true;
                }
                if (row + 1 < SIDE && board[row][column] == board[row + 1][column]) {
                    return true;
                }
                if (column + 1 < SIDE && board[row][column] == board[row][column + 1]) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        if (spawnTicks > 0) {
            spawnTicks--;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.GOLD);

        String scoreLabel = "PONTOS " + score;
        int scoreWidth = textRenderer.getWidth(scoreLabel) + 18;
        ArcadeTheme.pill(context, textRenderer, scoreLabel, left, 34, ArcadeTheme.GOLD);
        String bestLabel = "RECORDE " + bestScore;
        ArcadeTheme.pill(context, textRenderer, bestLabel, left + SIDE * cell - textRenderer.getWidth(bestLabel) - 18,
                34, ArcadeTheme.TEAL);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(reached2048 ? "2048 alcançado! Continue ou reinicie."
                        : "Setas ou WASD deslizam  •  Z desfaz a última jogada"),
                width / 2, 55, reached2048 ? ArcadeTheme.SUCCESS : ArcadeTheme.MUTED);

        int pad = 8;
        ArcadeTheme.rounded(context, left - pad + 2, top - pad + 4, left + SIDE * cell + pad + 2,
                top + SIDE * cell + pad + 4, 14, 0x55000000);
        ArcadeTheme.rounded(context, left - pad, top - pad, left + SIDE * cell + pad, top + SIDE * cell + pad,
                14, 0x66FFD66B);
        ArcadeTheme.rounded(context, left - pad + 1, top - pad + 1, left + SIDE * cell + pad - 1,
                top + SIDE * cell + pad - 1, 13, 0xF00B1626);

        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int value = board[row][column];
                int x = left + column * cell;
                int y = top + row * cell;
                int inset = 3;
                ArcadeTheme.rounded(context, x + inset, y + inset, x + cell - inset, y + cell - inset, 7,
                        value == 0 ? 0xFF16273A : ArcadeTheme.darken(tileColor(value), 28));
                if (value > 0) {
                    ArcadeTheme.rounded(context, x + inset + 1, y + inset + 1, x + cell - inset - 1,
                            y + cell - inset - 1, 6, tileColor(value));
                    ArcadeTheme.rounded(context, x + inset + 2, y + inset + 2, x + cell - inset - 2,
                            y + cell / 2, 6, 0x33FFFFFF);
                    context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(value)),
                            x + cell / 2, y + cell / 2 - 4, value <= 4 ? 0xFF5C5348 : 0xFFFFFFFF);
                } else {
                    ArcadeTheme.disc(context, x + cell / 2, y + cell / 2, Math.max(2, cell / 14), 0xFF1E3247);
                }
                if (spawnTicks > 0 && row == spawnRow && column == spawnColumn) {
                    ArcadeTheme.ring(context, x + cell / 2, y + cell / 2, cell / 2 - 5, 2,
                            ArcadeTheme.alpha(ArcadeTheme.GOLD, 0x66 + spawnTicks * 12));
                }
            }
        }

        if (gameOver) {
            int bannerWidth = Math.min(width - 40, 240);
            int cx = width / 2;
            int cy = top + SIDE * cell / 2;
            ArcadeTheme.rounded(context, cx - bannerWidth / 2, cy - 26, cx + bannerWidth / 2, cy + 26, 12, 0xEE0A1422);
            ArcadeTheme.rounded(context, cx - bannerWidth / 2, cy - 26, cx + bannerWidth / 2, cy + 26, 12, 0x88FF7373);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("SEM MOVIMENTOS!"), cx, cy - 13, ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Pressione R para recomeçar"), cx, cy + 3, ArcadeTheme.TEXT);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private int tileColor(int value) {
        return switch (value) {
            case 0 -> 0xFF16273A;
            case 2 -> 0xFFEDE4D8;
            case 4 -> 0xFFE9D9B9;
            case 8 -> 0xFFF1B67B;
            case 16 -> 0xFFF28F62;
            case 32 -> 0xFFEB6C54;
            case 64 -> 0xFFE84C36;
            case 128 -> 0xFFE6D05B;
            case 256 -> 0xFFE5C842;
            case 512 -> 0xFFE5BE2E;
            case 1024 -> 0xFFDDB523;
            default -> 0xFFD6A913;
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> move(0);
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> move(1);
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> move(2);
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> move(3);
            case GLFW.GLFW_KEY_Z -> undo();
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
