package net.fexcraft.mod.addon.exivpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
		try{
			String packageName = this.getClass().getPackage().getName();
			String classDir = null;
			try{
				classDir = this.getClass().getClassLoader().getResource(packageName).getPath();
				URI classURI = new URI(classDir);
				classDir = classURI.getPath();
				classDir = classDir.substring(0, classDir.indexOf('!'));
			}
			catch(Exception e){
				classDir = "../src/main/resources/";
			}
			Class<?> packParserSystem = Class.forName("minecrafttransportsimulator.systems.PackParserSystem");
			Method getPackContentNamesMethod = packParserSystem.getMethod("getValidPackContentNames");
			String[] contentNames = (String[])getPackContentNamesMethod.invoke(null);
			for(String contentName : contentNames){
				List<String> entryNames = new ArrayList<String>();
				Method addContentMethod = packParserSystem.getMethod("add" + contentName.substring(0, 1).toUpperCase() + contentName.substring(1) + "Definition", InputStreamReader.class, String.class, String.class);
				File file = new File(classDir);
				System.out.println(file);
				System.out.println(file.toURI().toURL().toString());
				if(!file.isDirectory()){
					ZipFile jarFile = new ZipFile(classDir);
					Enumeration<? extends ZipEntry> entries = jarFile.entries();
					while(entries.hasMoreElements()){
						ZipEntry entry = entries.nextElement();
						if(entry.getName().endsWith(".json") && entry.getName().contains("jsondefs/" + contentName + "s")){
							entryNames.add(entry.getName());
						}
					}
					entryNames.sort(null);
					for(String entryName : entryNames){
						String entryFileName = entryName.substring(entryName.lastIndexOf('/') + 1, entryName.length() - ".json".length());
						addContentMethod.invoke(null, new InputStreamReader(jarFile.getInputStream(jarFile.getEntry(entryName)), "UTF-8"), entryFileName, MODID);
					}
					jarFile.close();
				}
				else{
					File folder = new File(file, "/assets/" + MODID + "/jsondefs/" + contentName + "s");
					if(!folder.exists()) folder.mkdirs();
					for(File fl : folder.listFiles()){
						if(!fl.getName().endsWith(".json")) continue;
						addContentMethod.invoke(null, new InputStreamReader(new FileInputStream(fl)), fl.getName().replace(".json", ""), MODID);
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event){
		try{
			Class<?> registry = Class.forName("minecrafttransportsimulator.dataclasses.MTSRegistry");
			Method getItemsMethod = registry.getMethod("getItemsForPack", String.class);
			List<Item> itemList = (List<Item>)getItemsMethod.invoke(null, MODID);
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
