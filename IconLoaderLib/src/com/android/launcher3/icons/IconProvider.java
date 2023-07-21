/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.icons;

import static android.content.Intent.ACTION_DATE_CHANGED;
import static android.content.Intent.ACTION_TIMEZONE_CHANGED;
import static android.content.Intent.ACTION_TIME_CHANGED;
import static android.content.res.Resources.ID_NULL;
import static android.graphics.drawable.AdaptiveIconDrawable.getExtraInsetFraction;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PatternMatcher;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.os.BuildCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.launcher3.util.SafeCloseable;
import com.nothing.launcher.icons.IconPackManager;
import com.nothing.launcher.icons.constant.IconPackStateConstant;
import com.nothing.launcher.icons.model.data.CachedIconEntity;

import java.util.Calendar;
import java.util.function.Supplier;

/**
 * Class to handle icon loading from different packages
 */
public class IconProvider {

    // Benny.Fang {@
    public static final String ACTION_APPLY_PICKED_ICON_PACK = "com.nothing.launcher.APPLY_PICKED_ICON_PACK";
    // @}
    // add by yixiong for NOS-2146 @{
    public static final String ACTION_NOTHING_ICON_FORCE_RENDER_ENABLE_CHANGED = "com.nothing.launcher.NOTHING_ICON_FORCE_RENDER_ENABLE_CHANGED";
    // @}
    public static final String ACTION_OVERLAY_CHANGED = "android.intent.action.OVERLAY_CHANGED";
    private static final int CONFIG_ICON_MASK_RES_ID = Resources.getSystem().getIdentifier(
            "config_icon_mask", "string", "android");

    private static final String TAG = "IconProvider";
    private static final boolean DEBUG = false;
    public static final boolean ATLEAST_T = BuildCompat.isAtLeastT();

    private static final String ICON_METADATA_KEY_PREFIX = ".dynamic_icons";

    private static final String SYSTEM_STATE_SEPARATOR = " ";

    protected final Context mContext;
    private final ComponentName mCalendar;
    private final ComponentName mClock;

    public IconProvider(Context context) {
        mContext = context;
        mCalendar = parseComponentOrNull(context, R.string.calendar_component_name);
        mClock = parseComponentOrNull(context, R.string.clock_component_name);
    }

    /**
     * Adds any modification to the provided systemState for dynamic icons. This system state
     * is used by caches to check for icon invalidation.
     */
    public String getSystemStateForPackage(String systemState, String packageName) {
        if (mCalendar != null && mCalendar.getPackageName().equals(packageName)) {
            return systemState + SYSTEM_STATE_SEPARATOR + getDay();
        } else {
            return systemState;
        }
    }

    /**
     * Loads the icon for the provided LauncherActivityInfo
     */
    public Drawable getIcon(LauncherActivityInfo info, int iconDpi) {
        // Add by benny.fang for ABR13T-287 @{
        return getIcon(false, info, iconDpi);
        // @}
    }

    // Add by benny.fang for ABR13T-287 @{
    public Drawable getIcon(boolean isBigIcon, LauncherActivityInfo info, int iconDpi) {
        if (IconPackManager.Companion.getInstance().isFuzzyMatchInIconPack(info.getApplicationInfo().packageName)) {
            return getActivityIcon(info, iconDpi);
        } else {
            return getIconWithOverrides(info.getApplicationInfo().packageName, iconDpi, () -> loadIconIconDrawable(isBigIcon, info, iconDpi));
        }
    }
    // @}

    /**
     * Loads the icon for the provided activity info
     */
    public Drawable getIcon(ActivityInfo info) {
        return getIcon(info, mContext.getResources().getConfiguration().densityDpi);
    }

    /**
     * Loads the icon for the provided activity info
     */
    public Drawable getIcon(ActivityInfo info, int iconDpi) {
        return getIconWithOverrides(info.applicationInfo.packageName, iconDpi,
                () -> loadActivityInfoIcon(info, iconDpi));
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private Drawable getIconWithOverrides(String packageName, int iconDpi,
            Supplier<Drawable> fallback) {
        ThemeData td = getThemeDataForPackage(packageName);

        Drawable icon = null;
        if (mCalendar != null && mCalendar.getPackageName().equals(packageName)) {
            icon = loadCalendarDrawable(iconDpi, td);
        } else if (mClock != null && mClock.getPackageName().equals(packageName)) {
            icon = ClockDrawableWrapper.forPackage(mContext, mClock.getPackageName(), iconDpi, td);
        }
        if (icon == null) {
            icon = fallback.get();
            if (ATLEAST_T && icon instanceof AdaptiveIconDrawable && td != null) {
                AdaptiveIconDrawable aid = (AdaptiveIconDrawable) icon;
                if  (aid.getMonochrome() == null) {
                    icon = new AdaptiveIconDrawable(aid.getBackground(),
                            aid.getForeground(), td.loadPaddedDrawable());
                }
            }
        }
        return icon;
    }

    protected ThemeData getThemeDataForPackage(String packageName) {
        return null;
    }

    // Modified by steve.tang {@
/*    private Drawable loadActivityInfoIcon(ActivityInfo ai, int density) {
        final int iconRes = ai.getIconResource();
        Drawable icon = null;
        // Get the preferred density icon from the app's resources
        if (density != 0 && iconRes != 0) {
            try {
                final Resources resources = mContext.getPackageManager()
                        .getResourcesForApplication(ai.applicationInfo);
                icon = resources.getDrawableForDensity(iconRes, density);
            } catch (NameNotFoundException | Resources.NotFoundException exc) { }
        }
        // Get the default density icon
        if (icon == null) {
            icon = ai.loadIcon(mContext.getPackageManager());
        }
        return icon;
    }*/
    /**
     * Only use for task icons, same logic with workspace/all-apps
     * @see IconProvider.loadIconIconDrawable
     * */
    private Drawable loadActivityInfoIcon(ActivityInfo ai, int density) {
        Drawable icon;
        if (IconPackManager.Companion.getInstance().isFuzzyMatchInIconPack(ai.packageName)) {
            icon = getDrawableFromApplicationResource(ai, density);
            if (icon == null) {
                // Get the default density icon
                icon = ai.loadIcon(mContext.getPackageManager());
            }
        } else {
            // Get the default density icon
            icon = ai.loadIcon(mContext.getPackageManager());
            if (icon == null) {
                icon = getDrawableFromApplicationResource(ai, density);
            }
        }
        return icon;
    }

    private Drawable getDrawableFromApplicationResource(ActivityInfo ai, int density) {
        final int iconRes = ai.getIconResource();
        Drawable icon = null;
        // Get the preferred density icon from the app's resources
        if (density != 0 && iconRes != 0) {
            try {
                final Resources resources = mContext.getPackageManager()
                        .getResourcesForApplication(ai.applicationInfo);
                icon = resources.getDrawableForDensity(iconRes, density);
            } catch (NameNotFoundException | Resources.NotFoundException exc) { }
        }
        return icon;
    }
    // @}

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private Drawable loadCalendarDrawable(int iconDpi, @Nullable ThemeData td) {
        PackageManager pm = mContext.getPackageManager();
        try {
            final Bundle metadata = pm.getActivityInfo(
                    mCalendar,
                    PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_META_DATA)
                    .metaData;
            final Resources resources = pm.getResourcesForApplication(mCalendar.getPackageName());
            final int id = getDynamicIconId(metadata, resources);
            if (id != ID_NULL) {
                if (DEBUG) Log.d(TAG, "Got icon #" + id);
                Drawable drawable = resources.getDrawableForDensity(id, iconDpi, null /* theme */);
                if (ATLEAST_T && drawable instanceof AdaptiveIconDrawable && td != null) {
                    AdaptiveIconDrawable aid = (AdaptiveIconDrawable) drawable;
                    if  (aid.getMonochrome() != null) {
                        return drawable;
                    }
                    if ("array".equals(td.mResources.getResourceTypeName(td.mResID))) {
                        TypedArray ta = td.mResources.obtainTypedArray(td.mResID);
                        int monoId = ta.getResourceId(IconProvider.getDay(), ID_NULL);
                        ta.recycle();
                        return monoId == ID_NULL ? drawable
                                : new AdaptiveIconDrawable(aid.getBackground(), aid.getForeground(),
                                        new ThemeData(td.mResources, monoId).loadPaddedDrawable());
                    }
                }
                return drawable;
            }
        } catch (PackageManager.NameNotFoundException
                | Resources.NotFoundException /*Add by benny for ABR-15632*/ e) {
            // Add by benny for ABR-15632 @{
     /*       if (DEBUG) {
                Log.d(TAG, "Could not get activityinfo or resources for package: "
                        + mCalendar.getPackageName());
            }*/
            Log.e(TAG, "Could not get activityinfo or resources for package: "
                    + mCalendar.getPackageName() + ", exception is " + e.getMessage());
            // @}
        }
        return null;
    }

    /**
     * @param metadata metadata of the default activity of Calendar
     * @param resources from the Calendar package
     * @return the resource id for today's Calendar icon; 0 if resources cannot be found.
     */
    private int getDynamicIconId(Bundle metadata, Resources resources) {
        if (metadata == null) {
            return ID_NULL;
        }
        String key = mCalendar.getPackageName() + ICON_METADATA_KEY_PREFIX;
        final int arrayId = metadata.getInt(key, ID_NULL);
        if (arrayId == ID_NULL) {
            return ID_NULL;
        }
        try {
            return resources.obtainTypedArray(arrayId).getResourceId(getDay(), ID_NULL);
        } catch (Resources.NotFoundException e) {
            if (DEBUG) {
                Log.d(TAG, "package defines '" + key + "' but corresponding array not found");
            }
            return ID_NULL;
        }
    }

    /**
     * @return Today's day of the month, zero-indexed.
     */
    private static int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1;
    }

    private static ComponentName parseComponentOrNull(Context context, int resId) {
        String cn = context.getString(resId);
        return TextUtils.isEmpty(cn) ? null : ComponentName.unflattenFromString(cn);
    }

    /**
     * Returns a string representation of the current system icon state
     */
    public String getSystemIconState() {
        return (CONFIG_ICON_MASK_RES_ID == ID_NULL
                ? "" : mContext.getResources().getString(CONFIG_ICON_MASK_RES_ID));
    }

    /**
     * Registers a callback to listen for various system dependent icon changes.
     */
    public SafeCloseable registerIconChangeListener(IconChangeListener listener, Handler handler) {
        return new IconChangeReceiver(listener, handler);
    }

    public static class ThemeData {

        final Resources mResources;
        final int mResID;

        public ThemeData(Resources resources, int resID) {
            mResources = resources;
            mResID = resID;
        }

        Drawable loadPaddedDrawable() {
            if (!"drawable".equals(mResources.getResourceTypeName(mResID))) {
                return null;
            }
            Drawable d = mResources.getDrawable(mResID).mutate();
            d = new InsetDrawable(d, .2f);
            float inset = getExtraInsetFraction() / (1 + 2 * getExtraInsetFraction());
            Drawable fg = new InsetDrawable(d, inset);
            return fg;
        }
    }

    private class IconChangeReceiver extends BroadcastReceiver implements SafeCloseable {

        private final IconChangeListener mCallback;
        private String mIconState;

        IconChangeReceiver(IconChangeListener callback, Handler handler) {
            mCallback = callback;
            mIconState = getSystemIconState();


            IntentFilter packageFilter = new IntentFilter(ACTION_OVERLAY_CHANGED);
            packageFilter.addDataScheme("package");
            packageFilter.addDataSchemeSpecificPart("android", PatternMatcher.PATTERN_LITERAL);
            mContext.registerReceiver(this, packageFilter, null, handler);

            if (mCalendar != null || mClock != null) {
                final IntentFilter filter = new IntentFilter(ACTION_TIMEZONE_CHANGED);
                if (mCalendar != null) {
                    filter.addAction(Intent.ACTION_TIME_CHANGED);
                    filter.addAction(ACTION_DATE_CHANGED);
                }
                mContext.registerReceiver(this, filter, null, handler);
            }
            // Benny.Fang {@
            IntentFilter iconPackPropertyChangedFilter = new IntentFilter(ACTION_APPLY_PICKED_ICON_PACK);
            iconPackPropertyChangedFilter.addAction(ACTION_NOTHING_ICON_FORCE_RENDER_ENABLE_CHANGED);/* Add by yixiong.NOS-2146 */
            LocalBroadcastManager.getInstance(mContext).registerReceiver(this, iconPackPropertyChangedFilter);
            // @}
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_TIMEZONE_CHANGED:
                    if (mClock != null) {
                        mCallback.onAppIconChanged(mClock.getPackageName(), Process.myUserHandle());
                    }
                    // follow through
                case ACTION_DATE_CHANGED:
                case ACTION_TIME_CHANGED:
                    if (mCalendar != null) {
                        for (UserHandle user
                                : context.getSystemService(UserManager.class).getUserProfiles()) {
                            mCallback.onAppIconChanged(mCalendar.getPackageName(), user);
                        }
                    }
                    break;
                case ACTION_OVERLAY_CHANGED: {
                    String newState = getSystemIconState();
                    if (!mIconState.equals(newState)) {
                        mIconState = newState;
                        mCallback.onSystemIconStateChanged(mIconState);
                    }
                    // add by henry {@
                    else if (IconPackManager.Companion.getInstance().isSystemIconSelected()) {
                        for (UserHandle user : mContext.getSystemService(UserManager.class).getUserProfiles()) {
                            // TODO: 2023/5/10 后期可以改成online config
                            mCallback.onAppIconChanged("com.android.settings", user);
                        }
                    }
                    // @}
                    break;
                }
                // Benny.Fang {@
                case ACTION_APPLY_PICKED_ICON_PACK: {
                    // When themed icon pack is picked, notify other listeners to update inner state
                    if (intent.getBooleanExtra(IconPackStateConstant.THEMED_ICON_PACK_IN_USE, false)) {
                        mCallback.onThemedIconChanged();
                    }
                    mIconState = getSystemIconState();
                    mCallback.onSystemIconStateChanged(mIconState);
                    break;
                }
                // @}
                // add by yixiong for NOS-2146 @{
                case ACTION_NOTHING_ICON_FORCE_RENDER_ENABLE_CHANGED:{
                    if (intent.hasExtra(IconPackStateConstant.EXTRA_NOTHING_ICON_FORCE_RENDER_ENABLE)) {
                        boolean isNothingForceRenderEnable = intent.getBooleanExtra(
                                IconPackStateConstant.EXTRA_NOTHING_ICON_FORCE_RENDER_ENABLE,false);
                        mCallback.onNothingIconForceRenderChanged(isNothingForceRenderEnable);
                    }
                    break;
                }
                // @}
            }
        }

        @Override
        public void close() {
            mContext.unregisterReceiver(this);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    }

    /**
     * Listener for receiving icon changes
     */
    public interface IconChangeListener {

        /**
         * Called when the icon for a particular app changes
         */
        void onAppIconChanged(String packageName, UserHandle user);

        /**
         * Called when the global icon state changed, which can typically affect all icons
         */
        void onSystemIconStateChanged(String iconState);

        // Benny add for NOS-607 @{
        /**
         * Callback of the change on themed icon state, which can typically affect in icon factory
         */
        default void onThemedIconChanged() {}
        // @}
        // add by yixiong for NOS-2146 @{
        default void onNothingIconForceRenderChanged(boolean isNothingForceRenderEnable) {}
        // @}
    }

    // Add by benny.fang for ABR13T-287 @{
    private Drawable loadIconIconDrawable(boolean isBigIcon, LauncherActivityInfo info, int iconDpi) {
        PackageManager pm = mContext.getPackageManager();
        try {
            ActivityInfo activityInfo = pm.getActivityInfo(info.getComponentName(),
                    PackageManager.ComponentInfoFlags.of(0));
            // Add by benny.fang for ABR13T-287 @{
            // Let remote side knows we are requesting High quality resource
            String bigIconKey = "use_density_xxxhigh";
            /*
              Add by benny for NOS-1072
              AOSP will use info.getIcon(iconDpi=640) firstly, so their picture is more clear.
              Now we try to get more clear resource by sending the signal to PackageManager
            */
            boolean useHighQuality = isBigIcon || IconPackManager.Companion.getInstance().isThemedIconSelected();
            if (activityInfo.metaData == null) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(bigIconKey, useHighQuality);
                activityInfo.metaData = bundle;
            } else {
                activityInfo.metaData.putBoolean(bigIconKey, useHighQuality);
            }
            // @}
            return activityInfo.loadIcon(pm);
        } catch (Exception e) {
            Log.e(TAG, "loadIconIconDrawable error: " + e.getMessage());
        }
        return info.getIcon(iconDpi);
    }
    // @}

    // add by benny {@
    /**
     * Loads the icon for the provided activity from icon pack
     */
    public Drawable getIconForIconPack(CachedIconEntity cachedIconEntity,
                                       LauncherActivityInfo info,
                                       int iconSize, int iconDpi) {
        return cachedIconEntity.getIcon(mContext, info.getComponentName(), iconSize,
                () -> getActivityIcon(info, iconDpi));
    }

    /**
     * Loads the icon for the provided activity
     *
     * Different with {@link IconProvider#getIcon(LauncherActivityInfo, int)},
     * here we are using the original icon announced in component property
     */
    private Drawable getActivityIcon(LauncherActivityInfo info, int iconDpi) {
        return getIconWithOverrides(info.getApplicationInfo().packageName, iconDpi,
                () -> info.getIcon(iconDpi));
    }
    // @}

    // Tenda add this for ABR13T-1432 {@
    // 获取应用原始定义的图标，避免PackageManager 返回包含图标包中的图标资源【系统针对图标包中定义过的应用，都会返回图标包中的资源】
    public Drawable getAppDefinedIcon(ApplicationInfo info, int iconDpi) {
        PackageManager pm = mContext.getPackageManager();
        if (IconPackManager.Companion.getInstance().isFuzzyMatchInIconPack(info.packageName)) {
            try {
                return pm.getResourcesForApplication(info).getDrawableForDensity(info.icon, iconDpi, null);
            } catch (NameNotFoundException | Resources.NotFoundException e) {
                return info.loadIcon(pm);
            }
        } else {
            return info.loadIcon(pm);
        }
    }
    // @}
}
