package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Acerte a toupeira antes que o tempo acabe. */
public final class WhackAMoleScreen extends Screen {
    private static final int SIDE = 3;
    private static final int MAX_CELL_SIZE = 84;
    private static final int ROUND_TICKS = 600;
    private final List<Particle> particles = new ArrayList<>();
    private int cellSize;
    private int left;
    private int top;
    private int score;
    private int misses;
    private int combo;
    private int ticksLeft;
    private int moleTicks;
    private int moleTotal;
    private int moleCell = -1;
    private int shakeTicks;
    private int hitFlash;
    private boolean gameOver;
    private boolean initialized;

    public WhackAMoleScreen() {
        super(Text.literal("Acerte a Toupeira"));
    }

    @Override
    protected void init() {
        int widthLimit = (width - 40) / SIDE;
        int heightLimit = (height - 154) / SIDE;
        cellSize = Math.max(34, Math.min(MAX_CELL_SIZE, Math.min(widthLimit, heightLimit)));
        left = (width - SIDE * cellSize) / 2;
        top = Math.max(66, (height - SIDE * cellSize - 108) / 2 + 30);
        int buttonY = Math.min(height - 25, top + SIDE * cellSize + 14);
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Jogar novamente"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) {
            newGame();
        }
    }

    private void newGame() {
        score = 0;
        misses = 0;
        combo = 0;
        ticksLeft = ROUND_TICKS;
        shakeTicks = 0;
        hitFlash = 0;
        particles.clear();
        moleCell = ThreadLocalRandom.current().nextInt(SIDE * SIDE);
        moleTotal = 24;
        moleTicks = moleTotal;
        gameOver = false;
        initialized = true;
    }

    @Override
    public void tick() {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.x += particle.vx;
            particle.y += particle.vy;
            particle.vy += 0.16;
            particle.life--;
            if (particle.life <= 0) {
                iterator.remove();
            }
        }
        if (hitFlash > 0) {
            hitFlash--;
        }
        if (shakeTicks > 0) {
            shakeTicks--;
        }
        if (gameOver) {
            return;
        }
        ticksLeft--;
        if (--moleTicks <= 0) {
            if (moleCell >= 0 && combo > 0) {
                combo = 0;
            }
            spawnMole();
        }
        if (ticksLeft <= 0) {
            gameOver = true;
            moleCell = -1;
        }
    }

    private void spawnMole() {
        int previous = moleCell;
        do {
            moleCell = ThreadLocalRandom.current().nextInt(SIDE * SIDE);
        } while (moleCell == previous);
        moleTotal = Math.max(11, 30 - score);
        moleTicks = moleTotal;
    }

    private void burst(int cellIndex) {
        int row = cellIndex / SIDE;
        int column = cellIndex % SIDE;
        double centerX = left + column * cellSize + cellSize / 2.0;
        double centerY = top + row * cellSize + cellSize / 2.0;
        for (int index = 0; index < 12; index++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 0.5 + Math.random() * 1.8;
            particles.add(new Particle(centerX, centerY, Math.cos(angle) * speed, Math.sin(angle) * speed - 0.6,
                    14 + (int) (Math.random() * 10), index % 3 == 0 ? ArcadeTheme.GOLD : ArcadeTheme.LIME));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.CORAL);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? "Tempo esgotado! Pontos: " + score
                        : "Clique na toupeira — quanto mais rápida, mais pontos"),
                width / 2, 32, gameOver ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        int boardWidth = SIDE * cellSize;
        int right = left + boardWidth;
        int bottom = top + SIDE * cellSize;
        ArcadeTheme.pill(context, textRenderer, "PONTOS  " + score, left, top - 22, ArcadeTheme.CORAL);
        ArcadeTheme.pillRight(context, textRenderer, "TEMPO  " + Math.max(0, ticksLeft / 20) + "s", right,
                top - 22, ArcadeTheme.GOLD);
        float remaining = Math.max(0.0F, ticksLeft / (float) ROUND_TICKS);
        ArcadeTheme.progress(context, left, top - 6, boardWidth, 4, remaining,
                remaining > 0.35F ? ArcadeTheme.LIME : ArcadeTheme.DANGER);

        int shakeX = shakeTicks > 0 ? (shakeTicks % 2 == 0 ? 1 : -1) : 0;
        int shakeY = shakeTicks > 2 ? 1 : 0;
        ArcadeTheme.rounded(context, left - 7 + shakeX, top - 7 + shakeY, right + 7 + shakeX, bottom + 7 + shakeY, 12,
                ArcadeTheme.alpha(ArcadeTheme.CORAL, 100));
        ArcadeTheme.rounded(context, left - 6 + shakeX, top - 6 + shakeY, right + 6 + shakeX, bottom + 6 + shakeY, 11,
                ArcadeTheme.PANEL_DEEP);
        context.fill(left + shakeX, top + shakeY, right + shakeX, bottom + shakeY, 0xFF1B120C);

        for (int index = 0; index < SIDE * SIDE; index++) {
            int row = index / SIDE;
            int column = index % SIDE;
            int x = left + column * cellSize + shakeX + 3;
            int y = top + row * cellSize + shakeY + 3;
            int innerRight = x + cellSize - 6;
            int innerBottom = y + cellSize - 6;
            boolean hovered = mouseX >= x && mouseX < innerRight && mouseY >= y && mouseY < innerBottom
                    && !gameOver;
            ArcadeTheme.rounded(context, x, y, innerRight, innerBottom, 8,
                    index % 2 == 0 ? 0xFF2C1E14 : 0xFF261A11);
            ArcadeTheme.rounded(context, x + 2, y + 2, innerRight - 2, innerBottom - 2, 7,
                    hovered ? 0xFF3B2A1B : 0xFF332317);
            ArcadeTheme.rounded(context, x + 3, y + 3, innerRight - 3, y + 7, 4, 0x18FFFFFF);
            drawHole(context, x + (cellSize - 6) / 2, y + cellSize - 18, cellSize);
            if (index == moleCell && !gameOver) {
                float rise = 1.0F - moleTicks / (float) moleTotal;
                int offset = (int) ((1.0F - Math.min(1.0F, rise * 1.8F)) * (cellSize * 0.32F));
                drawMole(context, x + (cellSize - 6) / 2, y + (cellSize - 6) / 2 + offset, offset > cellSize / 5);
                if (hitFlash > 0 && index == moleCell) {
                    ArcadeTheme.glow(context, x + cellSize / 2, y + cellSize / 2, cellSize / 2,
                            ArcadeTheme.alpha(ArcadeTheme.LIME, hitFlash * 12));
                }
            }
        }

        for (Particle particle : particles) {
            ArcadeTheme.disc(context, (int) particle.x, (int) particle.y, 2,
                    ArcadeTheme.alpha(particle.color, Math.min(230, particle.life * 14)));
        }

        if (combo >= 2) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("COMBO x" + combo),
                    width / 2, bottom + 2, ArcadeTheme.GOLD);
        }
        if (gameOver) {
            int centerY = top + SIDE * cellSize / 2;
            int bannerWidth = Math.min(boardWidth + 30, width - 20);
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, centerY - 34,
                    width / 2 + bannerWidth / 2, centerY + 34, 11, 0xF00A1422);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("TEMPO ESGOTADO"),
                    width / 2, centerY - 22, ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Pontos: " + score),
                    width / 2, centerY - 5, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Erros: " + misses),
                    width / 2, centerY + 7, ArcadeTheme.MUTED);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Jogar novamente para tentar"),
                    width / 2, centerY + 21, ArcadeTheme.DIM);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawHole(DrawContext context, int centerX, int centerY, int size) {
        int width = Math.max(16, size / 2);
        int height = Math.max(6, size / 6);
        ArcadeTheme.rounded(context, centerX - width / 2, centerY - height / 2,
                centerX + width / 2, centerY + height / 2, Math.max(3, height / 2), 0xFF0B0704);
        ArcadeTheme.rounded(context, centerX - width / 2, centerY - height / 2,
                centerX + width / 2, centerY, Math.max(3, height / 2), 0xFF17100A);
    }

    private void drawMole(DrawContext context, int centerX, int centerY, boolean eyesClosed) {
        int radius = Math.max(9, Math.min(23, cellSize / 2 - 6));
        ArcadeTheme.disc(context, centerX, centerY + radius / 3, radius, 0xFF8A5A3D);
        ArcadeTheme.disc(context, centerX, centerY, radius, 0xFFA9744F);
        ArcadeTheme.disc(context, centerX - radius / 3, centerY - radius / 3, radius / 3, 0xFFC08A62);
        int earSize = Math.max(3, radius / 3);
        ArcadeTheme.disc(context, centerX - radius + earSize / 2, centerY - radius / 2, earSize, 0xFF8A5A3D);
        ArcadeTheme.disc(context, centerX + radius - earSize / 2, centerY - radius / 2, earSize, 0xFF8A5A3D);
        int eye = Math.max(2, radius / 4);
        if (eyesClosed) {
            context.fill(centerX - radius / 2 - eye, centerY - radius / 5,
                    centerX - radius / 2 + eye, centerY - radius / 5 + 1, 0xFF201510);
            context.fill(centerX + radius / 2 - eye, centerY - radius / 5,
                    centerX + radius / 2 + eye, centerY - radius / 5 + 1, 0xFF201510);
        } else {
            ArcadeTheme.disc(context, centerX - radius / 2, centerY - radius / 5, eye, 0xFFFFFFFF);
            ArcadeTheme.disc(context, centerX + radius / 2, centerY - radius / 5, eye, 0xFFFFFFFF);
            ArcadeTheme.disc(context, centerX - radius / 2, centerY - radius / 5, Math.max(1, eye - 2), 0xFF17120F);
            ArcadeTheme.disc(context, centerX + radius / 2, centerY - radius / 5, Math.max(1, eye - 2), 0xFF17120F);
        }
        ArcadeTheme.disc(context, centerX, centerY + radius / 4, Math.max(2, radius / 4), 0xFFFFB0A6);
        ArcadeTheme.disc(context, centerX, centerY + radius / 4, Math.max(1, radius / 8), 0xFFFF7E6F);
        if (radius >= 12) {
            context.fill(centerX - radius / 3, centerY + radius / 2, centerX, centerY + radius / 2 + 1,
                    0x55FFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0 || gameOver || mouseX < left || mouseY < top
                || mouseX >= left + SIDE * cellSize || mouseY >= top + SIDE * cellSize) {
            return false;
        }
        int column = (int) ((mouseX - left) / cellSize);
        int row = (int) ((mouseY - top) / cellSize);
        int clickedCell = row * SIDE + column;
        if (clickedCell == moleCell) {
            score++;
            combo++;
            hitFlash = 4;
            burst(clickedCell);
            spawnMole();
        } else {
            combo = 0;
            misses++;
            shakeTicks = 6;
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
