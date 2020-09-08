package net.fexcraft.mod.addon.exivpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.systems.PackParserSystem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = ExIvPack.MODID, name = ExIvPack.NAME, version = ExIvPack.VERSION, useMetadata = true, dependencies = "required-after:mts@[18.0.0,);")
public class ExIvPack {
	
    public static final String MODID = "exivpack";
    public static final String NAME = "Experimental Pack";
    public static final String VERSION = "1.0";

    /** code to get file partially from fvtm */
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event){
		ModCandidate mod = (ModCandidate)event.getAsmData().getCandidatesFor("net.fexcraft.mod.addon.exivpack").toArray()[0];
		ContainerType type = mod.getSourceType();
		System.out.println(mod.getModContainer());
    	for(String str : PackParserSystem.getValidPackContentNames()){
			try{
	    		ArrayList<String> entryNames = new ArrayList<String>();
				Method addContentMethod = PackParserSystem.class.getMethod("add" + str.substring(0, 1).toUpperCase() + str.substring(1) + "Definition", InputStreamReader.class, String.class, String.class);
				if(type == ContainerType.JAR){
					ZipFile jarFile = new ZipFile(mod.getModContainer());
					Enumeration<? extends ZipEntry> entries = jarFile.entries();
					while(entries.hasMoreElements()){
						ZipEntry entry = entries.nextElement();
						if(entry.getName().endsWith(".json") && entry.getName().contains("jsondefs/" + str + "s")){
							entryNames.add(entry.getName());
						}
					}
					entryNames.sort(null);
					for(String entryName : entryNames){
						String entryFileName = entryName.substring(entryName.lastIndexOf('/') + 1, entryName.length() - ".json".length());
						addContentMethod.invoke(null, new InputStreamReader(jarFile.getInputStream(jarFile.getEntry(entryName)), "UTF-8"), entryFileName, MODID);
					}
				}
				else{//DIR
					File folder = new File(mod.getModContainer(), "/assets/" + MODID + "/jsondefs/" + str + "s");
					if(!folder.exists()) folder.mkdirs();
					for(File file : folder.listFiles()){
						if(!file.getName().endsWith(".json")) continue;
						addContentMethod.invoke(null, new InputStreamReader(new FileInputStream(file)), file.getName().replace(".json", ""), MODID);
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
    	}
	}
    
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event){
		try{
			List<Item> itemList = MTSRegistry.getItemsForPack(MODID);
			for(Item item : itemList){
				item.setRegistryName(new ResourceLocation(MODID, item.getUnlocalizedName().replace("item." + MODID + ".", "")));
				event.getRegistry().register(item);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
    
}
