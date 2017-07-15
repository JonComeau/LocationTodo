package com.skazerk.locationtodo.Location;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Skaze on 6/8/17.
 */

public class MySupportMapFragment extends SupportMapFragment {
    public View originalContentview;
    public TouchableWrapper touchView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup parent,
                             Bundle savedInstanceState) {
        originalContentview = super.onCreateView(inflater, parent, savedInstanceState);
        touchView = new TouchableWrapper(getActivity());
        touchView.addView(originalContentview);
        return touchView;
    }

    @Override
    public View getView() {
        return originalContentview;
    }

    class TouchableWrapper extends FrameLayout {
        public TouchableWrapper(@NonNull Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Location.mapIsTouched = true;
                    break;
                case MotionEvent.ACTION_UP:
                    Location.mapIsTouched = false;
                    break;
            }
            return super.dispatchTouchEvent(event);
        }
    }


}
