package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.psychedelicraft.blocks.TileEntityCoffeeMachine;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

public class TESRCoffeeMachine extends TileEntitySpecialRenderer {
    private final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("psychedelicraft", "models/coffeeMachine.obj"));
    private final ResourceLocation texture = new ResourceLocation("psychedelicraft", "textures/mod/coffeeMachineUV.png");

    @Override
    public void renderTileEntityAt(TileEntity entity, double x, double y, double z, float p_147500_8_) {
        TileEntityCoffeeMachine tec = (TileEntityCoffeeMachine) entity;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.01F, (float) z + 0.5F);
        this.bindTexture(this.texture);

        float angle;
        switch (tec.getFacingDirection()){
            case 0:
                angle = 90.0f;
                break;
            case 2:
                angle = 270.0f;
                break;
            case 3:
                angle = 180.0f;
                break;
            default:
                angle = 0.0f;
                break;
        }

        GL11.glRotatef(angle, 0.0F, 1.0F, 0.0F);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glScalef(0.25F, 0.28F, 0.25F);
        this.model.renderAll();
        GL11.glPopMatrix();
    }
}
