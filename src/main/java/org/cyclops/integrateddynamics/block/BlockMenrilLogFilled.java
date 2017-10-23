package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockLog;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.item.ItemCrystalizedMenrilChunkConfig;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Menril wood block that is filled.
 * @author rubensworks
 */
public class BlockMenrilLogFilled extends ConfigurableBlockLog {

    @BlockProperty
    public static final PropertyInteger SIDE = PropertyInteger.create("side", 0, 3);

    private static BlockMenrilLogFilled _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockMenrilLogFilled getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockMenrilLogFilled(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig);
        this.setHardness(2.5F);
    }

    @Nullable
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(BlockMenrilLog.getInstance());
    }

    @SuppressWarnings("deprecation")
    @Override
    public SoundType getSoundType() {
        return SoundType.WOOD;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos blockPos, IBlockState blockStatedata, int fortune) {
        drops.add(new ItemStack(getItemDropped(blockStatedata, RANDOM, fortune)));
        drops.add(new ItemStack(ItemCrystalizedMenrilChunkConfig._instance.getItemInstance(), 1 + RANDOM.nextInt(3 + fortune)));
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }
}
