package org.embeddedt.tinkerleveling.capability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public interface IDamageXp extends INBTSerializable<ListNBT> {
    void addDamageFromTool(float damage, UUID tool, PlayerEntity player);

    float getDamageDealtByTool(UUID tool, PlayerEntity player);

    void distributeXpToTools(LivingEntity deadEntity);
}
