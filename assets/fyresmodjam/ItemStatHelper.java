package assets.fyresmodjam;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import cpw.mods.fml.common.network.PacketDispatcher;

import assets.fyresmodjam.ItemStatHelper.ItemStat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class ItemStatHelper {
	
	//There's probably a better way of doing all of this. :P Oh well.
	
	public static class ItemStatTracker {
		public Class c;
		public int id;
		
		public ItemStatTracker(Class c, int id) {
			this.c = c;
			this.id = id;
		}

		//public HashMap<String, String> stats = new HashMap<String, String>();
		public ArrayList<ItemStat> stats = new ArrayList<ItemStat>();
		//public StatTracker giveStat(String name, String value) {stats.put(name, value); return this;}

		public void addStat(ItemStat stat) {
			if(!stats.contains(stat)) {stats.add(stat);}
		}
	}
	
	public static class ItemStat {
		public String name;
		public String value;
		
		public ItemStat(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		public Object getNewValue(Random r) {return value;}
		public String getLore(ItemStack stack) {return null;}
	}
	
	//public static String[] swordPrefixes = new String[] {"Old", "Sharp", "Average"};
	
	public static HashMap<Class, ItemStatTracker> statTrackersByClass = new HashMap<Class, ItemStatTracker>();
	public static HashMap<Integer, ItemStatTracker> statTrackersByID = new HashMap<Integer, ItemStatTracker>();
	
	public static void addStatTracker(ItemStatTracker statTracker, Class c, int id) {
		if(c != null) {statTrackersByClass.put(c, statTracker);}
		if(id >= 0) {statTrackersByID.put(id, statTracker);}
	}
	
	public static ItemStack giveStat(ItemStack stack, String name, String value) {
		if(!stack.hasTagCompound()) {stack.setTagCompound(new NBTTagCompound());}
		NBTTagCompound data = stack.stackTagCompound;
		data.setString(name, value);
		return stack;
	}
	
	public static ItemStack setName(ItemStack stack, String name) {
		if(!stack.hasTagCompound()) {stack.setTagCompound(new NBTTagCompound());}
		if(!stack.getTagCompound().hasKey("display")) {stack.getTagCompound().setTag("display", new NBTTagCompound());}
		stack.getTagCompound().getCompoundTag("display").setString("Name", name);
		return stack;
	}
	
	public static ItemStack addLore(ItemStack stack, String lore) {
		if(!stack.hasTagCompound()) {stack.setTagCompound(new NBTTagCompound());}
		if(!stack.getTagCompound().hasKey("display")) {stack.getTagCompound().setTag("display", new NBTTagCompound());}
		if(!stack.getTagCompound().getCompoundTag("display").hasKey("Lore")) {stack.getTagCompound().getCompoundTag("display").setTag("Lore", new NBTTagList());}
		stack.getTagCompound().getCompoundTag("display").getTagList("Lore").appendTag(new NBTTagString("", lore));
		return stack;
	}
	
	public static String getName(ItemStack stack) {
		if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("display") && stack.getTagCompound().getCompoundTag("display").hasKey("Name")) {
			return stack.getTagCompound().getCompoundTag("display").getString("Name");
		}
		
		return null;
	}
	
	public static String getStat(ItemStack stack, String name) {
		String s = null;
		if(stack.getTagCompound() != null && stack.getTagCompound().hasKey(name)) {s = stack.getTagCompound().getString(name);}
		return s;
	}
	
	public static boolean hasStat(ItemStack stack, String name) {
		if(stack.getTagCompound() != null && stack.getTagCompound().hasKey(name)) {return true;}
		return false;
	}
	
	/*@ForgeSubscribe
	public void entityJoinWorld(EntityJoinWorldEvent event) {
		if(!event.world.isRemote && event.entity instanceof EntityItem) {
			EntityItem item = (EntityItem) event.entity;
			processItemStack(item.getDataWatcher().getWatchableObjectItemStack(10), ModjamMod.r);
		}
	}*/
	
	@ForgeSubscribe
	public void itemPickUp(EntityItemPickupEvent event) {
		if(!event.entityPlayer.worldObj.isRemote) {
			processItemStack(event.item.getDataWatcher().getWatchableObjectItemStack(10), ModjamMod.r);
		}
	}
	
	@ForgeSubscribe
	public void livingHurt(LivingHurtEvent event) {
		if(event.source != null && event.source.getEntity() != null && event.source.getEntity() instanceof EntityLivingBase) {
			EntityLivingBase entity = (EntityLivingBase) event.source.getEntity();
			
			ItemStack held = entity.getCurrentItemOrArmor(0);
			
			if(held != null) {
				String s = getStat(held, "BonusDamage");
				if(s != null) {event.ammount += Integer.parseInt(s);}
			}
		}
	}
	
	public static void processItemStack(ItemStack stack, Random r) {
		Class c = stack.getItem().getClass();
		int id = stack.itemID;
		
		if(stack != null && (statTrackersByClass.containsKey(c) || statTrackersByID.containsKey(id))) {
			String processed = ItemStatHelper.getStat(stack, "processed");
			if(processed == null || processed.equals("false")) {
				
				ItemStatTracker statTrackerClass = statTrackersByClass.get(c);
				ItemStatTracker statTrackerID = statTrackersByID.get(c);
				
				ItemStatHelper.giveStat(stack, "processed", "true");
				
				if(statTrackerClass != null) {
					for(ItemStat s : statTrackerClass.stats) {
						/*String[] value = statTrackerClass.stats.get(s).split(",");
						if(value.length == 3 && value[0].equals("#i")) {value[0] = "" + (Integer.parseInt(value[1]) + ModjamMod.r.nextInt(Integer.parseInt(value[2])));}
						String[] data = s.replace("%v", value[0]).split(",");
						
						System.out.println(data + ", " + value);
						
						giveStat(stack, data[0], value[0]);
						
						if(data.length > 1) {addLore(stack, data[1]);}*/
						
						giveStat(stack, s.name, s.getNewValue(r).toString());
						
						String lore = s.getLore(stack);
						if(lore != null) {addLore(stack, lore);}
					}
				}
				
				if(statTrackerID != null) {
					for(ItemStat s : statTrackerID.stats) {
						giveStat(stack, s.name, s.getNewValue(r).toString());
						
						String lore = s.getLore(stack);
						if(lore != null) {addLore(stack, lore);}
					}
				}
				
				//Apparently is was syncing fine. :P
				
				/*try {
	                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
	                DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
	                //dataoutputstream.writeInt(this.currentWindowId);
	                Packet.writeItemStack(stack, dataoutputstream);
	                //PacketDispatcher.sendPacketToAllPlayers(new Packet250CustomPayload("MC|TrList", bytearrayoutputstream.toByteArray()));
	            } catch (IOException ioexception) {ioexception.printStackTrace();}*/
			}
		}
	}
}
