package org.embeddedt.tinkerleveling.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CapabilityDamageXp implements ICapabilityProvider, INBTSerializable<ListTag> {

  public static Capability<DamageXp> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

  private DamageXp damageXp = null;
  private final LazyOptional<DamageXp> opt = LazyOptional.of(this::createDamageXp);

  @Nonnull
  private DamageXp createDamageXp() {
    if (damageXp == null) {
      damageXp = new DamageXp();
    }
    return damageXp;
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
    if (cap == CAPABILITY) {
      return opt.cast();
    }
    return LazyOptional.empty();
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    return getCapability(cap);
  }

  @Override
  public ListTag serializeNBT() {
    return createDamageXp().serializeNBT();
  }

  @Override
  public void deserializeNBT(ListTag nbt) {
    createDamageXp().deserializeNBT(nbt);
  }
}
