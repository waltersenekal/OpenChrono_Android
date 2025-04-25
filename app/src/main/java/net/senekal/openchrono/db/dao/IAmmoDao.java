package net.senekal.openchrono.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import net.senekal.openchrono.db.table.Ammo;

import java.util.List;

@Dao
public interface IAmmoDao {
    @Insert
    void insert(Ammo ammo);

    @Update
    void update(Ammo ammo);

    @Delete
    void delete(Ammo ammo);

    @Query("SELECT * FROM ammo WHERE id = :id")
    Ammo getAmmoById(int id);

    @Query("SELECT * FROM ammo")
    List<Ammo> getAllAmmo();

    @Query("SELECT * FROM ammo WHERE name = :name")
    List<Ammo> getAmmoByName(String name);

    @Query("SELECT * FROM ammo WHERE name = :name AND brand = :brand")
    Ammo getAmmoByNameAndBrand(String name, String brand);
    default void updateOrInsert(Ammo ammo) {
        Ammo existingAmmo = getAmmoById(ammo.id);
        if (existingAmmo != null) {
            update(ammo);
        } else {
            insert(ammo);
        }
    }
}
