package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Dez desafios rápidos de soma, subtração e multiplicação. */
public final class MathQuizScreen extends Screen {
    private int first;
    private int second;
    private int answer;
    private int question;
    private int score;
    private int feedbackTicks;
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
        int buttonY = height - 35;
        addDrawableChild(ButtonWidget.builder(Text.literal("Reiniciar"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (question == 0 && !gameOver && answer == 0 && operator == null) newGame();
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
        if (waitingNext && --feedbackTicks <= 0) advance();
    }

    private void submit() {
        if (gameOver) return;
        if (waitingNext) {
            advance();
            return;
        }
        if (input.isEmpty()) return;
        int guessed = Integer.parseInt(input);
        input = "";
        if (guessed == answer) {
            score++;
            feedback = "Correto!";
        } else {
            feedback = "Quase! A resposta era " + answer + ".";
        }
        question++;
        if (question >= 10) {
            gameOver = true;
            feedback = "Fim! Você acertou " + score + " de 10 contas. Pressione R para jogar de novo.";
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

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 105, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Questão " + Math.min(question + 1, 10) + "/10  •  Pontos: " + score),
                width / 2, height / 2 - 76, 0xFFDDEEFF);
        if (!gameOver) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(first + " " + operator + " " + second + " = ?"),
                    width / 2, height / 2 - 35, 0xFFFFD65A);
            context.fill(width / 2 - 85, height / 2 - 5, width / 2 + 85, height / 2 + 45, 0xFF293B4A);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(input.isEmpty() ? "_" : input),
                    width / 2, height / 2 + 13, 0xFFFFFFFF);
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(feedback),
                width / 2, height / 2 + 65, gameOver ? 0xFF79E991 : 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Digite a resposta • Backspace apaga • Enter confirma/continua"),
                width / 2, height / 2 + 88, 0xFFCBD6E0);
        super.render(context, mouseX, mouseY, delta);
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
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !input.isEmpty() && !waitingNext && !gameOver) {
            input = input.substring(0, input.length() - 1);
            return true;
        }
        if (!waitingNext && !gameOver && keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9
                && input.length() < 4) {
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
