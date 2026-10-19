package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
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
    private final char[][] guesses = new char[ROWS][COLS];
    private final int[][] feedback = new int[ROWS][COLS];
    private final char[] currentGuess = new char[COLS];
    private String target;
    private int currentRow;
    private int currentLength;
    private int tile = 32;
    private int gap = 4;
    private int left;
    private int top;
    private int keyboardTop;
    private int keySize = 22;
    private int keyHeight = 17;
    private final int keyGap = 3;
    private boolean gameOver;
    private String message = "Digite letras e pressione Enter";

    public WordleScreen() {
        super(Text.literal("Termo"));
    }

    @Override
    protected void init() {
        int side = 16;
        int reservedKeyboard = 74;
        int maxTileByWidth = (width - side * 2 - (COLS - 1) * 2) / COLS;
        int maxTileByHeight = (height - 54 - 32 - reservedKeyboard) / ROWS - 2;
        tile = Math.max(13, Math.min(34, Math.min(maxTileByWidth, maxTileByHeight)));
        gap = Math.max(2, tile / 9);
        int boardWidth = COLS * tile + (COLS - 1) * gap;
        int boardHeight = ROWS * tile + (ROWS - 1) * gap;
        left = (width - boardWidth) / 2;
        top = Math.max(50, 50 + (height - 54 - 32 - reservedKeyboard - boardHeight) / 2);
        keyboardTop = top + boardHeight + 8;
        keySize = Math.max(14, Math.min(22, (width - 34) / 10 - keyGap));
        keyHeight = 17;
        int needed = keyboardTop + 3 * keyHeight + 2 * keyGap + 2;
        if (needed > height - 30) {
            int shift = needed - (height - 30);
            top = Math.max(46, top - shift);
            keyboardTop -= shift;
        }
        int buttonY = height - 26;
        int buttonWidth = Math.min(112, Math.max(74, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Nova palavra"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
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
        message = "Verde: posição certa • Amarelo: existe na palavra";
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
                message = "Tente outra palavra. Restam " + (ROWS - currentRow) + " tentativas.";
            }
        }
    }

    private void typeLetter(char letter) {
        if (!gameOver && currentLength < COLS) {
            currentGuess[currentLength++] = letter;
        }
    }

    private void eraseLetter() {
        if (!gameOver && currentLength > 0) {
            currentGuess[--currentLength] = '\0';
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.LIME);
        ArcadeTheme.status(context, textRenderer, Text.literal(textRenderer.trimToWidth(message, width - 24)),
                width / 2, 31, gameOver ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLS; column++) {
                int x = left + column * (tile + gap);
                int y = top + row * (tile + gap);
                int mark = feedback[row][column];
                boolean activeRow = row == currentRow && !gameOver;
                int fill = mark == 2 ? 0xFF2E9E5B : mark == 1 ? 0xFFC6A02F : mark == 0 ? 0xFF334052 : 0xFF1A2B40;
                if (activeRow && column == currentLength) {
                    fill = 0xFF24405C;
                }
                int border = mark == -1 ? (activeRow ? 0x66FFFFFF : 0x33FFFFFF) : 0;
                if (border != 0) {
                    ArcadeTheme.rounded(context, x, y, x + tile, y + tile, 5, border);
                }
                ArcadeTheme.rounded(context, x + (border != 0 ? 1 : 0), y + (border != 0 ? 1 : 0),
                        x + tile - (border != 0 ? 1 : 0), y + tile - (border != 0 ? 1 : 0), 4, fill);
                if (mark >= 0) {
                    ArcadeTheme.rounded(context, x + 1, y + 1, x + tile - 1, y + tile / 2, 4, 0x18FFFFFF);
                }
                char letter = row < currentRow ? guesses[row][column]
                        : row == currentRow && column < currentLength ? currentGuess[column] : '\0';
                if (letter != '\0') {
                    context.drawCenteredTextWithShadow(textRenderer, Text.literal(String.valueOf(letter)),
                            x + tile / 2, y + tile / 2 - 4, 0xFFFFFFFF);
                }
            }
        }
        drawKeyboard(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    private int letterState(char letter) {
        int state = 0;
        for (int row = 0; row < currentRow; row++) {
            for (int column = 0; column < COLS; column++) {
                if (guesses[row][column] == letter) {
                    int mark = feedback[row][column];
                    if (mark == 2) return 2;
                    if (mark == 1) state = 1;
                    else if (mark == 0 && state == 0) state = 3;
                }
            }
        }
        return state;
    }

    private int keyColor(int state) {
        return switch (state) {
            case 2 -> 0xFF2E9E5B;
            case 1 -> 0xFFB8912F;
            case 3 -> 0xFF1D2A3B;
            default -> 0xFF243A54;
        };
    }

    private int rowWidth(int count) {
        return count * keySize + (count - 1) * keyGap;
    }

    private void drawKey(DrawContext context, int x, int y, int width, int height, String label, int fill, int textColor) {
        ArcadeTheme.rounded(context, x, y + 2, x + width, y + height + 2, 4, 0x44000000);
        ArcadeTheme.rounded(context, x, y, x + width, y + height, 4, 0x55FFFFFF);
        ArcadeTheme.rounded(context, x + 1, y + 1, x + width - 1, y + height - 1, 3, fill);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + width / 2, y + height / 2 - 4, textColor);
    }

    private void drawKeyboard(DrawContext context, int mouseX, int mouseY) {
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        for (int row = 0; row < rows.length; row++) {
            int y = keyboardTop + row * (keyHeight + keyGap);
            if (row == 2) {
                int enterWidth = 38;
                int backWidth = 32;
                int lettersWidth = rowWidth(rows[row].length());
                int total = enterWidth + backWidth + lettersWidth + keyGap * 2;
                int x = (width - total) / 2;
                drawKey(context, x, y, enterWidth, keyHeight, "ENTER", 0xFF1C5A44, 0xFFB6F2CE);
                x += enterWidth + keyGap;
                for (int i = 0; i < rows[row].length(); i++) {
                    char letter = rows[row].charAt(i);
                    drawKey(context, x + i * (keySize + keyGap), y, keySize, keyHeight, String.valueOf(letter),
                            keyColor(letterState(letter)), 0xFFFFFFFF);
                }
                drawKey(context, x + lettersWidth + keyGap, y, backWidth, keyHeight, "APAGAR", 0xFF3A2B33, 0xFFFFD6D6);
            } else {
                int startX = (width - rowWidth(rows[row].length())) / 2 + (row == 1 ? (keySize + keyGap) / 2 : 0);
                for (int i = 0; i < rows[row].length(); i++) {
                    char letter = rows[row].charAt(i);
                    int x = startX + i * (keySize + keyGap);
                    int fill = keyColor(letterState(letter));
                    boolean hovered = mouseX >= x && mouseX < x + keySize && mouseY >= y && mouseY < y + keyHeight;
                    drawKey(context, x, y, keySize, keyHeight, String.valueOf(letter),
                            hovered ? ArcadeTheme.lighten(fill, 18) : fill, 0xFFFFFFFF);
                }
            }
        }
    }

    private boolean clickKeyboard(double mouseX, double mouseY) {
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        for (int row = 0; row < rows.length; row++) {
            int y = keyboardTop + row * (keyHeight + keyGap);
            if (mouseY < y || mouseY >= y + keyHeight) continue;
            if (row == 2) {
                int enterWidth = 38;
                int backWidth = 32;
                int lettersWidth = rowWidth(rows[row].length());
                int total = enterWidth + backWidth + lettersWidth + keyGap * 2;
                int x = (width - total) / 2;
                if (mouseX >= x && mouseX < x + enterWidth) {
                    submit();
                    return true;
                }
                int lettersX = x + enterWidth + keyGap;
                if (mouseX >= lettersX && mouseX < lettersX + lettersWidth) {
                    int index = (int) ((mouseX - lettersX) / (keySize + keyGap));
                    if (index >= 0 && index < rows[row].length()) {
                        typeLetter(rows[row].charAt(index));
                        return true;
                    }
                }
                int backX = lettersX + lettersWidth + keyGap;
                if (mouseX >= backX && mouseX < backX + backWidth) {
                    eraseLetter();
                    return true;
                }
            } else {
                int startX = (width - rowWidth(rows[row].length())) / 2 + (row == 1 ? (keySize + keyGap) / 2 : 0);
                if (mouseX >= startX && mouseX < startX + rowWidth(rows[row].length())) {
                    int index = (int) ((mouseX - startX) / (keySize + keyGap));
                    if (index >= 0 && index < rows[row].length()) {
                        typeLetter(rows[row].charAt(index));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) return false;
        return clickKeyboard(mouseX, mouseY);
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
            eraseLetter();
            return true;
        }
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            typeLetter((char) ('A' + keyCode - GLFW.GLFW_KEY_A));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
