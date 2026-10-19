package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Asteroides: gire a nave, acelere e destrua as rochas antes que elas acertem você. */
public final class AsteroidsScreen extends Screen {
    private final List<Rock> rocks = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final List<Spark> sparks = new ArrayList<>();
    private int left;
    private int top;
    private int arenaWidth;
    private int arenaHeight;
    private float shipX;
    private float shipY;
    private float shipVX;
    private float shipVY;
    private float shipAngle;
    private int lives;
    private int score;
    private int wave;
    private int invulnerable;
    private int ticks;
    private int cooldown;
    private boolean gameOver;
    private boolean thrusting;
    private boolean turningLeft;
    private boolean turningRight;
    private boolean firing;

    public AsteroidsScreen() {
        super(Text.literal("Asteroides"));
    }

    @Override
    protected void init() {
        left = 14;
        top = 64;
        arenaWidth = Math.max(120, width - 28);
        arenaHeight = Math.max(110, height - 106);
        int buttonY = Math.min(height - 24, top + arenaHeight + 8);
        int buttonWidth = Math.min(112, Math.max(74, (width - 40) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Novo jogo"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        newGame();
    }

    private void newGame() {
        score = 0;
        lives = 3;
        wave = 1;
        ticks = 0;
        cooldown = 0;
        gameOver = false;
        shipX = arenaWidth / 2.0F;
        shipY = arenaHeight / 2.0F;
        shipVX = 0;
        shipVY = 0;
        shipAngle = -1.5708F;
        invulnerable = 80;
        rocks.clear();
        shots.clear();
        sparks.clear();
        spawnWave();
    }

    private void spawnWave() {
        int amount = 2 + wave;
        for (int index = 0; index < amount; index++) {
            float x;
            float y;
            do {
                x = ThreadLocalRandom.current().nextFloat() * arenaWidth;
                y = ThreadLocalRandom.current().nextFloat() * arenaHeight;
            } while (Math.hypot(x - shipX, y - shipY) < Math.min(arenaWidth, arenaHeight) * 0.35F);
            addRock(x, y, 3 + (index % 2));
        }
    }

    private void addRock(float x, float y, int size) {
        Rock rock = new Rock();
        rock.x = x;
        rock.y = y;
        rock.size = Math.max(1, Math.min(3, size));
        rock.radius = (arenaWidth + arenaHeight) / 46.0F * rock.size * 0.8F + 5;
        float speed = 0.35F + rock.size * 0.12F + wave * 0.03F;
        float direction = ThreadLocalRandom.current().nextFloat() * 6.2832F;
        rock.vx = (float) Math.cos(direction) * speed;
        rock.vy = (float) Math.sin(direction) * speed;
        rock.spin = (ThreadLocalRandom.current().nextFloat() - 0.5F) * 0.05F;
        rock.shape = new float[9];
        for (int index = 0; index < rock.shape.length; index++) {
            rock.shape[index] = 0.72F + ThreadLocalRandom.current().nextFloat() * 0.42F;
        }
        rocks.add(rock);
    }

    @Override
    public void tick() {
        if (gameOver) return;
        ticks++;
        if (cooldown > 0) cooldown--;
        if (firing) shoot();
        if (invulnerable > 0) invulnerable--;
        if (turningLeft) shipAngle -= 0.075F;
        if (turningRight) shipAngle += 0.075F;
        if (thrusting) {
            shipVX += (float) Math.cos(shipAngle) * 0.085F;
            shipVY += (float) Math.sin(shipAngle) * 0.085F;
        }
        shipVX *= 0.988F;
        shipVY *= 0.988F;
        float speed = (float) Math.hypot(shipVX, shipVY);
        if (speed > 3.4F) {
            shipVX = shipVX / speed * 3.4F;
            shipVY = shipVY / speed * 3.4F;
        }
        shipX = wrap(shipX + shipVX, arenaWidth);
        shipY = wrap(shipY + shipVY, arenaHeight);

        for (int index = shots.size() - 1; index >= 0; index--) {
            Shot shot = shots.get(index);
            shot.x = wrap(shot.x + shot.vx, arenaWidth);
            shot.y = wrap(shot.y + shot.vy, arenaHeight);
            shot.life--;
            if (shot.life <= 0) shots.remove(index);
        }

        for (Rock rock : rocks) {
            rock.x = wrap(rock.x + rock.vx, arenaWidth);
            rock.y = wrap(rock.y + rock.vy, arenaHeight);
            rock.angle += rock.spin;
        }

        for (int index = sparks.size() - 1; index >= 0; index--) {
            Spark spark = sparks.get(index);
            spark.x += spark.vx;
            spark.y += spark.vy;
            spark.vx *= 0.96F;
            spark.vy *= 0.96F;
            spark.life--;
            if (spark.life <= 0) sparks.remove(index);
        }

        handleCollisions();
        if (rocks.isEmpty() && !gameOver) {
            wave++;
            score += 50;
            spawnWave();
        }
    }

    private void handleCollisions() {
        for (int shotIndex = shots.size() - 1; shotIndex >= 0; shotIndex--) {
            Shot shot = shots.get(shotIndex);
            for (int rockIndex = rocks.size() - 1; rockIndex >= 0; rockIndex--) {
                Rock rock = rocks.get(rockIndex);
                if (Math.hypot(shot.x - rock.x, shot.y - rock.y) > rock.radius) continue;
                shots.remove(shotIndex);
                rocks.remove(rockIndex);
                score += rock.size == 3 ? 20 : rock.size == 2 ? 50 : 100;
                burst(rock.x, rock.y, rock.radius, 0xFFFFC46B);
                if (rock.size > 1) {
                    for (int child = 0; child < 2; child++) {
                        float x = rock.x;
                        float y = rock.y;
                        int size = rock.size - 1;
                        addRock(x, y, size);
                        Rock created = rocks.get(rocks.size() - 1);
                        float angle = ThreadLocalRandom.current().nextFloat() * 6.2832F;
                        created.vx += (float) Math.cos(angle) * 0.55F;
                        created.vy += (float) Math.sin(angle) * 0.55F;
                    }
                }
                break;
            }
        }
        if (invulnerable > 0) return;
        for (int index = rocks.size() - 1; index >= 0; index--) {
            Rock rock = rocks.get(index);
            if (Math.hypot(shipX - rock.x, shipY - rock.y) < rock.radius + 6) {
                rocks.remove(index);
                burst(rock.x, rock.y, rock.radius * 0.7F, ArcadeTheme.CORAL);
                loseLife();
                return;
            }
        }
    }

    private void loseLife() {
        lives--;
        shipX = arenaWidth / 2.0F;
        shipY = arenaHeight / 2.0F;
        shipVX = 0;
        shipVY = 0;
        shipAngle = -1.5708F;
        invulnerable = 80;
        if (lives <= 0) gameOver = true;
    }

    private void burst(float x, float y, float radius, int color) {
        int amount = Math.max(6, (int) (radius / 3));
        for (int index = 0; index < amount; index++) {
            Spark spark = new Spark();
            spark.x = x;
            spark.y = y;
            float angle = ThreadLocalRandom.current().nextFloat() * 6.2832F;
            float speed = 0.4F + ThreadLocalRandom.current().nextFloat() * 1.4F;
            spark.vx = (float) Math.cos(angle) * speed;
            spark.vy = (float) Math.sin(angle) * speed;
            spark.life = 14 + ThreadLocalRandom.current().nextInt(16);
            spark.color = color;
            sparks.add(spark);
        }
    }

    private void shoot() {
        if (cooldown > 0 || gameOver) return;
        Shot shot = new Shot();
        shot.x = wrap(shipX + (float) Math.cos(shipAngle) * 10, arenaWidth);
        shot.y = wrap(shipY + (float) Math.sin(shipAngle) * 10, arenaHeight);
        shot.vx = (float) Math.cos(shipAngle) * 4.4F + shipVX * 0.5F;
        shot.vy = (float) Math.sin(shipAngle) * 4.4F + shipVY * 0.5F;
        shot.life = 55;
        shots.add(shot);
        cooldown = 7;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.SKY);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? "Sua nave foi destruída" : "← → giram  •  ↑ acelera  •  Espaço atira"),
                width / 2, 32, gameOver ? ArcadeTheme.DANGER : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "PONTOS " + score, left, 47, ArcadeTheme.SKY);
        ArcadeTheme.pillRight(context, textRenderer, "ONDA " + wave, left + arenaWidth, 47, ArcadeTheme.GOLD);

        ArcadeTheme.rounded(context, left - 4, top - 4, left + arenaWidth + 4, top + arenaHeight + 4, 12,
                0x552C6CB8);
        context.fill(left, top, left + arenaWidth, top + arenaHeight, 0xFF050C16);
        for (int row = 0; row < arenaHeight; row += 3) {
            context.fill(left, top + row, left + arenaWidth, top + row + 1, 0x06FFFFFF);
        }

        for (Spark spark : sparks) {
            int alpha = Math.max(0, Math.min(220, spark.life * 10));
            ArcadeTheme.disc(context, (int) (left + spark.x), (int) (top + spark.y), 2,
                    ArcadeTheme.alpha(spark.color, alpha));
        }
        for (Shot shot : shots) {
            int x = (int) (left + shot.x);
            int y = (int) (top + shot.y);
            ArcadeTheme.glow(context, x, y, 5, 0x66FFF3C4);
            ArcadeTheme.disc(context, x, y, 2, 0xFFFFF6D8);
        }
        for (Rock rock : rocks) {
            drawRock(context, rock);
        }
        drawShip(context);

        for (int index = 0; index < lives; index++) {
            ArcadeTheme.heart(context, left + 10 + index * 13, top + 10, 5, ArcadeTheme.CORAL);
        }
        if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, "FIM DE JOGO",
                    "Pontos: " + score + "  •  ondas: " + wave,
                    width / 2, top + arenaHeight / 2, Math.min(width - 20, arenaWidth),
                    ArcadeTheme.DANGER);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawRock(DrawContext context, Rock rock) {
        int centerX = (int) (left + rock.x);
        int centerY = (int) (top + rock.y);
        int[] xs = new int[rock.shape.length];
        int[] ys = new int[rock.shape.length];
        for (int index = 0; index < rock.shape.length; index++) {
            float angle = rock.angle + index * 6.2832F / rock.shape.length;
            xs[index] = (int) (centerX + Math.cos(angle) * rock.radius * rock.shape[index]);
            ys[index] = (int) (centerY + Math.sin(angle) * rock.radius * rock.shape[index]);
        }
        ArcadeTheme.polygon(context, xs, ys, 0xFF3A4A63);
        int[] innerX = new int[xs.length];
        int[] innerY = new int[ys.length];
        for (int index = 0; index < xs.length; index++) {
            innerX[index] = centerX + (xs[index] - centerX) * 3 / 4;
            innerY[index] = centerY + (ys[index] - centerY) * 3 / 4;
        }
        ArcadeTheme.polygon(context, innerX, innerY, 0xFF516381);
        ArcadeTheme.disc(context, centerX - (int) (rock.radius * 0.3F), centerY - (int) (rock.radius * 0.25F),
                Math.max(1, (int) (rock.radius * 0.16F)), 0xFF2C3950);
        ArcadeTheme.disc(context, centerX + (int) (rock.radius * 0.25F), centerY + (int) (rock.radius * 0.2F),
                Math.max(1, (int) (rock.radius * 0.12F)), 0xFF2C3950);
    }

    private void drawShip(DrawContext context) {
        if (invulnerable > 0 && !gameOver && (invulnerable / 4) % 2 == 0) return;
        int centerX = (int) (left + shipX);
        int centerY = (int) (top + shipY);
        float cos = (float) Math.cos(shipAngle);
        float sin = (float) Math.sin(shipAngle);
        int[] xs = new int[3];
        int[] ys = new int[3];
        xs[0] = (int) (centerX + cos * 11);
        ys[0] = (int) (centerY + sin * 11);
        xs[1] = (int) (centerX - cos * 7 - sin * 7);
        ys[1] = (int) (centerY - sin * 7 + cos * 7);
        xs[2] = (int) (centerX - cos * 7 + sin * 7);
        ys[2] = (int) (centerY - sin * 7 - cos * 7);
        ArcadeTheme.glow(context, centerX, centerY, 14, 0x446CC9F5);
        ArcadeTheme.polygon(context, xs, ys, 0xFFCDE8FF);
        int[] innerX = {(int) (centerX + cos * 5), (int) (centerX - cos * 3 - sin * 3), (int) (centerX - cos * 3 + sin * 3)};
        int[] innerY = {(int) (centerY + sin * 5), (int) (centerY - sin * 3 + cos * 3), (int) (centerY - sin * 3 - cos * 3)};
        ArcadeTheme.polygon(context, innerX, innerY, ArcadeTheme.SKY);
        if (thrusting && ticks % 4 < 3) {
            float flicker = 5 + (ticks % 5);
            int[] flameX = {(int) (centerX - cos * 6 - sin * 3), (int) (centerX - cos * (6 + flicker)),
                    (int) (centerX - cos * 6 + sin * 3)};
            int[] flameY = {(int) (centerY - sin * 6 + cos * 3), (int) (centerY - sin * (6 + flicker)),
                    (int) (centerY - sin * 6 - cos * 3)};
            ArcadeTheme.polygon(context, flameX, flameY, ArcadeTheme.GOLD);
        }
    }

    private static float wrap(float value, int limit) {
        if (value < 0) return value + limit;
        if (value >= limit) return value - limit;
        return value;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            newGame();
            return true;
        }
        if (gameOver) return super.keyPressed(keyCode, scanCode, modifiers);
        switch (keyCode) {
            case 263, 65 -> turningLeft = true;
            case 262, 68 -> turningRight = true;
            case 265, 87 -> thrusting = true;
            case 32 -> firing = true;
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 263, 65 -> turningLeft = false;
            case 262, 68 -> turningRight = false;
            case 265, 87 -> thrusting = false;
            case 32 -> firing = false;
            default -> {
                return super.keyReleased(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Rock {
        private float x;
        private float y;
        private float vx;
        private float vy;
        private float radius;
        private float angle;
        private float spin;
        private int size;
        private float[] shape;
    }

    private static final class Shot {
        private float x;
        private float y;
        private float vx;
        private float vy;
        private int life;
    }

    private static final class Spark {
        private float x;
        private float y;
        private float vx;
        private float vy;
        private int life;
        private int color;
    }
}
