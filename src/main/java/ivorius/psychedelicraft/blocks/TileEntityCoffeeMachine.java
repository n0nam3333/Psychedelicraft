package ivorius.psychedelicraft.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import ivorius.psychedelicraft.fluids.FluidHelper;
import ivorius.psychedelicraft.fluids.PSFluids;
import ivorius.psychedelicraft.items.PSItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityCoffeeMachine extends TileEntity implements ISidedInventory, IFluidHandler {
    private final FluidTank coffeeTank = new FluidTank(10000);
    private final FluidTank waterTank = new FluidTank(10000);
    private ItemStack[] items = new ItemStack[7];
    private int burnTime;
    private int currentItemBurnTime;
    private int cookTime;
    private int timeNeeded = 100;
    private int counter;
    private int facingDirection;

    public FluidTank getCoffeeTank() {
        return this.coffeeTank;
    }

    public FluidTank getWaterTank() {
        return this.waterTank;
    }

    public int getBurnTime() {
        return this.burnTime;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }

    public int getCurrentItemBurnTime() {
        return this.currentItemBurnTime;
    }

    public void setCurrentItemBurnTime(int currentItemBurnTime) {
        this.currentItemBurnTime = currentItemBurnTime;
    }

    public int getCookTime() {
        return this.cookTime;
    }

    public void setCookTime(int cookTime) {
        this.cookTime = cookTime;
    }

    public int getTimeNeeded() {
        return this.timeNeeded;
    }

    public void setTimeNeeded(int timeNeeded) {
        this.timeNeeded = timeNeeded;
    }

    public void setFacingDirection(int newFacing) {
        this.facingDirection = newFacing;
    }

    public int getFacingDirection() {
        return this.facingDirection;
    }

    @Override
    public int getSizeInventory() {
        return this.items.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.items[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (this.items[slot] != null) {
            if (this.items[slot].stackSize == amount) {
                ItemStack itemstack = this.items[slot];
                this.items[slot] = null;
                this.markDirty(); // = this.func_70296_d();??
                return itemstack;
            } else {
                ItemStack itemstack = this.items[slot].splitStack(amount);
                if (this.items[slot].stackSize == 0) {
                    this.items[slot] = null;
                }

                this.markDirty();
                return itemstack;
            }
        } else {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (this.items[slot] != null) {
            ItemStack itemstack = this.items[slot];
            this.items[slot] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        boolean sameStackAlreadyInSlot = stack != null && this.items[slot] != null && stack.isItemEqual(this.items[slot]) && ItemStack.areItemStacksEqual(stack, this.items[slot]);
        this.items[slot] = stack;
        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = this.getInventoryStackLimit();
        }

        if (slot == TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal() && !sameStackAlreadyInSlot) {
            this.cookTime = 0;
        }

        this.markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.coffeeMachine";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && player.getDistanceSq((double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal() && stack.getItem() == PSItems.coffeePowder) {
            return true;
        } else if (slot == TileEntityCoffeeMachine.slotEnum.FILTER_SLOT.ordinal() && stack.getItem() == PSItems.coffeeFilter) {
            return true;
        } else if (slot == TileEntityCoffeeMachine.slotEnum.FUEL_SLOT.ordinal() && this.getItemBurnTime(stack) > 0) {
            return true;
        } else {
            return slot == TileEntityCoffeeMachine.slotEnum.WATER_SLOT.ordinal() && stack.getItem() == Items.water_bucket;
        }
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        this.items = new ItemStack[this.getSizeInventory()];

        for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbtTagCompound = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbtTagCompound.getByte("Slot");
            if (b0 >= 0 && b0 < this.items.length) {
                this.items[b0] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
            }
        }

        if (this.coffeeTank.getFluid() != null) {
            this.coffeeTank.getFluid().amount = compound.getShort("coffeeTankContent");
        } else if (this.coffeeTank.getFluid() == null) {
            FluidStack coffee = new FluidStack(PSFluids.coffee, compound.getShort("coffeeTankContent"));
            FluidHelper.ensureTag(coffee);
            coffee.tag.setInteger("temperature", 2);
            this.coffeeTank.setFluid(coffee);
        }

        if (this.waterTank.getFluid() != null) {
            this.waterTank.getFluid().amount = compound.getShort("waterTankContent");
        } else if (this.waterTank.getFluid() == null) {
            this.waterTank.setFluid(new FluidStack(FluidRegistry.WATER, compound.getShort("waterTankContent")));
        }

        this.burnTime = compound.getShort("BurnTime");
        this.cookTime = compound.getShort("CookTime");
        this.timeNeeded = compound.getShort("CookTimeTotal");
        this.currentItemBurnTime = this.getItemBurnTime(this.items[TileEntityCoffeeMachine.slotEnum.FUEL_SLOT.ordinal()]);
        this.facingDirection = compound.getInteger("FacingDirection");  //  func_74762_e getInt?
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setShort("BurnTime", (short)this.burnTime);
        compound.setShort("CookTime", (short)this.cookTime);
        compound.setShort("CookTimeTotal", (short)this.timeNeeded);
        compound.setInteger("FacingDirection", this.facingDirection);  // func_74768_a setInt
        if (this.coffeeTank.getFluid() != null) {
            compound.setShort("coffeeTankContent", (short)this.coffeeTank.getFluidAmount());
        }

        if (this.waterTank.getFluid() != null) {
            compound.setShort("waterTankContent", (short)this.waterTank.getFluidAmount());
        }

        NBTTagList nbttaglist = new NBTTagList();

        for(int i = 0; i < this.items.length; ++i) {
            if (this.items[i] != null) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                nbtTagCompound.setByte("Slot", (byte)i);
                this.items[i].writeToNBT(nbtTagCompound);
                nbttaglist.appendTag(nbtTagCompound);
            }
        }

        compound.setTag("Items", nbttaglist);
    }

    private boolean canProcess() {
        if (this.items[TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal()] == null) {
            return false;
        } else if (this.items[TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal()].getItem() != PSItems.coffeePowder) {
            return false;
        } else if (this.items[TileEntityCoffeeMachine.slotEnum.FILTER_SLOT.ordinal()] == null) {
            return false;
        } else if (this.items[TileEntityCoffeeMachine.slotEnum.FILTER_SLOT.ordinal()].getItem() != PSItems.coffeeFilter) {
            return false;
        } else if (this.coffeeTank.getCapacity() - this.coffeeTank.getFluidAmount() < 1000) {
            return false;
        } else {
            return this.waterTank.getFluidAmount() >= 1000;
        }
    }

    private void processItem() {
        if (this.canProcess()) {
            --this.items[TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal()].stackSize;
            if (this.items[TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal()].stackSize <= 0) {
                this.items[TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal()] = null;
            }

            --this.items[TileEntityCoffeeMachine.slotEnum.FILTER_SLOT.ordinal()].stackSize;
            if (this.items[TileEntityCoffeeMachine.slotEnum.FILTER_SLOT.ordinal()].stackSize <= 0) {
                this.items[TileEntityCoffeeMachine.slotEnum.FILTER_SLOT.ordinal()] = null;
            }

            this.waterTank.drain(1000, true);
            FluidStack coffee = new FluidStack(PSFluids.coffee, 1000);
            FluidHelper.ensureTag(coffee);
            coffee.tag.setInteger("temperature", 2);
            this.coffeeTank.fill(coffee, true);
        }

    }

    public void updateEntity() {
        boolean isActive = this.burnTime > 0;
        boolean inventoryUpdated = false;
        if (!this.worldObj.isRemote) {
            if (this.items[TileEntityCoffeeMachine.slotEnum.WATER_SLOT.ordinal()] != null) {
                ItemStack waterBucket = this.items[TileEntityCoffeeMachine.slotEnum.WATER_SLOT.ordinal()];
                ItemStack waterBucketEmpty = null;
                int bucketEmptyStackSize = 0;
                if (this.items[TileEntityCoffeeMachine.slotEnum.WATER_EMPTY_SLOT.ordinal()] != null) {
                    waterBucketEmpty = this.items[TileEntityCoffeeMachine.slotEnum.WATER_EMPTY_SLOT.ordinal()];
                    bucketEmptyStackSize = waterBucketEmpty.stackSize;
                }

                if (waterBucket.getItem() == Items.water_bucket && bucketEmptyStackSize < 16 && this.waterTank.getCapacity() - this.waterTank.getFluidAmount() >= 1000) {
                    this.waterTank.fill(new FluidStack(FluidRegistry.WATER, 1000), true);
                    this.items[TileEntityCoffeeMachine.slotEnum.WATER_SLOT.ordinal()] = null;
                    if (waterBucketEmpty != null && waterBucketEmpty.getItem() == Items.bucket) {
                        if (waterBucketEmpty.getItem() == Items.bucket && bucketEmptyStackSize < 16) {
                            ++this.items[TileEntityCoffeeMachine.slotEnum.WATER_EMPTY_SLOT.ordinal()].stackSize;
                        }
                    } else {
                        this.items[TileEntityCoffeeMachine.slotEnum.WATER_EMPTY_SLOT.ordinal()] = new ItemStack(Items.bucket);
                    }
                }
            }

            int amount = 10;
            ItemStack coffeeContainerItem = this.items[TileEntityCoffeeMachine.slotEnum.COFFEE_SLOT.ordinal()];
            if (coffeeContainerItem != null && this.items[TileEntityCoffeeMachine.slotEnum.COFFEE_EMPTY_SLOT.ordinal()] == null && this.coffeeTank.getFluidAmount() > 0 && coffeeContainerItem.getItem() instanceof IFluidContainerItem) {
                if ((coffeeContainerItem.getItem() == PSItems.itemBarrel || coffeeContainerItem.getItem() == PSItems.bottle) && this.coffeeTank.getFluidAmount() > 100) {
                    amount = 100;
                }

                IFluidContainerItem fluidContainerItem = (IFluidContainerItem)coffeeContainerItem.getItem();
                FluidStack coffee = new FluidStack(PSFluids.coffee, amount);
                FluidHelper.ensureTag(coffee);
                coffee.tag.setInteger("temperature", 2);
                if (fluidContainerItem.getFluid(coffeeContainerItem) != null) {
                    if (fluidContainerItem.getCapacity(coffeeContainerItem) - fluidContainerItem.getFluid(coffeeContainerItem).amount > 0) {
                        fluidContainerItem.fill(coffeeContainerItem, coffee, true);
                        this.coffeeTank.drain(amount, true);
                    } else {
                        this.items[TileEntityCoffeeMachine.slotEnum.COFFEE_EMPTY_SLOT.ordinal()] = coffeeContainerItem.copy();
                        this.items[TileEntityCoffeeMachine.slotEnum.COFFEE_SLOT.ordinal()] = null;
                    }
                } else if (fluidContainerItem.getFluid(coffeeContainerItem) == null) {
                    fluidContainerItem.fill(coffeeContainerItem, coffee, true);
                    this.coffeeTank.drain(amount, true);
                }
            }
        }

        if (this.burnTime > 0) {
            --this.burnTime;
        }

        if (!this.worldObj.isRemote) {
            if (this.burnTime == 0 && this.canProcess()) {
                if (this.items[TileEntityCoffeeMachine.slotEnum.FUEL_SLOT.ordinal()] != null) {
                    this.currentItemBurnTime = this.burnTime = this.getItemBurnTime(this.items[TileEntityCoffeeMachine.slotEnum.FUEL_SLOT.ordinal()]);
                    --this.items[TileEntityCoffeeMachine.slotEnum.FUEL_SLOT.ordinal()].stackSize;
                    if (this.items[TileEntityCoffeeMachine.slotEnum.FUEL_SLOT.ordinal()].stackSize <= 0) {
                        this.items[TileEntityCoffeeMachine.slotEnum.FUEL_SLOT.ordinal()] = null;
                    }
                }

                if (this.burnTime > 0) {
                    inventoryUpdated = true;
                    if (this.items[TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal()] != null && this.items[TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal()].stackSize == 0) {
                        this.items[TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal()] = this.items[TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal()].getItem().getContainerItem(this.items[TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal()]);
                    }
                }
            }

            if (this.isBurning() && this.canProcess()) {
                ++this.cookTime;
                if (this.cookTime == this.timeNeeded) {
                    this.cookTime = 0;
                    this.processItem();
                    inventoryUpdated = true;
                }
            } else {
                this.cookTime = 0;
            }

            if (isActive != this.burnTime > 0) {
                inventoryUpdated = true;
            }
        }

        if (inventoryUpdated) {
            this.markDirty();
        }

    }

    private int getItemBurnTime(ItemStack stack) {
        if (stack == null) {
            return 0;
        } else {
            Item item = stack.getItem();
            if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.air) {
                Block block = Block.getBlockFromItem(item);

                if (block == Blocks.wooden_slab) {
                    return 150;
                }

                if (block.getMaterial() == Material.wood) {
                    return 300;
                }

                if (block == Blocks.coal_block) {
                    return 16000;
                }
            }

            if (item instanceof ItemTool && ((ItemTool) item).getToolMaterialName().equals("WOOD")) return 200;
            if (item instanceof ItemSword && ((ItemSword) item).getToolMaterialName().equals("WOOD")) return 200;
            if (item instanceof ItemHoe && ((ItemHoe) item).getToolMaterialName().equals("WOOD")) return 200;
            if (item == Items.stick) return 100;
            if (item == Items.coal) return 1600;
            if (item == Items.lava_bucket) return 20000;
            if (item == Item.getItemFromBlock(Blocks.sapling)) return 100;
            if (item == Items.blaze_rod) return 2400;
            return GameRegistry.getFuelValue(stack);
        }
    }

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[]{TileEntityCoffeeMachine.slotEnum.FILTER_SLOT.ordinal(), TileEntityCoffeeMachine.slotEnum.INPUT_SLOT.ordinal(), TileEntityCoffeeMachine.slotEnum.FUEL_SLOT.ordinal(), TileEntityCoffeeMachine.slotEnum.WATER_SLOT.ordinal()};
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack item, int side) {
        return true;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack item, int side) {
        return true;
    }

    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource.getFluid() == PSFluids.coffee) {
            return this.coffeeTank.fill(resource, doFill);
        } else {
            return resource.getFluid() == FluidRegistry.WATER ? this.waterTank.fill(resource, doFill) : 0;
        }
    }

    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource.getFluid() == PSFluids.coffee) {
            return this.coffeeTank.drain(resource.amount, doDrain);
        } else {
            return resource.getFluid() == FluidRegistry.WATER ? this.waterTank.drain(resource.amount, doDrain) : null;
        }
    }

    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        System.out.println("draining");
        return this.coffeeTank.drain(maxDrain, doDrain);
    }

    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{this.coffeeTank.getInfo(), this.waterTank.getInfo()};
    }

    public S35PacketUpdateTileEntity getDescriptionPacket() {
        int coffeeAmount = 0;
        if (this.coffeeTank.getFluid() != null) {
            coffeeAmount = this.coffeeTank.getFluidAmount();
        }

        int waterAmount = 0;
        if (this.waterTank.getFluid() != null) {
            waterAmount = this.waterTank.getFluidAmount();
        }

        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setShort("coffeeAmount", (short)coffeeAmount);
        tagCompound.setShort("waterAmount", (short)waterAmount);
        this.writeToNBT(tagCompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, tagCompound);
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
        if (this.coffeeTank.getFluid() != null) {
            this.coffeeTank.getFluid().amount = pkt.func_148857_g().getShort("coffeeAmount");
        } else if (this.coffeeTank.getFluid() == null) {
            FluidStack coffee = new FluidStack(PSFluids.coffee, pkt.func_148857_g().getShort("coffeeAmount"));
            FluidHelper.ensureTag(coffee);
            coffee.tag.setInteger("temperature", 2);
            this.coffeeTank.setFluid(coffee);
        }

        if (this.waterTank.getFluid() != null) {
            this.waterTank.getFluid().amount = pkt.func_148857_g().getShort("waterAmount");
        } else if (this.waterTank.getFluid() == null) {
            this.waterTank.setFluid(new FluidStack(FluidRegistry.WATER, pkt.func_148857_g().getShort("waterAmount")));
        }

    }

    public static enum slotEnum {
        INPUT_SLOT,
        FILTER_SLOT,
        FUEL_SLOT,
        WATER_SLOT,
        WATER_EMPTY_SLOT,
        COFFEE_SLOT,
        COFFEE_EMPTY_SLOT;
    }
}
