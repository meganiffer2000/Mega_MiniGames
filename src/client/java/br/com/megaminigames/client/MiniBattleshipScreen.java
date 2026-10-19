package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Batalha naval compacta: encontre um navio de três casas em até doze tiros. */
public final class MiniBattleshipScreen extends Screen {
    private static final int SIDE = 5;
    private static final int SHIP_SIZE = 3;
    private int cellSize;
    private int left;
    private int top;
    private boolean[][] ship = new boolean[SIDE][SIDE];
    private boolean[][] shots = new boolean[SIDE][SIDE];
    private boolean[][] hits = new boolean[SIDE][SIDE];
    private int hitCount;
    private int shotCount;
    private boolean gameOver;
    private boolean won;

    public MiniBattleshipScreen() {
        super(Text.literal("Batalha Naval"));
    }

    @Override
    protected void init() {
        cellSize = Math.max(18, Math.min(58, Math.min((width - 36) / SIDE, (height - 130) / SIDE)));
        left = (width - SIDE * cellSize) / 2;
        top = Math.max(52, (height - SIDE * cellSize - 55) / 2);
        addDrawableChild(ButtonWidget.builder(Text.literal("Nova batalha"), button -> newGame())
                .dimensions(width / 2 - 116, height - 25, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, height - 25, 112, 20).build());
        if (!initialized) {
            newGame();
            initialized = true;
        }
    }

    private boolean initialized;

    private void newGame() {
        ship = new boolean[SIDE][SIDE];
        shots = new boolean[SIDE][SIDE];
        hits = new boolean[SIDE][SIDE];
        boolean horizontal = ThreadLocalRandom.current().nextBoolean();
        int row = horizontal ? ThreadLocalRandom.current().nextInt(SIDE) : ThreadLocalRandom.current().nextInt(SIDE - SHIP_SIZE + 1);
        int column = horizontal ? ThreadLocalRandom.current().nextInt(SIDE - SHIP_SIZE + 1) : ThreadLocalRandom.current().nextInt(SIDE);
        for (int i = 0; i < SHIP_SIZE; i++) ship[row + (horizontal ? 0 : i)][column + (horizontal ? i : 0)] = true;
        hitCount = 0;
        shotCount = 0;
        gameOver = false;
        won = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 13, 0xFFFFFFFF);
        String status = won ? "Navio afundado! Você venceu!" : gameOver ? "Acabaram os tiros." :
                "Tiros: " + shotCount + "/12  •  Acertos: " + hitCount + "/3  •  Clique para disparar";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, 34,
                won ? 0xFF70EB89 : 0xFFE8E8E8);
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int x = left + column * cellSize;
                int y = top + row * cellSize;
                int color = shots[row][column] ? hits[row][column] ? 0xFFD94C4C : 0xFF52677A : 0xFF2878A8;
                if (gameOver && ship[row][column] && !shots[row][column]) color = 0xFFB88743;
                context.fill(x + 2, y + 2, x + cellSize - 2, y + cellSize - 2, color);
                if (shots[row][column]) {
                    context.drawCenteredTextWithShadow(textRenderer, Text.literal(hits[row][column] ? "X" : "·"),
                            x + cellSize / 2, y + cellSize / 2 - 4, 0xFFFFFFFF);
                }
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0 || gameOver || mouseX < left || mouseY < top
                || mouseX >= left + SIDE * cellSize || mouseY >= top + SIDE * cellSize) return false;
        int column = (int) ((mouseX - left) / cellSize);
        int row = (int) ((mouseY - top) / cellSize);
        if (shots[row][column]) return true;
        shots[row][column] = true;
        shotCount++;
        if (ship[row][column]) {
            hits[row][column] = true;
            hitCount++;
        }
        if (hitCount == SHIP_SIZE) {
            won = true;
            gameOver = true;
        } else if (shotCount >= 12) {
            gameOver = true;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
