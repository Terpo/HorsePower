package se.gory_moon.horsepower.advancements;

import net.minecraft.advancements.CriteriaTriggers;

public class Manager {

    public static final UseHorseGrindstoneTrigger USE_GRINDSTONE = CriteriaTriggers.register(new UseHorseGrindstoneTrigger());
    public static final UseChoppingBlockTrigger USE_CHOPPER = CriteriaTriggers.register(new UseChoppingBlockTrigger());

    public static void register() {}

}
