package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Colete moedas no tabuleiro antes que o minuto acabe. */
public final class CoinCollectorScreen extends Screen {
    private static final int SIDE = 9;
    private static final int ROUND_TICKS = 1200;
    private final List<Particle> particles = new ArrayList<>();
    private int cellSize;
    private int left;
    private int top;
    private int playerX;
    private int playerY;
    private int coinX;
    private int coinY;
    private int ticksLeft;
    private int ticks;
    private int score;
    private int streak;
    private boolean gameOver;
    private boolean initialized;

    public CoinCollectorScreen() {
        super(Text.literal("Caça-Moedas"));
    }

    @Override
    protected void init() {
        cellSize = Math.max(16, Math.min(40, Math.min((width - 44) / SIDE, (height - 152) / SIDE)));
        left = (width - SIDE * cellSize) / 2;
        top = Math.max(64, (height - SIDE * cellSize - 104) / 2 + 28);
        int buttonY = Math.min(height - 25, top + SIDE * cellSize + 14);
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Recomeçar"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) {
            newGame();
            initialized = true;
        }
    }

    private void newGame() {
        playerX = SIDE / 2;
        playerY = SIDE / 2;
        score = 0;
        streak = 0;
        ticks = 0;
        ticksLeft = ROUND_TICKS;
        particles.clear();
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
        ticks++;
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.x += particle.vx;
            particle.y += particle.vy;
            particle.vy += 0.12;
            particle.life--;
            if (particle.life <= 0) {
                iterator.remove();
            }
        }
        if (!gameOver && --ticksLeft <= 0) {
            gameOver = true;
        }
    }

    private void move(int dx, int dy) {
        if (gameOver) {
            return;
        }
        playerX = Math.max(0, Math.min(SIDE - 1, playerX + dx));
        playerY = Math.max(0, Math.min(SIDE - 1, playerY + dy));
        if (playerX == coinX && playerY == coinY) {
            score++;
            streak++;
            burst(coinX, coinY);
            placeCoin();
        }
    }

    private void burst(int column, int row) {
        double centerX = left + column * cellSize + cellSize / 2.0;
        double centerY = top + row * cellSize + cellSize / 2.0;
        for (int index = 0; index < 10; index++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 0.4 + Math.random() * 1.5;
            particles.add(new Particle(centerX, centerY, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    12 + (int) (Math.random() * 8), ArcadeTheme.GOLD));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.GOLD);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? "Tempo! Você coletou " + score + " moedas"
                        : "Setas ou WASD para mover — pegue as moedas"),
                width / 2, 32, gameOver ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        int boardSize = SIDE * cellSize;
        int right = left + boardSize;
        int bottom = top + boardSize;
        ArcadeTheme.pill(context, textRenderer, "MOEDAS  " + score, left, top - 22, ArcadeTheme.GOLD);
        ArcadeTheme.pillRight(context, textRenderer, "TEMPO  " + Math.max(0, ticksLeft / 20) + "s", right,
                top - 22, ArcadeTheme.TEAL);
        float remaining = Math.max(0.0F, ticksLeft / (float) ROUND_TICKS);
        ArcadeTheme.progress(context, left, top - 6, boardSize, 4, remaining,
                remaining > 0.25F ? ArcadeTheme.GOLD : ArcadeTheme.DANGER);

        ArcadeTheme.rounded(context, left - 6, top - 6, right + 6, bottom + 6, 10,
                ArcadeTheme.alpha(ArcadeTheme.GOLD, 90));
        ArcadeTheme.rounded(context, left - 5, top - 5, right + 5, bottom + 5, 9, ArcadeTheme.PANEL_DEEP);
        context.fill(left, top, right, bottom, 0xFF101B26);

        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int x = left + column * cellSize;
                int y = top + row * cellSize;
                boolean path = column == playerX || row == playerY;
                int color = (row + column) % 2 == 0 ? 0xFF1B2C3B : 0xFF172634;
                if (path) {
                    color = 0xFF20374A;
                }
                context.fill(x, y, x + cellSize, y + cellSize, color);
                context.fill(x + cellSize - 1, y, x + cellSize, y + cellSize, 0x14000000);
            }
        }

        int coinLeft = left + coinX * cellSize;
        int coinTop = top + coinY * cellSize;
        int coinRadius = Math.max(3, cellSize / 3);
        int bob = (int) (Math.sin(ticks * 0.16) * Math.max(1, cellSize / 12));
        ArcadeTheme.glow(context, coinLeft + cellSize / 2, coinTop + cellSize / 2, coinRadius + 5, 0x55FFD66B);
        ArcadeTheme.disc(context, coinLeft + cellSize / 2, coinTop + cellSize / 2 + bob, coinRadius, 0xFFC99A2E);
        ArcadeTheme.disc(context, coinLeft + cellSize / 2, coinTop + cellSize / 2 + bob, coinRadius - 1, ArcadeTheme.GOLD);
        ArcadeTheme.ring(context, coinLeft + cellSize / 2, coinTop + cellSize / 2 + bob,
                Math.max(1, coinRadius - 2), 1, 0xAAFFF3C4);
        if (cellSize >= 22) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("$"),
                    coinLeft + cellSize / 2, coinTop + cellSize / 2 + bob - 4, 0xFF7A5A12);
        }

        int playerLeft = left + playerX * cellSize;
        int playerTop = top + playerY * cellSize;
        int pad = Math.max(3, cellSize / 6);
        ArcadeTheme.glow(context, playerLeft + cellSize / 2, playerTop + cellSize / 2, cellSize / 2 + 3, 0x4456C6FF);
        ArcadeTheme.rounded(context, playerLeft + pad, playerTop + pad, playerLeft + cellSize - pad,
                playerTop + cellSize - pad, 5, ArcadeTheme.SKY);
        ArcadeTheme.rounded(context, playerLeft + pad + 1, playerTop + pad + 1, playerLeft + cellSize - pad - 1,
                playerTop + pad + 4, 3, 0x55FFFFFF);
        if (cellSize >= 18) {
            int eye = Math.max(1, cellSize / 14);
            ArcadeTheme.disc(context, playerLeft + cellSize / 2 - cellSize / 5,
                    playerTop + cellSize / 2 - cellSize / 10, eye, 0xFF0B1626);
            ArcadeTheme.disc(context, playerLeft + cellSize / 2 + cellSize / 5,
                    playerTop + cellSize / 2 - cellSize / 10, eye, 0xFF0B1626);
        }

        for (Particle particle : particles) {
            ArcadeTheme.disc(context, (int) particle.x, (int) particle.y, 2,
                    ArcadeTheme.alpha(particle.color, Math.min(230, particle.life * 16)));
        }

        if (streak >= 2 && !gameOver) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("COMBO x" + streak),
                    width / 2, bottom + 2, ArcadeTheme.LIME);
        }
        if (gameOver) {
            int centerY = top + boardSize / 2;
            int bannerWidth = Math.min(boardSize + 30, width - 20);
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, centerY - 34,
                    width / 2 + bannerWidth / 2, centerY + 34, 11, 0xF00A1422);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("TEMPO ESGOTADO"),
                    width / 2, centerY - 22, ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Moedas: " + score),
                    width / 2, centerY - 4, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Recomeçar para tentar de novo"),
                    width / 2, centerY + 12, ArcadeTheme.MUTED);
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

    private static final class Particle {
        private double x;
        private double y;
        private final double vx;
        private double vy;
        private int life;
        private final int color;

        private Particle(double x, double y, double vx, double vy, int life, int color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.color = color;
        }
    }
}
