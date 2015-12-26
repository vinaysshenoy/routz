package com.vinaysshenoy.routz;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;


/**
 * Created by vinaysshenoy on 21/12/15.
 */
public class Router {

    private final String KEY_SAVED_STATE = "com.vinaysshenoy.routz.ROUTER_SAVED_STATE";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOAD_MODE_CREATE, LOAD_MODE_CLEAR, LOAD_MODE_REORDER})
    public @interface LoadMode {
    }

    /**
     * Always create a new instance of the screen
     */
    public static final int LOAD_MODE_CREATE = 0;

    /**
     * If the screen is already present in the stack, clear the stack until the screen is reached,
     * then load it. If it doesn't exist in the stack, then create a new instance
     */
    public static final int LOAD_MODE_CLEAR = 1;

    /**
     * If the screen is already present in the stack, remove it from its place in the stack,
     * then move it to the top and load it. If it doesn't exist, then create a new instance
     */
    public static final int LOAD_MODE_REORDER = 2;

    @NonNull
    private final FrameLayout mContainer;

    @NonNull
    private final RouteCreator mRouteCreator;

    private int mScreenIdGenerator;

    private final Handler mMainHandler;

    private LinkedList<Screen> mScreenStack;

    private Router(@NonNull FrameLayout container, @NonNull RouteCreator routeCreator, @Nullable Bundle savedInstanceState) {
        mContainer = container;
        mRouteCreator = routeCreator;
        mScreenIdGenerator = 0;
        mMainHandler = new Handler(Looper.getMainLooper());

        mScreenStack = new LinkedList<>();
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

    private int getNextScreenId() {
        return mScreenIdGenerator++;
    }

    public void saveState(@NonNull Bundle savedInstanceState) {

        savedInstanceState.putParcelable(KEY_SAVED_STATE, SavedState.from(this));
    }

    private void restoreState(@NonNull Bundle savedInstanceState) {

        final SavedState savedState = savedInstanceState.getParcelable(KEY_SAVED_STATE);
        if (savedState != null) {
            mScreenIdGenerator = savedState.currentIdNumber;
            if (savedState.screenSavedStates.length > 0) {
                restoreScreenStackFromSavedStates(savedState.screenSavedStates);
            }
        }
    }

    private void restoreScreenStackFromSavedStates(@NonNull ScreenSavedState[] screenSavedStates) {

        /* We need to restore the screens back to front since it's a stack and the screen on
        * top, i.e, the one that was on top, is the one that was on display
        **/

        ScreenSavedState screenSavedState;
        for (int i = screenSavedStates.length - 1; i >= 0; i--) {

            screenSavedState = screenSavedStates[i];
            //TODO: Save and restore the screen params and screen states
            final Screen screen = initScreenForRoute(screenSavedState.screenRoute, screenSavedState.screenId, screenSavedState.screenParams);
            final boolean displayScreen = (i == 0);

            runOnMainThread(new Runnable() {

                @Override
                public void run() {
                    pushScreen(screen, displayScreen);
                }
            });
        }
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public void onDestroy() {

        hideCurrentTopScreen();
        mScreenStack.clear();
    }

    /**
     * Method used to go to a particular route
     *
     * @param route    The route to go to. Must not be {@code null}
     * @param params   An optional {@link Bundle} that can be provided to the screen
     * @param loadMode The way to load the screen. One of {@link #LOAD_MODE_CLEAR}, {@link #LOAD_MODE_CREATE} or {@link #LOAD_MODE_REORDER}
     * @return The screen id, which can be used later for performing any backstack operations relating the screen
     */
    public int load(@NonNull String route, @Nullable Bundle params, @LoadMode int loadMode) {

        throwIfNull(route);

        Screen screen;
        switch (loadMode) {

            case LOAD_MODE_REORDER: {
                screen = findScreenForRoute(route);
                if (screen != null) {
                    screen.setParams(params);
                    moveScreenToTop(screen);
                } else {
                    screen = createAndLoadNewScreen(route, params);
                }
                break;
            }

            case LOAD_MODE_CLEAR: {
                screen = findScreenForRoute(route);
                if (screen != null) {
                    screen.setParams(params);
                    clearStackUpTo(screen);
                } else {
                    screen = createAndLoadNewScreen(route, params);
                }
                break;
            }

            case LOAD_MODE_CREATE:
            default: {
                screen = createAndLoadNewScreen(route, params);
                break;
            }
        }

        return screen.getId();
    }

    @Nullable
    private Screen findScreenForRoute(@NonNull String route) {

        final Iterator<Screen> iterator = mScreenStack.iterator();
        Screen screen = null;
        while (iterator.hasNext()) {
            screen = iterator.next();
            if (route.equals(screen.getRoute())) {
                break;
            }
        }
        return screen;
    }

    @NonNull
    private Screen createAndLoadNewScreen(@NonNull String route, @Nullable Bundle params) {

        final Screen screen = initScreenForRoute(route, getNextScreenId(), params);

        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                pushScreen(screen, true);
            }
        });

        return screen;
    }

    /**
     * Method used to go to a particular route
     *
     * @param route    The route to go to. Must not be {@code null}
     * @param loadMode The way to load the screen. One of {@link #LOAD_MODE_CLEAR}, {@link #LOAD_MODE_CREATE} or {@link #LOAD_MODE_REORDER}
     * @return The screen id, which can be used later for performing any backstack operations relating the screen
     */
    public int load(@NonNull String route, @LoadMode int loadMode) {
        return load(route, null, loadMode);
    }

    /**
     * Method used to go to a particular route
     *
     * @param route  The route to go to. Must not be {@code null}
     * @param params An optional {@link Bundle} that can be provided to the screen
     * @return The screen id, which can be used later for performing any backstack operations relating the screen
     */
    public int load(@NonNull String route, @Nullable Bundle params) {
        return load(route, params, LOAD_MODE_CREATE);
    }

    /**
     * Method used to go to a particular route
     *
     * @param route The route to go to. Must not be {@code null}
     * @return The screen id, which can be used later for performing any backstack operations relating the screen
     */
    public int load(@NonNull String route) {
        return load(route, null, LOAD_MODE_CREATE);
    }

    /**
     * Method used to go back to the previous route
     */
    public void goBack() {

        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                popScreen(true);
            }
        });

    }

    /**
     * Get the number of screens in the backstack
     */
    public int getBackstackCount() {

        return mScreenStack.size();
    }

    private void clearStackUpTo(@NonNull final Screen screenToShow) {

        runOnMainThread(new Runnable() {
            @Override
            public void run() {

                int numPops = 0;
                for (Screen screen : mScreenStack) {
                    if (screenToShow.getId() == screen.getId()) {
                        break;
                    } else {
                        numPops++;
                    }
                }

                for (int i = 0; i < numPops; i++) {
                    if (i == (numPops - 1)) {
                        popScreen(true);
                    } else {
                        popScreen(false);
                    }
                }
            }
        });

    }

    private void moveScreenToTop(@NonNull final Screen screenToShow) {

        runOnMainThread(new Runnable() {
            @Override
            public void run() {

                int screenLocation;
                for (screenLocation = 0; screenLocation < mScreenStack.size(); screenLocation++) {
                    if (screenToShow.getId() == mScreenStack.get(screenLocation).getId()) {
                        break;
                    }
                }
                if (screenLocation < mScreenStack.size()) {
                    mScreenStack.remove(screenLocation);
                    pushScreen(screenToShow, true);
                }
            }
        });
    }

    /**
     * Method used to go back to a previous route. This pops the backstack until the first screen with the given route
     * is encountered
     * <p/>
     * <b>NOTE: </b> If the screen route is not present in the backstack, all screens will be popped.
     */
    public void goBackTo(@NonNull final String route) {

        throwIfNull(route);
        runOnMainThread(new Runnable() {
            @Override
            public void run() {

                int numPops = 0;
                for (Screen screen : mScreenStack) {
                    if (screen.getRoute().equals(route)) {
                        break;
                    } else {
                        numPops++;
                    }
                }

                for (int i = 0; i < numPops; i++) {
                    if (i == numPops - 1) {
                        popScreen(true);
                    } else {
                        popScreen(false);
                    }
                }
            }
        });
    }

    /**
     * Method used to go back to a previous screen. The screen id will be the one returned from the {@link #load(String, Bundle)} methods
     * <p/>
     * <b>NOTE: </b> If the screen id is not present in the backstack, all screens will be popped.
     */
    public void goBackTo(final int screenId) {

        runOnMainThread(new Runnable() {
            @Override
            public void run() {

                int numPops = 0;
                for (Screen screen : mScreenStack) {
                    if (screen.getId() == screenId) {
                        break;
                    } else {
                        numPops++;
                    }
                }

                for (int i = 0; i < numPops; i++) {
                    if (i == numPops - 1) {
                        popScreen(true);
                    } else {
                        popScreen(false);
                    }
                }
            }
        });
    }

    private Screen initScreenForRoute(@NonNull String route, int screenId, @Nullable Bundle params) {

        final Screen screen = mRouteCreator.instantiateScreenForRoute(route, params);
        if (screen == null) {
            throw new IllegalArgumentException(String.format(Locale.US, "No screen defined for route: {%s}", route));
        }
        screen.setRouter(this);
        screen.setId(screenId);
        screen.setRoute(route);
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
            screen.onPopped();
            mScreenStack.pop();
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
            if (contentView != null) {
                screen.onHidden();
                screen.clearView();
                mContainer.removeView(contentView);
            }
        }
    }

    @MainThread
    private void displayCurrentTopScreen() {

        if (!mScreenStack.isEmpty()) {
            final Screen screen = mScreenStack.peek();
            if (screen.getContentView() == null) {
                screen.setContentView(screen.createView(LayoutInflater.from(mContainer.getContext()), mContainer));
                mContainer.addView(screen.getContentView());
                screen.onShown();
            }
        }

    }

    /**
     * Runs a runnable on the Main thread
     */
    private void runOnMainThread(@NonNull Runnable runnable) {

        if (isOnMainThread()) {
            runnable.run();
        } else {
            mMainHandler.post(runnable);
        }
    }

    private static boolean isOnMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    private static void throwIfNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot be null!");
        }
    }

    private static final class ScreenSavedState implements Parcelable {

        public final int screenId;

        public final String screenRoute;

        public final Bundle screenParams;

        private ScreenSavedState(int screenId, String screenRoute, Bundle screenParams) {
            this.screenId = screenId;
            this.screenRoute = screenRoute;
            this.screenParams = screenParams;
        }

        public static ScreenSavedState fromScreen(@NonNull Screen screen) {
            return new ScreenSavedState(screen.getId(), screen.getRoute(), screen.getParams());
        }

        public static ScreenSavedState fromParcel(Parcel in) {

            final int screenId = in.readInt();
            final String screenRoute = in.readString();
            final Bundle params = in.readBundle(ScreenSavedState.class.getClassLoader());
            return new ScreenSavedState(screenId, screenRoute, params);
        }

        public static final Creator<ScreenSavedState> CREATOR = new Creator<ScreenSavedState>() {
            @Override
            public ScreenSavedState createFromParcel(Parcel in) {
                return ScreenSavedState.fromParcel(in);
            }

            @Override
            public ScreenSavedState[] newArray(int size) {
                return new ScreenSavedState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(screenId);
            dest.writeString(screenRoute);
            dest.writeBundle(screenParams);
        }
    }

    private static final class SavedState implements Parcelable {

        public final int currentIdNumber;

        public final ScreenSavedState[] screenSavedStates;

        private SavedState(int currentIdNumber, ScreenSavedState[] screenSavedStates) {
            this.currentIdNumber = currentIdNumber;
            this.screenSavedStates = screenSavedStates;
        }

        private static SavedState from(@NonNull Router router) {

            final int currentIdNumber = router.mScreenIdGenerator;
            final ScreenSavedState[] screenSavedStates = new ScreenSavedState[router.mScreenStack.size()];
            for (int i = 0; i < router.mScreenStack.size(); i++) {
                screenSavedStates[i] = ScreenSavedState.fromScreen(router.mScreenStack.get(i));
            }
            return new SavedState(currentIdNumber, screenSavedStates);
        }

        private static SavedState from(Parcel in) {

            return new SavedState(
                    in.readInt(),
                    in.createTypedArray(ScreenSavedState.CREATOR)
            );
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
            dest.writeTypedArray(screenSavedStates, flags);
        }
    }


}
