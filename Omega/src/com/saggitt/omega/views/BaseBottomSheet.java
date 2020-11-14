/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.IntProperty;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.util.SystemUiController;
import com.android.launcher3.util.Themes;
import com.android.launcher3.views.AbstractSlideInView;

public class BaseBottomSheet extends AbstractSlideInView implements Insettable {

    private static final IntProperty<View> PADDING_BOTTOM =
            new IntProperty<View>("paddingBottom") {
                @Override
                public void setValue(View view, int paddingBottom) {
                    view.setPadding(view.getPaddingLeft(), view.getPaddingTop(),
                            view.getPaddingRight(), paddingBottom);
                }

                @Override
                public Integer get(View view) {
                    return view.getPaddingBottom();
                }
            };

    private static final int DEFAULT_CLOSE_DURATION = 200;
    protected final ColorScrim mColorScrim;
    private Rect mInsets;

    public BaseBottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mColorScrim = ColorScrim.createExtractedColorScrim(this);
        setWillNotDraw(false);
        mInsets = new Rect();
        mContent = this;
    }

    public static BaseBottomSheet inflate(Launcher launcher) {
        return (BaseBottomSheet) launcher.getLayoutInflater()
                .inflate(R.layout.base_bottom_sheet, launcher.getDragLayer(), false);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        setTranslationShift(mTranslationShift);
    }

    public void show(View view, boolean animate) {
        ((ViewGroup) findViewById(R.id.sheet_contents)).addView(view);

        mLauncher.getDragLayer().addView(this);
        mIsOpen = false;
        animateOpen(animate);
    }

    protected void setTranslationShift(float translationShift) {
        super.setTranslationShift(translationShift);
        mColorScrim.setProgress(1 - mTranslationShift);
    }

    protected void onCloseComplete() {
        super.onCloseComplete();
        clearNavBarColor();
    }

    protected void clearNavBarColor() {
        mLauncher.getSystemUiController().updateUiState(
                SystemUiController.UI_STATE_WIDGET_BOTTOM_SHEET, 0);
    }

    protected void setupNavBarColor() {
        boolean isSheetDark = Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark);
        mLauncher.getSystemUiController().updateUiState(
                SystemUiController.UI_STATE_WIDGET_BOTTOM_SHEET,
                isSheetDark ? SystemUiController.FLAG_DARK_NAV : SystemUiController.FLAG_LIGHT_NAV);
    }

    private void animateOpen(boolean animate) {
        if (mIsOpen || mOpenCloseAnimator.isRunning()) {
            return;
        }
        mIsOpen = true;
        setupNavBarColor();
        mOpenCloseAnimator.setValues(
                PropertyValuesHolder.ofFloat(TRANSLATION_SHIFT, TRANSLATION_SHIFT_OPENED));
        mOpenCloseAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        if (!animate) {
            mOpenCloseAnimator.setDuration(0);
        }
        mOpenCloseAnimator.start();
    }

    @Override
    protected void handleClose(boolean animate) {
        handleClose(animate, DEFAULT_CLOSE_DURATION);
    }

    @Override
    protected boolean isOfType(@FloatingViewType int type) {
        return (type & TYPE_SETTINGS_SHEET) != 0;
    }

    @Override
    public void setInsets(Rect insets) {
        // Extend behind left, right, and bottom insets.
        int leftInset = insets.left - mInsets.left;
        int rightInset = insets.right - mInsets.right;
        int bottomInset = insets.bottom - mInsets.bottom;
        mInsets.set(insets);

        if (!Utilities.ATLEAST_OREO && !mLauncher.getDeviceProfile().isVerticalBarLayout()) {
            View navBarBg = findViewById(R.id.nav_bar_bg);
            ViewGroup.LayoutParams navBarBgLp = navBarBg.getLayoutParams();
            navBarBgLp.height = bottomInset;
            navBarBg.setLayoutParams(navBarBgLp);
            bottomInset = 0;
        }

        setPadding(getPaddingLeft() + leftInset, getPaddingTop(),
                getPaddingRight() + rightInset, getPaddingBottom() + bottomInset);
    }

    @Override
    public final void logActionCommand(int command) {

    }

    @Nullable
    @Override
    public Animator createHintCloseAnim(float distanceToMove) {
        return ObjectAnimator.ofInt(this, PADDING_BOTTOM, (int) (distanceToMove + mInsets.bottom));
    }
}
