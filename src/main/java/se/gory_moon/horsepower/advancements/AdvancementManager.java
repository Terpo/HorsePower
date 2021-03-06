package se.gory_moon.horsepower.advancements;

import net.minecraft.advancements.CriteriaTriggers;

public class AdvancementManager {

    public static final UseHorseMillstoneTrigger USE_MILLSTONE = CriteriaTriggers.register(new UseHorseMillstoneTrigger());
    public static final UseChoppingBlockTrigger USE_CHOPPER = CriteriaTriggers.register(new UseChoppingBlockTrigger());
    public static final UsePressTrigger USE_PRESS = CriteriaTriggers.register(new UsePressTrigger());

    public static void register() {}

}
