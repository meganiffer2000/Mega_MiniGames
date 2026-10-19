package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Digitação rápida: escreva o maior número de palavras em um minuto. */
public final class TypingSprintScreen extends Screen {
    private static final String[] WORDS = {
            "CASA", "MUNDO", "JOGO", "LIVRO", "PEDRA", "NUVEM", "PLANTA", "CAMINHO",
            "FLORESTA", "PRAIA", "TECLADO", "MUSICA", "ESTRELA", "AMIGOS", "CIDADE",
            "AVENTURA", "JANELA", "PIPOCA", "PLANETA", "CASTELO", "MONTANHA", "GIRASSOL"
    };
    private String target;
    private final StringBuilder typed = new StringBuilder();
    private int ticksLeft;
    private int score;
    private int mistakes;
    private boolean gameOver;

    public TypingSprintScreen() {
        super(Text.literal("Digitação Rápida"));
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Nova partida"), button -> newGame())
                .dimensions(width / 2 - 116, height - 34, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, height - 34, 112, 20).build());
        if (target == null) newGame();
    }

    private void newGame() {
        ticksLeft = 1200;
        score = 0;
        mistakes = 0;
        gameOver = false;
        typed.setLength(0);
        nextWord();
    }

    private void nextWord() {
        String next;
        do {
            next = WORDS[ThreadLocalRandom.current().nextInt(WORDS.length)];
        } while (next.equals(target) && WORDS.length > 1);
        target = next;
    }

    @Override
    public void tick() {
        if (!gameOver && --ticksLeft <= 0) gameOver = true;
    }

    private void submitWord() {
        if (gameOver || typed.isEmpty()) return;
        if (typed.toString().equals(target)) score++;
        else mistakes++;
        typed.setLength(0);
        nextWord();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 94, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Tempo: " + ticksLeft / 20 + "s  •  Certas: " + score + "  •  Erros: " + mistakes),
                width / 2, height / 2 - 65, 0xFFDDEEFF);
        if (gameOver) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Fim! Você digitou " + score + " palavras."),
                    width / 2, height / 2, 0xFF78E88B);
        } else {
            context.fill(width / 2 - 150, height / 2 - 35, width / 2 + 150, height / 2 + 20, 0xFF2A3D4D);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(target), width / 2,
                    height / 2 - 25, 0xFFFFD65A);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(typed + "_"), width / 2,
                    height / 2 + 1, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Digite a palavra e pressione Enter • Backspace apaga"),
                    width / 2, height / 2 + 45, 0xFFD3DEE8);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submitWord();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !typed.isEmpty() && !gameOver) {
            typed.deleteCharAt(typed.length() - 1);
            return true;
        }
        if (!gameOver && keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z && typed.length() < 20) {
            typed.append((char) ('A' + keyCode - GLFW.GLFW_KEY_A));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
