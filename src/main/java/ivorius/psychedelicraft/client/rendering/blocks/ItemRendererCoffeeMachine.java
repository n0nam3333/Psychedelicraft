package ivorius.psychedelicraft.client.rendering.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

public class ItemRendererCoffeeMachine implements IItemRenderer {
    private final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("psychedelicraft", "models/coffeeMachine.obj"));
    private final ResourceLocation texture = new ResourceLocation("psychedelicraft", "textures/mod/coffeeMachineUV.png");

    public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
        return true;
    }

    public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper) {
        return true;
    }

    public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        GL11.glTranslatef(0.5F, 0.01F, 0.5F);
        GL11.glDisable(2884);
        GL11.glEnable(3008);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glScalef(0.25F, 0.28F, 0.25F);
        this.model.renderAll();
        GL11.glPopMatrix();
    }
}
