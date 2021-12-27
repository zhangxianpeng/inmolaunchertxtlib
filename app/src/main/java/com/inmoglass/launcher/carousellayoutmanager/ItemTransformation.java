package com.inmoglass.launcher.carousellayoutmanager;

/**
 * @author Administrator
 */
public class ItemTransformation {

    final float mScaleX;
    final float mScaleY;
    final float mTranslationX;
    final float mTranslationY;
    final float mAlpha;

    public ItemTransformation(final float scaleX, final float scaleY, final float translationX, final float translationY, final float alpha) {
        mScaleX = scaleX;
        mScaleY = scaleY;
        mTranslationX = translationX;
        mTranslationY = translationY;
        mAlpha = alpha;
    }
}