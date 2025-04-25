package net.senekal.openchrono.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import net.senekal.openchrono.db.dao.IAmmoDao;
import net.senekal.openchrono.db.dao.IGunDao;
import net.senekal.openchrono.db.table.Ammo;
import net.senekal.openchrono.db.table.Device;
import net.senekal.openchrono.db.table.Gun;
import net.senekal.openchrono.db.table.ShotEntry;
import net.senekal.openchrono.db.dao.IDeviceDao;
import net.senekal.openchrono.db.dao.IShotEntryDao;

@Database(entities = {Device.class, ShotEntry.class, Ammo.class, Gun.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract IDeviceDao deviceDao();
    public abstract IShotEntryDao shotEntryDao();
    public abstract IAmmoDao ammoDao();
    public abstract IGunDao gunDao();


}