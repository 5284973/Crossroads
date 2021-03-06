package com.Da_Technomancer.crossroads.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ObsidianCuttingKit extends Item{

	public ObsidianCuttingKit(){
		setUnlocalizedName("obsidianCuttingKit");
		setRegistryName("obsidianCuttingKit");
		GameRegistry.register(this);
		this.setCreativeTab(ModItems.tabCrossroads);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(worldIn.getBlockState(pos).getBlock() == Blocks.OBSIDIAN){
			if(!worldIn.isRemote){
				worldIn.destroyBlock(pos, true);
				if(!playerIn.isCreative() && --stack.stackSize <= 0){
					stack = null;
				}
			}
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
}
