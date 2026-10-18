package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Pong contra um adversário simples controlado pelo computador. */
public final class PongScreen extends Screen {
    private static final int PADDLE_WIDTH = 9;
    private static final int PADDLE_HEIGHT = 58;
    private static final int BALL_SIZE = 8;
    private static final int WIN_SCORE = 5;

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
    private int playerScore;
    private int opponentScore;
    private int serveTicks;
    private int serveDirection = 1;
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
        arenaHeight = Math.min(290, height - 118);
        arenaLeft = (width - arenaWidth) / 2;
        arenaTop = Math.max(42, (height - arenaHeight) / 2 - 10);
        int buttonY = Math.min(height - 24, arenaTop + arenaHeight + 18);

        addDrawableChild(ButtonWidget.builder(Text.literal("Reiniciar (R)"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (!initialized) {
            newGame();
        }
    }

    private void newGame() {
        playerScore = 0;
        opponentScore = 0;
        gameOver = false;
        initialized = true;
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
        if (opponentY < opponentTarget - 5) {
            opponentY += 3.3;
        } else if (opponentY > opponentTarget + 5) {
            opponentY -= 3.3;
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
        } else if (velocityX > 0 && ballX + BALL_SIZE >= opponentX
                && Math.abs(ballY - opponentY) <= PADDLE_HEIGHT / 2.0 + BALL_SIZE) {
            bounceOffPaddle(opponentX - PADDLE_WIDTH, opponentY, -1);
        }

        if (ballX < -BALL_SIZE) {
            opponentScore++;
            serveDirection = -1;
            finishPoint();
        } else if (ballX > arenaWidth + BALL_SIZE) {
            playerScore++;
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
        context.fill(0, 0, width, height, 0xFF10202D);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("PONG"), width / 2, 12, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("W / S ou ↑ / ↓ para mover sua raquete"), width / 2, 29, 0xFFCCDDEE);

        int left = arenaLeft;
        int top = arenaTop;
        context.fill(left - 2, top - 2, left + arenaWidth + 2, top + arenaHeight + 2, 0xFFB0C9D4);
        context.fill(left, top, left + arenaWidth, top + arenaHeight, 0xFF07121A);
        for (int y = top + 8; y < top + arenaHeight; y += 22) {
            context.fill(width / 2 - 1, y, width / 2 + 1, Math.min(y + 12, top + arenaHeight), 0xFF536873);
        }

        context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(playerScore)),
                left + arenaWidth / 4, top + 13, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(opponentScore)),
                left + arenaWidth * 3 / 4, top + 13, 0xFFFFFFFF);
        context.fill(left + 20, top + (int) playerY - PADDLE_HEIGHT / 2,
                left + 20 + PADDLE_WIDTH, top + (int) playerY + PADDLE_HEIGHT / 2, 0xFF67D9FF);
        context.fill(left + arenaWidth - 20 - PADDLE_WIDTH,
                top + (int) opponentY - PADDLE_HEIGHT / 2,
                left + arenaWidth - 20, top + (int) opponentY + PADDLE_HEIGHT / 2, 0xFFFF8267);
        context.fill(left + (int) ballX - BALL_SIZE / 2, top + (int) ballY - BALL_SIZE / 2,
                left + (int) ballX + BALL_SIZE / 2, top + (int) ballY + BALL_SIZE / 2, 0xFFFFFFFF);

        if (gameOver) {
            String result = playerScore > opponentScore ? "Você venceu!" : "O computador venceu!";
            context.fill(left, top + arenaHeight / 2 - 25,
                    left + arenaWidth, top + arenaHeight / 2 + 25, 0xAA000000);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(result),
                    width / 2, top + arenaHeight / 2 - 4, 0xFFFFFFFF);
        } else if (serveTicks > 0) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Prepare-se!"),
                    width / 2, top + arenaHeight / 2 + 18, 0xFFCCDDDD);
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
