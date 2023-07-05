package com.byjus.zendeskMigration.util;

import com.byjus.zendeskMigration.pojo.IWithID;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author koushik.majhi1@byjus.com
 * @created 04/07/2023 - 5:05 pm
 */

@Component
public class FileHelper {

    String CompiledDataPath = "/Users/tnluser/Desktop/work/wfms/repositpries/wfms-zendesk-migration-v2/Data/Compiled-" + "stage" +"/";
    String DownloadedDataPath =  "/Users/tnluser/Desktop/work/wfms/repositpries/wfms-zendesk-migration-v2/Data/Downloaded/";;

    public String getFilePath(String fileName) {
        return DownloadedDataPath + fileName + ".json";
    }
    public String getCompiledFilePath(String fileName) {
        return CompiledDataPath + fileName + ".json";
    }

    public <T extends IWithID> HashMap<String, T> getData(String fileName, Type type) {
        if (Files.exists(Path.of((fileName)))) {
            ArrayList<T> lst = FileHelper.ReadJsonObject(fileName, type);
            HashMap<String, T> dict = new HashMap<String, T>();
            for (var item : lst) {
                dict.put(item.getId(), item);
            }
            return dict;
        }
        return new HashMap<String, T>();
    }

    public <T extends IWithID> ConcurrentHashMap<String, T> getConcurrentData(String fileName, Type type) {
        if (Files.exists(Path.of((fileName)))) {
            Type empMapType = new TypeToken<T>() {
            }.getType();
            ArrayList<T> lst = FileHelper.ReadJsonObject(fileName, type);
            ConcurrentHashMap<String, T> dict = new ConcurrentHashMap<String, T>();
            for (var item : lst) {
                dict.put(item.getId(), item);
            }
            return dict;
        }
        return new ConcurrentHashMap<String, T>();
    }

    public static <T> ArrayList<T> ReadJsonObject(String fileName, Type type) {
        try {

            String s = Files.readString(Path.of(fileName));
            ArrayList<T> t = new Gson().fromJson(s, type);
            return t;
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }

    public void saveFile(String filePath, Object obj) throws Exception {
        try {
            String s = new GsonBuilder().setPrettyPrinting().create().toJson(obj);
            Files.writeString(Path.of(filePath), s);
        } catch (Exception ex) {
            System.out.println(ex.toString() + ex.getStackTrace());
            throw ex;
        }
    }
}
