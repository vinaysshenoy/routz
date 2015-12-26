package com.vinaysshenoy.routz;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import com.vinaysshenoy.routz.screens.EmptyScreen;
import com.vinaysshenoy.routz.screens.Screen1;
import com.vinaysshenoy.routz.screens.Screen2;
import com.vinaysshenoy.routz.screens.Screen3;
import com.vinaysshenoy.routz.screens.Screen4;
import com.vinaysshenoy.routz.screens.Screen5;

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
                            case Routes.SCREEN_1: {
                                return new Screen1(params);
                            }

                            case Routes.SCREEN_2: {
                                return new Screen2(params);
                            }

                            case Routes.SCREEN_3: {
                                return new Screen3(params);
                            }

                            case Routes.SCREEN_4: {
                                return new Screen4(params);
                            }

                            case Routes.SCREEN_5: {
                                return new Screen5(params);
                            }

                            default: {
                                return new EmptyScreen(params);
                            }
                        }
                    }
                },
                savedInstanceState);

        if(savedInstanceState == null) {
            final Bundle args = new Bundle(1);
            args.putInt("screen", 1);
            mRouter.load(Routes.SCREEN_1, args);
        }
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
        if(mRouter.getBackstackCount() > 1) {
            mRouter.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
