package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Quebra-blocos: rebata a bola com a raquete e destrua todos os tijolos. */
public final class BreakoutScreen extends Screen {
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int BRICK_HEIGHT = 16;
    private static final int BRICK_GAP = 4;
    private static final int BALL = 9;
    private static final int TRAIL = 7;
    private static final int[] BRICK_COLORS = {
            0xFFF0509B, 0xFFFF8A65, 0xFFFFD66B, 0xFFB9F26B, 0xFF35C8B9
    };
    private final boolean[][] bricks = new boolean[ROWS][COLUMNS];
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private final double[] trailX = new double[TRAIL];
    private final double[] trailY = new double[TRAIL];

    private int arenaLeft;
    private int arenaTop;
    private int arenaWidth;
    private int arenaHeight;
    private int brickWidth;
    private int brickStartX;
    private int brickStartY;
    private double paddleX;
    private double ballX;
    private double ballY;
    private double velocityX;
    private double velocityY;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean launched;
    private boolean gameOver;
    private boolean won;
    private boolean initialized;
    private int lives;
    private int score;
    private int bricksLeft;
    private int flashTicks;

    public BreakoutScreen() {
        super(Text.literal("Quebra-Blocos"));
    }

    @Override
    protected void init() {
        arenaWidth = Math.min(500, width - 40);
        arenaHeight = Math.min(290, height - 156);
        arenaLeft = (width - arenaWidth) / 2;
        arenaTop = Math.max(74, (height - arenaHeight - 104) / 2 + 32);
        brickWidth = (arenaWidth - 24 - (COLUMNS - 1) * BRICK_GAP) / COLUMNS;
        brickStartX = (arenaWidth - (COLUMNS * brickWidth + (COLUMNS - 1) * BRICK_GAP)) / 2;
        brickStartY = 26;
        int buttonY = Math.min(height - 24, arenaTop + arenaHeight + 16);
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
        for (boolean[] row : bricks) {
            Arrays.fill(row, true);
        }
        bricksLeft = ROWS * COLUMNS;
        lives = 3;
        score = 0;
        flashTicks = 0;
        particles.clear();
        gameOver = false;
        won = false;
        initialized = true;
        paddleX = arenaWidth / 2.0;
        launchBall();
    }

    private void launchBall() {
        launched = false;
        ballX = paddleX;
        ballY = arenaHeight - 38;
        velocityX = 2.6;
        velocityY = -3.6;
        for (int index = 0; index < TRAIL; index++) {
            trailX[index] = ballX;
            trailY[index] = ballY;
        }
    }

    @Override
    public void tick() {
        if (flashTicks > 0) {
            flashTicks--;
        }
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.x += particle.vx;
            particle.y += particle.vy;
            particle.vy += 0.18;
            particle.life--;
            if (particle.life <= 0) {
                iterator.remove();
            }
        }
        if (gameOver) {
            return;
        }
        if (leftPressed) {
            paddleX -= 6;
        }
        if (rightPressed) {
            paddleX += 6;
        }
        paddleX = clamp(paddleX, 36, arenaWidth - 36);
        if (!launched) {
            ballX = paddleX;
            ballY = arenaHeight - 38;
            return;
        }

        for (int index = TRAIL - 1; index > 0; index--) {
            trailX[index] = trailX[index - 1];
            trailY[index] = trailY[index - 1];
        }
        trailX[0] = ballX;
        trailY[0] = ballY;

        ballX += velocityX;
        ballY += velocityY;
        if (ballX <= BALL || ballX >= arenaWidth - BALL) {
            velocityX *= -1;
            ballX = clamp(ballX, BALL, arenaWidth - BALL);
            spawnParticles(ballX, ballY, ArcadeTheme.SKY, 4);
        }
        if (ballY <= BALL) {
            velocityY = Math.abs(velocityY);
            ballY = BALL;
        }

        double paddleY = arenaHeight - 27;
        if (velocityY > 0 && ballY + BALL >= paddleY - 6
                && ballY <= paddleY + 8 && Math.abs(ballX - paddleX) <= 42) {
            double offset = (ballX - paddleX) / 42.0;
            velocityX = offset * 5.0;
            velocityY = -Math.max(2.6, Math.abs(velocityY));
            ballY = paddleY - BALL;
            spawnParticles(ballX, ballY, ArcadeTheme.TEAL, 6);
        }

        hitBrick();
        if (ballY > arenaHeight + BALL) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
                spawnParticles(ballX, arenaHeight - 4, ArcadeTheme.DANGER, 16);
            } else {
                launchBall();
            }
        }
        if (bricksLeft == 0) {
            won = true;
            gameOver = true;
        }
    }

    private void spawnParticles(double x, double y, int color, int amount) {
        if (particles.size() > 90) {
            return;
        }
        for (int index = 0; index < amount; index++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 0.6 + random.nextDouble() * 2.2;
            particles.add(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    14 + random.nextInt(14), color));
        }
    }

    private void hitBrick() {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (!bricks[row][column]) {
                    continue;
                }
                int x = brickStartX + column * (brickWidth + BRICK_GAP);
                int y = brickStartY + row * (BRICK_HEIGHT + BRICK_GAP);
                if (ballX + BALL >= x && ballX - BALL <= x + brickWidth
                        && ballY + BALL >= y && ballY - BALL <= y + BRICK_HEIGHT) {
                    bricks[row][column] = false;
                    bricksLeft--;
                    score += 10 + row * 5;
                    velocityY *= -1;
                    flashTicks = 3;
                    spawnParticles(x + brickWidth / 2.0, y + BRICK_HEIGHT / 2.0,
                            BRICK_COLORS[Math.min(row, BRICK_COLORS.length - 1)], 9);
                    return;
                }
            }
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void drawBrick(DrawContext context, int x, int y, int color) {
        int right = x + brickWidth;
        int bottom = y + BRICK_HEIGHT;
        ArcadeTheme.rounded(context, x, y, right, bottom, 4, ArcadeTheme.mix(0xFF000000, color, 0.55F));
        ArcadeTheme.rounded(context, x + 1, y + 1, right, bottom, 4, color);
        ArcadeTheme.rounded(context, x + 2, y + 2, right - 2, y + 6, 3, ArcadeTheme.lighten(color, 45));
        context.fill(x + 3, y + 9, right - 3, y + 11, 0x22000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.MAGENTA);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? (won ? "Todos os blocos destruídos!" : "Fim de jogo — pressione R")
                        : "A/D ou ←/→ movem • ESPAÇO lança a bola"),
                width / 2, 32, gameOver ? (won ? ArcadeTheme.SUCCESS : ArcadeTheme.GOLD) : ArcadeTheme.MUTED);

        int left = arenaLeft;
        int top = arenaTop;
        int right = left + arenaWidth;
        int bottom = top + arenaHeight;
        ArcadeTheme.pill(context, textRenderer, "PONTOS  " + score, left, top - 22, ArcadeTheme.MAGENTA);
        ArcadeTheme.pillRight(context, textRenderer, "VIDAS  " + Math.max(0, lives), right, top - 22, ArcadeTheme.CORAL);

        ArcadeTheme.rounded(context, left - 5, top - 5, right + 5, bottom + 5, 9,
                ArcadeTheme.alpha(ArcadeTheme.MAGENTA, 110));
        ArcadeTheme.rounded(context, left - 4, top - 4, right + 4, bottom + 4, 8, ArcadeTheme.PANEL_DEEP);
        context.fill(left, top, right, bottom, 0xFF070C16);
        context.fillGradient(left, top, right, top + 30, 0x2AF0509B, 0x00000000);
        for (int x = left + 14; x < right; x += 26) {
            context.fill(x, top + 2, x + 1, bottom - 2, 0x0CFFFFFF);
        }
        if (flashTicks > 0) {
            context.fill(left, top, right, bottom, ArcadeTheme.alpha(ArcadeTheme.MAGENTA, 22));
        }

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (bricks[row][column]) {
                    int x = left + brickStartX + column * (brickWidth + BRICK_GAP);
                    int y = top + brickStartY + row * (BRICK_HEIGHT + BRICK_GAP);
                    drawBrick(context, x, y, BRICK_COLORS[Math.min(row, BRICK_COLORS.length - 1)]);
                }
            }
        }

        for (Particle particle : particles) {
            ArcadeTheme.disc(context, left + (int) particle.x, top + (int) particle.y, 2,
                    ArcadeTheme.alpha(particle.color, Math.min(220, particle.life * 12)));
        }

        int paddleY = top + arenaHeight - 27;
        int paddleLeft = left + (int) paddleX - 38;
        ArcadeTheme.glow(context, left + (int) paddleX, paddleY + 4, 34, 0x2235C8B9);
        ArcadeTheme.rounded(context, paddleLeft, paddleY, paddleLeft + 76, paddleY + 9, 4, ArcadeTheme.TEAL);
        ArcadeTheme.rounded(context, paddleLeft + 2, paddleY + 1, paddleLeft + 74, paddleY + 4, 2,
                ArcadeTheme.lighten(ArcadeTheme.TEAL, 70));

        int ballCenterX = left + (int) ballX;
        int ballCenterY = top + (int) ballY;
        for (int index = TRAIL - 1; index >= 0; index--) {
            float strength = 1.0F - index / (float) TRAIL;
            ArcadeTheme.disc(context, left + (int) trailX[index], top + (int) trailY[index],
                    Math.max(1, Math.round(BALL / 2.0F * strength)),
                    ArcadeTheme.alpha(ArcadeTheme.GOLD, (int) (80 * strength)));
        }
        ArcadeTheme.glow(context, ballCenterX, ballCenterY, BALL + 6, 0x44FFD66B);
        ArcadeTheme.disc(context, ballCenterX, ballCenterY, BALL / 2 + 1, 0xFFFFF6D8);

        if (!launched && !gameOver) {
            ArcadeTheme.status(context, textRenderer, Text.literal("ESPAÇO para lançar"),
                    left + arenaWidth / 2, top + arenaHeight / 2 + 6, ArcadeTheme.GOLD);
        }
        if (gameOver) {
            int bannerWidth = Math.min(arenaWidth - 20, 260);
            int centerY = top + arenaHeight / 2;
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, centerY - 30,
                    width / 2 + bannerWidth / 2, centerY + 30, 12, 0xF00A1422);
            context.fill(width / 2 - bannerWidth / 2 + 12, centerY - 30, width / 2 + bannerWidth / 2 - 12,
                    centerY - 28, won ? ArcadeTheme.SUCCESS : ArcadeTheme.GOLD);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(won ? "NÍVEL LIMPO!" : "FIM DE JOGO"),
                    width / 2, centerY - 18, won ? ArcadeTheme.SUCCESS : ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Pontos: " + score),
                    width / 2, centerY - 2, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("R para jogar de novo"),
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
        if (keyCode == GLFW.GLFW_KEY_SPACE && !launched && !gameOver) {
            launched = true;
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
