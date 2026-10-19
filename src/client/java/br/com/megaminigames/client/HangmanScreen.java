package br.com.megaminigames.client;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Forca: descubra a palavra antes de completar o boneco. */
public final class HangmanScreen extends Screen {
    private static final String[] WORDS = {
            "MINECRAFT", "DIAMANTE", "CREEPER", "POCAO", "ENDERMAN", "PICARETA", "GRANADA",
            "MONTANHA", "FLORESTA", "CASTELO", "PANTUFA", "TECLADO"
    };
    private String word;
    private final Set<Character> guessed = new HashSet<>();
    private int mistakes;
    private boolean won;
    private boolean gameOver;
    private int panelTop;
    private int panelBottom;
    private int wordY;
    private int keyboardTop;
    private int keySize = 20;
    private int keyHeight = 18;
    private int keyGap = 4;

    public HangmanScreen() {
        super(Text.literal("Forca"));
    }

    @Override
    protected void init() {
        keySize = Math.max(14, Math.min(22, (width - 34) / 9 - keyGap));
        keyHeight = Math.max(12, Math.min(18, height / 16));
        int keyboardHeight = 3 * keyHeight + 2 * keyGap;
        panelTop = 48;
        panelBottom = Math.max(panelTop + 56, (height - 96) / 2);
        keyboardTop = Math.min(height - 34 - keyboardHeight, panelBottom + 66);
        panelBottom = Math.min(panelBottom, keyboardTop - 58);
        wordY = panelBottom + 18;
        int buttonY = height - 26;
        int buttonWidth = Math.min(124, Math.max(76, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Nova palavra (R)"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
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
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 11, ArcadeTheme.CORAL);

        String status = "Erros " + mistakes + "/7   •   " + (gameOver
                ? (won ? "Você salvou o boneco!" : "A palavra era " + word)
                : "Adivinhe as letras");
        ArcadeTheme.status(context, textRenderer, Text.literal(status), width / 2, 30,
                gameOver ? (won ? ArcadeTheme.SUCCESS : ArcadeTheme.DANGER) : ArcadeTheme.MUTED);

        int panelWidth = Math.min(250, width - 36);
        int panelX = (width - panelWidth) / 2;
        ArcadeTheme.rounded(context, panelX, panelTop, panelX + panelWidth, panelBottom, 12, 0x55000000);
        ArcadeTheme.rounded(context, panelX, panelTop, panelX + panelWidth, panelBottom, 12, 0x5535C8B9);
        ArcadeTheme.rounded(context, panelX + 1, panelTop + 1, panelX + panelWidth - 1, panelBottom - 1, 11, 0xF00A1720);

        drawGallows(context, panelX + 10, panelTop + 6, panelWidth - 20, panelBottom - panelTop - 12);

        // Células das letras.
        int slot = Math.max(14, Math.min(24, (width - 44) / Math.max(1, word.length())));
        int spacing = slot + 3;
        int wordWidth = word.length() * spacing - 3;
        int startX = (width - wordWidth) / 2;
        for (int i = 0; i < word.length(); i++) {
            int x = startX + i * spacing;
            boolean shown = guessed.contains(word.charAt(i)) || (gameOver && !won);
            ArcadeTheme.rounded(context, x, wordY, x + slot, wordY + slot + 4, 4,
                    shown ? (guessed.contains(word.charAt(i)) ? 0xFF2E9E5B : 0xFF7A3B3B) : 0xFF16283C);
            ArcadeTheme.rounded(context, x + 1, wordY + 1, x + slot - 1, wordY + slot + 3, 3, 0x10FFFFFF);
            if (shown) {
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(String.valueOf(word.charAt(i))),
                        x + slot / 2, wordY + slot / 2 - 2, 0xFFFFFFFF);
            } else {
                ArcadeTheme.disc(context, x + slot / 2, wordY + slot + 1, 1, 0xFF5A7794);
            }
        }

        drawKeyboard(context, mouseX, mouseY, startX);
        if (!gameOver) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Clique nas letras ou use o teclado"),
                    width / 2, keyboardTop + 3 * (keyHeight + keyGap) + 2, ArcadeTheme.DIM);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawGallows(DrawContext context, int x, int y, int width, int height) {
        int wood = 0xFFB08A5A;
        int woodDark = 0xFF7A5B38;
        int baseY = y + height - 6;
        int left = x + width / 2 - Math.min(70, width / 2 - 10);
        int beamTop = y + 8;
        context.fill(left, baseY - 3, left + Math.min(120, width / 2 + 30), baseY, woodDark);
        context.fill(left + 4, beamTop, left + 9, baseY, wood);
        context.fill(left + 4, beamTop, left + Math.min(100, width / 2 + 20), beamTop + 5, wood);
        for (int i = 0; i < 10; i++) {
            context.fill(left + 8 + i * 2, beamTop + 8 + i, left + 12 + i * 2, beamTop + 10 + i, woodDark);
        }
        int hookX = left + Math.min(94, width / 2 + 14);
        context.fill(hookX, beamTop + 5, hookX + 3, beamTop + 14, 0xFFD8C9AA);

        int headX = hookX + 1;
        int headY = beamTop + 23;
        int headRadius = Math.max(7, Math.min(12, height / 8));
        if (mistakes >= 1) {
            ArcadeTheme.disc(context, headX, headY, headRadius, 0xFFE7D4B3);
            ArcadeTheme.ring(context, headX, headY, headRadius, 1, 0xFF9C8463);
            int eyeColor = gameOver && !won ? ArcadeTheme.DANGER : 0xFF2B3038;
            context.fill(headX - 4, headY - 2, headX - 3, headY - 1, eyeColor);
            context.fill(headX + 3, headY - 2, headX + 4, headY - 1, eyeColor);
            if (won) {
                for (int i = -3; i <= 3; i++) {
                    context.fill(headX + i, headY + 4 + (i * i) / 6, headX + i + 1, headY + 5 + (i * i) / 6, 0xFF2B3038);
                }
            }
        }
        if (mistakes >= 2) context.fill(headX - 1, headY + headRadius, headX + 2, headY + headRadius + 28, 0xFFE7D4B3);
        if (mistakes >= 3) {
            for (int i = 0; i < 16; i++) {
                context.fill(headX - i, headY + headRadius + 4 + i / 2, headX - i + 2, headY + headRadius + 6 + i / 2, 0xFFE7D4B3);
            }
        }
        if (mistakes >= 4) {
            for (int i = 0; i < 16; i++) {
                context.fill(headX + i, headY + headRadius + 4 + i / 2, headX + i + 2, headY + headRadius + 6 + i / 2, 0xFFE7D4B3);
            }
        }
        if (mistakes >= 5) {
            for (int i = 0; i < 16; i++) {
                context.fill(headX - i / 2, headY + headRadius + 26 + i, headX - i / 2 + 3, headY + headRadius + 27 + i, 0xFFE7D4B3);
            }
        }
        if (mistakes >= 6) {
            for (int i = 0; i < 16; i++) {
                context.fill(headX + i / 2, headY + headRadius + 26 + i, headX + i / 2 + 3, headY + headRadius + 27 + i, 0xFFE7D4B3);
            }
        }
        if (mistakes >= 7) {
            context.fill(headX - 7, headY - 4, headX - 1, headY - 2, ArcadeTheme.DANGER);
            context.fill(headX + 2, headY - 4, headX + 8, headY - 2, ArcadeTheme.DANGER);
        }

        // Corações restantes.
        int hearts = 7 - mistakes;
        int heartX = x + width - 12;
        for (int i = 0; i < 7; i++) {
            int color = i < hearts ? ArcadeTheme.MAGENTA : 0x33FFFFFF;
            ArcadeTheme.heart(context, heartX - i * 11, y + 6, 4, color);
        }
    }

    private void drawKeyboard(DrawContext context, int mouseX, int mouseY, int ignored) {
        String[] rows = {"ABCDEFGHI", "JKLMNOPQR", "STUVWXYZ"};
        for (int row = 0; row < rows.length; row++) {
            int y = keyboardTop + row * (keyHeight + keyGap);
            int startX = (width - (rows[row].length() * keySize + (rows[row].length() - 1) * keyGap)) / 2;
            for (int i = 0; i < rows[row].length(); i++) {
                char letter = rows[row].charAt(i);
                int x = startX + i * (keySize + keyGap);
                boolean hovered = mouseX >= x && mouseX < x + keySize && mouseY >= y && mouseY < y + keyHeight;
                int fill;
                if (guessed.contains(letter)) {
                    fill = word.indexOf(letter) >= 0 ? 0xFF2E9E5B : 0xFF7A3B3B;
                } else {
                    fill = hovered ? 0xFF2E4B69 : 0xFF1C2E44;
                }
                ArcadeTheme.rounded(context, x, y + 2, x + keySize, y + keyHeight + 2, 4, 0x44000000);
                ArcadeTheme.rounded(context, x, y, x + keySize, y + keyHeight, 4, 0x44FFFFFF);
                ArcadeTheme.rounded(context, x + 1, y + 1, x + keySize - 1, y + keyHeight - 1, 3, fill);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(String.valueOf(letter)),
                        x + keySize / 2, y + keyHeight / 2 - 4,
                        guessed.contains(letter) ? 0xFFFFFFFF : ArcadeTheme.MUTED);
            }
        }
    }

    private boolean clickKeyboard(double mouseX, double mouseY) {
        String[] rows = {"ABCDEFGHI", "JKLMNOPQR", "STUVWXYZ"};
        for (int row = 0; row < rows.length; row++) {
            int y = keyboardTop + row * (keyHeight + keyGap);
            int startX = (width - (rows[row].length() * keySize + (rows[row].length() - 1) * keyGap)) / 2;
            if (mouseY < y || mouseY >= y + keyHeight) continue;
            for (int i = 0; i < rows[row].length(); i++) {
                int x = startX + i * (keySize + keyGap);
                if (mouseX >= x && mouseX < x + keySize) {
                    guess(rows[row].charAt(i));
                    return true;
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
