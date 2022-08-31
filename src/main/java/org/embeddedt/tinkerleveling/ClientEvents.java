package org.embeddedt.tinkerleveling;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.awt.*;
import java.util.List;

@Mod.EventBusSubscriber(modid = TinkerLeveling.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {
    @SubscribeEvent
    static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltips = event.getToolTip();
        ToolStack tool = ToolStack.from(stack);
        if(tool.getModifierLevel(TinkerLeveling.LEVELING_MODIFIER.getId()) > 0) {
            ModDataNBT levelData = tool.getPersistentData();
            int xp = levelData.getInt(ModToolLeveling.XP_KEY);
            int level = levelData.getInt(ModToolLeveling.LEVEL_KEY);
            tooltips.add(1, new TranslatableComponent("tooltip.tinkerleveling.xp").append(": ").append(new TextComponent(String.format("%d / %d", xp, ModToolLeveling.getXpForLevelup(level, stack.getItem())))));
            tooltips.add(1, getLevelTooltip(level));
        }
    }

    private static Component getLevelTooltip(int level) {
        return new TranslatableComponent("tooltip.tinkerleveling.level").append(": ").append(getLevelString(level));
    }

    public static Component getLevelString(int level) {
        return new TextComponent(getRawLevelString(level)).withStyle(style -> style.withColor(getLevelColor(level)));
    }

    private static String getRawLevelString(int level) {
        if(level <= 0) {
            return "";
        }

        // try a basic translated string
        if(I18n.exists("tooltip.tinkerleveling.level." + level)) {
            return I18n.get("tooltip.tinkerleveling.level." + level);
        }

        // ok. try to find a modulo
        int i = 1;
        while(I18n.exists("tooltip.tinkerleveling.level." + i)) {
            i++;
        }

        // get the modulo'd string
        String str = I18n.get("tooltip.level." + (level % i));
        // and add +s!
        for(int j = level / i; j > 0; j--) {
            str += '+';
        }

        return str;
    }

    private static int getLevelColor(int level) {
        float hue = (0.277777f * level);
        hue = hue - (int) hue;
        return Color.HSBtoRGB(hue, 0.75f, 0.8f);
    }
}
