package com.sfsu.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sfsu.investickation.R;
import com.sfsu.investickation.TickGuideMasterActivity;
import com.sfsu.investickation.TickInfoActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Adapter to display images in the ViewPager contained in the
 * {@link com.sfsu.investickation.fragments.TickGuideDetailFragment}. The slider displays multiple images for each Tick
 * Created by Pavitra on 4/30/2016.
 */
public class ImagePagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<String> mImageUrls;
    private PhotoViewAttacher mAttacher;

    // private List<Integer> mImageUrls;

    public ImagePagerAdapter(Context mContext, List<String> mImageUrls) {
        //   public ImagePagerAdapter(Context mContext, List<Interger> mImageUrls) {
        this.mContext = mContext;
        this.mImageUrls = mImageUrls;
    }

    @Override
    public int getCount() {
        return mImageUrls.size();
    }

    public void showImage(String imageUrl) {
        Dialog builder = new Dialog((TickGuideMasterActivity)mContext);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(mContext);
//        Bitmap bitmap = new ImageController(mContext).getBitmapForImageView(imageView, mObservation.getImageUrl());
//        imageView.setImageBitmap(bitmap);
        Picasso.with(mContext).load(imageUrl).into(imageView);
        mAttacher = new PhotoViewAttacher(imageView);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((TickGuideMasterActivity)mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                width,
                height));
        builder.show();
        mAttacher.update();
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.pagerimage_slider, container, false);
        ImageView mImageView = (ImageView) view.findViewById(R.id.imageview_pager_tick_image_main);
//        mImageView.setImageResource(mImageUrls.get(position)); // delete when u use glide -- its a set image resource
        // IMP step
        container.addView(view, 0);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage(mImageUrls.get(position));
            }
        });

        // insert Picasso
        Picasso.with(mContext).load(mImageUrls.get(position)).into(mImageView);

        return view;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
