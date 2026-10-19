package br.com.megaminigames;

import br.com.megaminigames.client.GamesHubScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.TypedActionResult;

/** Inicialização cliente: usar o Console Retrô abre o hub, sem comandos de chat. */
public final class MegaMiniGamesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            var stack = player.getStackInHand(hand);
            if (!stack.isOf(MegaMiniGames.RETRO_CONSOLE)) return TypedActionResult.pass(stack);
            if (world.isClient) {
                MinecraftClient.getInstance().setScreen(player.isSneaking()
                        ? GamesHubScreen.randomGame()
                        : new GamesHubScreen());
            }
            return TypedActionResult.success(stack);
        });
    }
}
