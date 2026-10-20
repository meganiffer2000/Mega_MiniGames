package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Órbita Estável — simulação orbital em pixel art.
 *
 * <p>Você pilota um módulo científico ao redor de um planeta. A gravidade puxa
 * o módulo para o centro e cada propulsão gasta combustível; o objetivo é
 * manter a nave dentro da faixa verde da órbita-alvo pelo maior tempo possível
 * enquanto desvia de destroços e recolhe células de combustível.</p>
 */
public final class OrbitScreen extends ArcadeScreen {
    /** Constante gravitacional do mundo (px³/tick²). */
    private static final float GRAVITY = 380.0F;
    private static final float THRUST = 0.04F;
    private static final float DRAG = 0.99993F;
    private static final float MAX_FUEL = 100.0F;
    private static final float FUEL_DRAIN = 0.08F;
    private static final int MAX_LEVEL = 5;
    private static final int RING_TARGET = 520;

    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final List<float[]> debris = new ArrayList<>();
    private final List<float[]> cells = new ArrayList<>();
    private final List<float[]> trail = new ArrayList<>();
    private final List<float[]> sparks = new ArrayList<>();

    private float px;
    private float py;
    private float vx;
    private float vy;
    private float fuel = MAX_FUEL;
    private int level = 1;
    private int score;
    private int lives = 3;
    private int ringTicks;
    private int ticks;
    private int flashTicks;
    private int respawnTicks;
    private int messageTicks;
    private int stationAngle = 180;
    private boolean destroyed;
    private boolean finished;
    private boolean stable;
    private boolean thrustUp;
    private boolean thrustDown;
    private boolean thrustLeft;
    private boolean thrustRight;
    private static int best;

    private String message = "";

    public OrbitScreen() {
        super("Órbita Estável");
    }

    @Override
    protected void init() {
        addFooterButtons(height - 26, this::restart);
        if (ticks == 0) restart();
    }

    private void restart() {
        level = 1;
        score = 0;
        lives = 3;
        fuel = MAX_FUEL;
        ticks = 0;
        finished = false;
        destroyed = false;
        respawnTicks = 0;
        flashTicks = 0;
        ringTicks = 0;
        debris.clear();
        cells.clear();
        trail.clear();
        sparks.clear();
        spawnDebris(1 + level);
        spawnCell();
        resetOrbit();
        message = "Mantenha o módulo na faixa verde da órbita-alvo.";
        messageTicks = 200;
    }

    /* ------------------------------------------------------------------ */
    /* Geometria do mundo                                                 */
    /* ------------------------------------------------------------------ */

    private int centerX() {
        return width / 2;
    }

    private int centerY() {
        return height / 2 + 4;
    }

    private int planetRadius() {
        return Math.max(26, Math.min(46, Math.min(width, height) / 7));
    }

    private int maxRadius() {
        return Math.max(planetRadius() + 34,
                Math.min(Math.min(width, height) / 2 - 14, centerY() - 12));
    }

    private int targetRadius() {
        float[] fractions = {0.76F, 0.80F, 0.72F, 0.84F, 0.78F};
        return Math.round(maxRadius() * fractions[(level - 1) % fractions.length]);
    }

    private int tolerance() {
        return Math.max(8, 18 - level * 2);
    }

    private float distance() {
        return (float) Math.hypot(px, py);
    }

    private float circularSpeed(float radius) {
        return (float) Math.sqrt(GRAVITY / Math.max(1.0F, radius));
    }

    /* ------------------------------------------------------------------ */
    /* Estado do jogo                                                     */
    /* ------------------------------------------------------------------ */

    private void resetOrbit() {
        destroyed = false;
        float radius = targetRadius();
        px = radius;
        py = 0;
        float speed = circularSpeed(radius);
        vx = 0;
        vy = speed;
        fuel = Math.max(fuel, 60);
        ringTicks = 0;
        trail.clear();
    }

    private void spawnDebris(int count) {
        if (debris.size() >= 7) return;
        int target = targetRadius();
        for (int index = 0; index < count; index++) {
            float radius;
            if (random.nextBoolean()) {
                radius = planetRadius() + 16 + random.nextFloat()
                        * Math.max(8, target - tolerance() - 12 - planetRadius() - 16);
            } else {
                float low = target + tolerance() + 10;
                radius = low + random.nextFloat() * Math.max(8, maxRadius() + 8 - low);
            }
            float angle = random.nextFloat() * 6.283F;
            float direction = random.nextBoolean() ? 1 : -1;
            float angular = circularSpeed(radius) / radius * (0.55F + random.nextFloat() * 0.7F) * direction;
            debris.add(new float[] {radius, angle, angular, 3 + random.nextInt(3)});
        }
    }

    private void spawnCell() {
        float angle = (float) (Math.PI + (random.nextFloat() - 0.5F) * 1.6F);
        float radius = targetRadius() + (random.nextFloat() - 0.5F) * tolerance();
        cells.add(new float[] {radius, angle, 240});
    }

    @Override
    public void tick() {
        ticks++;
        if (messageTicks > 0) messageTicks--;
        if (flashTicks > 0) flashTicks--;
        updateParticles();
        if (finished) return;
        if (destroyed) {
            if (respawnTicks > 0) respawnTicks--;
            if (respawnTicks <= 0) resetOrbit();
            return;
        }

        // física orbital
        float dist = distance();
        float gravity = GRAVITY / Math.max(64.0F, dist * dist);
        vx -= px / Math.max(1.0F, dist) * gravity;
        vy -= py / Math.max(1.0F, dist) * gravity;
        if (dist < planetRadius() + 16) {
            // atmosfera densa: arrasto extra perto do planeta
            vx *= 0.985F;
            vy *= 0.985F;
        }

        boolean thrusting = false;
        float ax = 0;
        float ay = 0;
        if (thrustLeft) ax -= THRUST;
        if (thrustRight) ax += THRUST;
        if (thrustUp) ay -= THRUST;
        if (thrustDown) ay += THRUST;
        if (ax != 0 || ay != 0) {
            float length = (float) Math.hypot(ax, ay);
            ax /= length;
            ay /= length;
            if (fuel > 0) {
                vx += ax * THRUST;
                vy += ay * THRUST;
                fuel = Math.max(0, fuel - FUEL_DRAIN);
                thrusting = true;
                if (ticks % 2 == 0) {
                    sparks.add(new float[] {px - ax * 8, py - ay * 8, -ax * 0.6F, -ay * 0.6F, 12});
                }
            }
        }
        vx *= DRAG;
        vy *= DRAG;
        px += vx;
        py += vy;

        if (ticks % 2 == 0) {
            trail.add(new float[] {px, py, 22});
        }

        // anéis e progresso
        dist = distance();
        stable = Math.abs(dist - targetRadius()) <= tolerance();
        if (stable) {
            score += 1;
            ringTicks++;
            if (ringTicks >= RING_TARGET) nextLevel();
        }
        if (fuel < 20 && ticks % 220 == 0) {
            say("Combustível baixo! Colete as células verdes.");
        }
        if (!stable && ticks % 300 == 0) {
            say(dist < targetRadius() ? "Órbita baixa: empurre para fora (WASD)."
                    : "Órbita alta: empurre para dentro (WASD).");
        }

        // destroços
        for (float[] rock : debris) {
            rock[1] += rock[2];
            float rx = (float) Math.cos(rock[1]) * rock[0];
            float ry = (float) Math.sin(rock[1]) * rock[0];
            if (Math.hypot(rx - px, ry - py) < rock[3] + 6) {
                hitDebris(rx, ry);
                break;
            }
        }

        // células de combustível
        if (ticks % 480 == 0 && cells.size() < 2) spawnCell();
        for (int index = cells.size() - 1; index >= 0; index--) {
            float[] cell = cells.get(index);
            cell[1] += 0.004F;
            cell[2] -= 1;
            float cx = (float) Math.cos(cell[1]) * cell[0];
            float cy = (float) Math.sin(cell[1]) * cell[0];
            if (Math.hypot(cx - px, cy - py) < 11) {
                fuel = Math.min(MAX_FUEL, fuel + 32);
                cells.remove(index);
                score += 25;
                say("Célula coletada! Combustível +32.");
                for (int spark = 0; spark < 8; spark++) {
                    sparks.add(new float[] {cx, cy, (random.nextFloat() - 0.5F) * 1.2F,
                            (random.nextFloat() - 0.5F) * 1.2F, 18});
                }
            } else if (cell[2] <= 0) {
                cells.remove(index);
            }
        }

        stationAngle = (stationAngle + 2) % 360;

        // limites
        if (dist < planetRadius() + 5) {
            fail("Reentrada na atmosfera! -1 vida.");
        } else if (dist > maxRadius() + 30) {
            fail("Você escapou da órbita! -1 vida.");
        }
    }

    private void nextLevel() {
        level++;
        ringTicks = 0;
        score += 150;
        if (level > MAX_LEVEL) {
            finished = true;
            best = Math.max(best, score);
            message = "Órbita perfeita! Missão concluída com " + score + " pontos.";
            messageTicks = 9999;
            return;
        }
        spawnDebris(1);
        fuel = Math.min(MAX_FUEL, fuel + 25);
        say("Fase " + level + "! Órbita-alvo: " + targetRadius() + " km. +25 de combustível.");
    }

    private void hitDebris(float rx, float ry) {
        if (flashTicks > 0) return;
        flashTicks = 45;
        fuel = Math.max(0, fuel - 6);
        float dx = px - rx;
        float dy = py - ry;
        float length = Math.max(1.0F, (float) Math.hypot(dx, dy));
        vx += dx / length * 0.4F;
        vy += dy / length * 0.4F;
        score = Math.max(0, score - 20);
        say("Impacto! Casco danificado e -6 de combustível.");
        for (int index = 0; index < 12; index++) {
            sparks.add(new float[] {px, py, (random.nextFloat() - 0.5F) * 1.6F,
                    (random.nextFloat() - 0.5F) * 1.6F, 20});
        }
    }

    private void fail(String reason) {
        lives--;
        destroyed = true;
        respawnTicks = 60;
        fuel = MAX_FUEL;
        message = reason;
        messageTicks = 200;
        for (int index = 0; index < 26; index++) {
            sparks.add(new float[] {px, py, (random.nextFloat() - 0.5F) * 2.4F,
                    (random.nextFloat() - 0.5F) * 2.4F, 26 + random.nextInt(16)});
        }
        trail.clear();
        if (lives <= 0) {
            finished = true;
            destroyed = false;
            best = Math.max(best, score);
            message = "Missão encerrada. Pontuação: " + score + "  •  Recorde: " + best;
            messageTicks = 9999;
        }
    }

    private void updateParticles() {
        for (int index = trail.size() - 1; index >= 0; index--) {
            float[] point = trail.get(index);
            point[2] -= 2;
            if (point[2] <= 0) trail.remove(index);
        }
        for (int index = sparks.size() - 1; index >= 0; index--) {
            float[] spark = sparks.get(index);
            spark[0] += spark[2];
            spark[1] += spark[3];
            spark[4] -= 1;
            if (spark[4] <= 0) sparks.remove(index);
        }
    }

    private void say(String text) {
        message = text;
        messageTicks = 170;
    }

    /* ------------------------------------------------------------------ */
    /* Entrada                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 87, 265 -> {
                thrustUp = true;
                return true;
            }
            case 83, 264 -> {
                thrustDown = true;
                return true;
            }
            case 65, 263 -> {
                thrustLeft = true;
                return true;
            }
            case 68, 262 -> {
                thrustRight = true;
                return true;
            }
            case 82 -> {
                restart();
                return true;
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 87, 265 -> thrustUp = false;
            case 83, 264 -> thrustDown = false;
            case 65, 263 -> thrustLeft = false;
            case 68, 262 -> thrustRight = false;
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

    /* ------------------------------------------------------------------ */
    /* Render                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawSpace(context);
        drawPlanet(context);
        drawOrbit(context);
        drawCells(context);
        drawDebris(context);
        drawTrail(context);
        if (!destroyed && !finished) drawShip(context);
        drawSparks(context);
        if (flashTicks > 0) {
            context.fill(0, 0, width, height, (Math.min(60, flashTicks * 3) << 24) | 0xFF6E6E);
        }
        drawHud(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawSpace(DrawContext context) {
        context.fillGradient(0, 0, width, height, 0xFF08060F, 0xFF161A33);
        context.fillGradient(0, 0, width, height / 2, 0x222A6EE0, 0x00000000);
        context.fillGradient(width / 2, height / 2, width, height, 0x22F0509B, 0x00000000);
        for (int index = 0; index < 90; index++) {
            int hash = index * 7919 + 13;
            int x = Math.floorMod(hash * 31 + 7, Math.max(1, width));
            int y = Math.floorMod(hash * 17 + 3, Math.max(1, height));
            int twinkle = (int) (120 + 90 * Math.sin((ticks + index * 7) / 15.0));
            int size = index % 13 == 0 ? 2 : 1;
            context.fill(x, y, x + size, y + size, (twinkle << 24) | 0xDFE9FF);
        }
    }

    private void drawPlanet(DrawContext context) {
        int cx = centerX();
        int cy = centerY();
        int radius = planetRadius();
        ArcadeTheme.disc(context, cx, cy, radius + 5, 0x2235C8B9);
        ArcadeTheme.disc(context, cx, cy, radius + 2, 0x4435C8B9);
        for (int row = -radius; row <= radius; row++) {
            int half = (int) Math.sqrt(Math.max(0, radius * radius - row * row));
            int color = row < -radius / 3 ? 0xFF3C8F7E : row < radius / 4 ? 0xFF2F7A6A : 0xFF235F55;
            context.fill(cx - half, cy + row, cx + half, cy + row + 1, color);
        }
        // continentes simplificados
        ArcadeTheme.disc(context, cx - radius / 3, cy - radius / 4, Math.max(3, radius / 4), 0xFF4FA37E);
        ArcadeTheme.disc(context, cx + radius / 4, cy + radius / 6, Math.max(2, radius / 5), 0xFF479A74);
        ArcadeTheme.disc(context, cx - radius / 6, cy + radius / 3, Math.max(2, radius / 7), 0xFF54AA84);
        ArcadeTheme.disc(context, cx + radius / 5, cy - radius / 3, Math.max(2, radius / 8), 0xFF4FA37E);
        // terminador (luz vindo da esquerda)
        for (int row = -radius; row <= radius; row++) {
            int half = (int) Math.sqrt(Math.max(0, radius * radius - row * row));
            context.fill(cx, cy + row, cx + half, cy + row + 1, 0x30000000);
        }
        // brilho da atmosfera no limbo
        context.fill(cx - radius / 2, cy - radius - 1, cx + radius / 2, cy - radius, 0x665FE0D0);
    }

    private void drawOrbit(DrawContext context) {
        int cx = centerX();
        int cy = centerY();
        int target = targetRadius();
        int steps = 110;
        int color = stable ? 0xCC6EE58C : 0x55708AA8;
        for (int index = 0; index < steps; index++) {
            if (index % 7 > 3) continue;
            double angle = index * 2 * Math.PI / steps;
            int x = cx + (int) Math.round(Math.cos(angle) * target);
            int y = cy + (int) Math.round(Math.sin(angle) * target);
            context.fill(x, y, x + 2, y + 2, color);
        }
        // zona de perigo junto à atmosfera
        for (int index = 0; index < 64; index++) {
            if (index % 2 == 0) continue;
            double angle = index * 2 * Math.PI / 64;
            int x = cx + (int) Math.round(Math.cos(angle) * (planetRadius() + 12));
            int y = cy + (int) Math.round(Math.sin(angle) * (planetRadius() + 12));
            context.fill(x, y, x + 1, y + 1, 0x44FF6E6E);
        }
        // estação de pesquisa na órbita-alvo
        double angle = Math.toRadians(stationAngle);
        int sx = cx + (int) Math.round(Math.cos(angle) * target);
        int sy = cy + (int) Math.round(Math.sin(angle) * target);
        context.fill(sx - 4, sy - 2, sx + 4, sy + 2, 0xFF9DB2CC);
        context.fill(sx - 1, sy - 4, sx + 1, sy + 4, 0xFFC9D8F0);
        context.fill(sx + 4, sy - 1, sx + 6, sy + 1, 0xFFFFD66B);
    }

    private void drawCells(DrawContext context) {
        for (float[] cell : cells) {
            int x = centerX() + (int) Math.round(Math.cos(cell[1]) * cell[0]);
            int y = centerY() + (int) Math.round(Math.sin(cell[1]) * cell[0]);
            int pulse = (ticks / 6) % 2 == 0 ? 1 : 0;
            ArcadeTheme.disc(context, x, y, 5 + pulse, 0x446EE58C);
            context.fill(x - 3, y - 4, x + 3, y + 4, 0xFFB9F26B);
            context.fill(x - 1, y - 6, x + 1, y - 4, 0xFF6EE58C);
            context.fill(x - 1, y + 4, x + 1, y + 6, 0xFF6EE58C);
            context.fill(x - 1, y - 2, x + 1, y + 2, 0xFF2C6B3E);
        }
    }

    private void drawDebris(DrawContext context) {
        for (float[] rock : debris) {
            int x = centerX() + (int) Math.round(Math.cos(rock[1]) * rock[0]);
            int y = centerY() + (int) Math.round(Math.sin(rock[1]) * rock[0]);
            int radius = (int) rock[3];
            ArcadeTheme.disc(context, x, y, radius, 0xFF6B6A78);
            ArcadeTheme.disc(context, x - 1, y - 1, Math.max(1, radius - 2), 0xFF8C8A9C);
            context.fill(x + 1, y, x + 2, y + 2, 0xFF4A4956);
            context.fill(x - radius - 2, y - 1, x - radius, y, 0xFF4A4956);
        }
    }

    private void drawTrail(DrawContext context) {
        for (float[] point : trail) {
            int x = centerX() + Math.round(point[0]);
            int y = centerY() + Math.round(point[1]);
            int alpha = (int) Math.min(110, point[2] * 5);
            context.fill(x, y, x + 1, y + 1, (alpha << 24) | 0x9FD8FF);
        }
    }

    private void drawShip(DrawContext context) {
        int sx = centerX() + Math.round(px);
        int sy = centerY() + Math.round(py);
        float speed = (float) Math.hypot(vx, vy);
        float angle = speed > 0.05F ? (float) Math.atan2(vy, vx) : -1.5708F;
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        boolean thrusting = thrustLeft || thrustRight || thrustUp || thrustDown;
        if (thrusting && fuel > 0) {
            float fx = sx - cos * 9;
            float fy = sy - sin * 9;
            int flame = (ticks / 3) % 2 == 0 ? 0xFFFF8A2B : 0xFFFFD66B;
            context.fill((int) fx - 1, (int) fy - 1, (int) fx + 2, (int) fy + 2, flame);
            context.fill((int) (fx - cos * 2) - 1, (int) (fy - sin * 2) - 1,
                    (int) (fx - cos * 2) + 2, (int) (fy - sin * 2) + 2, 0x88FFF0B8);
        }
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = -8; dy <= 8; dy++) {
                float lx = dx * cos + dy * sin;
                float ly = -dx * sin + dy * cos;
                float half = (6.0F - lx) * 0.5F;
                if (lx > -6 && lx < 6 && Math.abs(ly) <= half) {
                    context.fill(sx + dx, sy + dy, sx + dx + 1, sy + dy + 1, 0xFFDCE8F6);
                }
            }
        }
        context.fill(sx - 1, sy - 1, sx + 2, sy + 2, 0xFF5FA9D6);
        if (stable) {
            int pulse = (ticks / 4) % 3;
            ArcadeTheme.ring(context, sx, sy, 12 + pulse, 1, 0x886EE58C);
        }
    }

    private void drawSparks(DrawContext context) {
        for (float[] spark : sparks) {
            int x = centerX() + Math.round(spark[0]);
            int y = centerY() + Math.round(spark[1]);
            int alpha = (int) Math.min(200, spark[4] * 8);
            context.fill(x, y, x + 1, y + 1, (alpha << 24) | 0xFFD66B);
        }
    }

    private void drawHud(DrawContext context) {
        context.fill(0, 0, width, 22, 0xE00A1220);
        ArcadeTheme.pill(context, textRenderer, "PONTOS " + score, 4, 4, ArcadeTheme.GOLD);
        int pillWidth = textRenderer.getWidth("PONTOS " + score) + 18;
        ArcadeTheme.pill(context, textRenderer, "FASE " + Math.min(level, MAX_LEVEL) + "/" + MAX_LEVEL,
                10 + pillWidth, 4, ArcadeTheme.LIME);
        for (int life = 0; life < 3; life++) {
            int color = life < lives ? 0xFFFF6E6E : 0xFF3A4A60;
            ArcadeTheme.disc(context, width - 18 - life * 12, 11, 4, color);
        }
        // progresso da fase em uma faixa fina sob o cabeçalho
        context.fill(0, 22, width, 24, 0x55000000);
        int progress = (int) (width * Math.min(1.0F, ringTicks / (float) RING_TARGET));
        context.fill(0, 22, progress, 24, 0xFF6CC9F5);

        // faixa compacta de instrumentos
        context.fill(0, 24, width, 42, 0xD90A1220);
        context.fill(0, 41, width, 42, 0x33FFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("COMB"), 8, 29, ArcadeTheme.MUTED);
        ArcadeTheme.progress(context, 40, 30, 46, 6, fuel / MAX_FUEL,
                fuel < 25 ? ArcadeTheme.DANGER : ArcadeTheme.GOLD);
        context.drawTextWithShadow(textRenderer, Text.literal(Math.round(fuel) + "%"),
                90, 29, fuel < 25 ? ArcadeTheme.DANGER : ArcadeTheme.TEXT);
        int orbitX = 122;
        int target = targetRadius();
        context.drawTextWithShadow(textRenderer,
                Text.literal("ÓRBITA " + Math.round(distance()) + "/" + target),
                orbitX, 29, stable ? ArcadeTheme.LIME : ArcadeTheme.TEXT);
        String status = stable ? "ESTÁVEL" : distance() < target ? "SUBA" : "BAIXE";
        int statusColor = stable ? ArcadeTheme.LIME : ArcadeTheme.CORAL;
        context.drawTextWithShadow(textRenderer, Text.literal(status),
                width - 8 - textRenderer.getWidth(status), 29, statusColor);

        String controls = "[WASD] propulsão   [R] reinicia";
        context.drawTextWithShadow(textRenderer, Text.literal(controls), 8, height - 38,
                ArcadeTheme.DIM);

        if (messageTicks > 0 && !message.isEmpty()) {
            String text = textRenderer.trimToWidth(message, width - 24);
            int textWidth = textRenderer.getWidth(text);
            ArcadeTheme.rounded(context, width / 2 - textWidth / 2 - 8, 50,
                    width / 2 + textWidth / 2 + 8, 66, 6, 0xE00A1220);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(text), width / 2, 55,
                    finished ? ArcadeTheme.GOLD : ArcadeTheme.TEXT);
        }
        if (finished) {
            String hint = "R para jogar de novo";
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(hint), width / 2,
                    height / 2 + 34, ArcadeTheme.MUTED);
        }
    }

}
