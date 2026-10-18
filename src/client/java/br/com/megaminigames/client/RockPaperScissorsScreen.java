package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Pedra, papel e tesoura em uma série de melhor de cinco. */
public final class RockPaperScissorsScreen extends Screen {
    private static final String[] CHOICES = {"Pedra", "Papel", "Tesoura"};
    private int playerScore;
    private int computerScore;
    private int playerChoice = -1;
    private int computerChoice = -1;
    private String roundMessage = "Escolha sua jogada";
    private boolean finished;

    public RockPaperScissorsScreen() {
        super(Text.literal("Pedra, Papel e Tesoura"));
    }

    @Override
    protected void init() {
        int buttonWidth = 100;
        int start = (width - buttonWidth * 3 - 16) / 2;
        for (int choice = 0; choice < CHOICES.length; choice++) {
            final int selected = choice;
            addDrawableChild(ButtonWidget.builder(Text.literal(CHOICES[choice]), button -> play(selected))
                    .dimensions(start + choice * (buttonWidth + 8), height / 2 + 50, buttonWidth, 22)
                    .build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Nova série"), button -> newGame())
                .dimensions(width / 2 - 116, height - 34, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, height - 34, 112, 20).build());
    }

    private void newGame() {
        playerScore = 0;
        computerScore = 0;
        playerChoice = -1;
        computerChoice = -1;
        roundMessage = "Escolha sua jogada";
        finished = false;
    }

    private void play(int choice) {
        if (finished) return;
        playerChoice = choice;
        computerChoice = ThreadLocalRandom.current().nextInt(3);
        int difference = (playerChoice - computerChoice + 3) % 3;
        if (difference == 1) {
            playerScore++;
            roundMessage = "Você venceu esta rodada!";
        } else if (difference == 2) {
            computerScore++;
            roundMessage = "O computador venceu esta rodada.";
        } else {
            roundMessage = "Empate nesta rodada.";
        }
        if (playerScore == 3 || computerScore == 3) {
            finished = true;
            roundMessage = playerScore == 3 ? "Você venceu a série!" : "O computador venceu a série.";
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 105, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Você  " + playerScore + "  :  " + computerScore + "  Computador (primeiro a 3)"),
                width / 2, height / 2 - 72, 0xFFDDEEFF);
        String lastRound = playerChoice < 0 ? "" : "Você: " + CHOICES[playerChoice]
                + "   •   Computador: " + CHOICES[computerChoice];
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(lastRound), width / 2,
                height / 2 - 20, 0xFFFFD65A);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(roundMessage), width / 2,
                height / 2 + 22, finished && playerScore == 3 ? 0xFF72E78B : 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
