package org.uvigo.esei.com.dm.habitapp;

import android.app.Application;

import org.uvigo.esei.com.dm.habitapp.database.DBManager;

public class HabitApplication  extends Application {

    private DBManager dbManager;

    public void onCreate(){
        super.onCreate();
        this.dbManager = new DBManager(this);
    }

    public DBManager getDbManager() {
        return dbManager;
    }
}
