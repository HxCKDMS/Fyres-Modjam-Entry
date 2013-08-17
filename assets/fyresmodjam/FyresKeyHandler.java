package assets.fyresmodjam;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;

public class FyresKeyHandler extends KeyHandler {
	
	public static KeyBinding examine = new KeyBinding("Examine", Keyboard.KEY_X);
	
	public static KeyBinding[] keyBindings = new KeyBinding[] {examine};
	public static boolean[] repeat = new boolean[] {false};

	public FyresKeyHandler() {
		super(keyBindings, repeat);
	}

	@Override
	public String getLabel() {
		return "KeyHandler for FyresModJamMod";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
		
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		if(kb.equals(examine) && tickEnd) {
			Minecraft minecraft = Minecraft.getMinecraft();
			EntityPlayer player = minecraft.thePlayer;
			
			if(player != null && minecraft.objectMouseOver != null) {
				
				MovingObjectPosition o = minecraft.objectMouseOver;
				
				if(o.typeOfHit == EnumMovingObjectType.TILE) {
					int x = minecraft.objectMouseOver.blockX;
					int y = minecraft.objectMouseOver.blockY;
					int z = minecraft.objectMouseOver.blockZ; 
					
					if(minecraft.theWorld.getBlockId(x, y, z) == ModjamMod.blockPillar.blockID && minecraft.theWorld.getBlockMetadata(x, y, z) == 1) {y--;}
					
					TileEntity te = minecraft.theWorld.getBlockTileEntity(x, y, z);
					
					if(te != null && te instanceof TileEntityPillar) {
						int index = 0;
						for(int i = 0; i < TileEntityPillar.validBlessings.length; i++) {if(TileEntityPillar.validBlessings[i].equals(((TileEntityPillar) te).blessing)) {index = i; break;}}
						Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("\u00A7eBlessing of the " + ((TileEntityPillar) te).blessing + ": " + TileEntityPillar.blessingDescriptions[index] + ".");
					} else if(te != null && te instanceof TileEntityTrap) {
						String placedBy = ((TileEntityTrap) te).placedBy;
						
						if(placedBy != null) {
							Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("\u00A7eThis " + ItemTrap.names[te.getBlockMetadata() % BlockTrap.trapTypes].toLowerCase() + " was placed by " + (placedBy.equals(player.getEntityName()) ? "you": placedBy) + ".");
						} else {
							Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("\u00A7eThis " + ItemTrap.names[te.getBlockMetadata() % BlockTrap.trapTypes].toLowerCase() + " doesn't seem to have been placed by anyone.");
						}
					} else {
						String name = new ItemStack(minecraft.theWorld.getBlockId(x, y, z), 1, minecraft.theWorld.getBlockMetadata(x, y, z)).getDisplayName().toLowerCase();
						Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("\u00A7eIt's a " + name + (!name.contains("block") ? " block." : "."));
					}
				} else if(o.typeOfHit == EnumMovingObjectType.ENTITY && o.entityHit != null) {
					PacketDispatcher.sendPacketToServer(PacketHandler.newPacket(PacketHandler.EXAMINE_MOB, new Object[] {o.entityHit.dimension, o.entityHit.entityId}));
				}
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

}
