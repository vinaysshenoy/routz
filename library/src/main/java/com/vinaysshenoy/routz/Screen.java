package com.vinaysshenoy.routz;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by vinaysshenoy on 21/12/15.
 */
public abstract class Screen {

    private View mContentView;

    private int mId;

    private Router mRouter;

    @Nullable
    private Bundle mParams;

    public Screen(@Nullable Bundle params) {
        this.mParams = params;
    }

    /* package */ void setRouter(@NonNull Router router) {
        mRouter = router;
        mId = router.getNextScreenId();
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


}
