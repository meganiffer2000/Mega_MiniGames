package br.com.megaminigames;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Inicialização principal do mod Mega MiniGames. */
public final class MegaMiniGames implements ModInitializer {
    public static final String MOD_ID = "mega_minigames";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Item RETRO_CONSOLE = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "retro_console"),
            new RetroConsoleItem(new Item.Settings().maxCount(1).fireproof())
    );

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(RETRO_CONSOLE));
        LOGGER.info("Mega MiniGames carregado. Use o Console Retrô do mod para abrir o hub.");
    }
}
