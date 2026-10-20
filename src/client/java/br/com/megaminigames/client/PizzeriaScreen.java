package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;

/**
 * Pizzaria Expressa — a primeira loja construída sobre {@link ShopSimScreen}.
 *
 * <p>12 ingredientes, 10 sabores (só a pizza de queijo começa liberada),
 * forno de tijolos e bancada de mármore. As receitas são descobertas
 * combinando ingredientes na bancada.</p>
 */
public final class PizzeriaScreen extends ShopSimScreen {

    public PizzeriaScreen() {
        super("Pizzaria Expressa");
    }

    @Override
    protected void configure() {
        ING = new String[] {
                "Massa", "Molho", "Queijo", "Calabresa", "Presunto", "Frango",
                "Catupiry", "Pepperoni", "Cogumelo", "Bacon", "Tomate", "Chocolate"
        };
        ING_PRICE = new int[] {6, 4, 8, 7, 6, 9, 8, 11, 5, 10, 3, 7};
        ING_COLOR = new int[] {
                0xFFE0C08A, 0xFFC4472F, 0xFFFFD966, 0xFFB23A2E, 0xFFE38C8C, 0xFFE8C89A,
                0xFFF4F0E0, 0xFFA8333A, 0xFFB8A48C, 0xFFC9705F, 0xFFD9453C, 0xFF6B4028
        };

        RECIPE_NAME = new String[] {
                "Queijo", "Margherita", "Calabresa", "Presunto", "Frango c/ Catupiry",
                "Pepperoni", "Vegetariana", "Bacon", "Suprema", "Chocolate"
        };
        RECIPE_ITEMS = new int[][] {
                {0, 1, 2},
                {0, 1, 2, 10},
                {0, 1, 2, 3},
                {0, 1, 2, 4},
                {0, 1, 2, 5, 6},
                {0, 1, 2, 7},
                {0, 1, 2, 10, 8},
                {0, 1, 2, 9, 3},
                {0, 1, 2, 3, 4, 9},
                {0, 11, 11}
        };
        RECIPE_PRICE = new int[] {42, 48, 56, 54, 78, 66, 58, 62, 92, 46};
        RECIPE_COLOR = new int[] {
                0xFFFFD966, 0xFFD8453C, 0xFFC4472F, 0xFFE38C8C, 0xFFF4F0E0,
                0xFFA8333A, 0xFF7FB55C, 0xFFC9705F, 0xFFB23A2E, 0xFF6B4028
        };

        EMPLOYEE_NAME = new String[] {
                "Ajudante Júnior", "Garçom Ágil", "Mestre de Salão",
                "Cozinheiro Aprendiz", "Chef Experiente", "Pizzaiolo Lendário"
        };
        EMPLOYEE_CHEF = new boolean[] {false, false, false, true, true, true};
        EMPLOYEE_COST = new int[] {180, 420, 900, 300, 700, 1500};
        EMPLOYEE_LEVEL = new int[] {2, 4, 6, 3, 5, 8};
        EMPLOYEE_SPEED = new int[] {360, 240, 140, 300, 180, 100};

        COLLECTIBLE = new String[] {
                "Botão de Camisa", "Pena de Galinha", "Trigo Dourado", "Esmeralda",
                "Flor de Cerejeira", "Pão de Forma", "Moeda Antiga", "Lápis de Cor"
        };
        COLLECTIBLE_COLOR = new int[] {
                0xFF8FA6C4, 0xFFF2F0E4, 0xFFE5B84B, 0xFF49C08A,
                0xFFF39AC0, 0xFFD9A15C, 0xFFC9A227, 0xFF5AA9E6
        };
        SPECIAL_CUSTOMER = new String[] {
                "Bibliotecário", "Ferreiro", "Fazendeiro", "Carteiro", "Clérigo", "Comerciante"
        };

        shopSign = "PIZZARIA";
        shopWord = "pizzaria";
        shopWordCap = "Pizzaria";
        product = "pizza";
        products = "pizzas";
        productPrefix = "Pizza de ";
        productReady = "pronta";
        station = "fornalha";
        stationCap = "Forno";
        stationLabel = "FORNO";
        stationVerb = "assar";
        bakeAction = "ASSAR";
        bakingWord = "Assando";
        readySuffix = "saiu do forno!";
        waiterRole = "garçom";
        chefRole = "chefe";
        hintStation = "[E] fornalha";
        dailyPromo = true;
        promoLabel = "PIZZA DO DIA";

        WORLD_WIDTH = 1240;
        SHOP_END = 700;
        STREET_END = 980;
        FLOOR_Y = 176;
        STATION_A_X = 150;
        STATION_B_X = 330;
        COUNTER_X = 520;
        SHOP_X = 1120;
        DOOR_X = 660;

        WALL_TOP = 0xFF9A5D46;
        WALL_TRIM = 0xFF7B4A38;
        WALL_WOOD = 0xFFB07A54;
        WALL_WOOD_LINE = 0xFF8A5A3E;
        WALL_PANEL = 0xFF7B4A38;
        FLOOR_A = 0xFFC39A6B;
        FLOOR_B = 0xFF9C734A;
        SHOP_WALL = 0xFF3E7D78;
        SHOP_WALL_TOP = 0xFF2E5E5A;
        SHOP_SHELF = 0xFF8A5A3E;
        SHOP_JAR_COLORS = new int[] {0xFFC4472F, 0xFFE5B84B, 0xFF6EA35C, 0xFFD98C5F};
        SIGN_BG = 0xFFE5B84B;
        SIGN_BORDER = 0xFF3E2A1C;
        SIGN_TEXT = 0xFF4A2C13;
        ACCENT_STATION = ArcadeTheme.CORAL;
        ACCENT_BENCH = ArcadeTheme.LIME;
    }

    @Override
    protected void drawStationA(DrawContext context, int x, int floorY) {
        int top = floorY - 56;
        ArcadeTheme.rounded(context, x - 24, top, x + 24, floorY, 4, 0xFF3A3F47);
        ArcadeTheme.rounded(context, x - 21, top + 3, x + 21, floorY - 3, 3, 0xFF5A616B);
        for (int row = 0; row < 4; row++) {
            context.fill(x - 20, top + 8 + row * 11, x + 20, top + 9 + row * 11, 0xFF454B54);
        }
        ArcadeTheme.rounded(context, x - 13, floorY - 34, x + 13, floorY - 4, 3, 0xFF1B1D21);
        context.fill(x - 4, top - 12, x + 4, top, 0xFF4A4F58);
        drawLabel(context, x, floorY + 2, stationLabel, ACCENT_STATION);
    }

    @Override
    protected void drawStationAFront(DrawContext context, int x, int floorY) {
        int glow = (ticks / 6) % 2 == 0 ? 0xFFFF8A2B : 0xFFFFC24D;
        context.fill(x - 10, floorY - 30, x + 10, floorY - 16, glow);
        context.fill(x - 7, floorY - 26, x + 7, floorY - 18, 0xFFFFF0B8);
        ArcadeTheme.glow(context, x, floorY - 23, 18, 0x66FF9A3C);
        context.fill(x - 13, floorY - 30, x + 13, floorY - 28, 0xFF5A616B);
        for (int index = 0; index < SAVE.baking.size() && index < ovenSlots(); index++) {
            ArcadeTheme.disc(context, x - 10 + index * 10, floorY - 23, 3, 0xFFFFD66B);
        }
    }

    @Override
    protected void drawStationB(DrawContext context, int x, int floorY) {
        int top = floorY - 34;
        context.fill(x - 30, top, x + 30, top + 5, 0xFFB07A54);
        context.fill(x - 30, top + 5, x + 30, top + 8, 0xFF8A5A3E);
        context.fill(x - 26, top + 8, x - 22, floorY, 0xFF7A4A32);
        context.fill(x + 22, top + 8, x + 26, floorY, 0xFF7A4A32);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                context.fill(x - 18 + column * 12, top - 10 + row * 4, x - 10 + column * 12,
                        top - 7 + row * 4, 0xFF6B4028);
            }
        }
        ArcadeTheme.disc(context, x + 16, top - 8, 6, 0xFFFFD966);
        ArcadeTheme.disc(context, x + 16, top - 8, 3, 0xFFC4472F);
        drawLabel(context, x, floorY + 2, "BANCADA", ACCENT_BENCH);
    }
}
