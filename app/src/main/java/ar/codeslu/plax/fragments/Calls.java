package ar.codeslu.plax.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ar.codeslu.plax.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Calls extends Fragment {

    View view;

    public Calls() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_calls, container, false);


        return view;
    }

}