package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Adivinhe o número secreto de 1 a 100 em até sete tentativas. */
public final class NumberGuessScreen extends Screen {
    private static final int MAX_ATTEMPTS = 7;
    private final List<Integer> history = new ArrayList<>();
    private final List<Boolean> historyHigh = new ArrayList<>();
    private int secret;
    private int attempts;
    private int ticks;
    private int keypadTop;
    private int keyWidth;
    private int keyHeight;
    private int keypadLeft;
    private String input = "";
    private String message = "Digite um número de 1 a 100 e pressione Enter";
    private boolean gameOver;
    private boolean won;

    public NumberGuessScreen() {
        super(Text.literal("Adivinhe o Número"));
    }

    @Override
    protected void init() {
        keyWidth = Math.max(18, Math.min(34, (width - 60) / 6 - 4));
        keyHeight = Math.max(10, Math.min(20, (height - 246) / 2 - 4));
        keypadLeft = (width - (6 * keyWidth + 5 * 4)) / 2;
        int keypadEnd = height - 32;
        int block = 2 * keyHeight + 4;
        keypadTop = 198 + Math.min(40, Math.max(0, (keypadEnd - 198 - block) / 2));
        if (keypadTop + block > keypadEnd) {
            keypadTop = 0;
        }
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, height - 26, buttonWidth, 20,
                Text.literal("Novo jogo"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, height - 26, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (secret == 0) {
            newGame();
        }
    }

    private void newGame() {
        secret = ThreadLocalRandom.current().nextInt(1, 101);
        attempts = 0;
        input = "";
        history.clear();
        historyHigh.clear();
        message = "Digite um número de 1 a 100 e pressione Enter";
        gameOver = false;
        won = false;
    }

    private void submitGuess() {
        if (gameOver || input.isEmpty()) {
            return;
        }
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
            won = true;
            history.add(guess);
            historyHigh.add(false);
        } else if (attempts >= MAX_ATTEMPTS) {
            message = "As tentativas acabaram. O número era " + secret + ".";
            gameOver = true;
            history.add(guess);
            historyHigh.add(guess > secret);
        } else {
            history.add(guess);
            historyHigh.add(guess > secret);
            message = (guess < secret ? "Tente um número MAIOR." : "Tente um número MENOR.")
                    + " Restam " + (MAX_ATTEMPTS - attempts) + " tentativas.";
        }
    }

    private void typeDigit(int digit) {
        if (!gameOver && input.length() < 3) {
            input += digit;
        }
    }

    private void erase() {
        if (!input.isEmpty()) {
            input = input.substring(0, input.length() - 1);
        }
    }

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.PURPLE);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? (won ? "Número encontrado!" : "Fim das tentativas — R para tentar de novo")
                        : "Tente descobrir o número secreto entre 1 e 100"),
                width / 2, 32, gameOver ? (won ? ArcadeTheme.SUCCESS : ArcadeTheme.GOLD) : ArcadeTheme.MUTED);

        int cardLeft = width / 2 - 122;
        int cardRight = width / 2 + 122;
        ArcadeTheme.pill(context, textRenderer, "TENTATIVAS  " + attempts + "/" + MAX_ATTEMPTS, cardLeft, 50,
                ArcadeTheme.PURPLE);
        ArcadeTheme.pillRight(context, textRenderer, "1 — 100", cardRight, 50, ArcadeTheme.SKY);

        ArcadeTheme.rounded(context, cardLeft, 76, cardRight, 148, 11, ArcadeTheme.alpha(ArcadeTheme.PURPLE, 110));
        ArcadeTheme.rounded(context, cardLeft + 1, 77, cardRight - 1, 147, 10, 0xFF10203A);
        context.fillGradient(cardLeft + 2, 78, cardRight - 2, 110, 0x229B7BF0, 0x00000000);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("NÚMERO SECRETO"), width / 2, 83,
                ArcadeTheme.DIM);
        if (won) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(secret)),
                    width / 2, 96, ArcadeTheme.SUCCESS);
        } else if (gameOver) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(secret)),
                    width / 2, 96, ArcadeTheme.DANGER);
        } else {
            String text = input.isEmpty() ? "?" : input + (ticks / 10 % 2 == 0 ? "_" : "");
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(text), width / 2, 93, ArcadeTheme.GOLD);
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(message), width / 2, 118,
                gameOver ? ArcadeTheme.TEXT : ArcadeTheme.TEXT);
        for (int index = 0; index < MAX_ATTEMPTS; index++) {
            int dotX = width / 2 - (MAX_ATTEMPTS * 12) / 2 + index * 12 + 1;
            int color = index < attempts ? (history.size() > index && history.get(index) == secret
                    ? ArcadeTheme.SUCCESS : ArcadeTheme.PURPLE) : 0xFF25374D;
            ArcadeTheme.disc(context, dotX + 3, 136, 3, color);
        }

        int chipWidth = 40;
        int chipStart = width / 2 - (history.size() * (chipWidth + 4)) / 2;
        for (int index = 0; index < history.size(); index++) {
            int x = chipStart + index * (chipWidth + 4);
            int y = 156;
            ArcadeTheme.rounded(context, x, y, x + chipWidth, y + 13, 4,
                    historyHigh.get(index) ? 0xFF3A2130 : 0xFF1B2E44);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(Integer.toString(history.get(index)) + (historyHigh.get(index) ? " ↓" : " ↑")),
                    x + chipWidth / 2, y + 3, historyHigh.get(index) ? ArcadeTheme.CORAL : ArcadeTheme.SKY);
        }

        if (keypadTop > 0) {
            String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "APAGAR", "ENTER"};
            for (int index = 0; index < keys.length; index++) {
                int row = index / 6;
                int column = index % 6;
                int x = keypadLeft + column * (keyWidth + 4);
                int y = keypadTop + row * (keyHeight + 4);
                boolean hovered = mouseX >= x && mouseX < x + keyWidth && mouseY >= y && mouseY < y + keyHeight;
                int color = index == 11 ? ArcadeTheme.TEAL_DEEP : index == 10 ? 0xFF3A2632 : 0xFF1B2E44;
                if (hovered) {
                    color = ArcadeTheme.lighten(color, 22);
                }
                ArcadeTheme.rounded(context, x, y, x + keyWidth, y + keyHeight, 4, color);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(keys[index]),
                        x + keyWidth / 2, y + keyHeight / 2 - 4,
                        index == 10 ? ArcadeTheme.DANGER : ArcadeTheme.TEXT);
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0 || keypadTop <= 0) {
            return false;
        }
        String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "APAGAR", "ENTER"};
        for (int index = 0; index < keys.length; index++) {
            int row = index / 6;
            int column = index % 6;
            int x = keypadLeft + column * (keyWidth + 4);
            int y = keypadTop + row * (keyHeight + 4);
            if (mouseX >= x && mouseX < x + keyWidth && mouseY >= y && mouseY < y + keyHeight) {
                if (index == 11) {
                    submitGuess();
                } else if (index == 10) {
                    erase();
                } else {
                    typeDigit(index == 9 ? 0 : index + 1);
                }
                return true;
            }
        }
        return false;
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
            erase();
            return true;
        }
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            typeDigit(keyCode - GLFW.GLFW_KEY_0);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
