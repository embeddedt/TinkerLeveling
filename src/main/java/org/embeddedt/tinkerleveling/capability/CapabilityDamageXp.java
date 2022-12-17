package org.embeddedt.tinkerleveling.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CapabilityDamageXp implements Capability.IStorage<DamageXp> {
  @CapabilityInject(DamageXp.class)
  public static Capability<DamageXp> CAPABILITY = null;

  public static void register() {
    CapabilityManager.INSTANCE.register(DamageXp.class, new CapabilityDamageXp(), DamageXp::new);
  }

  @Override
  public void readNBT(Capability<DamageXp> cap, DamageXp instance, Direction side, INBT nbt) {
    instance.deserializeNBT((ListNBT)nbt);
  }

  @Nullable
  @Override
  public INBT writeNBT(Capability<DamageXp> capability, DamageXp instance, Direction side) {
    return instance.serializeNBT();
  }
}
