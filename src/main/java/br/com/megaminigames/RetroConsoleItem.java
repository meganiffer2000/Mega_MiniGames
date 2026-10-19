package br.com.megaminigames;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Console Retrô: item de acesso ao hub. A dica (tooltip) é traduzida pelo jogo
 * e aparece em português, inglês ou espanhol conforme o idioma escolhido.
 */
public final class RetroConsoleItem extends Item {
    public RetroConsoleItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.mega_minigames.retro_console.tip1"));
        tooltip.add(Text.translatable("item.mega_minigames.retro_console.tip2"));
        tooltip.add(Text.translatable("item.mega_minigames.retro_console.tip3"));
        tooltip.add(Text.translatable("item.mega_minigames.retro_console.tip4"));
    }
}
