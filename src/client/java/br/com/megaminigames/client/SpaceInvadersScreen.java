package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Invasores espaciais: mova a nave, atire e impeça os alienígenas de chegar ao chão. */
public final class SpaceInvadersScreen extends Screen {
    private static final int COLUMNS = 15;
    private static final int ROWS = 11;
    private static final int CELL = 30;
    private final List<Alien> aliens = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private int left;
    private int top;
    private int playerColumn;
    private int alienDirection;
    private int alienMoveTicks;
    private int shotCooldown;
    private int playerMoveCooldown;
    private int score;
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
        left = (width - COLUMNS * CELL) / 2;
        top = Math.max(48, (height - ROWS * CELL - 55) / 2);
        int buttonY = Math.min(height - 24, top + ROWS * CELL + 12);
        addDrawableChild(ButtonWidget.builder(Text.literal("Reiniciar (R)"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (!initialized) newGame();
    }

    private void newGame() {
        aliens.clear();
        bullets.clear();
        for (int row = 0; row < 3; row++) {
            for (int column = 2; column < COLUMNS - 2; column++) {
                aliens.add(new Alien(column, row + 1));
            }
        }
        playerColumn = COLUMNS / 2;
        alienDirection = 1;
        alienMoveTicks = 0;
        shotCooldown = 0;
        playerMoveCooldown = 0;
        score = 0;
        gameOver = false;
        won = false;
        initialized = true;
    }

    @Override
    public void tick() {
        if (gameOver) return;
        if (playerMoveCooldown > 0) playerMoveCooldown--;
        else if (leftPressed || rightPressed) {
            if (leftPressed) playerColumn = Math.max(0, playerColumn - 1);
            else playerColumn = Math.min(COLUMNS - 1, playerColumn + 1);
            playerMoveCooldown = 3;
        }
        if (shotCooldown > 0) shotCooldown--;

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
                score += 10;
                bulletIterator.remove();
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
                    if (atEdge) alien.row++;
                    else alien.column += alienDirection;
                    if (alien.row >= ROWS - 1) {
                        gameOver = true;
                    }
                }
            }
            if (atEdge) alienDirection *= -1;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF070B18);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Invasores Espaciais  •  Pontos: " + score + "  •  ←/→ ou A/D, Espaço atira"),
                width / 2, 14, 0xFFFFFFFF);
        int bottom = top + ROWS * CELL;
        context.fill(left - 2, top - 2, left + COLUMNS * CELL + 2, bottom + 2, 0xFF334355);
        for (int i = 0; i < 35; i++) {
            int starX = left + (i * 97 + 23) % (COLUMNS * CELL);
            int starY = top + (i * 53 + 17) % (ROWS * CELL);
            context.fill(starX, starY, starX + 2, starY + 2, 0xFF8797B0);
        }
        for (Alien alien : aliens) {
            if (alien.alive) {
                int x = left + alien.column * CELL;
                int y = top + alien.row * CELL;
                context.fill(x + 5, y + 5, x + CELL - 5, y + CELL - 8, 0xFF75E66B);
                context.fill(x + 1, y + 13, x + 6, y + 23, 0xFF75E66B);
                context.fill(x + CELL - 6, y + 13, x + CELL - 1, y + 23, 0xFF75E66B);
                context.fill(x + 9, y + 11, x + 12, y + 14, 0xFF101820);
                context.fill(x + 18, y + 11, x + 21, y + 14, 0xFF101820);
            }
        }
        for (Bullet bullet : bullets) {
            int x = left + (int) (bullet.column * CELL) + CELL / 2;
            int y = top + (int) (bullet.row * CELL);
            context.fill(x - 2, y, x + 2, y + 12, 0xFFFFF27A);
        }
        int playerX = left + playerColumn * CELL;
        int playerY = top + (ROWS - 1) * CELL;
        context.fill(playerX + 5, playerY + 15, playerX + CELL - 5, playerY + 26, 0xFF51BFFF);
        context.fill(playerX + 12, playerY + 7, playerX + 18, playerY + 17, 0xFF51BFFF);
        if (gameOver) {
            context.fill(left, top + 140, left + COLUMNS * CELL, top + 190, 0xAA000000);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(won ? "Vitória! Invasores eliminados." : "Os invasores chegaram! Pressione R"),
                    width / 2, top + 159, 0xFFFFFFFF);
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
        private boolean alive = true;

        private Alien(int column, int row) {
            this.column = column;
            this.row = row;
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
}
