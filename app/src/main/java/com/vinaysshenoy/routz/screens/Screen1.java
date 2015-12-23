package com.vinaysshenoy.routz.screens;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vinaysshenoy.routz.R;
import com.vinaysshenoy.routz.Routes;
import com.vinaysshenoy.routz.Screen;

/**
 * Created by vinaysshenoy on 21/12/15.
 */
public class Screen1 extends Screen {

    private static final String TAG = "Screen1";

    public Screen1(@Nullable Bundle params) {
        super(params);
    }

    @Override
    public View createView(LayoutInflater layoutInflater, ViewGroup container) {
        return layoutInflater.inflate(R.layout.frame_screen_1, container, false);
    }

    @Override
    public void setupView() {

        final View contentView = getContentView();
        contentView.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRouter().load(Routes.SCREEN_2, null);
            }
        });
        Toast.makeText(contentView.getContext(), "Stack Count: " + getRouter().getBackstackCount(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPushed() {
        super.onPushed();
        Log.d(TAG, "On Pushed");
    }

    @Override
    public void onPopped() {
        super.onPopped();
        Log.d(TAG, "On Popped");
    }

    @Override
    public void onHidden() {
        super.onHidden();
        Log.d(TAG, "On Hidden");
    }

    @Override
    public void onShown() {
        super.onShown();
        Log.d(TAG, "On Shown");
    }
}
