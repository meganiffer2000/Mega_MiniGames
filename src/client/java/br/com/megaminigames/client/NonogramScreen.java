package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Nonograma: descubra o desenho escondido usando as pistas numéricas de cada
 * linha e coluna. Clique esquerdo pinta, clique direito marca com X.
 */
public final class NonogramScreen extends ArcadeScreen {
    private static final int SIZE = 10;
    private static final int UNKNOWN = 0;
    private static final int FILLED = 1;
    private static final int MARKED = 2;

    private final boolean[][] solution = new boolean[SIZE][SIZE];
    private final int[][] state = new int[SIZE][SIZE];
    private final int[][] rowClues = new int[SIZE][];
    private final int[][] columnClues = new int[SIZE][];
    private int cell;
    private int gridLeft;
    private int gridTop;
    private int clueLeft;
    private int clueTop;
    private int hintsLeft;
    private int mistakes;
    private int moves;
    private int ticks;
    private boolean solved;
    private int solvedTicks;

    public NonogramScreen() {
        super("Nonograma");
    }

    @Override
    protected void init() {
        int budget = Math.min(width - 36, height - 96);
        cell = Math.max(9, Math.min(22, budget / (SIZE + 3)));
        clueLeft = cell * 2 + 4;
        clueTop = cell * 2 + 4;
        gridLeft = (width - (clueLeft + cell * SIZE)) / 2;
        gridTop = 62 + clueTop;
        addFooterButtons(height - 26, this::newPuzzle);
        newPuzzle();
    }

    private void newPuzzle() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        do {
            for (int row = 0; row < SIZE; row++) {
                for (int column = 0; column < SIZE; column++) {
                    solution[row][column] = random.nextInt(100) < 52;
                    state[row][column] = UNKNOWN;
                }
            }
        } while (!isInteresting());
        for (int index = 0; index < SIZE; index++) {
            rowClues[index] = runs(true, index);
            columnClues[index] = runs(false, index);
        }
        hintsLeft = 3;
        mistakes = 0;
        moves = 0;
        solved = false;
        solvedTicks = 0;
    }

    private boolean isInteresting() {
        int total = 0;
        for (int row = 0; row < SIZE; row++) {
            int rowCount = 0;
            for (int column = 0; column < SIZE; column++) if (solution[row][column]) rowCount++;
            if (rowCount == 0 || rowCount == SIZE) return false;
            total += rowCount;
        }
        for (int column = 0; column < SIZE; column++) {
            int count = 0;
            for (int row = 0; row < SIZE; row++) if (solution[row][column]) count++;
            if (count == 0 || count == SIZE) return false;
        }
        return total >= SIZE * SIZE / 3;
    }

    private int[] runs(boolean row, int index) {
        List<Integer> list = new ArrayList<>();
        int run = 0;
        for (int step = 0; step < SIZE; step++) {
            boolean filled = row ? solution[index][step] : solution[step][index];
            if (filled) {
                run++;
            } else if (run > 0) {
                list.add(run);
                run = 0;
            }
        }
        if (run > 0) list.add(run);
        int[] result = new int[list.size()];
        for (int i = 0; i < result.length; i++) result[i] = list.get(i);
        return result;
    }

    private boolean isSolved() {
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                boolean filled = state[row][column] == FILLED;
                if (filled != solution[row][column]) return false;
            }
        }
        return true;
    }

    private void useHint() {
        if (solved || hintsLeft <= 0) return;
        List<int[]> options = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (solution[row][column] && state[row][column] != FILLED) {
                    options.add(new int[] {row, column});
                }
            }
        }
        if (options.isEmpty()) return;
        int[] pick = options.get(ThreadLocalRandom.current().nextInt(options.size()));
        state[pick[0]][pick[1]] = FILLED;
        hintsLeft--;
        moves++;
        if (isSolved()) {
            solved = true;
            solvedTicks = 0;
        }
    }

    @Override
    public void tick() {
        ticks++;
        if (solved) solvedTicks++;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (solved || button > 1) return false;
        int column = (int) ((mouseX - gridLeft) / cell);
        int row = (int) ((mouseY - gridTop) / cell);
        if (row < 0 || row >= SIZE || column < 0 || column >= SIZE) return false;
        if (mouseX < gridLeft || mouseY < gridTop) return false;
        if (button == 1) {
            state[row][column] = state[row][column] == MARKED ? UNKNOWN : MARKED;
        } else {
            state[row][column] = state[row][column] == FILLED ? UNKNOWN : FILLED;
            if (state[row][column] == FILLED && !solution[row][column]) {
                mistakes++;
            }
        }
        moves++;
        if (isSolved()) {
            solved = true;
            solvedTicks = 0;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            newPuzzle();
            return true;
        }
        if (keyCode == 72) {
            useHint();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 10, ArcadeTheme.PURPLE);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(solved ? "Desenho revelado!" : "Esquerdo pinta  •  direito marca X  •  H dica"),
                width / 2, 30, solved ? ArcadeTheme.SUCCESS : ArcadeTheme.MUTED);
        int filledCount = 0;
        int target = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (state[row][column] == FILLED) filledCount++;
                if (solution[row][column]) target++;
            }
        }
        ArcadeTheme.pill(context, textRenderer, "PINTADAS " + filledCount + "/" + target, gridLeft, 44,
                ArcadeTheme.PURPLE);
        ArcadeTheme.pillRight(context, textRenderer, "DICAS " + hintsLeft + "  •  ERROS " + mistakes,
                gridLeft + clueLeft + cell * SIZE, 44, ArcadeTheme.GOLD);
        float progress = target == 0 ? 0 : filledCount / (float) target;
        ArcadeTheme.progress(context, gridLeft, 56, clueLeft + cell * SIZE, 4,
                Math.min(1.0F, progress), ArcadeTheme.PURPLE);

        ArcadeTheme.rounded(context, gridLeft - clueLeft - 6, gridTop - clueTop - 6,
                gridLeft + cell * SIZE + 6, gridTop + cell * SIZE + 6, 10, 0x66000000);
        ArcadeTheme.rounded(context, gridLeft - clueLeft - 5, gridTop - clueTop - 5,
                gridLeft + cell * SIZE + 5, gridTop + cell * SIZE + 5, 9, 0x55000000);
        context.fill(gridLeft - clueLeft, gridTop - clueTop, gridLeft + cell * SIZE, gridTop + cell * SIZE,
                0x11FFFFFF);

        for (int row = 0; row < SIZE; row++) {
            int y = gridTop + row * cell;
            for (int column = 0; column < SIZE; column++) {
                int x = gridLeft + column * cell;
                boolean dark = (row + column) % 2 == 0;
                context.fill(x, y, x + cell, y + cell, dark ? 0xFF101B2B : 0xFF152233);
                context.fill(x, y, x + cell, y + 1, 0x14FFFFFF);
                if (state[row][column] == FILLED) {
                    ArcadeTheme.rounded(context, x + 1, y + 1, x + cell - 1, y + cell - 1, 2,
                            ArcadeTheme.PURPLE);
                    context.fillGradient(x + 2, y + 2, x + cell - 2, y + cell / 2,
                            ArcadeTheme.lighten(ArcadeTheme.PURPLE, 40), ArcadeTheme.PURPLE);
                } else if (state[row][column] == MARKED) {
                    ArcadeTheme.drawX(context, x + cell / 2, y + cell / 2, Math.max(2, cell / 4), 1,
                            0x88A9B8CC);
                }
            }
        }
        for (int step = 0; step <= SIZE; step++) {
            int x = gridLeft + step * cell;
            context.fill(x, gridTop, x + 1, gridTop + cell * SIZE, step % 5 == 0 ? 0xFF6C7FA0 : 0x55FFFFFF);
            int y = gridTop + step * cell;
            context.fill(gridLeft, y, gridLeft + cell * SIZE, y + 1, step % 5 == 0 ? 0xFF6C7FA0 : 0x55FFFFFF);
        }

        int rowSpacing = Math.max(7, Math.min(cell / 2 + 3, 18));
        for (int row = 0; row < SIZE; row++) {
            int[] clues = rowClues[row];
            int spacing = clues.length <= 1 ? rowSpacing
                    : Math.min(rowSpacing, (clueLeft - 8) / clues.length);
            int y = gridTop + row * cell + cell / 2 - 4;
            for (int index = 0; index < clues.length; index++) {
                int x = clueLeft - 7 - (clues.length - 1 - index) * spacing;
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(clues[index])),
                        x, y, ArcadeTheme.TEXT);
            }
        }
        for (int column = 0; column < SIZE; column++) {
            int[] clues = columnClues[column];
            int spacing = clues.length <= 1 ? rowSpacing
                    : Math.min(rowSpacing, (clueTop - 8) / clues.length);
            int x = gridLeft + column * cell + cell / 2;
            for (int index = 0; index < clues.length; index++) {
                int y = gridTop - 6 - (clues.length - 1 - index) * spacing;
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(clues[index])),
                        x, y, ArcadeTheme.TEXT);
            }
        }

        if (!solved) {
            int lineRow = (int) ((mouseY - gridTop) / cell);
            int lineColumn = (int) ((mouseX - gridLeft) / cell);
            if (lineRow >= 0 && lineRow < SIZE && lineColumn >= 0 && lineColumn < SIZE
                    && mouseX >= gridLeft && mouseY >= gridTop) {
                int x = gridLeft + lineColumn * cell;
                int y = gridTop + lineRow * cell;
                context.fill(x, y, x + cell, y + cell, 0x22FFFFFF);
            }
        } else {
            ArcadeTheme.banner(context, textRenderer, "DESENHO COMPLETO",
                    "em " + moves + " jogadas  •  " + mistakes + " erro(s)  •  R para outro",
                    width / 2, gridTop + cell * SIZE / 2, Math.min(width - 20, 280), ArcadeTheme.SUCCESS);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
