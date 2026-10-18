package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
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
        int y = height - 32;
        addDrawableChild(ButtonWidget.builder(Text.literal("Comprar carta"), button -> hit())
                .dimensions(width / 2 - 174, y, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Parar"), button -> stand())
                .dimensions(width / 2 - 56, y, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Nova partida"), button -> newGame())
                .dimensions(width / 2 + 62, y, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width - 70, 8, 60, 20).build());
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
        context.fill(0, 0, width, height, 0xFF15563E);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 18, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Dealer: " + handValue(dealer)),
                width / 2, height / 2 - 95, 0xFFFFFFFF);
        drawHand(context, dealer, height / 2 - 72);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Você: " + handValue(player)),
                width / 2, height / 2 + 18, 0xFFFFFFFF);
        drawHand(context, player, height / 2 + 42);

        String result = switch (state) {
            case 1 -> "Você venceu!";
            case 2 -> handValue(player) > 21 ? "Passou de 21! Dealer venceu." : "Dealer venceu.";
            case 3 -> "Empate!";
            default -> "Compre cartas ou pare para revelar a mão do dealer.";
        };
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(result), width / 2,
                height / 2 + 115, 0xFFFFE7A3);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawHand(DrawContext context, List<Card> hand, int y) {
        int cardWidth = 42;
        int gap = 5;
        int totalWidth = hand.size() * (cardWidth + gap) - gap;
        int startX = (width - totalWidth) / 2;
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            int x = startX + i * (cardWidth + gap);
            context.fill(x, y, x + cardWidth, y + 62, 0xFFF2F0E8);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(card.rankName() + card.suitName()), x + cardWidth / 2, y + 25,
                    card.suit == 1 || card.suit == 2 ? 0xFFCE3434 : 0xFF17212B);
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
        private String suitName() {
            return switch (suit) {
                case 0 -> "P";
                case 1 -> "C";
                case 2 -> "O";
                default -> "E";
            };
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
