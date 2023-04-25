package org.embeddedt.tinkerleveling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;

@Mod.EventBusSubscriber(modid = TinkerLeveling.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {
    @SubscribeEvent
    static void onTooltipEvent(ItemTooltipEvent event) {
        if(Minecraft.getInstance().level == null)
            return;
        ItemStack stack = event.getItemStack();
        if(!stack.getItem().is(TinkerTags.Items.MODIFIABLE))
            return;
        List<ITextComponent> tooltips = event.getToolTip();
        ToolStack tool = ToolStack.copyFrom(stack);
        if(tool.getModifierLevel(TinkerLeveling.LEVELING_MODIFIER.get()) > 0) {
            ModDataNBT levelData = tool.getPersistentData();
            int xp = levelData.getInt(ModToolLeveling.XP_KEY);
            int level = levelData.getInt(ModToolLeveling.LEVEL_KEY);
            tooltips.add(1, new TranslationTextComponent("tooltip.tinkerleveling.xp").append(": ").append(new StringTextComponent(String.format("%d / %d", xp, ModToolLeveling.getXpForLevelup(level, stack.getItem())))));
            tooltips.add(1, getLevelTooltip(level));
        }
    }

    private static ITextComponent getLevelTooltip(int level) {
        return new TranslationTextComponent("tooltip.tinkerleveling.level").append(": ").append(getLevelString(level));
    }

    public static ITextComponent getLevelString(int level) {
        return new StringTextComponent(getRawLevelString(level)).withStyle(style -> style.withColor(Color.fromRgb(getLevelColor(level))));
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
        return java.awt.Color.HSBtoRGB(hue, 0.75f, 0.8f);
    }
}
