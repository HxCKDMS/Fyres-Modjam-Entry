package assets.fyresmodjam;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityPillar extends TileEntity {

	public static String[] validBlessings = {"Miner", "Lumberjack"};
	public String blessing = null;
	
    public TileEntityPillar() {}

    public void updateEntity() {
    	super.updateEntity();
    	
    	if(worldObj.isRemote) {
    		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
    		
    		if(player != null && player.getEntityData().hasKey("Blessing")) {
    			for(int i = 0; i < 2; i++) {this.worldObj.spawnParticle("portal", this.xCoord + ModjamMod.r.nextDouble(), this.yCoord + ModjamMod.r.nextDouble() * 2, this.zCoord + ModjamMod.r.nextDouble(), (ModjamMod.r.nextDouble() - 0.5D) * 2.0D, -ModjamMod.r.nextDouble(), (ModjamMod.r.nextDouble() - 0.5D) * 2.0D);}
    		}
    	}
    }

    public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
    	super.writeToNBT(par1NBTTagCompound);
    	if(blessing == null) {blessing = validBlessings[ModjamMod.r.nextInt(validBlessings.length)];}
    	par1NBTTagCompound.setString("Blessing", blessing);
    }

    public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
    	super.readFromNBT(par1NBTTagCompound);
    	blessing = par1NBTTagCompound.hasKey("Blessing") ? par1NBTTagCompound.getString("Blessing") : validBlessings[ModjamMod.r.nextInt(validBlessings.length)];
    }

    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, tag);
    }

    public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) {this.readFromNBT(packet.customParam1);}
    
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
    	//fixes rendering bug, but on downside, always renders, even off screen
        return INFINITE_EXTENT_AABB;
    }
}