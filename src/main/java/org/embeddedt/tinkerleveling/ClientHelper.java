package org.embeddedt.tinkerleveling;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.DistExecutor;

public class ClientHelper {

    public static void playLevelupDing(Player player) {
        player.playSound(TinkerLeveling.SOUND_LEVELUP, 1f, 1f);
    }

    public static void sendLevelUpMessage(int level, Player player) {
        Component textComponent;
        // special message
        if(I18n.exists("message.levelup." + level)) {
            textComponent = new TranslatableComponent("message.levelup." + level, "tool").withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA));
        }
        // generic message
        else {
            textComponent = new TranslatableComponent("message.levelup.generic", "tool").append(ClientEvents.getLevelString(level));
        }
        player.sendMessage(textComponent, Util.NIL_UUID);
    }

}
