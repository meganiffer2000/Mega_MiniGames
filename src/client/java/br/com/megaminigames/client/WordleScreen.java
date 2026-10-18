package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Termo: descubra a palavra de cinco letras em até seis tentativas. */
public final class WordleScreen extends Screen {
    private static final String[] WORDS = {
            "MUNDO", "LIVRO", "PEDRA", "NUVEM", "PLANO", "FESTA", "FOLHA", "TIGRE",
            "VELHO", "GATOS", "COBRA", "CASAS", "LAPIS", "PRAIA", "SABOR", "PORTA",
            "RAIOS", "CAMPO", "JOGAR", "PONTO", "VERDE", "SONHO", "NOITE", "FAROL"
    };
    private static final int ROWS = 6;
    private static final int COLS = 5;
    private static final int TILE = 34;
    private static final int GAP = 4;
    private final char[][] guesses = new char[ROWS][COLS];
    private final int[][] feedback = new int[ROWS][COLS];
    private final char[] currentGuess = new char[COLS];
    private String target;
    private int currentRow;
    private int currentLength;
    private int left;
    private int top;
    private boolean gameOver;
    private String message = "Digite letras e pressione Enter";

    public WordleScreen() {
        super(Text.literal("Termo"));
    }

    @Override
    protected void init() {
        left = (width - COLS * TILE - (COLS - 1) * GAP) / 2;
        top = Math.max(64, (height - ROWS * TILE - 90) / 2);
        int buttonY = height - 34;
        addDrawableChild(ButtonWidget.builder(Text.literal("Nova palavra"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (target == null) newGame();
    }

    private void newGame() {
        target = WORDS[ThreadLocalRandom.current().nextInt(WORDS.length)];
        for (char[] row : guesses) java.util.Arrays.fill(row, '\0');
        for (int[] row : feedback) java.util.Arrays.fill(row, -1);
        java.util.Arrays.fill(currentGuess, '\0');
        currentRow = 0;
        currentLength = 0;
        gameOver = false;
        message = "Digite 5 letras • verde: posição certa • amarelo: existe";
    }

    private void submit() {
        if (gameOver || currentLength != COLS) {
            message = "A palavra precisa ter 5 letras.";
            return;
        }
        String guess = new String(currentGuess);
        int[] marks = new int[COLS];
        int[] remaining = new int[26];
        for (int i = 0; i < COLS; i++) {
            if (guess.charAt(i) == target.charAt(i)) {
                marks[i] = 2;
            } else {
                remaining[target.charAt(i) - 'A']++;
            }
        }
        for (int i = 0; i < COLS; i++) {
            if (marks[i] == 0) {
                int letter = guess.charAt(i) - 'A';
                if (remaining[letter] > 0) {
                    marks[i] = 1;
                    remaining[letter]--;
                }
            }
            guesses[currentRow][i] = guess.charAt(i);
            feedback[currentRow][i] = marks[i];
        }
        if (guess.equals(target)) {
            message = "Acertou! A palavra era " + target + ".";
            gameOver = true;
        } else {
            currentRow++;
            currentLength = 0;
            java.util.Arrays.fill(currentGuess, '\0');
            if (currentRow >= ROWS) {
                message = "Fim de jogo. A palavra era " + target + ".";
                gameOver = true;
            } else {
                message = "Tente outra palavra.";
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(message), width / 2, 32, 0xFFE8E8E8);
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLS; column++) {
                int x = left + column * (TILE + GAP);
                int y = top + row * (TILE + GAP);
                int mark = feedback[row][column];
                int color = mark == 2 ? 0xFF538D4E : mark == 1 ? 0xFFB59F3B : 0xFF3A3A3C;
                boolean hasLetter = guesses[row][column] != '\0';
                if (row == currentRow && column < currentLength && !gameOver) {
                    hasLetter = true;
                }
                context.fill(x, y, x + TILE, y + TILE, mark == -1 ? 0xFF777777 : color);
                char letter = row < currentRow ? guesses[row][column]
                        : row == currentRow && column < currentLength ? currentGuess[column] : '\0';
                if (hasLetter && letter != '\0') {
                    context.drawCenteredTextWithShadow(textRenderer, Text.literal(String.valueOf(letter)),
                            x + TILE / 2, y + TILE / 2 - 4, 0xFFFFFFFF);
                }
            }
        }
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Teclado A-Z • Backspace apaga • Enter confirma"), width / 2,
                top + ROWS * (TILE + GAP) + 4, 0xFFCBD6E0);
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
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && currentLength > 0 && !gameOver) {
            currentGuess[--currentLength] = '\0';
            return true;
        }
        if (!gameOver && currentLength < COLS
                && keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            currentGuess[currentLength++] = (char) ('A' + keyCode - GLFW.GLFW_KEY_A);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
