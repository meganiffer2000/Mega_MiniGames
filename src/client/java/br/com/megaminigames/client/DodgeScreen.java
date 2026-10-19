package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Desvie dos blocos que caem e sobreviva o máximo possível. */
public final class DodgeScreen extends Screen {
    private static final int COLUMNS = 11;
    private static final int ROWS = 12;
    private static final int MAX_CELL = 30;
    private static final int MIN_CELL = 12;
    private final List<FallingBlock> blocks = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private int cell = 24;
    private int left;
    private int top;
    private int playerColumn;
    private int ticks;
    private int score;
    private int spawnCounter;
    private int shakeTicks;
    private boolean gameOver;
    private boolean initialized;

    public DodgeScreen() {
        super(Text.literal("Desvie!"));
    }

    @Override
    protected void init() {
        cell = Math.max(MIN_CELL, Math.min(MAX_CELL, Math.min((width - 44) / COLUMNS, (height - 154) / ROWS)));
        left = (width - COLUMNS * cell) / 2;
        top = Math.max(62, (height - ROWS * cell - 104) / 2 + 28);
        int buttonY = Math.min(height - 24, top + ROWS * cell + 14);
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
        blocks.clear();
        particles.clear();
        playerColumn = COLUMNS / 2;
        ticks = 0;
        score = 0;
        spawnCounter = 0;
        shakeTicks = 0;
        gameOver = false;
        initialized = true;
    }

    @Override
    public void tick() {
        if (shakeTicks > 0) {
            shakeTicks--;
        }
        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle particle = particleIterator.next();
            particle.x += particle.vx;
            particle.y += particle.vy;
            particle.vy += 0.18;
            particle.life--;
            if (particle.life <= 0) {
                particleIterator.remove();
            }
        }
        if (gameOver) {
            return;
        }
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
                shakeTicks = 8;
                burst(block.column, ROWS - 1.4);
                return;
            }
            if (block.row >= ROWS) {
                iterator.remove();
                score++;
            }
        }
    }

    private void burst(int column, double row) {
        double centerX = left + column * cell + cell / 2.0;
        double centerY = top + row * cell + cell / 2.0;
        for (int index = 0; index < 14; index++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 0.5 + Math.random() * 2.0;
            particles.add(new Particle(centerX, centerY, Math.cos(angle) * speed, Math.sin(angle) * speed - 0.5,
                    16 + (int) (Math.random() * 12), index % 3 == 0 ? ArcadeTheme.GOLD : ArcadeTheme.CORAL));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.CORAL);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? "Você foi atingido! Pressione R"
                        : "←/→ ou A/D para desviar dos blocos que caem"),
                width / 2, 32, gameOver ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        int boardWidth = COLUMNS * cell;
        int boardHeight = ROWS * cell;
        int right = left + boardWidth;
        int bottom = top + boardHeight;
        ArcadeTheme.pill(context, textRenderer, "PONTOS  " + score, left, top - 22, ArcadeTheme.CORAL);
        ArcadeTheme.pillRight(context, textRenderer, "NÍVEL  " + (1 + score / 15), right, top - 22, ArcadeTheme.SKY);

        int shakeX = shakeTicks > 0 ? (shakeTicks % 2 == 0 ? 1 : -2) : 0;
        ArcadeTheme.rounded(context, left - 6 + shakeX, top - 6, right + 6 + shakeX, bottom + 6, 10,
                ArcadeTheme.alpha(ArcadeTheme.CORAL, 100));
        ArcadeTheme.rounded(context, left - 5 + shakeX, top - 5, right + 5 + shakeX, bottom + 5, 9,
                ArcadeTheme.PANEL_DEEP);
        context.fill(left + shakeX, top, right + shakeX, bottom, 0xFF0C1420);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int x = left + column * cell + shakeX;
                int y = top + row * cell;
                int color = (row + column) % 2 == 0 ? 0xFF172435 : 0xFF131E2D;
                context.fill(x, y, x + cell, y + cell, color);
                if (cell >= 16) {
                    context.fill(x + 1, y + 1, x + 2, y + 2, 0x18FFFFFF);
                }
            }
        }
        context.fillGradient(left + shakeX, bottom - cell, right + shakeX, bottom, 0x33FF8A65, 0x00000000);
        context.fill(left + shakeX, bottom - 2, right + shakeX, bottom, 0x66FF8A65);

        for (FallingBlock block : blocks) {
            int x = left + block.column * cell + shakeX;
            int y = top + (int) (block.row * cell);
            int pad = Math.max(1, cell / 8);
            boolean nearFloor = y + cell >= bottom - cell * 2;
            if (nearFloor) {
                ArcadeTheme.glow(context, x + cell / 2, y + cell / 2, cell / 2 + 3, 0x44FF8A65);
            }
            ArcadeTheme.rounded(context, x + pad, y + pad, x + cell - pad, y + cell - pad, 3, 0xFF8E2A26);
            ArcadeTheme.rounded(context, x + pad + 1, y + pad + 1, x + cell - pad - 1, y + cell - pad - 1, 3,
                    ArcadeTheme.CORAL);
            if (cell >= 14) {
                context.fill(x + pad + 3, y + pad + 2, x + cell - pad - 3, y + pad + 4, 0x55FFFFFF);
                ArcadeTheme.drawX(context, x + cell / 2, y + cell / 2, Math.max(2, cell / 5), 1, 0x66FFFFFF);
            }
        }

        for (Particle particle : particles) {
            ArcadeTheme.disc(context, (int) particle.x, (int) particle.y, 2,
                    ArcadeTheme.alpha(particle.color, Math.min(220, particle.life * 12)));
        }

        int playerX = left + playerColumn * cell + shakeX;
        int playerY = top + (ROWS - 1) * cell;
        int pad = Math.max(2, cell / 7);
        ArcadeTheme.glow(context, playerX + cell / 2, playerY + cell / 2, cell / 2 + 4,
                gameOver ? 0x22FF8A65 : 0x4456C6FF);
        ArcadeTheme.rounded(context, playerX + pad, playerY + pad, playerX + cell - pad, playerY + cell - pad, 4,
                gameOver ? 0xFF8E2A26 : ArcadeTheme.SKY);
        if (!gameOver) {
            int eye = Math.max(1, cell / 12);
            ArcadeTheme.disc(context, playerX + cell / 2 - cell / 5, playerY + cell / 2 - cell / 8, eye, 0xFF0B1626);
            ArcadeTheme.disc(context, playerX + cell / 2 + cell / 5, playerY + cell / 2 - cell / 8, eye, 0xFF0B1626);
            int flicker = ticks % 6 < 3 ? 0 : 1;
            ArcadeTheme.rounded(context, playerX + cell / 3, playerY + cell - pad, playerX + cell / 3 + 2,
                    playerY + cell - pad + 3 + flicker, 1, ArcadeTheme.GOLD);
            ArcadeTheme.rounded(context, playerX + cell * 2 / 3 - 2, playerY + cell - pad,
                    playerX + cell * 2 / 3, playerY + cell - pad + 3 + flicker, 1, ArcadeTheme.GOLD);
        }

        if (gameOver) {
            int centerY = top + boardHeight / 2;
            int bannerWidth = Math.min(boardWidth + 20, width - 20);
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, centerY - 32,
                    width / 2 + bannerWidth / 2, centerY + 32, 10, 0xF00A1422);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("ATINGIDO!"),
                    width / 2, centerY - 20, ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Pontos: " + score),
                    width / 2, centerY - 4, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("R para tentar de novo"),
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
