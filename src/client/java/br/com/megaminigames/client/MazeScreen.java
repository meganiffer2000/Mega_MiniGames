package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Labirinto aleatório: encontre a saída usando as setas ou WASD. */
public final class MazeScreen extends Screen {
    private static final int COLUMNS = 15;
    private static final int ROWS = 11;
    private boolean[][] walls = new boolean[ROWS][COLUMNS];
    private boolean[][] visited = new boolean[ROWS][COLUMNS];
    private final List<int[]> trail = new ArrayList<>();
    private int cellSize;
    private int left;
    private int top;
    private int playerX;
    private int playerY;
    private int steps;
    private int ticks;
    private boolean won;
    private boolean initialized;

    public MazeScreen() {
        super(Text.literal("Labirinto"));
    }

    @Override
    protected void init() {
        cellSize = Math.max(10, Math.min(28, Math.min((width - 40) / COLUMNS, (height - 156) / ROWS)));
        left = (width - COLUMNS * cellSize) / 2;
        top = Math.max(64, (height - ROWS * cellSize - 104) / 2 + 28);
        int buttonY = Math.min(height - 25, top + ROWS * cellSize + 16);
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Novo labirinto"), this::newMaze));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) {
            newMaze();
            initialized = true;
        }
    }

    private void newMaze() {
        walls = new boolean[ROWS][COLUMNS];
        visited = new boolean[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++) {
            java.util.Arrays.fill(walls[row], true);
        }
        carve(1, 1);
        walls[ROWS - 2][COLUMNS - 2] = false;
        playerX = 1;
        playerY = 1;
        steps = 0;
        trail.clear();
        won = false;
    }

    private void carve(int x, int y) {
        visited[y][x] = true;
        walls[y][x] = false;
        List<int[]> directions = new ArrayList<>(List.of(
                new int[]{0, -2}, new int[]{0, 2}, new int[]{-2, 0}, new int[]{2, 0}));
        Collections.shuffle(directions);
        for (int[] direction : directions) {
            int nextX = x + direction[0];
            int nextY = y + direction[1];
            if (nextX > 0 && nextX < COLUMNS - 1 && nextY > 0 && nextY < ROWS - 1
                    && !visited[nextY][nextX]) {
                walls[y + direction[1] / 2][x + direction[0] / 2] = false;
                carve(nextX, nextY);
            }
        }
    }

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.TEAL);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(won ? "Saída encontrada em " + steps + " passos!"
                        : "Setas/WASD para andar até a saída verde"),
                width / 2, 32, won ? ArcadeTheme.SUCCESS : ArcadeTheme.MUTED);

        int boardWidth = COLUMNS * cellSize;
        int boardHeight = ROWS * cellSize;
        int right = left + boardWidth;
        int bottom = top + boardHeight;
        ArcadeTheme.pill(context, textRenderer, "PASSOS  " + steps, left, top - 22, ArcadeTheme.TEAL);
        ArcadeTheme.pillRight(context, textRenderer, won ? "META!" : "META  →", right, top - 22,
                won ? ArcadeTheme.SUCCESS : ArcadeTheme.LIME);

        ArcadeTheme.rounded(context, left - 6, top - 6, right + 6, bottom + 6, 10,
                ArcadeTheme.alpha(ArcadeTheme.TEAL, 100));
        ArcadeTheme.rounded(context, left - 5, top - 5, right + 5, bottom + 5, 9, ArcadeTheme.PANEL_DEEP);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int x = left + column * cellSize;
                int y = top + row * cellSize;
                boolean exit = row == ROWS - 2 && column == COLUMNS - 2;
                if (walls[row][column]) {
                    context.fill(x, y, x + cellSize, y + cellSize, 0xFF1C2C42);
                    context.fill(x, y, x + cellSize, y + Math.max(2, cellSize / 4), 0xFF2A4059);
                    context.fill(x, y, x + Math.max(1, cellSize / 8), y + cellSize, 0xFF26394F);
                } else if (exit) {
                    ArcadeTheme.glow(context, x + cellSize / 2, y + cellSize / 2, cellSize, 0x55B9F26B);
                    context.fill(x, y, x + cellSize, y + cellSize, 0xFF1F3A24);
                    ArcadeTheme.disc(context, x + cellSize / 2, y + cellSize / 2, Math.max(2, cellSize / 3),
                            ArcadeTheme.LIME);
                } else {
                    context.fill(x, y, x + cellSize, y + cellSize, 0xFF16243A);
                }
            }
        }

        for (int[] step : trail) {
            ArcadeTheme.disc(context, left + step[0] * cellSize + cellSize / 2,
                    top + step[1] * cellSize + cellSize / 2, Math.max(1, cellSize / 8), 0x5556D6C9);
        }

        int px = left + playerX * cellSize;
        int py = top + playerY * cellSize;
        int pad = Math.max(2, cellSize / 6);
        ArcadeTheme.glow(context, px + cellSize / 2, py + cellSize / 2, cellSize / 2 + 4, 0x556CC9F5);
        ArcadeTheme.rounded(context, px + pad, py + pad, px + cellSize - pad, py + cellSize - pad, 4, ArcadeTheme.SKY);
        ArcadeTheme.rounded(context, px + pad + 1, py + pad + 1, px + cellSize - pad - 1, py + pad + 4, 2, 0x66FFFFFF);
        if (cellSize >= 16) {
            int eye = Math.max(1, cellSize / 14);
            int blink = ticks % 60 < 4 ? 1 : 0;
            ArcadeTheme.disc(context, px + cellSize / 2 - cellSize / 5, py + cellSize / 2 - cellSize / 10,
                    Math.max(1, eye - blink), 0xFF0B1626);
            ArcadeTheme.disc(context, px + cellSize / 2 + cellSize / 5, py + cellSize / 2 - cellSize / 10,
                    Math.max(1, eye - blink), 0xFF0B1626);
        }

        if (won) {
            int centerY = top + boardHeight / 2;
            int bannerWidth = Math.min(boardWidth + 20, width - 20);
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, centerY - 32,
                    width / 2 + bannerWidth / 2, centerY + 32, 10, 0xF00A1422);
            ArcadeTheme.drawCheck(context, width / 2 - 8, centerY - 22, ArcadeTheme.SUCCESS);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("SAÍDA ENCONTRADA!"),
                    width / 2, centerY - 6, ArcadeTheme.SUCCESS);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Passos: " + steps),
                    width / 2, centerY + 8, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("R para outro labirinto"),
                    width / 2, centerY + 21, ArcadeTheme.MUTED);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void move(int dx, int dy) {
        if (won) {
            return;
        }
        int nextX = playerX + dx;
        int nextY = playerY + dy;
        if (nextX >= 0 && nextX < COLUMNS && nextY >= 0 && nextY < ROWS && !walls[nextY][nextX]) {
            trail.add(new int[]{playerX, playerY});
            if (trail.size() > 400) {
                trail.remove(0);
            }
            playerX = nextX;
            playerY = nextY;
            steps++;
            won = playerX == COLUMNS - 2 && playerY == ROWS - 2;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newMaze();
            return true;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> move(0, -1);
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> move(0, 1);
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> move(-1, 0);
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> move(1, 0);
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
