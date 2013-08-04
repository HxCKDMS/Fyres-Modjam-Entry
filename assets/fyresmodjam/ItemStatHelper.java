package assets.fyresmodjam;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import cpw.mods.fml.common.ICraftingHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;

import assets.fyresmodjam.EntityStatHelper.EntityStat;
import assets.fyresmodjam.EntityStatHelper.EntityStatTracker;
import assets.fyresmodjam.ItemStatHelper.ItemStat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet5PlayerInventory;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class ItemStatHelper {
	
	//There's probably a better way of doing all of this. :P Oh well.
	
	public static class ItemStatTracker {
		public Class[] classes;
		public int[] ids;
		
		public boolean instanceAllowed = false;
		
		public ItemStatTracker(Class[] classes, int[] ids, boolean instanceAllowed) {
			this.classes = classes;
			this.ids = ids;
			this.instanceAllowed = instanceAllowed;
		}
		
		public ItemStatTracker(Class c, int id, boolean instanceAllowed) {this(new Class[] {c}, new int[] {id}, instanceAllowed);}

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
		
		public ItemStat(String name, Object value) {
			this.name = name;
			this.value = value.toString();
		}
		
		public Object getNewValue(ItemStack stack, Random r) {return value;}
		public String getLore(ItemStack stack) {return null;}
		public String getAlteredStackName(ItemStack stack) {return stack.getDisplayName();}
		public void modifyStack(ItemStack stack) {}
	}
	
	public static HashMap<Class, ItemStatTracker> statTrackersByClass = new HashMap<Class, ItemStatTracker>();
	public static HashMap<Integer, ItemStatTracker> statTrackersByID = new HashMap<Integer, ItemStatTracker>();
	
	public static ArrayList<ItemStatTracker> genericTrackers = new ArrayList<ItemStatTracker>();
	
	public static void addStatTracker(ItemStatTracker statTracker) {
		if(statTracker.classes != null) {for(Class c : statTracker.classes) {statTrackersByClass.put(c, statTracker);}}
		if(statTracker.ids != null) {for(int i : statTracker.ids) {if(i < 0) {continue;} statTrackersByID.put(i, statTracker);}}
		if(statTracker.instanceAllowed) {genericTrackers.add(statTracker);}
	}
	
	public static ItemStack giveStat(ItemStack stack, String name, Object value) {
		if(!stack.hasTagCompound()) {stack.setTagCompound(new NBTTagCompound());}
		NBTTagCompound data = stack.stackTagCompound;
		data.setString(name, value.toString());
		return stack;
	}
	
	public static ItemStack setName(ItemStack stack, String name) {
		if(!stack.hasTagCompound()) {stack.setTagCompound(new NBTTagCompound());}
		if(!stack.getTagCompound().hasKey("display")) {stack.getTagCompound().setTag("display", new NBTTagCompound());}
		stack.getTagCompound().getCompoundTag("display").setString("Name", name);
		
		//if(getName(stack).equals(StatCollector.translateToLocal(stack.getItemName()))) {stack.getTagCompound().getCompoundTag("display").removeTag("Name");}
		
		return stack;
	}
	
	public static ItemStack addLore(ItemStack stack, String lore) {
		if(!stack.hasTagCompound()) {stack.setTagCompound(new NBTTagCompound());}
		if(!stack.getTagCompound().hasKey("display")) {stack.getTagCompound().setTag("display", new NBTTagCompound());}
		if(!stack.getTagCompound().getCompoundTag("display").hasKey("Lore")) {stack.getTagCompound().getCompoundTag("display").setTag("Lore", new NBTTagList());}
		if(lore != null) {stack.getTagCompound().getCompoundTag("display").getTagList("Lore").appendTag(new NBTTagString("", lore));}
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
			//processItemStack(event.item.getDataWatcher().getWatchableObjectItemStack(10), ModjamMod.r);
			
			ItemStack stack = event.item.getDataWatcher().getWatchableObjectItemStack(10);
			
			if(FyresWorldData.currentTask.equals("Collect") && stack.getItem().itemID == FyresWorldData.currentTaskID) {
				FyresWorldData.progress += stack.stackSize;
				
				if(FyresWorldData.progress > FyresWorldData.currentTaskAmount) {
					FyresWorldData.progress = 0;
					FyresWorldData.tasksCompleted++;
					
					CommonTickHandler.worldData.giveNewTask();
					
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eA world goal has been completed!" + (!FyresWorldData.currentDisadvantage.equals("None") ? " World disadvantage has been lifted!": "")}));
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eA new world goal has been set: " + (FyresWorldData.currentTask + " " + FyresWorldData.currentTaskAmount + " " + (FyresWorldData.currentTask.equals("Kill") ? FyresWorldData.validMobNames[FyresWorldData.currentTaskID] : new ItemStack(Item.itemsList[FyresWorldData.currentTaskID], 1).getDisplayName()) + "s. (" + FyresWorldData.progress + " " + FyresWorldData.currentTask + "ed)")}));
					
					FyresWorldData.currentDisadvantage = "None";
				}
				
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.UPDATE_WORLD_DATA, new Object[] {FyresWorldData.potionValues, FyresWorldData.potionDurations, FyresWorldData.currentDisadvantage, FyresWorldData.currentTask, FyresWorldData.currentTaskID, FyresWorldData.currentTaskAmount, FyresWorldData.progress, FyresWorldData.tasksCompleted}));
			}
		}
	}
	
	@ForgeSubscribe
	public void livingHurt(LivingHurtEvent event) {
		if(event.source != null && event.source.getEntity() != null) {
			if(event.source.getEntity() instanceof EntityLivingBase) {
				EntityLivingBase entity = (EntityLivingBase) event.source.getEntity();
				
				ItemStack held = entity.getCurrentItemOrArmor(0);
				
				if(held != null && (event.source.getDamageType().equals("player") || event.source.getDamageType().equals("mob") || (held.getItem().itemID == Item.bow.itemID && event.source.isProjectile()))) {
					String s = getStat(held, "BonusDamage");
					if(s != null) {event.ammount += Integer.parseInt(s);}
				}
			}
			
			float damageMultiplier = 1.0F;
			
			if(FyresWorldData.currentDisadvantage.equals("Weak") || (FyresWorldData.currentDisadvantage.equals("Tougher Mobs") && event.entity instanceof EntityMob)) {
				damageMultiplier -= 0.25F;
			}
			
			if(event.entity.getEntityData().hasKey("Blessing")) {
				if(event.entity.getEntityData().getString("Blessing").equals("Guardian")) {
					damageMultiplier -= 0.25F;
				}
			}
			
			if(event.source.getEntity().getEntityData().hasKey("Blessing")) {
				String blessing = event.source.getEntity().getEntityData().getString("Blessing");
				
				if(blessing.equals("Warrior") && (event.source.getDamageType().equals("player") || event.source.getDamageType().equals("mob"))) {
					damageMultiplier += 0.25F;
				} else if(blessing.equals("Hunter") && event.source.isProjectile()) {
					damageMultiplier += 0.25F;
				} else if(event.entityLiving != null && blessing.equals("Ninja") && event.source.getEntity().isSneaking() && event.entityLiving.func_110143_aJ() == event.entityLiving.func_110138_aP()) {
					damageMultiplier += 1.0F;
				} else if(blessing.equals("Swamp") && event.entityLiving != null) {
					event.entityLiving.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 1, false));
				} else if(blessing.equals("Vampire") && event.source.getEntity() instanceof EntityLivingBase) {
					((EntityLivingBase) event.source.getEntity()).heal(event.ammount * damageMultiplier * 0.1F);
				}
			}
			
			event.ammount *= damageMultiplier;
		}
	}
	
	public static ArrayList<ItemStatTracker> temp = new ArrayList<ItemStatTracker>();
	
	public static void processItemStack(ItemStack stack, Random r) {
		if(stack == null) {return;}
		
		temp.clear();
		
		if(statTrackersByClass.containsKey(stack.getItem().getClass())) {temp.add(statTrackersByClass.get(stack.getItem().getClass()));}
		if(statTrackersByID.containsKey(stack.getItem().itemID)) {temp.add(statTrackersByID.get(stack.getItem().itemID));}
		
		for(ItemStatTracker e : genericTrackers) {
			if(!temp.contains(e)) {
				for(Class c : e.classes) {if(c.isAssignableFrom(stack.getItem().getClass())) {temp.add(e); break;}}
			}
		}
		
		if(!temp.isEmpty()) {
			if(!stack.hasTagCompound()) {stack.setTagCompound(new NBTTagCompound());}
			
			String processed = ItemStatHelper.getStat(stack, "processed");
			
			if(processed == null || processed.equals("false")) {
				stack.getTagCompound().setTag("Lore", new NBTTagList());
				
				ItemStatHelper.giveStat(stack, "processed", "true");
				
				for(ItemStatTracker statTracker : temp) {
					for(ItemStat s : statTracker.stats) {
						giveStat(stack, s.name, s.getNewValue(stack, r).toString());
						
						String lore = s.getLore(stack);
						if(lore != null) {addLore(stack, lore);}
						
						setName(stack, s.getAlteredStackName(stack));
						
						s.modifyStack(stack);
					}
				}
			}
		}
		
		/*String stackName = getName(stack);
		if(FyresWorldData.currentDisadvantage.equals("Illiterate")) {
			if((stackName == null || !stackName.startsWith("\u00A7k"))) {
				setName(stack, "\u00A7k" + (stackName == null ? stack.getDisplayName() : stackName));
			}
		} else if(stackName != null && stackName.startsWith("\u00A7k")) {
			setName(stack, stackName.replace("\u00A7k", ""));
		}*/
	}

	/*@Override
	public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix) {
		if(player != null && !player.worldObj.isRemote) {
			processItemStack(item, ModjamMod.r);
			((EntityPlayerMP) player).sendContainerAndContentsToPlayer(player.openContainer, player.openContainer.getInventory());
			//player.openContainer.detectAndSendChanges();
			//Not what I'm looking for. :P PacketDispatcher.sendPacketToPlayer(new Packet5PlayerInventory(player.entityId, 0, item), (Player) player);
		}
	}

	@Override
	public void onSmelting(EntityPlayer player, ItemStack item) {
		if(player != null && !player.worldObj.isRemote) {
			processItemStack(item, ModjamMod.r);
			((EntityPlayerMP) player).sendContainerAndContentsToPlayer(player.openContainer, player.openContainer.getInventory());
		}
	}*/
	
	public void register() {
		MinecraftForge.EVENT_BUS.register(this);
		//GameRegistry.registerCraftingHandler(this);
	}
}
