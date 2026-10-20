package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Meu Bichinho — simulação de cuidados em pixel art.
 *
 * <p>Um bichinho vive num quarto e tem quatro necessidades: fome, higiene,
 * energia e alegria. O jogador cuida dele com as teclas 1–4 ou clicando nos
 * móveis e, em troca, recebe moedas para comprar melhorias. Não existe game
 * over: quando uma necessidade chega a zero o bichinho fica triste, para de
 * render moedas e precisa de carinho para se recuperar.</p>
 */
public final class PetScreen extends ArcadeScreen {
    private static final String[] NEED = {"FOME", "HIGIENE", "ENERGIA", "ALEGRIA"};
    private static final int[] NEED_COLOR = {0xFFFF9A5C, 0xFF6CC9F5, 0xFFB9F26B, 0xFFF39AC0};
    private static final String[] UPGRADE = {"Cama macia", "Brinquedo", "Banheira"};
    private static final int[] UPGRADE_COST = {40, 70, 110};
    private static final String[] FOOD = {"maçã", "peixe", "bolo"};

    /* paleta do quarto */
    private static final int TAN = 0xFFDFB47E;
    private static final int TAN_DARK = 0xFFB98A57;
    private static final int CREAM = 0xFFF6E3BF;
    private static final int CREAM_LIGHT = 0xFFFFF2D6;
    private static final int INK = 0xFF2B2218;
    private static final int PINK = 0xFFF39AC0;

    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final int[] need = {70, 70, 70, 70};
    private final int[] upgrade = new int[3];
    private final List<float[]> hearts = new ArrayList<>();
    private final List<float[]> crumbs = new ArrayList<>();

    private int coins;
    private int totalCoins;
    private int best;
    private int ticks;
    private int petX = 150;
    private int petTargetX = 150;
    private int petFrame;
    private int facing = 1;
    private int actionTicks;
    private int messageTicks;
    private int petLevel = 1;
    private int xp;
    private int shopIndex;
    private boolean playing;
    private int playPhase;
    private int playScore;
    private float ballX;
    private float ballY;
    private float ballVX;
    private float ballVY;
    private int night;
    private boolean sleeping;
    private boolean happy;
    private boolean started;
    private int petCooldown;
    private String message = "";

    public PetScreen() {
        super("Meu Bichinho");
    }

    @Override
    protected void init() {
        addFooterButtons(height - 26, this::restart);
        if (!started) {
            started = true;
            restart();
        }
        petX = Math.max(60, Math.min(width - 80, petX));
        petTargetX = Math.max(60, Math.min(width - 80, petTargetX));
    }

    private void restart() {
        for (int index = 0; index < need.length; index++) need[index] = 70;
        for (int index = 0; index < upgrade.length; index++) upgrade[index] = 0;
        coins = 0;
        totalCoins = 0;
        xp = 0;
        petLevel = 1;
        ticks = 0;
        night = 0;
        sleeping = false;
        playing = false;
        actionTicks = 0;
        shopIndex = 0;
        message = "Cuide do bichinho: 1 alimenta, 2 brinca, 3 limpa e 4 dorme. Clique nele para fazer carinho!";
        messageTicks = 220;
        hearts.clear();
        crumbs.clear();
    }

    /* ------------------------------------------------------------------ */
    /* Simulação                                                          */
    /* ------------------------------------------------------------------ */

    @Override
    public void tick() {
        ticks++;
        if (messageTicks > 0) messageTicks--;
        if (actionTicks > 0) actionTicks--;
        if (petCooldown > 0) petCooldown--;
        night = (ticks / 760) % 2;
        updateParticles();

        if (ticks % 40 == 0) {
            int decay = sleeping ? 1 : 1;
            need[0] = Math.max(0, need[0] - decay);
            need[1] = Math.max(0, need[1] - decay);
            need[3] = Math.max(0, need[3] - decay);
            if (!sleeping) need[2] = Math.max(0, need[2] - Math.max(1, decay - upgrade[0] / 3));
        }
        if (sleeping) {
            if (ticks % 3 == 0) need[2] = Math.min(100, need[2] + 1);
            if (need[2] >= 100) {
                sleeping = false;
                say("O bichinho acordou descansado!");
            }
        }
        if (ticks % 260 == 0) hint();
        happy = need[0] > 20 && need[1] > 20 && need[2] > 12 && need[3] > 20;

        if (happy && ticks % 30 == 0) {
            int gain = 1 + petLevel / 2 + (upgrade[0] + upgrade[1] + upgrade[2]) / 3;
            coins += gain;
            totalCoins += gain;
            best = Math.max(best, totalCoins);
            xp += 2;
            if (xp >= 40 + petLevel * 20) {
                xp = 0;
                petLevel++;
                say("Seu bichinho subiu para o nível " + petLevel + "!");
            }
        }

        if (!sleeping && ticks % 52 == 0) {
            petTargetX = 58 + random.nextInt(Math.max(40, width - 136));
        }
        if (Math.abs(petTargetX - petX) > 2 && !sleeping && actionTicks == 0) {
            facing = petTargetX > petX ? 1 : -1;
            petX += facing * 2;
            petFrame++;
        }
        if (playing) tickPlay();
    }

    /** Dica periódica apontando a necessidade mais crítica. */
    private void hint() {
        int worst = 0;
        for (int index = 1; index < need.length; index++) {
            if (need[index] < need[worst]) worst = index;
        }
        if (need[worst] >= 30) return;
        switch (worst) {
            case 0 -> say("O bichinho está com fome! Aperte 1 para alimentar.");
            case 1 -> say("Ele está sujinho! Aperte 3 para dar um banho.");
            case 2 -> say("Ele está com sono... Aperte 4 para ele dormir.");
            default -> say("Ele quer atenção! Aperte 2 ou faça carinho nele.");
        }
    }

    /** Carinho: rende alegria e uma moeda, sem gastar nada. */
    private void pet() {
        if (sleeping) {
            say("Shhh... não acorde o bichinho.");
            return;
        }
        if (petCooldown > 0) return;
        petCooldown = 20;
        need[3] = Math.min(100, need[3] + 4);
        coins += 1;
        totalCoins += 1;
        say("Carinho! O bichinho abanou o rabo. +1 moeda.");
        hearts.add(new float[] {petX + (random.nextFloat() - 0.5F) * 10, floorY() - 50, 0.25F, 1.0F});
    }

    private void tickPlay() {
        playPhase++;
        ballX += ballVX;
        ballY += ballVY;
        ballVY += 0.11F;
        int floor = floorY() - 6;
        if (ballY > floor) {
            ballY = floor;
            ballVY = -Math.abs(ballVY) * 0.72F;
            ballVX *= 0.94F;
            if (Math.abs(ballVY) < 0.6F) ballVY = -1.6F;
        }
        if (ballY < 40 || ballX < 24 || ballX > width - 24) {
            ballVX = Math.abs(ballVX) * (ballX < 24 ? 1 : ballX > width - 24 ? -1 : 1);
            ballX = Math.max(24, Math.min(width - 24, ballX));
            ballVY = Math.abs(ballVY);
        }
        need[3] = Math.min(100, need[3] + 1);
        need[2] = Math.max(0, need[2] - 1);
        if (playPhase > 200) {
            playing = false;
            int reward = 6 + playScore * 2 + upgrade[1] * 6;
            coins += reward;
            totalCoins += reward;
            say("Fim da brincadeira: +" + reward + " moedas.");
            playPhase = 0;
        }
    }

    private void updateParticles() {
        for (int index = hearts.size() - 1; index >= 0; index--) {
            float[] heart = hearts.get(index);
            heart[0] += heart[2];
            heart[1] -= 0.5F;
            heart[3] -= 0.02F;
            if (heart[3] <= 0) hearts.remove(index);
        }
        for (int index = crumbs.size() - 1; index >= 0; index--) {
            float[] crumb = crumbs.get(index);
            crumb[0] += crumb[2];
            crumb[1] += crumb[3];
            crumb[3] += 0.06F;
            crumb[4] -= 1;
            if (crumb[4] <= 0) crumbs.remove(index);
        }
    }

    private int floorY() {
        return height - 62;
    }

    private int rightPanelX() {
        return width - 124;
    }

    /* ------------------------------------------------------------------ */
    /* Ações                                                              */
    /* ------------------------------------------------------------------ */

    private void say(String text) {
        message = text;
        messageTicks = 180;
    }

    private void feed() {
        if (sleeping) {
            say("Shhh... o bichinho está dormindo.");
            return;
        }
        if (coins < 4) {
            say("Sem moedas para a comida (custa 4).");
            return;
        }
        coins -= 4;
        need[0] = Math.min(100, need[0] + 26);
        need[1] = Math.max(0, need[1] - 4);
        actionTicks = 50;
        say("O bichinho comeu " + FOOD[random.nextInt(FOOD.length)] + " e ficou satisfeito!");
        for (int index = 0; index < 7; index++) {
            crumbs.add(new float[] {petX + 6, floorY() - 12, (random.nextFloat() - 0.5F) * 1.4F,
                    -random.nextFloat() * 1.2F, 18 + random.nextInt(10), 0xFFD9A15C});
        }
        hearts.add(new float[] {petX, floorY() - 44, 0.3F, 1.0F});
    }

    private void play() {
        if (sleeping) {
            say("Shhh... o bichinho está dormindo.");
            return;
        }
        if (playing) {
            say("A brincadeira já está rolando! Clique na bola.");
            return;
        }
        if (need[2] < 12) {
            say("Ele está cansado demais. Deixe dormir (tecla 4).");
            return;
        }
        playing = true;
        playPhase = 0;
        playScore = 0;
        ballX = petX + 22;
        ballY = floorY() - 52;
        ballVX = 1.4F * facing;
        ballVY = -1.8F;
        say("Clique na bola para quicar! A alegria está subindo.");
    }

    private void clean() {
        if (sleeping) {
            say("Shhh... o bichinho está dormindo.");
            return;
        }
        int gain = 18 + upgrade[2] * 8;
        need[1] = Math.min(100, need[1] + gain);
        need[3] = Math.min(100, need[3] + 4);
        actionTicks = 46;
        say("Banho tomado! Higiene +" + gain + ".");
        for (int index = 0; index < 16; index++) {
            crumbs.add(new float[] {petX + (random.nextFloat() - 0.5F) * 22,
                    floorY() - 34 + random.nextFloat() * 16, (random.nextFloat() - 0.5F) * 0.8F,
                    -random.nextFloat() * 0.9F, 26, 0xFFBFE6F7});
        }
    }

    private void sleep() {
        if (sleeping) {
            sleeping = false;
            say("O bichinho acordou.");
            return;
        }
        sleeping = true;
        actionTicks = 40;
        say("Boa noite! A energia sobe e as necessidades caem menos.");
    }

    private void buyUpgrade() {
        if (upgrade[shopIndex] >= 3) {
            say(UPGRADE[shopIndex] + " já está no nível máximo.");
            return;
        }
        int cost = UPGRADE_COST[shopIndex] + upgrade[shopIndex] * 60;
        if (coins < cost) {
            say("Faltam " + (cost - coins) + " moedas para a " + UPGRADE[shopIndex].toLowerCase(java.util.Locale.ROOT) + ".");
            return;
        }
        coins -= cost;
        upgrade[shopIndex]++;
        say(UPGRADE[shopIndex] + " nível " + upgrade[shopIndex] + "! O bichinho adorou.");
    }

    /* ------------------------------------------------------------------ */
    /* Entrada                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 49 -> {
                feed();
                return true;
            }
            case 50 -> {
                play();
                return true;
            }
            case 51 -> {
                clean();
                return true;
            }
            case 52 -> {
                sleep();
                return true;
            }
            case 262 -> {
                shopIndex = (shopIndex + 1) % UPGRADE.length;
                return true;
            }
            case 263 -> {
                shopIndex = (shopIndex + UPGRADE.length - 1) % UPGRADE.length;
                return true;
            }
            case 257, 32, 335 -> {
                buyUpgrade();
                return true;
            }
            case 82 -> {
                restart();
                return true;
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (playing && Math.abs(mx - ballX) <= 9 && Math.abs(my - ballY) <= 9) {
            ballVY = -2.2F;
            ballVX = (ballX < petX ? 1 : -1) * (1.2F + random.nextFloat());
            playScore++;
            need[3] = Math.min(100, need[3] + 3);
            hearts.add(new float[] {ballX, ballY - 10, 0.2F, 0.8F});
            return true;
        }
        if (Math.abs(mx - petX) < 30 && my >= floorY() - 64 && my <= floorY() && !sleeping) {
            pet();
            return true;
        }
        if (my >= height - 54 && my <= height - 34) {
            int cell = (width - 16) / 4;
            for (int index = 0; index < 4; index++) {
                int x = 8 + index * cell;
                if (mx >= x && mx <= x + cell - 4) {
                    switch (index) {
                        case 0 -> feed();
                        case 1 -> play();
                        case 2 -> clean();
                        default -> sleep();
                    }
                    return true;
                }
            }
        }
        if (mx >= rightPanelX() && mx <= width - 6) {
            for (int index = 0; index < UPGRADE.length; index++) {
                int y = 62 + index * 26;
                if (my >= y && my <= y + 23) {
                    shopIndex = index;
                    buyUpgrade();
                    return true;
                }
            }
        }
        return false;
    }

    /* ------------------------------------------------------------------ */
    /* Render                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawRoom(context);
        drawWindow(context);
        drawShelf(context);
        drawFurniture(context);
        drawPet(context);
        drawParticles(context);
        if (night == 1) {
            context.fill(0, 22, width, height, 0x33081124);
        }
        drawHud(context);
        super.render(context, mouseX, mouseY, delta);
    }

    /* ------------------------------- cenário --------------------------- */

    private void drawRoom(DrawContext context) {
        int wallBottom = floorY();
        boolean dark = night == 1;
        int wall = dark ? 0xFF3F3A5C : 0xFFF0E3CC;
        int stripe = dark ? 0x22312B4A : 0x147A5A3A;
        int base = dark ? 0xFF2E2A46 : 0xFFC9A87C;
        int floor = dark ? 0xFF6B4E36 : 0xFFBA8C58;
        int floorLine = dark ? 0xFF533B27 : 0xFF94683C;

        context.fill(0, 0, width, wallBottom, wall);
        for (int x = 0; x < width; x += 26) {
            context.fill(x, 22, Math.min(width, x + 13), wallBottom, stripe);
        }
        context.fill(0, wallBottom - 10, width, wallBottom, base);
        context.fill(0, wallBottom - 11, width, wallBottom - 10, 0x33000000);
        context.fill(0, wallBottom, width, height, floor);
        for (int y = wallBottom + 9; y < height; y += 9) {
            context.fill(0, y, width, y + 1, floorLine);
        }
        for (int x = 46; x < width; x += 52) {
            context.fill(x, wallBottom, x + 1, height, 0x5594683C);
        }
    }

    private void drawWindow(DrawContext context) {
        int wx = 126;
        int wy = 44;
        int available = rightPanelX() - 16 - wx;
        if (available < 88) return;
        int ww = Math.min(96, available);
        int wh = 76;

        context.fill(wx - 4, wy - 4, wx + ww + 4, wy + wh + 4, 0xFF7A5A3C);
        context.fill(wx - 4, wy - 4, wx + ww + 4, wy - 2, 0xFF9C7A54);
        context.fillGradient(wx, wy, wx + ww, wy + wh,
                night == 1 ? 0xFF0E1730 : 0xFF8FCBEE, night == 1 ? 0xFF2C3D6B : 0xFFD9F0FA);

        if (night == 1) {
            for (int index = 0; index < 14; index++) {
                int sx = wx + 4 + Math.floorMod(index * 37, Math.max(1, ww - 8));
                int sy = wy + 4 + Math.floorMod(index * 23, Math.max(1, wh / 2));
                int twinkle = (int) (140 + 100 * Math.sin((ticks + index * 9) / 9.0));
                context.fill(sx, sy, sx + 1, sy + 1, (twinkle << 24) | 0xFFF3C4);
            }
            ArcadeTheme.disc(context, wx + ww - 22, wy + 20, 8, 0xFFF2E6C0);
            ArcadeTheme.disc(context, wx + ww - 25, wy + 18, 8,
                    night == 1 ? 0xFF101B36 : 0xFF0E1730);
        } else {
            ArcadeTheme.disc(context, wx + ww - 22, wy + 20, 8, 0xFFFFE59A);
            context.fill(wx + 10, wy + 16, wx + 34, wy + 22, 0xCCFFFFFF);
            context.fill(wx + 16, wy + 12, wx + 30, wy + 16, 0xCCFFFFFF);
            context.fill(wx + 40, wy + 34, wx + 60, wy + 39, 0xAAFFFFFF);
        }
        // caixilho
        context.fill(wx + ww / 2 - 1, wy, wx + ww / 2 + 1, wy + wh, 0xFF7A5A3C);
        context.fill(wx, wy + wh / 2 - 1, wx + ww, wy + wh / 2 + 1, 0xFF7A5A3C);
        // peitoril
        context.fill(wx - 6, wy + wh + 4, wx + ww + 6, wy + wh + 7, 0xFF9C7A54);
        // cortinas
        if (rightPanelX() - (wx + ww) > 26) {
            context.fill(wx - 10, wy - 8, wx - 3, wy + wh + 4, 0xFFE9A4B0);
            context.fill(wx + ww + 3, wy - 8, wx + ww + 10, wy + wh + 4, 0xFFE9A4B0);
            context.fill(wx - 10, wy - 8, wx + ww + 10, wy - 5, 0xFF8A5A3E);
            for (int y = wy; y < wy + wh; y += 12) {
                context.fill(wx - 8, y, wx - 5, y + 4, 0x33FFFFFF);
                context.fill(wx + ww + 5, y, wx + ww + 8, y + 4, 0x33FFFFFF);
            }
        }
    }

    private void drawShelf(DrawContext context) {
        int left = 126 + 102;
        int right = rightPanelX() - 8;
        if (right - left < 96) return;
        int shelfY = 96;
        int x1 = left + 12;
        int x2 = right - 12;
        if (x2 - x1 < 84) return;
        context.fill(x1, shelfY, x2, shelfY + 4, 0xFF9C7A54);
        context.fill(x1, shelfY + 4, x2, shelfY + 6, 0x55000000);
        // vaso de planta
        int potX = x1 + 10;
        context.fill(potX, shelfY - 9, potX + 12, shelfY, 0xFFC97B54);
        context.fill(potX - 1, shelfY - 10, potX + 13, shelfY - 8, 0xFFA85F3C);
        context.fill(potX + 5, shelfY - 18, potX + 7, shelfY - 9, 0xFF4E7A3A);
        ArcadeTheme.disc(context, potX + 2, shelfY - 20, 5, 0xFF5E9446);
        ArcadeTheme.disc(context, potX + 10, shelfY - 22, 5, 0xFF4E7A3A);
        ArcadeTheme.disc(context, potX + 6, shelfY - 26, 4, 0xFF5E9446);
        // porta-retrato
        int frameX = x2 - 32;
        context.fill(frameX, shelfY - 22, frameX + 22, shelfY, 0xFF8A5A3E);
        context.fill(frameX + 2, shelfY - 20, frameX + 20, shelfY - 2, 0xFFEAD9BE);
        ArcadeTheme.disc(context, frameX + 11, shelfY - 13, 4, TAN);
        context.fill(frameX + 9, shelfY - 14, frameX + 10, shelfY - 13, INK);
        context.fill(frameX + 13, shelfY - 14, frameX + 14, shelfY - 13, INK);
    }

    private void drawFurniture(DrawContext context) {
        int floor = floorY();
        // tapete
        context.fill(66, floor + 8, Math.min(width - 70, 300), floor + 22, 0x33E9A4B0);
        context.fill(72, floor + 10, Math.min(width - 76, 294), floor + 20, 0x22FFFFFF);

        // cama
        context.fill(16, floor - 26, 20, floor - 6, 0xFF8A5A3E);
        context.fill(74, floor - 26, 78, floor - 6, 0xFF8A5A3E);
        context.fill(16, floor - 20, 78, floor - 4, 0xFFB07A54);
        context.fill(20, floor - 26, 56, floor - 18, 0xFFF6F0E4);
        context.fill(50, floor - 27, 76, floor - 18, 0xFFE45A6B);
        for (int level = 0; level < upgrade[0]; level++) {
            context.fill(22 + level * 9, floor - 30, 30 + level * 9, floor - 26, PINK);
        }

        // banheira (comprada)
        if (upgrade[2] > 0) {
            ArcadeTheme.rounded(context, 96, floor - 18, 138, floor - 2, 5, 0xFFE8F4F8);
            context.fill(100, floor - 15, 134, floor - 8, 0xFF9FD8F0);
            context.fill(100, floor - 15, 134, floor - 14, 0x88FFFFFF);
        }

        // comedouro
        int bowlX = width - 96;
        ArcadeTheme.rounded(context, bowlX, floor - 9, bowlX + 28, floor, 4, 0xFF9DB2CC);
        context.fill(bowlX + 3, floor - 7, bowlX + 25, floor - 4, 0xFFD9A15C);
        context.fill(bowlX, floor - 10, bowlX + 28, floor - 8, 0xFFC2D3E6);

        // bola
        if (!playing) {
            ArcadeTheme.disc(context, width - 44, floor - 7, 7, 0xFFE45A6B);
            ArcadeTheme.disc(context, width - 46, floor - 9, 2, 0xAAFFFFFF);
        }
    }

    /* -------------------------------- bichinho ------------------------- */

    private void drawPet(DrawContext context) {
        int floor = floorY();
        int x = petX;
        int facingSign = facing;
        boolean sad = !happy;

        // sombra
        context.fill(x - 22, floor - 4, x + 22, floor, 0x33000000);
        context.fill(x - 15, floor - 5, x + 15, floor - 2, 0x33000000);

        if (sleeping) {
            ArcadeTheme.rounded(context, x - 26, floor - 20, x + 16, floor - 2, 10, TAN);
            ArcadeTheme.disc(context, x + 10, floor - 16, 13, CREAM);
            context.fill(x + 12, floor - 19, x + 17, floor - 18, INK);
            context.fill(x + 5, floor - 10, x + 15, floor - 9, 0x88F39AC0);
            int bob = (ticks / 12) % 4;
            context.drawTextWithShadow(textRenderer, Text.literal("z"), x + 18, floor - 36 - bob, 0xFF9FD8F0);
            context.drawTextWithShadow(textRenderer, Text.literal("z"), x + 25, floor - 46 - bob, 0xCC9FD8F0);
            return;
        }

        int bob = (int) Math.round(Math.sin(ticks / 16.0));
        int y = floor + bob;
        boolean walking = Math.abs(petTargetX - petX) > 2 && actionTicks == 0;
        int step = walking ? ((petFrame / 5) % 2 == 0 ? 1 : 0) : 0;
        int hx = x + facingSign * 9;

        int body = sad ? 0xFFCDB29E : TAN;
        int light = sad ? 0xFFDCC9B8 : CREAM;

        // rabo enrolado para fora do corpo
        int dir = facingSign;
        int tx = x - dir * 18;
        context.fill(Math.min(tx, tx + 5), y - 20, Math.max(tx, tx + 5), y - 16, body);
        context.fill(Math.min(tx - dir * 2, tx + 5 - dir * 2), y - 25,
                Math.max(tx - dir * 2, tx + 5 - dir * 2), y - 20, body);
        context.fill(Math.min(tx - dir * 4, tx + 3 - dir * 4), y - 30,
                Math.max(tx - dir * 4, tx + 3 - dir * 4), y - 25, TAN_DARK);

        // corpo
        ArcadeTheme.rounded(context, x - 20, y - 31, x + 15, y - 3, 11, body);
        ArcadeTheme.rounded(context, x - 12, y - 21, x + 10, y - 4, 7, light);

        // patas
        context.fill(x - 16, y - 6 + step, x - 7, y - 1, TAN_DARK);
        context.fill(x + 3, y - 6 + (1 - step), x + 12, y - 1, TAN_DARK);
        context.fill(x - 15, y - 4 + step, x - 12, y - 2 + step, body);
        context.fill(x + 4, y - 4 + (1 - step), x + 7, y - 2 + (1 - step), body);

        // cabeça
        ArcadeTheme.disc(context, hx, y - 39, 15, light);
        // orelhas
        ArcadeTheme.disc(context, hx - 10, y - 53, 6, body);
        ArcadeTheme.disc(context, hx + 9, y - 54, 6, body);
        ArcadeTheme.disc(context, hx - 10, y - 53, 3, PINK);
        ArcadeTheme.disc(context, hx + 9, y - 54, 3, PINK);

        // focinho
        ArcadeTheme.disc(context, hx + facingSign * 6, y - 32, 7, CREAM_LIGHT);
        int nx = hx + facingSign * 7;
        context.fill(nx - 2, y - 35, nx + 2, y - 33, INK);
        context.fill(nx - 1, y - 31, nx + 1, y - 30, INK);
        context.fill(nx + facingSign * 2, y - 31, nx + facingSign * 3, y - 30, INK);

        // olhos (pisca de vez em quando)
        boolean blink = (ticks % 140) < 6;
        if (blink) {
            context.fill(hx - 8, y - 42, hx - 3, y - 41, INK);
            context.fill(hx + 2, y - 42, hx + 7, y - 41, INK);
        } else {
            context.fill(hx - 8, y - 44, hx - 4, y - 39, INK);
            context.fill(hx + 2, y - 44, hx + 6, y - 39, INK);
            context.fill(hx - 8, y - 44, hx - 7, y - 43, 0xAAFFFFFF);
            context.fill(hx + 2, y - 44, hx + 3, y - 43, 0xAAFFFFFF);
        }

        // bochechas
        context.fill(hx - 13, y - 35, hx - 8, y - 32, 0x55F39AC0);
        context.fill(hx + 7, y - 35, hx + 12, y - 32, 0x55F39AC0);

        // coleira
        context.fill(x - 8, y - 26, x + 9, y - 23, 0xFFE45A6B);
        context.fill(x - 2, y - 26, x + 1, y - 23, 0xFFFFD66B);

        // sujeirinha
        if (need[1] < 45) {
            context.fill(x - 16, y - 18, x - 13, y - 15, 0x99614B2E);
            context.fill(x + 5, y - 12, x + 8, y - 9, 0x99614B2E);
            context.fill(x - 6, y - 8, x - 3, y - 5, 0x99614B2E);
        }

        // fome / sono
        if (need[0] < 30 && (ticks / 20) % 2 == 0) {
            context.drawTextWithShadow(textRenderer, Text.literal("!"), x + 20, y - 60, ArcadeTheme.CORAL);
        }
        if (need[2] < 25 && (ticks / 24) % 2 == 0) {
            context.drawTextWithShadow(textRenderer, Text.literal("z"), x + 24, y - 56, 0xFF9FD8F0);
        }
        if (playing && Math.abs(ballX - x) < 44) {
            context.drawTextWithShadow(textRenderer, Text.literal("!"), x + 6, y - 64, ArcadeTheme.GOLD);
        }
    }

    private void drawParticles(DrawContext context) {
        for (float[] heart : hearts) {
            int x = (int) heart[0];
            int y = (int) heart[1];
            int alpha = Math.max(0, (int) (heart[3] * 210));
            int color = (alpha << 24) | 0xF39AC0;
            context.fill(x - 2, y, x, y + 2, color);
            context.fill(x + 2, y, x + 4, y + 2, color);
            context.fill(x - 3, y + 2, x + 5, y + 4, color);
            context.fill(x - 1, y + 4, x + 3, y + 6, color);
        }
        for (float[] crumb : crumbs) {
            int x = (int) crumb[0];
            int y = (int) crumb[1];
            context.fill(x, y, x + 2, y + 2, (int) crumb[5]);
        }
        if (playing) {
            ArcadeTheme.disc(context, (int) ballX, (int) ballY, 7, 0xFFE45A6B);
            ArcadeTheme.disc(context, (int) ballX - 3, (int) ballY - 3, 2, 0xAAFFFFFF);
        }
    }

    /* ---------------------------------- HUD ---------------------------- */

    private void drawHud(DrawContext context) {
        context.fill(0, 0, width, 22, 0xE00A1220);
        context.fill(0, 21, width, 22, 0x55000000);
        int x = 4;
        x += ArcadeTheme.pill(context, textRenderer, "R$ " + coins, x, 4, ArcadeTheme.GOLD) + 6;
        x += ArcadeTheme.pill(context, textRenderer, "Nv " + petLevel, x, 4, ArcadeTheme.LIME) + 6;
        int heartsCount = 0;
        float mood = (need[0] + need[1] + need[2] + need[3]) / 400.0F;
        heartsCount = mood > 0.75F ? 3 : mood > 0.45F ? 2 : mood > 0.2F ? 1 : 0;
        for (int index = 0; index < 3; index++) {
            int cx = x + 5 + index * 9;
            int color = index < heartsCount ? PINK : 0xFF3A4A60;
            context.fill(cx - 2, 7, cx, 9, color);
            context.fill(cx + 2, 7, cx + 4, 9, color);
            context.fill(cx - 3, 9, cx + 5, 11, color);
            context.fill(cx - 1, 11, cx + 3, 13, color);
        }
        String period = night == 1 ? "NOITE" : "DIA";
        ArcadeTheme.pillRight(context, textRenderer, period, width - 4, 4,
                night == 1 ? ArcadeTheme.PURPLE : ArcadeTheme.SKY);

        drawNeedPanel(context);
        drawUpgradePanel(context);
        drawActions(context);

        if (messageTicks > 0 && !message.isEmpty()) {
            String text = textRenderer.trimToWidth(message, width - 24);
            int textWidth = textRenderer.getWidth(text);
            int centerY = 25;
            ArcadeTheme.rounded(context, width / 2 - textWidth / 2 - 8, centerY,
                    width / 2 + textWidth / 2 + 8, centerY + 15, 6, 0xE00A1220);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(text), width / 2,
                    centerY + 4, ArcadeTheme.TEXT);
        }
        if (playing) {
            String hint = "CLIQUE NA BOLA (" + playScore + ")";
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(hint), width / 2,
                    floorY() - 44, ArcadeTheme.GOLD);
        }
    }

    private void drawNeedPanel(DrawContext context) {
        ArcadeTheme.panel(context, 6, 44, 118, 146);
        context.drawTextWithShadow(textRenderer, Text.literal("CUIDADOS"), 12, 48, ArcadeTheme.MUTED);
        for (int index = 0; index < NEED.length; index++) {
            int rowY = 62 + index * 20;
            context.drawTextWithShadow(textRenderer, Text.literal(NEED[index]), 12, rowY,
                    ArcadeTheme.TEXT);
            String value = String.valueOf(need[index]);
            context.drawTextWithShadow(textRenderer, Text.literal(value),
                    112 - textRenderer.getWidth(value), rowY, ArcadeTheme.DIM);
            ArcadeTheme.progress(context, 12, rowY + 11, 100, 5, need[index] / 100.0F,
                    need[index] < 25 ? ArcadeTheme.DANGER : NEED_COLOR[index]);
        }
    }

    private void drawUpgradePanel(DrawContext context) {
        int px = rightPanelX();
        ArcadeTheme.panel(context, px, 44, width - 6, 146);
        context.drawTextWithShadow(textRenderer, Text.literal("MELHORIAS"), px + 6, 49, ArcadeTheme.MUTED);
        for (int index = 0; index < UPGRADE.length; index++) {
            int y = 62 + index * 26;
            boolean selected = index == shopIndex;
            int cost = UPGRADE_COST[index] + upgrade[index] * 60;
            ArcadeTheme.rounded(context, px + 4, y, width - 10, y + 23, 5,
                    selected ? 0xFF2A3F5C : 0xCC152438);
            if (selected) {
                context.fill(px + 4, y, px + 6, y + 23, ArcadeTheme.GOLD);
            }
            context.drawTextWithShadow(textRenderer,
                    Text.literal(textRenderer.trimToWidth(UPGRADE[index], width - px - 34)),
                    px + 9, y + 3, ArcadeTheme.TEXT);
            String detail = upgrade[index] >= 3 ? "MÁXIMO" : "R$ " + cost + "   Nv " + upgrade[index];
            context.drawTextWithShadow(textRenderer, Text.literal(detail), px + 9, y + 13,
                    upgrade[index] >= 3 ? ArcadeTheme.LIME : ArcadeTheme.GOLD);
        }
    }

    private void drawActions(DrawContext context) {
        String[] labels = {"COMER", "BRINCAR", "LIMPAR", "DORMIR"};
        String[] keys = {"1", "2", "3", "4"};
        int[] colors = {0xFFFF9A5C, PINK, 0xFF6CC9F5, 0xFFB9F26B};
        int top = height - 54;
        int cell = (width - 16) / 4;
        for (int index = 0; index < 4; index++) {
            int x = 8 + index * cell;
            int x2 = x + cell - 4;
            ArcadeTheme.rounded(context, x, top, x2, top + 20, 5, 0xE00F1E30);
            context.fill(x + 3, top + 4, x + 6, top + 16, colors[index]);
            ArcadeTheme.rounded(context, x + 9, top + 5, x + 20, top + 15, 3, 0x33000000);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(keys[index]), x + 14,
                    top + 7, colors[index]);
            context.drawTextWithShadow(textRenderer,
                    Text.literal(textRenderer.trimToWidth(labels[index], x2 - x - 26)),
                    x + 23, top + 6, ArcadeTheme.TEXT);
        }
    }
}
