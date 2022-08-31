package org.embeddedt.tinkerleveling.capability;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public interface IDamageXp extends INBTSerializable<ListTag> {
    void addDamageFromTool(float damage, UUID tool, Player player);

    float getDamageDealtByTool(UUID tool, Player player);

    void distributeXpToTools(LivingEntity deadEntity);
}
