package com.sfsu.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sfsu.entities.AppConfig;
import com.sfsu.entities.Entity;
import com.sfsu.entities.Tick;

import java.util.ArrayList;
import java.util.List;

/**
 * Dao for Ticks related DB operation and data manipulation.
 * Created by Pavitra on 10/8/2015.
 */
public class TickDao implements EntityDao {
    private SQLiteDatabase db;
    private String[] tickEntryArray = new String[]{TicksTable.COLUMN_ID, TicksTable.COLUMN_TICK_NAME, TicksTable.COLUMN_TICK_SPECIES, TicksTable.COLUMN_KNOWN_FOR, TicksTable.COLUMN_DESCRIPTION, TicksTable.COLUMN_IMAGE, TicksTable.COLUMN_CREATEDAT, TicksTable.COLUMN_UPDATEDAT};

    @Override
    public void setDatabase(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * save(Tick) method is used to save the entries (field values) in to Tick Database table
     *
     * @param ticks
     * @return
     */
    public long save(Entity entity) {
        Tick tick = (Tick) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(TicksTable.COLUMN_ID, tick.getTick_id());
        contentValues.put(TicksTable.COLUMN_TICK_NAME, tick.getName());
        contentValues.put(TicksTable.COLUMN_TICK_SPECIES, tick.getSpecies());
        contentValues.put(TicksTable.COLUMN_KNOWN_FOR, tick.getKnown_for());
        contentValues.put(TicksTable.COLUMN_DESCRIPTION, tick.getDescription());
        contentValues.put(TicksTable.COLUMN_IMAGE, tick.getImageUrl());
        contentValues.put(TicksTable.COLUMN_CREATEDAT, tick.getCreated_at());
        contentValues.put(TicksTable.COLUMN_UPDATEDAT, tick.getUpdated_at());
        Log.d(AppConfig.LOGTAG, "TICK : INSERT reached");
        return db.insert(TicksTable.TABLENAME, null, contentValues);
    }

    /**
     * update(Tick) method to update the entries in Tick Table
     *
     * @param Ticks
     * @return
     */
    public boolean update(Entity entity) {
        Tick tick = (Tick) entity;
        ContentValues contentValues = new ContentValues();
        contentValues.put(TicksTable.COLUMN_ID, tick.getTick_id());
        contentValues.put(TicksTable.COLUMN_TICK_NAME, tick.getName());
        contentValues.put(TicksTable.COLUMN_TICK_SPECIES, tick.getSpecies());
        contentValues.put(TicksTable.COLUMN_KNOWN_FOR, tick.getKnown_for());
        contentValues.put(TicksTable.COLUMN_DESCRIPTION, tick.getDescription());
        contentValues.put(TicksTable.COLUMN_IMAGE, tick.getImageUrl());
        contentValues.put(TicksTable.COLUMN_CREATEDAT, tick.getCreated_at());
        contentValues.put(TicksTable.COLUMN_UPDATEDAT, tick.getUpdated_at());
        Log.d(AppConfig.LOGTAG, "Tick : UPDATE reached");
        // the db.update() method will return INT for number of rows updated. and so return db.update()>0 will check
        // for whether its true or false.
        return db.update(TicksTable.TABLENAME, contentValues, TicksTable.COLUMN_ID + "=?", new String[]{tick.getTick_id() + ""}) > 0;
    }

    // build the Tick Object using Cursor.
    public Tick buildFromCursor(Cursor c) {
        Tick tickItem = null;
        if (c != null) {
            tickItem = new Tick();
            tickItem.setTick_id(c.getInt(0));
            tickItem.setTickName(c.getString(1));
            tickItem.setSpecies(c.getString(2));
            tickItem.setKnown_for(c.getString(3));
            tickItem.setDescription(c.getString(4));
            tickItem.setImageUrl(c.getString(5));
            tickItem.setCreated_at(c.getLong(6));
            tickItem.setUpdated_at(c.getLong(7));
        }
        return tickItem;
    }

    /**
     * Delete the Tick entry from the Table.
     *
     * @param Ticks
     * @return
     */
    public boolean delete(Entity entity) {
        Tick ticks = (Tick) entity;
        return db.delete(TicksTable.TABLENAME, TicksTable.COLUMN_ID + "=?", new String[]{ticks.getTick_id() + ""}) > 0;
    }

    /**
     * get specific Tick using ID.
     *
     * @param id
     * @return
     */
    public Entity get(long id) {

        Tick tickItem = null;
        Cursor c = db.query(true, TicksTable.TABLENAME, tickEntryArray, TicksTable.COLUMN_ID + "=?", new String[]{id + ""}, null, null, null, null);

        if (c != null && c.moveToFirst()) {
            tickItem = buildFromCursor(c);
            if (!c.isClosed()) {
                c.close();
            }
        }
        return tickItem;
    }

    /**
     * Get list of all the ticks stored in DB
     *
     * @return
     */
    public List<Entity> getAll() {
        List<Entity> ticksList = new ArrayList<Entity>();

        // Query the Database to get all the records.
        Cursor c = db.query(TicksTable.TABLENAME, tickEntryArray, null, null, null, null, null);

        if (c != null && c.moveToFirst()) {
            // loop until the end of Cursor and add each entry to Ticks ArrayList.
            do {
                Tick tickItem = buildFromCursor(c);
                if (tickItem != null) {
                    ticksList.add(tickItem);
                }
            } while (c.moveToNext());
        }
        return ticksList;
    }


}
