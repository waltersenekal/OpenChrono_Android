package net.senekal.openchrono.pages;

import static net.senekal.openchrono.utils.FileHelper.readFileContent;


import java.util.List;

public class pageSetupAddAmmo {



    public static String getPage(String htmlFolder,String dummy){
        return getPage(htmlFolder);
    }

    private static String getPage(String htmlFolder){
        String htmlData = readFileContent(htmlFolder + "/setup_add_ammo_page.shtml");
        return htmlData;
    }
}
