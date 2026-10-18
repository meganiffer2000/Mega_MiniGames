package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Labirinto aleatório: encontre a saída usando as setas ou WASD. */
public final class MazeScreen extends Screen {
    private static final int COLUMNS = 15;
    private static final int ROWS = 11;
    private boolean[][] walls = new boolean[ROWS][COLUMNS];
    private boolean[][] visited = new boolean[ROWS][COLUMNS];
    private int cellSize;
    private int left;
    private int top;
    private int playerX;
    private int playerY;
    private int steps;
    private boolean won;
    private boolean initialized;

    public MazeScreen() {
        super(Text.literal("Labirinto"));
    }

    @Override
    protected void init() {
        cellSize = Math.max(10, Math.min(30, Math.min((width - 30) / COLUMNS, (height - 120) / ROWS)));
        left = (width - COLUMNS * cellSize) / 2;
        top = 50;
        int buttonY = height - 25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Novo labirinto"), button -> newMaze())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (!initialized) {
            newMaze();
            initialized = true;
        }
    }

    private void newMaze() {
        walls = new boolean[ROWS][COLUMNS];
        visited = new boolean[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++) java.util.Arrays.fill(walls[row], true);
        carve(1, 1);
        walls[ROWS - 2][COLUMNS - 2] = false;
        playerX = 1;
        playerY = 1;
        steps = 0;
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF111923);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 13, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(won ? "Saída encontrada em " + steps + " passos!" : "Passos: " + steps + "  •  Setas/WASD para andar"),
                width / 2, 32, won ? 0xFF79E98A : 0xFFE6EEF5);
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int x = left + column * cellSize;
                int y = top + row * cellSize;
                int color = walls[row][column] ? 0xFF334255 : 0xFFE8E1CE;
                if (row == ROWS - 2 && column == COLUMNS - 2) color = 0xFF47B96A;
                context.fill(x, y, x + cellSize, y + cellSize, color);
            }
        }
        int px = left + playerX * cellSize;
        int py = top + playerY * cellSize;
        context.fill(px + 3, py + 3, px + cellSize - 3, py + cellSize - 3, 0xFF4A9DFF);
        super.render(context, mouseX, mouseY, delta);
    }

    private void move(int dx, int dy) {
        if (won) return;
        int nextX = playerX + dx;
        int nextY = playerY + dy;
        if (nextX >= 0 && nextX < COLUMNS && nextY >= 0 && nextY < ROWS && !walls[nextY][nextX]) {
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
            default -> { return super.keyPressed(keyCode, scanCode, modifiers); }
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
