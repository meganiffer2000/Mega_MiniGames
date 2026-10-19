package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Teste de reflexo: espere o sinal verde e clique o mais rápido possível. */
public final class ReactionTestScreen extends Screen {
    private static final int WAITING = 0;
    private static final int READY = 1;
    private static final int RESULT = 2;
    private static final int FALSE_START = 3;
    private int mode;
    private int delayTicks;
    private int reactionTicks;
    private int reactionMillis;

    public ReactionTestScreen() {
        super(Text.literal("Teste de Reflexo"));
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Tentar novamente"), button -> start())
                .dimensions(width / 2 - 116, height - 35, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, height - 35, 112, 20).build());
        if (delayTicks == 0 && mode == 0 && reactionTicks == 0) start();
    }

    private void start() {
        mode = WAITING;
        delayTicks = ThreadLocalRandom.current().nextInt(60, 161);
        reactionTicks = 0;
        reactionMillis = 0;
    }

    @Override
    public void tick() {
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
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 90, 0xFFFFFFFF);
        int panelLeft = width / 2 - 165;
        int panelTop = height / 2 - 55;
        int panelColor = mode == READY ? 0xFF36A852 : mode == FALSE_START ? 0xFFAC3434 : 0xFF34485A;
        context.fill(panelLeft, panelTop, width / 2 + 165, height / 2 + 65, panelColor);
        String message = switch (mode) {
            case WAITING -> "Aguarde o verde... não clique ainda!";
            case READY -> "CLIQUE AGORA!";
            case RESULT -> "Seu tempo: " + reactionMillis + " ms";
            default -> "Queimou a largada! Tente novamente.";
        };
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(message), width / 2,
                height / 2 - 4, 0xFFFFFFFF);
        if (mode == RESULT) {
            String rating = reactionMillis < 220 ? "Reflexos incríveis!" : reactionMillis < 320
                    ? "Muito rápido!" : "Continue treinando!";
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(rating), width / 2,
                    height / 2 + 22, 0xFFE7F1FF);
        }
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("O sinal aparece após um intervalo aleatório. Clique somente quando ficar verde."),
                width / 2, height / 2 + 88, 0xFFD6E0EA);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        if (mode == READY) {
            reactionMillis = reactionTicks * 50;
            mode = RESULT;
            return true;
        }
        if (mode == WAITING) {
            mode = FALSE_START;
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
