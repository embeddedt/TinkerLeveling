package org.embeddedt.tinkerleveling;

import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class ToolHelper {
    public static boolean isEqualTinkersItem(ItemStack item1, ItemStack item2) {
        if(item1 == null || item2 == null || item1.getItem() != item2.getItem()) {
            return false;
        }
        ToolStack stack1 = ToolStack.from(item1);
        ToolStack stack2 = ToolStack.from(item2);
        return stack1.getModifiers().equals(stack2.getModifiers()) &&
                stack1.getMaterials().equals(stack2.getMaterials());
    }
}
