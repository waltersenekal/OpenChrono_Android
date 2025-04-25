package net.senekal.openchrono.pages;

import static net.senekal.openchrono.utils.FileHelper.readFileContent;

import androidx.annotation.Nullable;

import net.senekal.openchrono.db.table.Device;

import java.util.List;
import java.util.Objects;

public class pageSetupAddChronograph {
    private static final String PLACEHOLDER_CHRONOGRAPH = "<!--#CHRONOGRAPH_DATA#-->";
    private static final String FRIENDLY_NAME = "#FRIENDLY_NAME#";

    private static String CreateChronographData(List<Device> allChronographs){
        if(allChronographs == null || allChronographs.isEmpty()) {
            return "<option value=\"none\" selected>No Devices Found (Is the device switched on)</option>";
        }
        //Each Entry Needs to look Like this
        /* <option value="addNew" selected>Add New ...</option> */
        StringBuilder ResponseString = new StringBuilder();
        boolean foundAny = false;
        for (Device chronograph : allChronographs) {
            ResponseString.append("<option value=\"")
                    .append(chronograph.nameReal)
                    .append("~")
                    .append(chronograph.macAddr)
                    .append("\" ");
            if (!foundAny) {
                ResponseString.append("selected");
                foundAny = true;
            }
            ResponseString.append(">")
                    .append(chronograph.nameReal)
                    .append("</option>");
        }
        if(!foundAny){
            //If we don't find or have default value then the default is to add new
            ResponseString.append("<option value=\"none\" selected>No devices Found</option>");
        }
        return ResponseString.toString();
    }

    public static String getPage(String htmlFolder, @Nullable List<Device> allChronographs) {
        String chronogragh_data = CreateChronographData(allChronographs);
        String friendlyName = (allChronographs != null && !allChronographs.isEmpty() && allChronographs.get(0).nameFriendly != null)
                ? allChronographs.get(0).nameFriendly : " ";
        return getPage(htmlFolder, chronogragh_data, friendlyName);
    }

    private static String getPage(String htmlFolder, String chronograghData,String friendlyName){
        String htmlData = readFileContent(htmlFolder + "/setup_add_chronograph_page.shtml");
        if (chronograghData != null && !chronograghData.isEmpty()) {
            htmlData = htmlData.replace(PLACEHOLDER_CHRONOGRAPH, chronograghData);
        }
        if (friendlyName != null && !friendlyName.isEmpty()) {
            htmlData = htmlData.replace(FRIENDLY_NAME, friendlyName);
        }
        return htmlData;
    }
}
