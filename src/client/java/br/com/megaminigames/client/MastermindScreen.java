package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Senha Secreta: descubra a combinação de quatro cores usando as dicas de cada tentativa. */
public final class MastermindScreen extends ArcadeScreen {
    private static final int SLOTS = 4;
    private static final int ATTEMPTS = 8;
    private static final int[] COLORS = {
            ArcadeTheme.CORAL, ArcadeTheme.GOLD, ArcadeTheme.LIME,
            ArcadeTheme.SKY, ArcadeTheme.PURPLE, 0xFFF2F2F2
    };

    private final int[] secret = new int[SLOTS];
    private final List<int[]> guesses = new ArrayList<>();
    private final List<int[]> feedback = new ArrayList<>();
    private final int[] current = new int[SLOTS];
    private int currentLength;
    private boolean gameOver;
    private boolean won;
    private int ticks;
    private int rowHeight;
    private int boardTop;
    private int boardLeft;
    private int pegGap;
    private int paletteY;
    private int paletteGap;
    private int paletteLeft;

    public MastermindScreen() {
        super("Senha Secreta");
    }

    @Override
    protected void init() {
        rowHeight = Math.max(13, Math.min(21, (height - 142) / ATTEMPTS));
        boardTop = 48;
        pegGap = rowHeight;
        boardLeft = Math.max(14, (width - (SLOTS + 2) * pegGap - 12) / 2);
        paletteY = boardTop + ATTEMPTS * rowHeight + 10;
        paletteGap = Math.min(30, Math.max(16, (width - 44) / COLORS.length));
        paletteLeft = (width - paletteGap * COLORS.length) / 2 + paletteGap / 2;
        addFooterButtons(height - 26, this::newGame);
        newGame();
    }

    private void newGame() {
        for (int index = 0; index < SLOTS; index++) {
            secret[index] = ThreadLocalRandom.current().nextInt(COLORS.length);
            current[index] = -1;
        }
        guesses.clear();
        feedback.clear();
        currentLength = 0;
        gameOver = false;
        won = false;
    }

    private void addColor(int color) {
        if (gameOver || currentLength >= SLOTS) return;
        current[currentLength++] = color;
    }

    private void erase() {
        if (gameOver || currentLength == 0) return;
        current[--currentLength] = -1;
    }

    private void submit() {
        if (gameOver || currentLength < SLOTS) return;
        int[] guess = new int[SLOTS];
        System.arraycopy(current, 0, guess, 0, SLOTS);
        int[] result = judge(guess);
        guesses.add(guess);
        feedback.add(result);
        currentLength = 0;
        for (int index = 0; index < SLOTS; index++) current[index] = -1;
        if (result[0] == SLOTS) {
            won = true;
            gameOver = true;
        } else if (guesses.size() >= ATTEMPTS) {
            gameOver = true;
        }
    }

    private int[] judge(int[] guess) {
        boolean[] usedSecret = new boolean[SLOTS];
        boolean[] usedGuess = new boolean[SLOTS];
        int black = 0;
        int white = 0;
        for (int index = 0; index < SLOTS; index++) {
            if (guess[index] == secret[index]) {
                black++;
                usedSecret[index] = true;
                usedGuess[index] = true;
            }
        }
        for (int index = 0; index < SLOTS; index++) {
            if (usedGuess[index]) continue;
            for (int other = 0; other < SLOTS; other++) {
                if (usedSecret[other] || guess[index] != secret[other]) continue;
                white++;
                usedSecret[other] = true;
                break;
            }
        }
        return new int[]{black, white};
    }

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || gameOver) return super.mouseClicked(mouseX, mouseY, button);
        for (int index = 0; index < COLORS.length; index++) {
            double centerX = paletteLeft + index * paletteGap;
            if (Math.abs(mouseX - centerX) <= 9 && Math.abs(mouseY - paletteY) <= 9) {
                addColor(index);
                return true;
            }
        }
        int row = (int) ((mouseY - boardTop) / rowHeight);
        if (row == guesses.size() && row < ATTEMPTS) {
            int column = (int) ((mouseX - boardLeft - 4) / pegGap);
            if (column >= 0 && column < SLOTS) {
                if (column < currentLength) currentLength = column;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            newGame();
            return true;
        }
        if (gameOver) return super.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode >= 49 && keyCode <= 54) {
            addColor(keyCode - 49);
            return true;
        }
        if (keyCode == 259) {
            erase();
            return true;
        }
        if (keyCode == 257 || keyCode == 335 || keyCode == 32) {
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.PURPLE);
        String status = gameOver ? (won ? "Código quebrado em " + guesses.size() + " tentativas!"
                : "O código era outro. Tente de novo!")
                : "Monte o código e pressione Enter";
        ArcadeTheme.status(context, textRenderer, Text.literal(status), width / 2, 31,
                gameOver ? (won ? ArcadeTheme.SUCCESS : ArcadeTheme.CORAL) : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "TENTATIVA " + Math.min(guesses.size() + 1, ATTEMPTS) + "/" + ATTEMPTS,
                12, 47, ArcadeTheme.PURPLE);
        ArcadeTheme.pillRight(context, textRenderer, "CHEIO = certa  •  ANEL = cor só", width - 12, 47,
                ArcadeTheme.MUTED);

        for (int row = 0; row < ATTEMPTS; row++) {
            int y = boardTop + row * rowHeight + rowHeight / 2;
            boolean activeRow = row == guesses.size() && !gameOver;
            int rowLeft = boardLeft;
            if (activeRow) {
                ArcadeTheme.rounded(context, rowLeft - 5, y - rowHeight / 2 - 1, width - rowLeft + 5,
                        y + rowHeight / 2 + 1, Math.max(3, rowHeight / 3), 0x1FFFFFFF);
            }
            for (int column = 0; column < SLOTS; column++) {
                int centerX = rowLeft + 4 + column * pegGap + pegGap / 2;
                int color = -1;
                if (row < guesses.size()) {
                    color = guesses.get(row)[column];
                } else if (activeRow && column < currentLength) {
                    color = current[column];
                }
                int radius = Math.max(3, rowHeight / 2 - 2);
                ArcadeTheme.disc(context, centerX, y, radius + 1, 0xFF0A1422);
                if (color >= 0) {
                    ArcadeTheme.disc(context, centerX, y, radius, COLORS[color]);
                    ArcadeTheme.disc(context, centerX - radius / 3, y - radius / 3,
                            Math.max(1, radius / 3), 0x77FFFFFF);
                } else {
                    ArcadeTheme.ring(context, centerX, y, radius, 1, activeRow ? 0x88FFFFFF : 0x33FFFFFF);
                }
            }
            if (row < feedback.size()) {
                int[] marks = feedback.get(row);
                int feedbackX = boardLeft + SLOTS * pegGap + 18;
                int peg = Math.max(2, Math.min(4, rowHeight / 4));
                int index = 0;
                for (int column = 0; column < 2; column++) {
                    for (int line = 0; line < 2; line++) {
                        int centerX = feedbackX + column * (peg * 2 + 3);
                        int centerY = y - peg - 1 + line * (peg * 2 + 2);
                        ArcadeTheme.disc(context, centerX, centerY, peg + 1, 0xFF0A1422);
                        if (index < marks[0]) {
                            ArcadeTheme.disc(context, centerX, centerY, peg, 0xFF1B2A3E);
                            ArcadeTheme.disc(context, centerX, centerY, Math.max(1, peg - 1), 0xFFFFFFFF);
                        } else if (index < marks[0] + marks[1]) {
                            ArcadeTheme.ring(context, centerX, centerY, peg, 1, ArcadeTheme.GOLD);
                        } else {
                            ArcadeTheme.ring(context, centerX, centerY, peg, 1, 0x33FFFFFF);
                        }
                        index++;
                    }
                }
            }
        }
        ArcadeTheme.divider(context, 12, width - 12, paletteY - 8);

        if (gameOver) {
            for (int index = 0; index < SLOTS; index++) {
                int centerX = width / 2 - (SLOTS - 1) * 8 + index * 16;
                ArcadeTheme.disc(context, centerX, paletteY + 4, 7, COLORS[secret[index]]);
                ArcadeTheme.ring(context, centerX, paletteY + 4, 7, 1, 0x99FFFFFF);
            }
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("O CÓDIGO SECRETO"),
                    width / 2, paletteY - 5, ArcadeTheme.MUTED);
        } else {
            for (int index = 0; index < COLORS.length; index++) {
                int centerX = paletteLeft + index * paletteGap;
                boolean hovered = Math.abs(mouseX - centerX) <= 9 && Math.abs(mouseY - paletteY) <= 9;
                if (hovered) {
                    ArcadeTheme.glow(context, centerX, paletteY, 11, 0x66FFFFFF);
                }
                ArcadeTheme.disc(context, centerX, paletteY, hovered ? 8 : 7, COLORS[index]);
                ArcadeTheme.disc(context, centerX - 2, paletteY - 2, 2, 0x66FFFFFF);
            }
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Clique nas cores (ou teclas 1–6)  •  Enter envia"),
                    width / 2, paletteY + 14, ArcadeTheme.DIM);
        }
        if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, won ? "CÓDIGO QUEBRADO!" : "TENTATIVAS ESGOTADAS",
                    won ? "Você usou " + guesses.size() + " tentativa(s)" : "Tente uma nova senha com R",
                    width / 2, boardTop + ATTEMPTS * rowHeight / 2, Math.min(width - 20, 260),
                    won ? ArcadeTheme.SUCCESS : ArcadeTheme.CORAL);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
