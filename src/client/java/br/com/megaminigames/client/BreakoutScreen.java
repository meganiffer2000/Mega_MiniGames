package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Quebra-blocos: rebata a bola com a raquete e destrua todos os tijolos. */
public final class BreakoutScreen extends Screen {
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int BRICK_WIDTH = 58;
    private static final int BRICK_HEIGHT = 18;
    private static final int BALL = 9;
    private final boolean[][] bricks = new boolean[ROWS][COLUMNS];

    private int arenaLeft;
    private int arenaTop;
    private int arenaWidth;
    private int arenaHeight;
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

    public BreakoutScreen() {
        super(Text.literal("Quebra-Blocos"));
    }

    @Override
    protected void init() {
        arenaWidth = Math.min(500, width - 40);
        arenaHeight = Math.min(300, height - 100);
        arenaLeft = (width - arenaWidth) / 2;
        arenaTop = Math.max(42, (height - arenaHeight) / 2 - 8);
        int buttonY = Math.min(height - 24, arenaTop + arenaHeight + 12);
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
        for (boolean[] row : bricks) {
            java.util.Arrays.fill(row, true);
        }
        bricksLeft = ROWS * COLUMNS;
        lives = 3;
        score = 0;
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
    }

    @Override
    public void tick() {
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
            return;
        }

        ballX += velocityX;
        ballY += velocityY;
        if (ballX <= BALL || ballX >= arenaWidth - BALL) {
            velocityX *= -1;
            ballX = clamp(ballX, BALL, arenaWidth - BALL);
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
        }

        hitBrick();
        if (ballY > arenaHeight + BALL) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
            } else {
                launchBall();
            }
        }
        if (bricksLeft == 0) {
            won = true;
            gameOver = true;
        }
    }

    private void hitBrick() {
        int brickStartX = (arenaWidth - COLUMNS * BRICK_WIDTH) / 2;
        int brickStartY = 34;
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (!bricks[row][column]) {
                    continue;
                }
                int x = brickStartX + column * BRICK_WIDTH;
                int y = brickStartY + row * (BRICK_HEIGHT + 4);
                if (ballX + BALL >= x && ballX - BALL <= x + BRICK_WIDTH - 4
                        && ballY + BALL >= y && ballY - BALL <= y + BRICK_HEIGHT) {
                    bricks[row][column] = false;
                    bricksLeft--;
                    score += 10;
                    velocityY *= -1;
                    return;
                }
            }
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF101724);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Quebra-Blocos  |  Pontos: " + score + "  |  Vidas: " + lives),
                width / 2, 13, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("A/D ou ←/→ movem a raquete • Espaço inicia"), width / 2, 29, 0xFFCBD7E5);
        context.fill(arenaLeft - 2, arenaTop - 2, arenaLeft + arenaWidth + 2,
                arenaTop + arenaHeight + 2, 0xFF758393);
        context.fill(arenaLeft, arenaTop, arenaLeft + arenaWidth, arenaTop + arenaHeight, 0xFF080E17);

        int brickStartX = (arenaWidth - COLUMNS * BRICK_WIDTH) / 2;
        int brickStartY = 34;
        int[] colors = {0xFFE95252, 0xFFEE8A45, 0xFFECC34A, 0xFF55C96D, 0xFF527FE8};
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (bricks[row][column]) {
                    int x = arenaLeft + brickStartX + column * BRICK_WIDTH;
                    int y = arenaTop + brickStartY + row * (BRICK_HEIGHT + 4);
                    context.fill(x, y, x + BRICK_WIDTH - 4, y + BRICK_HEIGHT, colors[row]);
                    context.fill(x + 3, y + 2, x + BRICK_WIDTH - 9, y + 5, 0x44FFFFFF);
                }
            }
        }
        int paddleY = arenaTop + arenaHeight - 27;
        context.fill(arenaLeft + (int) paddleX - 38, paddleY,
                arenaLeft + (int) paddleX + 38, paddleY + 9, 0xFF6AD7F2);
        context.fill(arenaLeft + (int) ballX - BALL / 2, arenaTop + (int) ballY - BALL / 2,
                arenaLeft + (int) ballX + BALL / 2, arenaTop + (int) ballY + BALL / 2, 0xFFFFFFFF);
        if (!launched && !gameOver) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Pressione ESPAÇO para lançar"),
                    width / 2, arenaTop + arenaHeight / 2, 0xFFFFFFFF);
        }
        if (gameOver) {
            String message = won ? "Você destruiu todos os blocos!" : "Fim de jogo! Pressione R";
            context.fill(arenaLeft, arenaTop + arenaHeight / 2 - 22,
                    arenaLeft + arenaWidth, arenaTop + arenaHeight / 2 + 22, 0xAA000000);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(message), width / 2,
                    arenaTop + arenaHeight / 2 - 4, 0xFFFFFFFF);
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
}
