package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Digitação rápida: escreva o maior número de palavras em um minuto. */
public final class TypingSprintScreen extends Screen {
    private static final String[] WORDS = {
            "CASA", "MUNDO", "JOGO", "LIVRO", "PEDRA", "NUVEM", "PLANTA", "CAMINHO",
            "FLORESTA", "PRAIA", "TECLADO", "MUSICA", "ESTRELA", "AMIGOS", "CIDADE",
            "AVENTURA", "JANELA", "PIPOCA", "PLANETA", "CASTELO", "MONTANHA", "GIRASSOL"
    };
    private static final int ROUND_TICKS = 1200;
    private String target;
    private final StringBuilder typed = new StringBuilder();
    private int ticksLeft;
    private int score;
    private int mistakes;
    private int streak;
    private int ticks;
    private int flashTicks;
    private boolean lastCorrect;
    private boolean gameOver;

    public TypingSprintScreen() {
        super(Text.literal("Digitação Rápida"));
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, height - 30, buttonWidth, 20,
                Text.literal("Nova partida"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, height - 30, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (target == null) {
            newGame();
        }
    }

    private void newGame() {
        ticksLeft = ROUND_TICKS;
        score = 0;
        mistakes = 0;
        streak = 0;
        flashTicks = 0;
        lastCorrect = false;
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
        ticks++;
        if (flashTicks > 0) {
            flashTicks--;
        }
        if (!gameOver && --ticksLeft <= 0) {
            gameOver = true;
        }
    }

    private void submitWord() {
        if (gameOver || typed.length() == 0) {
            return;
        }
        if (typed.toString().equals(target)) {
            score++;
            streak++;
        } else {
            mistakes++;
            streak = 0;
        }
        lastCorrect = typed.toString().equals(target);
        flashTicks = 5;
        typed.setLength(0);
        nextWord();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.TEAL);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? "Fim! Você digitou " + score + " palavras"
                        : "Digite a palavra e pressione Enter"),
                width / 2, 32, gameOver ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        int cardWidth = Math.min(330, width - 40);
        int cardLeft = (width - cardWidth) / 2;
        int cardRight = cardLeft + cardWidth;
        ArcadeTheme.pill(context, textRenderer, "ACERTOS  " + score, cardLeft, 50, ArcadeTheme.TEAL);
        ArcadeTheme.pillRight(context, textRenderer, "TEMPO  " + Math.max(0, ticksLeft / 20) + "s", cardRight, 50,
                ArcadeTheme.GOLD);
        float remaining = Math.max(0.0F, ticksLeft / (float) ROUND_TICKS);
        ArcadeTheme.progress(context, cardLeft, 70, cardWidth, 4, remaining,
                remaining > 0.25F ? ArcadeTheme.TEAL : ArcadeTheme.DANGER);

        int cardTop = 88;
        int cardBottom = Math.min(height - 74, cardTop + 124);
        ArcadeTheme.rounded(context, cardLeft, cardTop, cardRight, cardBottom, 12,
                ArcadeTheme.alpha(ArcadeTheme.TEAL, 105));
        ArcadeTheme.rounded(context, cardLeft + 1, cardTop + 1, cardRight - 1, cardBottom - 1, 11, 0xFF0D1E30);
        context.fillGradient(cardLeft + 2, cardTop + 2, cardRight - 2, cardTop + 52, 0x2235C8B9, 0x00000000);

        if (gameOver) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("FIM DO TEMPO"),
                    width / 2, cardTop + 22, ArcadeTheme.DANGER);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Palavras certas: " + score), width / 2, cardTop + 44, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Erros: " + mistakes), width / 2, cardTop + 58, ArcadeTheme.MUTED);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Nova partida para jogar de novo"), width / 2, cardTop + 80, ArcadeTheme.GOLD);
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("DIGITE"),
                    width / 2, cardTop + 10, ArcadeTheme.DIM);
            int wordWidth = textRenderer.getWidth(target);
            int wordLeft = width / 2 - wordWidth / 2;
            for (int index = 0; index < target.length(); index++) {
                char letter = target.charAt(index);
                int color = index < typed.length()
                        ? (typed.charAt(index) == letter ? ArcadeTheme.SUCCESS : ArcadeTheme.DANGER)
                        : ArcadeTheme.TEXT;
                String character = Character.toString(letter);
                context.drawTextWithShadow(textRenderer, character, wordLeft, cardTop + 24, color);
                wordLeft += textRenderer.getWidth(character) + 1;
            }
            ArcadeTheme.rounded(context, cardLeft + 16, cardTop + 48, cardRight - 16, cardTop + 76, 6,
                    flashTicks > 0 ? (lastCorrect ? 0xFF1D4A32 : 0xFF4A1D26) : 0xFF16283D);
            String typedText = typed.toString();
            int typedWidth = textRenderer.getWidth(typedText);
            int typedLeft = width / 2 - typedWidth / 2;
            for (int index = 0; index < typedText.length(); index++) {
                char letter = typedText.charAt(index);
                boolean correct = index < target.length() && target.charAt(index) == letter;
                String character = Character.toString(letter);
                context.drawTextWithShadow(textRenderer, character, typedLeft, cardTop + 57,
                        correct ? ArcadeTheme.TEXT : ArcadeTheme.DANGER);
                typedLeft += textRenderer.getWidth(character) + 1;
            }
            if (ticks / 10 % 2 == 0) {
                context.fill(typedLeft, cardTop + 56, typedLeft + 1, cardTop + 67, ArcadeTheme.TEAL);
            }
            int statsY = cardTop + 88;
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Erros: " + mistakes + (streak >= 2 ? "  •  Sequência: " + streak : "")),
                    width / 2, statsY, streak >= 2 ? ArcadeTheme.LIME : ArcadeTheme.MUTED);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Letras certas em verde, erradas em vermelho"),
                    width / 2, statsY + 14, ArcadeTheme.DIM);
        }
        if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, "TEMPO ESGOTADO",
                    score + " palavras • " + mistakes + " erro(s)",
                    width / 2, (cardTop + cardBottom) / 2, Math.min(width - 24, cardWidth + 10),
                    score >= 10 ? ArcadeTheme.SUCCESS : ArcadeTheme.GOLD);
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
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!typed.isEmpty() && !gameOver) {
                typed.deleteCharAt(typed.length() - 1);
            }
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
