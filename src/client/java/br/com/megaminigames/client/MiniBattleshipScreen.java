package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Batalha naval compacta: encontre um navio de três casas em até doze tiros. */
public final class MiniBattleshipScreen extends Screen {
    private static final int SIDE = 5;
    private static final int SHIP_SIZE = 3;
    private static final int MAX_SHOTS = 12;
    private final List<Particle> particles = new ArrayList<>();
    private int cellSize;
    private int left;
    private int top;
    private boolean[][] ship = new boolean[SIDE][SIDE];
    private boolean[][] shots = new boolean[SIDE][SIDE];
    private boolean[][] hits = new boolean[SIDE][SIDE];
    private int hitCount;
    private int shotCount;
    private int ticks;
    private int shakeTicks;
    private boolean gameOver;
    private boolean won;
    private boolean initialized;

    public MiniBattleshipScreen() {
        super(Text.literal("Batalha Naval"));
    }

    @Override
    protected void init() {
        cellSize = Math.max(20, Math.min(50, Math.min((width - 44) / SIDE, (height - 170) / SIDE)));
        left = (width - SIDE * cellSize) / 2;
        top = Math.max(66, (height - SIDE * cellSize - 110) / 2 + 28);
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        int buttonY = Math.min(height - 25, top + SIDE * cellSize + 16);
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Nova batalha"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) {
            newGame();
            initialized = true;
        }
    }

    private void newGame() {
        ship = new boolean[SIDE][SIDE];
        shots = new boolean[SIDE][SIDE];
        hits = new boolean[SIDE][SIDE];
        particles.clear();
        shakeTicks = 0;
        boolean horizontal = ThreadLocalRandom.current().nextBoolean();
        int row = horizontal ? ThreadLocalRandom.current().nextInt(SIDE)
                : ThreadLocalRandom.current().nextInt(SIDE - SHIP_SIZE + 1);
        int column = horizontal ? ThreadLocalRandom.current().nextInt(SIDE - SHIP_SIZE + 1)
                : ThreadLocalRandom.current().nextInt(SIDE);
        for (int index = 0; index < SHIP_SIZE; index++) {
            ship[row + (horizontal ? 0 : index)][column + (horizontal ? index : 0)] = true;
        }
        hitCount = 0;
        shotCount = 0;
        gameOver = false;
        won = false;
    }

    @Override
    public void tick() {
        ticks++;
        if (shakeTicks > 0) {
            shakeTicks--;
        }
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
    }

    private void burst(int column, int row, boolean hit) {
        double centerX = left + column * cellSize + cellSize / 2.0;
        double centerY = top + row * cellSize + cellSize / 2.0;
        int amount = hit ? 14 : 7;
        for (int index = 0; index < amount; index++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 0.6 + Math.random() * 2.0;
            particles.add(new Particle(centerX, centerY, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    14 + (int) (Math.random() * 10),
                    hit ? (index % 2 == 0 ? ArcadeTheme.CORAL : ArcadeTheme.GOLD) : ArcadeTheme.SKY));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.SKY);
        String status = won ? "Navio afundado! Você venceu!"
                : gameOver ? "Acabaram os tiros — o navio escapou"
                : "Clique em uma casa para disparar";
        ArcadeTheme.status(context, textRenderer, Text.literal(status), width / 2, 32,
                won ? ArcadeTheme.SUCCESS : gameOver ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        int boardWidth = SIDE * cellSize;
        int right = left + boardWidth;
        int bottom = top + boardWidth;
        ArcadeTheme.pill(context, textRenderer, "TIROS  " + shotCount + "/" + MAX_SHOTS, left, top - 22,
                ArcadeTheme.SKY);
        ArcadeTheme.pillRight(context, textRenderer, "ACERTOS  " + hitCount + "/" + SHIP_SIZE, right, top - 22,
                hitCount > 0 ? ArcadeTheme.CORAL : ArcadeTheme.MUTED);

        int shakeX = shakeTicks > 0 ? (shakeTicks % 2 == 0 ? 1 : -1) : 0;
        ArcadeTheme.rounded(context, left - 6 + shakeX, top - 6, right + 6 + shakeX, bottom + 6, 10,
                ArcadeTheme.alpha(ArcadeTheme.SKY, 100));
        ArcadeTheme.rounded(context, left - 5 + shakeX, top - 5, right + 5 + shakeX, bottom + 5, 9,
                ArcadeTheme.PANEL_DEEP);
        context.fillGradient(left + shakeX, top, right + shakeX, bottom, 0xFF0A2C46, 0xFF071B2C);

        for (int index = 0; index < 9; index++) {
            int waveY = top + 8 + index * Math.max(8, boardWidth / 9);
            int offset = (int) (Math.sin((ticks + index * 12) * 0.05) * 6);
            context.fill(left + 6 + offset, waveY, right - 6 + offset, waveY + 1, 0x22B8F0FF);
        }

        boolean over = mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
        int hoverColumn = over ? (int) ((mouseX - left) / cellSize) : -1;
        int hoverRow = over ? (int) ((mouseY - top) / cellSize) : -1;
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int x = left + column * cellSize + shakeX;
                int y = top + row * cellSize;
                int pad = Math.max(1, cellSize / 14);
                int cellColor = (row + column) % 2 == 0 ? 0xFF123650 : 0xFF0E2C43;
                if (over && row == hoverRow && column == hoverColumn && !shots[row][column] && !gameOver) {
                    cellColor = 0xFF1E4E6E;
                }
                ArcadeTheme.rounded(context, x + pad, y + pad, x + cellSize - pad, y + cellSize - pad, 3, cellColor);
                if (cellSize >= 20) {
                    context.fill(x + pad + 1, y + pad + 1, x + cellSize - pad - 1, y + pad + 3, 0x14FFFFFF);
                }
                if (shots[row][column]) {
                    if (hits[row][column]) {
                        ArcadeTheme.glow(context, x + cellSize / 2, y + cellSize / 2, cellSize / 2, 0x44FF8A65);
                        ArcadeTheme.drawX(context, x + cellSize / 2, y + cellSize / 2,
                                Math.max(3, cellSize / 4), 2, ArcadeTheme.CORAL);
                        context.fill(x + cellSize / 2 - 2, y + cellSize / 2 - 2,
                                x + cellSize / 2 + 2, y + cellSize / 2 + 2, 0xAAFFD66B);
                    } else {
                        ArcadeTheme.disc(context, x + cellSize / 2, y + cellSize / 2,
                                Math.max(2, cellSize / 9), 0xAA6CC9F5);
                        ArcadeTheme.ring(context, x + cellSize / 2, y + cellSize / 2,
                                Math.max(3, cellSize / 6), 1, 0x556CC9F5);
                    }
                } else if (over && row == hoverRow && column == hoverColumn && !gameOver) {
                    ArcadeTheme.drawO(context, x + cellSize / 2, y + cellSize / 2, Math.max(3, cellSize / 5), 1,
                            0x66FFFFFF);
                }
                if (gameOver && ship[row][column] && !shots[row][column]) {
                    int padShip = Math.max(3, cellSize / 4);
                    ArcadeTheme.rounded(context, x + padShip, y + padShip, x + cellSize - padShip,
                            y + cellSize - padShip, 4, 0xFFC9A05A);
                }
            }
        }
        if (gameOver && !won) {
            for (int row = 0; row < SIDE; row++) {
                for (int column = 0; column < SIDE; column++) {
                    if (ship[row][column]) {
                        int x = left + column * cellSize + shakeX;
                        int y = top + row * cellSize;
                        ArcadeTheme.rounded(context, x + 2, y + 2, x + cellSize - 2, y + cellSize - 2, 3,
                                0x44C9A05A);
                    }
                }
            }
        }

        for (Particle particle : particles) {
            ArcadeTheme.disc(context, (int) particle.x, (int) particle.y, 2,
                    ArcadeTheme.alpha(particle.color, Math.min(230, particle.life * 14)));
        }

        if (gameOver) {
            int centerY = top + boardWidth / 2;
            int bannerWidth = Math.min(boardWidth + 30, width - 20);
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, centerY - 34,
                    width / 2 + bannerWidth / 2, centerY + 34, 11, 0xF00A1422);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(won ? "VITÓRIA!" : "DERROTA"),
                    width / 2, centerY - 22, won ? ArcadeTheme.SUCCESS : ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Acertos: " + hitCount + "/" + SHIP_SIZE + "  •  Tiros: " + shotCount),
                    width / 2, centerY - 4, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Nova batalha para jogar"),
                    width / 2, centerY + 12, ArcadeTheme.MUTED);
        }
        super.render(context, mouseX, mouseY, delta);
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
        if (shots[row][column]) {
            return true;
        }
        shots[row][column] = true;
        shotCount++;
        if (ship[row][column]) {
            hits[row][column] = true;
            hitCount++;
            shakeTicks = 6;
            burst(column, row, true);
        } else {
            burst(column, row, false);
        }
        if (hitCount == SHIP_SIZE) {
            won = true;
            gameOver = true;
        } else if (shotCount >= MAX_SHOTS) {
            gameOver = true;
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
