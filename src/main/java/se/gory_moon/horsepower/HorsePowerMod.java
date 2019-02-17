package se.gory_moon.horsepower;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gory_moon.horsepower.advancements.AdvancementManager;
import se.gory_moon.horsepower.items.ModItems;
import se.gory_moon.horsepower.lib.Reference;
import se.gory_moon.horsepower.network.PacketHandler;
import se.gory_moon.horsepower.recipes.HPRecipes;
import se.gory_moon.horsepower.util.Utils;

//"after:crafttweaker;after:jei;after:waila;after:theoneprobe;"
@Mod(Reference.MODID)
public class HorsePowerMod {

    public static HorsePowerItemGroup itemGroup = new HorsePowerItemGroup();
    public static final Logger LOGGER = LogManager.getLogger(Reference.MODID);

    //public static ITweakerPlugin tweakerPlugin = new DummyTweakPluginImpl();

    public HorsePowerMod() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);
        eventBus.addListener(this::sendIMC);
        eventBus.addListener(this::loadComplete);
        eventBus.addListener(this::serverLoad);
        eventBus.addListener(this::onFingerprintViolation);
    }

    public void setup(FMLCommonSetupEvent event) {
        PacketHandler.init();
        AdvancementManager.register();

        /*if (Loader.isModLoaded("crafttweaker"))
            tweakerPlugin = new TweakerPluginImpl();

        tweakerPlugin.register();*/
        ModItems.registerRecipes();
    }

    public void sendIMC(InterModEnqueueEvent event) {
        //TODO how this will work?
        InterModComms.sendTo("waila", "register", () -> Reference.WAILA_PROVIDER);
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        //tweakerPlugin.getRemove().forEach(IHPAction::run);
        //tweakerPlugin.getAdd().forEach(IHPAction::run);

        HPEventHandler.reloadConfig();
    }

    public void serverLoad(FMLServerAboutToStartEvent event) {
        HPRecipes.instance().reloadRecipes();
        Utils.sendSavedErrors();
    }

    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        LOGGER.warn("Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with. This version will NOT be supported by the author!");
    }

}
