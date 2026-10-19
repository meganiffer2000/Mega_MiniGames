package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Jogo da memória com oito pares de cartas. */
public final class MemoryScreen extends Screen {
    private static final int SIDE = 4;
    private static final int MAX_CARD_SIZE = 56;
    private static final int[] PAIR_COLORS = {
            0xFF35C8B9, 0xFFFFD66B, 0xFFF0509B, 0xFFB9F26B,
            0xFF6CC9F5, 0xFFFF8A65, 0xFF9B7BF0, 0xFFE9EDF5
    };
    private int cardSize;
    private int gap;
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
        int availableHeight = Math.max(96, height - 150);
        int heightLimitedSize = (availableHeight - (SIDE - 1) * 8) / SIDE;
        int widthLimitedSize = (width - 44 - (SIDE - 1) * 8) / SIDE;
        cardSize = Math.max(24, Math.min(MAX_CARD_SIZE, Math.min(heightLimitedSize, widthLimitedSize)));
        gap = Math.max(4, Math.min(8, cardSize / 8));
        int boardWidth = SIDE * cardSize + (SIDE - 1) * gap;
        gridLeft = (width - boardWidth) / 2;
        gridTop = Math.max(80, 84 + (availableHeight - boardWidth) / 2);
        int buttonY = Math.min(height - 26, gridTop + boardWidth + 22);
        int buttonWidth = Math.min(112, Math.max(76, (width - 44) / 2));

        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Novo jogo"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
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
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.MAGENTA);

        int pairsFound = 0;
        for (boolean value : matched) {
            if (value) pairsFound++;
        }
        pairsFound /= 2;
        String info = "Jogadas " + moves + "   •   Pares " + pairsFound + "/8";
        int pillWidth = textRenderer.getWidth(info) + 18;
        ArcadeTheme.pill(context, textRenderer, info, (width - pillWidth) / 2, 32, ArcadeTheme.MAGENTA);
        int barWidth = Math.min(190, width - 90);
        ArcadeTheme.progress(context, (width - barWidth) / 2, 51, barWidth, 6, pairsFound / 8.0F, ArcadeTheme.MAGENTA);

        int boardWidth = SIDE * cardSize + (SIDE - 1) * gap;
        ArcadeTheme.rounded(context, gridLeft - 7, gridTop - 7, gridLeft + boardWidth + 7, gridTop + boardWidth + 7, 14, 0x55000000);
        ArcadeTheme.rounded(context, gridLeft - 8, gridTop - 8, gridLeft + boardWidth + 8, gridTop + boardWidth + 8, 14, 0x55F0509B);
        ArcadeTheme.rounded(context, gridLeft - 7, gridTop - 7, gridLeft + boardWidth + 7, gridTop + boardWidth + 7, 13, 0xF00A1422);

        for (int index = 0; index < cards.length; index++) {
            int row = index / SIDE;
            int column = index % SIDE;
            int x = gridLeft + column * (cardSize + gap);
            int y = gridTop + row * (cardSize + gap);
            boolean open = faceUp[index] || matched[index];
            boolean hovered = mouseX >= x && mouseX < x + cardSize && mouseY >= y && mouseY < y + cardSize;
            int shake = 0;
            if (mismatchTicks > 0 && (index == firstCard || index == secondCard)) {
                shake = (mismatchTicks / 3) % 2 == 0 ? -1 : 1;
            }
            int drawX = x + shake;
            int radius = Math.max(4, cardSize / 8);

            if (open) {
                int pairColor = PAIR_COLORS[cards[index] % PAIR_COLORS.length];
                ArcadeTheme.rounded(context, drawX, y + 2, drawX + cardSize, y + cardSize + 2, radius, 0x55000000);
                ArcadeTheme.rounded(context, drawX, y, drawX + cardSize, y + cardSize, radius, 0xFFEADFC7);
                ArcadeTheme.rounded(context, drawX + 1, y + 1, drawX + cardSize - 1, y + cardSize - 1, radius - 1, 0xFFF8F1E2);
                ArcadeTheme.rounded(context, drawX + 1, y + 1, drawX + cardSize - 1, y + cardSize / 2, radius - 1, 0x38FFFFFF);
                ArcadeTheme.disc(context, drawX + cardSize / 2, y + cardSize / 2, Math.max(6, cardSize / 4),
                        ArcadeTheme.alpha(pairColor, 0xAA));
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(symbol(cards[index])),
                        drawX + cardSize / 2, y + cardSize / 2 - 4, 0xFF101A28);
                if (matched[index]) {
                    ArcadeTheme.rounded(context, drawX + 1, y + 1, drawX + cardSize - 1, y + cardSize - 1, radius - 1, 0x3326C281);
                    ArcadeTheme.drawCheck(context, drawX + cardSize - 14, y + 5, 0xFF2ECC71);
                }
            } else {
                ArcadeTheme.rounded(context, drawX, y + 2, drawX + cardSize, y + cardSize + 2, radius, 0x55000000);
                ArcadeTheme.rounded(context, drawX, y, drawX + cardSize, y + cardSize, radius,
                        hovered ? 0xFF4CA9C9 : 0xFF2D5D80);
                ArcadeTheme.rounded(context, drawX + 1, y + 1, drawX + cardSize - 1, y + cardSize - 1, radius - 1,
                        hovered ? 0xFF1C3E5C : 0xFF152C43);
                for (int line = 3; line < cardSize - 3; line += 6) {
                    context.fill(drawX + 4, y + line, drawX + cardSize - 4, y + line + 1, 0x10FFFFFF);
                }
                ArcadeTheme.rounded(context, drawX + 3, y + 3, drawX + cardSize - 3, y + cardSize / 2, radius - 2, 0x14FFFFFF);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("?"),
                        drawX + cardSize / 2, y + cardSize / 2 - 4, hovered ? ArcadeTheme.GOLD : 0xFF8FB6D6);
            }
        }

        if (pairsFound == cards.length / 2) {
            int bannerWidth = Math.min(width - 40, 250);
            int cx = width / 2;
            int cy = gridTop + boardWidth + 12;
            ArcadeTheme.rounded(context, cx - bannerWidth / 2, cy - 9, cx + bannerWidth / 2, cy + 10, 8, 0xEE0A1422);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("PARABÉNS! TODOS OS PARES!"),
                    cx, cy - 4, ArcadeTheme.SUCCESS);
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
            int x = gridLeft + column * (cardSize + gap);
            int y = gridTop + row * (cardSize + gap);
            if (mouseX >= x && mouseX < x + cardSize && mouseY >= y && mouseY < y + cardSize) {
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
