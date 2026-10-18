package br.com.megaminigames;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

/** Ponto de entrada principal do mod Mega MiniGames. */
public final class MegaMiniGames implements ModInitializer {
    public static final String MOD_ID = "mega_minigames";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier OPEN_GAMES_HUB_PACKET = new Identifier(MOD_ID, "open_games_hub");

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("mmg")
                        .then(literal("games_hub").executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrException();
                            ServerPlayNetworking.send(player, OPEN_GAMES_HUB_PACKET, PacketByteBufs.empty());
                            return Command.SINGLE_SUCCESS;
                        }))));

        LOGGER.info("Mega MiniGames carregado. Use /mmg games_hub para abrir o menu de jogos.");
    }
}
