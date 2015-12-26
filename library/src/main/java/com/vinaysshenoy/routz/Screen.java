package com.vinaysshenoy.routz;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * //TODO: Add a state variable that indicates the current state of the Screen
 * Created by vinaysshenoy on 21/12/15.
 */
public abstract class Screen {

    private View mContentView;

    private int mId;

    private Router mRouter;

    @Nullable
    private Bundle mParams;

    private String mRoute;

    public Screen(@Nullable Bundle params) {
        this.mParams = params;
    }

    /* package */ void setRouter(@NonNull Router router) {
        mRouter = router;
    }

    /* package */ void setId(int id) {
        mId = id;
    }

    public abstract View createView(LayoutInflater layoutInflater, ViewGroup container);

    /**
     * This is a method that will be called immediately after setting up the view.
     * <p/>
     * Do the view initialization, setting up of listeners, data, etc. here
     */
    public void setupView() {

    }

    /* package */ void setContentView(View contentView) {
        if (contentView == null) {
            throw new IllegalArgumentException("Content View cannot be null");
        }
        mContentView = contentView;
        setupView();
    }

    /* package */ void clearView() {
        mContentView = null;
    }

    /* package */ void setRoute(@NonNull String route) {
        mRoute = route;
    }

    /* package */ void setParams(@Nullable Bundle params) {
        mParams = params;
    }

    public String getRoute() {
        return mRoute;
    }

    public View getContentView() {
        return mContentView;
    }

    public Router getRouter() {
        return mRouter;
    }

    public void onSaveState(@NonNull Bundle savedInstanceState) {

    }

    public void restoreState(@NonNull Bundle savedInstanceState) {

    }

    public void onPushed() {

    }

    public void onPopped() {

    }

    public void onShown() {

    }

    public void onHidden() {

    }

    @Nullable
    public Bundle getParams() {
        return mParams;
    }

    public int getId() {
        return mId;
    }


}
