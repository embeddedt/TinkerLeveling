package org.embeddedt.tinkerleveling.capability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import org.embeddedt.tinkerleveling.ModToolLeveling;
import org.embeddedt.tinkerleveling.TinkerLeveling;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DamageXp implements IDamageXp {
    private static String TAG_PLAYER_UUID = "player_uuid";
    private static String TAG_DAMAGE_LIST = "damage_data";
    private static String TAG_ITEM = "item";
    private static String TAG_DAMAGE = "damage";

    private Map<UUID, Map<UUID, Float>> playerToDamageMap = new HashMap<>();

    @Override
    public void addDamageFromTool(float damage, UUID tool, PlayerEntity player) {
        Map<UUID, Float> damageMap = playerToDamageMap.getOrDefault(player.getUUID(), new HashMap<>());

        damage += getDamageDealtByTool(tool, player);

        damageMap.put(tool, damage);
        playerToDamageMap.put(player.getUUID(), damageMap);
    }

    @Override
    public float getDamageDealtByTool(UUID tool, PlayerEntity player) {
        Map<UUID, Float> damageMap = playerToDamageMap.getOrDefault(player.getUUID(), new HashMap<>());

        return damageMap.entrySet().stream()
                .filter(itemStackFloatEntry -> tool.equals(itemStackFloatEntry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(0f);
    }

    @Override
    public void distributeXpToTools(LivingEntity deadEntity) {
        playerToDamageMap.forEach((uuid, itemStackFloatMap) -> distributeXpForPlayer(deadEntity.level, uuid, itemStackFloatMap));
    }

    private void distributeXpForPlayer(World world, UUID playerUuid, Map<UUID, Float> damageMap) {
        Optional.ofNullable(world.getPlayerByUUID(playerUuid))
                .ifPresent(
                        player -> damageMap.forEach(
                                (itemStack, damage) -> distributeXpToPlayerForTool(player, itemStack, damage)
                        )
                );
    }

    private void distributeXpToPlayerForTool(PlayerEntity player, UUID toolUUID, float damage) {
        if(toolUUID != null) {
            player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(itemHandler -> {
                // check for identity. should work in most cases because the entity was killed without loading/unloading
                for(int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if(stack.getItem() instanceof IModifiable) {
                        ToolStack tool = ToolStack.from(stack);
                        if(tool.getPersistentData().contains(ModToolLeveling.UUID_KEY, Constants.NBT.TAG_INT_ARRAY)) {
                            if(NBTUtil.loadUUID(tool.getPersistentData().get(ModToolLeveling.UUID_KEY)).equals(toolUUID)) {
                                TinkerLeveling.LEVELING_MODIFIER.get().addXp(tool, Math.round(damage), player);
                                return;
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public ListNBT serializeNBT() {
        ListNBT playerList = new ListNBT();

        playerToDamageMap.forEach((uuid, itemStackFloatMap) -> playerList.add(convertPlayerDataToTag(uuid, itemStackFloatMap)));

        return playerList;
    }

    private CompoundNBT convertPlayerDataToTag(UUID uuid, Map<UUID, Float> itemStackFloatMap) {
        CompoundNBT tag = new CompoundNBT();
        tag.putUUID(TAG_PLAYER_UUID, uuid);

        ListNBT damageTag = new ListNBT();

        itemStackFloatMap.forEach((itemStack, damage) -> damageTag.add(convertItemDamageDataToTag(itemStack, damage)));

        tag.put(TAG_DAMAGE_LIST, damageTag);
        return tag;
    }

    private CompoundNBT convertItemDamageDataToTag(UUID stack, Float damage) {
        CompoundNBT tag = new CompoundNBT();

        tag.put(TAG_ITEM, NBTUtil.createUUID(stack));
        tag.putFloat(TAG_DAMAGE, damage);

        return tag;
    }


    @Override
    public void deserializeNBT(ListNBT nbt) {
        playerToDamageMap = new HashMap<>();
        for(int i = 0; i < nbt.size(); i++) {
            CompoundNBT tag = nbt.getCompound(i);

            UUID playerUuid = tag.getUUID(TAG_PLAYER_UUID);
            ListNBT data = tag.getList(TAG_DAMAGE_LIST, 10);

            Map<UUID, Float> damageMap = new HashMap<>();

            for(int j = 0; j < data.size(); j++) {
                deserializeTagToMapEntry(damageMap, data.getCompound(j));
            }

            playerToDamageMap.put(playerUuid, damageMap);
        }
    }

    private void deserializeTagToMapEntry(Map<UUID, Float> damageMap, CompoundNBT tag) {
        UUID stack = NBTUtil.loadUUID(tag.get(TAG_ITEM));
        damageMap.put(stack, tag.getFloat(TAG_DAMAGE));
    }
}
