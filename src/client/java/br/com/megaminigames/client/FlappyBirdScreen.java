package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Minijogo Flappy Bird desenhado e executado diretamente na tela do Minecraft. */
public final class FlappyBirdScreen extends Screen {
    private static final int SKY = 0xFF70C5CE;
    private static final int GROUND_HEIGHT = 28;
    private static final int PIPE_WIDTH = 48;
    private static final int PIPE_GAP = 126;
    private static final double PIPE_SPEED = 2.6;
    private static final double BIRD_X_RATIO = 0.28;
    private static final int BIRD_SIZE = 18;

    private final List<Pipe> pipes = new ArrayList<>();
    private double birdY;
    private double birdVelocity;
    private int score;
    private boolean gameOver;
    private boolean gameStarted;

    public FlappyBirdScreen() {
        super(Text.literal("Flappy Bird"));
    }

    @Override
    protected void init() {
        int buttonWidth = 112;
        int buttonY = height - 25;
        int totalWidth = buttonWidth * 2 + 8;
        int startX = (width - totalWidth) / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("Reiniciar (R)"), button -> resetGame())
                .dimensions(startX, buttonY, buttonWidth, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(startX + buttonWidth + 8, buttonY, buttonWidth, 20)
                .build());

        if (!gameStarted) {
            resetGame();
        }
    }

    private void resetGame() {
        birdY = height * 0.43;
        birdVelocity = 0;
        score = 0;
        gameOver = false;
        gameStarted = true;
        pipes.clear();
        addPipe(width + 110);
        addPipe(width + 330);
    }

    private void addPipe(double x) {
        int groundY = height - GROUND_HEIGHT;
        int minCenter = 70 + PIPE_GAP / 2;
        int maxCenter = Math.max(minCenter + 1, groundY - 55 - PIPE_GAP / 2);
        int center = ThreadLocalRandom.current().nextInt(minCenter, maxCenter + 1);
        pipes.add(new Pipe(x, center));
    }

    @Override
    public void tick() {
        if (gameOver || !gameStarted) {
            return;
        }

        birdVelocity = Math.min(birdVelocity + 0.34, 7.0);
        birdY += birdVelocity;

        for (Pipe pipe : pipes) {
            pipe.x -= PIPE_SPEED;

            if (!pipe.scored && pipe.x + PIPE_WIDTH < birdX()) {
                pipe.scored = true;
                score++;
            }
        }
        pipes.removeIf(pipe -> pipe.x + PIPE_WIDTH < -4);
        if (!pipes.isEmpty() && pipes.get(pipes.size() - 1).x < width - 150) {
            addPipe(width + 30);
        }

        checkCollisions();
    }

    private void checkCollisions() {
        double birdLeft = birdX() - BIRD_SIZE / 2.0;
        double birdRight = birdX() + BIRD_SIZE / 2.0;
        double birdTop = birdY - BIRD_SIZE / 2.0;
        double birdBottom = birdY + BIRD_SIZE / 2.0;
        int groundY = height - GROUND_HEIGHT;

        if (birdTop < 0 || birdBottom >= groundY) {
            gameOver = true;
            return;
        }

        for (Pipe pipe : pipes) {
            if (birdRight > pipe.x && birdLeft < pipe.x + PIPE_WIDTH) {
                int gapTop = pipe.gapCenter - PIPE_GAP / 2;
                int gapBottom = pipe.gapCenter + PIPE_GAP / 2;
                if (birdTop < gapTop || birdBottom > gapBottom) {
                    gameOver = true;
                    return;
                }
            }
        }
    }

    private int birdX() {
        return (int) (width * BIRD_X_RATIO);
    }

    private void flap() {
        if (gameOver) {
            resetGame();
        }
        birdVelocity = -5.8;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, SKY);
        drawScenery(context);
        drawPipes(context);
        drawBird(context);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Flappy Bird  |  Pontos: " + score), width / 2, 12, 0xFFFFFF);

        if (gameOver) {
            context.fill(0, height / 2 - 45, width, height / 2 + 34, 0x99000000);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Fim de jogo!"),
                    width / 2, height / 2 - 30, 0xFFFF5555);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Pontuação: " + score + "  |  Pressione ESPAÇO para tentar novamente"),
                    width / 2, height / 2 - 10, 0xFFFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Clique ou pressione ESPAÇO / ↑ para voar"),
                    width / 2, height - 43, 0xFFFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawScenery(DrawContext context) {
        int groundY = height - GROUND_HEIGHT;
        context.fill(0, groundY - 3, width, groundY, 0xFFDED895);
        context.fill(0, groundY, width, height, 0xFFDEB85A);
        context.fill(0, groundY, width, groundY + 4, 0xFF73BF2E);

        for (int x = 0; x < width; x += 24) {
            context.fill(x, groundY + 11, x + 11, groundY + 14, 0xFFB48B3C);
            context.fill(x + 12, groundY + 20, x + 21, groundY + 23, 0xFFB48B3C);
        }

        // Nuvens simples para dar profundidade ao cenário.
        drawCloud(context, width / 5, height / 5);
        drawCloud(context, width * 3 / 4, height / 4);
    }

    private void drawCloud(DrawContext context, int x, int y) {
        context.fill(x, y, x + 42, y + 11, 0x99FFFFFF);
        context.fill(x + 9, y - 6, x + 31, y + 13, 0x99FFFFFF);
    }

    private void drawPipes(DrawContext context) {
        int groundY = height - GROUND_HEIGHT;
        for (Pipe pipe : pipes) {
            int x = (int) pipe.x;
            int gapTop = pipe.gapCenter - PIPE_GAP / 2;
            int gapBottom = pipe.gapCenter + PIPE_GAP / 2;

            context.fill(x, 0, x + PIPE_WIDTH, gapTop, 0xFF58B82E);
            context.fill(x - 4, gapTop - 14, x + PIPE_WIDTH + 4, gapTop, 0xFF4CA526);
            context.fill(x + 5, 0, x + 11, gapTop - 14, 0x557DFF58);

            context.fill(x, gapBottom, x + PIPE_WIDTH, groundY, 0xFF58B82E);
            context.fill(x - 4, gapBottom, x + PIPE_WIDTH + 4, gapBottom + 14, 0xFF4CA526);
            context.fill(x + 5, gapBottom + 14, x + 11, groundY, 0x557DFF58);
        }
    }

    private void drawBird(DrawContext context) {
        int x = birdX();
        int y = (int) birdY;
        context.fill(x - 9, y - 8, x + 10, y + 9, 0xFFFFD83D);
        context.fill(x - 5, y + 1, x + 3, y + 6, 0xFFFFB52E);
        context.fill(x + 3, y - 5, x + 7, y - 1, 0xFF202020);
        context.fill(x + 8, y, x + 15, y + 4, 0xFFFF7138);
        context.fill(x - 8, y - 8, x + 10, y - 5, 0xFFFFF078);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && !gameOver) {
            flap();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            resetGame();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_UP) {
            flap();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Pipe {
        private double x;
        private final int gapCenter;
        private boolean scored;

        private Pipe(double x, int gapCenter) {
            this.x = x;
            this.gapCenter = gapCenter;
        }
    }
}
