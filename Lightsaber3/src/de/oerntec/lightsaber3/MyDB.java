package de.oerntec.lightsaber3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MyDB{  

private DatabaseHelper dbHelper;  

private SQLiteDatabase database;  

public final static String EMP_TABLE="saves"; // name of table 

public final static String COLUMN_ID="_id"; // id value for employee
public final static String COLUMN_NAME="savename";  // name of employee
public final static String COLUMN_DATA="savedata";  // name of employee
/** 
 * 
 * @param context 
 */  
public MyDB(Context context){  
    dbHelper = new DatabaseHelper(context);
    database = dbHelper.getWritableDatabase();  
}


public long createRecords(String id, String name){  
   ContentValues values = new ContentValues();  
   values.put(COLUMN_ID, id);  
   values.put(COLUMN_NAME, name);  
   return database.insert(EMP_TABLE, null, values);  
}    

public void storeRecord(String data, String saveName){
	String[] cols = new String[] {COLUMN_ID};
	Cursor mCursor = database.query(true, EMP_TABLE, cols, null, null, null, null, null, null);  
	if (mCursor != null)
		if(!mCursor.moveToFirst()){
			Log.w("db", "empty cursor");
		}
	//create values for insert or update
	ContentValues values = new ContentValues();
	values.put(COLUMN_NAME, saveName);
	values.put(COLUMN_DATA, data);
	
	//name already exists->update
	if(mCursor.getCount()==1){
		int existingId=mCursor.getInt(0);
		String[] whereArgs={String.valueOf(existingId)};
		database.update(EMP_TABLE, values, COLUMN_ID+"=?", whereArgs);
	}
	//insert
	else
		database.insert(EMP_TABLE, null, values);
	//mandatory cursorclosing
	mCursor.close();
}

public Cursor selectRecord(String saveName) {
	String[] whereArgs= {saveName};
	String[] cols = new String[] {COLUMN_DATA};
	//							distinct, table, columns, WHERE,	WHEREARGS,	GROUP, HAVING, ORDER, LIMIT
	Cursor mCursor = database.query(true, EMP_TABLE, cols, COLUMN_NAME+"=?", whereArgs, null, null, null, null);  
	if (mCursor != null)
		mCursor.moveToFirst();  
	return mCursor; // iterate to get each value.
}

public Cursor allRecordNames(){
	String[] cols = new String[] {COLUMN_ID, COLUMN_NAME};
	Cursor mCursor = database.query(true, EMP_TABLE, cols, null, null, null, null, null, null);  
	if (mCursor != null)
		mCursor.moveToFirst();  
	return mCursor; // iterate to get each value.
}
}
