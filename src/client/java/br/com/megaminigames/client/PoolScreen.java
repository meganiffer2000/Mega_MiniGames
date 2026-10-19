package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Sinuca: mire com o mouse, segure para carregar a força e encaçape as nove bolas. */
public final class PoolScreen extends ArcadeScreen {
    private static final int BALLS = 9;
    private static final int[] BALL_COLORS = {
            ArcadeTheme.GOLD, ArcadeTheme.SKY, ArcadeTheme.CORAL, ArcadeTheme.PURPLE, ArcadeTheme.MAGENTA,
            ArcadeTheme.LIME, ArcadeTheme.TEAL, 0xFF33405C, ArcadeTheme.GOLD
    };
    private static final float FRICTION = 0.985F;
    private static final float CUSHION = 0.82F;

    private final List<Ball> balls = new ArrayList<>();
    private int tableLeft;
    private int tableTop;
    private int tableWidth;
    private int tableHeight;
    private int pocketRadius;
    private float aimX;
    private float aimY;
    private boolean charging;
    private float charge;
    private int score;
    private int remaining;
    private boolean gameOver;
    private boolean won;
    private int ticks;
    private String message = "Mire com o mouse, segure e solte para tacar";
    private int messageTicks;

    public PoolScreen() {
        super("Sinuca");
    }

    @Override
    protected void init() {
        tableWidth = Math.max(160, Math.min(500, width - 28));
        tableHeight = Math.max(120, Math.min(250, height - 108));
        tableLeft = (width - tableWidth) / 2;
        tableTop = 62 + Math.max(0, (height - 104 - tableHeight) / 2);
        pocketRadius = Math.max(8, Math.min(13, tableHeight / 18));
        addFooterButtons(height - 26, this::newGame);
        newGame();
    }

    private void newGame() {
        balls.clear();
        score = 0;
        remaining = BALLS;
        gameOver = false;
        won = false;
        charge = 0;
        charging = false;
        aimX = tableLeft + tableWidth * 0.3F;
        aimY = tableTop + tableHeight / 2.0F;
        message = "Mire com o mouse, segure e solte para tacar";
        messageTicks = 0;
        Ball cue = new Ball();
        cue.cue = true;
        cue.radius = Math.max(5, Math.min(8, tableHeight / 30.0F));
        resetCue(cue);
        balls.add(cue);
        float radius = cue.radius;
        float rackX = tableLeft + tableWidth * 0.72F;
        float centerY = tableTop + tableHeight / 2.0F;
        int[][] layout = {{0}, {-1, 1}, {-2, 0, 2}, {-1, 1}, {0}};
        int number = 1;
        for (int row = 0; row < layout.length; row++) {
            for (int column : layout[row]) {
                Ball ball = new Ball();
                ball.number = number;
                ball.radius = radius;
                ball.x = rackX + row * radius * 2.02F;
                ball.y = centerY + column * radius * 2.05F;
                ball.color = BALL_COLORS[number - 1];
                balls.add(ball);
                number++;
                if (number > BALLS) break;
            }
            if (number > BALLS) break;
        }
    }

    private void resetCue(Ball cue) {
        cue.x = tableLeft + tableWidth * 0.26F;
        cue.y = tableTop + tableHeight / 2.0F;
        cue.vx = 0;
        cue.vy = 0;
        cue.sunk = false;
    }

    private boolean moving() {
        for (Ball ball : balls) {
            if (ball.sunk) continue;
            if (Math.hypot(ball.vx, ball.vy) > 0.05F) return true;
        }
        return false;
    }

    @Override
    public void tick() {
        ticks++;
        if (messageTicks > 0) messageTicks--;
        if (gameOver) return;
        if (charging) {
            charge = Math.min(1.0F, charge + 0.022F);
        }
        List<Ball> active = new ArrayList<>();
        for (Ball ball : balls) {
            if (!ball.sunk) active.add(ball);
        }
        for (Ball ball : active) {
            ball.x += ball.vx;
            ball.y += ball.vy;
            ball.vx *= FRICTION;
            ball.vy *= FRICTION;
            if (Math.hypot(ball.vx, ball.vy) < 0.03F) {
                ball.vx = 0;
                ball.vy = 0;
            }
            bounce(ball);
        }
        for (int first = 0; first < active.size(); first++) {
            for (int second = first + 1; second < active.size(); second++) {
                collide(active.get(first), active.get(second));
            }
        }
        for (Ball ball : active) {
            checkPocket(ball);
        }
        if (remaining <= 0 && !gameOver) {
            gameOver = true;
            won = true;
            score += 50;
        }
    }

    private void bounce(Ball ball) {
        float left = tableLeft + ball.radius;
        float right = tableLeft + tableWidth - ball.radius;
        float top = tableTop + ball.radius;
        float bottom = tableTop + tableHeight - ball.radius;
        if (ball.x < left) {
            ball.x = left;
            ball.vx = Math.abs(ball.vx) * CUSHION;
        } else if (ball.x > right) {
            ball.x = right;
            ball.vx = -Math.abs(ball.vx) * CUSHION;
        }
        if (ball.y < top) {
            ball.y = top;
            ball.vy = Math.abs(ball.vy) * CUSHION;
        } else if (ball.y > bottom) {
            ball.y = bottom;
            ball.vy = -Math.abs(ball.vy) * CUSHION;
        }
    }

    private void collide(Ball first, Ball second) {
        float dx = second.x - first.x;
        float dy = second.y - first.y;
        float distance = (float) Math.hypot(dx, dy);
        float minimum = first.radius + second.radius;
        if (distance >= minimum || distance <= 0.0001F) return;
        float nx = dx / distance;
        float ny = dy / distance;
        float overlap = minimum - distance;
        first.x -= nx * overlap / 2.0F;
        first.y -= ny * overlap / 2.0F;
        second.x += nx * overlap / 2.0F;
        second.y += ny * overlap / 2.0F;
        float relative = (first.vx - second.vx) * nx + (first.vy - second.vy) * ny;
        if (relative <= 0) return;
        float impulse = relative * 0.97F;
        first.vx -= impulse * nx;
        first.vy -= impulse * ny;
        second.vx += impulse * nx;
        second.vy += impulse * ny;
    }

    private void checkPocket(Ball ball) {
        if (ball.sunk) return;
        float[][] pockets = {
                {tableLeft + pocketRadius * 0.4F, tableTop + pocketRadius * 0.4F},
                {tableLeft + tableWidth / 2.0F, tableTop - pocketRadius * 0.2F},
                {tableLeft + tableWidth - pocketRadius * 0.4F, tableTop + pocketRadius * 0.4F},
                {tableLeft + pocketRadius * 0.4F, tableTop + tableHeight - pocketRadius * 0.4F},
                {tableLeft + tableWidth / 2.0F, tableTop + tableHeight + pocketRadius * 0.2F},
                {tableLeft + tableWidth - pocketRadius * 0.4F, tableTop + tableHeight - pocketRadius * 0.4F}
        };
        for (float[] pocket : pockets) {
            if (Math.hypot(ball.x - pocket[0], ball.y - pocket[1]) < pocketRadius * 0.82F) {
                ball.sunk = true;
                ball.vx = 0;
                ball.vy = 0;
                if (ball.cue) {
                    score -= 5;
                    message = "Bola branca na caçapa! −5 pontos";
                    messageTicks = 70;
                    resetCue(ball);
                } else {
                    remaining--;
                    score += 10;
                    message = "Bola " + ball.number + " encaçapada! +10";
                    messageTicks = 60;
                }
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !gameOver) {
            if (moving()) {
                message = "Espere as bolas pararem";
                messageTicks = 40;
                return true;
            }
            charging = true;
            charge = 0;
            aimX = (float) mouseX;
            aimY = (float) mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && charging && !gameOver) {
            charging = false;
            shoot();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void shoot() {
        Ball cue = balls.get(0);
        if (cue.sunk) return;
        float dx = aimX - cue.x;
        float dy = aimY - cue.y;
        float length = (float) Math.hypot(dx, dy);
        if (length < 1) return;
        float speed = 1.4F + charge * 7.2F;
        cue.vx = dx / length * speed;
        cue.vy = dy / length * speed;
        charge = 0;
        message = "";
        messageTicks = 0;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            newGame();
            return true;
        }
        if (keyCode == 32 && !gameOver && !moving() && !charging) {
            charge = Math.min(1.0F, charge + 0.12F);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 32 && !gameOver) {
            shoot();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.TEAL);
        ArcadeTheme.status(context, textRenderer, Text.literal(message), width / 2, 32,
                messageTicks > 0 ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "PONTOS " + score, tableLeft, 47, ArcadeTheme.TEAL);
        ArcadeTheme.pillRight(context, textRenderer, "BOLAS " + remaining + "/" + BALLS,
                tableLeft + tableWidth, 47, ArcadeTheme.GOLD);

        ArcadeTheme.rounded(context, tableLeft - 14, tableTop - 14, tableLeft + tableWidth + 14,
                tableTop + tableHeight + 14, 16, 0x55000000);
        ArcadeTheme.rounded(context, tableLeft - 13, tableTop - 13, tableLeft + tableWidth + 13,
                tableTop + tableHeight + 13, 15, 0xFF5A3820);
        ArcadeTheme.rounded(context, tableLeft - 10, tableTop - 10, tableLeft + tableWidth + 10,
                tableTop + tableHeight + 10, 13, 0xFF7A4E2A);
        context.fillGradient(tableLeft - 6, tableTop - 6, tableLeft + tableWidth + 6,
                tableTop + tableHeight + 6, 0xFF1E7A4A, 0xFF0F5233);
        context.fillGradient(tableLeft, tableTop, tableLeft + tableWidth, tableTop + tableHeight,
                0xFF17734A, 0xFF0B4A2C);
        for (int index = 0; index < 5; index++) {
            int x = tableLeft + tableWidth * (index + 1) / 6;
            context.fill(x, tableTop, x + 1, tableTop + tableHeight, 0x0AFFFFFF);
        }
        context.fill(tableLeft, tableTop + tableHeight / 2, tableLeft + tableWidth,
                tableTop + tableHeight / 2 + 1, 0x0AFFFFFF);

        float[][] pockets = {
                {tableLeft + pocketRadius * 0.4F, tableTop + pocketRadius * 0.4F},
                {tableLeft + tableWidth / 2.0F, tableTop - pocketRadius * 0.2F},
                {tableLeft + tableWidth - pocketRadius * 0.4F, tableTop + pocketRadius * 0.4F},
                {tableLeft + pocketRadius * 0.4F, tableTop + tableHeight - pocketRadius * 0.4F},
                {tableLeft + tableWidth / 2.0F, tableTop + tableHeight + pocketRadius * 0.2F},
                {tableLeft + tableWidth - pocketRadius * 0.4F, tableTop + tableHeight - pocketRadius * 0.4F}
        };
        for (float[] pocket : pockets) {
            ArcadeTheme.disc(context, (int) pocket[0], (int) pocket[1], pocketRadius, 0xFF2A1A10);
            ArcadeTheme.disc(context, (int) pocket[0], (int) pocket[1], pocketRadius - 3, 0xFF05080C);
        }

        Ball cue = balls.get(0);
        if (!cue.sunk && !moving() && !gameOver) {
            float targetX = charging ? aimX : mouseX;
            float targetY = charging ? aimY : mouseY;
            drawAim(context, cue, targetX, targetY);
        }
        for (Ball ball : balls) {
            if (ball.sunk) continue;
            drawBall(context, ball);
        }
        if (charging) {
            int barWidth = Math.min(180, tableWidth - 40);
            ArcadeTheme.progress(context, (width - barWidth) / 2, tableTop + tableHeight - 12, barWidth, 6,
                    charge, charge > 0.8F ? ArcadeTheme.DANGER : ArcadeTheme.GOLD);
        }
        if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, won ? "MESA LIMPA!" : "FIM DE JOGO",
                    "Pontuação: " + score, width / 2, tableTop + tableHeight / 2,
                    Math.min(width - 20, tableWidth), won ? ArcadeTheme.SUCCESS : ArcadeTheme.GOLD);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawAim(DrawContext context, Ball cue, float targetX, float targetY) {
        float dx = targetX - cue.x;
        float dy = targetY - cue.y;
        float length = (float) Math.hypot(dx, dy);
        if (length < 1) return;
        float nx = dx / length;
        float ny = dy / length;
        for (int step = 0; step < 60; step += 6) {
            int x = (int) (cue.x + nx * (cue.radius + step));
            int y = (int) (cue.y + ny * (cue.radius + step));
            context.fill(x, y, x + 2, y + 2, 0x99FFFFFF);
        }
        float stickStart = cue.radius + 6;
        float stickLength = 46 + charge * 26;
        for (float step = 0; step <= stickLength; step += 2.0F) {
            int x = (int) (cue.x - nx * (stickStart + step));
            int y = (int) (cue.y - ny * (stickStart + step));
            ArcadeTheme.disc(context, x, y, 2, 0xFFB98552);
            ArcadeTheme.disc(context, x, y, 1, 0xFFE5B984);
        }
        ArcadeTheme.disc(context, (int) (cue.x - nx * stickStart),
                (int) (cue.y - ny * stickStart), 2, 0xFF4FC3F7);
    }

    private void drawBall(DrawContext context, Ball ball) {
        int x = (int) ball.x;
        int y = (int) ball.y;
        int radius = (int) ball.radius;
        ArcadeTheme.disc(context, x + 1, y + 2, radius, 0x55000000);
        ArcadeTheme.glow(context, x, y, radius + 2, ArcadeTheme.alpha(ball.cue ? 0xFFFFFFFF : ball.color, 40));
        ArcadeTheme.disc(context, x, y, radius, arcadeDark(ball.cue ? 0xFFFFFFFF : ball.color));
        ArcadeTheme.disc(context, x - 1, y - 1, Math.max(1, radius - 1),
                ball.cue ? 0xFFFFFFFF : ball.color);
        if (!ball.cue && radius >= 5) {
            ArcadeTheme.disc(context, x, y, Math.max(2, radius / 2), 0xFFF7FAFF);
            String number = Integer.toString(ball.number);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(number), x, y - 4, 0xFF16233A);
        }
        ArcadeTheme.disc(context, x - radius / 3, y - radius / 3, Math.max(1, radius / 4), 0x99FFFFFF);
    }

    private static int arcadeDark(int color) {
        return ArcadeTheme.darken(color, 60);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Ball {
        private float x;
        private float y;
        private float vx;
        private float vy;
        private float radius;
        private int color;
        private int number;
        private boolean cue;
        private boolean sunk;
    }
}
