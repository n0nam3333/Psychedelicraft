package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockCoffeeMachine extends BlockContainer {
    public static final String name = "coffeeMachine";
    private final Random rand = new Random();

    public BlockCoffeeMachine() {
        super(Material.glass);
    }

    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        int facing = MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + (double) 0.5F) & 3;
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityCoffeeMachine) {
            TileEntityCoffeeMachine tec = (TileEntityCoffeeMachine) te;
            tec.setFacingDirection(facing);
            world.markBlockForUpdate(x, y, z);
        }
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float lx, float ly, float lz) {
        if (world.isRemote) {
            return true;
        } else {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityCoffeeMachine) {
                player.openGui(Psychedelicraft.instance, 10, world, x, y, z);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int par2) {
        return new TileEntityCoffeeMachine();
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }
}
