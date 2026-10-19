package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Pescaria de reflexo: lance a linha e recolha quando o peixe morder. */
public final class FishingScreen extends Screen {
    private static final int READY = 0;
    private static final int WAITING = 1;
    private static final int BITE = 2;
    private static final int RESULT = 3;
    private int state = READY;
    private int waitTicks;
    private int biteTicks;
    private int fishCaught;
    private String message = "Clique em Lançar para começar";

    public FishingScreen() {
        super(Text.literal("Pescaria"));
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Lançar linha"), button -> cast())
                .dimensions(width / 2 - 116, height - 34, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, height - 34, 112, 20).build());
    }

    private void cast() {
        if (state == WAITING || state == BITE) return;
        state = WAITING;
        waitTicks = ThreadLocalRandom.current().nextInt(50, 121);
        message = "Linha na água... aguarde a mordida";
    }

    @Override
    public void tick() {
        if (state == WAITING && --waitTicks <= 0) {
            state = BITE;
            biteTicks = 30;
            message = "MORDEU! Clique ou pressione ESPAÇO agora!";
        } else if (state == BITE && --biteTicks <= 0) {
            state = RESULT;
            message = "O peixe escapou! Lance novamente.";
        }
    }

    private void reelIn() {
        if (state == BITE) {
            fishCaught++;
            state = RESULT;
            message = "Você pescou um peixe! Total: " + fishCaught;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF5BA6D4);
        context.fill(0, height / 2, width, height, 0xFF236A91);
        for (int i = 0; i < 8; i++) {
            int waveY = height / 2 + 18 + i * 24;
            context.fill(width / 2 - 140, waveY, width / 2 + 140, waveY + 2, 0x556FE2ED);
        }
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 105, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Peixes: " + fishCaught),
                width / 2, height / 2 - 78, 0xFFFFFFFF);
        int panelColor = state == BITE ? 0xFFFFD45B : 0xAA173955;
        context.fill(width / 2 - 170, height / 2 - 48, width / 2 + 170, height / 2 + 42, panelColor);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(message), width / 2,
                height / 2 - 4, state == BITE ? 0xFF1D2B34 : 0xFFFFFFFF);
        if (state == WAITING) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("..."), width / 2,
                    height / 2 + 20, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0 && state == BITE) {
            reelIn();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            reelIn();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER && (state == READY || state == RESULT)) {
            cast();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
