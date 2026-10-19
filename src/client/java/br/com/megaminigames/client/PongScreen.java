package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Pong contra um adversário simples controlado pelo computador. */
public final class PongScreen extends Screen {
    private static final int PADDLE_WIDTH = 9;
    private static final int PADDLE_HEIGHT = 58;
    private static final int BALL_SIZE = 8;
    private static final int WIN_SCORE = 5;
    private static final int TRAIL = 8;

    private int arenaLeft;
    private int arenaTop;
    private int arenaWidth;
    private int arenaHeight;
    private double playerY;
    private double opponentY;
    private double ballX;
    private double ballY;
    private double velocityX;
    private double velocityY;
    private final double[] trailX = new double[TRAIL];
    private final double[] trailY = new double[TRAIL];
    private int playerScore;
    private int opponentScore;
    private int serveTicks;
    private int serveDirection = 1;
    private int rally;
    private boolean upPressed;
    private boolean downPressed;
    private boolean gameOver;
    private boolean initialized;

    public PongScreen() {
        super(Text.literal("Pong"));
    }

    @Override
    protected void init() {
        arenaWidth = Math.min(520, width - 50);
        arenaHeight = Math.min(290, height - 148);
        arenaLeft = (width - arenaWidth) / 2;
        arenaTop = Math.max(64, (height - arenaHeight - 96) / 2 + 22);
        int buttonY = Math.min(height - 24, arenaTop + arenaHeight + 18);
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
        playerScore = 0;
        opponentScore = 0;
        gameOver = false;
        initialized = true;
        rally = 0;
        playerY = arenaHeight / 2.0;
        opponentY = arenaHeight / 2.0;
        serveDirection = 1;
        serve();
    }

    private void serve() {
        ballX = arenaWidth / 2.0;
        ballY = arenaHeight / 2.0;
        velocityX = 0;
        velocityY = 0;
        serveTicks = 35;
        for (int index = 0; index < TRAIL; index++) {
            trailX[index] = ballX;
            trailY[index] = ballY;
        }
    }

    @Override
    public void tick() {
        if (gameOver) {
            return;
        }
        if (upPressed) {
            playerY -= 5;
        }
        if (downPressed) {
            playerY += 5;
        }
        playerY = clamp(playerY, PADDLE_HEIGHT / 2.0, arenaHeight - PADDLE_HEIGHT / 2.0);

        double opponentTarget = ballX > arenaWidth * 0.44 ? ballY : arenaHeight / 2.0;
        double opponentSpeed = Math.min(4.4, 3.0 + rally * 0.09);
        if (opponentY < opponentTarget - 5) {
            opponentY += opponentSpeed;
        } else if (opponentY > opponentTarget + 5) {
            opponentY -= opponentSpeed;
        }
        opponentY = clamp(opponentY, PADDLE_HEIGHT / 2.0, arenaHeight - PADDLE_HEIGHT / 2.0);

        if (serveTicks > 0) {
            serveTicks--;
            if (serveTicks == 0) {
                velocityX = 4.2 * serveDirection;
                velocityY = (Math.random() - 0.5) * 4.0;
            }
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
        if (ballY <= BALL_SIZE || ballY >= arenaHeight - BALL_SIZE) {
            velocityY *= -1;
            ballY = clamp(ballY, BALL_SIZE, arenaHeight - BALL_SIZE);
        }

        double playerX = 20;
        double opponentX = arenaWidth - 20;
        if (velocityX < 0 && ballX - BALL_SIZE <= playerX + PADDLE_WIDTH
                && Math.abs(ballY - playerY) <= PADDLE_HEIGHT / 2.0 + BALL_SIZE) {
            bounceOffPaddle(playerX + PADDLE_WIDTH, playerY, 1);
            rally++;
        } else if (velocityX > 0 && ballX + BALL_SIZE >= opponentX
                && Math.abs(ballY - opponentY) <= PADDLE_HEIGHT / 2.0 + BALL_SIZE) {
            bounceOffPaddle(opponentX - PADDLE_WIDTH, opponentY, -1);
            rally++;
        }

        if (ballX < -BALL_SIZE) {
            opponentScore++;
            rally = 0;
            serveDirection = -1;
            finishPoint();
        } else if (ballX > arenaWidth + BALL_SIZE) {
            playerScore++;
            rally = 0;
            serveDirection = 1;
            finishPoint();
        }
    }

    private void bounceOffPaddle(double contactX, double paddleY, int direction) {
        double impact = (ballY - paddleY) / (PADDLE_HEIGHT / 2.0);
        velocityX = direction * Math.min(7.0, Math.abs(velocityX) + 0.25);
        velocityY = impact * 4.5;
        ballX = contactX + direction * (BALL_SIZE + 1);
    }

    private void finishPoint() {
        if (playerScore >= WIN_SCORE || opponentScore >= WIN_SCORE) {
            gameOver = true;
        } else {
            serve();
        }
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.SKY);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? (playerScore > opponentScore ? "Você venceu a partida!" : "O computador venceu!")
                        : "W / S ou as setas para mover a raquete"),
                width / 2, 32, gameOver ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        int left = arenaLeft;
        int top = arenaTop;
        int right = left + arenaWidth;
        int bottom = top + arenaHeight;
        int accent = ArcadeTheme.SKY;

        ArcadeTheme.pill(context, textRenderer, "VOCÊ  " + playerScore, left, top - 23, ArcadeTheme.TEAL);
        ArcadeTheme.pillRight(context, textRenderer, "CPU  " + opponentScore, right, top - 23, ArcadeTheme.CORAL);

        ArcadeTheme.rounded(context, left - 6, top - 6, right + 6, bottom + 6, 10, ArcadeTheme.alpha(accent, 90));
        ArcadeTheme.rounded(context, left - 5, top - 5, right + 5, bottom + 5, 9, ArcadeTheme.PANEL_DEEP);
        context.fillGradient(left, top, right, top + 26, 0x33123456, 0x00000000);
        context.fill(left, top, right, bottom, 0xFF071019);

        context.fillGradient(left, top, left + 26, bottom, 0x14000000, 0x00000000);
        context.fillGradient(right - 26, top, right, bottom, 0x14000000, 0x00000000);

        for (int y = top + 10; y < bottom - 6; y += 22) {
            int segmentBottom = Math.min(y + 12, bottom - 4);
            context.fill(left + arenaWidth / 2 - 1, y, left + arenaWidth / 2 + 1, segmentBottom, 0x55536A80);
        }
        ArcadeTheme.ring(context, left + arenaWidth / 2, top + arenaHeight / 2, 26, 1, 0x44536A80);
        ArcadeTheme.disc(context, left + arenaWidth / 2, top + arenaHeight / 2, 2, 0x66536A80);

        context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(playerScore)),
                left + arenaWidth / 4, top + 26, 0x33BFEFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(opponentScore)),
                left + arenaWidth * 3 / 4, top + 26, 0x33FFC0AA);
        if (rally >= 2) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("RALLY " + rally),
                    left + arenaWidth / 2, top + 8, ArcadeTheme.DIM);
        }

        int playerPaddleTop = top + (int) playerY - PADDLE_HEIGHT / 2;
        int opponentPaddleTop = top + (int) opponentY - PADDLE_HEIGHT / 2;
        ArcadeTheme.glow(context, left + 25, top + (int) playerY, PADDLE_HEIGHT / 2 + 9, 0x2267D9FF);
        ArcadeTheme.rounded(context, left + 20, playerPaddleTop, left + 20 + PADDLE_WIDTH,
                playerPaddleTop + PADDLE_HEIGHT, 4, ArcadeTheme.SKY);
        ArcadeTheme.rounded(context, left + 22, playerPaddleTop + 2, left + 24,
                playerPaddleTop + PADDLE_HEIGHT - 2, 1, 0x99FFFFFF);
        ArcadeTheme.glow(context, right - 25, top + (int) opponentY, PADDLE_HEIGHT / 2 + 9, 0x22FF8A65);
        ArcadeTheme.rounded(context, right - 20 - PADDLE_WIDTH, opponentPaddleTop, right - 20,
                opponentPaddleTop + PADDLE_HEIGHT, 4, ArcadeTheme.CORAL);
        ArcadeTheme.rounded(context, right - 24, opponentPaddleTop + 2, right - 22,
                opponentPaddleTop + PADDLE_HEIGHT - 2, 1, 0x99FFFFFF);

        int ballCenterX = left + (int) ballX;
        int ballCenterY = top + (int) ballY;
        for (int index = TRAIL - 1; index >= 0; index--) {
            float strength = 1.0F - index / (float) TRAIL;
            int radius = Math.max(1, Math.round(BALL_SIZE / 2.0F * strength));
            ArcadeTheme.disc(context, left + (int) trailX[index], top + (int) trailY[index], radius,
                    ArcadeTheme.alpha(ArcadeTheme.LIME, (int) (90 * strength)));
        }
        ArcadeTheme.glow(context, ballCenterX, ballCenterY, BALL_SIZE + 6, 0x44B9F26B);
        ArcadeTheme.disc(context, ballCenterX, ballCenterY, BALL_SIZE / 2 + 1, ArcadeTheme.LIME);
        context.fill(ballCenterX - 1, ballCenterY - 2, ballCenterX + 1, ballCenterY, 0xCCFFFFFF);

        if (gameOver) {
            int bannerWidth = Math.min(arenaWidth - 24, 250);
            int centerY = top + arenaHeight / 2;
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, centerY - 30,
                    width / 2 + bannerWidth / 2, centerY + 30, 12, 0xF00A1422);
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, centerY - 30,
                    width / 2 + bannerWidth / 2, centerY - 27, 12, ArcadeTheme.GOLD);
            boolean victory = playerScore > opponentScore;
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(victory ? "VITÓRIA!" : "DERROTA"),
                    width / 2, centerY - 18, victory ? ArcadeTheme.SUCCESS : ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(playerScore + "  x  " + opponentScore),
                    width / 2, centerY - 4, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("R para jogar de novo"),
                    width / 2, centerY + 12, ArcadeTheme.MUTED);
        } else if (serveTicks > 0) {
            String countdown = serveTicks > 24 ? "3" : serveTicks > 12 ? "2" : "1";
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(countdown),
                    left + arenaWidth / 2, top + arenaHeight / 2 + 22, ArcadeTheme.GOLD);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_W || keyCode == GLFW.GLFW_KEY_UP) {
            upPressed = true;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_S || keyCode == GLFW.GLFW_KEY_DOWN) {
            downPressed = true;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_W || keyCode == GLFW.GLFW_KEY_UP) {
            upPressed = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_S || keyCode == GLFW.GLFW_KEY_DOWN) {
            downPressed = false;
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
