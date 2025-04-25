package net.senekal.openchrono.db.table;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(tableName = "devices", indices = {@Index(value = "macAddr", unique = true)})
public class Device {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nameFriendly;
    public String nameReal;

    public String macAddr;

    public Device(String nameFriendly,String nameReal, String macAddr) {
        this.nameFriendly = nameFriendly;
        this.nameReal = nameReal;
        this.macAddr = macAddr;
    }
}