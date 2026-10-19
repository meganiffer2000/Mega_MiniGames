package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Pule a corda no momento certo para manter a sequência. */
public final class JumpRopeScreen extends Screen {
    private int phase = 20;
    private int jumps;
    private boolean jumpedThisRound;
    private boolean gameOver;

    public JumpRopeScreen() {
        super(Text.literal("Pular Corda"));
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Recomeçar (R)"), button -> newGame())
                .dimensions(width / 2 - 116, height - 34, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, height - 34, 112, 20).build());
    }

    private void newGame() {
        phase = 20;
        jumps = 0;
        jumpedThisRound = false;
        gameOver = false;
    }

    @Override
    public void tick() {
        if (gameOver) return;
        phase = (phase + 1) % 40;
        if (phase == 0) jumpedThisRound = false;
        if (phase == 10 && !jumpedThisRound) gameOver = true;
    }

    private void jump() {
        if (gameOver) return;
        if (phase < 10 && !jumpedThisRound) {
            jumpedThisRound = true;
            jumps++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 105, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Saltos: " + jumps),
                width / 2, height / 2 - 75, 0xFFE5EEFF);
        int left = width / 2 - 155;
        int top = height / 2 - 46;
        int color = phase < 10 ? 0xFFB84943 : phase >= 30 ? 0xFF4B985E : 0xFF394D60;
        context.fill(left, top, left + 310, top + 100, color);
        String instruction = gameOver ? "A corda pegou! Pressione R para recomeçar."
                : phase < 10 ? "PULE AGORA! Pressione ESPAÇO" : "Prepare-se... pule quando ficar vermelho!";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(instruction), width / 2,
                height / 2 - 5, 0xFFFFFFFF);
        if (jumpedThisRound && !gameOver) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Boa!"), width / 2,
                    height / 2 + 20, 0xFFFFE27A);
        }
        super.render(context, mouseX, mouseY, delta);
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
