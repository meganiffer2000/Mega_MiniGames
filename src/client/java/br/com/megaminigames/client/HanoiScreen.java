package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Torre de Hanói: mova todos os discos para a última torre sem colocar um maior sobre um menor. */
public final class HanoiScreen extends Screen {
    private static final int DISKS = 5;
    private static final int[] DISK_COLORS = {
            ArcadeTheme.CORAL, ArcadeTheme.GOLD, ArcadeTheme.LIME, ArcadeTheme.SKY, ArcadeTheme.PURPLE
    };

    private final List<List<Integer>> pegs = new ArrayList<>();
    private int moves;
    private int selectedPeg = -1;
    private int messageTicks;
    private String message = "Escolha uma torre e depois o destino";
    private boolean gameOver;
    private int ticks;
    private int pegLeft;
    private int pegWidth;
    private int baseY;
    private int diskHeight;
    private int towerHeight;
    private static int bestMoves;

    public HanoiScreen() {
        super(Text.literal("Torre de Hanói"));
    }

    @Override
    protected void init() {
        pegWidth = Math.max(52, Math.min(96, (width - 48) / 3));
        pegLeft = (width - pegWidth * 3) / 2;
        int available = Math.max(90, height - 150);
        diskHeight = Math.max(8, Math.min(16, available / (DISKS + 2)));
        int total = DISKS * (diskHeight + 1);
        baseY = Math.min(height - 58, 84 + total + Math.max(10, (height - 84 - total - 74) / 2));
        towerHeight = total + 14;
        int buttonY = Math.min(height - 24, baseY + 18);
        int buttonWidth = Math.min(112, Math.max(74, (width - 40) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Recomeçar"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        newGame();
    }

    private void newGame() {
        pegs.clear();
        for (int index = 0; index < 3; index++) {
            pegs.add(new ArrayList<>());
        }
        for (int disk = DISKS; disk >= 1; disk--) {
            pegs.get(0).add(disk);
        }
        moves = 0;
        selectedPeg = -1;
        gameOver = false;
        messageTicks = 0;
        message = "Escolha uma torre e depois o destino";
    }

    private int topDisk(int peg) {
        List<Integer> stack = pegs.get(peg);
        return stack.isEmpty() ? DISKS + 1 : stack.get(stack.size() - 1);
    }

    @Override
    public void tick() {
        ticks++;
        if (messageTicks > 0) messageTicks--;
    }

    private void clickPeg(int peg) {
        if (gameOver || peg < 0 || peg > 2) return;
        if (selectedPeg == -1) {
            if (pegs.get(peg).isEmpty()) {
                message = "Essa torre está vazia";
                messageTicks = 40;
                return;
            }
            selectedPeg = peg;
            message = "Disco " + topDisk(peg) + " selecionado — escolha o destino";
            messageTicks = 0;
            return;
        }
        if (selectedPeg == peg) {
            selectedPeg = -1;
            message = "Seleção cancelada";
            messageTicks = 30;
            return;
        }
        int disk = topDisk(selectedPeg);
        if (disk < topDisk(peg)) {
            pegs.get(peg).add(pegs.get(selectedPeg).remove(pegs.get(selectedPeg).size() - 1));
            moves++;
            selectedPeg = -1;
            message = "Boa! Continue movendo";
            messageTicks = 24;
            if (pegs.get(2).size() == DISKS) {
                gameOver = true;
                message = "Você resolveu em " + moves + " movimentos!";
                if (bestMoves == 0 || moves < bestMoves) bestMoves = moves;
            }
        } else {
            message = "Um disco maior não pode ficar sobre outro menor";
            messageTicks = 50;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY > 60 && mouseY < baseY + 14) {
            for (int peg = 0; peg < 3; peg++) {
                int x1 = pegLeft + peg * pegWidth;
                if (mouseX >= x1 - 6 && mouseX <= x1 + pegWidth + 6) {
                    clickPeg(peg);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.MAGENTA);
        ArcadeTheme.status(context, textRenderer, Text.literal(message), width / 2, 31,
                gameOver ? ArcadeTheme.SUCCESS : messageTicks > 0 ? ArcadeTheme.GOLD : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "MOVIMENTOS " + moves, pegLeft, 47, ArcadeTheme.MAGENTA);
        ArcadeTheme.pillRight(context, textRenderer, "MÍNIMO " + ((1 << DISKS) - 1)
                + (bestMoves > 0 ? "  •  RECORDE " + bestMoves : ""), pegLeft + pegWidth * 3, 47, ArcadeTheme.GOLD);

        for (int peg = 0; peg < 3; peg++) {
            int centerX = pegLeft + peg * pegWidth + pegWidth / 2;
            int x1 = centerX - 3;
            ArcadeTheme.rounded(context, x1, baseY - towerHeight - 8, centerX + 3, baseY + 1, 3, 0xFF324B6B);
            ArcadeTheme.rounded(context, x1, baseY - towerHeight - 8, centerX + 1, baseY, 3, 0xFF5B7CA8);
            ArcadeTheme.rounded(context, centerX - pegWidth / 2 - 4, baseY, centerX + pegWidth / 2 + 4, baseY + 10,
                    5, 0xFF243B57);
            ArcadeTheme.rounded(context, centerX - pegWidth / 2 - 4, baseY + 1, centerX + pegWidth / 2 + 4, baseY + 4,
                    3, 0xFF45658F);
            if (selectedPeg == peg) {
                ArcadeTheme.ring(context, centerX, baseY - 4, pegWidth / 2, 2,
                        ArcadeTheme.alpha(ArcadeTheme.GOLD, 130 + (int) (Math.sin(ticks * 0.2) * 40)));
            }
            if (selectedPeg != -1 && selectedPeg != peg && topDisk(selectedPeg) < topDisk(peg)) {
                ArcadeTheme.disc(context, centerX, 40, 3, ArcadeTheme.SUCCESS);
            }
        }

        for (int peg = 0; peg < 3; peg++) {
            List<Integer> stack = pegs.get(peg);
            int centerX = pegLeft + peg * pegWidth + pegWidth / 2;
            for (int index = 0; index < stack.size(); index++) {
                int disk = stack.get(index);
                int width = diskWidth(disk);
                int y2 = baseY - index * (diskHeight + 1) - 2;
                int y1 = y2 - diskHeight;
                boolean lifted = selectedPeg == peg && index == stack.size() - 1;
                if (lifted) y1 -= 4;
                int color = DISK_COLORS[Math.min(DISK_COLORS.length - 1, disk - 1)];
                if (lifted) {
                    ArcadeTheme.glow(context, centerX, (y1 + y2) / 2, width / 2 + 4, ArcadeTheme.alpha(color, 90));
                }
                ArcadeTheme.rounded(context, centerX - width / 2, y1 + 2, centerX + width / 2, y2 + 2, 4,
                        0x55000000);
                ArcadeTheme.rounded(context, centerX - width / 2, y1, centerX + width / 2, y2, 4,
                        ArcadeTheme.darken(color, 55));
                ArcadeTheme.rounded(context, centerX - width / 2 + 1, y1 + 1, centerX + width / 2 - 1, y2 - 1, 3,
                        color);
                context.fillGradient(centerX - width / 2 + 2, y1 + 1, centerX + width / 2 - 2, y1 + diskHeight / 2,
                        ArcadeTheme.alpha(ArcadeTheme.lighten(color, 80), 120), 0x00FFFFFF);
                if (diskHeight >= 10 && width > 34) {
                    context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(disk)),
                            centerX, y1 + diskHeight / 2 - 4, 0xCC08111E);
                }
            }
        }

        if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, "TORRE COMPLETA!",
                    moves + " movimentos  •  mínimo " + ((1 << DISKS) - 1),
                    width / 2, 106, Math.min(width - 24, pegWidth * 3),
                    moves == (1 << DISKS) - 1 ? ArcadeTheme.GOLD : ArcadeTheme.SUCCESS);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private int diskWidth(int disk) {
        int max = pegWidth - 10;
        int min = Math.max(16, max / 2);
        return min + (max - min) * disk / DISKS;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
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
