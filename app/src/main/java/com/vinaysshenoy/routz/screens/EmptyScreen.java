package com.vinaysshenoy.routz.screens;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vinaysshenoy.routz.R;
import com.vinaysshenoy.routz.Screen;

/**
 * Created by vinaysshenoy on 21/12/15.
 */
public class EmptyScreen extends Screen {

    public EmptyScreen(@Nullable Bundle params) {
        super(params);
    }

    @Override
    public View createView(LayoutInflater layoutInflater, ViewGroup container) {
        return layoutInflater.inflate(R.layout.frame_screen_empty, container, false);
    }
}
