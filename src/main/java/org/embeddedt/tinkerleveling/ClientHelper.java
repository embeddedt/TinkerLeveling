package org.embeddedt.tinkerleveling;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ClientHelper {

    public static void playLevelupDing(Player player) {

    }

    public static void sendLevelUpMessage(int level) {
        Component textComponent;
        // special message
        if(I18n.exists("message.levelup." + level)) {
            textComponent = Component.translatable("message.levelup." + level, "tool").withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA));
        }
        // generic message
        else {
            textComponent = Component.translatable("message.levelup.generic", "tool").append(ClientEvents.getLevelString(level));
        }
        Minecraft.getInstance().player.sendSystemMessage(textComponent);
    }

}
