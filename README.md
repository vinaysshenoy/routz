# routz(PRE-ALPHA)

A simple, view-based router framework for Android. Designed with the goal of creating a single Activity application that can eliminate the need for Fragments(because we all love Fragments so much).

Routz pairs its own backstack manager with lightweight `Screen` objects. Pairing a router with an Activity will allow you to implement a view navigation hierarchy with the ease of loading web urls.

### TODO: Add more architectural and design information here.

## Usage

#### 1. Setting up the View Container
Add a `FrameLayout` in your Activity. All views will be swapped in and out of this `FrameLayout`.
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context="com.vinaysshenoy.routz.MainActivity">

    <android.support.v7.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        android:elevation="12dp"
        app:popupTheme="@style/AppTheme.PopupOverlay"/>

    <FrameLayout
        android:id="@+id/frame_content"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/grey50"/>


</LinearLayout>
```

#### 2. Setting up the Screens

Create a class that extends `com.vinaysshenoy.routz.Screen` and implement the methods.

* `Screen1.java`
```java
public class Screen1 extends Screen {

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
                getRouter().goTo("screens/SCREEN_2", null);
            }
        });
    }
}
```
* `Screen2.java`
```java
public class Screen2 extends Screen {

    public Screen2(@Nullable Bundle params) {
        super(params);
    }

    @Override
    public View createView(LayoutInflater layoutInflater, ViewGroup container) {
        return layoutInflater.inflate(R.layout.frame_screen_2, container, false);
    }

    @Override
    public void setupView() {

        final View contentView = getContentView();
        contentView.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRouter().goTo("screens/SCREEN_3", null);
            }
        });
        contentView.findViewById(R.id.btn_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRouter().goBack();
            }
        });
    }

}
```
* `Screen3.java`
```java
public class Screen3 extends Screen {

    public Screen3(@Nullable Bundle params) {
        super(params);
    }

    @Override
    public View createView(LayoutInflater layoutInflater, ViewGroup container) {
        return layoutInflater.inflate(R.layout.frame_screen_3, container, false);
    }

    @Override
    public void setupView() {

        final View contentView = getContentView();
        contentView.findViewById(R.id.btn_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRouter().goBack();
            }
        });
    }

}
```

#### 3. Setting up the Activity

Create a `Router` in your `Activity`, and provide it the reference to the `FrameLayout` and an implmentation of a `RouteCreator`, which decides which Screen object to instantiate for which route. You'll also need to pass some of the lifecycle methods over to the Router, so it can take care of managing the screen backstack properly. It would also be prudent to override the `onBackPressed()` method of the Activity, so you can tie it into the `goBack()` method of the `Router`.

* `MainActivity.java`
```java
public class MainActivity extends AppCompatActivity {

    private Router mRouter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_content);
        mRouter = Router.create(
                frameLayout,
                new RouteCreator() {
                    @Override
                    public Screen instantiateScreenForRoute(@NonNull String route, @Nullable Bundle params) {

                        switch (route) {
                            case "screens/SCREEN_1": {
                                return new Screen1(params);
                            }

                            case "screens/SCREEN_2": {
                                return new Screen2(params);
                            }

                            case "screens/SCREEN_3": {
                                return new Screen3(params);
                            }

                            default: {
                                return new EmptyScreen(params);
                            }
                        }
                    }
                },
                savedInstanceState);

        mRouter.goTo("screens/SCREEN_1");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mRouter.saveState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRouter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRouter.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRouter.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(mRouter.canGoBack()) {
            mRouter.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

```

That's it! Enjoy a fragment-free development experience.

