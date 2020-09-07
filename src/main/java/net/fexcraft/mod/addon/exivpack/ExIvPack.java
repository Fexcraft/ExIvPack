package net.fexcraft.mod.addon.exivpack;

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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = ExIvPack.MODID, name = ExIvPack.NAME, version = ExIvPack.VERSION, useMetadata = true, dependencies = "required-after:mts@[18.0.0,);")
public class ExIvPack {
	
    public static final String MODID = "exivpack";
    public static final String NAME = "Experimental Pack";
    public static final String VERSION = "1.0";
    
    public ExIvPack(){
		String packageName = this.getClass().getPackage().getName();
		String classDir = this.getClass().getClassLoader().getResource(packageName).getPath();
    	for(String str : PackParserSystem.getValidPackContentNames()){
			try{
	    		ArrayList<String> entryNames = new ArrayList<String>();
				ZipFile jarFile = new ZipFile(classDir);
				Enumeration<? extends ZipEntry> entries = jarFile.entries();
				while(entries.hasMoreElements()){
					ZipEntry entry = entries.nextElement();
					if(entry.getName().endsWith(".json") && entry.getName().contains("jsondefs/" + str + "s")){
						entryNames.add(entry.getName());
					}
				}
				Method addContentMethod = PackParserSystem.class.getMethod("add" + str.substring(0, 1).toUpperCase() + str.substring(1) + "Definition", InputStreamReader.class, String.class, String.class);
				entryNames.sort(null);
				for(String entryName : entryNames){
					String entryFileName = entryName.substring(entryName.lastIndexOf('/') + 1, entryName.length() - ".json".length());
					addContentMethod.invoke(null, new InputStreamReader(jarFile.getInputStream(jarFile.getEntry(entryName)), "UTF-8"), entryFileName, MODID);
				}
				jarFile.close();
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
				item.setRegistryName(new ResourceLocation(MODID, item.getTranslationKey().replace("item." + MODID + ".", "")));
				event.getRegistry().register(item);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
    
}
