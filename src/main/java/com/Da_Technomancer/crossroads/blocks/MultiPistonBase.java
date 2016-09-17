package com.Da_Technomancer.crossroads.blocks;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.Da_Technomancer.crossroads.API.Properties;
import com.Da_Technomancer.crossroads.API.WorldBuffer;
import com.Da_Technomancer.crossroads.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**Notable differences from a normal piston include:
 * 15 block head range, distance controlled by signal strength,
 * No quasi-connectivity,
 * Redstone can be placed on top of the piston,
 * Hit box does not change when extended,
 * Piston extension and retraction is instant, no 2-tick delay or rendering of block movement.
 */
public class MultiPistonBase extends Block{
	
	private final boolean sticky;

	protected MultiPistonBase(boolean sticky){
		super(Material.PISTON);
		String name = "multiPiston" + (sticky ? "Sticky" : "");
		setUnlocalizedName(name);
		setRegistryName(name);
		this.sticky = sticky;
		setHardness(0.5F);
		setCreativeTab(ModItems.tabCrossroads);
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this).setRegistryName(name));
		setDefaultState(this.blockState.getBaseState().withProperty(Properties.FACING, EnumFacing.NORTH).withProperty(Properties.REDSTONE_BOOL, false));
	}
	
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		return this.getDefaultState().withProperty(Properties.FACING, BlockPistonBase.getFacingFromEntity(pos, placer));
	}

	protected void safeBreak(World worldIn, BlockPos pos){
		if(safeToBreak){
			worldIn.destroyBlock(pos, true);
		}
	}
	
	private boolean safeToBreak = true;
	
	private void checkRedstone(World worldIn, BlockPos pos, EnumFacing dir){
		int i = Math.max(worldIn.getRedstonePower(pos.down(), EnumFacing.DOWN), Math.max(worldIn.getRedstonePower(pos.up(), EnumFacing.UP), Math.max(worldIn.getRedstonePower(pos.east(), EnumFacing.EAST), Math.max(worldIn.getRedstonePower(pos.west(), EnumFacing.WEST), Math.max(worldIn.getRedstonePower(pos.north(), EnumFacing.NORTH), worldIn.getRedstonePower(pos.south(), EnumFacing.SOUTH))))));
		if(i > 0){
			if(!worldIn.getBlockState(pos).getValue(Properties.REDSTONE_BOOL)){
				worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(Properties.REDSTONE_BOOL, true));
			}
		}else{
			if(worldIn.getBlockState(pos).getValue(Properties.REDSTONE_BOOL)){
				worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(Properties.REDSTONE_BOOL, false));
			}
		}
		if(getExtension(worldIn, pos, dir) != i){
			safeToBreak = false;
			setExtension(worldIn, pos, dir, i);
			safeToBreak = true;
		}
	}
	
	private int getExtension(World worldIn, BlockPos pos, EnumFacing dir){
		final Block GOAL = sticky ? ModBlocks.multiPistonExtendSticky : ModBlocks.multiPistonExtend;
		for(int i = 1; i <= 15; i++){
			if(worldIn.getBlockState(pos.offset(dir, i)).getBlock() != GOAL || worldIn.getBlockState(pos.offset(dir, i)).getValue(Properties.FACING) != dir){
				return i - 1;
			}else if(worldIn.getBlockState(pos.offset(dir, i)).getValue(Properties.HEAD)){
				return i;
			}
		}
		return 15;
	}
	
	private void setExtension(World worldIn, BlockPos pos, EnumFacing dir, int distance){
		int prev;
		if((prev = getExtension(worldIn, pos, dir)) == distance){
			return;
		}
		
		final WorldBuffer world = new WorldBuffer(worldIn);
		final Block GOAL = sticky ? ModBlocks.multiPistonExtendSticky : ModBlocks.multiPistonExtend;
		for(int i = 1; i <= prev; i++){
			if(world.getBlockState(pos.offset(dir, i)).getBlock() == GOAL && worldIn.getBlockState(pos.offset(dir, i)).getValue(Properties.FACING) == dir){
				world.addChange(pos, Blocks.AIR.getDefaultState());
			}
			if(sticky && i == prev && canPush(world.getBlockState(pos.offset(dir, prev + 1)), false)){
				world.addChange(pos.offset(dir, 1), world.getBlockState(pos.offset(dir, prev + 1)));
				world.addChange(pos.offset(dir, prev + 1), Blocks.AIR.getDefaultState());
			}
		}

		if(distance == 0){
			world.doChanges();
			return;
		}
		
		for(int i = 1; i <= distance; i++){
			ArrayList<BlockPos> list = new ArrayList<BlockPos>();

			if(canPush(world.getBlockState(pos.offset(dir, i)), false)){
				if(propogate(list, world, pos.offset(dir, i), dir, null)){
					world.doChanges();
					return;
				}
			}else if(!canPush(world.getBlockState(pos.offset(dir, i)), true)){
				world.doChanges();
				return;
			}
			
			for(int index = list.size() - 1; index >= 0; --index){
				BlockPos moving = list.get(index);
				
				if(world.getBlockState(moving.offset(dir)).getMobilityFlag() == EnumPushReaction.DESTROY){
					worldIn.destroyBlock(moving.offset(dir), true);
				}
				world.addChange(moving.offset(dir), world.getBlockState(moving));
				world.addChange(moving, Blocks.AIR.getDefaultState());
			}
			
			for(int j = 1; j <= i; j++){
				world.addChange(pos.offset(dir, j), sticky ? ModBlocks.multiPistonExtendSticky.getDefaultState().withProperty(Properties.FACING, dir).withProperty(Properties.HEAD, i == j) : ModBlocks.multiPistonExtend.getDefaultState().withProperty(Properties.FACING, dir).withProperty(Properties.HEAD, i == j));
			}
			
			if(i == distance){
				world.doChanges();
			}
		}
	}
	
	private static boolean canPush(IBlockState state, boolean blocking){
		//TODO
		
		if(blocking){
			//TODO
			return (state.getBlock() == Blocks.PISTON || state.getBlock() == Blocks.STICKY_PISTON) ? !state.getValue(BlockPistonBase.EXTENDED) : state.getMobilityFlag() == EnumPushReaction.NORMAL && state.getMaterial() != Material.AIR && !state.getBlock().hasTileEntity(state) && state.getBlock() != Blocks.OBSIDIAN && state.getBlockHardness(null, null) >= 0;
		}else{
			return (state.getBlock() == Blocks.PISTON || state.getBlock() == Blocks.STICKY_PISTON) ? !state.getValue(BlockPistonBase.EXTENDED) : state.getMobilityFlag() == EnumPushReaction.NORMAL && state.getMaterial() != Material.AIR && !state.getBlock().hasTileEntity(state) && state.getBlock() != Blocks.OBSIDIAN && state.getBlockHardness(null, null) >= 0;
		}
		
		return state.getMobilityFlag() == EnumPushReaction.NORMAL && state.getMaterial() != Material.AIR;
	}
	
	//TODO NOTE THERE ARE STILL SEVERAL BUGS!
	
	private static final int PUSH_LIMIT = 12;
	
	/**
	 * Used recursively to fill a list with the blocks to be moved. Returns true if there is a problem that stops the movement.
	 */
	private static boolean propogate(ArrayList<BlockPos> list, WorldBuffer buf, BlockPos pos, EnumFacing dir, @Nullable BlockPos forward){
		if(list.contains(pos)){
			return false;
		}
		if(buf.getBlockState(pos.offset(dir)).getMobilityFlag() == EnumPushReaction.BLOCK){
			return true;
		}
		if(forward == null){
			list.add(pos);
		}else{
			list.add(list.indexOf(forward), pos);
		}
		
		if(canPush(buf.getBlockState(pos.offset(dir)), false)){
			if(list.size() >= PUSH_LIMIT || propogate(list, buf, pos.offset(dir), dir, null)){
				return true;
			}
		}
		
		if(buf.getBlockState(pos).getBlock() == Blocks.SLIME_BLOCK){
			if(canPush(buf.getBlockState(pos.offset(dir.getOpposite())), false)){
				if(list.size() >= PUSH_LIMIT || propogate(list, buf, pos.offset(dir), dir, pos)){
					return true;
				}
			}
			
			for(EnumFacing checkDir : EnumFacing.VALUES){
				if(checkDir != dir && checkDir != dir.getOpposite()){
					if(canPush(buf.getBlockState(pos.offset(checkDir)), false)){
						if(list.size() >= PUSH_LIMIT || propogate(list, buf, pos.offset(checkDir), dir, null)){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		setExtension(world, pos, state.getValue(Properties.FACING), 0);
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn){
		if(worldIn.isRemote){
			return;
		}
		checkRedstone(worldIn, pos, state.getValue(Properties.FACING));
	}
	
	@Override
	public int damageDropped(IBlockState state){
		return 0;
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, new IProperty[] {Properties.FACING, Properties.REDSTONE_BOOL});
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(Properties.FACING, EnumFacing.getFront(meta & 7)).withProperty(Properties.REDSTONE_BOOL, (meta & 8) == 8);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(Properties.FACING).getIndex() + (state.getValue(Properties.REDSTONE_BOOL) ? 8 : 0);
	}
	
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state){
		return state.getValue(Properties.REDSTONE_BOOL) ? EnumPushReaction.BLOCK : EnumPushReaction.NORMAL;
	}
}