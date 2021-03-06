package se.gory_moon.horsepower.compat.jei.press;
/*
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import se.gory_moon.horsepower.recipes.HPRecipes;
import se.gory_moon.horsepower.recipes.PressRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PressRecipeMaker {

    public static List<PressRecipeWrapper> getPressItemRecipes(IJeiHelpers helpers) {
        IStackHelper stackHelper = helpers.getStackHelper();
        Collection<PressRecipe> pressRecipes = HPRecipes.instance().getPressRecipes();

        List<PressRecipeWrapper> recipes = new ArrayList<>();

        for (PressRecipe recipe : pressRecipes) {
            if (recipe.isLiquidRecipe()) continue;
            ItemStack input = recipe.getInput();
            ItemStack result = recipe.getOutput();

            List<ItemStack> inputs = stackHelper.getSubtypes(input);
            PressRecipeWrapper pressRecipeWrapper = new PressRecipeWrapper(inputs, result, null);
            recipes.add(pressRecipeWrapper);
        }

        return recipes;
    }

    public static List<PressRecipeWrapper> getPressFluidRecipes(IJeiHelpers helpers) {
        IStackHelper stackHelper = helpers.getStackHelper();
        Collection<PressRecipe> pressRecipes = HPRecipes.instance().getPressRecipes();

        List<PressRecipeWrapper> recipes = new ArrayList<>();

        for (PressRecipe recipe : pressRecipes) {
            if (!recipe.isLiquidRecipe()) continue;
            ItemStack input = recipe.getInput();
            FluidStack fluidOutput = recipe.getFluidOutput();

            List<ItemStack> inputs = stackHelper.getSubtypes(input);
            PressRecipeWrapper pressRecipeWrapper = new PressRecipeWrapper(inputs, null, fluidOutput);
            recipes.add(pressRecipeWrapper);
        }

        return recipes;
    }
}
*/