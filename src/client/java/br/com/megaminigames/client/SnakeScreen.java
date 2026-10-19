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
        cellSize = Math.max(8, Math.min(24, Math.min((width - 44) / COLUMNS, (height - 168) / ROWS)));
        boardLeft = (width - COLUMNS * cellSize) / 2;
        boardTop = Math.max(82, (height - ROWS * cellSize - 104) / 2 + 16);
        int buttonY = Math.min(height - 26, boardTop + ROWS * cellSize + 18);
        int buttonWidth = Math.min(112, Math.max(76, (width - 44) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20, Text.literal("Reiniciar (R)"), () -> newGame()));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20, Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
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
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.LIME);

        int boardRight = boardLeft + COLUMNS * cellSize;
        int boardBottom = boardTop + ROWS * cellSize;
        String scoreLabel = "PONTOS " + score;
        ArcadeTheme.pill(context, textRenderer, scoreLabel, boardLeft, 32, ArcadeTheme.LIME);
        int level = Math.max(1, 7 - Math.max(3, 7 - score / 4));
        String levelLabel = "VELOCIDADE " + level;
        ArcadeTheme.pillRight(context, textRenderer, levelLabel, boardRight, 32, ArcadeTheme.GOLD);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Setas ou WASD  •  R reinicia"), width / 2, 54, ArcadeTheme.MUTED);

        int pad = 6;
        ArcadeTheme.rounded(context, boardLeft - pad + 2, boardTop - pad + 4, boardRight + pad + 2,
                boardBottom + pad + 4, 12, 0x55000000);
        ArcadeTheme.rounded(context, boardLeft - pad, boardTop - pad, boardRight + pad, boardBottom + pad,
                12, 0x6635C8B9);
        ArcadeTheme.rounded(context, boardLeft - pad + 1, boardTop - pad + 1, boardRight + pad - 1,
                boardBottom + pad - 1, 11, 0xF00A1422);
        context.fillGradient(boardLeft, boardTop, boardRight, boardBottom, 0xFF10263A, 0xFF0A1626);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if ((row + column) % 2 == 0) {
                    int x = boardLeft + column * cellSize;
                    int y = boardTop + row * cellSize;
                    context.fill(x, y, x + cellSize, y + cellSize, 0x06FFFFFF);
                }
            }
        }

        drawFood(context);
        boolean isHead = true;
        int index = 0;
        int total = snake.size();
        for (Cell segment : snake) {
            int color = isHead ? 0xFFB9F26B : ArcadeTheme.mix(0xFF6ED36A, 0xFF2E9E6B, (float) index / Math.max(1, total));
            drawSegment(context, segment, color);
            if (isHead) {
                drawEyes(context, segment);
            }
            isHead = false;
            index++;
        }

        if (gameOver) {
            int bannerWidth = Math.min(boardRight - boardLeft - 12, 230);
            int cx = width / 2;
            int cy = (boardTop + boardBottom) / 2;
            boolean victory = snake.size() == COLUMNS * ROWS;
            ArcadeTheme.rounded(context, cx - bannerWidth / 2, cy - 30, cx + bannerWidth / 2, cy + 30, 12, 0xEE08111E);
            ArcadeTheme.rounded(context, cx - bannerWidth / 2, cy - 30, cx + bannerWidth / 2, cy - 28, 12,
                    victory ? ArcadeTheme.GOLD : ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(victory ? "VOCÊ VENCEU!" : "FIM DE JOGO"),
                    cx, cy - 17, victory ? ArcadeTheme.GOLD : ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Pontos: " + score), cx, cy - 3, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Pressione R para jogar de novo"), cx,
                    cy + 10, ArcadeTheme.MUTED);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawFood(DrawContext context) {
        if (food == null) return;
        int x = boardLeft + food.x * cellSize + cellSize / 2;
        int y = boardTop + food.y * cellSize + cellSize / 2;
        int radius = Math.max(3, cellSize / 2 - 3);
        ArcadeTheme.glow(context, x, y, radius + 3, ArcadeTheme.MAGENTA);
        ArcadeTheme.disc(context, x, y + 1, radius, 0xFFB3273F);
        ArcadeTheme.disc(context, x, y, radius, 0xFFFF4D6D);
        ArcadeTheme.disc(context, x - radius / 3, y - radius / 3, Math.max(1, radius / 3), 0x99FFFFFF);
        context.fill(x, y - radius - 2, x + 2, y - radius + 1, 0xFF6FE58E);
        context.fill(x + 1, y - radius - 3, x + 4, y - radius - 1, 0xFF3FAE67);
    }

    private void drawSegment(DrawContext context, Cell cell, int color) {
        int x = boardLeft + cell.x * cellSize;
        int y = boardTop + cell.y * cellSize;
        int inset = Math.max(1, cellSize / 12);
        ArcadeTheme.rounded(context, x + inset, y + inset, x + cellSize - inset, y + cellSize - inset,
                Math.max(2, cellSize / 4), ArcadeTheme.darken(color, 45));
        ArcadeTheme.rounded(context, x + inset + 1, y + inset + 1, x + cellSize - inset - 1,
                y + cellSize - inset - 1, Math.max(2, cellSize / 4), color);
        ArcadeTheme.rounded(context, x + inset + 1, y + inset + 1, x + cellSize - inset - 1,
                y + cellSize / 2, Math.max(2, cellSize / 4), 0x18FFFFFF);
    }

    private void drawEyes(DrawContext context, Cell head) {
        int x = boardLeft + head.x * cellSize + cellSize / 2;
        int y = boardTop + head.y * cellSize + cellSize / 2;
        int offset = Math.max(2, cellSize / 5);
        int eye = Math.max(1, cellSize / 8);
        int dx = direction.dx;
        int dy = direction.dy;
        int eye1X = x + dx * offset / 2 - dy * offset;
        int eye1Y = y + dy * offset / 2 - dx * offset;
        int eye2X = x + dx * offset / 2 + dy * offset;
        int eye2Y = y + dy * offset / 2 + dx * offset;
        context.fill(eye1X - eye, eye1Y - eye, eye1X + eye, eye1Y + eye, 0xFF0A1422);
        context.fill(eye2X - eye, eye2Y - eye, eye2X + eye, eye2Y + eye, 0xFF0A1422);
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
