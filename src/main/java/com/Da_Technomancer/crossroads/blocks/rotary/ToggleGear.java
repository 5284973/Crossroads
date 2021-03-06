package com.Da_Technomancer.crossroads.blocks.rotary;

import java.util.List;

import com.Da_Technomancer.crossroads.ServerProxy;
import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.MiscOperators;
import com.Da_Technomancer.crossroads.API.Properties;
import com.Da_Technomancer.crossroads.API.enums.GearTypes;
import com.Da_Technomancer.crossroads.items.ModItems;
import com.Da_Technomancer.crossroads.items.itemSets.GearFactory;
import com.Da_Technomancer.crossroads.tileentities.rotary.ToggleGearTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ToggleGear extends BlockContainer{

	private static final AxisAlignedBB DOWN = new AxisAlignedBB(0D, 0D, 0D, 1D, .125D, 1D);
	private GearTypes type;
	
	public ToggleGear(GearTypes type){
		super(Material.IRON);
		this.type = type;
		String name = "toggleGear" + type.toString();
		setUnlocalizedName(name);
		setRegistryName(name);
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this).setRegistryName(name));
		this.setCreativeTab(ModItems.tabCrossroads);
		this.setHardness(3);
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(this, 1), "dustRedstone", "dustRedstone", "stickIron", GearFactory.basicGears.get(type)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced){
		tooltip.add("Mass: " + MiscOperators.betterRound(type.getDensity() / 8D, 2));
		tooltip.add("I: " + MiscOperators.betterRound(type.getDensity() / 8D, 2) * .125D);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new ToggleGearTileEntity(type);
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state){
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos){
		TileEntity te = worldIn.getTileEntity(pos);
		if(!te.hasCapability(Capabilities.ROTARY_HANDLER_CAPABILITY, EnumFacing.DOWN)){
			return 0;
		}
		double holder = Math.abs(te.getCapability(Capabilities.ROTARY_HANDLER_CAPABILITY, EnumFacing.DOWN).getMotionData()[1]) / te.getCapability(Capabilities.ROTARY_HANDLER_CAPABILITY, EnumFacing.DOWN).getPhysData()[2];
		holder *= 15D;
		holder = Math.min(15, holder);
		
		return (int) holder;
	}
	
	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, new IProperty[] {Properties.REDSTONE_BOOL});
	}
	
	@Override
	public int damageDropped(IBlockState state){
		return 0;
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(Properties.REDSTONE_BOOL, meta == 1);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(Properties.REDSTONE_BOOL) ? 1 : 0;
	}

	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return DOWN;
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn){
		if(worldIn.isBlockPowered(pos)){
			if(!state.getValue(Properties.REDSTONE_BOOL)){
				worldIn.setBlockState(pos, state.withProperty(Properties.REDSTONE_BOOL, true));
			}
		}else{
			if(state.getValue(Properties.REDSTONE_BOOL)){
				worldIn.setBlockState(pos, state.withProperty(Properties.REDSTONE_BOOL, false));
			}
		}
		if(worldIn.isRemote){
			return;
		}
		ServerProxy.masterKey++;
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side){
		return false;
	}

}
