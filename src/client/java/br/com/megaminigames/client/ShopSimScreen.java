package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Motor compartilhado das lojas de gestão em pixel art.
 *
 * <p>Steve atende villagers atrás do balcão, compra ingredientes na loja do
 * outro lado da rua, monta o produto na bancada de trabalho (receitas
 * escondidas) e leva tudo para a estação de preparo. As três abas do topo
 * abrem melhorias, coleção e a equipe (funcionários e clientes). Não existe
 * game over.</p>
 *
 * <p>Cada loja (pizzaria, padaria, sorveteria) informa seus ingredientes,
 * receitas, funcionários, textos e paleta em {@link #configure()}, além de
 * desenhar as duas estações e os detalhes da parede. O cenário é desenhado em
 * blocos de pixels gerados aos poucos e guardados em cache: ao voltar para uma
 * área já visitada os pixels são recarregados do cache.</p>
 */
public abstract class ShopSimScreen extends ArcadeScreen {

    /* ------------------------------------------------------------------ */
    /* Conteúdo do jogo                                                   */
    /* ------------------------------------------------------------------ */

    /* Conteúdo da loja (definido pela subclasse) */
    protected String[] ING;
    protected int[] ING_PRICE;
    protected int[] ING_COLOR;

    protected String[] RECIPE_NAME;
    protected int[][] RECIPE_ITEMS;
    protected int[] RECIPE_PRICE;
    protected int[] RECIPE_COLOR;

    protected String[] EMPLOYEE_NAME;
    protected boolean[] EMPLOYEE_CHEF;
    protected int[] EMPLOYEE_COST;
    protected int[] EMPLOYEE_LEVEL;
    protected int[] EMPLOYEE_SPEED;

    protected String[] COLLECTIBLE;
    protected int[] COLLECTIBLE_COLOR;
    protected String[] SPECIAL_CUSTOMER;

    /* Textos e paleta da loja */
    protected String shopSign = "LOJA";
    protected String shopSignShop = "LOJA";
    protected String shopWord = "loja";
    protected String shopWordCap = "Loja";
    protected String product = "produto";
    protected String products = "produtos";
    protected String productPrefix = "";
    protected String productReady = "pronto";
    protected String station = "estação";
    protected String stationCap = "Estação";
    protected String stationLabel = "ESTAÇÃO";
    protected String stationVerb = "preparar";
    protected String bakeAction = "ASSAR";
    protected String bakingWord = "Preparando";
    protected String readySuffix = "saiu do forno!";
    protected String waiterRole = "garçom";
    protected String chefRole = "chefe";
    protected String hintBench = "[E] bancada de trabalho";
    protected String hintStation = "[E] estação de preparo";
    protected String hintShop = "[E] comprar ingredientes";

    /** > 0 faz o produto esfriar: depois disso o cliente paga {@link #stalePercent}. */
    protected int freshnessTicks = 0;
    protected int stalePercent = 70;
    protected String freshWord = "quentinho";
    protected String staleWord = "frio";

    /** Liga a onda de calor: mais clientes, menos paciência e preço melhor. */
    protected boolean heatWaves = false;
    protected int heatEvery = 1500;
    protected int heatDuration = 600;

    /** Promoção do dia: um sabor conhecido paga 50% a mais. */
    protected boolean dailyPromo = false;
    protected String promoLabel = "PROMO";

    /* Paleta do interior da loja */
    protected int WALL_TOP = 0xFF9A5D46;
    protected int WALL_TRIM = 0xFF7B4A38;
    protected int WALL_WOOD = 0xFFB07A54;
    protected int WALL_WOOD_LINE = 0xFF8A5A3E;
    protected int WALL_PANEL = 0xFF8A5A3E;
    protected int FLOOR_A = 0xFFC39A6B;
    protected int FLOOR_B = 0xFF9C734A;
    protected int SHOP_WALL = 0xFF3E7D78;
    protected int SHOP_WALL_TOP = 0xFF2E5E5A;
    protected int SHOP_SHELF = 0xFF8A5A3E;
    protected int[] SHOP_JAR_COLORS = {0xFFC4472F, 0xFFE5B84B, 0xFF6EA35C, 0xFFD98C5F};
    protected int SIGN_BG = 0xFFE5B84B;
    protected int SIGN_BORDER = 0xFF3E2A1C;
    protected int SIGN_TEXT = 0xFF4A2C13;
    protected int ACCENT_STATION = ArcadeTheme.CORAL;
    protected int ACCENT_BENCH = ArcadeTheme.LIME;

    /** Preenche os campos acima. Chamado pelo construtor, antes de criar o save. */
    protected abstract void configure();

    /** Detalhes extras da parede da loja; devolve 0 quando não pinta nada. */
    protected int decorColor(int wx, int wy, int delta) {
        return 0;
    }

    /** Desenha a estação de preparo (forno, freezer...). */
    protected abstract void drawStationA(DrawContext context, int x, int floorY);

    /** Desenha a bancada de trabalho (forno de barro, batedeira...). */
    protected abstract void drawStationB(DrawContext context, int x, int floorY);

    /** Frente da estação, desenhada depois do Steve (ele fica atrás do móvel). */
    protected void drawStationAFront(DrawContext context, int x, int floorY) {
    }

    /* ------------------------------------------------------------------ */
    /* Mundo                                                              */
    /* ------------------------------------------------------------------ */

    protected int WORLD_WIDTH = 1240;
    protected int SHOP_END = 700;
    protected int STREET_END = 980;
    protected int FLOOR_Y = 176;
    protected int PIXEL = 4;
    protected int CHUNK_SIZE = 32;
    protected int CHUNK_PIXELS = CHUNK_SIZE / PIXEL;

    protected int STATION_A_X = 150;
    protected int STATION_B_X = 330;
    protected int COUNTER_X = 520;
    protected int SHOP_X = 1120;
    protected int DOOR_X = 660;

    private final Map<Long, int[]> CHUNKS = new HashMap<>();
    private final Set<Long> LOADED = new HashSet<>();

    /* ------------------------------------------------------------------ */
    /* Estado persistente entre visitas                                   */
    /* ------------------------------------------------------------------ */

    protected static final class Save {
        private final boolean[] known;
        private final int[] stock;
        private final int[] pantry;
        private final boolean[] collection;
        private final boolean[] hired;
        private final int[] employeeTimer;
        private final int[] freshTimer;
        private int money = 70;
        private int xp;
        private int level = 1;
        protected final List<int[]> baking = new ArrayList<>();
        private int ovenLevel;
        private int pizzeriaLevel;
        private int steveLevel;
        private float steveX = 470F;
        private float cameraX = 300F;
        private final int[] bench = {-1, -1, -1, -1, -1, -1, -1, -1, -1};
        private final List<Customer> customers = new ArrayList<>();
        private int spawnTimer = 200;
        private int served;
        private int earned;
        private int brokeTimer = 600;
        private boolean heatActive;
        private int heatTimer;
        private int heatCooldown = 420;
        private int promoRecipe = -1;
        private int promoTimer = 1500;
        private final Set<Long> savedChunks = new HashSet<>();
        private boolean started;

        private Save(int recipes, int ingredients, int collectibles, int employees) {
            known = new boolean[recipes];
            stock = new int[recipes];
            freshTimer = new int[recipes];
            pantry = new int[ingredients];
            collection = new boolean[collectibles];
            hired = new boolean[employees];
            employeeTimer = new int[employees];
        }
    }

    /** Um save por loja: o progresso continua quando o jogador volta do hub. */
    private static final Map<Class<?>, Save> SAVES = new HashMap<>();

    private static final class Customer {
        private int recipe;
        private float x;
        private float targetX;
        private int patience;
        private int maxPatience;
        private int state;
        private int robe;
        private boolean special;
        private String name;
        private int bob;
    }

    private enum Overlay { NONE, CRAFT, SHOP, UPGRADES, COLLECTION, TEAM }

    /* ------------------------------------------------------------------ */
    /* Campos da tela                                                     */
    /* ------------------------------------------------------------------ */

    protected final Save SAVE;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private Overlay overlay = Overlay.NONE;
    private float cameraX;
    private int originY;
    private float steveX;
    private int steveFrame;
    private boolean leftHeld;
    private boolean rightHeld;
    private boolean walking;
    private String toast = "";
    private int toastTicks;
    protected int ticks;
    private int hoveredMenu = -1;
    private int hoveredCustomer = -1;
    private boolean nearStation;
    private boolean nearTable;
    private boolean nearShop;

    protected ShopSimScreen(String title) {
        super(title);
        configure();
        Save save = SAVES.get(getClass());
        if (save == null) {
            save = new Save(RECIPE_NAME.length, ING.length, COLLECTIBLE.length, EMPLOYEE_NAME.length);
            SAVES.put(getClass(), save);
        }
        SAVE = save;
    }

    @Override
    protected void init() {
        originY = height - 74 - FLOOR_Y;
        if (!SAVE.started) {
            SAVE.started = true;
            SAVE.known[0] = true;
            for (int item : RECIPE_ITEMS[0]) SAVE.pantry[item] += 2;
            toast = "Compre ingredientes, monte na bancada e leve ao " + station + "!";
            toastTicks = 220;
        }
        if (!SAVE.savedChunks.isEmpty()) {
            CHUNKS.clear();
            LOADED.clear();
        }
        cameraX = SAVE.cameraX;
        steveX = SAVE.steveX;
        clampCamera();
    }

    /* ------------------------------------------------------------------ */
    /* Utilidades                                                         */
    /* ------------------------------------------------------------------ */

    protected int floorScreen() {
        return FLOOR_Y + originY;
    }

    private float maxCamera() {
        return Math.max(0, WORLD_WIDTH - width);
    }

    private void clampCamera() {
        cameraX = Math.max(0, Math.min(maxCamera(), cameraX));
        SAVE.cameraX = cameraX;
    }

    private int menuSlotWidth() {
        int count = Math.max(1, knownCount());
        return Math.max(26, Math.min(52, (width - 96) / count));
    }

    private int knownCount() {
        int total = 0;
        for (boolean value : SAVE.known) if (value) total++;
        return total;
    }

    private int knownIndexAt(int slot) {
        int seen = 0;
        for (int index = 0; index < SAVE.known.length; index++) {
            if (!SAVE.known[index]) continue;
            if (seen == slot) return index;
            seen++;
        }
        return -1;
    }

    private int totalStock() {
        int total = 0;
        for (int value : SAVE.stock) total += value;
        return total;
    }

    protected int ovenSlots() {
        return 1 + SAVE.ovenLevel;
    }

    private int bakeTicks() {
        return Math.max(70, 150 - SAVE.ovenLevel * 12);
    }

    private int levelNeed(int level) {
        return 100 + (level - 1) * 130;
    }

    private void addXp(int amount) {
        SAVE.xp += amount;
        while (SAVE.xp >= levelNeed(SAVE.level)) {
            SAVE.xp -= levelNeed(SAVE.level);
            SAVE.level++;
            say("Nível " + SAVE.level + "! Novas melhorias e funcionários liberados.");
        }
    }

    private void say(String message) {
        toast = message;
        toastTicks = 180;
    }

    private int ovenCost() {
        return 260 + SAVE.ovenLevel * 220;
    }

    private int pizzeriaCost() {
        return 300 + SAVE.pizzeriaLevel * 260;
    }

    private int steveCost() {
        return 160 + SAVE.steveLevel * 130;
    }

    private float steveSpeed() {
        return 2.1F + SAVE.steveLevel * 0.36F;
    }

    private int sellPrice(int recipe) {
        int price = RECIPE_PRICE[recipe] * (10 + SAVE.pizzeriaLevel) / 10;
        if (isPromo(recipe)) price = price * 3 / 2;
        if (SAVE.heatActive) price = price * 5 / 4;
        if (!isFresh(recipe)) price = price * stalePercent / 100;
        return price;
    }

    private int patienceTicks() {
        int patience = 600 + SAVE.pizzeriaLevel * 170;
        if (SAVE.heatActive) patience = patience * 3 / 5;
        return patience;
    }

    private int maxCustomers() {
        return 3 + SAVE.pizzeriaLevel / 2;
    }

    private boolean hasIngredients(int recipe) {
        int[] need = new int[ING.length];
        for (int item : RECIPE_ITEMS[recipe]) need[item]++;
        for (int index = 0; index < ING.length; index++) {
            if (SAVE.pantry[index] < need[index]) return false;
        }
        return true;
    }

    private void consumeIngredients(int recipe) {
        for (int item : RECIPE_ITEMS[recipe]) SAVE.pantry[item]--;
    }

    /* ------------------------------------------------------------------ */
    /* Loop                                                               */
    /* ------------------------------------------------------------------ */

    @Override
    public void tick() {
        ticks++;
        if (toastTicks > 0) toastTicks--;
        updateSteve();
        updateCustomers();
        updateOvens();
        updateEmployees();
        updateSpecialEvents();
        checkFreshStart();
        updateNearby();
        updateCamera();
        SAVE.steveX = steveX;
    }

    private void updateSteve() {
        float speed = steveSpeed();
        float direction = (leftHeld ? -1 : 0) + (rightHeld ? 1 : 0);
        walking = direction != 0;
        if (walking) {
            float next = steveX + direction * speed;
            if (!blocked(next)) {
                steveX = next;
            } else if (!blocked(steveX + direction * 1.2F)) {
                steveX += direction * 1.2F;
            }
            steveX = Math.max(24, Math.min(WORLD_WIDTH - 24, steveX));
            steveFrame++;
        }
    }

    /** O Steve passa por trás dos móveis, mas não por cima deles. */
    private boolean blocked(float x) {
        return Math.abs(x - STATION_A_X) < 34 || Math.abs(x - STATION_B_X) < 40;
    }

    private void updateCamera() {
        float bias = Math.abs(steveX - COUNTER_X) < 140 ? 42 : 0;
        float target = steveX - width / 2.0F + bias;
        cameraX += (target - cameraX) * 0.16F;
        clampCamera();
    }

    private void checkFreshStart() {
        if (SAVE.money >= 12) return;
        int pantry = 0;
        for (int value : SAVE.pantry) pantry += value;
        if (pantry > 0 || totalStock() > 0 || !SAVE.baking.isEmpty()) return;
        if (--SAVE.brokeTimer > 0) return;
        SAVE.brokeTimer = 900;
        for (int item : RECIPE_ITEMS[0]) SAVE.pantry[item]++;
        say("Um cliente generoso deixou ingredientes na despensa. Recomece com "
                + productPrefix + RECIPE_NAME[0] + "!");
    }

    private void updateNearby() {
        nearStation = Math.abs(steveX - STATION_A_X) < 64;
        nearTable = Math.abs(steveX - STATION_B_X) < 64;
        nearShop = Math.abs(steveX - SHOP_X) < 104;
    }

    private void updateCustomers() {
        if (SAVE.spawnTimer > 0) SAVE.spawnTimer--;
        if (SAVE.spawnTimer <= 0 && SAVE.customers.size() < maxCustomers()) {
            spawnCustomer();
            int wait = Math.max(110, 280 - SAVE.level * 8) + random.nextInt(90);
            if (SAVE.heatActive) wait = wait * 3 / 5;
            SAVE.spawnTimer = wait;
        }
        for (int index = SAVE.customers.size() - 1; index >= 0; index--) {
            Customer customer = SAVE.customers.get(index);
            customer.bob = (customer.bob + 1) % 40;
            if (customer.state == 0) {
                float gap = customer.targetX - customer.x;
                float step = Math.min(2.6F, Math.max(0.7F, Math.abs(gap) * 0.18F));
                customer.x += Math.signum(gap) * step;
                if (Math.abs(gap) < 2F) {
                    customer.x = customer.targetX;
                    customer.state = 1;
                }
                continue;
            }
            if (customer.state == 2) {
                customer.x += 2.2F;
                if (customer.x > SHOP_END + 20) SAVE.customers.remove(index);
                continue;
            }
            customer.patience--;
            if (customer.patience <= 0) {
                customer.state = 2;
                SAVE.spawnTimer = Math.min(SAVE.spawnTimer, 90);
                say(customer.name + " foi embora sem ser atendido...");
            }
        }
    }

    private void spawnCustomer() {
        int options = knownCount();
        if (options == 0) return;
        int pick = random.nextInt(options);
        int recipe = knownIndexAt(pick);
        if (recipe < 0) return;
        Customer customer = new Customer();
        customer.recipe = recipe;
        customer.maxPatience = patienceTicks();
        customer.patience = customer.maxPatience;
        customer.state = 0;
        boolean special = random.nextInt(100) < 12;
        customer.special = special;
        customer.name = special ? SPECIAL_CUSTOMER[random.nextInt(SPECIAL_CUSTOMER.length)] : "Villager";
        customer.robe = special ? 0xFFB98A3A : new int[] {
                0xFF6B8E6B, 0xFF7D6B8E, 0xFF8E7B6B, 0xFF6B7B8E
        }[random.nextInt(4)];
        customer.x = SHOP_END - 20;
        int slot = 0;
        for (Customer other : SAVE.customers) {
            if (other.state != 2) slot++;
        }
        customer.targetX = COUNTER_X + 38 + slot * 34;
        SAVE.customers.add(customer);
    }

    /** Eventos próprios de cada loja: produto esfriando, onda de calor e promoção. */
    private void updateSpecialEvents() {
        if (freshnessTicks > 0) {
            for (int recipe = 0; recipe < SAVE.freshTimer.length; recipe++) {
                if (SAVE.freshTimer[recipe] > 0) SAVE.freshTimer[recipe]--;
            }
        }
        if (heatWaves) {
            if (SAVE.heatActive) {
                if (--SAVE.heatTimer <= 0) {
                    SAVE.heatActive = false;
                    SAVE.heatCooldown = heatEvery;
                    say("A onda de calor passou. Ufa!");
                }
            } else if (--SAVE.heatCooldown <= 0) {
                SAVE.heatActive = true;
                SAVE.heatTimer = heatDuration;
                say("ONDA DE CALOR! Clientes em dobro e preços melhores por "
                        + (heatDuration / 20) + "s.");
            }
        }
        if (dailyPromo) {
            if (--SAVE.promoTimer <= 0) {
                SAVE.promoTimer = 2400;
                int options = knownCount();
                if (options > 0) {
                    SAVE.promoRecipe = knownIndexAt(random.nextInt(options));
                    say(promoLabel + ": " + productPrefix + RECIPE_NAME[SAVE.promoRecipe]
                            + " paga 50% a mais!");
                }
            }
        }
    }

    private boolean isPromo(int recipe) {
        return dailyPromo && recipe >= 0 && recipe == SAVE.promoRecipe;
    }

    private boolean isFresh(int recipe) {
        return freshnessTicks <= 0 || SAVE.freshTimer[recipe] > 0;
    }

    private void updateOvens() {
        for (int index = SAVE.baking.size() - 1; index >= 0; index--) {
            int[] entry = SAVE.baking.get(index);
            entry[1]--;
            if (entry[1] > 0) continue;
            SAVE.stock[entry[0]]++;
            if (freshnessTicks > 0) SAVE.freshTimer[entry[0]] = freshnessTicks;
            SAVE.baking.remove(index);
            say(productPrefix + RECIPE_NAME[entry[0]] + " " + readySuffix);
        }
    }

    private void updateEmployees() {
        for (int index = 0; index < EMPLOYEE_NAME.length; index++) {
            if (!SAVE.hired[index]) continue;
            if (SAVE.employeeTimer[index] > 0) {
                SAVE.employeeTimer[index]--;
                continue;
            }
            SAVE.employeeTimer[index] = EMPLOYEE_SPEED[index];
            if (EMPLOYEE_CHEF[index]) {
                chefWork();
            } else {
                waiterWork();
            }
        }
    }

    private void chefWork() {
        if (SAVE.baking.size() >= ovenSlots()) return;
        int chosen = -1;
        for (Customer customer : SAVE.customers) {
            if (customer.state != 1 || SAVE.stock[customer.recipe] > 0) continue;
            if (!SAVE.known[customer.recipe] || !hasIngredients(customer.recipe)) continue;
            chosen = customer.recipe;
            break;
        }
        if (chosen < 0) {
            for (int recipe = RECIPE_NAME.length - 1; recipe >= 0; recipe--) {
                if (!SAVE.known[recipe] || SAVE.stock[recipe] > 0) continue;
                if (!hasIngredients(recipe)) continue;
                chosen = recipe;
                break;
            }
        }
        if (chosen < 0) return;
        consumeIngredients(chosen);
        queueBake(chosen);
    }

    private void waiterWork() {
        Customer target = null;
        for (Customer customer : SAVE.customers) {
            if (customer.state != 1) continue;
            if (SAVE.stock[customer.recipe] <= 0) continue;
            target = customer;
            break;
        }
        if (target != null) deliver(target, false);
    }

    private boolean queueBake(int recipe) {
        if (SAVE.baking.size() >= ovenSlots()) {
            say("A " + station + " está cheia. Compre mais em MELHORIAS.");
            return false;
        }
        SAVE.baking.add(new int[] {recipe, bakeTicks()});
        return true;
    }

    private void deliver(Customer customer, boolean byPlayer) {
        if (SAVE.stock[customer.recipe] <= 0) {
            say(productPrefix + RECIPE_NAME[customer.recipe] + " ainda não está " + productReady + ".");
            return;
        }
        SAVE.stock[customer.recipe]--;
        int price = sellPrice(customer.recipe);
        if (customer.special) price = price * 3 / 2;
        SAVE.money += price;
        SAVE.earned += price;
        SAVE.served++;
        addXp(4 + price / 3);
        if (customer.special || random.nextInt(100) < 22) {
            int item = random.nextInt(COLLECTIBLE.length);
            if (!SAVE.collection[item]) {
                SAVE.collection[item] = true;
                say(customer.name + " deixou " + COLLECTIBLE[item] + " na coleção!");
            } else {
                say("Cliente satisfeito: + R$ " + price);
            }
        } else if (byPlayer) {
            say("Cliente satisfeito: + R$ " + price);
        }
        if (!isFresh(customer.recipe) && freshnessTicks > 0) {
            say("O " + product + " já estava " + staleWord + ": pagou menos.");
        } else if (isPromo(customer.recipe)) {
            say(promoLabel + ": + R$ " + price + "!");
        }
        SAVE.customers.remove(customer);
    }

    /* ------------------------------------------------------------------ */
    /* Interação                                                          */
    /* ------------------------------------------------------------------ */

    private void interact() {
        if (overlay != Overlay.NONE) {
            overlay = Overlay.NONE;
            return;
        }
        updateNearby();
        if (nearTable) {
            overlay = Overlay.CRAFT;
        } else if (nearStation) {
            overlay = Overlay.NONE;
            if (SAVE.baking.isEmpty()) {
                say("Nada para " + stationVerb + ". Monte na bancada primeiro.");
            } else {
                int restante = Math.max(0, SAVE.baking.get(0)[1]);
                say(bakingWord + " " + SAVE.baking.size() + "/" + ovenSlots()
                        + ". Próximo fica pronto em " + (restante / 20 + 1) + "s.");
            }
        } else if (nearShop) {
            overlay = Overlay.SHOP;
        } else {
            say("Chegue perto da bancada, da " + station + " ou da loja.");
        }
    }

    private void tryServe(int recipe) {
        Customer target = null;
        for (Customer customer : SAVE.customers) {
            if (customer.state == 1 && customer.recipe == recipe) {
                target = customer;
                break;
            }
        }
        if (target != null) {
            deliver(target, true);
        } else {
            say("Ninguém pediu " + RECIPE_NAME[recipe] + " agora.");
        }
    }

    private void autoCraft(int recipe) {
        if (!SAVE.known[recipe]) return;
        if (!hasIngredients(recipe)) {
            say("Faltam ingredientes para " + RECIPE_NAME[recipe] + ".");
            return;
        }
        if (SAVE.baking.size() >= ovenSlots()) {
            say("A " + station + " está cheia: espere ou compre outra.");
            return;
        }
        consumeIngredients(recipe);
        queueBake(recipe);
        say("Preparando " + RECIPE_NAME[recipe] + "...");
    }

    private void bakeBench() {
        int count = 0;
        for (int value : SAVE.bench) if (value >= 0) count++;
        if (count == 0) {
            say("Coloque ingredientes na bancada.");
            return;
        }
        int match = matchedRecipe();
        if (match < 0) {
            say("Não é nenhuma receita conhecida... tente outra combinação.");
            return;
        }
        if (SAVE.baking.size() >= ovenSlots()) {
            say("A " + station + " está cheia: espere ou compre outra.");
            return;
        }
        if (!SAVE.known[match]) {
            SAVE.known[match] = true;
            addXp(25 + RECIPE_PRICE[match]);
            say("NOVA RECEITA: " + RECIPE_NAME[match] + "! Ela já apareceu no cardápio.");
        } else {
            say(productPrefix + RECIPE_NAME[match] + " " + productReady + "!");
        }
        queueBake(match);
        for (int index = 0; index < SAVE.bench.length; index++) SAVE.bench[index] = -1;
    }

    private boolean sameIngredients(int[] bench, int[] recipe) {
        int[] benchCount = new int[ING.length];
        for (int value : bench) if (value >= 0) benchCount[value]++;
        int[] need = new int[ING.length];
        for (int value : recipe) need[value]++;
        for (int index = 0; index < ING.length; index++) {
            if (benchCount[index] != need[index]) return false;
        }
        return true;
    }

    private void buyIngredient(int index, int amount) {
        int cost = ING_PRICE[index];
        int bought = Math.min(amount, SAVE.money / cost);
        if (bought <= 0) {
            say("Dinheiro insuficiente para " + ING[index] + ".");
            return;
        }
        SAVE.money -= cost * bought;
        SAVE.pantry[index] += bought;
        say("Comprou " + bought + "x " + ING[index] + " por R$ " + (cost * bought) + ".");
    }

    private void buyOven() {
        if (SAVE.ovenLevel >= 4) {
            say("A " + station + " já está no máximo.");
            return;
        }
        if (SAVE.money < ovenCost()) {
            say("Faltam R$ " + (ovenCost() - SAVE.money) + " para a " + station + ".");
            return;
        }
        SAVE.money -= ovenCost();
        SAVE.ovenLevel++;
        addXp(30);
        say("Nova " + station + "! " + ovenSlots() + " " + products + " por vez e mais rápido.");
    }

    private void upgradePizzeria() {
        if (SAVE.pizzeriaLevel >= 5) {
            say("A " + shopWord + " já está no máximo.");
            return;
        }
        if (SAVE.money < pizzeriaCost()) {
            say("Faltam R$ " + (pizzeriaCost() - SAVE.money) + ".");
            return;
        }
        SAVE.money -= pizzeriaCost();
        SAVE.pizzeriaLevel++;
        addXp(30);
        say(shopWordCap + " melhorada! Preços e paciência aumentaram.");
    }

    private void upgradeSteve() {
        if (SAVE.steveLevel >= 4) {
            say("Steve já está no máximo.");
            return;
        }
        if (SAVE.money < steveCost()) {
            say("Faltam R$ " + (steveCost() - SAVE.money) + ".");
            return;
        }
        SAVE.money -= steveCost();
        SAVE.steveLevel++;
        addXp(20);
        say("Steve está mais rápido!");
    }

    private void hire(int index) {
        if (SAVE.hired[index]) {
            say(EMPLOYEE_NAME[index] + " já trabalha aqui.");
            return;
        }
        if (SAVE.level < EMPLOYEE_LEVEL[index]) {
            say("Precisa do nível " + EMPLOYEE_LEVEL[index] + " para contratar.");
            return;
        }
        if (SAVE.money < EMPLOYEE_COST[index]) {
            say("Faltam R$ " + (EMPLOYEE_COST[index] - SAVE.money) + ".");
            return;
        }
        SAVE.money -= EMPLOYEE_COST[index];
        SAVE.hired[index] = true;
        SAVE.employeeTimer[index] = 40;
        say(EMPLOYEE_NAME[index] + " contratado!");
    }

    /* ------------------------------------------------------------------ */
    /* Entrada                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (overlay != Overlay.NONE) {
                overlay = Overlay.NONE;
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == 69) {
            interact();
            return true;
        }
        if (overlay != Overlay.NONE) {
            if (keyCode == 257 && overlay == Overlay.CRAFT) {
                bakeBench();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        switch (keyCode) {
            case 263, 65 -> {
                leftHeld = true;
                return true;
            }
            case 262, 68 -> {
                rightHeld = true;
                return true;
            }
            default -> {
            }
        }
        if (keyCode >= 49 && keyCode <= 57) {
            int slot = keyCode - 49;
            int recipe = knownIndexAt(slot);
            if (recipe >= 0) tryServe(recipe);
            return true;
        }
        if (keyCode == 48) {
            int recipe = knownIndexAt(9);
            if (recipe >= 0) tryServe(recipe);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 263, 65 -> {
                leftHeld = false;
                return true;
            }
            case 262, 68 -> {
                rightHeld = false;
                return true;
            }
            default -> {
                return super.keyReleased(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (clickTabs(mx, my)) return true;
        if (overlay != Overlay.NONE) {
            return clickOverlay(mx, my);
        }
        int menu = menuAt(mx, my);
        if (menu >= 0) {
            int recipe = knownIndexAt(menu);
            if (recipe >= 0) {
                if (SAVE.stock[recipe] > 0) {
                    tryServe(recipe);
                } else {
                    autoCraft(recipe);
                }
            }
            return true;
        }
        for (Customer customer : new ArrayList<>(SAVE.customers)) {
            if (customer.state != 1) continue;
            int screenX = (int) (customer.x - cameraX);
            int spriteTop = floorScreen() - 50;
            if (mx >= screenX - 14 && mx <= screenX + 14 && my >= spriteTop - 4 && my <= floorScreen()) {
                deliver(customer, true);
                return true;
            }
        }
        if (Math.abs(steveX - STATION_A_X) < 64 && mx >= STATION_A_X - cameraX - 26
                && mx <= STATION_A_X - cameraX + 26 && my >= floorScreen() - 60 && my <= floorScreen()) {
            interact();
            return true;
        }
        if (Math.abs(steveX - STATION_B_X) < 64 && mx >= STATION_B_X - cameraX - 30
                && mx <= STATION_B_X - cameraX + 30 && my >= floorScreen() - 44 && my <= floorScreen()) {
            interact();
            return true;
        }
        if (Math.abs(steveX - SHOP_X) < 104 && mx >= SHOP_X - cameraX - 40
                && mx <= SHOP_X - cameraX + 40 && my >= floorScreen() - 56 && my <= floorScreen()) {
            interact();
            return true;
        }
        return false;
    }

    private boolean customerWants(int recipe) {
        for (Customer customer : SAVE.customers) {
            if (customer.state == 1 && customer.recipe == recipe) return true;
        }
        return false;
    }

    private boolean clickTabs(int mx, int my) {
        Overlay[] tabs = {Overlay.UPGRADES, Overlay.COLLECTION, Overlay.TEAM};
        int tabWidth = tabWidth();
        int startX = width - 44 - (tabWidth * 3 + 8);
        for (int index = 0; index < tabs.length; index++) {
            int x = startX + index * (tabWidth + 4);
            if (mx >= x && mx <= x + tabWidth && my >= 3 && my <= 20) {
                overlay = overlay == tabs[index] ? Overlay.NONE : tabs[index];
                return true;
            }
        }
        int menuX = width - 42;
        if (mx >= menuX && mx <= width - 6 && my >= 3 && my <= 20) {
            saveWorld();
            client.setScreen(new GamesHubScreen());
            return true;
        }
        return false;
    }

    private int tabWidth() {
        return width >= 430 ? 68 : 46;
    }

    private boolean clickOverlay(int mx, int my) {
        int[] panel = panelRect();
        if (mx < panel[0] || my < panel[1] || mx > panel[0] + panel[2] || my > panel[1] + panel[3]) {
            overlay = Overlay.NONE;
            return true;
        }
        if (mx >= panel[0] + panel[2] - 26 && my <= panel[1] + 20) {
            overlay = Overlay.NONE;
            return true;
        }
        switch (overlay) {
            case CRAFT -> {
                int[] pantry = pantryArea();
                int cardWidth = pantry[2] / 3;
                int cardHeight = 26;
                for (int index = 0; index < ING.length; index++) {
                    int x = pantry[0] + (index % 3) * cardWidth;
                    int y = pantry[1] + (index / 3) * (cardHeight + 3);
                    if (mx >= x && mx < x + cardWidth - 2 && my >= y && my < y + cardHeight) {
                        if (SAVE.pantry[index] <= 0) {
                            say("Sem " + ING[index] + " na despensa.");
                        } else {
                            int slot = freeBenchSlot();
                            if (slot < 0) {
                                say("A bancada está cheia (6 ingredientes).");
                            } else {
                                SAVE.bench[slot] = index;
                                SAVE.pantry[index]--;
                            }
                        }
                        return true;
                    }
                }
                int[] gridArea = craftGrid();
                int cellWidth = gridArea[2] / 3;
                int cellHeight = gridArea[3] / 3;
                for (int index = 0; index < SAVE.bench.length; index++) {
                    int x = gridArea[0] + (index % 3) * cellWidth;
                    int y = gridArea[1] + (index / 3) * cellHeight;
                    if (mx >= x && mx < x + cellWidth - 1 && my >= y && my < y + cellHeight - 1) {
                        int item = SAVE.bench[index];
                        if (item >= 0) {
                            SAVE.pantry[item]++;
                            SAVE.bench[index] = -1;
                        }
                        return true;
                    }
                }
                if (inside(mx, my, resultSlot())) {
                    if (matchedRecipe() < 0) {
                        say("Esses ingredientes não formam nenhum " + product + ".");
                    } else {
                        bakeBench();
                    }
                    return true;
                }
                int[] bake = bakeButton();
                if (inside(mx, my, bake)) {
                    bakeBench();
                    return true;
                }
                int[] clear = clearButton();
                if (inside(mx, my, clear)) {
                    for (int index = 0; index < SAVE.bench.length; index++) {
                        if (SAVE.bench[index] >= 0) {
                            SAVE.pantry[SAVE.bench[index]]++;
                            SAVE.bench[index] = -1;
                        }
                    }
                    return true;
                }
            }
            case SHOP -> {
                int[] grid = shopArea();
                int cardWidth = grid[2] / 4;
                int cardHeight = 30;
                for (int index = 0; index < ING.length; index++) {
                    int x = grid[0] + (index % 4) * cardWidth;
                    int y = grid[1] + (index / 4) * (cardHeight + 4);
                    if (mx >= x && mx < x + cardWidth - 3 && my >= y && my < y + cardHeight) {
                        buyIngredient(index, Screen.hasShiftDown() ? 5 : 1);
                        return true;
                    }
                }
            }
            case UPGRADES -> {
                int[] base = listArea();
                int rowHeight = 34;
                for (int index = 0; index < 3; index++) {
                    int y = base[1] + index * (rowHeight + 6);
                    if (mx >= base[0] && mx < base[0] + base[2] && my >= y && my < y + rowHeight) {
                        if (index == 0) buyOven();
                        else if (index == 1) upgradePizzeria();
                        else upgradeSteve();
                        return true;
                    }
                }
            }
            case TEAM -> {
                int[] base = listArea();
                int rowHeight = 21;
                for (int index = 0; index < EMPLOYEE_NAME.length; index++) {
                    int y = base[1] + index * (rowHeight + 2);
                    if (mx >= base[0] && mx < base[0] + base[2] && my >= y && my < y + rowHeight) {
                        hire(index);
                        return true;
                    }
                }
            }
            default -> {
            }
        }
        return true;
    }

    private int freeBenchSlot() {
        for (int index = 0; index < SAVE.bench.length; index++) {
            if (SAVE.bench[index] < 0) return index;
        }
        return -1;
    }

    private boolean inside(int mx, int my, int[] rect) {
        return mx >= rect[0] && mx < rect[0] + rect[2] && my >= rect[1] && my < rect[1] + rect[3];
    }

    /* ------------------------------------------------------------------ */
    /* Layout                                                             */
    /* ------------------------------------------------------------------ */

    private int[] panelRect() {
        int panelWidth = Math.min(width - 12, 420);
        int top = overlay == Overlay.CRAFT ? 24 : 26;
        int panelHeight = overlay == Overlay.CRAFT
                ? Math.min(height - 56, 236)
                : Math.max(118, Math.min(height - 64, 206));
        return new int[] {(width - panelWidth) / 2, top, panelWidth, panelHeight};
    }

    protected String elide(String text, int maxWidth) {
        if (textRenderer.getWidth(text) <= maxWidth) return text;
        int fit = Math.max(1, maxWidth / 6 - 1);
        return text.substring(0, Math.min(text.length(), fit)) + ".";
    }

    private void section(DrawContext context, int x, int y, String label) {
        context.drawTextWithShadow(textRenderer, Text.literal(label), x, y, ArcadeTheme.SKY);
        context.fill(x, y + 10, x + textRenderer.getWidth(label), y + 11, 0x55FFFFFF);
    }

    private int[] pantryArea() {
        int[] panel = panelRect();
        return new int[] {panel[0] + 8, panel[1] + 42, panel[2] / 2 - 12, 120};
    }

    private int[] benchArea() {
        int[] panel = panelRect();
        int left = panel[0] + panel[2] / 2 + 2;
        int available = panel[0] + panel[2] - 6 - left;
        return new int[] {left, panel[1] + 42, available, 72};
    }

    private int[] craftGrid() {
        int[] bench = benchArea();
        return new int[] {bench[0], bench[1], Math.min(84, bench[2] - 46), 63};
    }

    private int[] resultSlot() {
        int[] grid = craftGrid();
        int size = 24;
        return new int[] {grid[0] + grid[2] + 16, grid[1] + 19, size, size};
    }

    private int matchedRecipe() {
        for (int recipe = 0; recipe < RECIPE_NAME.length; recipe++) {
            if (sameIngredients(SAVE.bench, RECIPE_ITEMS[recipe])) return recipe;
        }
        return -1;
    }

    private int[] bakeButton() {
        int[] panel = panelRect();
        int[] bench = benchArea();
        int width = Math.min(76, (bench[2] - 6) / 2);
        int y = Math.max(bench[1] + bench[3] + 8, panel[1] + panel[3] - 56);
        return new int[] {bench[0], y, width, 18};
    }

    private int[] clearButton() {
        int[] panel = panelRect();
        int[] bake = bakeButton();
        return new int[] {bake[0] + bake[2] + 6, bake[1], Math.min(66, panel[0] + panel[2] - 8 - bake[0] - bake[2] - 6), 18};
    }

    private int[] shopArea() {
        int[] panel = panelRect();
        return new int[] {panel[0] + 8, panel[1] + 24, panel[2] - 16, 120};
    }

    private int[] listArea() {
        int[] panel = panelRect();
        return new int[] {panel[0] + 8, panel[1] + 26, panel[2] - 16, panel[3] - 34};
    }

    /* ------------------------------------------------------------------ */
    /* Render                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawWorld(context);
        drawFurniture(context);
        drawCustomers(context, mouseX, mouseY);
        drawSteve(context);
        drawStationAFront(context, STATION_A_X - (int) cameraX, floorScreen());
        drawHud(context, mouseX, mouseY);
        if (overlay != Overlay.NONE) {
            drawOverlay(context, mouseX, mouseY);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawWorld(DrawContext context) {
        int budget = 10;
        int startCx = (int) cameraX / CHUNK_SIZE;
        int endCx = (int) (cameraX + width) / CHUNK_SIZE + 1;
        int endCy = (FLOOR_Y + 60) / CHUNK_SIZE;
        Set<Long> visible = new HashSet<>(64);
        for (int cy = 0; cy <= endCy; cy++) {
            for (int cx = startCx; cx <= endCx; cx++) {
                if (cx < 0 || cx * CHUNK_SIZE > WORLD_WIDTH) continue;
                long key = ((long) cx << 32) | (cy & 0xFFFFFFFFL);
                visible.add(key);
                int[] pixels = CHUNKS.get(key);
                if (pixels == null) {
                    if (budget <= 0) {
                        drawChunkPlaceholder(context, cx, cy);
                        continue;
                    }
                    pixels = generateChunk(cx, cy);
                    CHUNKS.put(key, pixels);
                    budget--;
                }
                LOADED.add(key);
                drawChunk(context, cx, cy, pixels);
            }
        }
        LOADED.retainAll(visible);
        context.fill(0, 0, width, Math.max(0, originY), 0xFF0A1220);
    }

    private void drawChunkPlaceholder(DrawContext context, int cx, int cy) {
        int x = cx * CHUNK_SIZE - (int) cameraX;
        int y = cy * CHUNK_SIZE + originY;
        context.fill(x, y, x + CHUNK_SIZE, y + CHUNK_SIZE, 0xFF0E1A28);
    }

    private int[] generateChunk(int cx, int cy) {
        int[] pixels = new int[CHUNK_PIXELS * CHUNK_PIXELS];
        for (int py = 0; py < CHUNK_PIXELS; py++) {
            for (int px = 0; px < CHUNK_PIXELS; px++) {
                int wx = cx * CHUNK_SIZE + px * PIXEL + PIXEL / 2;
                int wy = cy * CHUNK_SIZE + py * PIXEL + PIXEL / 2;
                pixels[py * CHUNK_PIXELS + px] = colorAt(wx, wy);
            }
        }
        return pixels;
    }

    private void drawChunk(DrawContext context, int cx, int cy, int[] pixels) {
        int baseX = cx * CHUNK_SIZE - (int) cameraX;
        int baseY = cy * CHUNK_SIZE + originY;
        for (int py = 0; py < CHUNK_PIXELS; py++) {
            int runStart = 0;
            for (int px = 1; px <= CHUNK_PIXELS; px++) {
                boolean end = px == CHUNK_PIXELS;
                if (!end && pixels[py * CHUNK_PIXELS + px] == pixels[py * CHUNK_PIXELS + runStart]) continue;
                int color = pixels[py * CHUNK_PIXELS + runStart];
                int x1 = baseX + runStart * PIXEL;
                int y1 = baseY + py * PIXEL;
                context.fill(x1, y1, baseX + px * PIXEL, y1 + PIXEL, color);
                runStart = px;
            }
        }
    }

    protected int noise(int wx, int wy) {
        int hash = wx * 73856093 ^ wy * 19349663;
        hash ^= hash >>> 13;
        hash *= 0x5bd1e995;
        hash ^= hash >>> 15;
        return hash & 15;
    }

    protected int floorColor(int wx, int wy) {
        return ((wx / 16) + (wy / 16)) % 2 == 0 ? FLOOR_A : FLOOR_B;
    }

    protected int shade(int color, int delta) {
        int r = Math.max(0, Math.min(255, ((color >> 16) & 0xFF) + delta));
        int g = Math.max(0, Math.min(255, ((color >> 8) & 0xFF) + delta));
        int b = Math.max(0, Math.min(255, (color & 0xFF) + delta));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int colorAt(int wx, int wy) {
        int n = noise(wx / PIXEL, wy / PIXEL);
        int delta = (n % 5) - 2;
        boolean floorArea = wy >= FLOOR_Y;
        if (wx < SHOP_END) {
            if (floorArea) {
                int base = floorColor(wx, wy);
                if (wy > FLOOR_Y + 34) base = shade(base, -18);
                return shade(base, delta);
            }
            int decor = decorColor(wx, wy, delta);
            if (decor != 0) return decor;
            // janela com vista para a rua
            if (wx >= 36 && wx <= 214 && wy >= 40 && wy <= 112) {
                if (wx < 42 || wx > 208 || wy < 46 || wy > 106) {
                    return shade(wx == 36 || wx == 214 || wy == 40 || wy == 112 ? WALL_TRIM : WALL_PANEL, delta);
                }
                if ((wx >= 122 && wx <= 126) || (wy >= 74 && wy <= 78)) return shade(WALL_PANEL, delta);
                int sky = wy < 60 ? 0xFFA8DDF5 : wy < 88 ? 0xFF8FCBEC : 0xFFC4E8F7;
                if ((wx + wy) % 36 == 0) sky = 0xFFE9F9FF;
                return shade(sky, delta);
            }
            // quadro de menu na parede
            if (wx >= 590 && wx <= 718 && wy >= 38 && wy <= 100) {
                if (wx < 596 || wx > 712 || wy < 44 || wy > 94) return shade(WALL_WOOD_LINE, delta);
                boolean line = (wy == 52 || wy == 63 || wy == 74 || wy == 85) && wx >= 604 && wx <= 704;
                if (line) return shade(0xFFEDE6D2, delta);
                return shade(0xFF22302A, delta);
            }
            // luminárias penduradas
            if (wy <= 38) {
                int lamp = wx >= 270 && wx <= 292 ? 281 : wx >= 626 && wx <= 648 ? 637 : -1;
                if (lamp >= 0) {
                    int d = Math.abs(wx - lamp);
                    if (wy <= 16 && d <= 1) return shade(0xFF2A2118, delta);
                    if (wy >= 17 && wy <= 29 && d <= 3 + (wy - 17) / 3) return shade(0xFF333F4A, delta);
                    if (wy >= 29 && wy <= 35 && d <= 5) {
                        return shade(wy <= 32 ? 0xFFFFE2A0 : 0xFFFFC46A, delta);
                    }
                }
            }
            if (wy >= 166) return shade(wy <= 169 ? WALL_TRIM : 0xFF5E3626, delta);
            if (wy < 108) {
                if (wy % 8 == 0) return shade(0xFFD8BE9A, delta);
                boolean brickLine = (wy / 8) % 2 == 0;
                int offset = brickLine ? 0 : 12;
                if ((wx + offset) % 24 == 0) return shade(WALL_TRIM, delta);
                return shade(WALL_TOP, delta);
            }
            if (wy % 6 == 0) return shade(WALL_WOOD_LINE, delta);
            return shade(WALL_WOOD, delta);
        }
        if (wx < STREET_END) {
            if (floorArea) {
                if (wy < FLOOR_Y + 10) {
                    return shade((wx / 20) % 2 == 0 ? 0xFF9AA0A8 : 0xFF8A9098, delta);
                }
                if (wy < FLOOR_Y + 14) return shade(0xFF6E747C, delta);
                if (wy % 6 == 0) return shade(0xFF24282F, delta);
                boolean dash = (wx / 26) % 2 == 0 && wy > FLOOR_Y + 28 && wy < FLOOR_Y + 34;
                if (dash) return shade(0xFFE5C15A, delta);
                return shade(0xFF33383F, delta);
            }
            if (wy < 58) {
                int cloud = noise(wx / 26, wy / 9);
                if (wy < 20 && cloud > 11) return shade(0xFFF2FAFF, delta);
                if (wy >= 22 && wy < 34 && cloud > 12) return shade(0xFFE3F2FD, delta);
                return shade(wy < 26 ? 0xFF8CC6EC : 0xFFA9D8F5, delta);
            }
            int building = (wx / 58) % 3;
            int facade = building == 0 ? 0xFF4C5364 : building == 1 ? 0xFF5A4A46 : 0xFF3F4E5A;
            int columns = wx % 22;
            if (wy < 62) return shade(0xFF6B6257, delta);
            if (wy < 152) {
                boolean band = (wy - 62) % 34 < 8;
                boolean frame = columns < 5 || columns > 16;
                if (band || frame) return shade(facade, delta - 14);
                boolean lit = noise(wx / 6, wy / 6) > 9;
                return shade(lit ? 0xFFB8DCEF : 0xFF6E8CA6, delta);
            }
            if (wy < 160) return shade(0xFF8A8578, delta);
            if (wy < 176) {
                int awning = building == 0 ? 0xFFB23A2E : building == 1 ? 0xFF2E5E5A : 0xFF3A4E7A;
                return shade((wx / 12) % 2 == 0 ? awning : 0xFFEDE6D2, delta);
            }
            return shade(0xFF8A8578, delta);
        }
        if (floorArea) {
            boolean alt = ((wx / 16) + (wy / 16)) % 2 == 0;
            return shade(alt ? 0xFF7D6B55 : 0xFF655440, delta);
        }
        if (wy < 14) return shade(SHOP_WALL_TOP, delta);
        if (wy > 34 && wy < 46) return shade(SHOP_WALL, delta);
        if (wy > 60 && wy < 96) {
            boolean shelf = wy % 30 < 4;
            if (shelf) return shade(SHOP_SHELF, delta);
            int jar = wx % 18;
            if (jar < 8) return shade(SHOP_JAR_COLORS[((wx / 18) % SHOP_JAR_COLORS.length)], delta);
            return shade(0xFF4A3B2E, delta);
        }
        return shade(SHOP_WALL, delta);
    }

    /* ------------------------------------------------------------------ */
    /* Mobília e personagens                                              */
    /* ------------------------------------------------------------------ */

    private void drawFurniture(DrawContext context) {
        int floorY = floorScreen();
        drawSign(context, COUNTER_X - 48 - (int) cameraX, floorY - 116, shopSign);
        drawStationA(context, STATION_A_X - (int) cameraX, floorY);
        drawStationB(context, STATION_B_X - (int) cameraX, floorY);
        drawCounter(context, COUNTER_X - (int) cameraX, floorY);
        drawShop(context, SHOP_X - (int) cameraX, floorY);
        drawDoor(context, DOOR_X - (int) cameraX, floorY);
    }

    private void drawSign(DrawContext context, int x, int y, String label) {
        ArcadeTheme.rounded(context, x, y, x + 96, y + 30, 6, SIGN_BORDER);
        ArcadeTheme.rounded(context, x + 2, y + 2, x + 94, y + 28, 5, SIGN_BG);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(elide(label, 88)), x + 48, y + 11,
                SIGN_TEXT);
        for (int index = 0; index < 6; index++) {
            context.fill(x + 10 + index * 14, y + 2, x + 11 + index * 14, y + 5, 0xFFFFF3C4);
        }
    }

    private void drawCounterFront(DrawContext context, int x, int floorY) {
        int top = floorY - 40;
        context.fill(x - 66, top + 11, x + 66, floorY, 0xFF8A5F38);
        context.fill(x - 66, top + 13, x + 66, top + 16, 0xFFA2734A);
        for (int column = 0; column <= 5; column++) {
            context.fill(x - 64 + column * 26, top + 16, x - 62 + column * 26, floorY, 0xFF6E4A2C);
        }
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 5; column++) {
                context.fill(x - 60 + column * 26, top + 20 + row * 12, x - 42 + column * 26,
                        top + 27 + row * 12, 0xFFA2734A);
                context.fill(x - 60 + column * 26, top + 20 + row * 12, x - 42 + column * 26,
                        top + 21 + row * 12, 0xFFC08B5A);
            }
        }
        context.fill(x - 66, floorY - 3, x + 66, floorY, 0xFF5E3E24);
    }

    private void drawCounter(DrawContext context, int x, int floorY) {
        int top = floorY - 40;
        context.fill(x - 67, top, x + 67, top + 6, 0xFFC79A66);
        context.fill(x - 67, top + 6, x + 67, top + 12, 0xFF9C7245);
        drawCounterFront(context, x, floorY);
        ArcadeTheme.disc(context, x, top - 1, 4, 0xFFFFD966);
        context.fill(x - 1, top - 5, x + 1, top - 1, 0xFFB58553);
    }

    private void drawShop(DrawContext context, int x, int floorY) {
        int top = floorY - 52;
        context.fill(x - 44, top, x + 44, top + 5, SHOP_WALL);
        context.fill(x - 44, top + 5, x + 44, floorY - 6, SHOP_WALL_TOP);
        for (int row = 0; row < 3; row++) {
            context.fill(x - 40, top + 10 + row * 14, x + 40, top + 12 + row * 14, SHOP_SHELF);
            for (int column = 0; column < 5; column++) {
                ArcadeTheme.disc(context, x - 30 + column * 15, top + 8 + row * 14, 3,
                        ING_COLOR[(row * 5 + column) % ING.length]);
            }
        }
        context.fill(x - 48, floorY - 8, x + 48, floorY - 2, 0xFF9C7245);
        drawLabel(context, x, floorY + 2, shopSignShop, ArcadeTheme.SKY);
    }

    private void drawDoor(DrawContext context, int x, int floorY) {
        context.fill(x - 20, floorY - 74, x + 20, floorY, 0xFF4A2C1B);
        context.fill(x - 16, floorY - 70, x + 16, floorY - 4, 0xFF6B4028);
        context.fill(x + 10, floorY - 40, x + 14, floorY - 36, 0xFFE5B84B);
        drawLabel(context, x, floorY + 2, "SAÍDA", ArcadeTheme.MUTED);
    }

    protected void drawLabel(DrawContext context, int centerX, int y, String label, int color) {
        int textWidth = textRenderer.getWidth(label);
        context.fill(centerX - textWidth / 2 - 3, y - 1, centerX + textWidth / 2 + 3, y + 9, 0xAA0A1220);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), centerX, y, color);
    }

    private static final String[] STEVE_ART = {
            "  HHHH  ",
            " HHHHHH ",
            " HSSSSH ",
            " HSWWSH ",
            " HSSSSH ",
            "  SSSS  ",
            " SCCCCS ",
            "SCCCCCCS",
            "SCCCCCCS",
            "SCCCCCCS",
            "  CCCC  ",
            "  PPPP  ",
            "  PPPP  ",
            "  PPPP  ",
            "  PPPP  ",
            "  BBBB  "
    };

    private static final String[] VILLAGER_ART = {
            "  HHHH  ",
            " HHHHHH ",
            " HSSSSH ",
            " SSSSSS ",
            " SSWWSH ",
            "  SNNS  ",
            " RRRRRR ",
            "RRRRRRRR",
            "RRRRRRRR",
            "RRRRRRRR",
            " RRRRRR ",
            "  RRRR  ",
            "  RRRR  ",
            "  RRRR  ",
            "  BBBB  "
    };

    private void drawSteve(DrawContext context) {
        int x = (int) (steveX - cameraX);
        int floorY = floorScreen();
        boolean behindCounter = Math.abs(steveX - COUNTER_X) < 66;
        int feet = floorY - (behindCounter ? 18 : 0);
        ArcadeTheme.disc(context, x, feet, 10, 0x55000000);
        int scale = 3;
        int spriteWidth = 8 * scale;
        int spriteHeight = 16 * scale;
        int bob = walking ? (steveFrame / 4) % 2 : 0;
        int drawX = x - spriteWidth / 2;
        int drawY = feet - spriteHeight - bob;
        int[] colors = {0xFF3B2416, 0xFFC68863, 0xFFF2F7FF, 0xFF2FBFB4, 0xFF35507A, 0xFF2A2118};
        drawSprite(context, STEVE_ART, "HSWCPB", colors, drawX, drawY, scale, false);
        if (behindCounter) {
            ArcadeTheme.rounded(context, drawX - 1, drawY - 10, drawX + spriteWidth + 1, drawY + 3, 4, 0xFFF4F0E0);
            context.fill(drawX, drawY - 4, drawX + spriteWidth, drawY - 1, 0xFFD8D2C4);
            drawCounterFront(context, COUNTER_X - (int) cameraX, floorY);
        }
    }

    private void drawCustomers(DrawContext context, int mouseX, int mouseY) {
        int floorY = floorScreen();
        hoveredCustomer = -1;
        float nextX = Float.MAX_VALUE;
        for (Customer customer : SAVE.customers) {
            if (customer.state == 1 && customer.x < nextX) nextX = customer.x;
        }
        Customer hovered = null;
        Customer hoveredRef = null;
        for (Customer customer : SAVE.customers) {
            int x = (int) (customer.x - cameraX);
            int scale = 3;
            int spriteWidth = 8 * scale;
            int spriteHeight = 15 * scale;
            int feet = floorY;
            ArcadeTheme.disc(context, x, feet, 10, 0x55000000);
            int bob = customer.state == 1 && (customer.bob / 20) % 2 == 0 ? 1 : 0;
            int drawY = feet - spriteHeight - bob;
            int[] colors = {0xFF3B2416, 0xFFC68863, 0xFFF2F7FF, 0xFF5A3A2A, customer.robe, 0xFF2A2118};
            drawSprite(context, VILLAGER_ART, "HSNWRB", colors, x - spriteWidth / 2, drawY, scale, true);
            if (customer.state == 1) {
                float mood = customer.patience / (float) Math.max(1, customer.maxPatience);
                if (mood < 0.35F && (ticks / 12) % 2 == 0) {
                    for (int drop = 0; drop < 2; drop++) {
                        int dropY = drawY - 6 - drop * 5 - (ticks / 3) % 5;
                        context.fill(x - 1 + drop * 4, dropY, x + 2 + drop * 4, dropY + 3, 0xFF6CC9F5);
                    }
                }
                if (customer.special) {
                    for (int spark = 0; spark < 3; spark++) {
                        int angle = ticks / 4 + spark * 40;
                        int sparkX = x + (int) (Math.cos(Math.toRadians(angle)) * 12);
                        int sparkY = drawY + 8 + (int) (Math.sin(Math.toRadians(angle)) * 4);
                        context.fill(sparkX, sparkY, sparkX + 1, sparkY + 1, 0xFFFFF3C4);
                    }
                }
            }
            if (customer.state == 1) {
                boolean first = customer.x <= nextX + 0.5F;
                drawRequestBubble(context, x, drawY - 26, customer, first);
                if (mouseX >= x - 13 && mouseX <= x + 13 && mouseY >= drawY - 6 && mouseY <= feet) {
                    hovered = customer;
                    hoveredRef = customer;
                }
            }
        }
        if (hovered != null) {
            String label = RECIPE_NAME[hoveredRef.recipe] + "  •  R$ " + sellPrice(hoveredRef.recipe);
            int boxWidth = textRenderer.getWidth(label) + 10;
            int boxX = (int) (hoveredRef.x - cameraX);
            ArcadeTheme.rounded(context, boxX - boxWidth / 2, floorY - 96, boxX + boxWidth / 2,
                    floorY - 82, 4, 0xE00A1220);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), boxX,
                    floorY - 93, ArcadeTheme.GOLD);
        }
        hoveredCustomer = hovered == null ? -1 : 0;
    }

    private void drawRequestBubble(DrawContext context, int x, int y, Customer customer, boolean showName) {
        int widthBubble = 44;
        ArcadeTheme.rounded(context, x - widthBubble / 2, y - 14, x + widthBubble / 2, y + 12, 5,
                customer.special ? 0xFFE5B84B : 0xFFF2F7FF);
        ArcadeTheme.disc(context, x, y - 2, 7, 0xFFE0B979);
        ArcadeTheme.disc(context, x, y - 2, 6, RECIPE_COLOR[customer.recipe]);
        for (int index = 0; index < 4; index++) {
            ArcadeTheme.disc(context, x - 3 + index * 2, y - 4 + (index % 2) * 2, 1, 0xFFB23A2E);
        }
        float patience = customer.patience / (float) Math.max(1, customer.maxPatience);
        ArcadeTheme.progress(context, x - widthBubble / 2 + 4, y + 6, widthBubble - 8, 3, patience,
                patience > 0.5F ? 0xFF6EE58C : patience > 0.25F ? ArcadeTheme.GOLD : ArcadeTheme.CORAL);
        if (customer.special) {
            ArcadeTheme.star(context, x + widthBubble / 2 - 2, y - 14, 3, ArcadeTheme.GOLD);
        }
        if (showName) {
            String label = elide(RECIPE_NAME[customer.recipe], 70);
            int labelWidth = textRenderer.getWidth(label);
            context.fill(x - labelWidth / 2 - 2, y - 33, x + labelWidth / 2 + 2, y - 23, 0xB30A1220);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x, y - 31,
                    customer.special ? ArcadeTheme.GOLD : ArcadeTheme.TEXT);
        }
    }

    private void drawSprite(DrawContext context, String[] art, String keys, int[] colors,
                            int x, int y, int scale, boolean flip) {
        for (int row = 0; row < art.length; row++) {
            String line = art[row];
            for (int column = 0; column < line.length(); column++) {
                char ch = line.charAt(flip ? line.length() - 1 - column : column);
                if (ch == ' ') continue;
                int index = keys.indexOf(ch);
                if (index < 0 || index >= colors.length) continue;
                context.fill(x + column * scale, y + row * scale, x + column * scale + scale,
                        y + row * scale + scale, colors[index]);
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* HUD                                                                */
    /* ------------------------------------------------------------------ */

    private void drawHud(DrawContext context, int mouseX, int mouseY) {
        context.fill(0, 0, width, 22, 0xE00A1220);
        context.fill(0, 22, width, 23, 0x66FFFFFF);
        int tabWidth = tabWidth();
        int startX = width - 44 - (tabWidth * 3 + 8);
        String moneyLabel = width >= 380 ? "R$ " + SAVE.money : "R$" + SAVE.money;
        String levelLabel = width >= 380 ? "NÍVEL " + SAVE.level : "N" + SAVE.level;
        int moneyWidth = ArcadeTheme.pill(context, textRenderer, moneyLabel, 4, 3, ArcadeTheme.GOLD);
        ArcadeTheme.pill(context, textRenderer, levelLabel, 8 + moneyWidth, 3, ArcadeTheme.LIME);
        int need = levelNeed(SAVE.level);
        int xpLeft = 8 + moneyWidth + textRenderer.getWidth(levelLabel) + 24;
        int xpWidth = Math.min(72, startX - 6 - xpLeft);
        if (xpWidth >= 34) {
            ArcadeTheme.progress(context, xpLeft, 4, xpWidth, 5, SAVE.xp / (float) need, ArcadeTheme.SKY);
            context.drawTextWithShadow(textRenderer, Text.literal(SAVE.xp + "/" + need),
                    xpLeft, 11, ArcadeTheme.MUTED);
        }

        Overlay[] tabs = {Overlay.UPGRADES, Overlay.COLLECTION, Overlay.TEAM};
        String[] labels = tabWidth >= 60
                ? new String[] {"MELHORIAS", "COLEÇÃO", "EQUIPE"}
                : new String[] {"MELH.", "COL.", "EQU."};
        int[] accents = {ArcadeTheme.LIME, ArcadeTheme.PURPLE, ArcadeTheme.CORAL};
        for (int index = 0; index < tabs.length; index++) {
            int x = startX + index * (tabWidth + 4);
            boolean active = overlay == tabs[index];
            boolean hovered = mouseX >= x && mouseX <= x + tabWidth && mouseY >= 3 && mouseY <= 20;
            ArcadeTheme.rounded(context, x, 3, x + tabWidth, 20, 6,
                    active ? accents[index] : hovered ? 0xFF233650 : 0xCC122033);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(labels[index]),
                    x + tabWidth / 2, 8, active ? 0xFF0A1220 : ArcadeTheme.TEXT);
        }
        int menuX = width - 42;
        ArcadeTheme.rounded(context, menuX, 3, width - 6, 20, 6, 0xCC122033);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("MENU"), menuX + 18, 8, ArcadeTheme.MUTED);

        drawBottomBar(context, mouseX, mouseY);
        if (toastTicks > 0 && !toast.isEmpty()) {
            String text = elide(toast, width - 20);
            int textWidth = textRenderer.getWidth(text);
            int centerX = width / 2;
            ArcadeTheme.rounded(context, centerX - textWidth / 2 - 8, 27,
                    centerX + textWidth / 2 + 8, 43, 6, 0xE00A1220);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(text), centerX,
                    32, ArcadeTheme.TEXT);
        }
        if (SAVE.heatActive) {
            String banner = "ONDA DE CALOR!  +25% nos preços  " + (SAVE.heatTimer / 20 + 1) + "s";
            int bannerWidth = textRenderer.getWidth(banner) + 14;
            int bannerY = toastTicks > 0 ? 47 : 27;
            ArcadeTheme.rounded(context, width / 2 - bannerWidth / 2, bannerY,
                    width / 2 + bannerWidth / 2, bannerY + 15, 6, 0xE0C4472F);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(banner), width / 2,
                    bannerY + 4, 0xFFFFF3C4);
        }
        String hint = nearTable ? hintBench
                : nearStation ? hintStation
                : nearShop ? hintShop : "";
        if (!hint.isEmpty() && hoveredMenu < 0) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(hint), width / 2,
                    height - 47, ArcadeTheme.GOLD);
        }
    }

    private void drawBottomBar(DrawContext context, int mouseX, int mouseY) {
        int barTop = height - 32;
        context.fill(0, barTop, width, height, 0xE00A1220);
        context.fill(0, barTop, width, barTop + 1, 0x66FFFFFF);
        int count = Math.max(1, knownCount());
        int slotWidth = menuSlotWidth();
        int startX = 4;
        hoveredMenu = -1;
        for (int slot = 0; slot < count; slot++) {
            int recipe = knownIndexAt(slot);
            if (recipe < 0) continue;
            int x = startX + slot * slotWidth;
            boolean hovered = mouseX >= x && mouseX < x + slotWidth - 3 && mouseY >= barTop + 3
                    && mouseY < height - 2;
            if (hovered) hoveredMenu = slot;
            boolean ready = SAVE.stock[recipe] > 0;
            boolean fresh = isFresh(recipe);
            ArcadeTheme.rounded(context, x, barTop + 3, x + slotWidth - 3, height - 2, 5,
                    hovered ? 0xFF2A3F5C : 0xCC152438);
            ArcadeTheme.disc(context, x + slotWidth / 2 - 1, barTop + 13, 6,
                    ready ? (fresh ? RECIPE_COLOR[recipe] : 0xFF6A6A72) : 0xFF3A4250);
            if (ready) {
                ArcadeTheme.disc(context, x + slotWidth / 2 - 4, barTop + 11, 2, 0xFFB23A2E);
                ArcadeTheme.disc(context, x + slotWidth / 2 + 1, barTop + 14, 2, 0xFFB23A2E);
            } else {
                context.fill(x + slotWidth / 2 - 5, barTop + 12, x + slotWidth / 2 + 3, barTop + 13,
                        0xFF6C7E93);
            }
            context.drawTextWithShadow(textRenderer, Text.literal(slot < 9 ? String.valueOf(slot + 1) : "0"),
                    x + 2, barTop + 4, ArcadeTheme.GOLD);
            String stock = "x" + SAVE.stock[recipe];
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(stock),
                    x + slotWidth / 2 - 1, height - 11,
                    ready ? (fresh ? ArcadeTheme.LIME : 0xFF9DB2CC) : ArcadeTheme.DIM);
            if (isPromo(recipe)) {
                ArcadeTheme.star(context, x + slotWidth - 9, barTop + 8, 4, ArcadeTheme.GOLD);
            }
        }
        if (hoveredMenu >= 0) {
            int recipe = knownIndexAt(hoveredMenu);
            if (recipe >= 0) {
                String label = RECIPE_NAME[recipe] + "  •  R$ " + sellPrice(recipe)
                        + (isPromo(recipe) ? "  •  " + promoLabel : "")
                        + (freshnessTicks > 0 && SAVE.stock[recipe] > 0 && !isFresh(recipe)
                                ? "  •  " + staleWord : "  •  " + freshWord);
                int labelWidth = textRenderer.getWidth(label);
                ArcadeTheme.rounded(context, width / 2 - labelWidth / 2 - 5, barTop - 15,
                        width / 2 + labelWidth / 2 + 5, barTop - 1, 4, 0xE00A1220);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), width / 2,
                        barTop - 12, ArcadeTheme.TEXT);
            }
        }
        int infoX = startX + count * slotWidth + 4;
        if (infoX < width - 60) {
            context.drawTextWithShadow(textRenderer,
                    Text.literal(stationLabel + " " + activeBakes() + "/" + ovenSlots()),
                    infoX, barTop + 5, ACCENT_STATION);
            context.drawTextWithShadow(textRenderer, Text.literal("CLIENTES " + SAVE.customers.size()),
                    infoX, barTop + 16, ArcadeTheme.SKY);
        }
    }

    private int activeBakes() {
        int count = 0;
        for (int[] entry : SAVE.baking) {
            if (entry[1] > 0) count++;
        }
        return count;
    }

    private int menuAt(int mouseX, int mouseY) {
        int barTop = height - 32;
        if (mouseY < barTop + 3 || mouseY >= height - 2) return -1;
        int count = Math.max(1, knownCount());
        int slotWidth = menuSlotWidth();
        int slot = (mouseX - 4) / Math.max(1, slotWidth);
        if (slot < 0 || slot >= count || mouseX < 4) return -1;
        return slot;
    }

    /* ------------------------------------------------------------------ */
    /* Abas                                                               */
    /* ------------------------------------------------------------------ */

    private void drawOverlay(DrawContext context, int mouseX, int mouseY) {
        context.fill(0, 0, width, height, 0x99000000);
        int[] panel = panelRect();
        ArcadeTheme.rounded(context, panel[0] - 2, panel[1] - 2, panel[0] + panel[2] + 2,
                panel[1] + panel[3] + 2, 10, 0x66000000);
        ArcadeTheme.rounded(context, panel[0], panel[1], panel[0] + panel[2], panel[1] + panel[3],
                9, 0xFF0C1727);
        ArcadeTheme.rounded(context, panel[0] + 1, panel[1] + 1, panel[0] + panel[2] - 1,
                panel[1] + 22, 8, 0xFF16243A);
        String title = switch (overlay) {
            case CRAFT -> "BANCADA DE TRABALHO";
            case SHOP -> "LOJA DE INGREDIENTES";
            case UPGRADES -> "MELHORIAS";
            case COLLECTION -> "COLEÇÃO";
            case TEAM -> "EQUIPE E CLIENTES";
            default -> "";
        };
        context.drawTextWithShadow(textRenderer, Text.literal(title), panel[0] + 8, panel[1] + 7,
                ArcadeTheme.GOLD);
        String hint = "[E] fecha";
        int hintX = panel[0] + panel[2] - textRenderer.getWidth(hint) - 24;
        if (hintX > panel[0] + 60) {
            context.drawTextWithShadow(textRenderer, Text.literal(hint), hintX, panel[1] + 7,
                    ArcadeTheme.MUTED);
        }
        int closeX = panel[0] + panel[2] - 16;
        ArcadeTheme.disc(context, closeX, panel[1] + 11, 8, 0x66203048);
        ArcadeTheme.drawX(context, closeX, panel[1] + 11, 3, 1, ArcadeTheme.MUTED);
        switch (overlay) {
            case CRAFT -> drawCraft(context, mouseX, mouseY);
            case SHOP -> drawShopOverlay(context, mouseX, mouseY);
            case UPGRADES -> drawUpgrades(context, mouseX, mouseY);
            case COLLECTION -> drawCollection(context);
            case TEAM -> drawTeam(context, mouseX, mouseY);
            default -> {
            }
        }
    }

    private void drawCraft(DrawContext context, int mouseX, int mouseY) {
        int[] pantry = pantryArea();
        section(context, pantry[0], pantry[1] - 12, "DESPENSA");
        int cardWidth = pantry[2] / 3;
        for (int index = 0; index < ING.length; index++) {
            int x = pantry[0] + (index % 3) * cardWidth;
            int y = pantry[1] + (index / 3) * 29;
            int owned = SAVE.pantry[index];
            boolean hovered = mouseX >= x && mouseX < x + cardWidth - 2 && mouseY >= y
                    && mouseY < y + 26 && owned > 0;
            ArcadeTheme.rounded(context, x, y, x + cardWidth - 2, y + 26, 4,
                    hovered ? 0xFF2A3F5C : owned > 0 ? 0xCC1A2A40 : 0x88121D2C);
            ArcadeTheme.disc(context, x + 6, y + 6, 4, owned > 0 ? ING_COLOR[index] : 0xFF3A4250);
            String name = elide(ING[index], cardWidth - 10);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(name),
                    x + (cardWidth - 2) / 2 + 2, y + 4, owned > 0 ? ArcadeTheme.TEXT : ArcadeTheme.DIM);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("x" + owned),
                    x + (cardWidth - 2) / 2 + 2, y + 15, owned > 0 ? ArcadeTheme.LIME : ArcadeTheme.DIM);
        }
        int[] bench = benchArea();
        section(context, bench[0], bench[1] - 12, "BANCADA");
        int hintX = bench[0] + textRenderer.getWidth("BANCADA") + 6;
        if (bench[0] + bench[2] - hintX > 40) {
            context.drawTextWithShadow(textRenderer, Text.literal("[clique] usar"),
                    hintX, bench[1] - 12, ArcadeTheme.DIM);
        }
        int[] gridArea = craftGrid();
        int cellWidth = gridArea[2] / 3;
        int cellHeight = gridArea[3] / 3;
        ArcadeTheme.rounded(context, gridArea[0] - 4, gridArea[1] - 4, gridArea[0] + gridArea[2],
                gridArea[1] + gridArea[3], 5, 0xFF0A1422);
        for (int index = 0; index < SAVE.bench.length; index++) {
            int x = gridArea[0] + (index % 3) * cellWidth;
            int y = gridArea[1] + (index / 3) * cellHeight;
            int item = SAVE.bench[index];
            boolean hovered = mouseX >= x && mouseX < x + cellWidth - 1 && mouseY >= y
                    && mouseY < y + cellHeight - 1;
            ArcadeTheme.rounded(context, x, y, x + cellWidth - 1, y + cellHeight - 1, 3,
                    hovered ? 0xFF3A4F6C : 0xCC1A2A40);
            if (item >= 0) {
                ArcadeTheme.disc(context, x + (cellWidth - 1) / 2, y + 9, 5, ING_COLOR[item]);
                String name = elide(ING[item], cellWidth - 5);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(name),
                        x + (cellWidth - 1) / 2, y + 16, ArcadeTheme.TEXT);
            }
        }
        int[] result = resultSlot();
        int match = matchedRecipe();
        for (int step = 0; step < 3; step++) {
            int x = result[0] - 11 + step * 3;
            context.fill(x, result[1] + 11, x + 2, result[1] + 13, ArcadeTheme.DIM);
        }
        context.fill(result[0] - 6, result[1] + 10, result[0] - 3, result[1] + 14, ArcadeTheme.DIM);
        boolean resultHovered = inside(mouseX, mouseY, result);
        ArcadeTheme.rounded(context, result[0], result[1], result[0] + result[2],
                result[1] + result[3], 4, match >= 0 ? 0xFF6E9D3F : resultHovered ? 0xFF3A4F6C : 0xCC1A2A40);
        if (match >= 0) {
            ArcadeTheme.disc(context, result[0] + result[2] / 2, result[1] + result[3] / 2 - 2, 7,
                    RECIPE_COLOR[match]);
            ArcadeTheme.disc(context, result[0] + result[2] / 2 - 2, result[1] + result[3] / 2 - 4, 2,
                    0xFFB23A2E);
            if (!SAVE.known[match]) {
                ArcadeTheme.star(context, result[0] + result[2] - 3, result[1] + 3, 3, ArcadeTheme.GOLD);
            }
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("?"),
                    result[0] + result[2] / 2, result[1] + 9, ArcadeTheme.DIM);
        }
        String resultName = match >= 0
                ? elide(RECIPE_NAME[match] + (SAVE.known[match] ? "" : " (nova!)"), result[2] + 24)
                : "resultado";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(resultName),
                result[0] + result[2] / 2, result[1] + result[3] + 3,
                match >= 0 ? ArcadeTheme.LIME : ArcadeTheme.DIM);
        int[] bake = bakeButton();
        boolean hovered = inside(mouseX, mouseY, bake);
        ArcadeTheme.rounded(context, bake[0], bake[1], bake[0] + bake[2], bake[1] + bake[3], 5,
                hovered ? ArcadeTheme.LIME : 0xFF6E9D3F);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(bakeAction),
                bake[0] + bake[2] / 2, bake[1] + 6, 0xFF0A1220);
        int[] clear = clearButton();
        ArcadeTheme.rounded(context, clear[0], clear[1], clear[0] + clear[2], clear[1] + clear[3], 5,
                inside(mouseX, mouseY, clear) ? 0xFFB06A5F : 0xFF7A4A40);
        if (clear[2] > 20) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("LIMPAR"),
                    clear[0] + clear[2] / 2, clear[1] + 6, ArcadeTheme.TEXT);
        }
        int[] panel = panelRect();
        int infoY = clear[1] + clear[3] + 5;
        context.drawTextWithShadow(textRenderer,
                Text.literal("Receitas: " + knownCount() + "/" + RECIPE_NAME.length),
                bench[0], infoY, ArcadeTheme.MUTED);
        context.drawTextWithShadow(textRenderer,
                Text.literal(elide("Monte livremente: a combinação certa revela um sabor novo.",
                        panel[2] - 16)),
                pantry[0], panel[1] + panel[3] - 14, ArcadeTheme.DIM);
    }

    private void drawShopOverlay(DrawContext context, int mouseX, int mouseY) {
        int[] grid = shopArea();
        int cardWidth = grid[2] / 4;
        for (int index = 0; index < ING.length; index++) {
            int x = grid[0] + (index % 4) * cardWidth;
            int y = grid[1] + (index / 4) * 34;
            boolean hovered = mouseX >= x && mouseX < x + cardWidth - 3 && mouseY >= y
                    && mouseY < y + 30;
            ArcadeTheme.rounded(context, x, y, x + cardWidth - 3, y + 30, 4,
                    hovered ? 0xFF2A3F5C : 0xCC1A2A40);
            ArcadeTheme.disc(context, x + 10, y + 15, 6, ING_COLOR[index]);
            String name = elide(ING[index], cardWidth - 24);
            context.drawTextWithShadow(textRenderer, Text.literal(name), x + 19, y + 5, ArcadeTheme.TEXT);
            context.drawTextWithShadow(textRenderer, Text.literal("R$ " + ING_PRICE[index]), x + 19,
                    y + 16, ArcadeTheme.GOLD);
            context.drawTextWithShadow(textRenderer, Text.literal("x" + SAVE.pantry[index]),
                    x + cardWidth - 18, y + 16, ArcadeTheme.LIME);
        }
        int[] panel = panelRect();
        context.drawTextWithShadow(textRenderer,
                Text.literal("Clique compra 1  •  Shift + clique compra 5"),
                grid[0], panel[1] + panel[3] - 28, ArcadeTheme.DIM);
        context.drawTextWithShadow(textRenderer, Text.literal("Caixa: R$ " + SAVE.money),
                grid[0], panel[1] + panel[3] - 15, ArcadeTheme.MUTED);
    }

    private void drawUpgrades(DrawContext context, int mouseX, int mouseY) {
        int[] base = listArea();
        String[] names = {stationCap + " extra", "Melhorar " + shopWord, "Melhorar Steve"};
        String[] details = {
                "Capacidade: " + ovenSlots() + " → " + (ovenSlots() + 1) + "  •  " + (bakeTicks() / 20) + "s → " + (Math.max(70, 150 - (SAVE.ovenLevel + 1) * 12) / 20) + "s",
                "Preços e paciência: nível " + (SAVE.pizzeriaLevel + 1) + "/5",
                "Velocidade do Steve: nível " + (SAVE.steveLevel + 1) + "/4"
        };
        int[] costs = {ovenCost(), pizzeriaCost(), steveCost()};
        int[] levels = {SAVE.ovenLevel, SAVE.pizzeriaLevel, SAVE.steveLevel};
        int[] maxes = {4, 5, 4};
        for (int index = 0; index < 3; index++) {
            int y = base[1] + index * 40;
            boolean hovered = mouseX >= base[0] && mouseX < base[0] + base[2] && mouseY >= y
                    && mouseY < y + 34;
            ArcadeTheme.rounded(context, base[0], y, base[0] + base[2], y + 34, 6,
                    hovered ? 0xFF2A3F5C : 0xCC1A2A40);
            context.drawTextWithShadow(textRenderer, Text.literal(names[index]), base[0] + 8, y + 5,
                    ArcadeTheme.TEXT);
            context.drawTextWithShadow(textRenderer,
                    Text.literal(levels[index] >= maxes[index] ? "Máximo alcançado" : details[index]),
                    base[0] + 8, y + 17, ArcadeTheme.MUTED);
            String cost = levels[index] >= maxes[index] ? "MAX" : "R$ " + costs[index];
            context.drawTextWithShadow(textRenderer, Text.literal(cost),
                    base[0] + base[2] - textRenderer.getWidth(cost) - 8, y + 11,
                    levels[index] >= maxes[index] ? ArcadeTheme.LIME : ArcadeTheme.GOLD);
        }
        int footer = base[1] + 3 * 40 + 6;
        context.drawTextWithShadow(textRenderer,
                Text.literal(shopWordCap + ": preços e paciência  •  " + stationCap + ": +1 por vez"),
                base[0], footer, ArcadeTheme.DIM);
    }

    private void drawCollection(DrawContext context) {
        int[] panel = panelRect();
        int discovered = 0;
        for (boolean value : SAVE.collection) if (value) discovered++;
        context.drawTextWithShadow(textRenderer,
                Text.literal("Itens deixados por clientes: " + discovered + "/" + COLLECTIBLE.length),
                panel[0] + 8, panel[1] + 28, ArcadeTheme.MUTED);
        int gridWidth = panel[2] - 16;
        int cardWidth = gridWidth / 4;
        for (int index = 0; index < COLLECTIBLE.length; index++) {
            int x = panel[0] + 8 + (index % 4) * cardWidth;
            int y = panel[1] + 40 + (index / 4) * 58;
            ArcadeTheme.rounded(context, x, y, x + cardWidth - 4, y + 52, 5,
                    SAVE.collection[index] ? 0xCC1F3A52 : 0x88121D2C);
            if (SAVE.collection[index]) {
                ArcadeTheme.disc(context, x + cardWidth / 2 - 2, y + 18, 7, COLLECTIBLE_COLOR[index]);
                ArcadeTheme.disc(context, x + cardWidth / 2 - 4, y + 15, 2, 0x88FFFFFF);
            } else {
                ArcadeTheme.ring(context, x + cardWidth / 2 - 2, y + 18, 7, 1, 0xFF3A4250);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("?"),
                        x + cardWidth / 2 - 2, y + 14, ArcadeTheme.DIM);
            }
            String name = SAVE.collection[index]
                    ? elide(COLLECTIBLE[index], cardWidth - 8) : "???";
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(name),
                    x + cardWidth / 2 - 2, y + 31, SAVE.collection[index] ? ArcadeTheme.TEXT : ArcadeTheme.DIM);
        }
        context.drawTextWithShadow(textRenderer,
                Text.literal("Clientes especiais deixam lembranças ao serem atendidos."),
                panel[0] + 8, panel[1] + panel[3] - 16, ArcadeTheme.DIM);
    }

    private void drawTeam(DrawContext context, int mouseX, int mouseY) {
        int[] base = listArea();
        for (int index = 0; index < EMPLOYEE_NAME.length; index++) {
            int y = base[1] + index * 23;
            boolean hired = SAVE.hired[index];
            boolean locked = SAVE.level < EMPLOYEE_LEVEL[index];
            boolean hovered = mouseX >= base[0] && mouseX < base[0] + base[2] && mouseY >= y
                    && mouseY < y + 21;
            ArcadeTheme.rounded(context, base[0], y, base[0] + base[2], y + 21, 5,
                    hovered ? 0xFF2A3F5C : 0xCC1A2A40);
            ArcadeTheme.disc(context, base[0] + 9, y + 10, 5,
                    EMPLOYEE_CHEF[index] ? ArcadeTheme.CORAL : ArcadeTheme.SKY);
            String detail = hired ? "ATIVO  •  " + (EMPLOYEE_SPEED[index] / 20) + "s"
                    : locked ? "NÍVEL " + EMPLOYEE_LEVEL[index] : "R$ " + EMPLOYEE_COST[index];
            int detailWidth = textRenderer.getWidth(detail);
            int nameSpace = base[2] - 24 - detailWidth - 8;
            String name = EMPLOYEE_NAME[index] + (EMPLOYEE_CHEF[index] ? " (" + chefRole + ")" : " (" + waiterRole + ")");
            context.drawTextWithShadow(textRenderer, Text.literal(elide(name, nameSpace)),
                    base[0] + 18, y + 6, hired ? ArcadeTheme.LIME : ArcadeTheme.TEXT);
            context.drawTextWithShadow(textRenderer, Text.literal(detail),
                    base[0] + base[2] - detailWidth - 7, y + 6,
                    hired ? ArcadeTheme.LIME : locked ? ArcadeTheme.DANGER : ArcadeTheme.GOLD);
        }
        int footer = base[1] + EMPLOYEE_NAME.length * 23 + 3;
        context.drawTextWithShadow(textRenderer,
                Text.literal("Clientes especiais: " + SPECIAL_CUSTOMER.length
                        + " tipos que pagam 50% a mais."),
                base[0], footer, ArcadeTheme.MUTED);
    }

    /** Guarda os pixels já desenhados; ao voltar, eles são recarregados aos poucos. */
    private void saveWorld() {
        if (!LOADED.isEmpty()) {
            SAVE.savedChunks.addAll(LOADED);
        }
    }

    @Override
    public void close() {
        saveWorld();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
