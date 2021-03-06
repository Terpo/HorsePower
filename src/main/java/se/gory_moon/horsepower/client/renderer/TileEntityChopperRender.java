package se.gory_moon.horsepower.client.renderer;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import se.gory_moon.horsepower.blocks.ChopperBlock;
import se.gory_moon.horsepower.blocks.HPBaseBlock;
import se.gory_moon.horsepower.client.model.modelvariants.ChopperModels;
import se.gory_moon.horsepower.tileentity.ChopperTileEntity;
import se.gory_moon.horsepower.util.RenderUtils;

public class TileEntityChopperRender extends TileEntityHPBaseRenderer<ChopperTileEntity> {

    @Override
    public void render(ChopperTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockState blockState = te.getWorld().getBlockState( te.getPos() );
        if (!(blockState.getBlock() instanceof HPBaseBlock)) return;
        BlockState bladeState = blockState.with(ChopperBlock.PART, ChopperModels.BLADE);
        if (!(bladeState.getBlock() instanceof HPBaseBlock)) return;
        IBakedModel bladeModel = dispatcher.getBlockModelShapes().getModel(bladeState);

        preDestroyRender(destroyStage);
        setRenderSettings();

        buffer.begin( GL11.GL_QUADS, DefaultVertexFormats.BLOCK );
        // The translation ensures the vertex buffer positions are relative to 0,0,0 instead of the block pos
        // This makes the translations that follow much easier
        buffer.setTranslation( -te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ() );

        if (destroyStage >= 0) {
            buffer.noColor();
            renderBlockDamage(bladeState, te.getPos(), destroyStage, te.getWorld());
        } else
            dispatcher.getBlockModelRenderer().renderModel( te.getWorld(), bladeModel, blockState, te.getPos(), buffer, false, getWorld().rand, bladeState.getPositionRandom(te.getPos()));

        buffer.setTranslation( 0, 0, 0 );

        GlStateManager.pushMatrix();
        GlStateManager.translated( x, y, z );

        // Apply GL transformations relative to the center of the block: 1) TE rotation and 2) crank rotation
        GlStateManager.translated( 0.5, 0.5, 0.5 );
        GlStateManager.translated( 0, te.getVisualWindup(), 0 );
        GlStateManager.translated( -0.5, -0.5, -0.5 );

        tessellator.draw();
        GlStateManager.popMatrix();
        buffer.setTranslation(0.0D, 0.0D, 0.0D);
        postDestroyRender(destroyStage);
        RenderHelper.enableStandardItemLighting();

        
        renderLeach(x + 0.5, y + 2.9 + te.getVisualWindup(), z + 0.5, x + 0.5, y + 0.2, z + 0.5, x + 0.5, y + 1.7, z + 0.5);

        if (te.hasWorker())
            renderLeash(te.getWorker(), x, y, z, 0D, 1.1D, 0D, partialTicks, te.getPos());

        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        if (!te.getStackInSlot(0).isEmpty())
            renderStillItem(te, te.getStackInSlot(0), 0.5F, 0.54F, 0.5F, 1.3F);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        if (!te.getStackInSlot(1).isEmpty())
            renderStillItem(te, te.getStackInSlot(1), 0.5F, 0.54F, 0.5F, 1.3F);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();

        drawDisplayText(te, x, y + 1, z);

        if (te.isInvalid())
            RenderUtils.renderInvalidArea(te.getWorld(), te.getPos(), 0);
        GlStateManager.popMatrix();
    }

}
