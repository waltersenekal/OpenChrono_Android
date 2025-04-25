package net.senekal.openchrono.db.table;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
@Entity(tableName = "ammo",
        indices = {@Index(value = {"name", "brand"}, unique = true)})
public class Ammo {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String name;
    @NonNull
    public String brand;
    public double calibre;
    public double weight;


    public Ammo(@NonNull String name, @NonNull String brand, double calibre, double weight) {
        this.name = name;
        this.brand = brand;
        this.calibre = calibre;
        this.weight = weight;
    }
}
