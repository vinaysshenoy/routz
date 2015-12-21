package com.vinaysshenoy.routz;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Locale;
import java.util.Stack;


/**
 * Created by vinaysshenoy on 21/12/15.
 */
public class Router {

    private final String KEY_SAVED_STATE = "com.vinaysshenoy.routz.ROUTER_SAVED_STATE";

    @NonNull
    private final FrameLayout mContainer;

    @NonNull
    private final RouteCreator mRouteCreator;

    private int mScreenIdGenerator;

    private final Handler mMainHandler;

    private Stack<Screen> mScreenStack;

    private Router(@NonNull FrameLayout container, @NonNull RouteCreator routeCreator, @Nullable Bundle savedInstanceState) {
        mContainer = container;
        mRouteCreator = routeCreator;
        mScreenIdGenerator = 0;
        mMainHandler = new Handler(Looper.getMainLooper());

        mScreenStack = new Stack<>();
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SAVED_STATE)) {
            restoreState(savedInstanceState);
        }
    }

    /**
     * Instantiate a Router, along with a FrameLayout.
     * <p/>
     * This router will then load all its screens into that container
     *
     * @param container          The framelayout to serve as the container for all the screens
     *                           of this router
     * @param routeCreator       A creator to instantiate the screens for a route
     * @param savedInstanceState The bundle that contains the saved state
     */
    public static Router create(@NonNull FrameLayout container, @NonNull RouteCreator routeCreator, @Nullable Bundle savedInstanceState) {
        return new Router(container, routeCreator, savedInstanceState);
    }

    /* package */ int getNextScreenId() {
        return mScreenIdGenerator++;
    }

    public void saveState(@NonNull Bundle savedInstanceState) {

        //TODO: Save the stack state and params as well
        savedInstanceState.putParcelable(KEY_SAVED_STATE, SavedState.from(this));
    }

    private void restoreState(@NonNull Bundle savedInstanceState) {

        final SavedState savedState = savedInstanceState.getParcelable(KEY_SAVED_STATE);
        if (savedState != null) {
            mScreenIdGenerator = savedState.currentIdNumber;
        }
    }

    public void onStart() {

        //TODO: Display the top of the stack
    }

    public void onStop() {

        //TODO: Hide the top of the stack
    }

    public void onDestroy() {

        //TODO: Clear the complete stack
    }

    /**
     * Method used to go to a particular route
     *
     * @param route  The route to go to. Must not be {@code null}
     * @param params An optional {@link Bundle} that can be provided to the screen
     * @return The screen object, which can be used later for performing any backstack operations relating the screen
     */
    public Screen goTo(@NonNull String route, @Nullable Bundle params) {

        final Screen screen = initScreenForRoute(route, params);

        if (isOnMainThread()) {
            pushScreen(screen, true);
        } else {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    pushScreen(screen, true);
                }
            });
        }

        return screen;
    }

    /**
     * Returns {@code true} if there are states to pop out.
     * <p/>
     * <b>NOTE:</b> This will return {@code false} when there is one state left in the stack
     */
    public boolean canGoBack() {

        return mScreenStack.size() > 1;
    }

    /**
     * Method used to go to a particular route
     *
     * @param route The route to go to. Must not be {@code null}
     * @return The screen object, which can be used later for performing any backstack operations relating the screen
     */
    public Screen goTo(@NonNull String route) {
        return goTo(route, null);
    }

    /**
     * Method used to go back to the previous route
     */
    public void goBack() {

        if (isOnMainThread()) {
            popScreen(true);
        } else {
            popScreen(false);
        }
    }

    private Screen initScreenForRoute(@NonNull String route, @Nullable Bundle params) {

        if (route == null) {
            throw new IllegalArgumentException("Route cannot be null!");
        }
        final Screen screen = mRouteCreator.instantiateScreenForRoute(route, params);
        if (screen == null) {
            throw new IllegalArgumentException(String.format(Locale.US, "No screen defined for route: {%s}", route));
        }
        return screen;
    }

    /**
     * Pops the current top of the stack
     *
     * @param display {@code true} to display the next screen on the stack
     */
    @MainThread
    private void popScreen(boolean display) {

        if (!mScreenStack.isEmpty()) {
            final Screen screen = mScreenStack.peek();

            hideCurrentTopScreen();
            mScreenStack.pop();
            screen.onPopped();
            if (display) {
                displayCurrentTopScreen();
            }
        }
    }


    /**
     * Pushes a screen into the stack and then displays it on screen
     *
     * @param screen  The Screen to push on the stack
     * @param display {@code true} to display the screen after pushing on the stack
     */
    @MainThread
    private void pushScreen(@NonNull Screen screen, boolean display) {

        screen.setRouter(this);
        if (display) {
            hideCurrentTopScreen();
        }
        mScreenStack.push(screen);
        screen.onPushed();
        if (display) {
            displayCurrentTopScreen();
        }
    }

    @MainThread
    private void hideCurrentTopScreen() {

        if (!mScreenStack.isEmpty()) {
            final Screen screen = mScreenStack.peek();

            final View contentView = screen.getContentView();
            screen.clearView();
            mContainer.removeView(contentView);
            screen.onHidden();
        }
    }

    @MainThread
    private void displayCurrentTopScreen() {

        if (!mScreenStack.isEmpty()) {
            final Screen screen = mScreenStack.peek();
            screen.setContentView(screen.createView(LayoutInflater.from(mContainer.getContext()), mContainer));
            mContainer.addView(screen.getContentView());
            screen.onShown();
        }

    }

    private static boolean isOnMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    private static final class SavedState implements Parcelable {

        public final int currentIdNumber;

        private SavedState(int currentIdNumber) {
            this.currentIdNumber = currentIdNumber;
        }

        protected static SavedState from(Router router) {

            final int currentIdNumber = router.mScreenIdGenerator;
            return new SavedState(currentIdNumber);
        }

        protected static SavedState from(Parcel in) {

            final int currentIdNumber = in.readInt();
            return new SavedState(currentIdNumber);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return SavedState.from(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(currentIdNumber);
        }
    }

}
