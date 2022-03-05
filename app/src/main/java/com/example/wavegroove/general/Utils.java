package com.example.wavegroove.general;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.wavegroove.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.InputStream;

public class Utils {

    // Creating bitmap with rounded corners
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    // Creating bitmap with rounded corners and black border
    public static Bitmap getRoundedCornerBitmapWithBorder(Bitmap bitmap, int cornerSizePx, int borderSizePx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) borderSizePx);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        return output;
    }

    // Covers setter fot available songs
    public static void setRoundedImageWithPicasso(Context context, ImageView imageView, int sizeInDp, String imagePath) {
        imageView.setImageDrawable(null);

        File file = new File(imagePath);
        Target target = drawPicassoTarget(context, imageView, sizeInDp / 4);
        imageView.setTag(target);
        int sizeInPx = Utils.dpToPixels(sizeInDp);

        Picasso.get().load(file).resize(sizeInPx, sizeInPx).centerCrop().placeholder(R.drawable.ic_refresh).into(target);
    }

    // Covers setter fot playlists
    public static void setRoundedImageWithPicasso(Context context, ImageView imageView, int sizeInDp, Uri imageUri) {
        imageView.setImageDrawable(null);

        Target target = drawPicassoTarget(context, imageView, sizeInDp / 4);
        imageView.setTag(target);
        int sizeInPx = Utils.dpToPixels(sizeInDp);

        Picasso.get().load(imageUri).resize(sizeInPx, sizeInPx).centerCrop().placeholder(R.drawable.ic_refresh).into(target);
    }

    // Covers setter fot not available songs
    public static void setRoundedErrorImageWithPicasso(Context context, ImageView imageView, int sizeInDp, String imagePath) {
        imageView.setImageResource(R.drawable.ic_alert_octagon);

        File file = new File(imagePath);
        Target target = drawPicassoErrorTarget(context, imageView, sizeInDp / 4);
        imageView.setTag(target);
        int sizeInPx = Utils.dpToPixels(sizeInDp);

        Picasso.get().load(file).resize(sizeInPx, sizeInPx).centerCrop().placeholder(R.drawable.ic_refresh).into(target);
    }

    // Picasso standard target
    private static Target drawPicassoTarget(Context context, ImageView imageView, int pixels) {
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Bitmap roundedBitmap = getRoundedCornerBitmapWithBorder(bitmap, pixels, pixels / 5);
                Drawable drawable = new BitmapDrawable(context.getResources(), roundedBitmap);
                imageView.setBackground(drawable);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                e.printStackTrace();
            }

            @Override
            public void onPrepareLoad(final Drawable placeHolderDrawable) {
            }
        };
    }

    // Picasso greyed out target with warning in the middle
    private static Target drawPicassoErrorTarget(Context context, ImageView imageView, int pixels) {
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Bitmap roundedBitmap = getRoundedCornerBitmapWithBorder(bitmap, pixels, pixels / 5);
                Drawable drawable = new BitmapDrawable(context.getResources(), roundedBitmap);
                imageView.setBackground(drawable);
                imageView.setColorFilter(Color.GRAY);
                PorterDuffColorFilter greyFilter = new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                imageView.getBackground().setColorFilter(greyFilter);
                imageView.setColorFilter(greyFilter);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                e.printStackTrace();
            }

            @Override
            public void onPrepareLoad(final Drawable placeHolderDrawable) {
            }
        };
    }

    // DPI to pixels calculator
    public static int dpToPixels(int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
        return (int) px;
    }

    // Uri checker
    public static boolean isUriAvailable(Context context, String uri) {
        boolean available = false;
        if (null != uri) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(uri));
                inputStream.close();
                available = true;
            } catch (Exception ignored) {
            }
        }
        return available;
    }

    // Animations setters
    @SuppressLint("ClickableViewAccessibility")
    public static void addScaleUpDownAnimation(MainActivity main, View viewToAnimate) {
        viewToAnimate.setOnTouchListener((v, event) -> {
            Animation scaleUp = AnimationUtils.loadAnimation(main, R.anim.scale_up);
            Animation scaleDown = AnimationUtils.loadAnimation(main, R.anim.scale_down);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                viewToAnimate.startAnimation(scaleUp);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                viewToAnimate.startAnimation(scaleDown);
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void addScaleUpDownAnimation(MainActivity main, int rid) {
        View viewToAnimate = main.findViewById(rid);
        addScaleUpDownAnimation(main, viewToAnimate);
    }

    // Actual fragment getter
    public static Fragment actualFragment(MainActivity mainActivity) {
        return mainActivity.getSupportFragmentManager().findFragmentById(R.id.mainFrameLayout);
    }
}
