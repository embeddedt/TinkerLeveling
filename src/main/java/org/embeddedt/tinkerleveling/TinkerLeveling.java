package org.embeddedt.tinkerleveling;

import com.mojang.logging.LogUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.embeddedt.tinkerleveling.capability.CapabilityDamageXp;
import org.slf4j.Logger;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
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
    private static final ResourceLocation TOOL_CAP_KEY = new ResourceLocation(TinkerLeveling.MODID, "toolxp");

    @SubscribeEvent
    public void attachEntityDamageCap(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof LivingEntity && event.getObject().isAlive()) {
            event.addCapability(CAPABILITY_KEY, new CapabilityDamageXp());
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if(!event.getEntity().getLevel().isClientSide) {
            event.getEntity().getCapability(CapabilityDamageXp.CAPABILITY, null).ifPresent(cap -> {
                cap.distributeXpToTools(event.getEntityLiving());
            });
        }
    }

    private void processInvList(NonNullList<ItemStack> items) {
        for(ItemStack itemStack : items) {
            if(!itemStack.isEmpty() && itemStack.getItem() instanceof ModifiableItem) {
                ToolStack tool = ToolStack.from(itemStack);
                if(tool.getModifierLevel(LEVELING_MODIFIER.getId()) == 0) {
                    tool.addModifier(LEVELING_MODIFIER.getId(), 1);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(!event.player.getLevel().isClientSide) {
            Inventory inventory = event.player.getInventory();
            processInvList(inventory.items);
            processInvList(inventory.armor);
            processInvList(inventory.offhand);
        }
    }

    public static SoundEvent SOUND_LEVELUP = sound("levelup");

    private static SoundEvent sound(String name) {
        ResourceLocation location = new ResourceLocation(TinkerLeveling.MODID, name);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(location);
        return event;
    }

    @SubscribeEvent
    public static void registerSoundEvent(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(SOUND_LEVELUP);
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // Register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
