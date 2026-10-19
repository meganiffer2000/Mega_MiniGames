package br.com.megaminigames.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Duelo de Heróis: batalha por turnos contra três criaturas, com ataque, defesa, poção e especial. */
public final class HeroDuelScreen extends ArcadeScreen {
    private static final String[] ENEMY_NAMES = {"Goblin", "Golem", "Dragão"};
    private static final int[] ENEMY_HP = {18, 30, 44};
    private static final int[] ENEMY_ATTACK = {4, 6, 8};
    private static final int[] ENEMY_COLORS = {ArcadeTheme.LIME, ArcadeTheme.SKY, ArcadeTheme.CORAL};

    private final Deque<String> log = new ArrayDeque<>();
    private final List<Popup> popups = new ArrayList<>();
    private ButtonWidget attackButton;
    private ButtonWidget defendButton;
    private ButtonWidget potionButton;
    private ButtonWidget specialButton;
    private int heroHp;
    private int heroMaxHp;
    private int heroAttack;
    private int potions;
    private int specials;
    private int wave;
    private int enemyHp;
    private int enemyMaxHp;
    private int enemyAttack;
    private boolean defending;
    private int state;
    private int delay;
    private int ticks;
    private int heroShake;
    private int enemyShake;
    private boolean victory;
    private boolean defeat;
    private int sceneTop;
    private int sceneBottom;

    public HeroDuelScreen() {
        super("Duelo de Heróis");
    }

    @Override
    protected void init() {
        sceneTop = 60;
        sceneBottom = height - 62;
        int buttonWidth = Math.max(58, (width - 50) / 4);
        int startX = (width - buttonWidth * 4 - 18) / 2;
        int buttonY = height - 50;
        attackButton = addDrawableChild(ArcadeTheme.primaryButton(startX, buttonY, buttonWidth, 20,
                Text.literal("Atacar"), ArcadeTheme.CORAL, this::attack));
        defendButton = addDrawableChild(ArcadeTheme.button(startX + buttonWidth + 6, buttonY, buttonWidth, 20,
                Text.literal("Defender"), this::defend));
        potionButton = addDrawableChild(ArcadeTheme.button(startX + (buttonWidth + 6) * 2, buttonY,
                buttonWidth, 20, Text.literal("Poção"), this::usePotion));
        specialButton = addDrawableChild(ArcadeTheme.button(startX + (buttonWidth + 6) * 3, buttonY,
                buttonWidth, 20, Text.literal("Especial"), this::useSpecial));
        addFooterButtons(height - 26, this::newGame);
        newGame();
    }

    private void newGame() {
        heroMaxHp = 40;
        heroHp = heroMaxHp;
        heroAttack = 6;
        potions = 2;
        specials = 2;
        wave = 0;
        defending = false;
        state = 0;
        delay = 0;
        ticks = 0;
        victory = false;
        defeat = false;
        popups.clear();
        log.clear();
        log.add("Um " + ENEMY_NAMES[0] + " bloqueia o caminho!");
        spawnEnemy();
        updateButtons();
    }

    private void spawnEnemy() {
        enemyMaxHp = ENEMY_HP[wave];
        enemyHp = enemyMaxHp;
        enemyAttack = ENEMY_ATTACK[wave];
    }

    private void log(String message) {
        log.addLast(message);
        while (log.size() > 3) log.removeFirst();
    }

    private void popup(String text, int x, int y, int color) {
        Popup popup = new Popup();
        popup.text = text;
        popup.x = x;
        popup.y = y;
        popup.color = color;
        popup.life = 40;
        popups.add(popup);
    }

    private void attack() {
        if (state != 0) return;
        int damage = heroAttack + ThreadLocalRandom.current().nextInt(-1, 3);
        enemyHp -= damage;
        enemyShake = 8;
        popup("-" + damage, enemyCenterX(), enemyCenterY() - 16, ArcadeTheme.CORAL);
        log("Você golpeia: " + damage + " de dano.");
        afterHeroAction(20);
    }

    private void defend() {
        if (state != 0) return;
        defending = true;
        log("Você ergue o escudo.");
        afterHeroAction(14);
    }

    private void usePotion() {
        if (state != 0 || potions <= 0) return;
        potions--;
        int healed = Math.min(heroMaxHp - heroHp, 14);
        heroHp += healed;
        popup("+" + healed, heroCenterX(), heroCenterY() - 20, ArcadeTheme.SUCCESS);
        log("Poção restaura " + healed + " de vida.");
        afterHeroAction(16);
    }

    private void useSpecial() {
        if (state != 0 || specials <= 0) return;
        specials--;
        int damage = heroAttack * 2 + 4;
        enemyHp -= damage;
        enemyShake = 14;
        popup("-" + damage + "!", enemyCenterX(), enemyCenterY() - 16, ArcadeTheme.GOLD);
        log("Golpe especial: " + damage + " de dano!");
        afterHeroAction(22);
    }

    private void afterHeroAction(int wait) {
        state = 1;
        delay = wait;
        updateButtons();
    }

    private void enemyTurn() {
        int damage = Math.max(1, enemyAttack - (defending ? 3 : 0)
                + ThreadLocalRandom.current().nextInt(-1, 2));
        defending = false;
        heroHp -= damage;
        heroShake = 8;
        popup("-" + damage, heroCenterX(), heroCenterY() - 20, ArcadeTheme.DANGER);
        log(ENEMY_NAMES[wave] + " contra-ataca: " + damage + " de dano.");
        if (heroHp <= 0) {
            heroHp = 0;
            defeat = true;
            state = 5;
            log("Você caiu em batalha...");
        } else {
            state = 0;
        }
        updateButtons();
    }

    private void nextWave() {
        wave++;
        if (wave >= ENEMY_NAMES.length) {
            victory = true;
            state = 4;
            log("Todos os inimigos foram derrotados!");
            updateButtons();
            return;
        }
        heroMaxHp += 4;
        heroHp = Math.min(heroMaxHp, heroHp + 12);
        heroAttack += 1;
        spawnEnemy();
        state = 0;
        log("Um " + ENEMY_NAMES[wave] + " surge na arena!");
        updateButtons();
    }

    private void updateButtons() {
        boolean ready = state == 0;
        if (attackButton != null) attackButton.active = ready;
        if (defendButton != null) defendButton.active = ready;
        if (potionButton != null) {
            potionButton.active = ready && potions > 0;
            potionButton.setMessage(Text.literal("Poção " + potions));
        }
        if (specialButton != null) {
            specialButton.active = ready && specials > 0;
            specialButton.setMessage(Text.literal("Especial " + specials));
        }
    }

    @Override
    public void tick() {
        ticks++;
        if (heroShake > 0) heroShake--;
        if (enemyShake > 0) enemyShake--;
        for (int index = popups.size() - 1; index >= 0; index--) {
            Popup popup = popups.get(index);
            popup.life--;
            popup.y -= 0.6F;
            if (popup.life <= 0) popups.remove(index);
        }
        if (state == 1) {
            delay--;
            if (delay <= 0) {
                if (enemyHp <= 0) {
                    enemyHp = 0;
                    nextWave();
                } else {
                    enemyTurn();
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            newGame();
            return true;
        }
        if (state == 0) {
            switch (keyCode) {
                case 49 -> attack();
                case 50 -> defend();
                case 51 -> usePotion();
                case 52 -> useSpecial();
                default -> {
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.CORAL);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(state == 0 ? "Escolha sua ação  •  teclas 1–4"
                        : state == 1 ? "Resolvendo o turno..."
                        : victory ? "A arena é sua!" : "Fim da batalha"),
                width / 2, 32, victory ? ArcadeTheme.SUCCESS : defeat ? ArcadeTheme.DANGER : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "ONDA " + (wave + 1) + "/" + ENEMY_NAMES.length, 12, 47,
                ArcadeTheme.CORAL);
        ArcadeTheme.pillRight(context, textRenderer, "HERÓI " + heroHp + "/" + heroMaxHp, width - 12, 47,
                ArcadeTheme.TEAL);

        ArcadeTheme.rounded(context, 9, sceneTop, width - 9, sceneBottom, 12, 0x55000000);
        ArcadeTheme.rounded(context, 8, sceneTop - 1, width - 8, sceneBottom + 1, 12, 0x33FF8A65);
        context.fillGradient(10, sceneTop + 1, width - 10, sceneBottom - 1, 0xFF182C42, 0xFF0A1524);
        for (int index = 0; index < 7; index++) {
            int y = sceneBottom - 10 - index * 8;
            context.fill(16, y, width - 16, y + 1, 0x08FFFFFF);
        }
        ArcadeTheme.glow(context, width / 2, sceneBottom - 10, width / 2, 0x22FF8A65);

        drawHero(context, heroCenterX(), heroCenterY());
        drawEnemy(context, enemyCenterX(), enemyCenterY());
        drawHealthBars(context);

        int logY = sceneBottom - 44;
        int index = 0;
        for (String line : log) {
            int color = index == log.size() - 1 ? ArcadeTheme.TEXT : ArcadeTheme.DIM;
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(textRenderer.trimToWidth(line, width - 32)), width / 2, logY + index * 11, color);
            index++;
        }
        for (Popup popup : popups) {
            int alpha = Math.max(0, Math.min(255, popup.life * 8));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(popup.text),
                    (int) popup.x, (int) popup.y, ArcadeTheme.alpha(popup.color, alpha));
        }
        if (victory || defeat) {
            ArcadeTheme.banner(context, textRenderer, victory ? "VOCÊ VENCEU!" : "DERROTA",
                    victory ? "Três criaturas derrotadas" : "Tente outra estratégia com R",
                    width / 2, (sceneTop + sceneBottom) / 2, Math.min(width - 20, 300),
                    victory ? ArcadeTheme.GOLD : ArcadeTheme.DANGER);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawHealthBars(DrawContext context) {
        int barWidth = Math.min(132, (width - 60) / 2);
        int barY = sceneTop + 8;
        ArcadeTheme.rounded(context, 16, barY, 16 + barWidth, barY + 9, 4, 0xAA08111E);
        float heroRatio = Math.max(0.0F, heroHp / (float) Math.max(1, heroMaxHp));
        ArcadeTheme.rounded(context, 16, barY, 16 + Math.max(2, (int) (barWidth * heroRatio)), barY + 9, 4,
                heroRatio > 0.5F ? ArcadeTheme.SUCCESS : heroRatio > 0.25F ? ArcadeTheme.GOLD : ArcadeTheme.DANGER);
        context.drawTextWithShadow(textRenderer, Text.literal("HERÓI"), 16, barY + 12, ArcadeTheme.MUTED);
        int enemyBarLeft = width - 16 - barWidth;
        ArcadeTheme.rounded(context, enemyBarLeft, barY, enemyBarLeft + barWidth, barY + 9, 4, 0xAA08111E);
        float enemyRatio = Math.max(0.0F, enemyHp / (float) Math.max(1, enemyMaxHp));
        ArcadeTheme.rounded(context, enemyBarLeft, barY, enemyBarLeft + Math.max(2, (int) (barWidth * enemyRatio)),
                barY + 9, 4, ArcadeTheme.CORAL);
        String enemyLabel = ENEMY_NAMES[wave] + "  " + enemyHp + "/" + enemyMaxHp;
        context.drawTextWithShadow(textRenderer, Text.literal(enemyLabel), enemyBarLeft, barY + 12,
                ArcadeTheme.MUTED);
    }

    private int heroCenterX() {
        return width / 4;
    }

    private int heroCenterY() {
        return sceneTop + (sceneBottom - sceneTop) / 2 + 6;
    }

    private int enemyCenterX() {
        return width * 3 / 4;
    }

    private int enemyCenterY() {
        return sceneTop + (sceneBottom - sceneTop) / 2 + 4;
    }

    private void drawHero(DrawContext context, int centerX, int centerY) {
        int bob = (int) (Math.sin(ticks * 0.12) * 2);
        int offset = heroShake > 0 ? (heroShake / 2 % 2 == 0 ? -2 : 2) : 0;
        int x = centerX + offset;
        int y = centerY + bob;
        ArcadeTheme.glow(context, x, y, 34, 0x336CC9F5);
        ArcadeTheme.rounded(context, x - 13, y - 4, x + 13, y + 26, 6, 0xFF2B4E77);
        ArcadeTheme.rounded(context, x - 11, y - 2, x + 11, y + 24, 5, 0xFF3E6FA3);
        ArcadeTheme.disc(context, x, y - 14, 11, 0xFF1B3A5C);
        ArcadeTheme.disc(context, x, y - 14, 9, 0xFFE8C9A0);
        context.fill(x - 10, y - 22, x + 10, y - 17, 0xFFB8C7DA);
        context.fill(x - 2, y - 27, x + 4, y - 18, ArcadeTheme.CORAL);
        ArcadeTheme.disc(context, x - 3, y - 14, 1, 0xFF22334A);
        ArcadeTheme.disc(context, x + 3, y - 14, 1, 0xFF22334A);
        context.fill(x - 3, y - 8, x + 3, y - 7, 0xFF7A4A3A);
        if (defending) {
            ArcadeTheme.rounded(context, x + 8, y + 2, x + 22, y + 24, 5, 0xFF9FB8D8);
            ArcadeTheme.rounded(context, x + 10, y + 4, x + 20, y + 22, 4, 0xFFD6E6FF);
            ArcadeTheme.drawCheck(context, x + 12, y + 10, 0xFF3E6FA3);
        } else {
            context.fill(x + 12, y - 4, x + 15, y + 20, 0xFFD9E6F5);
            context.fill(x + 11, y - 12, x + 16, y - 3, 0xFFB8C7DA);
            ArcadeTheme.disc(context, x + 13, y + 21, 3, ArcadeTheme.GOLD);
        }
    }

    private void drawEnemy(DrawContext context, int centerX, int centerY) {
        int bob = (int) (Math.sin(ticks * 0.15 + 1.0) * 2);
        int offset = enemyShake > 0 ? (enemyShake / 2 % 2 == 0 ? -3 : 3) : 0;
        int x = centerX + offset;
        int y = centerY + bob;
        int color = ENEMY_COLORS[wave];
        ArcadeTheme.glow(context, x, y, 36, ArcadeTheme.alpha(color, 60));
        if (wave == 0) {
            ArcadeTheme.disc(context, x, y, 18, 0xFF2B5A2B);
            ArcadeTheme.disc(context, x, y - 1, 16, color);
            ArcadeTheme.polygon(context, new int[]{x - 16, x - 26, x - 12},
                    new int[]{y - 8, y - 22, y - 20}, color);
            ArcadeTheme.polygon(context, new int[]{x + 16, x + 26, x + 12},
                    new int[]{y - 8, y - 22, y - 20}, color);
        } else if (wave == 1) {
            ArcadeTheme.rounded(context, x - 20, y - 22, x + 20, y + 22, 7, 0xFF3A5566);
            ArcadeTheme.rounded(context, x - 17, y - 20, x + 17, y + 19, 6, 0xFF6A8CA3);
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    ArcadeTheme.disc(context, x - 10 + column * 10, y - 8 + row * 10, 2, 0xFF4A6A80);
                }
            }
        } else {
            ArcadeTheme.polygon(context, new int[]{x - 18, x - 44, x - 20},
                    new int[]{y - 6, y - 30, y - 22}, 0xFF8A2E3E);
            ArcadeTheme.polygon(context, new int[]{x + 18, x + 44, x + 20},
                    new int[]{y - 6, y - 30, y - 22}, 0xFF8A2E3E);
            ArcadeTheme.rounded(context, x - 20, y - 18, x + 20, y + 22, 9, 0xFF5A1B2A);
            ArcadeTheme.rounded(context, x - 17, y - 16, x + 17, y + 19, 8, color);
            ArcadeTheme.polygon(context, new int[]{x - 12, x - 8, x - 4}, new int[]{y - 20, y - 30, y - 20},
                    0xFFFFD66B);
            ArcadeTheme.polygon(context, new int[]{x + 12, x + 8, x + 4}, new int[]{y - 20, y - 30, y - 20},
                    0xFFFFD66B);
            ArcadeTheme.polygon(context, new int[]{x - 8, x + 8, x + 4},
                    new int[]{y + 20, y + 20, y + 30}, ArcadeTheme.CORAL);
        }
        int eyeY = wave == 1 ? y - 10 : y - 4;
        ArcadeTheme.disc(context, x - 6, eyeY, 3, 0xFFFFFFFF);
        ArcadeTheme.disc(context, x + 6, eyeY, 3, 0xFFFFFFFF);
        ArcadeTheme.disc(context, x - 6, eyeY, 1, 0xFF1A0B0B);
        ArcadeTheme.disc(context, x + 6, eyeY, 1, 0xFF1A0B0B);
        if (wave == 0) {
            context.fill(x - 6, y + 8, x + 6, y + 9, 0xFF203A20);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Popup {
        private String text;
        private float x;
        private float y;
        private int color;
        private int life;
    }
}
