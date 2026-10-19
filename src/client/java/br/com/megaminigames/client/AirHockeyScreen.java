package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Hóquei de Ar: mesa vertical, disco com física própria e adversário que
 * persegue o disco. Primeiro a marcar sete gols vence a partida.
 */
public final class AirHockeyScreen extends ArcadeScreen {
    private static final int WIN_SCORE = 7;
    private static final int TRAIL = 10;

    private final List<float[]> trail = new ArrayList<>();
    private int left;
    private int top;
    private int right;
    private int bottom;
    private int midY;
    private int goalHalf;
    private float malletRadius;
    private float puckRadius;
    private float puckX;
    private float puckY;
    private float puckVX;
    private float puckVY;
    private float playerX;
    private float playerY;
    private float cpuX;
    private float cpuY;
    private float targetX;
    private float targetY;
    private int scorePlayer;
    private int scoreCpu;
    private int freeze;
    private int rally;
    private int bestRally;
    private int cpuLag;
    private boolean gameOver;
    private String flash = "";

    public AirHockeyScreen() {
        super("Hóquei de Ar");
    }

    @Override
    protected void init() {
        int tableWidth = Math.min(width - 36, 330);
        int tableHeight = height - 122;
        left = (width - tableWidth) / 2;
        top = 56;
        right = left + tableWidth;
        bottom = top + tableHeight;
        midY = (top + bottom) / 2;
        goalHalf = tableWidth / 5;
        malletRadius = Math.max(9, Math.min(14, tableWidth * 0.05F));
        puckRadius = Math.max(5, malletRadius * 0.56F);
        addFooterButtons(height - 26, this::restart);
        restart();
    }

    private void restart() {
        scorePlayer = 0;
        scoreCpu = 0;
        bestRally = 0;
        gameOver = false;
        flash = "";
        playerX = width / 2.0F;
        playerY = bottom - malletRadius - 18;
        targetX = playerX;
        targetY = playerY;
        cpuX = width / 2.0F;
        cpuY = top + malletRadius + 18;
        resetPuck(1);
    }

    private void resetPuck(int direction) {
        puckX = (left + right) / 2.0F;
        puckY = midY;
        puckVX = ThreadLocalRandom.current().nextFloat() * 1.6F - 0.8F;
        puckVY = direction * (1.6F + ThreadLocalRandom.current().nextFloat());
        freeze = 26;
        rally = 0;
        trail.clear();
    }

    @Override
    public void tick() {
        if (gameOver) return;
        if (freeze > 0) {
            freeze--;
            movePlayerMallet();
            moveCpuMallet();
            return;
        }
        movePlayerMallet();
        moveCpuMallet();
        puckX += puckVX;
        puckY += puckVY;
        puckVX *= 0.994F;
        puckVY *= 0.994F;
        if (Math.abs(puckVX) < 0.05F) puckVX = 0;
        float speed = (float) Math.sqrt(puckVX * puckVX + puckVY * puckVY);
        if (speed > 11) {
            puckVX = puckVX / speed * 11;
            puckVY = puckVY / speed * 11;
        }
        if (puckX - puckRadius < left) {
            puckX = left + puckRadius;
            puckVX = Math.abs(puckVX) * 0.92F;
        } else if (puckX + puckRadius > right) {
            puckX = right - puckRadius;
            puckVX = -Math.abs(puckVX) * 0.92F;
        }
        float center = (left + right) / 2.0F;
        boolean inGoalMouth = Math.abs(puckX - center) < goalHalf;
        if (puckY - puckRadius < top) {
            if (inGoalMouth) {
                scorePlayer++;
                flash = "GOL!";
                afterGoal(-1);
                return;
            }
            puckY = top + puckRadius;
            puckVY = Math.abs(puckVY) * 0.92F;
        } else if (puckY + puckRadius > bottom) {
            if (inGoalMouth) {
                scoreCpu++;
                flash = "GOL DO RIVAL";
                afterGoal(1);
                return;
            }
            puckY = bottom - puckRadius;
            puckVY = -Math.abs(puckVY) * 0.92F;
        }
        if (hitMallet(playerX, playerY)) {
            bounceFrom(playerX, playerY);
            rally++;
            bestRally = Math.max(bestRally, rally);
        } else if (hitMallet(cpuX, cpuY)) {
            bounceFrom(cpuX, cpuY);
            rally++;
            bestRally = Math.max(bestRally, rally);
        }
        if (rally > 0 && speed < 0.6F) rally = 0;
        trail.add(new float[] {puckX, puckY});
        while (trail.size() > TRAIL) trail.remove(0);
        if (scorePlayer >= WIN_SCORE || scoreCpu >= WIN_SCORE) {
            gameOver = true;
        }
    }

    private void afterGoal(int direction) {
        if (scorePlayer >= WIN_SCORE || scoreCpu >= WIN_SCORE) {
            gameOver = true;
            freeze = 0;
            return;
        }
        resetPuck(direction);
    }

    private boolean hitMallet(float malletX, float malletY) {
        float dx = puckX - malletX;
        float dy = puckY - malletY;
        float limit = malletRadius + puckRadius;
        return dx * dx + dy * dy < limit * limit;
    }

    private void bounceFrom(float malletX, float malletY) {
        float dx = puckX - malletX;
        float dy = puckY - malletY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < 0.01F) {
            dx = 0;
            dy = playerY > midY ? -1 : 1;
            distance = 1;
        }
        float nx = dx / distance;
        float ny = dy / distance;
        puckX = malletX + nx * (malletRadius + puckRadius + 0.6F);
        puckY = malletY + ny * (malletRadius + puckRadius + 0.6F);
        float incoming = puckVX * nx + puckVY * ny;
        if (incoming < 0) {
            puckVX -= 2 * incoming * nx;
            puckVY -= 2 * incoming * ny;
        }
        puckVX += nx * 2.1F;
        puckVY += ny * 2.1F;
    }

    private void movePlayerMallet() {
        float dx = targetX - playerX;
        float dy = targetY - playerY;
        float limit = 7;
        playerX += clamp(dx, -limit, limit);
        playerY += clamp(dy, -limit, limit);
        keepInside(false);
    }

    private void moveCpuMallet() {
        float homeX = (left + right) / 2.0F;
        float homeY = top + (midY - top) * 0.5F;
        float aimX = homeX;
        float aimY = homeY;
        if (puckY < midY + 30 && freeze <= 0) {
            aimX = puckX + puckVX * 5;
            aimY = puckY - puckVY * 3;
            if (aimY > midY - malletRadius - 6) aimY = midY - malletRadius - 6;
        }
        if (++cpuLag % 4 == 0 || puckY > midY + 30) {
            cpuX += clamp(aimX - cpuX, -2.5F, 2.5F);
            cpuY += clamp(aimY - cpuY, -2.5F, 2.5F);
        }
        keepInside(true);
    }

    private void keepInside(boolean cpu) {
        float x;
        float y;
        if (cpu) {
            x = cpuX;
            y = cpuY;
        } else {
            x = playerX;
            y = playerY;
        }
        float minY = cpu ? top + malletRadius + 2 : midY + malletRadius + 2;
        float maxY = cpu ? midY - malletRadius - 2 : bottom - malletRadius - 2;
        x = clamp(x, left + malletRadius + 2, right - malletRadius - 2);
        y = clamp(y, minY, maxY);
        if (cpu) {
            cpuX = x;
            cpuY = y;
        } else {
            playerX = x;
            playerY = y;
        }
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        targetX = (float) mouseX;
        targetY = (float) mouseY;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        targetX = (float) mouseX;
        targetY = (float) mouseY;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            restart();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 10, ArcadeTheme.SKY);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? "Partida encerrada" : "Mova o mouse na sua metade para rebater"),
                width / 2, 30, ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "VOCÊ " + scorePlayer, left, 45, ArcadeTheme.SKY);
        ArcadeTheme.pillRight(context, textRenderer, "RIVAL " + scoreCpu, right, 45, ArcadeTheme.CORAL);

        ArcadeTheme.rounded(context, left - 6, top - 6, right + 6, bottom + 6, 12, 0x66000000);
        ArcadeTheme.rounded(context, left - 5, top - 5, right + 5, bottom + 5, 11, ArcadeTheme.SKY);
        ArcadeTheme.rounded(context, left - 3, top - 3, right + 3, bottom + 3, 10, 0xFF0B1B2A);
        context.fillGradient(left, top, right, bottom, 0xFF153C55, 0xFF0D2536);
        context.fill(left, midY - 1, right, midY + 1, 0x66FFFFFF);
        ArcadeTheme.ring(context, (left + right) / 2, midY, (right - left) / 5, 1, 0x44FFFFFF);
        ArcadeTheme.disc(context, (left + right) / 2, midY, 3, 0x66FFFFFF);

        drawGoal(context, true);
        drawGoal(context, false);
        for (int index = 0; index < trail.size(); index++) {
            float[] point = trail.get(index);
            int alpha = (index + 1) * 14;
            ArcadeTheme.disc(context, (int) point[0], (int) point[1], Math.max(1, (int) (puckRadius * 0.7F)),
                    ArcadeTheme.alpha(ArcadeTheme.GOLD, Math.min(160, alpha)));
        }
        ArcadeTheme.glow(context, (int) puckX, (int) puckY, (int) (puckRadius * 2.4F), 0x66FFD66B);
        ArcadeTheme.disc(context, (int) puckX, (int) puckY, (int) puckRadius, 0xFF131A24);
        ArcadeTheme.ring(context, (int) puckX, (int) puckY, (int) puckRadius, 2, ArcadeTheme.GOLD);

        drawMallet(context, (int) playerX, (int) playerY, ArcadeTheme.SKY, true);
        drawMallet(context, (int) cpuX, (int) cpuY, ArcadeTheme.CORAL, false);

        if (flash != null && !flash.isEmpty() && (freeze > 0 || gameOver)) {
            ArcadeTheme.banner(context, textRenderer, gameOver
                            ? (scorePlayer > scoreCpu ? "VOCÊ VENCEU!" : "O RIVAL VENCEU")
                            : flash,
                    gameOver ? "R para uma revanche  •  sequência " + bestRally : "Prepare o disco...",
                    width / 2, midY, Math.min(width - 24, 260),
                    scorePlayer > scoreCpu ? ArcadeTheme.SUCCESS : ArcadeTheme.CORAL);
        } else if (freeze > 0) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("PREPARE-SE"), width / 2, midY - 30,
                    ArcadeTheme.GOLD);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawGoal(DrawContext context, boolean topGoal) {
        int center = (left + right) / 2;
        int y1 = topGoal ? top - 5 : bottom;
        int y2 = topGoal ? top : bottom + 5;
        context.fill(center - goalHalf, y1, center + goalHalf, y2, 0xFF6E2233);
        context.fill(center - goalHalf, topGoal ? y1 : y2 - 2, center + goalHalf, topGoal ? y1 + 2 : y2,
                topGoal ? ArcadeTheme.CORAL : ArcadeTheme.SKY);
        for (int x = center - goalHalf + 2; x < center + goalHalf; x += 6) {
            context.fill(x, y1, x + 1, y2, 0x66FFFFFF);
        }
    }

    private void drawMallet(DrawContext context, int x, int y, int color, boolean player) {
        int radius = (int) malletRadius;
        ArcadeTheme.glow(context, x, y, radius + 4, ArcadeTheme.alpha(color, 70));
        ArcadeTheme.disc(context, x, y, radius, ArcadeTheme.darken(color, 60));
        ArcadeTheme.disc(context, x, y, radius - 2, color);
        ArcadeTheme.disc(context, x - radius / 4, y - radius / 4, radius / 3, 0x66FFFFFF);
        ArcadeTheme.ring(context, x, y, radius, 2, 0xAAFFFFFF);
        ArcadeTheme.ring(context, x, y, Math.max(2, radius - 4), 1, ArcadeTheme.alpha(0xFFFFFFFF, 120));
        if (player) {
            ArcadeTheme.disc(context, x, y, Math.max(1, radius / 4), 0xFFFFFFFF);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
