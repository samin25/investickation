package com.sfsu.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sfsu.entities.Entity;
import com.sfsu.entities.Observation;

import java.util.ArrayList;
import java.util.List;

/**
 * ObservationsDao for providing abstraction layer over DB. Since the Observation entity is a composite object the entire logic
 * for building up the composite object is handled by the DAO layer. The DAO layer is used for getting the data from the
 * Database and then build up the Observation object.
 * <p>
 * Created by Pavitra on 6/3/2015.
 */
public class ObservationsDao implements EntityDao {
    private final String TAG = "~!@#$ObsDao :";

    private SQLiteDatabase db;
    // Observation's entry array for storing all the column names
    private String[] observationEntryArray = new String[]{EntityTable.ObservationsTable.COLUMN_ID, EntityTable.ObservationsTable.COLUMN_NUMOFTICKS, EntityTable.ObservationsTable.COLUMN_TIMESTAMP, EntityTable.ObservationsTable.COLUMN_CREATEDAT, EntityTable.ObservationsTable
            .COLUMN_FK_TICK_ID, EntityTable.ObservationsTable.COLUMN_FK_LOCATION_ID};


    @Override
    public void setDatabase(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Deletes the {@link Observation} entry from the Table.
     *
     * @param observations
     * @return
     */
    public boolean delete(Entity entity) {
        Observation observations = (Observation) entity;

        return db.delete(EntityTable.ObservationsTable.TABLENAME, EntityTable.ObservationsTable.COLUMN_ID + "=?", new String[]{observations.getId() + ""})
                > 0;
    }

    /**
     * get specific observation using ID.
     *
     * @param id
     * @return
     */
    public Entity get(String id) {

        Observation observationItem = null;
        Cursor c = db.query(true, EntityTable.ObservationsTable.TABLENAME, observationEntryArray, EntityTable.ObservationsTable.COLUMN_ID + "=?", new String[]{id + ""}, null, null, null, null);

        if (c != null && c.moveToFirst()) {
            // once the Observation Item is build from cursor, create Tick object and Location object.
            observationItem = buildFromCursor(c);
            // TODO: create Tick object and Location Object.
            observationItem.setLocation(null);
            if (!c.isClosed()) {
                c.close();
            }
        }
        // the final ObservationItem will contain the Composite Object composed of Location and Tick objects.
        return observationItem;
    }

    @Override
    public Entity getByName(String entityName) {
        return null;
    }

    /**
     * Returns all the {@link Observation} from the DB
     *
     * @return
     */
    public List<Observation> getAll() {
        List<Observation> observationsList = new ArrayList<Observation>();

        // Query the Database to get all the records.
        Cursor c = db.query(EntityTable.ObservationsTable.TABLENAME, observationEntryArray, null, null, null, null, null);

        if (c != null && c.moveToFirst()) {
            // loop until the end of Cursor and add each entry to Observations ArrayList.
            do {
                Observation observationItem = buildFromCursor(c);
                if (observationItem != null) {
                    observationsList.add(observationItem);
                }
            } while (c.moveToNext());
        }
        return observationsList;
    }

    /**
     * This method is used to save the entries (field values) in to Observation Database table
     *
     * @param observations
     * @return
     */
    public long save(Entity entity) {
        Observation observations = (Observation) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(EntityTable.ObservationsTable.COLUMN_ID, observations.getId());
        contentValues.put(EntityTable.ObservationsTable.COLUMN_NUMOFTICKS, observations.getNum_ticks());
        contentValues.put(EntityTable.ObservationsTable.COLUMN_TIMESTAMP, observations.getTimestamp());
        contentValues.put(EntityTable.ObservationsTable.COLUMN_CREATEDAT, observations.getCreated_at());
        // get TickId and LocationId
//        contentValues.put(EntityTable.ObservationsTable.COLUMN_FK_TICK_ID, observations.getTickObj().getId());
        contentValues.put(EntityTable.ObservationsTable.COLUMN_FK_LOCATION_ID, observations.getLocation().getId());
        Log.d(TAG, "Observation : INSERT reached");
        return db.insert(EntityTable.ObservationsTable.TABLENAME, null, contentValues);
    }

    /**
     * This method is used to update the entries in Observation Table
     *
     * @param observations
     * @return
     */
    public boolean update(Entity entity) {
        Observation observations = (Observation) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(EntityTable.ObservationsTable.COLUMN_ID, observations.getId());
        contentValues.put(EntityTable.ObservationsTable.COLUMN_NUMOFTICKS, observations.getNum_ticks());
        contentValues.put(EntityTable.ObservationsTable.COLUMN_TIMESTAMP, observations.getTimestamp());
        contentValues.put(EntityTable.ObservationsTable.COLUMN_CREATEDAT, observations.getCreated_at());
        // get LocationId and TickId
//        contentValues.put(EntityTable.ObservationsTable.COLUMN_FK_TICK_ID, observations.getTickObj().getId());
        contentValues.put(EntityTable.ObservationsTable.COLUMN_FK_LOCATION_ID, observations.getLocation().getId());
        Log.d(TAG, "Observation : UPDATE reached");
        // the db.update() method will return INT for number of rows updated. and so return db.update()>0 will check
        // for whether its true or false.
        return db.update(EntityTable.ObservationsTable.TABLENAME, contentValues, EntityTable.ObservationsTable.COLUMN_ID + "=?", new
                String[]{observations.getId() + ""}) > 0;
    }

    // build the Observation Object using Cursor.
    public Observation buildFromCursor(Cursor c) {
        Observation observationItem = null;
        if (c != null) {
            observationItem = new Observation();
            observationItem.setId(c.getString(0));
//            observationItem.setNum_ticks(c.getInt(1));
//            observationItem.setTickObj(c.getExtras(2));
//            observationItem.setLatitude(c.getDouble(3));
//            observationItem.setLongitude(c.getDouble(4));
            observationItem.setTimestamp(c.getLong(5));
            observationItem.setCreated_at(c.getLong(6));
            observationItem.setUpdated_at(c.getLong(7));
        }
        return observationItem;
    }

    /**
     * Method to build the Tick Object and Location Object.
     */
}
