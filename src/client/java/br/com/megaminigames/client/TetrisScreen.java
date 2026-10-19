package br.com.megaminigames.client;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Tetris clássico com sete peças, rotação, linhas e pontuação. */
public final class TetrisScreen extends Screen {
    private static final int COLUMNS = 10;
    private static final int ROWS = 20;
    private static final int MIN_CELL_SIZE = 6;
    private static final int MAX_CELL_SIZE = 22;
    private static final int[][][] SHAPES = {
            {{0, 1}, {1, 1}, {2, 1}, {3, 1}},
            {{1, 0}, {2, 0}, {1, 1}, {2, 1}},
            {{1, 0}, {0, 1}, {1, 1}, {2, 1}},
            {{1, 0}, {2, 0}, {0, 1}, {1, 1}},
            {{0, 0}, {1, 0}, {1, 1}, {2, 1}},
            {{0, 0}, {0, 1}, {1, 1}, {2, 1}},
            {{2, 0}, {0, 1}, {1, 1}, {2, 1}}
    };
    private static final int[] COLORS = {
            0xFF42D9E8, 0xFFF2D64B, 0xFFAA65E8, 0xFF62D36F,
            0xFFE84F61, 0xFF4E79E8, 0xFFE99C43
    };

    private final int[][] board = new int[ROWS][COLUMNS];
    private int cellSize;
    private int boardLeft;
    private int boardTop;
    private int panelLeft;
    private int panelWidth;
    private int piece;
    private int nextPiece;
    private int rotation;
    private int pieceX;
    private int pieceY;
    private int fallTicks;
    private int score;
    private int lines;
    private int flashTicks;
    private boolean gameOver;
    private boolean initialized;

    public TetrisScreen() {
        super(Text.literal("Tetris"));
    }

    @Override
    protected void init() {
        int reserved = width > 340 ? 104 : 44;
        int heightLimited = Math.max(MIN_CELL_SIZE, (height - 132) / ROWS);
        int widthLimited = Math.max(MIN_CELL_SIZE, (width - reserved) / COLUMNS);
        cellSize = Math.max(MIN_CELL_SIZE, Math.min(MAX_CELL_SIZE, Math.min(heightLimited, widthLimited)));
        int boardWidth = COLUMNS * cellSize;
        boolean sidePanel = width - boardWidth - 24 >= 62;
        int blockWidth = boardWidth + (sidePanel ? 70 : 0);
        boardLeft = (width - blockWidth) / 2;
        boardTop = 56 + Math.max(0, (height - 30 - ROWS * cellSize - 56) / 2);
        panelLeft = boardLeft + boardWidth + 10;
        panelWidth = Math.min(60, width - panelLeft - 10);

        int buttonY = height - 22;
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Reiniciar (R)"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) {
            newGame();
        }
    }

    private void newGame() {
        for (int[] row : board) {
            Arrays.fill(row, 0);
        }
        score = 0;
        lines = 0;
        fallTicks = 0;
        flashTicks = 0;
        gameOver = false;
        initialized = true;
        nextPiece = ThreadLocalRandom.current().nextInt(SHAPES.length);
        spawnPiece();
    }

    private void spawnPiece() {
        piece = nextPiece;
        nextPiece = ThreadLocalRandom.current().nextInt(SHAPES.length);
        rotation = 0;
        pieceX = 3;
        pieceY = 0;
        if (collides(pieceX, pieceY, rotation)) {
            gameOver = true;
        }
    }

    private int rotatedX(int x, int y, int turns) {
        for (int i = 0; i < turns; i++) {
            int oldX = x;
            x = 3 - y;
            y = oldX;
        }
        return x;
    }

    private int rotatedY(int x, int y, int turns) {
        for (int i = 0; i < turns; i++) {
            int oldX = x;
            x = 3 - y;
            y = oldX;
        }
        return y;
    }

    private boolean collides(int x, int y, int turns) {
        for (int[] block : SHAPES[piece]) {
            int blockX = x + rotatedX(block[0], block[1], turns);
            int blockY = y + rotatedY(block[0], block[1], turns);
            if (blockX < 0 || blockX >= COLUMNS || blockY < 0 || blockY >= ROWS
                    || board[blockY][blockX] != 0) {
                return true;
            }
        }
        return false;
    }

    private void move(int dx, int dy) {
        if (!collides(pieceX + dx, pieceY + dy, rotation)) {
            pieceX += dx;
            pieceY += dy;
        } else if (dy > 0) {
            lockPiece();
        }
    }

    private void rotate() {
        int nextRotation = (rotation + 1) % 4;
        if (!collides(pieceX, pieceY, nextRotation)) {
            rotation = nextRotation;
        } else if (!collides(pieceX - 1, pieceY, nextRotation)) {
            pieceX--;
            rotation = nextRotation;
        } else if (!collides(pieceX + 1, pieceY, nextRotation)) {
            pieceX++;
            rotation = nextRotation;
        }
    }

    private void lockPiece() {
        for (int[] block : SHAPES[piece]) {
            int x = pieceX + rotatedX(block[0], block[1], rotation);
            int y = pieceY + rotatedY(block[0], block[1], rotation);
            if (y >= 0 && y < ROWS && x >= 0 && x < COLUMNS) {
                board[y][x] = piece + 1;
            }
        }
        clearLines();
        spawnPiece();
    }

    private void clearLines() {
        int cleared = 0;
        for (int row = ROWS - 1; row >= 0; row--) {
            boolean full = true;
            for (int value : board[row]) {
                if (value == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                cleared++;
                for (int moveRow = row; moveRow > 0; moveRow--) {
                    System.arraycopy(board[moveRow - 1], 0, board[moveRow], 0, COLUMNS);
                }
                Arrays.fill(board[0], 0);
                row++;
            }
        }
        if (cleared > 0) {
            flashTicks = 8;
            lines += cleared;
        }
        score += switch (cleared) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            case 4 -> 800;
            default -> 0;
        };
    }

    @Override
    public void tick() {
        if (flashTicks > 0) {
            flashTicks--;
        }
        if (gameOver) {
            return;
        }
        if (++fallTicks >= Math.max(2, 10 - score / 500)) {
            fallTicks = 0;
            move(0, 1);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.PURPLE);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? "Fim de jogo — pressione R" : "← → mover  •  ↑ girar  •  ↓ descer"),
                width / 2, 32, gameOver ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        int right = boardLeft + COLUMNS * cellSize;
        int bottom = boardTop + ROWS * cellSize;
        ArcadeTheme.pill(context, textRenderer, "PONTOS  " + score, boardLeft, boardTop - 22, ArcadeTheme.PURPLE);
        ArcadeTheme.pillRight(context, textRenderer, "LINHAS  " + lines, right, boardTop - 22, ArcadeTheme.LIME);

        ArcadeTheme.rounded(context, boardLeft - 5, boardTop - 5, right + 5, bottom + 5, 8,
                ArcadeTheme.alpha(ArcadeTheme.PURPLE, 110));
        ArcadeTheme.rounded(context, boardLeft - 4, boardTop - 4, right + 4, bottom + 4, 7, ArcadeTheme.PANEL_DEEP);
        context.fill(boardLeft, boardTop, right, bottom, 0xFF070B14);

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                if (cellSize >= 9) {
                    int color = (x + y) % 2 == 0 ? 0x08FFFFFF : 0x04FFFFFF;
                    context.fill(boardLeft + x * cellSize, boardTop + y * cellSize,
                            boardLeft + (x + 1) * cellSize, boardTop + (y + 1) * cellSize, color);
                }
                if (board[y][x] != 0) {
                    drawBlock(context, boardLeft, boardTop, x, y, COLORS[board[y][x] - 1], 255);
                }
            }
        }

        if (!gameOver) {
            int ghostY = pieceY;
            while (!collides(pieceX, ghostY + 1, rotation)) {
                ghostY++;
            }
            for (int[] block : SHAPES[piece]) {
                int x = pieceX + rotatedX(block[0], block[1], rotation);
                int y = ghostY + rotatedY(block[0], block[1], rotation);
                if (y >= 0 && ghostY > pieceY) {
                    int blockX = boardLeft + x * cellSize;
                    int blockY = boardTop + y * cellSize;
                    drawBlock(context, boardLeft, boardTop, x, y, COLORS[piece], 60);
                    if (cellSize >= 10) {
                        ArcadeTheme.rounded(context, blockX + 1, blockY + 1, blockX + cellSize - 1,
                                blockY + cellSize - 1, 2, 0x22FFFFFF);
                    }
                }
            }
            for (int[] block : SHAPES[piece]) {
                int x = pieceX + rotatedX(block[0], block[1], rotation);
                int y = pieceY + rotatedY(block[0], block[1], rotation);
                if (y >= 0) {
                    drawBlock(context, boardLeft, boardTop, x, y, COLORS[piece], 255);
                }
            }
        }

        if (flashTicks > 0) {
            context.fill(boardLeft, boardTop, right, bottom, ArcadeTheme.alpha(ArcadeTheme.LIME, 10 + flashTicks * 4));
        }

        if (panelWidth >= 40) {
            ArcadeTheme.panelAccent(context, panelLeft, boardTop - 5, panelLeft + panelWidth,
                    boardTop + 78, ArcadeTheme.PURPLE, 7);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("PRÓXIMA"),
                    panelLeft + panelWidth / 2, boardTop + 4, ArcadeTheme.MUTED);
            int previewCell = Math.max(6, Math.min(12, (panelWidth - 16) / 4));
            int previewLeft = panelLeft + (panelWidth - 4 * previewCell) / 2;
            int previewTop = boardTop + 20;
            for (int[] block : SHAPES[nextPiece]) {
                int blockX = previewLeft + block[0] * previewCell;
                int blockY = previewTop + block[1] * previewCell;
                ArcadeTheme.rounded(context, blockX, blockY, blockX + previewCell - 1, blockY + previewCell - 1, 2,
                        COLORS[nextPiece]);
                if (previewCell >= 9) {
                    context.fill(blockX + 1, blockY + 1, blockX + previewCell - 2, blockY + 2, 0x55FFFFFF);
                }
            }
            ArcadeTheme.panel(context, panelLeft, boardTop + 88, panelLeft + panelWidth, bottom);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("NÍVEL"),
                    panelLeft + panelWidth / 2, boardTop + 96, ArcadeTheme.MUTED);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(Integer.toString(Math.min(99, 1 + score / 1000))),
                    panelLeft + panelWidth / 2, boardTop + 110, ArcadeTheme.GOLD);
            int hintY = boardTop + 132;
            if (hintY + 30 < bottom) {
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("R"),
                        panelLeft + panelWidth / 2, hintY, ArcadeTheme.DIM);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("recomeçar"),
                        panelLeft + panelWidth / 2, hintY + 12, ArcadeTheme.DIM);
            }
        }

        if (gameOver) {
            int centerY = boardTop + ROWS * cellSize / 2;
            int bannerWidth = Math.min(COLUMNS * cellSize + 40, width - 20);
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, centerY - 28,
                    width / 2 + bannerWidth / 2, centerY + 28, 10, 0xF00A1422);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("FIM DE JOGO"),
                    width / 2, centerY - 16, ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Pontos: " + score),
                    width / 2, centerY - 1, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("R para jogar de novo"),
                    width / 2, centerY + 13, ArcadeTheme.MUTED);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBlock(DrawContext context, int left, int top, int x, int y, int color, int alpha) {
        int blockX = left + x * cellSize;
        int blockY = top + y * cellSize;
        int base = alpha >= 255 ? color : ArcadeTheme.alpha(color, alpha);
        ArcadeTheme.rounded(context, blockX, blockY, blockX + cellSize, blockY + cellSize, 2, base);
        if (cellSize >= 10 && alpha >= 255) {
            ArcadeTheme.rounded(context, blockX + 1, blockY + 1, blockX + cellSize - 1, blockY + cellSize - 1, 2,
                    ArcadeTheme.lighten(color, 40));
            context.fill(blockX + 2, blockY + 2, blockX + cellSize - 3, blockY + 4, 0x55FFFFFF);
            context.fill(blockX + 2, blockY + cellSize - 4, blockX + cellSize - 3, blockY + cellSize - 2, 0x33000000);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        if (gameOver) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> move(-1, 0);
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> move(1, 0);
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> move(0, 1);
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_SPACE -> rotate();
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
