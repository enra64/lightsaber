package de.oerntex.lightsaber;


import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class AFragment extends Fragment {
	View root;
	private LayoutInflater mInflater;
	private WeakReference<View> mRootView = null;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
               
        if (inflater != null)
            mInflater = inflater;
          else
            mInflater = LayoutInflater.from(getActivity());
        
          View rootView = mRootView == null ? null : mRootView.get();
          if (rootView != null) {
              final ViewParent parent = rootView.getParent();
              if (parent != null && parent instanceof ViewGroup)
                ((ViewGroup) parent).removeView(rootView);  
              }
          else {
               rootView = mInflater.inflate(R.layout.afragment, container, false);
               mRootView = new WeakReference<View>(rootView);
               }
        
        root=rootView;
        //seeeekbars
		
		
		return root;
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).AFragmentinit(root);
    }
    
    @Override
    public void onResume()
    {
    	//((MainActivity)getActivity()).loadPage(0);
    	super.onResume();
    }
	
}
