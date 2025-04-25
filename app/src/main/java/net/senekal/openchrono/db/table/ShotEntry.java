package net.senekal.openchrono.db.table;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "shot_entry",
        foreignKeys = {
                @ForeignKey(
                        entity = Device.class,
                        parentColumns = "id",
                        childColumns = "deviceId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Ammo.class,
                        parentColumns = "id",
                        childColumns = "ammoId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Gun.class,
                        parentColumns = "id",
                        childColumns = "gunId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "deviceId"),
                @Index(value = "ammoId"),
                @Index(value = "gunId")
        }
)
public class ShotEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int deviceId; // Foreign key referencing Device.id
    public int ammoId;   // Foreign key referencing Ammo.id
    public int gunId;    // Foreign key referencing Gun.id

    public long timestamp; // Timestamp of the shot entry

    public int timein_us;

    public ShotEntry(int deviceId, int ammoId, int gunId, int timein_us, long timestamp) {
        this.deviceId = deviceId;
        this.ammoId = ammoId;
        this.gunId = gunId;
        this.timein_us = timein_us;
        this.timestamp = timestamp;
    }
}