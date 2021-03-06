package se.gory_moon.horsepower.tileentity;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import se.gory_moon.horsepower.Configs;
import se.gory_moon.horsepower.recipes.AbstractHPRecipe.Type;
import se.gory_moon.horsepower.recipes.HPRecipes;
import se.gory_moon.horsepower.recipes.RecipeSerializers;
import se.gory_moon.horsepower.util.Localization;

public class ChopperTileEntity extends HPHorseBaseTileEntity {

    private int currentWindup;
    private int currentItemChopTime;
    private int totalItemChopTime;
    private float visualWindup = 0;
    private float oldVisualWindup = -1;

    public ChopperTileEntity(@NonnullType TileEntityType<ChopperTileEntity> tileEntityType) {
        super(2, tileEntityType);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("currentWindup", currentWindup);
        compound.putInt("chopTime", currentItemChopTime);
        compound.putInt("totalChopTime", totalItemChopTime);

        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        currentWindup = compound.getInt("currentWindup");

        if (getStackInSlot(0).getCount() > 0) {
            currentItemChopTime = compound.getInt("chopTime");
            totalItemChopTime = compound.getInt("totalChopTime");
        } else {
            currentItemChopTime = 0;
            totalItemChopTime = 1;
        }
    }

//    public IExtendedBlockState getExtendedState(IExtendedBlockState state) {
//        state = (IExtendedBlockState) state.withProperty(BlockChopper.FACING, getForward());
//        state = (IExtendedBlockState) state.withProperty(BlockChopper.PART, state.getValue(BlockChopper.PART));
//
//        return state;
//    }

    public void setTextureBlock(CompoundNBT textureBlock) {
        getTileData().put("textureBlock", textureBlock);
    }

    public CompoundNBT getTextureBlock() {
        return getTileData().getCompound("textureBlock");
    }

    @Override
    public boolean canBeRotated() {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index != 1 && index == 0 && getStackInSlot(1).isEmpty() && getStackInSlot(0).isEmpty()
                && getRecipe(stack) != null;
    }

    @Override
    public boolean validateArea() {
        if (searchPos == null) {
            searchPos = Lists.newArrayList();

            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    if ((x <= 1 && x >= -1) && (z <= 1 && z >= -1))
                        continue;
                    searchPos.add(getPos().add(x, 0, z));
                    searchPos.add(getPos().add(x, 1, z));
                }
            }
        }

        for (BlockPos pos : searchPos) {
            if (!getWorld().getBlockState(pos).getMaterial().isReplaceable())
                return false;
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        float windup = Configs.SERVER.pointsForWindup.get().intValue() > 0
                ? Configs.SERVER.pointsForWindup.get().intValue()
                : 1;
        visualWindup = -0.74F + (0.74F * ((currentWindup) / (windup - 1)));
    }

    @Override
    public boolean targetReached() {
        currentWindup++;

        float windup = Configs.SERVER.pointsForWindup.get().intValue() > 0
                ? Configs.SERVER.pointsForWindup.get().intValue()
                : 1;
        if (currentWindup >= windup) {
            currentWindup = 0;
            currentItemChopTime++;

            if (currentItemChopTime >= totalItemChopTime) {
                currentItemChopTime = 0;

                totalItemChopTime = HPRecipes.getTypeTime(getRecipe(), null);
                chopItem();
                return true;
            }
        }
        markDirty();
        return false;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack itemstack = getStackInSlot(index);
        super.setInventorySlotContents(index, stack);

        if (index == 1 && getStackInSlot(1).isEmpty()) {
            markDirty();
        }

        boolean flag = !stack.isEmpty() && stack.isItemEqual(itemstack)
                && ItemStack.areItemStackTagsEqual(stack, itemstack);
        if (index == 0 && !flag) {
            totalItemChopTime = HPRecipes.getTypeTime(getRecipe(), null);
            currentItemChopTime = 0;
            currentWindup = 0;
            markDirty();
        }
    }

    private void chopItem() {
        if (canWork()) {
            ItemStack input = getStackInSlot(0);
            ItemStack output = getStackInSlot(1);

            ItemStack result = getRecipe().getCraftingResult(inventory);

            if (output.isEmpty()) {
                setInventorySlotContents(1, result.copy());
            } else if (output.isItemEqual(result)) {
                output.grow(result.getCount());
            }

            input.shrink(1);
            markDirty();
        }
    }

    @Override
    public int getPositionOffset() {
        return 0;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public ITextComponent getName() {
        return new StringTextComponent("container.chopper");
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getOutputSlot() {
        return 1;
    }

    public float getVisualWindup() {
        return visualWindup;
    }

    public float getOldVisualWindup() {
        return oldVisualWindup;
    }

    @Nullable
    @Override
    public ITextComponent getDisplayName() {
        if (valid)
            return null;
        return new TranslationTextComponent(Localization.INFO.CHOPPING_INVALID.key())
                .setStyle(new Style().setColor(TextFormatting.RED));
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return null;
    }

    @Override
    public IRecipeType<? extends IRecipe<IInventory>> getRecipeType() {
        return RecipeSerializers.CHOPPING_TYPE;
    }

    @Override
    protected Type getHPRecipeType() {
        return Type.HORSE;
    }

    public int getTotalItemChopTime() {
        return totalItemChopTime;
    }

    public int getCurrentItemChopTime() {
        return currentItemChopTime;
    }

    public int getCurrentWindup() {
        return currentWindup;
    }
}
