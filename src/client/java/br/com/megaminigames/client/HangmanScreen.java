package br.com.megaminigames.client;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Forca em português, controlada pelo teclado. */
public final class HangmanScreen extends Screen {
    private static final String[] WORDS = {
            "CAVALO", "JANELA", "MONTANHA", "PIPOCA", "GIRASSOL", "PLANETA", "BICICLETA",
            "COZINHA", "FLORESTA", "CADERNO", "ABACAXI", "CASTELO", "PANTUFA", "TECLADO"
    };
    private String word;
    private final Set<Character> guessed = new HashSet<>();
    private int mistakes;
    private boolean won;
    private boolean gameOver;

    public HangmanScreen() {
        super(Text.literal("Forca"));
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Nova palavra (R)"), button -> newGame())
                .dimensions(width / 2 - 116, height - 34, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, height - 34, 112, 20).build());
        if (word == null) newGame();
    }

    private void newGame() {
        word = WORDS[ThreadLocalRandom.current().nextInt(WORDS.length)];
        guessed.clear();
        mistakes = 0;
        won = false;
        gameOver = false;
    }

    private void guess(char letter) {
        if (gameOver || !guessed.add(letter)) return;
        if (word.indexOf(letter) < 0) mistakes++;
        won = true;
        for (int i = 0; i < word.length(); i++) {
            if (!guessed.contains(word.charAt(i))) {
                won = false;
                break;
            }
        }
        if (won || mistakes >= 7) gameOver = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Erros: " + mistakes + "/7  •  Pressione letras de A a Z"),
                width / 2, 35, 0xFFE7E7E7);
        drawHangman(context);

        int wordWidth = word.length() * 22;
        int startX = width / 2 - wordWidth / 2;
        for (int i = 0; i < word.length(); i++) {
            int x = startX + i * 22;
            context.fill(x, height / 2 + 50, x + 18, height / 2 + 52, 0xFFFFFFFF);
            if (guessed.contains(word.charAt(i)) || (gameOver && !won)) {
                context.drawCenteredTextWithShadow(textRenderer,
                        Text.literal(String.valueOf(word.charAt(i))), x + 9, height / 2 + 31, 0xFFFFFFFF);
            }
        }
        String used = "Letras: " + guessed.toString().replace("[", "").replace("]", "");
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(used), width / 2,
                height / 2 + 75, 0xFFCEDBE8);
        String result = gameOver ? won ? "Você acertou!" : "A palavra era " + word + "." : "";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(result), width / 2,
                height / 2 + 100, won ? 0xFF6EEC83 : 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawHangman(DrawContext context) {
        int centerX = width / 2;
        int baseY = height / 2 - 15;
        context.fill(centerX - 65, baseY + 70, centerX + 25, baseY + 74, 0xFFD8C9AA);
        context.fill(centerX - 25, baseY - 5, centerX - 21, baseY + 72, 0xFFD8C9AA);
        context.fill(centerX - 25, baseY - 5, centerX + 20, baseY - 1, 0xFFD8C9AA);
        context.fill(centerX + 16, baseY - 5, centerX + 20, baseY + 10, 0xFFD8C9AA);
        if (mistakes >= 1) context.fill(centerX + 9, baseY + 8, centerX + 27, baseY + 26, 0xFFE7D4B3);
        if (mistakes >= 2) context.fill(centerX + 17, baseY + 25, centerX + 19, baseY + 51, 0xFFE7D4B3);
        if (mistakes >= 3) context.fill(centerX + 5, baseY + 31, centerX + 18, baseY + 39, 0xFFE7D4B3);
        if (mistakes >= 4) context.fill(centerX + 19, baseY + 31, centerX + 31, baseY + 39, 0xFFE7D4B3);
        if (mistakes >= 5) context.fill(centerX + 6, baseY + 49, centerX + 18, baseY + 63, 0xFFE7D4B3);
        if (mistakes >= 6) context.fill(centerX + 19, baseY + 49, centerX + 30, baseY + 63, 0xFFE7D4B3);
        if (mistakes >= 7) context.fill(centerX + 12, baseY + 14, centerX + 14, baseY + 16, 0xFF222222);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            guess((char) ('A' + keyCode - GLFW.GLFW_KEY_A));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
