package net.senekal.openchrono.pages;

import static net.senekal.openchrono.utils.FileHelper.readFileContent;

import net.senekal.openchrono.db.table.Ammo;
import net.senekal.openchrono.db.table.Device;
import net.senekal.openchrono.db.table.Gun;
import net.senekal.openchrono.db.table.ShotEntry;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class pageChronographMain {

    private static final String PLACEHOLDER_CHRONOGRAPH = "<!--#CHRONOGRAPH_DATA#-->";
    private static final String PLACEHOLDER_GUN = "<!--#GUN_DATA#-->";
    private static final String PLACEHOLDER_AMMO = "<!--#AMMO_DATA#-->";
    private static final String PLACEHOLDER_CONNECTION = "<!--#CONNECTION_STATUS#-->";
    private static final String PLACEHOLDER_SHOTS = "<!--#SHOTS_DATA#-->";

    private static String CreateChronographData(Device chronograph){
        return chronograph.nameFriendly + ":" + chronograph.macAddr;
    }
    private static String CreateGunData(Gun gun){

        return gun.name + ":" + gun.brand;
    }
    private static String CreateAmmoData(Ammo ammo){

        return ammo.name + ":" + ammo.brand;
    }
    private static String GetFPS(ShotEntry shot){
        if(shot.timein_us == 0){
            return "0";
        }
        // Convert 6 cm to feet (1 cm = 0.0328084 feet)
        double distanceInFeet = 6 * 0.0328084;
        // Calculate FPS
        double fps = distanceInFeet / ((double) shot.timein_us / 1_000_000);
        return String.format(Locale.US,"%.2f", fps);
    }
    private static String GetMPS(ShotEntry shot){
        if (shot.timein_us == 0) {
            return "0";
        }
        // Convert 6 cm to meters
        double distanceInMeters = 6 * 0.01;
        // Calculate MPS
        double mps = distanceInMeters / ((double) shot.timein_us / 1_000_000);
        return String.format(Locale.US,"%.2f", mps);
    }
    private static String GetEnergy(ShotEntry shot, Ammo ammo) {
        if (shot.timein_us == 0 || ammo.weight == 0) {
            return "0";
        }
        // Convert ammo weight to kilograms
        double massInKg = ammo.weight * 0.001;
        // Get velocity in meters per second
        double velocityInMps = Double.parseDouble(GetMPS(shot));
        // Calculate energy in joules
        double energy = 0.5 * massInKg * Math.pow(velocityInMps, 2);
        return String.format(Locale.US,"%.2f", energy);
    }
    private static String formatTimestamp(long timestamp) {
        // Create a SimpleDateFormat instance with the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.US);
        // Convert the timestamp to a Date object and format it
        return dateFormat.format(new Date(timestamp));
    }
    private static String CreateShotsData(List<ShotEntry> shots,Ammo ammo){
        StringBuilder ResponseString = new StringBuilder();
        ResponseString.append("<tbody>");
        if(shots.isEmpty()){
            ResponseString.append("<tr><th>No Entries Yet</th>");
        }
        else{
            Collections.reverse(shots); // Reverse the order of the list
            for (ShotEntry shot : shots) {
                ResponseString.append("<tr>")
                        .append("<td>")
                        .append(shot.timestamp).append("uS")
                        .append("</td>")
                        .append("<td>")
                        .append(GetFPS(shot))
                        .append("</td>")
                        .append("<td>")
                        .append(GetMPS(shot))
                        .append("</td>")
                        .append("<td>")
                        .append(GetEnergy(shot,ammo)).append("J")
                        .append("</td>")
                        .append("<td>")
                        .append(formatTimestamp(shot.timestamp))
                        .append("</td>")
                        .append("</tr>");
            }
        }
        ResponseString.append("</tbody>");
        return ResponseString.toString();
    }

    public static String getPage(String htmlFolder, Device chronograph, Gun gun, Ammo ammo, List<ShotEntry> shots,boolean isConnected) {
        String chronogragh_data = CreateChronographData(chronograph);
        String gun_data = CreateGunData(gun);
        String ammo_data = CreateAmmoData(ammo);
        String shots_data = CreateShotsData(shots,ammo);
        String connectionStatus = isConnected ? "Connected" : "Disconnected";
        return getPage(htmlFolder,chronogragh_data,gun_data,ammo_data,shots_data,connectionStatus);
    }

    private static String getPage(String htmlFolder,String chronograghData,String gunData,String ammoData,String shotsData,String connectionStatus) {
        String htmlData = readFileContent(htmlFolder + "/chronograph_main_page.shtml");
        if (chronograghData != null && !chronograghData.isEmpty()) {
            htmlData = htmlData.replace(PLACEHOLDER_CHRONOGRAPH, chronograghData);
        }
        if (gunData != null && !gunData.isEmpty()) {
            htmlData = htmlData.replace(PLACEHOLDER_GUN, gunData);
        }
        if (ammoData != null && !ammoData.isEmpty()) {
            htmlData = htmlData.replace(PLACEHOLDER_AMMO, ammoData);
        }
        if(connectionStatus != null && !connectionStatus.isEmpty()){
            htmlData = htmlData.replace(PLACEHOLDER_CONNECTION, connectionStatus);
        }

        if (shotsData != null && !shotsData.isEmpty()) {
            htmlData = htmlData.replace(PLACEHOLDER_SHOTS, shotsData);
        }
        return htmlData;
    }
}
