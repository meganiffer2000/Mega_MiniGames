package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Jogo da memória com oito pares de cartas. */
public final class MemoryScreen extends Screen {
    private static final int SIDE = 4;
    private static final int CARD_SIZE = 54;
    private static final int GAP = 7;
    private final int[] cards = new int[SIDE * SIDE];
    private final boolean[] matched = new boolean[SIDE * SIDE];

    private boolean[] faceUp = new boolean[SIDE * SIDE];
    private int firstCard = -1;
    private int secondCard = -1;
    private int mismatchTicks;
    private int moves;
    private int gridLeft;
    private int gridTop;
    private boolean initialized;

    public MemoryScreen() {
        super(Text.literal("Jogo da Memória"));
    }

    @Override
    protected void init() {
        gridLeft = (width - SIDE * CARD_SIZE - (SIDE - 1) * GAP) / 2;
        gridTop = Math.max(58, (height - SIDE * CARD_SIZE - 70) / 2);
        int buttonY = Math.min(height - 24, gridTop + SIDE * CARD_SIZE + (SIDE - 1) * GAP + 18);

        addDrawableChild(ButtonWidget.builder(Text.literal("Novo jogo"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (!initialized) {
            newGame();
        }
    }

    private void newGame() {
        for (int i = 0; i < cards.length; i++) {
            cards[i] = i / 2;
            matched[i] = false;
        }
        for (int i = cards.length - 1; i > 0; i--) {
            int other = ThreadLocalRandom.current().nextInt(i + 1);
            int value = cards[i];
            cards[i] = cards[other];
            cards[other] = value;
        }
        faceUp = new boolean[cards.length];
        firstCard = -1;
        secondCard = -1;
        mismatchTicks = 0;
        moves = 0;
        initialized = true;
    }

    @Override
    public void tick() {
        if (mismatchTicks > 0 && --mismatchTicks == 0) {
            faceUp[firstCard] = false;
            faceUp[secondCard] = false;
            firstCard = -1;
            secondCard = -1;
        }
    }

    private void flip(int index) {
        if (mismatchTicks > 0 || matched[index] || faceUp[index]) {
            return;
        }
        faceUp[index] = true;
        if (firstCard == -1) {
            firstCard = index;
        } else {
            secondCard = index;
            moves++;
            if (cards[firstCard] == cards[secondCard]) {
                matched[firstCard] = true;
                matched[secondCard] = true;
                firstCard = -1;
                secondCard = -1;
            } else {
                mismatchTicks = 24;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
        int pairsFound = 0;
        for (boolean value : matched) {
            if (value) {
                pairsFound++;
            }
        }
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Jogadas: " + moves + "   Pares: " + pairsFound / 2 + "/8"),
                width / 2, 34, 0xFFE0E8FF);

        for (int index = 0; index < cards.length; index++) {
            int row = index / SIDE;
            int column = index % SIDE;
            int x = gridLeft + column * (CARD_SIZE + GAP);
            int y = gridTop + row * (CARD_SIZE + GAP);
            boolean open = faceUp[index] || matched[index];
            int background = matched[index] ? 0xFF3B7958 : open ? 0xFFE6D8B5 : 0xFF496D8B;
            context.fill(x, y, x + CARD_SIZE, y + CARD_SIZE, 0xFF17232D);
            context.fill(x + 2, y + 2, x + CARD_SIZE - 2, y + CARD_SIZE - 2, background);

            String label = open ? symbol(cards[index]) : "?";
            int color = open ? 0xFF26343A : 0xFFFFFFFF;
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(label),
                    x + CARD_SIZE / 2, y + CARD_SIZE / 2 - 4, color);
        }

        if (pairsFound == cards.length) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Parabéns! Você encontrou todos os pares!"),
                    width / 2, gridTop + SIDE * (CARD_SIZE + GAP) + 2, 0xFF77FF99);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private String symbol(int value) {
        return switch (value) {
            case 0 -> "A";
            case 1 -> "B";
            case 2 -> "C";
            case 3 -> "D";
            case 4 -> "E";
            case 5 -> "F";
            case 6 -> "G";
            default -> "H";
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }
        for (int index = 0; index < cards.length; index++) {
            int row = index / SIDE;
            int column = index % SIDE;
            int x = gridLeft + column * (CARD_SIZE + GAP);
            int y = gridTop + row * (CARD_SIZE + GAP);
            if (mouseX >= x && mouseX < x + CARD_SIZE && mouseY >= y && mouseY < y + CARD_SIZE) {
                flip(index);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
