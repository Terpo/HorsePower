package se.gory_moon.horsepower;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import se.gory_moon.horsepower.lib.Reference;
import se.gory_moon.horsepower.network.PacketHandler;
import se.gory_moon.horsepower.network.messages.SyncServerRecipesMessage;
import se.gory_moon.horsepower.recipes.HPRecipes;
import se.gory_moon.horsepower.util.Utils;
import se.gory_moon.horsepower.util.color.Colors;

import java.util.*;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class HPEventHandler {

    public static Map<ItemStack, Pair<Integer, Integer>> choppingAxes = new HashMap<>();
    public static Map<Integer, Pair<Integer, Integer>> harvestPercentages = new HashMap<>();
    private static Map<ResourceLocation, Collection<ResourceLocation>> tagCache = new HashMap<>();

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Reference.MODID)) {
            reloadConfig();
            Utils.sendSavedErrors();
        }
    }

    public static void reloadConfig() {
        //ConfigManager.sync(Reference.MODID, Config.Type.INSTANCE);
        HPRecipes.instance().reloadRecipes();
        choppingAxes.clear();
        Arrays.stream(Configs.general.choppingBlockAxes).forEach(s -> {
            String[] data = s.split("=");
            int base = Utils.getBaseAmount(data[1]);
            int chance = Utils.getChance(data[1]);
            ItemStack stack = ItemStack.EMPTY;

            try {
                stack = (ItemStack) Utils.parseItemStack(data[0], false, false);
            } catch (Exception e) {
                Utils.errorMessage("Parse error with item for custom axes for the chopping block", false);
            }

            if (!stack.isEmpty())
                choppingAxes.put(stack, Pair.of(base, chance));
        });

        harvestPercentages.clear();
        Arrays.stream(Configs.general.harvestable_percentage).forEach(s -> {
            String[] data = s.split("=");
            try {
                int harvestLevel = Integer.parseInt(data[0]);
                int base = Utils.getBaseAmount(data[1]);
                int chance = Utils.getChance(data[1]);

                harvestPercentages.put(harvestLevel, Pair.of(base, chance));
            } catch (NumberFormatException e) {
                Utils.errorMessage("HarvestLevel config is malformed, make sure only numbers are used as values, (" + s + ")", false);
            }
        });
    }

    @SubscribeEvent
    public static void onWorldJoin(EntityJoinWorldEvent event) {
        if (FMLEnvironment.dist.isClient()) {
            if (event.getEntity() instanceof EntityPlayerSP && event.getWorld() instanceof WorldClient && Minecraft.getInstance().player != null) {
                Utils.sendSavedErrors();
                //HPEventHandler.reloadConfig();
            }
        };
    }

    @SubscribeEvent
    public static void onServerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            PacketHandler.INSTANCE.sendTo(new SyncServerRecipesMessage(), ((EntityPlayerMP)event.getPlayer()).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    @SubscribeEvent
    public static void onServerLeave(WorldEvent.Unload event) {
        if (FMLEnvironment.dist.isClient()) {
            NetHandlerPlayClient handler = Minecraft.getInstance().getConnection();
            if (handler != null && !handler.getNetworkManager().isLocalChannel() && HPRecipes.serverSyncedRecipes) {
                HPRecipes.serverSyncedRecipes = false;
                HPRecipes.instance().reloadRecipes();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @OnlyIn(Dist.CLIENT)
    public static void onToolTip(ItemTooltipEvent event) {
        if (!event.getItemStack().isEmpty()) {
            List<ITextComponent> tooltipsToAdd = new ArrayList<>();
            StringBuilder part = new StringBuilder();
            if (Configs.CLIENT.showTags.get()) {
                Item item = event.getItemStack().getItem();
                Collection<ResourceLocation> tags = Optional.ofNullable(getTags(item)).orElse(Collections.emptyList());

                StringBuilder out = null;
                for (ResourceLocation tag: tags) {
                    if (out == null) out = new StringBuilder(Colors.LIGHTGRAY + "Tags:\n    " + Colors.ORANGE + tag);
                    else out.append("\n    ").append(tag);
                }
                if (out != null) {
                    tooltipsToAdd.add(new TextComponentString(out.toString()));
                    part = new StringBuilder("Tags");
                }
            }

            if (Configs.CLIENT.showHarvestLevel.get()) {
                boolean added = false;
                for (String harv : Configs.CLIENT.harvestTypes.get()) {
                    int harvestLevel = event.getItemStack().getItem().getHarvestLevel(event.getItemStack(), ToolType.get(harv), null, null);
                    if (harvestLevel > -1) {
                        tooltipsToAdd.add(new TextComponentString(Colors.LIGHTGRAY + "HarvestLevel: " + Colors.ORANGE + StringUtils.capitalize(harv) + " (" + harvestLevel + ")"));
                        if (!added) {
                            part.append((part.length() > 0) ? " and " : "").append("HarvestLevel");
                            added = true;
                        }
                    }
                }
            }

            if (!tooltipsToAdd.isEmpty()) {
                tooltipsToAdd.add(new TextComponentString(Colors.LIGHTGRAY + "The " + part + " tooltip was added by HorsePower, to disabled check the config."));
                if (GuiScreen.isShiftKeyDown()) {
                    event.getToolTip().addAll(tooltipsToAdd);
                } else {
                    event.getToolTip().add(new TextComponentString(Colors.LIGHTGRAY + "[Hold shift for more]"));
                }
            }
        }
    }

    private static Collection<ResourceLocation> getTags(Item item) {
        return tagCache.computeIfAbsent(item.getRegistryName(), resourceLocation ->
                ItemTags.getCollection().getOwningTags(item).isEmpty() ?
                        null :
                        ItemTags.getCollection().getOwningTags(item));
    }
}
