package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Pule a corda no momento certo para manter a sequência. */
public final class JumpRopeScreen extends Screen {
    private static final int CYCLE = 40;
    private static final int JUMP_WINDOW = 10;
    private int phase = 20;
    private int jumps;
    private int best;
    private int ticks;
    private boolean jumpedThisRound;
    private boolean gameOver;

    public JumpRopeScreen() {
        super(Text.literal("Pular Corda"));
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, height - 30, buttonWidth, 20,
                Text.literal("Recomeçar (R)"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, height - 30, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
    }

    private void newGame() {
        if (jumps > best) {
            best = jumps;
        }
        phase = 20;
        jumps = 0;
        jumpedThisRound = false;
        gameOver = false;
    }

    @Override
    public void tick() {
        ticks++;
        if (gameOver) {
            return;
        }
        phase = (phase + 1) % CYCLE;
        if (phase == 0) {
            jumpedThisRound = false;
        }
        if (phase == JUMP_WINDOW && !jumpedThisRound) {
            gameOver = true;
            if (jumps > best) {
                best = jumps;
            }
        }
    }

    private void jump() {
        if (gameOver) {
            return;
        }
        if (phase < JUMP_WINDOW && !jumpedThisRound) {
            jumpedThisRound = true;
            jumps++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.GOLD);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? "A corda pegou! Pressione R para recomeçar"
                        : phase < JUMP_WINDOW ? "PULE AGORA! ESPAÇO" : "Prepare-se... pule quando ficar dourado!"),
                width / 2, 32, gameOver ? ArcadeTheme.DANGER : phase < JUMP_WINDOW ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        int sceneWidth = Math.min(400, width - 40);
        int sceneHeight = Math.min(200, height - 140);
        int left = (width - sceneWidth) / 2;
        int top = Math.max(58, (height - sceneHeight - 96) / 2 + 26);
        int right = left + sceneWidth;
        int bottom = top + sceneHeight;
        ArcadeTheme.pill(context, textRenderer, "SALTOS  " + jumps, left, top - 22, ArcadeTheme.GOLD);
        ArcadeTheme.pillRight(context, textRenderer, "RECORDE  " + Math.max(best, jumps), right, top - 22,
                ArcadeTheme.SKY);

        ArcadeTheme.rounded(context, left - 6, top - 6, right + 6, bottom + 6, 12,
                ArcadeTheme.alpha(ArcadeTheme.GOLD, 100));
        ArcadeTheme.rounded(context, left - 5, top - 5, right + 5, bottom + 5, 11, ArcadeTheme.PANEL_DEEP);
        context.fillGradient(left, top, right, bottom - sceneHeight / 3, 0xFF17263C, 0xFF1D3550);
        context.fillGradient(left, top + sceneHeight / 2, right, bottom, 0xFF1D3550, 0xFF132033);
        for (int index = 0; index < 26; index++) {
            int starX = left + (index * 89 + 31) % sceneWidth;
            int starY = top + (index * 47 + 13) % (sceneHeight / 2);
            int alpha = 70 + (int) (Math.sin((ticks + index * 9) * 0.08) * 50);
            context.fill(starX, starY, starX + 1, starY + 1, ArcadeTheme.alpha(0xFFFFFFFF, Math.max(30, alpha)));
        }
        int floorY = bottom - 18;
        context.fill(left, floorY, right, bottom, 0xFF0F1926);
        context.fill(left, floorY, right, floorY + 2, 0xFF2C465F);

        int centerX = left + sceneWidth / 2;
        int characterY = floorY - 34;
        boolean airborne = phase < JUMP_WINDOW && jumpedThisRound;
        int hop = airborne ? (int) (Math.sin(phase / (float) JUMP_WINDOW * Math.PI) * 26) : 0;
        int bodyY = characterY - hop;

        int ropeAngle = (int) (phase / (float) CYCLE * 360);
        drawRope(context, centerX, floorY, sceneWidth / 2 - 20, ropeAngle);
        drawCharacter(context, centerX, bodyY, airborne, gameOver);

        int barWidth = Math.min(sceneWidth - 60, 260);
        int barX = centerX - barWidth / 2;
        int barY = bottom - 12;
        ArcadeTheme.progress(context, barX, barY, barWidth, 4, phase / (float) CYCLE,
                phase < JUMP_WINDOW ? ArcadeTheme.GOLD : ArcadeTheme.TEAL);
        if (!gameOver) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(jumpedThisRound ? "BOA!" : phase < JUMP_WINDOW ? "PULE!" : "espera…"),
                    centerX, top + 14, jumpedThisRound ? ArcadeTheme.SUCCESS
                            : phase < JUMP_WINDOW ? ArcadeTheme.GOLD : ArcadeTheme.DIM);
        }

        if (gameOver) {
            int centerY = top + sceneHeight / 2;
            int bannerWidth = Math.min(sceneWidth - 30, 240);
            ArcadeTheme.rounded(context, centerX - bannerWidth / 2, centerY - 34,
                    centerX + bannerWidth / 2, centerY + 34, 11, 0xF00A1422);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("A CORDA PEGOU!"),
                    centerX, centerY - 22, ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Saltos: " + jumps),
                    centerX, centerY - 4, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Recorde: " + Math.max(best, jumps)),
                    centerX, centerY + 8, ArcadeTheme.GOLD);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("R para recomeçar"),
                    centerX, centerY + 22, ArcadeTheme.MUTED);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawRope(DrawContext context, int centerX, int floorY, int radius, int angle) {
        int steps = 18;
        double startAngle = Math.toRadians(angle);
        for (int step = 0; step < steps; step++) {
            double a1 = startAngle + Math.PI * step / steps;
            double a2 = startAngle + Math.PI * (step + 1) / steps;
            int x1 = centerX + (int) (Math.cos(a1) * radius);
            int y1 = floorY - 26 - (int) (Math.sin(a1) * radius * 0.85);
            int x2 = centerX + (int) (Math.cos(a2) * radius);
            int y2 = floorY - 26 - (int) (Math.sin(a2) * radius * 0.85);
            int thickness = 2;
            int stepsLine = Math.max(1, Math.abs(x2 - x1));
            for (int index = 0; index <= stepsLine; index++) {
                int x = x1 + (x2 - x1) * index / stepsLine;
                int y = y1 + (y2 - y1) * index / stepsLine;
                int color = step % 2 == 0 ? 0xFFF0509B : 0xFFB9F26B;
                context.fill(x, y, x + thickness, y + thickness, color);
            }
        }
    }

    private void drawCharacter(DrawContext context, int centerX, int feetY, boolean airborne, boolean fallen) {
        int color = fallen ? ArcadeTheme.CORAL : ArcadeTheme.SKY;
        ArcadeTheme.glow(context, centerX, feetY - 12, 22, fallen ? 0x33FF8A65 : 0x336CC9F5);
        ArcadeTheme.disc(context, centerX, feetY - 28, 8, 0xFFFFE0BD);
        ArcadeTheme.rounded(context, centerX - 9, feetY - 28, centerX + 9, feetY - 20, 5, 0xFF2A3B52);
        ArcadeTheme.rounded(context, centerX - 7, feetY - 20, centerX + 7, feetY - 2, 5, color);
        ArcadeTheme.rounded(context, centerX - 7, feetY - 6, centerX - 2, feetY, 2, 0xFF203348);
        ArcadeTheme.rounded(context, centerX + 2, feetY - 6, centerX + 7, feetY, 2, 0xFF203348);
        ArcadeTheme.disc(context, centerX - 3, feetY - 30, 1, 0xFF1B2430);
        ArcadeTheme.disc(context, centerX + 3, feetY - 30, 1, 0xFF1B2430);
        if (airborne) {
            context.fill(centerX - 4, feetY - 25, centerX + 4, feetY - 24, 0xFF9A5B4A);
        }
        int armY = feetY - 18;
        ArcadeTheme.rounded(context, centerX - 13, armY, centerX - 7, armY + 3, 1, color);
        ArcadeTheme.rounded(context, centerX + 7, armY, centerX + 13, armY + 3, 1, color);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            jump();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
