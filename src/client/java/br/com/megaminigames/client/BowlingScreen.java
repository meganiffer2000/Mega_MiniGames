package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Boliche: mire, ajuste a força e derrube o máximo de pinos em cinco rodadas. */
public final class BowlingScreen extends Screen {
    private static final int FRAMES = 5;
    private static final int PINS = 10;

    private final float[] pinX = new float[PINS];
    private final float[] pinY = new float[PINS];
    private final boolean[] standing = new boolean[PINS];
    private final boolean[] fallen = new boolean[PINS];
    private float ballX;
    private float ballY;
    private float ballVX;
    private float ballVY;
    private float aimX;
    private float power;
    private int powerDirection = 1;
    private int frame = 1;
    private int roll = 1;
    private int score;
    private int framePins;
    private int phase;
    private int settleTicks;
    private int messageTicks;
    private String message = "Clique na pista para mirar";
    private boolean gameOver;
    private int ticks;
    private int laneLeft;
    private int laneTop;
    private int laneWidth;
    private int laneHeight;
    private int pinTop;

    public BowlingScreen() {
        super(Text.literal("Boliche"));
    }

    @Override
    protected void init() {
        laneWidth = Math.max(120, Math.min(210, width - 66));
        laneHeight = Math.max(150, Math.min(300, height - 132));
        laneLeft = (width - laneWidth) / 2;
        laneTop = Math.max(64, (height - laneHeight - 66) / 2 + 18);
        pinTop = laneTop + (int) (laneHeight * 0.26F);
        int buttonY = Math.min(height - 24, laneTop + laneHeight + 8);
        int buttonWidth = Math.min(112, Math.max(74, (width - 40) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Nova partida"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        newGame();
    }

    private void newGame() {
        aimX = laneLeft + laneWidth / 2.0F;
        score = 0;
        frame = 1;
        roll = 1;
        framePins = 0;
        phase = 0;
        gameOver = false;
        message = "Clique na pista para mirar";
        messageTicks = 0;
        resetPins();
        resetBall();
    }

    private void resetPins() {
        java.util.Arrays.fill(standing, true);
        java.util.Arrays.fill(fallen, false);
        float spacing = laneWidth / 9.0F;
        int index = 0;
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column <= row; column++) {
                pinX[index] = laneLeft + laneWidth / 2.0F + (column - row / 2.0F) * spacing;
                pinY[index] = pinTop + row * spacing * 0.92F;
                index++;
            }
        }
    }

    private void resetBall() {
        ballX = aimX > 0 ? aimX : laneLeft + laneWidth / 2.0F;
        ballY = laneTop + laneHeight - 16;
        ballVX = 0;
        ballVY = 0;
    }

    @Override
    public void tick() {
        ticks++;
        if (messageTicks > 0) messageTicks--;
        if (gameOver) return;
        if (phase == 1) {
            power += powerDirection * 0.028F;
            if (power >= 1.0F) {
                power = 1.0F;
                powerDirection = -1;
            } else if (power <= 0.0F) {
                power = 0.0F;
                powerDirection = 1;
            }
        } else if (phase == 2) {
            ballX += ballVX;
            ballY += ballVY;
            ballVX *= 0.995F;
            checkPins();
            if (ballY < laneTop - 26 || (ballY < laneTop + 6 && Math.abs(ballVY) < 0.2F)) {
                settle();
            }
        } else if (phase == 3) {
            settleTicks--;
            if (settleTicks <= 0) nextRoll();
        }
    }

    private void checkPins() {
        for (int index = 0; index < PINS; index++) {
            if (!standing[index]) continue;
            if (Math.abs(ballX - pinX[index]) < 8 && Math.abs(ballY - pinY[index]) < 10) {
                knock(index);
            }
        }
    }

    private void knock(int index) {
        if (!standing[index]) return;
        standing[index] = false;
        fallen[index] = true;
        framePins++;
        if (ThreadLocalRandom.current().nextInt(100) < 55) {
            for (int other = 0; other < PINS; other++) {
                if (standing[other] && Math.hypot(pinX[other] - pinX[index], pinY[other] - pinY[index]) < 15
                        && ThreadLocalRandom.current().nextInt(100) < 60) {
                    standing[other] = false;
                    fallen[other] = true;
                    framePins++;
                }
            }
        }
    }

    private void settle() {
        phase = 3;
        settleTicks = 26;
        ballX = laneLeft + laneWidth / 2.0F;
        ballY = laneTop + laneHeight - 16;
        ballVX = 0;
        ballVY = 0;
        if (framePins >= PINS) {
            message = "STRIKE! Todos os pinos!";
        } else if (framePins == 0) {
            message = "Bola na canaleta...";
        } else {
            message = framePins + " pino(s) derrubado(s)";
        }
        messageTicks = 60;
    }

    private void nextRoll() {
        score += framePins;
        if (roll == 1 && framePins < PINS) {
            roll = 2;
            phase = 0;
            message = "Segunda bola da rodada " + frame;
            messageTicks = 50;
            resetBall();
            return;
        }
        frame++;
        framePins = 0;
        roll = 1;
        if (frame > FRAMES) {
            gameOver = true;
            message = "Fim de jogo!";
            return;
        }
        resetPins();
        resetBall();
        phase = 0;
        message = "Rodada " + frame + " de " + FRAMES;
        messageTicks = 50;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || gameOver) return super.mouseClicked(mouseX, mouseY, button);
        boolean inLane = mouseX >= laneLeft - 6 && mouseX <= laneLeft + laneWidth + 18
                && mouseY >= laneTop - 8 && mouseY <= laneTop + laneHeight + 10;
        if (!inLane && mouseY >= laneTop + laneHeight + 4) return super.mouseClicked(mouseX, mouseY, button);
        if (phase == 0) {
            if (!inLane) return super.mouseClicked(mouseX, mouseY, button);
            float clamped = (float) Math.max(laneLeft + 12, Math.min(laneLeft + laneWidth - 12, mouseX));
            aimX = clamped;
            ballX = clamped;
            power = 0.15F;
            powerDirection = 1;
            phase = 1;
            message = "Clique para lançar — barra indica a força";
            messageTicks = 0;
            return true;
        }
        if (phase == 1) {
            float strength = 1.6F + power * 3.2F;
            ballVX = (aimX - (laneLeft + laneWidth / 2.0F)) * 0.006F;
            ballVY = -strength;
            phase = 2;
            message = "";
            return true;
        }
        if (phase == 3 && settleTicks > 4) {
            settleTicks = 1;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.CORAL);
        ArcadeTheme.status(context, textRenderer, Text.literal(message), width / 2, 31,
                gameOver ? ArcadeTheme.GOLD : messageTicks > 0 ? ArcadeTheme.TEXT : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "PONTOS " + score, laneLeft, 47, ArcadeTheme.CORAL);
        ArcadeTheme.pillRight(context, textRenderer, "RODADA " + Math.min(frame, FRAMES) + "/" + FRAMES
                + "  •  BOLA " + roll, laneLeft + laneWidth, 47, ArcadeTheme.TEAL);

        int laneBottom = laneTop + laneHeight;
        ArcadeTheme.rounded(context, laneLeft - 16, laneTop - 10, laneLeft + laneWidth + 16, laneBottom + 12, 14,
                0x55000000);
        context.fill(laneLeft - 13, laneTop - 8, laneLeft + laneWidth + 13, laneBottom + 8, 0xFF101B2A);
        context.fillGradient(laneLeft, laneTop, laneLeft + laneWidth, laneBottom, 0xFF9B6234, 0xFFC98B52);
        for (int index = 1; index < 12; index++) {
            int x = laneLeft + laneWidth * index / 12;
            context.fill(x, laneTop, x + 1, laneBottom, 0x1A4A2A12);
        }
        for (int index = 0; index < 6; index++) {
            int y = laneTop + laneHeight - 42 - index * 9;
            int arrowY = laneTop + laneHeight - 34 - index * 9;
            ArcadeTheme.polygon(context, new int[]{laneLeft + laneWidth / 2 - 4, laneLeft + laneWidth / 2,
                            laneLeft + laneWidth / 2 + 4},
                    new int[]{arrowY + 5, arrowY, arrowY + 5}, 0x66FFF0D0);
        }
        context.fill(laneLeft, laneBottom - 5, laneLeft + laneWidth, laneBottom, 0x33300F04);

        for (int index = 0; index < PINS; index++) {
            int x = (int) pinX[index];
            int y = (int) pinY[index];
            if (standing[index]) {
                ArcadeTheme.rounded(context, x - 3, y + 2, x + 3, y + 8, 3, 0x44000000);
                context.fill(x - 1, y - 6, x + 1, y + 4, 0xFFE8EEF8);
                ArcadeTheme.disc(context, x, y - 6, 2, 0xFFFFFFFF);
                ArcadeTheme.rounded(context, x - 2, y - 2, x + 2, y + 7, 2, 0xFFF7FAFF);
                context.fill(x - 2, y - 3, x + 2, y - 1, 0xFFE0465C);
                ArcadeTheme.disc(context, x - 1, y - 7, 1, 0xFFE0465C);
            } else if (fallen[index]) {
                context.fill(x - 3, y - 1, x + 3, y + 2, 0x88E8EEF8);
                context.fill(x - 1, y - 1, x + 1, y + 1, 0x88E0465C);
            }
        }

        if (phase == 0 || phase == 1) {
            int aim = (int) (aimX > 0 ? aimX : mouseX);
            aim = Math.max(laneLeft + 8, Math.min(laneLeft + laneWidth - 8, aim));
            context.fill(aim, laneTop + laneHeight - 30, aim + 1, laneTop + laneHeight - 8, 0xAAFFD66B);
            ArcadeTheme.polygon(context, new int[]{aim - 3, aim + 4, aim - 3},
                    new int[]{laneTop + laneHeight - 32, laneTop + laneHeight - 28, laneTop + laneHeight - 24},
                    ArcadeTheme.GOLD);
        }
        if (phase == 1) {
            int barHeight = Math.min(120, laneHeight - 60);
            ArcadeTheme.rounded(context, laneLeft + laneWidth + 5, laneTop + 6, laneLeft + laneWidth + 13,
                    laneTop + 12 + barHeight, 4, 0xAA08111E);
            int filled = (int) ((barHeight) * power);
            context.fillGradient(laneLeft + laneWidth + 6, laneTop + 12 + barHeight - filled,
                    laneLeft + laneWidth + 12, laneTop + 12 + barHeight,
                    ArcadeTheme.SUCCESS, power > 0.75F ? ArcadeTheme.DANGER : ArcadeTheme.GOLD);
        }

        int ballRadius = 5;
        ArcadeTheme.glow(context, (int) ballX, (int) ballY, ballRadius + 5, 0x6689C4FF);
        ArcadeTheme.disc(context, (int) ballX, (int) ballY + 1, ballRadius, 0x55000000);
        ArcadeTheme.disc(context, (int) ballX, (int) ballY, ballRadius, 0xFF16233A);
        ArcadeTheme.disc(context, (int) ballX - 1, (int) ballY - 1, ballRadius - 2, 0xFF3E5A86);
        ArcadeTheme.disc(context, (int) ballX - 1, (int) ballY - 1, Math.max(1, ballRadius / 3), 0xAA9BC4FF);
        context.fill((int) ballX - 2, (int) ballY - 3, (int) ballX - 1, (int) ballY - 2, 0xFF08111E);
        context.fill((int) ballX, (int) ballY - 3, (int) ballX + 1, (int) ballY - 2, 0xFF08111E);
        context.fill((int) ballX - 2, (int) ballY, (int) ballX - 1, (int) ballY + 1, 0xFF08111E);

        if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, "FIM DE JOGO", "Pontuação final: " + score,
                    width / 2, laneTop + laneHeight / 2, Math.min(width - 20, laneWidth + 20),
                    score >= 30 ? ArcadeTheme.SUCCESS : ArcadeTheme.GOLD);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            newGame();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
