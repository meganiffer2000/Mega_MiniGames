package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Pinball Neon: mesa com três amortecedores, alvos luminosos e flippers.
 * Segure A/D (ou clique nos lados) para bater e mantenha a bola em jogo.
 */
public final class PinballScreen extends ArcadeScreen {
    private static final float GRAVITY = 0.115F;
    private static final float MAX_SPEED = 7.2F;
    private static final float BALL_RADIUS = 3.4F;
    private static final float FLIPPER_RADIUS = 3.0F;

    private final List<float[]> trail = new ArrayList<>();
    private int left;
    private int top;
    private int right;
    private int bottom;
    private int tableWidth;
    private int tableHeight;
    private float ballX;
    private float ballY;
    private float velocityX;
    private float velocityY;
    private float leftAngle;
    private float rightAngle;
    private float leftOmega;
    private float rightOmega;
    private boolean leftHeld;
    private boolean rightHeld;
    private boolean waiting;
    private int respawnTicks;
    private int score;
    private int balls;
    private int ticks;
    private boolean gameOver;
    private final boolean[] targets = new boolean[3];
    private final int[] bumperHits = new int[3];
    private final float[] bumperX = new float[3];
    private final float[] bumperY = new float[3];
    private final float[] targetX = new float[3];
    private final float[] targetY = new float[3];
    private int targetFlash;
    private String message = "A / D movem os flippers  •  Espaço lança";

    public PinballScreen() {
        super("Pinball Neon");
    }

    @Override
    protected void init() {
        tableWidth = Math.min(width - 36, 320);
        tableHeight = height - 94;
        left = (width - tableWidth) / 2;
        top = 60;
        right = left + tableWidth;
        bottom = top + tableHeight;
        bumperX[0] = left + tableWidth * 0.30F;
        bumperY[0] = top + tableHeight * 0.28F;
        bumperX[1] = left + tableWidth * 0.70F;
        bumperY[1] = top + tableHeight * 0.28F;
        bumperX[2] = left + tableWidth * 0.50F;
        bumperY[2] = top + tableHeight * 0.44F;
        targetY[0] = targetY[1] = targetY[2] = top + tableHeight * 0.10F;
        targetX[0] = left + tableWidth * 0.28F;
        targetX[1] = left + tableWidth * 0.50F;
        targetX[2] = left + tableWidth * 0.72F;
        addFooterButtons(height - 26, this::restart);
        restart();
    }

    private void restart() {
        score = 0;
        balls = 3;
        gameOver = false;
        waiting = true;
        respawnTicks = 0;
        for (int index = 0; index < targets.length; index++) targets[index] = true;
        resetBall();
    }

    private void resetBall() {
        ballX = left + tableWidth / 2.0F;
        ballY = bottom - tableHeight * 0.16F;
        velocityX = 0;
        velocityY = 0;
        trail.clear();
        leftAngle = 0.44F;
        rightAngle = (float) (Math.PI - 0.44F);
        message = "Espaço para lançar";
    }

    private float pivotLeftX() {
        return left + tableWidth * 0.29F;
    }

    private float pivotLeftY() {
        return bottom - tableHeight * 0.12F;
    }

    private float pivotRightX() {
        return left + tableWidth * 0.71F;
    }

    private float pivotRightY() {
        return bottom - tableHeight * 0.12F;
    }

    private float flipperLength() {
        return Math.min(tableWidth * 0.175F, tableHeight * 0.24F);
    }

    private float bumperRadius() {
        return Math.min(tableWidth * 0.072F, tableHeight * 0.13F);
    }

    @Override
    public void tick() {
        ticks++;
        if (gameOver) return;
        updateFlippers();
        if (waiting) {
            if (respawnTicks > 0) respawnTicks--;
            return;
        }
        moveBall(velocityX);
        moveBallY(velocityY);
        velocityY += GRAVITY;
        velocityX *= 0.996F;
        velocityY *= 0.996F;
        float speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (speed > MAX_SPEED) {
            velocityX = velocityX / speed * MAX_SPEED;
            velocityY = velocityY / speed * MAX_SPEED;
        }
        if (speed < 0.08F && ballY > bottom - tableHeight * 0.25F) {
            velocityX += ThreadLocalRandom.current().nextFloat() * 0.4F - 0.2F;
        }
        collideBumpers();
        collideTargets();
        collideFlippers();
        collideSlingshots();
        trail.add(new float[] {ballX, ballY});
        while (trail.size() > 12) trail.remove(0);
        if (ballY - BALL_RADIUS > bottom) {
            loseBall();
        }
        if (targetFlash > 0) targetFlash--;
    }

    private void moveBall(float amount) {
        ballX += amount;
        if (ballX - BALL_RADIUS < left) {
            ballX = left + BALL_RADIUS;
            velocityX = Math.abs(velocityX) * 0.85F;
        } else if (ballX + BALL_RADIUS > right) {
            ballX = right - BALL_RADIUS;
            velocityX = -Math.abs(velocityX) * 0.85F;
        }
    }

    private void moveBallY(float amount) {
        ballY += amount;
        if (ballY - BALL_RADIUS < top) {
            ballY = top + BALL_RADIUS;
            velocityY = Math.abs(velocityY) * 0.82F;
        }
    }

    private void loseBall() {
        balls--;
        if (balls <= 0) {
            gameOver = true;
            return;
        }
        waiting = true;
        respawnTicks = 30;
        resetBall();
        message = "Bola perdida  •  restam " + balls;
    }

    private void updateFlippers() {
        float leftTarget = leftHeld ? -0.38F : 0.44F;
        float rightTarget = rightHeld ? (float) (Math.PI + 0.38F) : (float) (Math.PI - 0.44F);
        float previousLeft = leftAngle;
        float previousRight = rightAngle;
        float speed = leftHeld ? 0.34F : 0.20F;
        if (leftAngle < leftTarget) leftAngle = Math.min(leftTarget, leftAngle + speed);
        else leftAngle = Math.max(leftTarget, leftAngle - speed);
        speed = rightHeld ? 0.34F : 0.20F;
        if (rightAngle < rightTarget) rightAngle = Math.min(rightTarget, rightAngle + speed);
        else rightAngle = Math.max(rightTarget, rightAngle - speed);
        leftOmega = leftAngle - previousLeft;
        rightOmega = rightAngle - previousRight;
    }

    private void collideBumpers() {
        for (int index = 0; index < 3; index++) {
            float radius = bumperRadius();
            float dx = ballX - bumperX[index];
            float dy = ballY - bumperY[index];
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            float limit = radius + BALL_RADIUS;
            if (distance >= limit || distance <= 0.01F) continue;
            float nx = dx / distance;
            float ny = dy / distance;
            ballX = bumperX[index] + nx * (limit + 0.5F);
            ballY = bumperY[index] + ny * (limit + 0.5F);
            float incoming = velocityX * nx + velocityY * ny;
            if (incoming < 0) {
                velocityX -= 2 * incoming * nx;
                velocityY -= 2 * incoming * ny;
            }
            velocityX += nx * 2.7F;
            velocityY += ny * 2.7F;
            score += 100;
            bumperHits[index] = 8;
        }
        for (int index = 0; index < 3; index++) {
            if (bumperHits[index] > 0) bumperHits[index]--;
        }
    }

    private void collideTargets() {
        for (int index = 0; index < 3; index++) {
            if (!targets[index]) continue;
            float dx = ballX - targetX[index];
            float dy = ballY - targetY[index];
            if (dx * dx + dy * dy > 170) continue;
            targets[index] = false;
            score += 250;
            targetFlash = 10;
            velocityY = Math.abs(velocityY) * 0.8F + 0.6F;
            if (!targets[0] && !targets[1] && !targets[2]) {
                score += 1000;
                for (int reset = 0; reset < 3; reset++) targets[reset] = true;
                message = "Bônus de alvos! +1000";
            }
        }
    }

    private void collideFlippers() {
        collideFlipper(pivotLeftX(), pivotLeftY(), leftAngle, leftOmega, flipperLength());
        collideFlipper(pivotRightX(), pivotRightY(), rightAngle, rightOmega, flipperLength());
    }

    private void collideFlipper(float pivotX, float pivotY, float angle, float omega, float length) {
        float tipX = pivotX + (float) Math.cos(angle) * length;
        float tipY = pivotY + (float) Math.sin(angle) * length;
        float dx = tipX - pivotX;
        float dy = tipY - pivotY;
        float lengthSquared = dx * dx + dy * dy;
        float t = ((ballX - pivotX) * dx + (ballY - pivotY) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        float closestX = pivotX + dx * t;
        float closestY = pivotY + dy * t;
        float offsetX = ballX - closestX;
        float offsetY = ballY - closestY;
        float distance = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
        float limit = BALL_RADIUS + FLIPPER_RADIUS;
        if (distance >= limit) return;
        if (distance < 0.01F) {
            offsetX = 0;
            offsetY = -1;
            distance = 1;
        }
        float nx = offsetX / distance;
        float ny = offsetY / distance;
        ballX = closestX + nx * (limit + 0.4F);
        ballY = closestY + ny * (limit + 0.4F);
        float incoming = velocityX * nx + velocityY * ny;
        if (incoming < 0) {
            velocityX -= 1.65F * incoming * nx;
            velocityY -= 1.65F * incoming * ny;
        }
        float kick = Math.abs(omega) * length * t;
        velocityX += nx * (0.9F + kick * 1.7F);
        velocityY += ny * (0.9F + kick * 1.7F);
        if (Math.abs(omega) > 0.05F) score += 10;
    }

    private void collideSlingshots() {
        float radius = Math.min(tableWidth * 0.028F, tableHeight * 0.06F);
        float y1 = bottom - tableHeight * 0.20F;
        float y2 = bottom - tableHeight * 0.42F;
        bounceSegment(left + tableWidth * 0.11F, y1, left + tableWidth * 0.20F, y2, radius, 1.9F);
        bounceSegment(right - tableWidth * 0.11F, y1, right - tableWidth * 0.20F, y2, radius, 1.9F);
    }

    private void bounceSegment(float x1, float y1, float x2, float y2, float radius, float power) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float lengthSquared = dx * dx + dy * dy;
        float t = ((ballX - x1) * dx + (ballY - y1) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        float closestX = x1 + dx * t;
        float closestY = y1 + dy * t;
        float offsetX = ballX - closestX;
        float offsetY = ballY - closestY;
        float distance = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
        float limit = BALL_RADIUS + radius;
        if (distance >= limit) return;
        if (distance < 0.01F) {
            offsetX = 0;
            offsetY = -1;
            distance = 1;
        }
        float nx = offsetX / distance;
        float ny = offsetY / distance;
        ballX = closestX + nx * (limit + 0.4F);
        ballY = closestY + ny * (limit + 0.4F);
        velocityX += nx * power;
        velocityY += ny * power;
        score += 25;
    }

    private void launch() {
        if (!waiting || respawnTicks > 0 || gameOver) return;
        waiting = false;
        velocityX = ThreadLocalRandom.current().nextFloat() * 1.6F - 0.8F;
        velocityY = -5.4F;
        message = "Boa sorte!";
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            restart();
            return true;
        }
        switch (keyCode) {
            case 263, 65 -> leftHeld = true;
            case 262, 68 -> rightHeld = true;
            case 32 -> {
                launch();
                return true;
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 263, 65 -> leftHeld = false;
            case 262, 68 -> rightHeld = false;
            default -> {
                return super.keyReleased(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        if (mouseX < width / 2.0) {
            leftHeld = true;
        } else {
            rightHeld = true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) return true;
        leftHeld = false;
        rightHeld = false;
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 8, ArcadeTheme.MAGENTA);
        ArcadeTheme.status(context, textRenderer, Text.literal(message), width / 2, 28, ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "PONTOS " + score, left, 43, ArcadeTheme.MAGENTA);
        ArcadeTheme.pillRight(context, textRenderer, "BOLAS " + balls, right, 43, ArcadeTheme.GOLD);

        ArcadeTheme.rounded(context, left - 8, top - 8, right + 8, bottom + 8, 14, 0x77000000);
        ArcadeTheme.rounded(context, left - 7, top - 7, right + 7, bottom + 7, 13, ArcadeTheme.MAGENTA);
        ArcadeTheme.rounded(context, left - 5, top - 5, right + 5, bottom + 5, 11, 0xFF0B0E24);
        context.fillGradient(left, top, right, bottom, 0xFF1A1240, 0xFF0A0B1B);
        for (int index = 0; index < 5; index++) {
            int y = top + tableHeight * (index + 1) / 6;
            context.fill(left + 6, y, right - 6, y + 1, 0x11FFFFFF);
        }
        ArcadeTheme.ring(context, left + tableWidth / 2, top + tableHeight / 2, tableWidth / 4, 1, 0x22FFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("MEGA"), left + tableWidth / 2,
                top + tableHeight / 2 - 12, 0x22FFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("PINBALL"), left + tableWidth / 2,
                top + tableHeight / 2, 0x22FFFFFF);

        for (int index = 0; index < 3; index++) {
            drawTarget(context, index);
        }
        for (int index = 0; index < 3; index++) {
            drawBumper(context, index);
        }
        drawFlipper(context, pivotLeftX(), pivotLeftY(), leftAngle, leftHeld);
        drawFlipper(context, pivotRightX(), pivotRightY(), rightAngle, rightHeld);
        drawSlingshot(context, true);
        drawSlingshot(context, false);

        for (int index = 0; index < trail.size(); index++) {
            float[] point = trail.get(index);
            ArcadeTheme.disc(context, (int) point[0], (int) point[1], 1,
                    ArcadeTheme.alpha(ArcadeTheme.GOLD, (index + 1) * 9));
        }
        if (!gameOver) {
            ArcadeTheme.glow(context, (int) ballX, (int) ballY, 8, 0x88FFE9A8);
            ArcadeTheme.disc(context, (int) ballX, (int) ballY, (int) BALL_RADIUS + 1, 0xFF8A6A2E);
            ArcadeTheme.disc(context, (int) ballX, (int) ballY, (int) BALL_RADIUS, 0xFFFFF0C9);
            ArcadeTheme.disc(context, (int) ballX - 1, (int) ballY - 1, 1, 0xFFFFFFFF);
        }

        if (waiting && !gameOver) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(respawnTicks > 0 ? "Preparando..." : "ESPAÇO / CLIQUE PARA LANÇAR"),
                    width / 2, bottom - tableHeight / 2, ArcadeTheme.GOLD);
        }
        if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, "FIM DE JOGO",
                    "Pontos " + score + "  •  R para outra mesa", width / 2,
                    top + tableHeight / 2, Math.min(width - 20, 280), ArcadeTheme.MAGENTA);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBumper(DrawContext context, int index) {
        int radius = (int) bumperRadius();
        int x = (int) bumperX[index];
        int y = (int) bumperY[index];
        int color = index == 0 ? ArcadeTheme.SKY : index == 1 ? ArcadeTheme.MAGENTA : ArcadeTheme.LIME;
        if (bumperHits[index] > 0) {
            ArcadeTheme.glow(context, x, y, radius + 8, 0xAAFFFFFF);
        }
        ArcadeTheme.glow(context, x, y, radius + 4, ArcadeTheme.alpha(color, 90));
        ArcadeTheme.disc(context, x, y, radius, ArcadeTheme.darken(color, 90));
        ArcadeTheme.ring(context, x, y, radius, 3, color);
        ArcadeTheme.ring(context, x, y, Math.max(2, radius - 5), 1, ArcadeTheme.alpha(color, 120));
        ArcadeTheme.disc(context, x, y, Math.max(2, radius / 3), ArcadeTheme.lighten(color, 30));
        context.fill(x - radius / 3, y - radius + 2, x + radius / 3, y - radius + 4, 0x66FFFFFF);
    }

    private void drawTarget(DrawContext context, int index) {
        int x = (int) targetX[index];
        int y = (int) targetY[index];
        int color = targets[index] ? ArcadeTheme.GOLD : 0xFF3A3F55;
        if (targets[index]) {
            ArcadeTheme.glow(context, x, y, 10, 0x66FFD66B);
        }
        ArcadeTheme.rounded(context, x - 9, y - 4, x + 9, y + 4, 3, 0xFF161227);
        ArcadeTheme.rounded(context, x - 8, y - 3, x + 8, y + 3, 2, color);
        context.fill(x - 6, y - 2, x + 6, y - 1, 0x88FFFFFF);
        if (targetFlash > 0) {
            ArcadeTheme.ring(context, x, y, 12, 1, ArcadeTheme.alpha(ArcadeTheme.GOLD, targetFlash * 20));
        }
    }

    private void drawFlipper(DrawContext context, float pivotX, float pivotY, float angle, boolean pressed) {
        float length = flipperLength();
        float tipX = pivotX + (float) Math.cos(angle) * length;
        float tipY = pivotY + (float) Math.sin(angle) * length;
        int color = pressed ? ArcadeTheme.GOLD : 0xFFE5B984;
        drawThickLine(context, pivotX, pivotY, tipX, tipY, (int) FLIPPER_RADIUS + 1, color);
        ArcadeTheme.disc(context, (int) tipX, (int) tipY, (int) FLIPPER_RADIUS + 1,
                ArcadeTheme.lighten(color, 30));
        ArcadeTheme.disc(context, (int) pivotX, (int) pivotY, 3, 0xFF2A2138);
        ArcadeTheme.ring(context, (int) pivotX, (int) pivotY, 3, 1, ArcadeTheme.GOLD);
    }

    private void drawSlingshot(DrawContext context, boolean isLeft) {
        float x1 = isLeft ? left + tableWidth * 0.11F : right - tableWidth * 0.11F;
        float x2 = isLeft ? left + tableWidth * 0.20F : right - tableWidth * 0.20F;
        float y1 = bottom - tableHeight * 0.20F;
        float y2 = bottom - tableHeight * 0.42F;
        drawThickLine(context, x1, y1, x2, y2, 3, ArcadeTheme.SKY);
        ArcadeTheme.disc(context, (int) x1, (int) y1, 3, ArcadeTheme.lighten(ArcadeTheme.SKY, 40));
    }

    private void drawThickLine(DrawContext context, float x1, float y1, float x2, float y2, int radius, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        int steps = Math.max(1, (int) (length / 2));
        for (int step = 0; step <= steps; step++) {
            float t = step / (float) steps;
            ArcadeTheme.disc(context, (int) (x1 + dx * t), (int) (y1 + dy * t), radius, color);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
