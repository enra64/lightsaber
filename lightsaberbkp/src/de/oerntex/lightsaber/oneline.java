package de.oerntex.lightsaber;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class oneline extends Fragment {

	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    	
        View test= inflater.inflate(R.layout.oneline, container, false);
        
        return test;
    }

	
}
