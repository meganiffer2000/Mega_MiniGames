package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Invasores espaciais: mova a nave, atire e impeça os alienígenas de chegar ao chão. */
public final class SpaceInvadersScreen extends Screen {
    private static final int COLUMNS = 15;
    private static final int ROWS = 11;
    private static final int MAX_CELL = 28;
    private static final int[] ALIEN_COLORS = {0xFFB9F26B, 0xFF6CC9F5, 0xFFF0509B, 0xFFFFD66B};
    private final List<Alien> aliens = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private int left;
    private int top;
    private int cell = 20;
    private int playerColumn;
    private int alienDirection;
    private int alienMoveTicks;
    private int shotCooldown;
    private int playerMoveCooldown;
    private int score;
    private int ticks;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean gameOver;
    private boolean won;
    private boolean initialized;

    public SpaceInvadersScreen() {
        super(Text.literal("Invasores Espaciais"));
    }

    @Override
    protected void init() {
        cell = Math.max(9, Math.min(MAX_CELL, Math.min((width - 44) / COLUMNS, (height - 152) / ROWS)));
        left = (width - COLUMNS * cell) / 2;
        top = Math.max(62, (height - ROWS * cell - 104) / 2 + 28);
        int buttonY = Math.min(height - 24, top + ROWS * cell + 16);
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
        aliens.clear();
        bullets.clear();
        particles.clear();
        for (int row = 0; row < 4; row++) {
            for (int column = 1; column < COLUMNS - 1; column++) {
                aliens.add(new Alien(column, row + 1, row));
            }
        }
        playerColumn = COLUMNS / 2;
        alienDirection = 1;
        alienMoveTicks = 0;
        shotCooldown = 0;
        playerMoveCooldown = 0;
        score = 0;
        ticks = 0;
        gameOver = false;
        won = false;
        initialized = true;
    }

    @Override
    public void tick() {
        ticks++;
        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle particle = particleIterator.next();
            particle.x += particle.vx;
            particle.y += particle.vy;
            particle.vy += 0.14;
            particle.life--;
            if (particle.life <= 0) {
                particleIterator.remove();
            }
        }
        if (gameOver) {
            return;
        }
        if (playerMoveCooldown > 0) {
            playerMoveCooldown--;
        } else if (leftPressed || rightPressed) {
            if (leftPressed) {
                playerColumn = Math.max(0, playerColumn - 1);
            } else {
                playerColumn = Math.min(COLUMNS - 1, playerColumn + 1);
            }
            playerMoveCooldown = 3;
        }
        if (shotCooldown > 0) {
            shotCooldown--;
        }

        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.row -= 0.55;
            if (bullet.row < 0) {
                bulletIterator.remove();
                continue;
            }
            Alien hit = null;
            for (Alien alien : aliens) {
                if (alien.alive && Math.abs(bullet.column - alien.column) < 0.45
                        && (int) bullet.row == alien.row) {
                    hit = alien;
                    break;
                }
            }
            if (hit != null) {
                hit.alive = false;
                score += 10 + (3 - hit.tint) * 5;
                bulletIterator.remove();
                burst(hit.column + 0.5, hit.row + 0.5, ALIEN_COLORS[Math.min(hit.tint, 3)], 10);
            }
        }

        if (++alienMoveTicks >= Math.max(4, 13 - score / 50)) {
            alienMoveTicks = 0;
            int minColumn = COLUMNS;
            int maxColumn = -1;
            for (Alien alien : aliens) {
                if (alien.alive) {
                    minColumn = Math.min(minColumn, alien.column);
                    maxColumn = Math.max(maxColumn, alien.column);
                }
            }
            if (maxColumn < 0) {
                won = true;
                gameOver = true;
                return;
            }
            boolean atEdge = alienDirection > 0 ? maxColumn >= COLUMNS - 1 : minColumn <= 0;
            for (Alien alien : aliens) {
                if (alien.alive) {
                    if (atEdge) {
                        alien.row++;
                    } else {
                        alien.column += alienDirection;
                    }
                    if (alien.row >= ROWS - 1) {
                        gameOver = true;
                    }
                }
            }
            if (atEdge) {
                alienDirection *= -1;
            }
        }
    }

    private void burst(double column, double row, int color, int amount) {
        for (int index = 0; index < amount; index++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 0.2 + Math.random() * 0.9;
            particles.add(new Particle(column, row, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    12 + (int) (Math.random() * 10), color));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.LIME);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? (won ? "Setor limpo! Todos os invasores caíram"
                        : "Os invasores chegaram! Pressione R")
                        : "←/→ ou A/D movem  •  ESPAÇO atira"),
                width / 2, 32, gameOver ? (won ? ArcadeTheme.SUCCESS : ArcadeTheme.GOLD) : ArcadeTheme.MUTED);

        int boardWidth = COLUMNS * cell;
        int boardHeight = ROWS * cell;
        int right = left + boardWidth;
        int bottom = top + boardHeight;
        int alive = 0;
        for (Alien alien : aliens) {
            if (alien.alive) {
                alive++;
            }
        }
        ArcadeTheme.pill(context, textRenderer, "PONTOS  " + score, left, top - 22, ArcadeTheme.LIME);
        ArcadeTheme.pillRight(context, textRenderer, "ALVOS  " + alive, right, top - 22, ArcadeTheme.MAGENTA);

        ArcadeTheme.rounded(context, left - 6, top - 6, right + 6, bottom + 6, 10,
                ArcadeTheme.alpha(ArcadeTheme.LIME, 100));
        ArcadeTheme.rounded(context, left - 5, top - 5, right + 5, bottom + 5, 9, ArcadeTheme.PANEL_DEEP);
        context.fill(left, top, right, bottom, 0xFF060A16);
        context.fillGradient(left, top, right, top + boardHeight / 2, 0x22204A6E, 0x00000000);
        context.fillGradient(left, bottom - 30, right, bottom, 0x3300FFB0, 0x00000000);

        for (int index = 0; index < 42; index++) {
            int starX = left + (index * 97 + 23) % boardWidth;
            int starY = top + (index * 53 + 17) % boardHeight;
            int twinkle = (int) (Math.sin((ticks + index * 11) * 0.09) * 60);
            context.fill(starX, starY, starX + 1 + (index % 2), starY + 1 + (index % 2),
                    ArcadeTheme.alpha(0xFFFFFFFF, 90 + Math.max(0, twinkle)));
        }

        for (Alien alien : aliens) {
            if (!alien.alive) {
                continue;
            }
            int x = left + alien.column * cell;
            int y = top + alien.row * cell;
            int body = ALIEN_COLORS[Math.min(alien.tint, 3)];
            int bob = (int) (Math.sin((ticks + alien.column) * 0.16) * 1.5);
            int pad = Math.max(1, cell / 8);
            ArcadeTheme.rounded(context, x + pad, y + pad + bob, x + cell - pad, y + cell - pad * 2 + bob, 3, body);
            int eyeSize = Math.max(1, cell / 10);
            context.fill(x + cell / 3 - eyeSize, y + cell / 3 + bob,
                    x + cell / 3 - eyeSize + 2 * eyeSize + 1, y + cell / 3 + bob + 2 * eyeSize, 0xFF081018);
            context.fill(x + cell - cell / 3 - eyeSize, y + cell / 3 + bob,
                    x + cell - cell / 3 - eyeSize + 2 * eyeSize + 1, y + cell / 3 + bob + 2 * eyeSize, 0xFF081018);
            if (cell >= 14) {
                context.fill(x + cell / 2 - 1, y + cell - pad * 2 + bob, x + cell / 2 + 1, y + cell - pad + bob, body);
                context.fill(x + pad, y + cell - pad * 2 + bob, x + pad + 2, y + cell - pad + bob, body);
                context.fill(x + cell - pad - 2, y + cell - pad * 2 + bob, x + cell - pad, y + cell - pad + bob, body);
            }
        }

        for (Particle particle : particles) {
            int x = left + (int) (particle.x * cell);
            int y = top + (int) (particle.y * cell);
            ArcadeTheme.disc(context, x, y, particle.life > 10 ? 2 : 1,
                    ArcadeTheme.alpha(particle.color, Math.min(220, particle.life * 14)));
        }
        for (Bullet bullet : bullets) {
            int x = left + (int) (bullet.column * cell) + cell / 2;
            int y = top + (int) (bullet.row * cell);
            ArcadeTheme.glow(context, x, y, 7, 0x55FFF27A);
            context.fill(x - 1, y, x + 2, y + Math.max(8, cell / 2), 0xFFFFF27A);
        }

        int playerX = left + playerColumn * cell;
        int playerY = top + (ROWS - 1) * cell;
        int shipTop = playerY + Math.max(5, cell / 3);
        ArcadeTheme.glow(context, playerX + cell / 2, shipTop + cell / 3, cell / 2 + 4, 0x4451BFFF);
        ArcadeTheme.rounded(context, playerX + 2, shipTop + cell / 3, playerX + cell - 2, playerY + cell - 2, 3,
                ArcadeTheme.SKY);
        ArcadeTheme.rounded(context, playerX + cell / 2 - 2, shipTop, playerX + cell / 2 + 2, shipTop + cell / 2, 2,
                0xFF9CE6FF);
        context.fill(playerX + cell / 2 - 1, playerY + cell - 2, playerX + cell / 2 + 1, playerY + cell,
                0xFFFFD66B);

        context.fill(left, bottom - 2, right, bottom, 0x8851BFFF);
        if (gameOver) {
            int centerY = top + boardHeight / 2;
            int bannerWidth = Math.min(boardWidth + 30, width - 20);
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, centerY - 32,
                    width / 2 + bannerWidth / 2, centerY + 32, 10, 0xF00A1422);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(won ? "VITÓRIA!" : "FIM DE JOGO"),
                    width / 2, centerY - 20, won ? ArcadeTheme.SUCCESS : ArcadeTheme.DANGER);
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
            leftPressed = true;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) {
            rightPressed = true;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE && !gameOver && shotCooldown == 0) {
            bullets.add(new Bullet(playerColumn, ROWS - 1.2));
            shotCooldown = 10;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) {
            leftPressed = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) {
            rightPressed = false;
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Alien {
        private int column;
        private int row;
        private final int tint;
        private boolean alive = true;

        private Alien(int column, int row, int tint) {
            this.column = column;
            this.row = row;
            this.tint = tint;
        }
    }

    private static final class Bullet {
        private final int column;
        private double row;

        private Bullet(int column, double row) {
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
