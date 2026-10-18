package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Adivinhe o número secreto de 1 a 100 em até sete tentativas. */
public final class NumberGuessScreen extends Screen {
    private int secret;
    private int attempts;
    private String input = "";
    private String message = "Digite um número de 1 a 100 e pressione Enter";
    private boolean gameOver;

    public NumberGuessScreen() {
        super(Text.literal("Adivinhe o Número"));
    }

    @Override
    protected void init() {
        int buttonY = height - 35;
        addDrawableChild(ButtonWidget.builder(Text.literal("Novo jogo"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (secret == 0) newGame();
    }

    private void newGame() {
        secret = ThreadLocalRandom.current().nextInt(1, 101);
        attempts = 0;
        input = "";
        message = "Digite um número de 1 a 100 e pressione Enter";
        gameOver = false;
    }

    private void submitGuess() {
        if (gameOver || input.isEmpty()) return;
        int guess;
        try {
            guess = Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            input = "";
            return;
        }
        input = "";
        if (guess < 1 || guess > 100) {
            message = "O número precisa estar entre 1 e 100.";
            return;
        }
        attempts++;
        if (guess == secret) {
            message = "Acertou em " + attempts + (attempts == 1 ? " tentativa!" : " tentativas!");
            gameOver = true;
        } else if (attempts >= 7) {
            message = "As tentativas acabaram. O número era " + secret + ".";
            gameOver = true;
        } else {
            message = (guess < secret ? "Tente um número MAIOR." : "Tente um número MENOR.")
                    + " Restam " + (7 - attempts) + " tentativas.";
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 100, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Tentativas: " + attempts + "/7"),
                width / 2, height / 2 - 70, 0xFFDDEEFF);
        context.fill(width / 2 - 92, height / 2 - 38, width / 2 + 92, height / 2 + 22, 0xFF263847);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(input.isEmpty() ? "_" : input),
                width / 2, height / 2 - 15, 0xFFFFD65A);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(message),
                width / 2, height / 2 + 43, gameOver && input.isEmpty() && attempts < 7
                        ? 0xFF74E68A : 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Teclas numéricas • Enter confirma • Backspace apaga • R reinicia"),
                width / 2, height / 2 + 67, 0xFFCBD6E0);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submitGuess();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!input.isEmpty()) input = input.substring(0, input.length() - 1);
            return true;
        }
        if (!gameOver && keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9 && input.length() < 3) {
            input += (char) ('0' + keyCode - GLFW.GLFW_KEY_0);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
