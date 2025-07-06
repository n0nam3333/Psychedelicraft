package ivorius.psychedelicraft.gui;

import ivorius.psychedelicraft.blocks.TileEntityCoffeeMachine;
import ivorius.psychedelicraft.fluids.PSFluids;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import org.lwjgl.opengl.GL11;

public class GuiCoffeeMachine extends GuiContainer {
    private final ResourceLocation containerTexture = new ResourceLocation("psychedelicraft", "textures/mod/guiCoffeeMachine.png");
    private final InventoryPlayer inventory;
    private final TileEntityCoffeeMachine te;
    private int overlayTexX = 176;
    private int overlayTexY = 31;
    private int progressBarX = 176;
    private int progressBarY = 14;
    private int fireX = 176;
    private int fireY = 14;
    protected boolean drawOverlay = true;

    public GuiCoffeeMachine(TileEntityCoffeeMachine te, EntityPlayer player) {
        super(new ContainerCoffeeMachine(player, te));
        this.inventory = player.inventory;
        this.te = te;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.containerTexture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }

    private void drawProgressBar() {
        int x = 0;
        if (this.te.getTimeNeeded() != 0) {
            x = this.te.getCookTime() * 24 / this.te.getTimeNeeded();
        }

        GL11.glPushAttrib(8192);
        GL11.glDisable(2896);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.containerTexture);
        this.drawTexturedModalRect(109, 35, this.progressBarX, this.progressBarY, x, 16);
        GL11.glPopAttrib();
    }

    private void drawFire() {
        int y = 0;
        if (this.te.getCurrentItemBurnTime() != 0) {
            y = this.te.getBurnTime() * 13 / this.te.getCurrentItemBurnTime();
        }

        GL11.glPushAttrib(8192);
        GL11.glDisable(2896);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.containerTexture);
        this.drawTexturedModalRect(87, 36 + this.fireY - y, this.fireX, this.fireY - y, 14, y);
        GL11.glPopAttrib();
    }

    private void drawTank(IFluidTank tank, int xPos, int yPos, int mX, int mY) {
        if (tank != null && tank.getCapacity() > 0) {
            FluidStack contents = tank.getFluid();
            if (contents != null && contents.amount > 0 && contents.getFluid() != null) {
                IIcon liquidIcon = contents.getFluid().getIcon();
                if (contents.getFluid() == PSFluids.coffee) {
                    liquidIcon = PSFluids.coffee.getIcon();
                }

                if (liquidIcon != null) {
                    int scaledLiquid = contents.amount * 60 / tank.getCapacity();
                    if (scaledLiquid == 60) {
                        --scaledLiquid;
                    }

                    GL11.glPushAttrib(8192);
                    GL11.glDisable(2896);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

                    for (int start = 0; scaledLiquid > 0; start += 16) {
                        int x1;
                        if (scaledLiquid > 16) {
                            x1 = 16;
                            scaledLiquid -= 16;
                        } else {
                            x1 = scaledLiquid;
                            scaledLiquid = 0;
                        }

                        this.drawTexturedModelRectFromIcon(xPos + mX * 16, yPos + 59 - x1 - start, liquidIcon, 16, x1);
                    }

                    Minecraft.getMinecraft().getTextureManager().bindTexture(this.containerTexture);
                    this.drawTexturedModalRect(xPos + mX * 16, yPos - 1 + mY * 16, this.overlayTexX, this.overlayTexY, 16, 60);
                    GL11.glPopAttrib();
                }
            }
        }
    }

    public boolean rectContains(int x, int y, int rectX, int rectY, int rectWidth, int rectHeight) {
        return x >= rectX && y >= rectY && x < rectX + rectWidth && y < rectY + rectHeight;
    }

    public void drawTankTooltip(FluidTank tank, int x, int y, int width, int height, int mouseX, int mouseY, List<String> additionalText) {
        if (this.rectContains(mouseX, mouseY, x, y, width, height) && tank.getFluid() != null) {
            FluidStack containedFluidStack = tank.getFluid();
            if (containedFluidStack != null) {
                List<String> tooltipList = new ArrayList();
                tooltipList.add(containedFluidStack.getLocalizedName());
                tooltipList.add(EnumChatFormatting.GRAY + "Amount: " + containedFluidStack.amount);
                if (additionalText != null) {
                    tooltipList.addAll(additionalText);
                }

                this.drawHoveringText(tooltipList, mouseX, mouseY, this.fontRendererObj);
            }
        }

    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int baseX = (this.width - this.xSize) / 2;
        int baseY = (this.height - this.ySize) / 2;
        this.drawTankTooltip(this.te.getCoffeeTank(), baseX + 144, baseY + 10, 16, 64, mouseX, mouseY, null);
    }

    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        this.fontRendererObj.drawString(I18n.format("Coffee Machine"), this.xSize / 2 - this.fontRendererObj.getStringWidth(I18n.format("Coffee Machine", new Object[0])) / 2, 6, 4210752, false);
        this.fontRendererObj.drawString(I18n.format(this.inventory.getInventoryName()), 8, this.ySize - 96 + 2, 4210752);
        this.drawTank(this.te.getWaterTank(), 14, 13, 0, 0);
        this.drawTank(this.te.getCoffeeTank(), 17, 13, 8, 0);
        this.drawProgressBar();
        this.drawFire();
    }
}
