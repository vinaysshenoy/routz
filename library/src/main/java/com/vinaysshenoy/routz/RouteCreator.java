package com.vinaysshenoy.routz;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by vinaysshenoy on 21/12/15.
 */
public interface RouteCreator {
    Screen instantiateScreenForRoute(@NonNull String route, @Nullable Bundle params);
}
