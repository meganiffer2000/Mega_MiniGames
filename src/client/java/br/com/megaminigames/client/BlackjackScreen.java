package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Blackjack simples: chegue o mais perto possível de 21 sem estourar. */
public final class BlackjackScreen extends Screen {
    private final List<Card> deck = new ArrayList<>();
    private final List<Card> player = new ArrayList<>();
    private final List<Card> dealer = new ArrayList<>();
    private int state;

    public BlackjackScreen() {
        super(Text.literal("Blackjack"));
    }

    @Override
    protected void init() {
        int y = height - 26;
        int buttonWidth = Math.min(104, Math.max(62, (width - 44) / 3));
        int total = buttonWidth * 3 + 8;
        int startX = (width - total) / 2;
        addDrawableChild(ArcadeTheme.button(startX, y, buttonWidth, 20, Text.literal("Comprar"), () -> hit()));
        addDrawableChild(ArcadeTheme.button(startX + buttonWidth + 4, y, buttonWidth, 20, Text.literal("Parar"), () -> stand()));
        addDrawableChild(ArcadeTheme.button(startX + (buttonWidth + 4) * 2, y, buttonWidth, 20,
                Text.literal("Nova mão"), () -> newGame()));
        addDrawableChild(ArcadeTheme.button(width - 66, 8, 58, 18, Text.literal("Menu"), () -> client.setScreen(new GamesHubScreen())));
        if (deck.isEmpty()) newGame();
    }

    private void newGame() {
        deck.clear();
        player.clear();
        dealer.clear();
        for (int suit = 0; suit < 4; suit++) {
            for (int rank = 1; rank <= 13; rank++) deck.add(new Card(rank, suit));
        }
        Collections.shuffle(deck);
        player.add(draw());
        dealer.add(draw());
        player.add(draw());
        dealer.add(draw());
        state = 0;
        if (handValue(player) == 21) stand();
    }

    private Card draw() {
        return deck.remove(deck.size() - 1);
    }

    private int handValue(List<Card> hand) {
        int total = 0;
        int aces = 0;
        for (Card card : hand) {
            if (card.rank == 1) {
                total += 11;
                aces++;
            } else {
                total += Math.min(card.rank, 10);
            }
        }
        while (total > 21 && aces-- > 0) total -= 10;
        return total;
    }

    private void hit() {
        if (state != 0 || deck.isEmpty()) return;
        player.add(draw());
        if (handValue(player) > 21) state = 2;
    }

    private void stand() {
        if (state != 0) return;
        while (handValue(dealer) < 17 && !deck.isEmpty()) dealer.add(draw());
        int p = handValue(player);
        int d = handValue(dealer);
        state = p > 21 || d > p ? 2 : d == p ? 3 : 1;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 10, ArcadeTheme.GOLD);
        int feltWidth = Math.min(width - 28, 430);
        int feltLeft = (width - feltWidth) / 2;
        int feltTop = 48;
        int feltBottom = height - 42;
        ArcadeTheme.rounded(context, feltLeft, feltTop, feltLeft + feltWidth, feltBottom, 18, 0x55000000);
        ArcadeTheme.rounded(context, feltLeft, feltTop, feltLeft + feltWidth, feltBottom, 18, 0x661D7A55);
        context.fillGradient(feltLeft + 1, feltTop + 1, feltLeft + feltWidth - 1, feltBottom - 1,
                0xFF116040, 0xFF0A3A29);
        ArcadeTheme.ring(context, feltLeft + feltWidth / 2, feltTop + 12, 10, 2, 0x33FFFFFF);
        ArcadeTheme.glow(context, feltLeft + feltWidth / 2, feltTop + 60, Math.max(40, feltWidth / 3), 0x2210FFC0);
        int chipsX = feltLeft + feltWidth - 46;
        int chipsY = feltTop + 30;
        for (int index = 0; index < 4; index++) {
            ArcadeTheme.disc(context, chipsX, chipsY + (3 - index) * 2, 5, 0xFFB83A3A);
            ArcadeTheme.disc(context, chipsX, chipsY + (3 - index) * 2, 3, 0xFFE86A5E);
        }
        for (int index = 0; index < 3; index++) {
            ArcadeTheme.disc(context, chipsX + 13, chipsY + 4 + (2 - index) * 2, 5, 0xFFB8932E);
            ArcadeTheme.disc(context, chipsX + 13, chipsY + 4 + (2 - index) * 2, 3, ArcadeTheme.GOLD);
        }
        ArcadeTheme.rounded(context, chipsX + 4, chipsY + 18, chipsX + 12, chipsY + 31, 2, 0xFF1B7D78);
        ArcadeTheme.pillRight(context, textRenderer, "BARALHO " + deck.size(), feltLeft + feltWidth - 12,
                feltTop + 10, ArcadeTheme.SKY);

        String dealerLabel = "DEALER  •  " + (state == 0 ? "?" : handValue(dealer));
        ArcadeTheme.pill(context, textRenderer, dealerLabel, feltLeft + 12, feltTop + 10, ArcadeTheme.CORAL);
        drawHand(context, dealer, feltLeft + 16, feltTop + 32, feltBottom - 10, state == 0);

        int resultY = (feltTop + feltBottom) / 2 - 14;
        if (state != 0) {
            String result = switch (state) {
                case 1 -> "VOCÊ VENCEU!";
                case 2 -> handValue(player) > 21 ? "ESTOUROU! DEALER VENCE" : "DEALER VENCEU";
                default -> "EMPATE!";
            };
            int color = state == 1 ? ArcadeTheme.SUCCESS : state == 3 ? ArcadeTheme.GOLD : ArcadeTheme.DANGER;
            int bannerWidth = Math.min(feltWidth - 30, 230);
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, resultY - 10, width / 2 + bannerWidth / 2,
                    resultY + 12, 9, 0xCC08111E);
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, resultY - 10, width / 2 + bannerWidth / 2,
                    resultY - 8, 9, color);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(result), width / 2, resultY - 2, color);
        } else {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Chegue perto de 21 sem estourar"), width / 2, resultY - 2, 0xCCFFFFFF);
        }

        String playerLabel = "VOCÊ  •  " + handValue(player);
        ArcadeTheme.pill(context, textRenderer, playerLabel, feltLeft + 12, feltBottom - 56, ArcadeTheme.TEAL);
        drawHand(context, player, feltLeft + 16, feltBottom - 38, feltBottom - 6, false);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawHand(DrawContext context, List<Card> hand, int startX, int y, int bottom, boolean hideSecond) {
        int cardWidth = 38;
        int cardHeight = 52;
        if (hand.size() > 2) {
            cardWidth = 34;
        }
        int maxWidth = width - 60;
        int step = cardWidth + 5;
        int totalWidth = hand.size() * step - 5;
        if (totalWidth > maxWidth) {
            step = Math.max(18, (maxWidth - cardWidth) / Math.max(1, hand.size() - 1));
            totalWidth = (hand.size() - 1) * step + cardWidth;
        }
        int start = startX + Math.max(0, (maxWidth - totalWidth) / 2);
        for (int i = 0; i < hand.size(); i++) {
            int x = start + i * step;
            if (hideSecond && i == 1) {
                drawCardBack(context, x, y, cardWidth, cardHeight);
            } else {
                drawCard(context, hand.get(i), x, y, cardWidth, cardHeight);
            }
        }
    }

    private void drawCardBack(DrawContext context, int x, int y, int width, int height) {
        ArcadeTheme.rounded(context, x, y, x + width, y + height, 5, 0x55000000);
        ArcadeTheme.rounded(context, x, y, x + width, y + height, 5, 0xFFE8E3D6);
        ArcadeTheme.rounded(context, x + 1, y + 1, x + width - 1, y + height - 1, 4, 0xFF243A54);
        for (int line = 3; line < height - 3; line += 5) {
            context.fill(x + 5, y + line, x + width - 5, y + line + 1, 0x22FFFFFF);
        }
        ArcadeTheme.ring(context, x + width / 2, y + height / 2, Math.max(5, width / 5), 2, 0x66FFFFFF);
    }

    private void drawCard(DrawContext context, Card card, int x, int y, int width, int height) {
        ArcadeTheme.rounded(context, x, y, x + width, y + height, 5, 0x55000000);
        ArcadeTheme.rounded(context, x, y, x + width, y + height, 5, 0xFFF7F2E6);
        ArcadeTheme.rounded(context, x + 1, y + 1, x + width - 1, y + height - 1, 4, 0xFFFBF6EA);
        int color = card.suit == 1 || card.suit == 2 ? 0xFFC8323C : 0xFF18212B;
        context.drawTextWithShadow(textRenderer, Text.literal(card.rankName()), x + 4, y + 4, color);
        drawSuit(context, card.suit, x + width / 2, y + height / 2 + 2, Math.max(4, width / 7), color);
        context.drawTextWithShadow(textRenderer, Text.literal(card.rankName()),
                x + width - 4 - textRenderer.getWidth(card.rankName()), y + height - 13, color);
    }

    private void drawSuit(DrawContext context, int suit, int cx, int cy, int size, int color) {
        switch (suit) {
            case 0 -> {
                for (int i = 0; i <= size; i++) {
                    context.fill(cx - i, cy - size + i / 2, cx + i + 1, cy - size + i / 2 + 1, color);
                }
                context.fill(cx - 1, cy - 1, cx + 2, cy + size / 2, color);
                ArcadeTheme.disc(context, cx - size / 2, cy - 1, size / 3, color);
                ArcadeTheme.disc(context, cx + size / 2, cy - 1, size / 3, color);
            }
            case 1 -> ArcadeTheme.heart(context, cx, cy, size, color);
            case 2 -> {
                for (int i = 0; i <= size; i++) {
                    int half = size - Math.abs(i - size / 2) * 2;
                    context.fill(cx - half, cy - size / 2 + i, cx + half + 1, cy - size / 2 + i + 1, color);
                }
            }
            default -> {
                context.fill(cx - 1, cy, cx + 2, cy + size, color);
                ArcadeTheme.disc(context, cx, cy - size / 2, Math.max(2, size / 2), color);
                ArcadeTheme.disc(context, cx - size / 2, cy, Math.max(2, size / 3), color);
                ArcadeTheme.disc(context, cx + size / 2, cy, Math.max(2, size / 3), color);
            }
        }
    }

    private record Card(int rank, int suit) {
        private String rankName() {
            return switch (rank) {
                case 1 -> "A";
                case 11 -> "J";
                case 12 -> "Q";
                case 13 -> "K";
                default -> Integer.toString(rank);
            };
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
