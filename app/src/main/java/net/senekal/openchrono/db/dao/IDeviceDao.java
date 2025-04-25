package net.senekal.openchrono.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import net.senekal.openchrono.db.table.Device;

import java.util.List;

@Dao
public interface IDeviceDao {

    @Query("SELECT * FROM devices")
    List<Device> getAllDevices();

    @Query("DELETE FROM devices WHERE macAddr = :macAddr")
    void deleteByMacAddr(String macAddr);

    @Query("SELECT * FROM devices WHERE id = :id")
    Device getDeviceById(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Device device);

    @Query("SELECT * FROM devices WHERE macAddr = :macAddr LIMIT 1")
    Device getDeviceByMacAddr(String macAddr);

    @Query("UPDATE devices SET nameFriendly = :nameFriendly, nameReal = :nameReal WHERE macAddr = :macAddr")
    void updateDevice(String nameFriendly, String nameReal, String macAddr);

    @Query("SELECT COUNT(*) FROM devices WHERE macAddr = :macAddr")
    int countByMacAddr(String macAddr);

    @androidx.room.Transaction
    default void updateOrInsert(Device device) {
        if (countByMacAddr(device.macAddr) > 0) {
            updateDevice(device.nameFriendly, device.nameReal, device.macAddr);
        } else {
            insert(device);
        }
    }
}