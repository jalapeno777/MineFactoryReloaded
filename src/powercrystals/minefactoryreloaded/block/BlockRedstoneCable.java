package powercrystals.minefactoryreloaded.block;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import powercrystals.core.position.BlockPosition;
import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import powercrystals.minefactoryreloaded.api.IToolHammer;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetNetworkContainer;
import powercrystals.minefactoryreloaded.api.rednet.RedNetConnectionType;
import powercrystals.minefactoryreloaded.gui.MFRCreativeTab;
import powercrystals.minefactoryreloaded.tile.rednet.TileRedstoneCable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRedstoneCable extends BlockContainer implements IRedNetNetworkContainer
{
	private static float _wireSize = 0.25F;
	private static float _plateWidth = 14.0F / 16.0F;
	private static float _plateDepth = 1.0F / 16.0F;
	private static float _bandWidth = 5.0F / 16.0F;
	private static float _bandOffset = 2.0F / 16.0F;
	private static float _bandDepth = 1.0F / 16.0F;
	
	private static float _wireStart = 0.5F - _wireSize / 2.0F;
	private static float _wireEnd = 0.5F + _wireSize / 2.0F;
	private static float _plateStart = 0.5F - _plateWidth / 2.0F;
	private static float _plateEnd = 0.5F + _plateWidth / 2.0F;
	private static float _bandWidthStart = 0.5F - _bandWidth / 2.0F;
	private static float _bandWidthEnd = 0.5F + _bandWidth / 2.0F;
	
	private static float _bandDepthStart = _bandOffset;
	private static float _bandDepthEnd = _bandOffset + _bandDepth;
	
	private static int[] _partSideMappings = new int[] { -1, -1, -1, 4, 5, 0, 1, 2, 3, 4, 5, 0, 1, 2, 3 };
	
	public BlockRedstoneCable(int id)
	{
		super(id, Material.clay);
		
		setUnlocalizedName("mfr.cable.redstone");
		setHardness(0.8F);
		
		setCreativeTab(MFRCreativeTab.tab);
	}
	
	private AxisAlignedBB[] getParts(TileRedstoneCable cable)
	{
		RedNetConnectionType csu = cable.getConnectionState(ForgeDirection.UP);
		RedNetConnectionType csd = cable.getConnectionState(ForgeDirection.DOWN);
		RedNetConnectionType csn = cable.getConnectionState(ForgeDirection.NORTH);
		RedNetConnectionType css = cable.getConnectionState(ForgeDirection.SOUTH);
		RedNetConnectionType csw = cable.getConnectionState(ForgeDirection.WEST);
		RedNetConnectionType cse = cable.getConnectionState(ForgeDirection.EAST); 
		
		AxisAlignedBB[] parts = new AxisAlignedBB[15];
		
		parts[0] = AxisAlignedBB.getBoundingBox(csw != RedNetConnectionType.None ? 0 : _wireStart, _wireStart, _wireStart, cse != RedNetConnectionType.None ? 1 : _wireEnd, _wireEnd, _wireEnd);
		parts[1] = AxisAlignedBB.getBoundingBox(_wireStart, csd != RedNetConnectionType.None ? 0 : _wireStart, _wireStart, _wireEnd, csu != RedNetConnectionType.None ? 1 : _wireEnd, _wireEnd);
		parts[2] = AxisAlignedBB.getBoundingBox(_wireStart, _wireStart, csn != RedNetConnectionType.None ? 0 : _wireStart, _wireEnd, _wireEnd, css != RedNetConnectionType.None ? 1 : _wireEnd);
		
		parts[3] = csw != RedNetConnectionType.PlateSingle && csw != RedNetConnectionType.PlateAll ? null : AxisAlignedBB.getBoundingBox(0, _plateStart, _plateStart, _plateDepth, _plateEnd, _plateEnd);
		parts[4] = cse != RedNetConnectionType.PlateSingle && cse != RedNetConnectionType.PlateAll ? null : AxisAlignedBB.getBoundingBox(1.0F - _plateDepth, _plateStart, _plateStart, 1.0F, _plateEnd, _plateEnd);
		parts[5] = csd != RedNetConnectionType.PlateSingle && csd != RedNetConnectionType.PlateAll ? null : AxisAlignedBB.getBoundingBox(_plateStart, 0 , _plateStart, _plateEnd, _plateDepth, _plateEnd);
		parts[6] = csu != RedNetConnectionType.PlateSingle && csu != RedNetConnectionType.PlateAll ? null : AxisAlignedBB.getBoundingBox(_plateStart, 1.0F - _plateDepth, _plateStart, _plateEnd, 1.0F, _plateEnd);
		parts[7] = csn != RedNetConnectionType.PlateSingle && csn != RedNetConnectionType.PlateAll ? null : AxisAlignedBB.getBoundingBox(_plateStart, _plateStart, 0, _plateEnd, _plateDepth, _plateEnd);
		parts[8] = css != RedNetConnectionType.PlateSingle && css != RedNetConnectionType.PlateAll ? null : AxisAlignedBB.getBoundingBox(_plateStart, _plateStart, 1.0F - _plateDepth, _plateEnd, _plateEnd, 1.0F);
		
		parts[9]  = csw != RedNetConnectionType.PlateSingle && csw != RedNetConnectionType.CableSingle ? null : AxisAlignedBB.getBoundingBox(_bandDepthStart, _bandWidthStart, _bandWidthStart, _bandDepthEnd, _bandWidthEnd, _bandWidthEnd);
		parts[10] = cse != RedNetConnectionType.PlateSingle && cse != RedNetConnectionType.CableSingle ? null : AxisAlignedBB.getBoundingBox(1.0F - _bandDepthEnd, _bandWidthStart, _bandWidthStart, 1.0F - _bandDepthStart, _bandWidthEnd, _bandWidthEnd);
		parts[11] = csd != RedNetConnectionType.PlateSingle && csd != RedNetConnectionType.CableSingle ? null : AxisAlignedBB.getBoundingBox(_bandWidthStart, _bandDepthStart, _bandWidthStart, _bandWidthEnd, _bandDepthEnd, _bandWidthEnd);
		parts[12] = csu != RedNetConnectionType.PlateSingle && csu != RedNetConnectionType.CableSingle ? null : AxisAlignedBB.getBoundingBox(_bandWidthStart, 1.0F - _bandDepthEnd, _bandWidthStart, _bandWidthEnd, 1.0F - _bandDepthStart, _bandWidthEnd);
		parts[13] = csn != RedNetConnectionType.PlateSingle && csn != RedNetConnectionType.CableSingle ? null : AxisAlignedBB.getBoundingBox(_bandWidthStart, _bandWidthStart, _bandDepthStart, _bandWidthEnd, _bandWidthEnd, _bandDepthEnd);
		parts[14] = css != RedNetConnectionType.PlateSingle && css != RedNetConnectionType.CableSingle ? null : AxisAlignedBB.getBoundingBox(_bandWidthStart, _bandWidthStart, 1.0F - _bandDepthEnd, _bandWidthEnd, _bandWidthEnd, 1.0F - _bandDepthStart);
		
		return parts;
	}
	
	private int getPartClicked(EntityPlayer player, double reachDistance, TileRedstoneCable cable)
	{
		AxisAlignedBB[] wireparts = getParts(cable);
		
		Vec3 playerPosition = Vec3.createVectorHelper(player.posX - cable.xCoord, player.posY - cable.yCoord + player.getEyeHeight(), player.posZ - cable.zCoord);
		Vec3 playerLook = player.getLookVec();
		
		Vec3 playerViewOffset = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord * reachDistance, playerPosition.yCoord + playerLook.yCoord * reachDistance, playerPosition.zCoord + playerLook.zCoord * reachDistance);
		int closest = -1;
		double closestdistance = Double.MAX_VALUE;
		
		for(int i = 0; i < wireparts.length; i++)
		{
			AxisAlignedBB part = wireparts[i];
			if(part == null)
			{
				continue;
			}
			MovingObjectPosition hit = part.calculateIntercept(playerPosition, playerViewOffset);
			if(hit != null)
			{
				double distance = playerPosition.distanceTo(hit.hitVec);
				if(distance < closestdistance)
				{
					distance = closestdistance;
					closest = i;
				}
			}
		}
		return closest;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float xOffset, float yOffset, float zOffset)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileRedstoneCable)
		{
			TileRedstoneCable cable = (TileRedstoneCable)te;
			
			int subHit = getPartClicked(player, 3.0F, cable);
			
			if(subHit < 0)
			{
				return false;
			}
			side = _partSideMappings[subHit];
			
			if(side >= 0)
			{
				ItemStack s = player.inventory.getCurrentItem();
				if(s != null && s.getItem() instanceof IToolHammer)
				{
					if(!world.isRemote)
					{
						int nextColor = cable.getSideColor(ForgeDirection.getOrientation(side)) + 1;
						if(nextColor > 15) nextColor = 0;
						cable.setSideColor(ForgeDirection.getOrientation(side), nextColor);
						world.markBlockForUpdate(x, y, z);
						return true;
					}
				}
				else if(s != null && s.itemID == Item.dyePowder.itemID)
				{
					if(!world.isRemote)
					{
						cable.setSideColor(ForgeDirection.getOrientation(side), 15 - s.getItemDamage());
						world.markBlockForUpdate(x, y, z);
						return true;
					}
				}
			}
			else
			{
				byte mode = cable.getMode();
				mode++;
				if(mode > 2)
				{
					mode = 0;
				}
				cable.setMode(mode);
				//PacketDispatcher.sendPacketToAllAround(x, y, z, 50, world.provider.dimensionId, cable.getDescriptionPacket());
				if(world.isRemote)
				{
					if(mode == 0)
					{
						player.sendChatToPlayer("Set cable to standard connection mode");
					}
					else if(mode == 1)
					{
						player.sendChatToPlayer("Set cable to forced-connection mode");
					}
					else if(mode == 2)
					{
						player.sendChatToPlayer("Set cable to cable-only connection mode");
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		float xMin = 1;
		float yMin = 1;
		float zMin = 1;
		float xMax = 0;
		float yMax = 0;
		float zMax = 0;
		
		TileEntity cable = world.getBlockTileEntity(x, y, z);
		if(cable instanceof TileRedstoneCable)
		{
			for(AxisAlignedBB aabb : getParts((TileRedstoneCable)cable))
			{
				if(aabb == null)
				{
					continue;
				}
				
				xMin = Math.min(xMin, (float)aabb.minX);
				yMin = Math.min(yMin, (float)aabb.minY);
				zMin = Math.min(zMin, (float)aabb.minZ);
				xMax = Math.max(xMax, (float)aabb.maxX);
				yMax = Math.max(yMax, (float)aabb.maxY);
				zMax = Math.max(zMax, (float)aabb.maxZ);
			}
			setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);
		}
		else
		{
			super.setBlockBoundsBasedOnState(world, x, y, z);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB collisionTest, List collisionBoxList, Entity entity)
	{
		TileEntity cable = world.getBlockTileEntity(x, y, z);
		if(cable instanceof TileRedstoneCable)
		{
			for(AxisAlignedBB aabb : getParts((TileRedstoneCable)cable))
			{
				if(aabb == null)
				{
					continue;
				}
				aabb = AxisAlignedBB.getBoundingBox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
				aabb.minX += x;
				aabb.maxX += x;
				aabb.minY += y;
				aabb.maxY += y;
				aabb.minZ += z;
				aabb.maxZ += z;
				if(collisionTest.intersectsWith(aabb))
				{
					collisionBoxList.add(aabb);
				}
			}
		}
		else
		{
			super.addCollisionBoxesToList(world, x, y, z, collisionTest, collisionBoxList, entity);
		}
	}
	
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockId)
	{
		super.onNeighborBlockChange(world, x, y, z, blockId);
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileRedstoneCable)
		{
			((TileRedstoneCable)te).onNeighboorChanged();
		}
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileRedstoneCable && ((TileRedstoneCable)te).getNetwork() != null)
		{
			((TileRedstoneCable)te).getNetwork().setInvalid();
		}
		for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
		{
			BlockPosition bp = new BlockPosition(x, y, z);
			bp.orientation = d;
			bp.moveForwards(1);
			world.notifyBlockOfNeighborChange(bp.x, bp.y, bp.z, MineFactoryReloadedCore.rednetCableBlock.blockID);
			world.notifyBlocksOfNeighborChange(bp.x, bp.y, bp.z, MineFactoryReloadedCore.rednetCableBlock.blockID);
		}
		super.breakBlock(world, x, y, z, id, meta);
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side)
	{
		int power = 0;
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileRedstoneCable && ((TileRedstoneCable)te).getNetwork() != null)
		{
			int subnet = ((TileRedstoneCable)te).getSideColor(ForgeDirection.getOrientation(side).getOpposite());
			power = Math.min(Math.max(((TileRedstoneCable)te).getNetwork().getPowerLevelOutput(subnet), 0), 15);
			//System.out.println("Asked for weak power at " + x + "," + y + "," + z + " - got " + power + " from network " + ((TileRedstoneCable)te).getNetwork().getId() + ":" + subnet);
		}
		return power;
	}
	
	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side)
	{
		int power = 0;
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileRedstoneCable && ((TileRedstoneCable)te).getNetwork() != null)
		{
			TileRedstoneCable cable = ((TileRedstoneCable)te);
			
			BlockPosition nodebp = new BlockPosition(x, y, z, ForgeDirection.getOrientation(side).getOpposite());
			nodebp.moveForwards(1);

			int subnet = cable.getSideColor(nodebp.orientation);
			
			if(cable.getNetwork().isWeakNode(nodebp))
			{
				power = 0;
				//System.out.println("Asked for strong power at " + x + "," + y + "," + z + " - weak node, power 0");
			}
			else
			{
				power = Math.min(Math.max(cable.getNetwork().getPowerLevelOutput(subnet), 0), 15);
				//System.out.println("Asked for strong power at " + x + "," + y + "," + z + " - got " + power + " from network " + ((TileRedstoneCable)te).getNetwork().getId() + ":" + subnet);
			}
		}
		return power;
	}
	
	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side)
	{
		return true;
	}
	
	@Override
	public boolean canProvidePower()
	{
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileRedstoneCable();
	}
	
	@Override
	public int getRenderType()
	{
		return MineFactoryReloadedCore.renderIdRedstoneCable;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister ir)
	{
		blockIcon = ir.registerIcon("powercrystals/minefactoryreloaded/" + getUnlocalizedName());
	}
	
	@Override
	public void updateNetwork(World world, int x, int y, int z)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileRedstoneCable && ((TileRedstoneCable)te).getNetwork() != null)
		{
			((TileRedstoneCable)te).getNetwork().updatePowerLevels();
		}
	}
	
	@Override
	public void updateNetwork(World world, int x, int y, int z, int subnet)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileRedstoneCable && ((TileRedstoneCable)te).getNetwork() != null)
		{
			((TileRedstoneCable)te).getNetwork().updatePowerLevels(subnet);
		}
	}
}
