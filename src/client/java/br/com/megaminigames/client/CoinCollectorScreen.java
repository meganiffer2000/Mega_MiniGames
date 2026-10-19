package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Colete moedas no tabuleiro antes que o minuto acabe. */
public final class CoinCollectorScreen extends Screen {
    private static final int SIDE = 9;
    private int cellSize;
    private int left;
    private int top;
    private int playerX;
    private int playerY;
    private int coinX;
    private int coinY;
    private int ticksLeft;
    private int score;
    private boolean gameOver;
    private boolean initialized;

    public CoinCollectorScreen() {
        super(Text.literal("Caça-Moedas"));
    }

    @Override
    protected void init() {
        cellSize = Math.max(14, Math.min(38, Math.min((width - 36) / SIDE, (height - 120) / SIDE)));
        left = (width - SIDE * cellSize) / 2;
        top = 50;
        addDrawableChild(ButtonWidget.builder(Text.literal("Recomeçar"), button -> newGame())
                .dimensions(width / 2 - 116, height - 25, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, height - 25, 112, 20).build());
        if (!initialized) {
            newGame();
            initialized = true;
        }
    }

    private void newGame() {
        playerX = SIDE / 2;
        playerY = SIDE / 2;
        score = 0;
        ticksLeft = 1200;
        gameOver = false;
        placeCoin();
    }

    private void placeCoin() {
        do {
            coinX = ThreadLocalRandom.current().nextInt(SIDE);
            coinY = ThreadLocalRandom.current().nextInt(SIDE);
        } while (coinX == playerX && coinY == playerY);
    }

    @Override
    public void tick() {
        if (!gameOver && --ticksLeft <= 0) gameOver = true;
    }

    private void move(int dx, int dy) {
        if (gameOver) return;
        playerX = Math.max(0, Math.min(SIDE - 1, playerX + dx));
        playerY = Math.max(0, Math.min(SIDE - 1, playerY + dy));
        if (playerX == coinX && playerY == coinY) {
            score++;
            placeCoin();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF14202A);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 13, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Moedas: " + score + "  •  Tempo: " + ticksLeft / 20 + "s  •  Setas/WASD para mover"),
                width / 2, 33, 0xFFE7EDF3);
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int x = left + column * cellSize;
                int y = top + row * cellSize;
                context.fill(x + 1, y + 1, x + cellSize - 1, y + cellSize - 1,
                        (row + column) % 2 == 0 ? 0xFF344A43 : 0xFF2D403B);
            }
        }
        int coinLeft = left + coinX * cellSize;
        int coinTop = top + coinY * cellSize;
        context.fill(coinLeft + cellSize / 4, coinTop + cellSize / 4,
                coinLeft + cellSize * 3 / 4, coinTop + cellSize * 3 / 4, 0xFFFFD84D);
        int playerLeft = left + playerX * cellSize;
        int playerTop = top + playerY * cellSize;
        context.fill(playerLeft + 4, playerTop + 4, playerLeft + cellSize - 4,
                playerTop + cellSize - 4, 0xFF56BFFF);
        if (gameOver) {
            int centerY = top + SIDE * cellSize / 2;
            context.fill(left, centerY - 18, left + SIDE * cellSize, centerY + 18, 0xAA000000);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Tempo! Você coletou " + score + " moedas."), width / 2,
                    centerY - 4, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
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
