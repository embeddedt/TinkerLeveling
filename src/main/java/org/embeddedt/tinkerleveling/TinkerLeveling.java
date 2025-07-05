package org.embeddedt.tinkerleveling;

import com.mojang.logging.LogUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.embeddedt.tinkerleveling.capability.CapabilityDamageXp;
import org.slf4j.Logger;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TinkerLeveling.MODID)
public class TinkerLeveling {

    public static final String MODID = "tinkerleveling";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    protected static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(TinkerLeveling.MODID);
    public static StaticModifier<ModToolLeveling> LEVELING_MODIFIER = MODIFIERS.register("leveling", ModToolLeveling::new);

    public static TinkerLeveling instance;

    public TinkerLeveling() {

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerModifiers);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TinkerConfig.SERVER_CONFIG);
        TinkerPacketHandler.register();

        instance = this;
    }

    public void registerModifiers(final FMLCommonSetupEvent event) {
        MODIFIERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static final ResourceLocation CAPABILITY_KEY = new ResourceLocation(TinkerLeveling.MODID, "entityxp");

    @SubscribeEvent
    public void attachEntityDamageCap(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof LivingEntity && event.getObject().isAlive()) {
            event.addCapability(CAPABILITY_KEY, new CapabilityDamageXp());
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if(!event.getEntity().level().isClientSide) {
            event.getEntity().getCapability(CapabilityDamageXp.CAPABILITY, null).ifPresent(cap -> {
                cap.distributeXpToTools(event.getEntity());
            });
        }
    }

    private void processInvList(NonNullList<ItemStack> items) {
        for(ItemStack itemStack : items) {
            if(itemStack.is(TinkerTags.Items.MODIFIABLE)) {
                if(ModifierUtil.getModifierLevel(itemStack, LEVELING_MODIFIER.getId()) == 0) {
                    ToolStack tool = ToolStack.from(itemStack);
                    tool.addModifier(LEVELING_MODIFIER.getId(), 1);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        /* TODO: replace with tool building event if/when Tinkers adds one */
        if(!event.player.level().isClientSide) {
            Inventory inventory = event.player.getInventory();
            processInvList(inventory.items);
            processInvList(inventory.armor);
            processInvList(inventory.offhand);
        }
    }

    public static ResourceLocation SOUND_LEVELUP_LOCATION = new ResourceLocation(TinkerLeveling.MODID, "levelup");
    public static SoundEvent SOUND_LEVELUP = SoundEvent.createVariableRangeEvent(SOUND_LEVELUP_LOCATION);

    @SubscribeEvent
    public static void registerSoundEvent(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.SOUND_EVENTS, helper -> {
            helper.register(SOUND_LEVELUP_LOCATION, SOUND_LEVELUP);
        });
    }
}
