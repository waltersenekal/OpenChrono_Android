package net.senekal.openchrono.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import net.senekal.openchrono.db.table.ShotEntry;

import java.util.List;

@Dao
public interface IShotEntryDao {
    @Insert
    void insert(ShotEntry shotEntry);

    @Query("SELECT * FROM shot_entry WHERE deviceId = :deviceId")
    List<ShotEntry> getUsageByDeviceId(int deviceId);

    @Query("SELECT * FROM shot_entry WHERE ammoId = :ammoId")
    List<ShotEntry> getUsageByAmmoId(int ammoId);

    @Query("SELECT * FROM shot_entry WHERE gunId = :gunId")
    List<ShotEntry> getUsageByGunId(int gunId);

    @Query("SELECT * FROM shot_entry WHERE deviceId = :deviceId AND ammoId = :ammoId AND gunId = :gunId")
    List<ShotEntry> getUsageByDeviceAmmoAndGun(int deviceId, int ammoId, int gunId);
    @Query("SELECT * FROM shot_entry WHERE deviceId = :deviceId AND ammoId = :ammoId AND gunId = :gunId AND timestamp BETWEEN :startTime AND :endTime")
    List<ShotEntry> getUsageByDeviceAmmoAndGunBetweenTimes(int deviceId, int ammoId, int gunId,long startTime, long endTime);

    @Query("SELECT * FROM shot_entry WHERE deviceId = :deviceId AND ammoId = :ammoId AND gunId = :gunId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC LIMIT :limit")
    List<ShotEntry> getLastXUsageByDeviceAmmoAndGunBetweenTimes(int deviceId, int ammoId, int gunId, long startTime, long endTime, int limit);
}
