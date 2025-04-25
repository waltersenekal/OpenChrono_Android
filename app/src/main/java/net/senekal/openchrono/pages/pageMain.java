package net.senekal.openchrono.pages;

import static net.senekal.openchrono.utils.FileHelper.readFileContent;

import androidx.annotation.Nullable;

import net.senekal.openchrono.db.table.Ammo;
import net.senekal.openchrono.db.table.Device;
import net.senekal.openchrono.db.table.Gun;

import java.util.List;

public class pageMain {

    private static final String PLACEHOLDER_CHRONOGRAPH = "<!--#CHRONOGRAPH_DATA#-->";
    private static final String PLACEHOLDER_GUN = "<!--#GUN_DATA#-->";
    private static final String PLACEHOLDER_AMMO = "<!--#AMMO_DATA#-->";

    private static String CreateChronographData(@Nullable List<Device> allChronographs){
        if(allChronographs == null || allChronographs.isEmpty()) {
            return "<option value=\"none\" selected>No Devices (Add in Config First)</option>";
        }
        //Each Entry Needs to look Like this
        /* <option value="addNew" selected>Add New ...</option> */
        StringBuilder ResponseString = new StringBuilder();
        boolean firstEntry = true;
        for (Device chronograph : allChronographs) {
            ResponseString.append("<option value=\"")
                    .append(chronograph.macAddr)
                    .append("\" ");
            if (firstEntry) {
                ResponseString.append("selected");
                firstEntry = false;
            }
            ResponseString.append(">")
                    .append(chronograph.nameFriendly)
                    .append("</option>");
        }
        if(firstEntry){
            //If we don't find or have default value then the default is to add new
            ResponseString.append("<option value=\"none\" selected>No Devices (Add in Config First)</option>");

        }
        return ResponseString.toString();
    }
    private static String CreateGunData(@Nullable List<Gun> allGuns){
        if(allGuns == null || allGuns.isEmpty()) {
            return "<option value=\"none\" selected>No Guns (Add in Config First)</option>";
        }
        //Each Entry Needs to look Like this
        /* <option value="addNew" selected>Add New ...</option> */
        StringBuilder ResponseString = new StringBuilder();
        boolean firstEntry = true;
        for (Gun gun : allGuns) {
            ResponseString.append("<option value=\"")
                    .append(gun.name)
                    .append("~")
                    .append(gun.brand)
                    .append("\" ");
            if (firstEntry) {
                ResponseString.append("selected");
                firstEntry = false;
            }
            ResponseString.append(">")
                    .append(gun.name)
                    .append("</option>");
        }
        if(firstEntry){
            //If we don't find or have default value then the default is to add new
            ResponseString.append("<option value=\"none\" selected>No Guns (Add in Config First)</option>");
        }
        return ResponseString.toString();
    }

    private static String CreateAmmoData(@Nullable List<Ammo> allAmmos){
        if(allAmmos == null || allAmmos.isEmpty()) {
            return "<option value=\"none\" selected>No Ammo's (Add in Config First)</option>";
        }
        //Each Entry Needs to look Like this
        /* <option value="addNew" selected>Add New ...</option> */
        StringBuilder ResponseString = new StringBuilder();
        boolean firstEntry = true;
        for (Ammo ammo : allAmmos) {
            ResponseString.append("<option value=\"")
                    .append(ammo.name)
                    .append("~")
                    .append(ammo.brand)
                    .append("\" ");
            if (firstEntry) {
                ResponseString.append("selected");
                firstEntry = false;
            }
            ResponseString.append(">")
                    .append(ammo.name)
                    .append("</option>");
        }
        if(firstEntry){
            //If we don't find or have default value then the default is to add new
            ResponseString.append("<option value=\"none\" selected>No Ammo's (Add in Config First)</option>");
        }
        return ResponseString.toString();
    }


    public static String getPage(String htmlFolder, @Nullable List<Device> allChronographs, @Nullable List<Gun> allGuns, @Nullable List<Ammo> allAmmos) {
        String chronogragh_data = CreateChronographData(allChronographs);
        String gun_data = CreateGunData(allGuns);
        String ammo_data = CreateAmmoData(allAmmos);

        return getPage(htmlFolder,chronogragh_data,gun_data,ammo_data);
    }

    private static String getPage(String htmlFolder, String chronograghData, String gunData,String ammoData){
        String htmlData = readFileContent(htmlFolder + "/main_page.shtml");
        if (chronograghData != null && !chronograghData.isEmpty()) {
            htmlData = htmlData.replace(PLACEHOLDER_CHRONOGRAPH, chronograghData);
        }
        if (gunData != null && !gunData.isEmpty()) {
            htmlData = htmlData.replace(PLACEHOLDER_GUN, gunData);
        }
        if (ammoData != null && !ammoData.isEmpty()) {
            htmlData = htmlData.replace(PLACEHOLDER_AMMO, ammoData);
        }
        return htmlData;
    }
}
