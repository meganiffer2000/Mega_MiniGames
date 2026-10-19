package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Salto nas Nuvens: suba pelas plataformas o mais alto que conseguir. */
public final class DoodleJumpScreen extends ArcadeScreen {
    private static final float GRAVITY = 0.38F;
    private static final float JUMP = 3.8F;

    private final List<Platform> platforms = new ArrayList<>();
    private float playerX;
    private float playerY;
    private float velocityY;
    private float velocityX;
    private float camera;
    private float highestPlatform;
    private int score;
    private int bestScore;
    private int ticks;
    private boolean gameOver;
    private boolean leftHeld;
    private boolean rightHeld;
    private int fieldTop;
    private int fieldBottom;

    public DoodleJumpScreen() {
        super("Salto nas Nuvens");
    }

    @Override
    protected void init() {
        fieldTop = 62;
        fieldBottom = height - 34;
        addFooterButtons(height - 26, this::newGame);
        newGame();
    }

    private void newGame() {
        platforms.clear();
        playerX = width / 2.0F;
        playerY = 0;
        velocityX = 0;
        velocityY = 0;
        camera = 0;
        highestPlatform = 0;
        score = 0;
        ticks = 0;
        gameOver = false;
        addPlatform(width / 2.0F, 0, 0);
        while (highestPlatform < fieldBottom - fieldTop + 160) {
            spawnNext();
        }
    }

    private void addPlatform(float x, float y, int type) {
        Platform platform = new Platform();
        platform.x = Math.max(24, Math.min(width - 24, x));
        platform.y = y;
        platform.width = Math.max(30, Math.min(56, width / 7.0F));
        platform.type = type;
        platform.vx = ThreadLocalRandom.current().nextBoolean() ? 0.55F : -0.55F;
        platforms.add(platform);
        highestPlatform = Math.max(highestPlatform, y);
    }

    private void spawnNext() {
        float y = highestPlatform + 36 + ThreadLocalRandom.current().nextFloat() * 22;
        float x = 26 + ThreadLocalRandom.current().nextFloat() * Math.max(1, width - 52);
        int roll = ThreadLocalRandom.current().nextInt(100);
        int type = roll < 12 ? 2 : roll < 22 ? 1 : roll < 27 ? 3 : 0;
        addPlatform(x, y, type);
    }

    @Override
    public void tick() {
        if (gameOver) return;
        ticks++;
        float target = (leftHeld ? -1.8F : 0.0F) + (rightHeld ? 1.8F : 0.0F);
        velocityX += (target - velocityX) * 0.22F;
        playerX += velocityX;
        if (playerX < 0) {
            playerX += width;
        } else if (playerX > width) {
            playerX -= width;
        }
        float previousY = playerY;
        velocityY -= GRAVITY;
        playerY += velocityY;
        if (velocityY < 0) {
            for (Iterator<Platform> iterator = platforms.iterator(); iterator.hasNext(); ) {
                Platform platform = iterator.next();
                if (previousY < platform.y || playerY > platform.y) continue;
                if (Math.abs(playerX - platform.x) > platform.width / 2.0F + 5) continue;
                velocityY = platform.type == 3 ? JUMP * 1.6F : JUMP;
                if (platform.type == 2) {
                    iterator.remove();
                }
                break;
            }
        }
        for (Platform platform : platforms) {
            if (platform.type == 1) {
                platform.x += platform.vx;
                if (platform.x < platform.width / 2.0F + 4 || platform.x > width - platform.width / 2.0F - 4) {
                    platform.vx = -platform.vx;
                    platform.x = Math.max(platform.width / 2.0F + 4,
                            Math.min(width - platform.width / 2.0F - 4, platform.x));
                }
            }
        }
        camera = Math.max(camera, playerY - (fieldBottom - fieldTop) * 0.55F);
        score = Math.max(score, (int) (playerY / 8));
        bestScore = Math.max(bestScore, score);
        platforms.removeIf(platform -> platform.y < camera - 40);
        while (highestPlatform < camera + (fieldBottom - fieldTop) + 80) {
            spawnNext();
        }
        if (playerY < camera - 36) {
            gameOver = true;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            newGame();
            return true;
        }
        if (keyCode == 263 || keyCode == 65) {
            leftHeld = true;
            return true;
        }
        if (keyCode == 262 || keyCode == 68) {
            rightHeld = true;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 263 || keyCode == 65) {
            leftHeld = false;
            return true;
        }
        if (keyCode == 262 || keyCode == 68) {
            rightHeld = false;
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.SKY);
        ArcadeTheme.status(context, textRenderer, Text.literal("← → ou A/D movem  •  pule automaticamente"),
                width / 2, 32, ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "ALTURA " + score + " m", 12, 47, ArcadeTheme.SKY);
        ArcadeTheme.pillRight(context, textRenderer, "RECORDE " + bestScore + " m", width - 12, 47,
                ArcadeTheme.GOLD);

        context.fillGradient(0, fieldTop, width, fieldBottom, 0xFF0A2A55, 0xFF2E7DBB);
        context.fillGradient(0, fieldBottom - 40, width, fieldBottom, 0x00000000, 0x55206490);
        drawClouds(context);
        ArcadeTheme.disc(context, width - 38, fieldTop + 22, 9, 0x99FFF2C4);
        ArcadeTheme.disc(context, width - 32, fieldTop + 19, 8, 0xFF0F3563);

        for (Platform platform : platforms) {
            float screenY = screenY(platform.y);
            if (screenY < fieldTop - 12 || screenY > fieldBottom + 12) continue;
            int x = (int) (platform.x - platform.width / 2);
            int y = (int) screenY;
            int color = switch (platform.type) {
                case 1 -> ArcadeTheme.TEAL;
                case 2 -> ArcadeTheme.CORAL;
                case 3 -> ArcadeTheme.GOLD;
                default -> ArcadeTheme.LIME;
            };
            ArcadeTheme.rounded(context, x, y + 3, x + (int) platform.width, y + 7, 3, 0x44000000);
            ArcadeTheme.rounded(context, x, y, x + (int) platform.width, y + 6, 3,
                    ArcadeTheme.darken(color, 45));
            ArcadeTheme.rounded(context, x, y, x + (int) platform.width, y + 4, 3, color);
            context.fillGradient(x + 2, y, x + (int) platform.width - 2, y + 2, 0x66FFFFFF, 0x00FFFFFF);
            if (platform.type == 3) {
                ArcadeTheme.disc(context, x + (int) platform.width / 2, y - 3, 2, 0xFFFFF3C4);
            }
        }
        drawPlayer(context);
        if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, "FIM DO SALTO",
                    "Altura: " + score + " m  •  recorde " + bestScore + " m",
                    width / 2, (fieldTop + fieldBottom) / 2, Math.min(width - 20, 280),
                    ArcadeTheme.SKY);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawClouds(DrawContext context) {
        for (int index = 0; index < 9; index++) {
            int hash = index * 73428767 + 13;
            float cloudX = Math.floorMod(hash, width + 80) - 40;
            float worldY = 40 + index * 70 + Math.floorMod(hash >> 5, 30);
            float screenY = fieldBottom - (worldY - camera * 0.45F);
            if (screenY < fieldTop - 20 || screenY > fieldBottom + 20) continue;
            int size = 8 + index % 3 * 3;
            int color = 0x33FFFFFF;
            ArcadeTheme.disc(context, (int) cloudX, (int) screenY, size, color);
            ArcadeTheme.disc(context, (int) cloudX + size, (int) screenY + 2, size - 3, color);
            ArcadeTheme.disc(context, (int) cloudX - size, (int) screenY + 3, size - 4, color);
        }
    }

    private void drawPlayer(DrawContext context) {
        int x = (int) playerX;
        int y = (int) screenY(playerY);
        int radius = 8;
        ArcadeTheme.glow(context, x, y - radius, radius + 5, 0x66B9F26B);
        ArcadeTheme.rounded(context, x - radius, y - radius * 2, x + radius, y, 6, 0xFF3E7D2A);
        ArcadeTheme.rounded(context, x - radius + 1, y - radius * 2 + 1, x + radius - 1, y - 1, 5,
                ArcadeTheme.LIME);
        ArcadeTheme.disc(context, x - 3, y - radius - 2, 2, 0xFFFFFFFF);
        ArcadeTheme.disc(context, x + 3, y - radius - 2, 2, 0xFFFFFFFF);
        ArcadeTheme.disc(context, x - 3, y - radius - 2, 1, 0xFF1A2A12);
        ArcadeTheme.disc(context, x + 3, y - radius - 2, 1, 0xFF1A2A12);
        context.fill(x - 4, y - radius + 2, x + 4, y - radius + 3, 0xAA1A2A12);
        context.fill(x - radius, y - 3, x - radius + 2, y, 0xFFB9F26B);
        context.fill(x + radius - 2, y - 3, x + radius, y, 0xFFB9F26B);
    }

    private float screenY(float worldY) {
        return fieldBottom - (worldY - camera);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Platform {
        private float x;
        private float y;
        private float width;
        private float vx;
        private int type;
    }
}
