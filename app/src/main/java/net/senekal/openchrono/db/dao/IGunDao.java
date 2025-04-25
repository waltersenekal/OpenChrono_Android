package net.senekal.openchrono.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import net.senekal.openchrono.db.table.Gun;

import java.util.List;

@Dao
public interface IGunDao {
    @Insert
    void insert(Gun gun);

    @Update
    void update(Gun gun);

    @Delete
    void delete(Gun gun);

    @Query("SELECT * FROM gun WHERE id = :id")
    Gun getGunById(int id);

    @Query("SELECT * FROM gun")
    List<Gun> getAllGuns();

    @Query("SELECT * FROM gun WHERE name = :name")
    List<Gun> getGunsByName(String name);

    @Query("SELECT * FROM gun WHERE name = :name AND brand = :brand")
    Gun getGunByNameAndBrand(String name, String brand);

    default void updateOrInsert(Gun gun) {
        Gun existingGun = getGunById(gun.id);
        if (existingGun != null) {
            update(gun);
        } else {
            insert(gun);
        }
    }
}
