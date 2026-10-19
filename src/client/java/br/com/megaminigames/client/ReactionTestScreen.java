package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Teste de reflexo: espere o sinal verde e clique o mais rápido possível. */
public final class ReactionTestScreen extends Screen {
    private static final int WAITING = 0;
    private static final int READY = 1;
    private static final int RESULT = 2;
    private static final int FALSE_START = 3;
    private int mode;
    private int delayTicks;
    private int delayTotal = 1;
    private int reactionTicks;
    private int reactionMillis;
    private int ticks;
    private int bestMillis;
    private int attempts;

    public ReactionTestScreen() {
        super(Text.literal("Teste de Reflexo"));
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, height - 30, buttonWidth, 20,
                Text.literal("Tentar novamente"), this::start));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, height - 30, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (delayTicks == 0 && mode == 0 && reactionTicks == 0) {
            start();
        }
    }

    private void start() {
        mode = WAITING;
        delayTicks = ThreadLocalRandom.current().nextInt(60, 161);
        delayTotal = delayTicks;
        reactionTicks = 0;
        reactionMillis = 0;
    }

    @Override
    public void tick() {
        ticks++;
        if (mode == WAITING) {
            if (--delayTicks <= 0) {
                mode = READY;
                reactionTicks = 0;
            }
        } else if (mode == READY) {
            reactionTicks++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.GOLD);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(mode == READY ? "CLIQUE AGORA!" : mode == FALSE_START
                        ? "Queimou a largada — tente de novo" : "Espere o painel ficar verde"),
                width / 2, 32, mode == READY ? ArcadeTheme.SUCCESS : mode == FALSE_START
                        ? ArcadeTheme.DANGER : ArcadeTheme.MUTED);

        int panelWidth = Math.min(340, width - 40);
        int panelHeight = Math.min(190, height - 180);
        int left = (width - panelWidth) / 2;
        int top = Math.max(60, (height - panelHeight - 96) / 2 + 26);
        int right = left + panelWidth;
        int bottom = top + panelHeight;
        ArcadeTheme.pill(context, textRenderer, "TENTATIVAS  " + attempts, left, top - 22, ArcadeTheme.GOLD);
        ArcadeTheme.pillRight(context, textRenderer, "RECORDE  " + (bestMillis == 0 ? "—" : bestMillis + "ms"),
                right, top - 22, ArcadeTheme.SKY);

        int accent = mode == READY ? ArcadeTheme.SUCCESS : mode == FALSE_START ? ArcadeTheme.DANGER
                : mode == RESULT ? ArcadeTheme.SKY : ArcadeTheme.GOLD;
        if (mode == READY) {
            int pulse = 20 + (int) (Math.sin(ticks * 0.5) * 16);
            ArcadeTheme.glow(context, width / 2, top + panelHeight / 2, panelWidth / 2 + 10,
                    ArcadeTheme.alpha(ArcadeTheme.SUCCESS, 40 + pulse));
        }
        ArcadeTheme.rounded(context, left - 6, top - 6, right + 6, bottom + 6, 13, ArcadeTheme.alpha(accent, 110));
        ArcadeTheme.rounded(context, left - 5, top - 5, right + 5, bottom + 5, 12, ArcadeTheme.PANEL_DEEP);
        context.fillGradient(left, top, right, bottom, ArcadeTheme.alpha(ArcadeTheme.darken(accent, 110), 190),
                ArcadeTheme.alpha(0xFF0B1626, 240));
        context.fill(left, top, right, top + 3, ArcadeTheme.alpha(accent, 200));

        int centerX = width / 2;
        int centerY = top + panelHeight / 2;
        if (mode == WAITING) {
            ArcadeTheme.ring(context, centerX, centerY - 12, 20, 2, 0x66FFD66B);
            int dots = (ticks / 8) % 4;
            StringBuilder text = new StringBuilder();
            for (int index = 0; index < 3; index++) {
                text.append(index < dots ? "•" : "·");
            }
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(text.toString()),
                    centerX, centerY - 17, ArcadeTheme.GOLD);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Aguarde o verde..."),
                    centerX, centerY + 18, ArcadeTheme.TEXT);
            float progress = 1.0F - delayTicks / (float) Math.max(1, delayTotal);
            ArcadeTheme.progress(context, left + 24, bottom - 16, panelWidth - 48, 3, progress, ArcadeTheme.GOLD);
        } else if (mode == READY) {
            ArcadeTheme.disc(context, centerX, centerY - 12, 24, 0x33B9F26B);
            ArcadeTheme.disc(context, centerX, centerY - 12, 16, ArcadeTheme.LIME);
            ArcadeTheme.drawCheck(context, centerX - 9, centerY - 22, 0xFF0B2010);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("CLIQUE!"), centerX,
                    centerY + 22, ArcadeTheme.TEXT);
        } else if (mode == RESULT) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(reactionMillis + " ms"), centerX, centerY - 26, ArcadeTheme.GOLD);
            String rating = reactionMillis < 220 ? "Reflexos incríveis!" : reactionMillis < 320
                    ? "Muito rápido!" : reactionMillis < 450 ? "Bom tempo." : "Continue treinando!";
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(rating), centerX, centerY + 2,
                    ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Recorde: " + bestMillis + " ms  •  clique em Tentar novamente"),
                    centerX, centerY + 22, ArcadeTheme.MUTED);
        } else {
            ArcadeTheme.drawX(context, centerX, centerY - 14, 16, 3, ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Queimou a largada!"),
                    centerX, centerY + 16, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Espere o verde antes de clicar"),
                    centerX, centerY + 30, ArcadeTheme.MUTED);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }
        if (mode == READY) {
            reactionMillis = reactionTicks * 50;
            attempts++;
            if (bestMillis == 0 || reactionMillis < bestMillis) {
                bestMillis = reactionMillis;
            }
            mode = RESULT;
            return true;
        }
        if (mode == WAITING) {
            mode = FALSE_START;
            return true;
        }
        if (mode == RESULT || mode == FALSE_START) {
            start();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE) {
            if (mode == READY) {
                reactionMillis = reactionTicks * 50;
                attempts++;
                if (bestMillis == 0 || reactionMillis < bestMillis) {
                    bestMillis = reactionMillis;
                }
                mode = RESULT;
            } else if (mode == WAITING) {
                mode = FALSE_START;
            } else {
                start();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
