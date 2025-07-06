package ivorius.psychedelicraft.gui;

import ivorius.psychedelicraft.blocks.TileEntityCoffeeMachine;
import ivorius.psychedelicraft.blocks.TileEntityCoffeeMachine.slotEnum;
import ivorius.psychedelicraft.fluids.FluidHelper;
import ivorius.psychedelicraft.fluids.PSFluids;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ContainerCoffeeMachine extends Container {
    private final TileEntityCoffeeMachine te;
    private int lastCookTime;
    private int lastBurnTime;
    private int lastItemBurnTime;
    private int lastCoffeeAmount;
    private int lastWaterAmount;

    public ContainerCoffeeMachine(EntityPlayer player, TileEntityCoffeeMachine te) {
        this.te = te;
        this.addSlotToContainer(new Slot(te, slotEnum.INPUT_SLOT.ordinal(), 86, 17));
        this.addSlotToContainer(new Slot(te, slotEnum.FILTER_SLOT.ordinal(), 67, 35));
        this.addSlotToContainer(new Slot(te, slotEnum.FUEL_SLOT.ordinal(), 86, 53));
        this.addSlotToContainer(new Slot(te, slotEnum.WATER_SLOT.ordinal(), 37, 17));
        this.addSlotToContainer(new Slot(te, slotEnum.WATER_EMPTY_SLOT.ordinal(), 37, 53));
        this.addSlotToContainer(new Slot(te, slotEnum.COFFEE_SLOT.ordinal(), 122, 17));
        this.addSlotToContainer(new Slot(te, slotEnum.COFFEE_EMPTY_SLOT.ordinal(), 122, 53));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    public void addCraftingToCrafters(ICrafting listener) {
        super.addCraftingToCrafters(listener);
        listener.sendProgressBarUpdate(this, 0, this.te.getCookTime());
        listener.sendProgressBarUpdate(this, 1, this.te.getBurnTime());
        listener.sendProgressBarUpdate(this, 2, this.te.getCurrentItemBurnTime());
        if (this.te.getCoffeeTank().getFluid() != null) {
            listener.sendProgressBarUpdate(this, 3, this.te.getCoffeeTank().getFluidAmount());
        } else if (this.te.getCoffeeTank().getFluid() == null) {
            FluidStack coffee = new FluidStack(PSFluids.coffee, 0);
            FluidHelper.ensureTag(coffee);
            coffee.tag.setInteger("temperature", 2);
            this.te.getCoffeeTank().fill(coffee, true);
            listener.sendProgressBarUpdate(this, 3, this.te.getCoffeeTank().getFluidAmount());
        }

        if (this.te.getWaterTank().getFluid() != null) {
            listener.sendProgressBarUpdate(this, 4, this.te.getWaterTank().getFluidAmount());
        } else if (this.te.getWaterTank().getFluid() == null) {
            this.te.getWaterTank().fill(new FluidStack(FluidRegistry.WATER, 0), true);
            listener.sendProgressBarUpdate(this, 4, this.te.getWaterTank().getFluidAmount());
        }
    }

    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (Object crafter : this.crafters) {
            ICrafting icrafting = (ICrafting) crafter;
            if (this.lastCookTime != this.te.getCookTime()) {
                icrafting.sendProgressBarUpdate(this, 0, this.te.getCookTime());
            }

            if (this.lastBurnTime != this.te.getBurnTime()) {
                icrafting.sendProgressBarUpdate(this, 1, this.te.getBurnTime());
            }

            if (this.lastItemBurnTime != this.te.getCurrentItemBurnTime()) {
                icrafting.sendProgressBarUpdate(this, 2, this.te.getCurrentItemBurnTime());
            }

            if (this.te.getCoffeeTank().getFluid() != null) {
                if (this.lastCoffeeAmount != this.te.getCoffeeTank().getFluidAmount()) {
                    icrafting.sendProgressBarUpdate(this, 3, this.te.getCoffeeTank().getFluidAmount());
                }
            } else if (this.te.getCoffeeTank().getFluid() == null) {
                FluidStack coffee = new FluidStack(PSFluids.coffee, 0);
                FluidHelper.ensureTag(coffee);
                coffee.tag.setInteger("temperature", 2);
                this.te.getCoffeeTank().fill(coffee, true);
                icrafting.sendProgressBarUpdate(this, 3, this.te.getCoffeeTank().getFluidAmount());
            }

            if (this.te.getWaterTank().getFluid() != null) {
                if (this.lastWaterAmount != this.te.getWaterTank().getFluidAmount()) {
                    icrafting.sendProgressBarUpdate(this, 4, this.te.getWaterTank().getFluidAmount());
                }
            } else if (this.te.getWaterTank().getFluid() == null) {
                this.te.getWaterTank().fill(new FluidStack(FluidRegistry.WATER, 0), true);
                icrafting.sendProgressBarUpdate(this, 4, this.te.getWaterTank().getFluidAmount());
            }
        }

        this.lastCookTime = this.te.getCookTime();
        this.lastBurnTime = this.te.getBurnTime();
        this.lastItemBurnTime = this.te.getCurrentItemBurnTime();
        if (this.te.getCoffeeTank().getFluid() != null) {
            this.lastCoffeeAmount = this.te.getCoffeeTank().getFluidAmount();
        } else if (this.te.getCoffeeTank().getFluid() == null) {
            this.lastCoffeeAmount = 0;
        }

        if (this.te.getWaterTank().getFluid() != null) {
            this.lastWaterAmount = this.te.getWaterTank().getFluidAmount();
        } else if (this.te.getWaterTank().getFluid() == null) {
            this.lastWaterAmount = 0;
        }
    }

    public void updateProgressBar(int valueType, int value) {
        super.updateProgressBar(valueType, value);
        if (valueType == 0) {
            this.te.setCookTime(value);
        } else if (valueType == 1) {
            this.te.setBurnTime(value);
        } else if (valueType == 2) {
            this.te.setCurrentItemBurnTime(value);
        } else if (valueType == 3) {
            if (this.te.getCoffeeTank().getFluid() != null) {
                this.te.getCoffeeTank().getFluid().amount = value;
            } else if (this.te.getCoffeeTank().getFluid() == null) {
                FluidStack coffee = new FluidStack(PSFluids.coffee, 0);
                FluidHelper.ensureTag(coffee);
                coffee.tag.setInteger("temperature", 2);
                this.te.getCoffeeTank().fill(coffee, true);
            }
        } else if (valueType == 4) {
            if (this.te.getWaterTank().getFluid() != null) {
                this.te.getWaterTank().getFluid().amount = value;
            } else if (this.te.getWaterTank().getFluid() == null) {
                this.te.getWaterTank().fill(new FluidStack(FluidRegistry.WATER, 0), true);
            }
        }
    }

    public ItemStack transferStackInSlot(EntityPlayer player, int slotRaw) {
        ItemStack stack = null;
        Slot slot = (Slot) this.inventorySlots.get(slotRaw);
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            if (slotRaw < 27) {
                if (!this.mergeItemStack(stackInSlot, 27, this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!this.mergeItemStack(stackInSlot, 0, 27, false)) {
                return null;
            }

            if (stackInSlot.stackSize == 0) {
                slot.putStack((ItemStack) null);
            } else {
                slot.onSlotChanged();
            }
        }

        return stack;
    }
}
