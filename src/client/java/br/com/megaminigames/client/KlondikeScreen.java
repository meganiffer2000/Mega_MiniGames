package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Paciência (Klondike): organize as cartas nas quatro fundações por naipe. */
public final class KlondikeScreen extends ArcadeScreen {
    private static final String[] RANKS = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

    private final List<Card> stock = new ArrayList<>();
    private final List<Card> waste = new ArrayList<>();
    private final List<List<Card>> tableau = new ArrayList<>();
    private final List<List<Card>> foundations = new ArrayList<>();
    private Selection selection;
    private int moves;
    private boolean won;
    private int cardWidth;
    private int cardHeight;
    private int topRowY;
    private int tableauY;
    private int stockX;
    private int wasteX;
    private int foundationX;
    private int pileGap;
    private int faceDownOffset;
    private int faceUpOffset;

    public KlondikeScreen() {
        super("Paciência");
    }

    @Override
    protected void init() {
        cardWidth = Math.max(30, Math.min(40, (width - 40) / 7));
        cardHeight = Math.max(40, cardWidth * 4 / 3);
        pileGap = Math.max(4, Math.min(8, (width - 28 - cardWidth * 7) / 6));
        stockX = 14;
        wasteX = stockX + cardWidth + 8;
        foundationX = width - 14 - cardWidth * 4 - pileGap * 3;
        topRowY = 52;
        tableauY = topRowY + cardHeight + 14;
        faceDownOffset = Math.max(3, cardHeight / 12);
        int available = Math.max(30, height - 34 - tableauY - cardHeight);
        faceUpOffset = Math.max(5, Math.min(14, available / 9));
        addFooterButtons(height - 26, this::newGame);
        newGame();
    }

    private void newGame() {
        stock.clear();
        waste.clear();
        tableau.clear();
        foundations.clear();
        selection = null;
        moves = 0;
        won = false;
        List<Card> deck = new ArrayList<>();
        for (int suit = 0; suit < 4; suit++) {
            for (int rank = 1; rank <= 13; rank++) {
                deck.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(deck, ThreadLocalRandom.current());
        for (int pile = 0; pile < 7; pile++) {
            List<Card> cards = new ArrayList<>();
            for (int index = 0; index <= pile; index++) {
                Card card = deck.remove(deck.size() - 1);
                card.faceUp = index == pile;
                cards.add(card);
            }
            tableau.add(cards);
        }
        for (int index = 0; index < 4; index++) {
            foundations.add(new ArrayList<>());
        }
        while (!deck.isEmpty()) {
            Card card = deck.remove(deck.size() - 1);
            card.faceUp = false;
            stock.add(card);
        }
    }

    private boolean isRed(Card card) {
        return card.suit == 0 || card.suit == 1;
    }

    private boolean canPlaceOnTableau(Card card, List<Card> pile) {
        if (pile.isEmpty()) return card.rank == 13;
        Card top = pile.get(pile.size() - 1);
        return top.faceUp && isRed(top) != isRed(card) && card.rank == top.rank - 1;
    }

    private boolean canPlaceOnFoundation(Card card, List<Card> pile) {
        if (pile.isEmpty()) return card.rank == 1;
        Card top = pile.get(pile.size() - 1);
        return top.suit == card.suit && card.rank == top.rank + 1;
    }

    private void flipTop(List<Card> pile) {
        if (!pile.isEmpty() && !pile.get(pile.size() - 1).faceUp) {
            pile.get(pile.size() - 1).faceUp = true;
        }
    }

    private List<Card> selectedCards() {
        if (selection == null) return Collections.emptyList();
        if (selection.type == 0) {
            return waste.isEmpty() ? Collections.emptyList()
                    : List.of(waste.get(waste.size() - 1));
        }
        if (selection.type == 1) {
            List<Card> pile = tableau.get(selection.pile);
            if (selection.index < 0 || selection.index >= pile.size()) return Collections.emptyList();
            return new ArrayList<>(pile.subList(selection.index, pile.size()));
        }
        List<Card> pile = foundations.get(selection.pile);
        return pile.isEmpty() ? Collections.emptyList() : List.of(pile.get(pile.size() - 1));
    }

    private void clearSelection() {
        selection = null;
    }

    private void tryMoveToTableau(int pileIndex) {
        List<Card> moving = selectedCards();
        if (moving.isEmpty()) return;
        List<Card> target = tableau.get(pileIndex);
        if (!canPlaceOnTableau(moving.get(0), target)) {
            clearSelection();
            return;
        }
        removeSelected(moving);
        target.addAll(moving);
        moves++;
        clearSelection();
        checkWin();
    }

    private void tryMoveToFoundation(int foundationIndex) {
        List<Card> moving = selectedCards();
        if (moving.size() != 1) {
            clearSelection();
            return;
        }
        List<Card> target = foundations.get(foundationIndex);
        if (!canPlaceOnFoundation(moving.get(0), target)) {
            clearSelection();
            return;
        }
        removeSelected(moving);
        target.add(moving.get(0));
        moves++;
        clearSelection();
        checkWin();
    }

    private void removeSelected(List<Card> moving) {
        if (selection.type == 0) {
            if (!waste.isEmpty()) waste.remove(waste.size() - 1);
        } else if (selection.type == 1) {
            List<Card> pile = tableau.get(selection.pile);
            pile.subList(selection.index, pile.size()).clear();
            flipTop(pile);
        } else {
            List<Card> pile = foundations.get(selection.pile);
            if (!pile.isEmpty()) pile.remove(pile.size() - 1);
        }
    }

    private void checkWin() {
        for (List<Card> foundation : foundations) {
            if (foundation.size() < 13) return;
        }
        won = true;
    }

    private void clickStock() {
        if (!stock.isEmpty()) {
            Card card = stock.remove(stock.size() - 1);
            card.faceUp = true;
            waste.add(card);
            clearSelection();
        } else if (!waste.isEmpty()) {
            while (!waste.isEmpty()) {
                Card card = waste.remove(waste.size() - 1);
                card.faceUp = false;
                stock.add(card);
            }
            clearSelection();
        }
        moves++;
    }

    private int tableauCardAt(int pileIndex, double mouseX, double mouseY) {
        if (mouseX < tableauX(pileIndex) || mouseX > tableauX(pileIndex) + cardWidth) return -1;
        List<Card> pile = tableau.get(pileIndex);
        int y = tableauY;
        int found = -1;
        for (int index = 0; index < pile.size(); index++) {
            int cardTop = y;
            int cardBottom = cardTop + cardHeight;
            if (mouseY >= cardTop && mouseY <= cardBottom) {
                found = index;
            }
            y += pile.get(index).faceUp ? faceUpOffset : faceDownOffset;
        }
        return found;
    }

    private int tableauX(int pileIndex) {
        return 14 + pileIndex * (cardWidth + pileGap);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        if (mouseY >= topRowY && mouseY <= topRowY + cardHeight) {
            if (mouseX >= stockX && mouseX <= stockX + cardWidth) {
                clickStock();
                return true;
            }
            if (!waste.isEmpty() && mouseX >= wasteX && mouseX <= wasteX + cardWidth) {
                selection = new Selection(0, 0, waste.size() - 1);
                return true;
            }
            for (int index = 0; index < 4; index++) {
                int x = foundationX + index * (cardWidth + pileGap);
                if (mouseX >= x && mouseX <= x + cardWidth) {
                    if (selection != null) {
                        tryMoveToFoundation(index);
                    } else if (!foundations.get(index).isEmpty()) {
                        selection = new Selection(2, index, foundations.get(index).size() - 1);
                    }
                    return true;
                }
            }
        }
        for (int pile = 0; pile < 7; pile++) {
            int cardIndex = tableauCardAt(pile, mouseX, mouseY);
            if (cardIndex >= 0) {
                if (selection != null) {
                    tryMoveToTableau(pile);
                } else {
                    List<Card> cards = tableau.get(pile);
                    Card card = cards.get(cardIndex);
                    if (!card.faceUp) {
                        if (cardIndex == cards.size() - 1) card.faceUp = true;
                        return true;
                    }
                    int start = isValidRun(cards, cardIndex) ? cardIndex : cards.size() - 1;
                    selection = new Selection(1, pile, start);
                }
                return true;
            }
        }
        clearSelection();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isValidRun(List<Card> cards, int start) {
        for (int index = start; index < cards.size() - 1; index++) {
            Card lower = cards.get(index);
            Card upper = cards.get(index + 1);
            if (!lower.faceUp || !upper.faceUp) return false;
            if (isRed(lower) == isRed(upper) || upper.rank != lower.rank - 1) return false;
        }
        return cards.get(start).faceUp;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            newGame();
            return true;
        }
        if (keyCode == 259) {
            clearSelection();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.LIME);
        String status = won ? "Paciência concluída! Todas as fundações cheias."
                : selection == null ? "Clique em uma carta e depois no destino  •  R embaralha"
                : "Carta selecionada — escolha uma pilha ou fundação";
        ArcadeTheme.status(context, textRenderer, Text.literal(status), width / 2, 31,
                won ? ArcadeTheme.SUCCESS : ArcadeTheme.MUTED);
        int foundationCards = 0;
        for (List<Card> foundation : foundations) foundationCards += foundation.size();
        ArcadeTheme.pill(context, textRenderer, "MONTE " + stock.size(), 12, 44, ArcadeTheme.LIME);
        ArcadeTheme.pillRight(context, textRenderer, "FUNDAÇÃO " + foundationCards + "/52  •  MOV " + moves,
                width - 12, 44, ArcadeTheme.TEAL);

        drawEmptySlot(context, stockX, topRowY);
        if (!stock.isEmpty()) {
            for (int layer = Math.min(3, stock.size()) - 1; layer >= 0; layer--) {
                ArcadeTheme.playingCard(context, textRenderer, stockX + layer, topRowY - layer,
                        cardWidth, cardHeight, "", 0, true);
            }
        } else {
            ArcadeTheme.ring(context, stockX + cardWidth / 2, topRowY + cardHeight / 2,
                    Math.min(cardWidth, cardHeight) / 5, 1, 0x66FFFFFF);
        }
        drawEmptySlot(context, wasteX, topRowY);
        if (!waste.isEmpty()) {
            Card card = waste.get(waste.size() - 1);
            drawCard(context, wasteX, topRowY, card, false);
            if (waste.size() > 1) {
                context.drawTextWithShadow(textRenderer, Text.literal("+" + (waste.size() - 1)),
                        wasteX + cardWidth - 12, topRowY + cardHeight - 10, ArcadeTheme.MUTED);
            }
        }
        for (int index = 0; index < 4; index++) {
            int x = foundationX + index * (cardWidth + pileGap);
            drawEmptySlot(context, x, topRowY);
            ArcadeTheme.drawSuitShape(context, x + cardWidth / 2, topRowY + cardHeight / 2,
                    Math.max(3, cardWidth / 6), index, 0x33FFFFFF);
            List<Card> pile = foundations.get(index);
            if (!pile.isEmpty()) {
                drawCard(context, x, topRowY, pile.get(pile.size() - 1), false);
            }
        }

        for (int pileIndex = 0; pileIndex < 7; pileIndex++) {
            int x = tableauX(pileIndex);
            List<Card> pile = tableau.get(pileIndex);
            if (pile.isEmpty()) {
                ArcadeTheme.rounded(context, x, tableauY, x + cardWidth, tableauY + cardHeight, 4,
                        0x22FFFFFF);
                ArcadeTheme.rounded(context, x + 1, tableauY + 1, x + cardWidth - 1,
                        tableauY + cardHeight - 1, 3, 0x0AFFFFFF);
            }
            int y = tableauY;
            for (int index = 0; index < pile.size(); index++) {
                Card card = pile.get(index);
                drawCard(context, x, y, card, false);
                y += card.faceUp ? faceUpOffset : faceDownOffset;
            }
        }
        if (selection != null) {
            int x;
            int y;
            if (selection.type == 0) {
                x = wasteX;
                y = topRowY;
            } else if (selection.type == 1) {
                x = tableauX(selection.pile);
                y = tableauY;
                List<Card> pile = tableau.get(selection.pile);
                for (int index = 0; index < selection.index && index < pile.size(); index++) {
                    y += pile.get(index).faceUp ? faceUpOffset : faceDownOffset;
                }
            } else {
                x = foundationX + selection.pile * (cardWidth + pileGap);
                y = topRowY;
            }
            ArcadeTheme.ring(context, x + cardWidth / 2, y + cardHeight / 2,
                    Math.max(cardWidth, cardHeight) / 2 + 2, 2, ArcadeTheme.GOLD);
        }
        if (won) {
            ArcadeTheme.banner(context, textRenderer, "VOCÊ VENCEU!",
                    "Paciência concluída em " + moves + " movimentos",
                    width / 2, (topRowY + tableauY + cardHeight) / 2, Math.min(width - 20, 300),
                    ArcadeTheme.SUCCESS);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawEmptySlot(DrawContext context, int x, int y) {
        ArcadeTheme.rounded(context, x, y, x + cardWidth, y + cardHeight, 4, 0x33FFFFFF);
        ArcadeTheme.rounded(context, x + 1, y + 1, x + cardWidth - 1, y + cardHeight - 1, 3, 0x660B1626);
    }

    private void drawCard(DrawContext context, int x, int y, Card card, boolean forceFaceDown) {
        if (!card.faceUp || forceFaceDown) {
            ArcadeTheme.playingCard(context, textRenderer, x, y, cardWidth, cardHeight, "", card.suit, true);
        } else {
            ArcadeTheme.playingCard(context, textRenderer, x, y, cardWidth, cardHeight,
                    RANKS[card.rank - 1], card.suit, false);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Card {
        private final int rank;
        private final int suit;
        private boolean faceUp;

        private Card(int rank, int suit) {
            this.rank = rank;
            this.suit = suit;
        }
    }

    private static final class Selection {
        private final int type;
        private final int pile;
        private final int index;

        private Selection(int type, int pile, int index) {
            this.type = type;
            this.pile = pile;
            this.index = index;
        }
    }
}
