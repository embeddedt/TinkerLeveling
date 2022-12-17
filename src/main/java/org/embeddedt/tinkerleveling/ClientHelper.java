package org.embeddedt.tinkerleveling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ClientHelper {

    public static void playLevelupDing(PlayerEntity player) {

    }

    public static void sendLevelUpMessage(int level) {
        ITextComponent textComponent;
        // special message
        if(I18n.exists("message.levelup." + level)) {
            textComponent = new TranslationTextComponent("message.levelup." + level, "tool").withStyle(style -> style.withColor(TextFormatting.DARK_AQUA));
        }
        // generic message
        else {
            textComponent = new TranslationTextComponent("message.levelup.generic", "tool").append(ClientEvents.getLevelString(level));
        }
        Minecraft.getInstance().player.sendMessage(textComponent, Util.NIL_UUID);
    }

}
