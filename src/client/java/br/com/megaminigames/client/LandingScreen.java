package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Pouso de Precisão — simulação de pouso com física própria.
 *
 * <p>O módulo desce sob gravidade, gasta combustível para frear e precisa
 * encostar numa das plataformas com velocidade vertical e horizontal baixas.
 * O terreno, as plataformas, o vento e a gravidade mudam a cada fase; a
 * câmera acompanha o módulo num mundo maior que a tela.</p>
 */
public final class LandingScreen extends ArcadeScreen {
    private static final int WORLD_WIDTH = 2200;
    private static final int GROUND_STEP = 4;
    private static final int COLUMNS = WORLD_WIDTH / GROUND_STEP;
    private static final int MAX_LEVEL = 5;
    private static final float SAFE_VY = 2.1F;
    private static final float SAFE_VX = 1.05F;

    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final int[] ground = new int[COLUMNS];
    private final List<int[]> pads = new ArrayList<>();
    private final List<float[]> particles = new ArrayList<>();
    private final List<float[]> stars = new ArrayList<>();
    private final List<float[]> trail = new ArrayList<>();

    private float shipX;
    private float shipY;
    private float velocityX;
    private float velocityY;
    private float fuel;
    private float maxFuel;
    private float gravity;
    private float wind;
    private float windMax;
    private float windPhase;
    private int tickInLevel;
    private float cameraX;
    private int level;
    private int score;
    private int lives;
    private int ticks;
    private int messageTicks;
    private int landingTicks;
    private int explosionTicks;
    private boolean thrusting;
    private boolean leftHeld;
    private boolean rightHeld;
    private boolean landed;
    private boolean crashed;
    private boolean finished;
    private String message = "";
    private int bestScore;

    public LandingScreen() {
        super("Pouso de Precisão");
    }

    @Override
    protected void init() {
        addFooterButtons(height - 26, this::restart);
        if (stars.isEmpty()) {
            for (int index = 0; index < 70; index++) {
                stars.add(new float[] {random.nextFloat() * 400, random.nextFloat() * 150,
                        0.2F + random.nextFloat() * 0.8F});
            }
        }
        if (ground[0] == 0) restart();
        cameraX = Math.max(0, Math.min(WORLD_WIDTH - width, shipX - width / 2.0F));
    }

    private void restart() {
        level = 1;
        score = 0;
        lives = 3;
        finished = false;
        startLevel();
    }

    private void startLevel() {
        // primeiro escolhe onde ficam as plataformas; o terreno é achatado nelas
        int[] centers = new int[3];
        int[] multipliers = {2, 3, 4};
        for (int index = 0; index < 3; index++) {
            centers[index] = 320 + index * (WORLD_WIDTH - 760) / 2 + random.nextInt(60);
        }
        buildTerrain(centers);
        pads.clear();
        for (int index = 0; index < 3; index++) {
            int half = Math.max(24, 42 - level * 3 + (index == 2 ? -6 : 0));
            int height = terrainAt(centers[index]);
            pads.add(new int[] {centers[index] - half, centers[index] + half, height, multipliers[index]});
        }
        shipX = centers[0];
        shipY = 34;
        velocityX = 0;
        velocityY = 0;
        maxFuel = 62 + level * 4;
        fuel = maxFuel;
        gravity = 0.026F + level * 0.003F;
        windPhase = random.nextFloat() * 6.283F;
        windMax = level >= 2 ? 0.0035F * Math.min(6, level) : 0;
        landed = false;
        crashed = false;
        thrusting = false;
        landingTicks = 0;
        explosionTicks = 0;
        tickInLevel = 0;
        message = "Fase " + level + ": pouse numa plataforma verde (x2 vale mais).";
        messageTicks = 150;
        cameraX = Math.max(0, Math.min(WORLD_WIDTH - width, shipX - width / 2.0F));
        particles.clear();
        trail.clear();
    }

    private void buildTerrain(int[] centers) {
        float roughness = 8 + level * 2;
        float base = 0;
        for (int column = 0; column < COLUMNS; column++) {
            base += (random.nextFloat() - 0.5F) * roughness;
            base *= 0.86F;
            double wave = Math.sin(column * 0.045 + level) * (10 + level * 2)
                    + Math.sin(column * 0.013) * 14;
            ground[column] = Math.max(6, Math.min(64, (int) (30 + wave + base)));
        }
        // achata o terreno em cada plataforma
        for (int center : centers) {
            int middle = Math.max(0, Math.min(COLUMNS - 1, center / GROUND_STEP));
            int flat = ground[middle];
            int start = Math.max(0, middle - 20);
            int finish = Math.min(COLUMNS - 1, middle + 20);
            for (int column = start; column <= finish; column++) {
                ground[column] = flat;
            }
        }
    }

    /** Altura do terreno em pixels do mundo, a partir do chão da tela. */
    private int terrainAt(int worldX) {
        int column = Math.max(0, Math.min(COLUMNS - 1, worldX / GROUND_STEP));
        return ground[column];
    }

    @Override
    public void tick() {
        ticks++;
        if (messageTicks > 0) messageTicks--;
        tickInLevel++;
        wind = windMax * (float) Math.sin(tickInLevel * 0.016 + windPhase);
        updateParticles();
        if (finished) return;
        if (landed) {
            landingTicks++;
            if (landingTicks > 70) nextLevel();
            return;
        }
        if (crashed) {
            explosionTicks++;
            if (explosionTicks > 60) {
                if (lives <= 0) {
                    finished = true;
                    message = "Missão encerrada. Pontuação: " + score;
                    messageTicks = 9999;
                } else {
                    startLevel();
                }
            }
            return;
        }

        // física do módulo
        float accelX = 0;
        if (leftHeld) accelX -= 0.085F;
        if (rightHeld) accelX += 0.085F;
        velocityX = (velocityX + accelX + wind) * 0.955F;
        velocityX = Math.max(-2.2F, Math.min(2.2F, velocityX));

        float accelY = gravity;
        thrusting = keyThrust && fuel > 0;
        if (thrusting) {
            accelY -= 0.115F;
            fuel = Math.max(0, fuel - 0.30F);
            spawnExhaust();
        }
        if (leftHeld || rightHeld) fuel = Math.max(0, fuel - 0.06F);
        velocityY += accelY;
        velocityY = Math.max(-2.2F, Math.min(3.4F, velocityY));

        shipX += velocityX;
        shipY += velocityY;
        shipX = Math.max(6, Math.min(WORLD_WIDTH - 6, shipX));
        if (ticks % 3 == 0) {
            trail.add(new float[] {shipX, shipY, 40});
        }
        for (int index = trail.size() - 1; index >= 0; index--) {
            float[] point = trail.get(index);
            point[2] -= 2;
            if (point[2] <= 0) trail.remove(index);
        }
        cameraX += (Math.max(0, Math.min(WORLD_WIDTH - width, shipX - width / 2.0F)) - cameraX) * 0.12F;
        cameraX = Math.max(0, Math.min(WORLD_WIDTH - width, cameraX));

        checkTouchdown();
    }

    private boolean keyThrust;

    private void checkTouchdown() {
        float groundY = height - 58 - terrainAt((int) shipX);
        if (shipY < groundY - 26) return;
        if (velocityY < SAFE_VY && Math.abs(velocityX) < SAFE_VX) {
            int[] pad = padAt((int) shipX);
            if (pad != null) {
                score += Math.round((70 + fuel * 3) * pad[3] / 2.0F);
                message = "Pousando na plataforma x" + pad[3] + "! +"
                        + Math.round((70 + fuel * 3) * pad[3] / 2.0F) + " pontos";
            } else {
                score += Math.round(20 + fuel);
                message = "Pouso suave fora da plataforma. +" + Math.round(20 + fuel) + " pontos";
            }
            landed = true;
            landingTicks = 0;
            messageTicks = 200;
            for (int index = 0; index < 26; index++) {
                particles.add(new float[] {shipX + (random.nextFloat() - 0.5F) * 30,
                        (float) (groundY - 2), (random.nextFloat() - 0.5F) * 1.5F,
                        -random.nextFloat() * 0.8F, 34, 0xFFB9A98A});
            }
        } else {
            crashesInto(groundY);
        }
    }

    private void crashesInto(float groundY) {
        crashed = true;
        landed = false;
        lives--;
        explosionTicks = 0;
        message = velocityY >= SAFE_VY ? "Batida forte! Reduza a velocidade de descida."
                : "Derrapou na horizontal. Chegue mais devagar.";
        messageTicks = 200;
        for (int index = 0; index < 40; index++) {
            particles.add(new float[] {shipX + (random.nextFloat() - 0.5F) * 16, groundY - 6,
                    (random.nextFloat() - 0.5F) * 3.2F, -random.nextFloat() * 2.4F,
                    30 + random.nextInt(20), index % 2 == 0 ? 0xFFFF8A2B : 0xFFFFD66B});
        }
    }

    private int[] padAt(int worldX) {
        for (int[] pad : pads) {
            if (worldX >= pad[0] && worldX <= pad[1]) return pad;
        }
        return null;
    }

    private void nextLevel() {
        if (level >= MAX_LEVEL) {
            finished = true;
            bestScore = Math.max(bestScore, score);
            message = "Todas as fases concluídas! Pontuação final: " + score;
            messageTicks = 9999;
            return;
        }
        level++;
        startLevel();
    }

    private void spawnExhaust() {
        if (random.nextInt(100) > 55) return;
        particles.add(new float[] {shipX + (random.nextFloat() - 0.5F) * 5, shipY + 13,
                (random.nextFloat() - 0.5F) * 0.5F, 1.1F + random.nextFloat() * 0.6F,
                14 + random.nextInt(8), random.nextBoolean() ? 0xFFFFD66B : 0xFFFF8A2B});
    }

    private void updateParticles() {
        for (int index = particles.size() - 1; index >= 0; index--) {
            float[] particle = particles.get(index);
            particle[0] += particle[2];
            particle[1] += particle[3];
            particle[3] += 0.03F;
            particle[4] -= 1;
            if (particle[4] <= 0) particles.remove(index);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 87, 32, 265 -> {
                keyThrust = true;
                return true;
            }
            case 65, 263 -> {
                leftHeld = true;
                return true;
            }
            case 68, 262 -> {
                rightHeld = true;
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
            case 87, 32, 265 -> {
                keyThrust = false;
                return true;
            }
            case 65, 263 -> {
                leftHeld = false;
                return true;
            }
            case 68, 262 -> {
                rightHeld = false;
                return true;
            }
            default -> {
                return super.keyReleased(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawSky(context);
        drawHills(context);
        drawTerrain(context);
        drawShadow(context);
        drawPads(context);
        drawTrail(context);
        drawShip(context);
        drawParticles(context);
        drawHud(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawSky(DrawContext context) {
        int top = 0xFFFF3355;
        int bottom = 0xFF1B2A4A;
        if (level == 1) {
            top = 0xFF20365C;
            bottom = 0xFFFF8A65;
        } else if (level == 2) {
            top = 0xFF10203C;
            bottom = 0xFF6A4A7A;
        } else if (level >= 3) {
            top = 0xFF070B18;
            bottom = 0xFF1B2A4A;
        }
        context.fillGradient(0, 0, width, height - 50, top, bottom);
        for (float[] star : stars) {
            float parallax = cameraX * (0.08F + star[2] * 0.08F);
            int x = (int) ((star[0] - parallax) % 400);
            if (x < 0) x += 400;
            int y = (int) (star[1] + (1 - star[2]) * 20);
            if (x < width && y < height - 70) {
                context.fill(x, y, x + 1, y + 1, star[2] > 0.6F ? 0xFFFFF3C4 : 0x99FFFFFF);
            }
        }
        // planeta distante
        int planetX = (int) (width * 0.17 - cameraX * 0.02F);
        ArcadeTheme.disc(context, planetX, 62, 18, 0x44C9D8F0);
        ArcadeTheme.disc(context, planetX, 62, 14, 0xBBC9D8F0);
        context.fill(planetX - 9, 60, planetX + 2, 62, 0x44FFFFFF);
        context.fill(planetX - 16, 69, planetX + 13, 71, 0x33A8B8D0);
        // halo no horizonte
        context.fillGradient(0, height - 130, width, height - 50, 0x00FFB080, 0x33FFB080);
    }

    /** Serras distantes com parallax, para dar profundidade ao cenário. */
    private void drawHills(DrawContext context) {
        int baseY = height - 58;
        int shift = (int) (cameraX * 0.35F);
        for (int x = 0; x < width; x += 2) {
            int world = x + shift;
            int ridge = (int) (15 + 9 * Math.sin(world * 0.0062F)
                    + 5 * Math.sin(world * 0.017F + 1.4F));
            int y = baseY - ridge;
            context.fill(x, y, x + 2, baseY, 0xFF4C4766);
            context.fill(x, y, x + 2, y + 2, 0xFF6A6388);
        }
        int shift2 = (int) (cameraX * 0.55F);
        for (int x = 0; x < width; x += 3) {
            int world = x + shift2;
            int ridge = (int) (7 + 6 * Math.sin(world * 0.011F + 2.2F));
            int y = baseY - ridge;
            context.fill(x, y, x + 3, baseY, 0xFF3A3552);
        }
    }

    private void drawTerrain(DrawContext context) {
        int baseY = height - 58;
        int startColumn = Math.max(0, (int) cameraX / GROUND_STEP - 1);
        int endColumn = Math.min(COLUMNS - 1, (int) (cameraX + width) / GROUND_STEP + 1);
        for (int column = startColumn; column <= endColumn; column++) {
            int x = column * GROUND_STEP - (int) cameraX;
            int heightLocal = ground[column];
            int y = baseY - heightLocal;
            context.fill(x, y, x + GROUND_STEP, baseY + 70, 0xFF565463);
            context.fill(x, y, x + GROUND_STEP, y + 4, 0xFF8C8A9C);
            context.fill(x, y + 4, x + GROUND_STEP, y + 7, 0xFF6E6C7E);
            int hash = Math.floorMod(column * 92821, 97);
            if (hash < 22 && heightLocal > 18) {
                context.fill(x + 1, y + 9 + hash % 7, x + 3, y + 11 + hash % 7, 0xFF454353);
            }
            if (hash > 88) {
                context.fill(x + 1, y + 1, x + 2, y + 3, 0xFFA9A7B8);
            }
        }
        context.fill(0, baseY + 3, width, height, 0xFF3E3D4A);
        context.fill(0, baseY + 3, width, baseY + 4, 0xFF5A5866);
    }

    /** Sombra e linha de mira: ajudam a julgar onde o módulo vai tocar o solo. */
    private void drawShadow(DrawContext context) {
        if (landed || crashed) return;
        int groundY = height - 58 - terrainAt((int) shipX);
        int x = (int) (shipX - cameraX);
        int altitude = Math.max(0, Math.round(groundY - shipY));
        int half = Math.max(3, 12 - altitude / 26);
        context.fill(x - half, groundY - 2, x + half, groundY, 0x55000000);
        for (int y = (int) shipY + 16; y < groundY - 4; y += 7) {
            context.fill(x, y, x + 1, y + 3, 0x26FFFFFF);
        }
    }

    private void drawPads(DrawContext context) {
        int baseY = height - 58;
        for (int[] pad : pads) {
            int x1 = pad[0] - (int) cameraX;
            int x2 = pad[1] - (int) cameraX;
            if (x2 < -40 || x1 > width + 40) continue;
            int y = baseY - pad[2];
            // estrutura do píer
            context.fill(x1, y - 1, x2, y + 5, 0xFF2C5C3E);
            context.fill(x1 + 2, y, x2 - 2, y + 3, 0xFF5BC77E);
            context.fill(x1 + 2, y, x2 - 2, y + 1, 0xFF8CF0AE);
            // faixas de pouso
            for (int stripe = x1 + 6; stripe < x2 - 4; stripe += 12) {
                context.fill(stripe, y + 1, stripe + 6, y + 2, 0xFF2C5C3E);
            }
            // luzes nas pontas
            boolean blink = (ticks / 8) % 2 == 0;
            context.fill(x1 + 1, y - 3, x1 + 4, y - 1, blink ? 0xFFFFF3C4 : 0xFF3E8A55);
            context.fill(x2 - 4, y - 3, x2 - 1, y - 1, blink ? 0xFF3E8A55 : 0xFFFFF3C4);
            int centerPad = (x1 + x2) / 2;
            // baliza central
            context.fill(centerPad - 1, y - 18, centerPad + 1, y - 4, 0xFF6EE58C);
            context.fill(centerPad - 4, y - 16, centerPad + 4, y - 14, 0xFF6EE58C);
            String label = "x" + pad[3];
            int labelWidth = textRenderer.getWidth(label) + 8;
            ArcadeTheme.rounded(context, centerPad - labelWidth / 2, y - 32,
                    centerPad + labelWidth / 2, y - 20, 5, 0xCC0A1220);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(label),
                    centerPad, y - 29, 0xFF8CF0AE);
        }
    }

    private void drawTrail(DrawContext context) {
        for (float[] point : trail) {
            int x = (int) (point[0] - cameraX);
            int y = (int) point[1];
            int alpha = (int) Math.min(120, point[2] * 3);
            context.fill(x, y, x + 2, y + 2, (alpha << 24) | 0x9FD8FF);
        }
    }

    private void drawShip(DrawContext context) {
        if (crashed && explosionTicks > 0) return;
        int x = (int) (shipX - cameraX);
        int y = (int) shipY;
        int body = landed ? 0xFF8FA6C4 : 0xFFD7E3F2;
        if (thrusting) {
            int flame = (ticks / 3) % 2 == 0 ? 0xFFFF8A2B : 0xFFFFD66B;
            context.fill(x - 3, y + 12, x + 3, y + 18, flame);
            context.fill(x - 2, y + 12, x + 2, y + 22, 0xFFFFF0B8);
        }
        ArcadeTheme.rounded(context, x - 7, y - 2, x + 7, y + 10, 2, body);
        ArcadeTheme.rounded(context, x - 5, y + 1, x + 5, y + 6, 2, 0xFF5FA9D6);
        context.fill(x - 7, y + 10, x - 4, y + 13, 0xFF8FA6C4);
        context.fill(x + 4, y + 10, x + 7, y + 13, 0xFF8FA6C4);
        context.fill(x - 1, y - 5, x + 1, y - 1, 0xFF8FA6C4);
        if (!landed) {
            ArcadeTheme.disc(context, x, y - 6, 2, (ticks / 5) % 2 == 0 ? 0xFFFF6E6E : 0xFF8A2B2B);
        }
    }

    private void drawParticles(DrawContext context) {
        for (float[] particle : particles) {
            int x = (int) (particle[0] - cameraX);
            int y = (int) particle[1];
            int life = (int) particle[4];
            int size = life > 20 ? 3 : 2;
            context.fill(x, y, x + size, y + size, (int) particle[5]);
        }
    }

    private void drawHud(DrawContext context) {
        context.fill(0, 0, width, 22, 0xE00A1220);
        ArcadeTheme.pill(context, textRenderer, "FASE " + level + "/" + MAX_LEVEL, 4, 3, ArcadeTheme.LIME);
        ArcadeTheme.pill(context, textRenderer, "PONTOS " + score, 92, 3, ArcadeTheme.GOLD);
        for (int life = 0; life < lives; life++) {
            ArcadeTheme.disc(context, width - 44 + life * 12, 11, 4, 0xFFFF6E6E);
        }
        int panelX = width - 108;
        int panelY = 50;
        ArcadeTheme.rounded(context, panelX - 6, panelY - 6, width - 6, panelY + 52, 6, 0xCC0A1220);
        drawMeter(context, panelX, panelY, 62, "COMBUSTÍVEL", fuel / maxFuel, ArcadeTheme.GOLD);
        String vertical = String.format("%.1f", velocityY);
        String horizontal = String.format("%.1f", Math.abs(velocityX));
        int altitude = Math.round((height - 58 - terrainAt((int) shipX)) - shipY);
        context.drawTextWithShadow(textRenderer, Text.literal("DESCIDA " + vertical),
                panelX, panelY + 22, velocityY > SAFE_VY ? ArcadeTheme.DANGER : ArcadeTheme.SUCCESS);
        context.drawTextWithShadow(textRenderer, Text.literal("LATERAL " + horizontal),
                panelX, panelY + 32,
                Math.abs(velocityX) > SAFE_VX ? ArcadeTheme.DANGER : ArcadeTheme.SUCCESS);
        context.drawTextWithShadow(textRenderer,
                Text.literal("ALT " + Math.max(0, altitude) + "  VENTO "
                        + (wind == 0 ? "0" : (wind > 0 ? "→" : "←") + String.format("%.1f", Math.abs(wind) * 40))),
                panelX, panelY + 42, Math.abs(wind) > 0.012F ? ArcadeTheme.CORAL : ArcadeTheme.MUTED);

        String controls = "[W/Espaço] acelera  [A/D] lateral  [R] reinicia";
        context.drawTextWithShadow(textRenderer, Text.literal(controls), 8, height - 38,
                ArcadeTheme.DIM);
        if (landed && landingTicks < 40) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("POUSO LIMPO"),
                    width / 2, height / 2, ArcadeTheme.SUCCESS);
        }
        if (messageTicks > 0 && !message.isEmpty()) {
            int toastRight = width - 122;
            String text = textRenderer.trimToWidth(message, toastRight - 18);
            int textWidth = textRenderer.getWidth(text);
            int centerX = toastRight / 2;
            ArcadeTheme.rounded(context, centerX - textWidth / 2 - 8, 28,
                    centerX + textWidth / 2 + 8, 44, 6, 0xE00A1220);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(text), centerX, 33,
                    finished ? ArcadeTheme.GOLD : ArcadeTheme.TEXT);
        }
    }

    private void drawMeter(DrawContext context, int x, int y, int width, String label, float value, int accent) {
        context.drawTextWithShadow(textRenderer, Text.literal(label), x, y, ArcadeTheme.MUTED);
        ArcadeTheme.progress(context, x, y + 10, width, 7, value, value < 0.2F ? ArcadeTheme.DANGER : accent);
        context.drawTextWithShadow(textRenderer, Text.literal(Math.round(value * 100) + "%"),
                x + width + 6, y + 9, value < 0.2F ? ArcadeTheme.DANGER : ArcadeTheme.TEXT);
    }
}
