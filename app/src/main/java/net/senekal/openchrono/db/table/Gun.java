package net.senekal.openchrono.db.table;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "gun",
        indices = {@Index(value = {"name", "brand"}, unique = true)}
)
public class Gun {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String name;
    @Nullable
    public String brand;
    public double calibre;



    public Gun(@NonNull String name, @Nullable String brand, double calibre) {
        this.name = name;
        this.brand = brand;
        this.calibre = calibre;
    }

}
