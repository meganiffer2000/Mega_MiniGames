package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Batida Perfeita: acerte as notas que descem nas quatro faixas no tempo certo. */
public final class RhythmScreen extends ArcadeScreen {
    private static final int LANES = 4;
    private static final int[] KEYS = {68, 70, 74, 75};
    private static final String[] KEY_LABELS = {"D", "F", "J", "K"};
    private static final int[] LANE_COLORS = {
            ArcadeTheme.TEAL, ArcadeTheme.LIME, ArcadeTheme.GOLD, ArcadeTheme.MAGENTA
    };

    private final List<Note> notes = new ArrayList<>();
    private final List<Effect> effects = new ArrayList<>();
    private final int[] laneFlash = new int[LANES];
    private int elapsed;
    private int lastNoteTick;
    private int score;
    private int combo;
    private int maxCombo;
    private int perfects;
    private int goods;
    private int misses;
    private boolean gameOver;
    private int ticks;
    private int laneWidth;
    private int lanesLeft;
    private int receptorY;
    private int boardTop;

    public RhythmScreen() {
        super("Batida Perfeita");
    }

    @Override
    protected void init() {
        laneWidth = Math.max(38, Math.min(64, (width - 48) / LANES));
        lanesLeft = (width - laneWidth * LANES) / 2;
        boardTop = 60;
        receptorY = height - 64;
        addFooterButtons(height - 26, this::newGame);
        newGame();
    }

    private void newGame() {
        notes.clear();
        effects.clear();
        elapsed = 0;
        score = 0;
        combo = 0;
        maxCombo = 0;
        perfects = 0;
        goods = 0;
        misses = 0;
        gameOver = false;
        int tick = 70;
        for (int index = 0; index < 110; index++) {
            int lane = ThreadLocalRandom.current().nextInt(LANES);
            notes.add(new Note(lane, tick));
            if (ThreadLocalRandom.current().nextInt(100) < 14) {
                notes.add(new Note((lane + 1 + ThreadLocalRandom.current().nextInt(LANES - 1)) % LANES,
                        tick + 4));
            }
            tick += 17 + ThreadLocalRandom.current().nextInt(12);
            if (index > 0 && index % 28 == 0) tick += 22;
        }
        lastNoteTick = tick;
    }

    @Override
    public void tick() {
        if (gameOver) return;
        ticks++;
        elapsed++;
        for (int lane = 0; lane < LANES; lane++) {
            if (laneFlash[lane] > 0) laneFlash[lane]--;
        }
        for (Note note : notes) {
            if (!note.judged && elapsed > note.tick + 13) {
                note.judged = true;
                note.missed = true;
                misses++;
                combo = 0;
            }
        }
        for (int index = effects.size() - 1; index >= 0; index--) {
            Effect effect = effects.get(index);
            effect.life--;
            effect.y -= 0.7F;
            if (effect.life <= 0) effects.remove(index);
        }
        if (elapsed > lastNoteTick + 34) {
            gameOver = true;
        }
    }

    private void hit(int lane) {
        if (gameOver) return;
        laneFlash[lane] = 8;
        Note best = null;
        int bestDelta = Integer.MAX_VALUE;
        for (Note note : notes) {
            if (note.judged || note.lane != lane) continue;
            int delta = Math.abs(note.tick - elapsed);
            if (delta < bestDelta && delta <= 13) {
                best = note;
                bestDelta = delta;
            }
        }
        float noteX = lanesLeft + lane * laneWidth + laneWidth / 2.0F;
        if (best == null) {
            effects.add(new Effect("", noteX, receptorY - 8, 0xFF9DB2CC, 12));
            combo = 0;
            return;
        }
        best.judged = true;
        if (bestDelta <= 5) {
            perfects++;
            score += 300 + Math.min(100, combo * 2);
            effects.add(new Effect("PERFEITO", noteX, receptorY - 12, ArcadeTheme.GOLD, 26));
        } else {
            goods++;
            score += 150 + Math.min(50, combo);
            effects.add(new Effect("BOM", noteX, receptorY - 12, ArcadeTheme.LIME, 22));
        }
        combo++;
        maxCombo = Math.max(maxCombo, combo);
        effects.add(new Effect(combo > 1 ? "x" + combo : "", width / 2.0F, boardTop + 26,
                ArcadeTheme.TEXT, 18));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            newGame();
            return true;
        }
        for (int lane = 0; lane < LANES; lane++) {
            if (keyCode == KEYS[lane]) {
                hit(lane);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 10, ArcadeTheme.MAGENTA);
        ArcadeTheme.status(context, textRenderer, Text.literal("Teclas D  F  J  K quando a nota cruzar o anel"),
                width / 2, 30, ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "PONTOS " + score, lanesLeft, 45, ArcadeTheme.MAGENTA);
        ArcadeTheme.pillRight(context, textRenderer, "COMBO x" + combo, lanesLeft + laneWidth * LANES, 45,
                ArcadeTheme.GOLD);
        float progress = Math.min(1.0F, elapsed / (float) Math.max(1, lastNoteTick + 34));
        ArcadeTheme.progress(context, lanesLeft, 58, laneWidth * LANES, 5, progress, ArcadeTheme.MAGENTA);

        int boardHeight = receptorY - boardTop + 30;
        ArcadeTheme.rounded(context, lanesLeft - 6, boardTop - 2, lanesLeft + laneWidth * LANES + 6,
                boardTop + boardHeight, 12, 0x55000000);
        for (int lane = 0; lane < LANES; lane++) {
            int x = lanesLeft + lane * laneWidth;
            context.fill(x, boardTop, x + laneWidth, receptorY + 12, 0x10FFFFFF);
            context.fill(x, boardTop, x + laneWidth, boardTop + 2, 0x22FFFFFF);
            context.fillGradient(x, boardTop, x + laneWidth, boardTop + 60,
                    ArcadeTheme.alpha(LANE_COLORS[lane], 30), 0x00000000);
            context.fill(x + laneWidth - 1, boardTop, x + laneWidth, receptorY + 12, 0x1AFFFFFF);
            if (laneFlash[lane] > 0) {
                context.fillGradient(x, receptorY - 70, x + laneWidth, receptorY + 12,
                        ArcadeTheme.alpha(LANE_COLORS[lane], 40 + laneFlash[lane] * 10), 0x00000000);
            }
        }
        for (Note note : notes) {
            if (note.judged) continue;
            int y = (int) (receptorY - (note.tick - elapsed) * 2.35F);
            if (y < boardTop - 20 || y > receptorY + 16) continue;
            int x = lanesLeft + note.lane * laneWidth + 6;
            int width = laneWidth - 12;
            int color = LANE_COLORS[note.lane];
            ArcadeTheme.glow(context, x + width / 2, y, width / 2 + 2, ArcadeTheme.alpha(color, 60));
            ArcadeTheme.rounded(context, x, y - 9, x + width, y + 9, 5, ArcadeTheme.darken(color, 40));
            ArcadeTheme.rounded(context, x, y - 8, x + width, y + 8, 5, color);
            context.fillGradient(x + 1, y - 7, x + width - 1, y, 0x66FFFFFF, 0x00FFFFFF);
        }
        for (int lane = 0; lane < LANES; lane++) {
            int centerX = lanesLeft + lane * laneWidth + laneWidth / 2;
            int color = LANE_COLORS[lane];
            if (laneFlash[lane] > 0) {
                ArcadeTheme.glow(context, centerX, receptorY, laneWidth / 2 + 6, 0x88FFFFFF);
            }
            ArcadeTheme.disc(context, centerX, receptorY, laneWidth / 3, 0xFF0A1422);
            ArcadeTheme.ring(context, centerX, receptorY, laneWidth / 3, 2,
                    laneFlash[lane] > 0 ? 0xFFFFFFFF : ArcadeTheme.alpha(color, 190));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(KEY_LABELS[lane]),
                    centerX, receptorY - 4, laneFlash[lane] > 0 ? 0xFFFFFFFF : color);
        }
        for (Effect effect : effects) {
            int alpha = Math.max(0, Math.min(255, effect.life * 10));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(effect.text),
                    (int) effect.x, (int) effect.y, ArcadeTheme.alpha(effect.color, alpha));
        }
        if (gameOver) {
            int judged = perfects + goods + misses;
            int accuracy = judged == 0 ? 0 : (perfects * 100 + goods * 60) / judged;
            String rank = accuracy >= 90 ? "S" : accuracy >= 75 ? "A" : accuracy >= 55 ? "B" : "C";
            ArcadeTheme.banner(context, textRenderer, "MÚSICA ENCERRADA",
                    "Precisão " + accuracy + "%  •  " + perfects + " perfeitas  •  nota " + rank,
                    width / 2, (boardTop + receptorY) / 2, Math.min(width - 20, 300),
                    accuracy >= 75 ? ArcadeTheme.SUCCESS : ArcadeTheme.GOLD);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Note {
        private final int lane;
        private final int tick;
        private boolean judged;
        private boolean missed;

        private Note(int lane, int tick) {
            this.lane = lane;
            this.tick = tick;
        }
    }

    private static final class Effect {
        private final String text;
        private final float x;
        private float y;
        private final int color;
        private int life;

        private Effect(String text, float x, float y, int color, int life) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.life = life;
        }
    }
}
