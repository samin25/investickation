package com.sfsu.model;

import android.database.sqlite.SQLiteDatabase;

import com.sfsu.entities.Entity;

import java.util.List;

/**
 * EntityDao is the interface to provide the implementing classes get access to the DAO layer. Every Entity has a default behavior
 * which can be implemented using EntityDao interface;
 * <p/>
 * Created by Pavitra on 10/19/2015.
 */
public interface EntityDao {

    void setDatabase(SQLiteDatabase db);

    long save(Entity entityItem);

    boolean update(Entity entityItem);

    boolean delete(Entity entityItem);

    Entity get(long id);

    List<Entity> getAll();

}