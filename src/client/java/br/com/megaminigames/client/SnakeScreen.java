package br.com.megaminigames.client;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Snake para um jogador, controlado pelas setas ou por WASD. */
public final class SnakeScreen extends Screen {
    private static final int COLUMNS = 20;
    private static final int ROWS = 14;
    private final Deque<Cell> snake = new ArrayDeque<>();

    private int cellSize;
    private int boardLeft;
    private int boardTop;
    private Cell food;
    private Direction direction = Direction.RIGHT;
    private Direction queuedDirection = Direction.RIGHT;
    private int ticksUntilMove;
    private int score;
    private boolean gameOver;
    private boolean initialized;

    public SnakeScreen() {
        super(Text.literal("Snake"));
    }

    @Override
    protected void init() {
        cellSize = Math.max(8, Math.min(24, Math.min((width - 40) / COLUMNS, (height - 112) / ROWS)));
        boardLeft = (width - COLUMNS * cellSize) / 2;
        boardTop = Math.max(42, (height - ROWS * cellSize) / 2 - 8);

        int buttonY = Math.min(height - 24, boardTop + ROWS * cellSize + 18);
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
        snake.clear();
        snake.addFirst(new Cell(COLUMNS / 2, ROWS / 2));
        snake.addLast(new Cell(COLUMNS / 2 - 1, ROWS / 2));
        snake.addLast(new Cell(COLUMNS / 2 - 2, ROWS / 2));
        direction = Direction.RIGHT;
        queuedDirection = Direction.RIGHT;
        ticksUntilMove = 0;
        score = 0;
        gameOver = false;
        initialized = true;
        placeFood();
    }

    private void placeFood() {
        do {
            food = new Cell(ThreadLocalRandom.current().nextInt(COLUMNS),
                    ThreadLocalRandom.current().nextInt(ROWS));
        } while (snake.contains(food));
    }

    @Override
    public void tick() {
        if (gameOver) {
            return;
        }
        if (++ticksUntilMove < Math.max(3, 7 - score / 4)) {
            return;
        }
        ticksUntilMove = 0;
        direction = queuedDirection;

        Cell head = snake.peekFirst();
        Cell next = new Cell(head.x + direction.dx, head.y + direction.dy);
        boolean eating = next.equals(food);
        if (!eating) {
            snake.removeLast();
        }
        if (next.x < 0 || next.x >= COLUMNS || next.y < 0 || next.y >= ROWS || snake.contains(next)) {
            gameOver = true;
            return;
        }

        snake.addFirst(next);
        if (eating) {
            score++;
            if (snake.size() == COLUMNS * ROWS) {
                gameOver = true;
            } else {
                placeFood();
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF101820);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Snake  |  Pontos: " + score),
                width / 2, 14, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Setas ou WASD para mover • R para reiniciar"), width / 2, 30, 0xFFCCDDDD);

        int boardRight = boardLeft + COLUMNS * cellSize;
        int boardBottom = boardTop + ROWS * cellSize;
        context.fill(boardLeft - 2, boardTop - 2, boardRight + 2, boardBottom + 2, 0xFF62717A);
        context.fill(boardLeft, boardTop, boardRight, boardBottom, 0xFF20333B);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if ((row + column) % 2 == 0) {
                    int x = boardLeft + column * cellSize;
                    int y = boardTop + row * cellSize;
                    context.fill(x, y, x + cellSize, y + cellSize, 0xFF263B42);
                }
            }
        }

        drawCell(context, food, 0xFFE94B5A);
        boolean isHead = true;
        for (Cell segment : snake) {
            drawCell(context, segment, isHead ? 0xFF9BE564 : 0xFF5DBB53);
            isHead = false;
        }

        if (gameOver) {
            context.fill(boardLeft, (boardTop + boardBottom) / 2 - 28,
                    boardRight, (boardTop + boardBottom) / 2 + 28, 0xAA000000);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(snake.size() == COLUMNS * ROWS ? "Você venceu!" : "Fim de jogo!"),
                    width / 2, (boardTop + boardBottom) / 2 - 5, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawCell(DrawContext context, Cell cell, int color) {
        int x = boardLeft + cell.x * cellSize;
        int y = boardTop + cell.y * cellSize;
        context.fill(x + 1, y + 1, x + cellSize - 1, y + cellSize - 1, color);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        Direction requested = switch (keyCode) {
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> Direction.UP;
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> Direction.DOWN;
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> Direction.LEFT;
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> Direction.RIGHT;
            default -> null;
        };
        if (requested != null) {
            if (requested != direction.opposite()) {
                queuedDirection = requested;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        private Direction opposite() {
            return switch (this) {
                case UP -> DOWN;
                case DOWN -> UP;
                case LEFT -> RIGHT;
                case RIGHT -> LEFT;
            };
        }
    }

    private record Cell(int x, int y) { }
}
