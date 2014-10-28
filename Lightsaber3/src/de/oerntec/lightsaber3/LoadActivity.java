package de.oerntec.lightsaber3;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LoadActivity extends Activity {
	private ListView mainListView ;
	private ArrayAdapter<String> listAdapter ;
	private MyDB db;
	private DrawingView dV;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load);
		db=new MyDB(this);
		dV=(DrawingView) findViewById(R.id.drawing);
		
		// Find the ListView resource. 
		mainListView = (ListView) findViewById( R.id.mainListView );
		mainListView.setClickable(true);
		
		Cursor allRecordCursor=db.allRecordNames();
		ArrayList<String> saveList = new ArrayList<String>();

		while(allRecordCursor.isAfterLast()==false){
			saveList.add(allRecordCursor.getString(0));
			allRecordCursor.moveToNext();
		}
		allRecordCursor.close();
		if(saveList.size()==0)
			saveList.add("Keine Shows gespeichert!");
		// Create ArrayAdapter using the planet list.
		listAdapter = new ArrayAdapter<String>(this, R.layout.row_save, saveList);
		
		// Set the ArrayAdapter as the ListView's adapter.
		mainListView.setAdapter(listAdapter);
		mainListView.setClickable(true);
		mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			    String clickedItem = mainListView.getItemAtPosition(position).toString();
			    if(clickedItem!="Keine Shows gespeichert!"){
				    Cursor loadData=db.selectRecord(clickedItem);
				    loadData.moveToFirst();
				    String data=loadData.getString(0);
				    dV.load(data);
				    loadData.close();
				    
			    }
			}
		});
	}	
}
