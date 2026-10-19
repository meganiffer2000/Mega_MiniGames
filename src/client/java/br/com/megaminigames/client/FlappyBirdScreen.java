package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Flappy Bird com cenário em parallax, canos arredondados e animação do pássaro. */
public final class FlappyBirdScreen extends Screen {
    private static final int GROUND_HEIGHT = 26;
    private static final int PIPE_WIDTH = 44;
    private static final int PIPE_GAP = 118;
    private static final double PIPE_SPEED = 2.6;
    private static final double BIRD_X_RATIO = 0.3;
    private static final int BIRD_SIZE = 18;
    private static int bestScore;

    private final List<Pipe> pipes = new ArrayList<>();
    private final List<double[]> feathers = new ArrayList<>();
    private int arenaLeft;
    private int arenaTop;
    private int arenaWidth;
    private int arenaHeight;
    private double birdY;
    private double birdVelocity;
    private double scroll;
    private int score;
    private int ticks;
    private boolean gameOver;
    private boolean gameStarted;
    private boolean initialized;

    public FlappyBirdScreen() {
        super(Text.literal("Flappy Bird"));
    }

    @Override
    protected void init() {
        arenaWidth = Math.min(480, width - 32);
        arenaHeight = Math.min(330, height - 140);
        arenaLeft = (width - arenaWidth) / 2;
        arenaTop = Math.max(58, (height - arenaHeight - 96) / 2 + 26);
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        int buttonY = Math.min(height - 24, arenaTop + arenaHeight + 16);
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Reiniciar (R)"), this::resetGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) {
            resetGame();
        }
    }

    private void resetGame() {
        birdY = arenaHeight * 0.42;
        birdVelocity = 0;
        scroll = 0;
        score = 0;
        gameOver = false;
        gameStarted = true;
        initialized = true;
        pipes.clear();
        feathers.clear();
        addPipe(arenaWidth + 90);
        addPipe(arenaWidth + 290);
    }

    private void addPipe(double x) {
        int groundY = arenaHeight - GROUND_HEIGHT;
        int minCenter = 62 + PIPE_GAP / 2;
        int maxCenter = Math.max(minCenter + 1, groundY - 50 - PIPE_GAP / 2);
        int center = ThreadLocalRandom.current().nextInt(minCenter, maxCenter + 1);
        pipes.add(new Pipe(x, center));
    }

    @Override
    public void tick() {
        ticks++;
        if (gameOver || !gameStarted) {
            return;
        }
        scroll += PIPE_SPEED;
        birdVelocity = Math.min(birdVelocity + 0.34, 7.0);
        birdY += birdVelocity;

        for (Pipe pipe : pipes) {
            pipe.x -= PIPE_SPEED;
            if (!pipe.scored && pipe.x + PIPE_WIDTH < birdX()) {
                pipe.scored = true;
                score++;
                if (score > bestScore) {
                    bestScore = score;
                }
            }
        }
        pipes.removeIf(pipe -> pipe.x + PIPE_WIDTH < -8);
        if (!pipes.isEmpty() && pipes.get(pipes.size() - 1).x < arenaWidth - 140) {
            addPipe(arenaWidth + 24);
        }
        checkCollisions();
    }

    private void checkCollisions() {
        double birdLeft = birdX() - BIRD_SIZE / 2.0;
        double birdRight = birdX() + BIRD_SIZE / 2.0;
        double birdTop = birdY - BIRD_SIZE / 2.0;
        double birdBottom = birdY + BIRD_SIZE / 2.0;
        int groundY = arenaHeight - GROUND_HEIGHT;

        if (birdTop < 4 || birdBottom >= groundY) {
            crash();
            return;
        }
        for (Pipe pipe : pipes) {
            if (birdRight > pipe.x && birdLeft < pipe.x + PIPE_WIDTH) {
                int gapTop = pipe.gapCenter - PIPE_GAP / 2;
                int gapBottom = pipe.gapCenter + PIPE_GAP / 2;
                if (birdTop < gapTop || birdBottom > gapBottom) {
                    crash();
                    return;
                }
            }
        }
    }

    private void crash() {
        if (gameOver) {
            return;
        }
        gameOver = true;
        for (int index = 0; index < 7; index++) {
            feathers.add(new double[]{birdX() + (Math.random() - 0.5) * 14, birdY + (Math.random() - 0.5) * 14,
                    (Math.random() - 0.5) * 2.4, -1.5 - Math.random() * 2.0, 20 + Math.random() * 14});
        }
    }

    private int birdX() {
        return (int) (arenaWidth * BIRD_X_RATIO);
    }

    private void flap() {
        if (gameOver) {
            resetGame();
            return;
        }
        birdVelocity = -5.8;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.LIME);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? "Choque! Pressione ESPAÇO para tentar novamente"
                        : "Clique ou pressione ESPAÇO / ↑ para bater as asas"),
                width / 2, 32, gameOver ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        int left = arenaLeft;
        int top = arenaTop;
        int right = left + arenaWidth;
        int bottom = top + arenaHeight;
        ArcadeTheme.pill(context, textRenderer, "PONTOS  " + score, left, top - 22, ArcadeTheme.LIME);
        ArcadeTheme.pillRight(context, textRenderer, "RECORDE  " + bestScore, right, top - 22, ArcadeTheme.GOLD);

        ArcadeTheme.rounded(context, left - 6, top - 6, right + 6, bottom + 6, 10,
                ArcadeTheme.alpha(ArcadeTheme.LIME, 100));
        ArcadeTheme.rounded(context, left - 5, top - 5, right + 5, bottom + 5, 9, ArcadeTheme.PANEL_DEEP);
        context.enableScissor(left, top, right, bottom);
        drawSky(context, left, top);
        drawPipes(context, left, top);
        drawGround(context, left, top, bottom);
        drawBird(context, left, top);
        drawFeathers(context, left, top);
        context.disableScissor();

        if (!gameOver && score == 0) {
            ArcadeTheme.status(context, textRenderer, Text.literal("ESPAÇO para voar"),
                    left + arenaWidth / 2, top + arenaHeight / 2 + 46, ArcadeTheme.TEXT);
        }
        if (gameOver) {
            int bannerWidth = Math.min(arenaWidth - 24, 236);
            int centerY = top + arenaHeight / 2;
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, centerY - 38,
                    width / 2 + bannerWidth / 2, centerY + 38, 12, 0xF00A1422);
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2 + 1, centerY - 37,
                    width / 2 + bannerWidth / 2 - 1, centerY - 34, 10, ArcadeTheme.GOLD);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("FIM DE VOO"),
                    width / 2, centerY - 24, ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Pontos: " + score),
                    width / 2, centerY - 8, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Recorde: " + bestScore),
                    width / 2, centerY + 4, ArcadeTheme.GOLD);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("ESPAÇO para voar de novo"),
                    width / 2, centerY + 20, ArcadeTheme.MUTED);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawSky(DrawContext context, int left, int top) {
        int bottom = top + arenaHeight - GROUND_HEIGHT;
        context.fillGradient(left, top, left + arenaWidth, top + arenaHeight / 3, 0xFF2E9BC9, 0xFF63C6DC);
        context.fillGradient(left, top + arenaHeight / 3, left + arenaWidth, bottom, 0xFF63C6DC, 0xFFA8E6EA);
        ArcadeTheme.glow(context, left + arenaWidth - 58, top + 44, 26, 0x40FFF0A8);
        ArcadeTheme.disc(context, left + arenaWidth - 58, top + 44, 14, 0xFFFFF2B0);

        int drift = (int) (scroll * 0.18);
        for (int index = 0; index < 5; index++) {
            int x = Math.floorMod(index * 121 - drift, arenaWidth + 90) - 60;
            int y = top + 22 + (index * 37) % Math.max(30, arenaHeight / 3);
            drawCloud(context, left + x, y);
        }
        int backDrift = (int) (scroll * 0.35);
        for (int index = 0; index < 6; index++) {
            int x = Math.floorMod(index * 96 - backDrift, arenaWidth + 120) - 80;
            int height = 30 + (index * 17) % 26;
            ArcadeTheme.rounded(context, left + x, bottom - height, left + x + 110, bottom, 20, 0xFF5FB99A);
        }
        for (int index = 0; index < 5; index++) {
            int x = Math.floorMod(index * 132 - (int) (scroll * 0.6), arenaWidth + 140) - 90;
            int height = 22 + (index * 23) % 20;
            ArcadeTheme.rounded(context, left + x, bottom - height, left + x + 120, bottom, 16, 0xFF4BA37F);
        }
    }

    private void drawCloud(DrawContext context, int x, int y) {
        ArcadeTheme.rounded(context, x, y, x + 44, y + 12, 6, 0xCCF3FBFF);
        ArcadeTheme.rounded(context, x + 9, y - 7, x + 33, y + 12, 8, 0xCCF3FBFF);
        ArcadeTheme.rounded(context, x + 20, y - 11, x + 40, y + 10, 8, 0xAAF3FBFF);
    }

    private void drawGround(DrawContext context, int left, int top, int bottom) {
        int groundTop = top + arenaHeight - GROUND_HEIGHT;
        context.fill(left, groundTop, left + arenaWidth, bottom, 0xFFD9B45B);
        context.fill(left, groundTop, left + arenaWidth, groundTop + 7, 0xFF6FBE41);
        context.fill(left, groundTop + 7, left + arenaWidth, groundTop + 9, 0xFF4E9B2F);
        int offset = (int) (scroll % 22);
        for (int x = left - 22; x < left + arenaWidth; x += 22) {
            int stripe = x + offset;
            context.fill(stripe, groundTop + 12, stripe + 12, groundTop + 15, 0xFFB9943F);
            context.fill(stripe + 11, groundTop + 19, stripe + 21, groundTop + 22, 0xFFB9943F);
        }
    }

    private void drawPipes(DrawContext context, int left, int top) {
        int groundTop = top + arenaHeight - GROUND_HEIGHT;
        for (Pipe pipe : pipes) {
            int x = left + (int) pipe.x;
            int gapTop = top + pipe.gapCenter - PIPE_GAP / 2;
            int gapBottom = top + pipe.gapCenter + PIPE_GAP / 2;
            drawPipePart(context, x, top, gapTop, true);
            drawPipePart(context, x, gapBottom, groundTop, false);
        }
    }

    private void drawPipePart(DrawContext context, int x, int y1, int y2, boolean upward) {
        if (y2 <= y1) {
            return;
        }
        ArcadeTheme.rounded(context, x, y1, x + PIPE_WIDTH, y2, 3, 0xFF3E7F28);
        ArcadeTheme.rounded(context, x + 1, y1 + 1, x + PIPE_WIDTH - 1, y2 - 1, 3, 0xFF5AB934);
        context.fill(x + 4, y1 + 2, x + 11, y2 - 2, 0x557FF65B);
        context.fill(x + PIPE_WIDTH - 9, y1 + 2, x + PIPE_WIDTH - 4, y2 - 2, 0x33000000);
        if (upward) {
            ArcadeTheme.rounded(context, x - 4, y2 - 15, x + PIPE_WIDTH + 4, y2, 4, 0xFF3E7F28);
            ArcadeTheme.rounded(context, x - 3, y2 - 14, x + PIPE_WIDTH + 3, y2 - 1, 4, 0xFF68C93D);
            context.fill(x + 2, y2 - 12, x + 10, y2 - 3, 0x667FF65B);
        } else {
            ArcadeTheme.rounded(context, x - 4, y1, x + PIPE_WIDTH + 4, y1 + 15, 4, 0xFF3E7F28);
            ArcadeTheme.rounded(context, x - 3, y1 + 1, x + PIPE_WIDTH + 3, y1 + 14, 4, 0xFF68C93D);
            context.fill(x + 2, y1 + 2, x + 10, y1 + 11, 0x667FF65B);
        }
    }

    private void drawBird(DrawContext context, int left, int top) {
        int x = left + birdX();
        int y = top + (int) birdY;
        double angle = Math.max(-0.6, Math.min(1.1, birdVelocity / 7.0));
        int bodyOffsetY = (int) (angle * 3);
        int wingPhase = (int) (Math.sin(ticks * 0.55) * 3);
        if (gameOver) {
            wingPhase = 0;
        }
        ArcadeTheme.glow(context, x, y, 18, 0x33FFD66B);
        ArcadeTheme.disc(context, x, y + bodyOffsetY, 10, 0xFFFFD83D);
        ArcadeTheme.disc(context, x - 2, y + 4 + bodyOffsetY, 6, 0xFFFFE98A);
        ArcadeTheme.rounded(context, x - 10, y + wingPhase + bodyOffsetY, x + 2, y + 7 + wingPhase + bodyOffsetY, 3,
                0xFFFFB52E);
        ArcadeTheme.rounded(context, x - 8, y + wingPhase + bodyOffsetY, x, y + 3 + wingPhase + bodyOffsetY, 2,
                0xFFFFD06B);
        ArcadeTheme.disc(context, x + 4, y - 4 + bodyOffsetY, 4, 0xFFFFFFFF);
        ArcadeTheme.disc(context, x + 5, y - 4 + bodyOffsetY, 2, 0xFF1B2430);
        ArcadeTheme.rounded(context, x + 8, y + 1 + bodyOffsetY, x + 16, y + 5 + bodyOffsetY, 2, 0xFFFF7A3D);
        ArcadeTheme.rounded(context, x + 9, y + 2 + bodyOffsetY, x + 14, y + 3 + bodyOffsetY, 1, 0xFFFFC49B);
    }

    private void drawFeathers(DrawContext context, int left, int top) {
        for (double[] feather : feathers) {
            context.fill(left + (int) feather[0], top + (int) feather[1],
                    left + (int) feather[0] + 3, top + (int) feather[1] + 2, 0xFFFFE98A);
        }
        feathers.removeIf(feather -> {
            feather[1] += feather[3];
            feather[3] += 0.18;
            feather[4] -= 1;
            return feather[4] <= 0;
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0) {
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
