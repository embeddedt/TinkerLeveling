package org.embeddedt.tinkerleveling;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.embeddedt.tinkerleveling.capability.CapabilityDamageXp;
import slimeknights.tconstruct.common.SoundUtils;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.hooks.IHarvestModifier;
import slimeknights.tconstruct.library.modifiers.hooks.IShearModifier;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.RestrictedCompoundTag;
import slimeknights.tconstruct.tools.ToolDefinitions;

import javax.annotation.Nullable;
import java.util.UUID;

public class ModToolLeveling extends Modifier implements IHarvestModifier, IShearModifier {

    public static final ResourceLocation XP_KEY = new ResourceLocation(TinkerLeveling.MODID, "xp");
    public static final ResourceLocation BONUS_MODIFIERS_KEY = new ResourceLocation(TinkerLeveling.MODID, "bonus_modifiers");
    public static final ResourceLocation LEVEL_KEY = new ResourceLocation(TinkerLeveling.MODID, "level");
    public static final ResourceLocation UUID_KEY = new ResourceLocation(TinkerLeveling.MODID, "uuid");

    @Override
    public boolean shouldDisplay(boolean advanced) {
        return false;
    }

    public ModToolLeveling() {
        super();
    }


    @Override
    public void addVolatileData(ToolRebuildContext context, int level, ModDataNBT volatileData) {
        IModDataView persistentData = context.getPersistentData();
        int numExtraModifiers = persistentData.getInt(BONUS_MODIFIERS_KEY);
        int numAbilitySlots = (numExtraModifiers / 2);
        volatileData.addSlots(SlotType.ABILITY, numAbilitySlots);
        volatileData.addSlots(SlotType.UPGRADE, numExtraModifiers - numAbilitySlots);
    }

    @Override
    public void onRemoved(IToolStackView tool) {
        tool.getPersistentData().remove(XP_KEY);
        tool.getPersistentData().remove(BONUS_MODIFIERS_KEY);
        tool.getPersistentData().remove(LEVEL_KEY);
        tool.getPersistentData().remove(UUID_KEY);
    }

    @Override
    public void addRawData(IToolStackView tool, int level, RestrictedCompoundTag tag) {
        if(!tool.getPersistentData().contains(UUID_KEY, Tag.TAG_INT_ARRAY)) {
            tool.getPersistentData().put(UUID_KEY, NbtUtils.createUUID(UUID.randomUUID()));
        }
        if(tool.getPersistentData().getInt(LEVEL_KEY) <= 0) {
            tool.getPersistentData().putInt(LEVEL_KEY, 1);
        }
    }

    public static int getXpForLevelup(int level, Item item) {
        if(level <= 1) {
            return TinkerConfig.getBaseXpForTool(item);
        }
        return (int) ((double) getXpForLevelup(level - 1, item) * TinkerConfig.levelMultiplier.get());
    }

    public void addXp(IToolStackView tool, int amount, Player player) {
        ModDataNBT levelData = tool.getPersistentData();

        // is max level?
        if(!TinkerConfig.canLevelUp(levelData.getInt(LEVEL_KEY))) {
            return;
        }

        levelData.putInt(XP_KEY, levelData.getInt(XP_KEY) + amount);

        int xpForLevelup = getXpForLevelup(levelData.getInt(LEVEL_KEY), tool.getItem());

        boolean leveledUp = false;
        // check for levelup
        if(levelData.getInt(XP_KEY) >= xpForLevelup) {
            levelData.putInt(XP_KEY, levelData.getInt(XP_KEY) - xpForLevelup);
            levelData.putInt(LEVEL_KEY, levelData.getInt(LEVEL_KEY) + 1);
            levelData.putInt(BONUS_MODIFIERS_KEY, levelData.getInt(BONUS_MODIFIERS_KEY) + 1);
            leveledUp = true;
        }

        if(leveledUp) {
            if(!player.getLevel().isClientSide) {
                SoundUtils.playSoundForPlayer(player, TinkerLeveling.SOUND_LEVELUP, 1f, 1f);
                TinkerPacketHandler.sendLevelUp(levelData.getInt(LEVEL_KEY), player);
            }
            /* FIXME: no other way of doing this that I see */
            if(tool instanceof ToolStack) {
                ((ToolStack)tool).rebuildStats();
            } else {
                throw new IllegalStateException("Unable to figure out how to rebuild this tool!");
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getModule(Class<T> type) {
        if (type == IHarvestModifier.class || type == IShearModifier.class) {
            return (T) this;
        }
        return null;
    }

    /* Handlers */

    @Override
    public void afterBlockBreak(IToolStackView tool, int level, ToolHarvestContext context) {
        if(context.isEffective() && context.getPlayer() != null) {
            addXp(tool, 1, context.getPlayer());
        }
    }

    @Override
    public void onAttacked(IToolStackView tool, int level, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        if(!(context.getEntity() instanceof Player player))
            return;
        boolean wasMobDamage = source.getEntity() != player && source.getEntity() instanceof LivingEntity;
        if(isDirectDamage
                && (wasMobDamage || TinkerConfig.allowArmorExploits.get())
                && slotType.getType() == EquipmentSlot.Type.ARMOR /* only level armor */
                && !player.getLevel().isClientSide) {
            addXp(tool, 1, player);
        }
    }

    @Override
    public int afterEntityHit(IToolStackView tool, int level, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if(target == null)
            return 0;
        if(!context.getTarget().getLevel().isClientSide && context.getPlayerAttacker() != null) {
            // if we killed it the event for distributing xp was already fired and we just do it manually here
            if(!context.getTarget().isAlive()) {
                addXp(tool, Math.round(damageDealt), context.getPlayerAttacker());
            }
            else {
                target.getCapability(CapabilityDamageXp.CAPABILITY, null).ifPresent(cap -> {
                    cap.addDamageFromTool(damageDealt, NbtUtils.loadUUID(tool.getPersistentData().get(UUID_KEY)), context.getPlayerAttacker());
                });
            }
        }
        return 0;
    }

    @Override
    public void afterHarvest(IToolStackView tool, int level, UseOnContext context, ServerLevel world, BlockState state, BlockPos pos) {
        if(context.getPlayer() != null)
            addXp(tool, 1, context.getPlayer());
    }

    @Override
    public void afterShearEntity(IToolStackView tool, int level, Player player, Entity entity, boolean isTarget) {
        addXp(tool, 1, player);
    }
}