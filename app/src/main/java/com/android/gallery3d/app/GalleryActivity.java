/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.gallery3d.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryUtils;
import com.codemx.rxpermission.RxPermissions;

import androidx.annotation.RequiresApi;
import io.reactivex.functions.Consumer;

public final class GalleryActivity extends AbstractGalleryActivity {
    private static final String TAG = "GalleryActivity";
//    public static final String EXTRA_SLIDESHOW = "slideshow";
//    public static final String EXTRA_DREAM = "dream";
//    public static final String EXTRA_CROP = "crop";

//    public static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";
//    public static final String KEY_GET_CONTENT = "get-content";
    public static final String KEY_GET_ALBUM = "get-album";
    public static final String KEY_TYPE_BITS = "type-bits";
    public static final String KEY_MEDIA_TYPES = "mediaTypes";
    public static final String KEY_DISMISS_KEYGUARD = "dismiss-keyguard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        if (getIntent().getBooleanExtra(KEY_DISMISS_KEYGUARD, false)) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        String CPU_ABI = android.os.Build.CPU_ABI;//查看Android设备的ABI
        Log.d("ABI", "CPU_ABI = " + CPU_ABI);

        setContentView(R.layout.main);
        requestPermission(this);

        if (savedInstanceState != null) {
            getStateManager().restoreFromState(savedInstanceState);
        } else {
            initializeByIntent();
        }
    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void requestPermission(Activity activity) {
        Consumer<? super Boolean> consumer = (Consumer<Boolean>) granted -> {
            if (granted) { // Always true pre-M

            } else {
                // Oups permission denied

            }
        };

        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(consumer);
    }

    private void initializeByIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
//
//        if (Intent.ACTION_GET_CONTENT.equalsIgnoreCase(action)) {
//            startGetContent(intent);
//        } else if (Intent.ACTION_PICK.equalsIgnoreCase(action)) {
//            // We do NOT really support the PICK intent. Handle it as
//            // the GET_CONTENT. However, we need to translate the type
//            // in the intent here.
//            Log.w(TAG, "action PICK is not supported");
//            String type = Utils.ensureNotNull(intent.getType());
//            if (type.startsWith("vnd.android.cursor.dir/")) {
//                if (type.endsWith("/image")) intent.setType("image/*");
//                if (type.endsWith("/video")) intent.setType("video/*");
//            }
//            startGetContent(intent);
//        } else if (Intent.ACTION_VIEW.equalsIgnoreCase(action)
//                || ACTION_REVIEW.equalsIgnoreCase(action)){
//            startViewAction(intent);
//        } else
        {
            startDefaultPage();
        }
    }

    public void startDefaultPage() {
        Bundle data = new Bundle();
        data.putString(AlbumPage.KEY_MEDIA_PATH, "/local/image/-1739773001"
                /*getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY)*/);
        getStateManager().startState(AlbumPage.class, data);
    }

    private String getContentType(Intent intent) {
        String type = intent.getType();
        if (type != null) {
            return GalleryUtils.MIME_TYPE_PANORAMA360.equals(type)
                ? MediaItem.MIME_TYPE_JPEG : type;
        }

        Uri uri = intent.getData();
        try {
            return getContentResolver().getType(uri);
        } catch (Throwable t) {
            Log.w(TAG, "get type fail", t);
            return null;
        }
    }

    @Override
    protected void onResume() {
        Utils.assertTrue(getStateManager().getStateCount() > 0);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        final boolean isTouchPad = (event.getSource()
                & InputDevice.SOURCE_CLASS_POSITION) != 0;
        if (isTouchPad) {
            float maxX = event.getDevice().getMotionRange(MotionEvent.AXIS_X).getMax();
            float maxY = event.getDevice().getMotionRange(MotionEvent.AXIS_Y).getMax();
            View decor = getWindow().getDecorView();
            float scaleX = decor.getWidth() / maxX;
            float scaleY = decor.getHeight() / maxY;
            float x = event.getX() * scaleX;
            //x = decor.getWidth() - x; // invert x
            float y = event.getY() * scaleY;
            //y = decor.getHeight() - y; // invert y
            MotionEvent touchEvent = MotionEvent.obtain(event.getDownTime(),
                    event.getEventTime(), event.getAction(), x, y, event.getMetaState());
            return dispatchTouchEvent(touchEvent);
        }
        return super.onGenericMotionEvent(event);
    }
}