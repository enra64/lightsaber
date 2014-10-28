package de.oerntex.lightsaber;

import android.os.Bundle;
import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BFragment extends Fragment {
	public static Context appContext;
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		View view=inflater.inflate(R.layout.bfragment, container, false);
		//die drei buttons
		((MainActivity)getActivity()).createListForViewSavePoints(view);
        return view;
        
    }
}

