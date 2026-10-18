package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Desvie dos blocos que caem e sobreviva o máximo possível. */
public final class DodgeScreen extends Screen {
    private static final int COLUMNS = 11;
    private static final int ROWS = 12;
    private static final int CELL = 32;
    private final List<FallingBlock> blocks = new ArrayList<>();
    private int left;
    private int top;
    private int playerColumn;
    private int ticks;
    private int score;
    private int spawnCounter;
    private boolean gameOver;
    private boolean initialized;

    public DodgeScreen() {
        super(Text.literal("Desvie!"));
    }

    @Override
    protected void init() {
        left = (width - COLUMNS * CELL) / 2;
        top = Math.max(52, (height - ROWS * CELL - 62) / 2);
        int buttonY = Math.min(height - 24, top + ROWS * CELL + 12);
        addDrawableChild(ButtonWidget.builder(Text.literal("Reiniciar (R)"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (!initialized) newGame();
    }

    private void newGame() {
        blocks.clear();
        playerColumn = COLUMNS / 2;
        ticks = 0;
        score = 0;
        spawnCounter = 0;
        gameOver = false;
        initialized = true;
    }

    @Override
    public void tick() {
        if (gameOver) return;
        ticks++;
        spawnCounter++;
        if (spawnCounter >= Math.max(5, 12 - score / 8)) {
            spawnCounter = 0;
            blocks.add(new FallingBlock(ThreadLocalRandom.current().nextInt(COLUMNS), 0));
            if (score > 20 && ThreadLocalRandom.current().nextInt(4) == 0) {
                int secondColumn = ThreadLocalRandom.current().nextInt(COLUMNS);
                if (secondColumn != blocks.get(blocks.size() - 1).column) {
                    blocks.add(new FallingBlock(secondColumn, -2));
                }
            }
        }

        double speed = 0.12 + Math.min(0.13, score * 0.002);
        Iterator<FallingBlock> iterator = blocks.iterator();
        while (iterator.hasNext()) {
            FallingBlock block = iterator.next();
            block.row += speed;
            if ((int) block.row >= ROWS - 1 && block.column == playerColumn) {
                gameOver = true;
                return;
            }
            if (block.row >= ROWS) {
                iterator.remove();
                score++;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF111923);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Desvie!  |  Pontos: " + score + "  •  Use ←/→ ou A/D"),
                width / 2, 13, 0xFFFFFFFF);
        int right = left + COLUMNS * CELL;
        int bottom = top + ROWS * CELL;
        context.fill(left - 2, top - 2, right + 2, bottom + 2, 0xFF596673);
        context.fill(left, top, right, bottom, 0xFF1D2A36);
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if ((row + column) % 2 == 0) {
                    int x = left + column * CELL;
                    int y = top + row * CELL;
                    context.fill(x, y, x + CELL, y + CELL, 0xFF24323E);
                }
            }
        }
        for (FallingBlock block : blocks) {
            int x = left + block.column * CELL;
            int y = top + (int) (block.row * CELL);
            context.fill(x + 3, y + 3, x + CELL - 3, y + CELL - 3, 0xFFE9544E);
            context.fill(x + 7, y + 7, x + 14, y + 10, 0x66FFFFFF);
        }
        int playerX = left + playerColumn * CELL;
        int playerY = top + (ROWS - 1) * CELL;
        context.fill(playerX + 4, playerY + 4, playerX + CELL - 4, playerY + CELL - 4, 0xFF58C6FF);
        context.fill(playerX + 10, playerY + 9, playerX + 14, playerY + 13, 0xFF10202B);
        context.fill(playerX + 19, playerY + 9, playerX + 23, playerY + 13, 0xFF10202B);
        if (gameOver) {
            context.fill(left, top + ROWS * CELL / 2 - 26, right, top + ROWS * CELL / 2 + 26, 0xAA000000);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Você foi atingido! Pressione R"),
                    width / 2, top + ROWS * CELL / 2 - 4, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) {
            playerColumn = Math.max(0, playerColumn - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) {
            playerColumn = Math.min(COLUMNS - 1, playerColumn + 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class FallingBlock {
        private final int column;
        private double row;

        private FallingBlock(int column, double row) {
            this.column = column;
            this.row = row;
        }
    }
}
