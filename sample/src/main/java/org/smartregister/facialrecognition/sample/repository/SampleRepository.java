package org.smartregister.facialrecognition.sample.repository;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.AllConstants;
import org.smartregister.repository.Repository;
import org.smartregister.facialrecognition.repository.ImageRepository;
/**
 * Created by wildan on 10/2/17.
 */

public class SampleRepository extends Repository {

    private static final String TAG = SampleRepository.class.getSimpleName();
    private final Context context;
    private SQLiteDatabase readableDatabase, writableDatabase;
    private String password = "Sample_PASS";

    public SampleRepository(Context context, org.smartregister.Context openSRPContext){
        super(context, AllConstants.DATABASE_NAME, AllConstants.DATABASE_VERSION, openSRPContext.session(), null, openSRPContext.sharedRepositoriesArray());
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);

        ImageRepository.createTable(database);
//        onUpgrade(database, 1, 2);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SampleRepository.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 2:
//                    upgradeToVersion2(db);
                    break;
                default:
                    break;
            }
            upgradeTo++;
        }
    }
    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        try {
            if (readableDatabase == null || !readableDatabase.isOpen()){
                if (readableDatabase != null){
                    readableDatabase.close();
                }
                readableDatabase = super.getReadableDatabase(password);
            }
            return readableDatabase;
        } catch (Exception e){
            Log.e(TAG, "getReadableDatabase: "+ e.getMessage());
            return null;
        }
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        try {
            if (writableDatabase == null || !writableDatabase.isOpen()){
                if (writableDatabase != null){
                    writableDatabase.close();
                }
                writableDatabase = super.getWritableDatabase(password);
            }
            return writableDatabase;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public synchronized void close() {
        if (readableDatabase != null){
            readableDatabase.close();
        }

        if (writableDatabase != null){
            writableDatabase.close();
        }
        super.close();
    }
}
