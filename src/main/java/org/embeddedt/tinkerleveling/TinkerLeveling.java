package org.embeddedt.tinkerleveling;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.embeddedt.tinkerleveling.capability.CapabilityDamageXp;
import org.embeddedt.tinkerleveling.capability.DamageXp;
import org.embeddedt.tinkerleveling.capability.SimplePersistentCapabilityProvider;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TinkerLeveling.MODID)
public class TinkerLeveling {

    public static final String MODID = "tinkerleveling";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogManager.getLogger();

    protected static final DeferredRegister<Modifier> MODIFIERS = DeferredRegister.create(Modifier.class, TinkerLeveling.MODID);
    public static RegistryObject<ModToolLeveling> LEVELING_MODIFIER = MODIFIERS.register("leveling", ModToolLeveling::new);

    public static TinkerLeveling instance;

    public TinkerLeveling() {

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerModifiers);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TinkerConfig.SERVER_CONFIG);
        TinkerPacketHandler.register();
        MODIFIERS.register(FMLJavaModLoadingContext.get().getModEventBus());

        instance = this;
    }

    public void registerModifiers(final FMLCommonSetupEvent event) {
        CapabilityDamageXp.register();
    }

    private static final ResourceLocation CAPABILITY_KEY = new ResourceLocation(TinkerLeveling.MODID, "entityxp");

    @SubscribeEvent
    public void attachEntityDamageCap(AttachCapabilitiesEvent<Entity> event) {
        DamageXp damageXp = new DamageXp();
        if(event.getObject() instanceof LivingEntity && event.getObject().isAlive()) {
            event.addCapability(CAPABILITY_KEY, SimplePersistentCapabilityProvider.from(CapabilityDamageXp.CAPABILITY, () -> damageXp));
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if(!event.getEntity().level.isClientSide) {
            event.getEntity().getCapability(CapabilityDamageXp.CAPABILITY, null).ifPresent(cap -> {
                cap.distributeXpToTools(event.getEntityLiving());
            });
        }
    }

    private void processInvList(NonNullList<ItemStack> items) {
        for(ItemStack itemStack : items) {
            if(itemStack.getItem().is(TinkerTags.Items.MODIFIABLE)) {
                if(ModifierUtil.getModifierLevel(itemStack, LEVELING_MODIFIER.get()) == 0) {
                    ToolStack tool = ToolStack.from(itemStack);
                    tool.addModifier(LEVELING_MODIFIER.get(), 1);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        /* TODO: replace with tool building event if/when Tinkers adds one */
        if(!event.player.level.isClientSide) {
            PlayerInventory inventory = event.player.inventory;
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
}
