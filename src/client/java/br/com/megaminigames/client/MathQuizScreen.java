package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Dez desafios rápidos de soma, subtração e multiplicação. */
public final class MathQuizScreen extends Screen {
    private static final int TOTAL = 10;
    private int first;
    private int second;
    private int answer;
    private int question;
    private int score;
    private int feedbackTicks;
    private int ticks;
    private int keypadTop;
    private int keyWidth;
    private int keyHeight;
    private int keypadLeft;
    private String operator;
    private String input = "";
    private String feedback = "";
    private boolean waitingNext;
    private boolean gameOver;

    public MathQuizScreen() {
        super(Text.literal("Desafio de Matemática"));
    }

    @Override
    protected void init() {
        keyWidth = Math.max(18, Math.min(34, (width - 60) / 6 - 4));
        keyHeight = Math.max(10, Math.min(20, (height - 236) / 2 - 4));
        keypadLeft = (width - (6 * keyWidth + 5 * 4)) / 2;
        int keypadEnd = height - 32;
        int keypadBlock = 2 * keyHeight + 4;
        keypadTop = 188 + Math.min(40, Math.max(0, (keypadEnd - 188 - keypadBlock) / 2));
        if (keypadTop + keypadBlock > keypadEnd) {
            keypadTop = 0;
        }
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, height - 26, buttonWidth, 20,
                Text.literal("Reiniciar"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, height - 26, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (question == 0 && !gameOver && answer == 0 && operator == null) {
            newGame();
        }
    }

    private void newGame() {
        question = 0;
        score = 0;
        input = "";
        feedback = "Resolva a conta e pressione Enter";
        feedbackTicks = 0;
        waitingNext = false;
        gameOver = false;
        makeQuestion();
    }

    private void makeQuestion() {
        int type = ThreadLocalRandom.current().nextInt(3);
        if (type == 0) {
            first = ThreadLocalRandom.current().nextInt(2, 51);
            second = ThreadLocalRandom.current().nextInt(2, 51);
            answer = first + second;
            operator = "+";
        } else if (type == 1) {
            first = ThreadLocalRandom.current().nextInt(2, 51);
            second = ThreadLocalRandom.current().nextInt(2, first + 1);
            answer = first - second;
            operator = "−";
        } else {
            first = ThreadLocalRandom.current().nextInt(2, 13);
            second = ThreadLocalRandom.current().nextInt(2, 13);
            answer = first * second;
            operator = "×";
        }
    }

    @Override
    public void tick() {
        ticks++;
        if (waitingNext && --feedbackTicks <= 0) {
            advance();
        }
    }

    private void submit() {
        if (gameOver) {
            return;
        }
        if (waitingNext) {
            advance();
            return;
        }
        if (input.isEmpty()) {
            return;
        }
        int guessed = Integer.parseInt(input);
        input = "";
        if (guessed == answer) {
            score++;
            feedback = "Correto!";
        } else {
            feedback = "Quase! A resposta era " + answer + ".";
        }
        question++;
        if (question >= TOTAL) {
            gameOver = true;
            feedback = "Fim! Você acertou " + score + " de " + TOTAL + " contas.";
        } else {
            waitingNext = true;
            feedbackTicks = 28;
        }
    }

    private void advance() {
        waitingNext = false;
        feedbackTicks = 0;
        input = "";
        feedback = "Resolva a conta e pressione Enter";
        makeQuestion();
    }

    private void typeDigit(int digit) {
        if (!waitingNext && !gameOver && input.length() < 4) {
            input += digit;
        }
    }

    private void erase() {
        if (!input.isEmpty() && !waitingNext && !gameOver) {
            input = input.substring(0, input.length() - 1);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.SKY);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(waitingNext ? "Enter para a próxima conta"
                        : gameOver ? "Fim do desafio! R para jogar de novo"
                        : "Digite a resposta — Enter confirma"),
                width / 2, 32, gameOver ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        int cardWidth = Math.min(300, width - 40);
        int cardLeft = (width - cardWidth) / 2;
        int cardRight = cardLeft + cardWidth;
        ArcadeTheme.pill(context, textRenderer, "PONTOS  " + score, cardLeft, 50, ArcadeTheme.SKY);
        ArcadeTheme.pillRight(context, textRenderer, "CONTA  " + Math.min(question + 1, TOTAL) + "/" + TOTAL,
                cardRight, 50, ArcadeTheme.GOLD);
        for (int index = 0; index < TOTAL; index++) {
            int dotX = cardLeft + index * (cardWidth / TOTAL);
            int color = index < question ? ArcadeTheme.SUCCESS : index == question ? ArcadeTheme.GOLD : 0xFF25374D;
            ArcadeTheme.rounded(context, dotX, 69, dotX + Math.max(3, cardWidth / TOTAL - 3), 72, 1, color);
        }

        int cardTop = 82;
        int cardBottom = cardTop + 96;
        ArcadeTheme.rounded(context, cardLeft + 2, cardTop + 4, cardRight + 2, cardBottom + 4, 12, 0x55000000);
        ArcadeTheme.rounded(context, cardLeft, cardTop, cardRight, cardBottom, 12, ArcadeTheme.alpha(ArcadeTheme.SKY, 110));
        ArcadeTheme.rounded(context, cardLeft + 1, cardTop + 1, cardRight - 1, cardBottom - 1, 11, 0xFF0E1D2E);
        context.fillGradient(cardLeft + 2, cardTop + 2, cardRight - 2, cardTop + 40, 0x226CC9F5, 0x00000000);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(first + " " + operator + " " + second), width / 2, cardTop + 14, ArcadeTheme.TEXT);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("=  ?"), width / 2, cardTop + 32,
                ArcadeTheme.DIM);
        if (gameOver) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Resultado: " + score + "/" + TOTAL),
                    width / 2, cardTop + 50, ArcadeTheme.GOLD);
        } else {
            int fieldWidth = Math.min(120, cardWidth - 40);
            int fieldLeft = width / 2 - fieldWidth / 2;
            ArcadeTheme.rounded(context, fieldLeft, cardTop + 46, fieldLeft + fieldWidth, cardTop + 70, 6, 0xFF16283D);
            ArcadeTheme.progress(context, fieldLeft, cardTop + 46, fieldWidth, 2, 0, ArcadeTheme.SKY);
            String text = input.isEmpty() ? "_" : input + (ticks / 10 % 2 == 0 ? "|" : "");
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(text), width / 2, cardTop + 56,
                    ArcadeTheme.SKY);
        }

        context.drawCenteredTextWithShadow(textRenderer, Text.literal(feedback),
                width / 2, cardBottom - 18, waitingNext ? ArcadeTheme.SUCCESS
                        : gameOver ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, "DESAFIO CONCLUÍDO",
                    "Você acertou " + score + " de " + TOTAL + " contas",
                    width / 2, cardTop + 40, Math.min(width - 24, cardWidth + 10),
                    score >= 7 ? ArcadeTheme.SUCCESS : score >= 4 ? ArcadeTheme.GOLD : ArcadeTheme.CORAL);
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
                    submit();
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
            submit();
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
