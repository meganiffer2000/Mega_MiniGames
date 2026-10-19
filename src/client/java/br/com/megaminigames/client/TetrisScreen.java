package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Tetris clássico com sete peças, rotação, linhas e pontuação. */
public final class TetrisScreen extends Screen {
    private static final int COLUMNS = 10;
    private static final int ROWS = 20;
    private static final int MAX_CELL_SIZE = 20;
    private int cellSize;
    private int boardLeft;
    private int boardTop;
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
    private int piece;
    private int rotation;
    private int pieceX;
    private int pieceY;
    private int fallTicks;
    private int score;
    private boolean gameOver;
    private boolean initialized;

    public TetrisScreen() {
        super(Text.literal("Tetris"));
    }

    @Override
    protected void init() {
        int availableHeight = Math.max(ROWS * 6, height - 74);
        int heightLimitedCell = availableHeight / ROWS;
        int widthLimitedCell = Math.max(6, (width - 40) / COLUMNS);
        cellSize = Math.max(6, Math.min(MAX_CELL_SIZE, Math.min(heightLimitedCell, widthLimitedCell)));
        boardLeft = (width - COLUMNS * cellSize) / 2;
        boardTop = Math.max(38, (height - ROWS * cellSize - 46) / 2);
        int buttonY = height - 22;
        addDrawableChild(ButtonWidget.builder(Text.literal("Reiniciar (R)"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
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
        initialized = true;
        spawnPiece();
    }

    private void spawnPiece() {
        piece = ThreadLocalRandom.current().nextInt(SHAPES.length);
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
                java.util.Arrays.fill(board[0], 0);
                row++;
            }
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
        context.fill(0, 0, width, height, 0xFF101720);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Tetris  |  Pontos: " + score + "  |  ← → mover, ↓ acelerar, ↑ girar"),
                width / 2, 12, 0xFFFFFFFF);

        int left = boardLeft;
        int top = boardTop;
        context.fill(left - 3, top - 3, left + COLUMNS * cellSize + 3,
                top + ROWS * cellSize + 3, 0xFF667782);
        context.fill(left, top, left + COLUMNS * cellSize, top + ROWS * cellSize, 0xFF111820);
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                if (board[y][x] != 0) {
                    drawBlock(context, left, top, x, y, COLORS[board[y][x] - 1]);
                }
            }
        }
        if (!gameOver) {
            for (int[] block : SHAPES[piece]) {
                int x = pieceX + rotatedX(block[0], block[1], rotation);
                int y = pieceY + rotatedY(block[0], block[1], rotation);
                if (y >= 0) {
                    drawBlock(context, left, top, x, y, COLORS[piece]);
                }
            }
        } else {
            int centerY = top + ROWS * cellSize / 2;
            context.fill(left, centerY - 20, left + COLUMNS * cellSize, centerY + 20, 0xAA000000);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Fim de jogo! Pressione R"),
                    width / 2, centerY - 4, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBlock(DrawContext context, int left, int top, int x, int y, int color) {
        int blockX = left + x * cellSize;
        int blockY = top + y * cellSize;
        context.fill(blockX + 1, blockY + 1, blockX + cellSize - 1, blockY + cellSize - 1, color);
        if (cellSize >= 10) {
            context.fill(blockX + 2, blockY + 2, blockX + cellSize / 2, blockY + 4, 0x55FFFFFF);
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
            default -> { return super.keyPressed(keyCode, scanCode, modifiers); }
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
