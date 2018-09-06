package com.xiaomi.emm.view.photoview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.xiaomi.emm.R;
import com.xiaomi.emm.view.photoview.bens.MediaPhoto;
import com.xiaomi.emm.view.photoview.constants.Constant;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2017/11/8.
 */

public class PhotoPreviewActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
	private static final String TAG = "PhotoPreviewActivity";
	private ArrayList<MediaPhoto> mPreviewList;
	private RelativeLayout mRlBack;
	private RelativeLayout mRlTopBar;
	private RelativeLayout mRlCheck;
	private RelativeLayout mRlBottomBar;
	private TextView mTvComplete;
	private PhotoPreviewViewPagerAdpater mAdvertAdapter;
	private ViewPager mViewPager;
	private ImageView mIvCheck;
	private int mPosition;
	private int mCheckNumber = 0;
	private TextView mTvTitle;
	private int mMaxCount;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview_photo);
		initView();
		initData();
		initListener();
		Log.w(TAG,"PhotoPreviewActivity");
	}

	private void initView() {
		mRlBack = (RelativeLayout) findViewById(R.id.rl_back);
		mRlTopBar = (RelativeLayout) findViewById(R.id.rl_top_bar);
		mRlCheck = (RelativeLayout) findViewById(R.id.rl_check);
		mRlBottomBar = (RelativeLayout) findViewById(R.id.rl_bottom_bar);
		mTvComplete = (TextView) findViewById(R.id.tv_complete);
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mViewPager = (ViewPager) findViewById(R.id.top_view_pager);
		mIvCheck = (ImageView) findViewById(R.id.iv_check);
	}

	private void initData() {
		Intent intent = getIntent();
		if (intent != null) {
			mMaxCount = intent.getIntExtra(Constant.MAX_COUNT, 0);
			mPreviewList = (ArrayList<MediaPhoto>) intent.getSerializableExtra(Constant.PHOTO_PREVIEW_LIST);
			mPosition = intent.getIntExtra(Constant.PHOTO_POSITION, 0);
			boolean titleIsGone = intent.getBooleanExtra(Constant.TITLE_IS_GONE, true);
			if (titleIsGone) {
				mTvTitle.setVisibility(View.GONE);
			} else {
				mTvTitle.setVisibility(View.VISIBLE);
			}
			if (mPreviewList == null || mPreviewList.size() == 0) {
                return;
            }
			List<ImageView> imageList = new ArrayList<>();
			for (int i = 0; i < mPreviewList.size(); i++) {
				ImageView imageView = new ImageView(this);
				imageList.add(imageView);
				if (mPreviewList.get(i).isCheck()) {
					mCheckNumber++;
				}
			}
			Log.e(TAG, "initData: " + mPreviewList.size());
			mAdvertAdapter = new PhotoPreviewViewPagerAdpater(this, mPreviewList, imageList);
			mViewPager.setAdapter(mAdvertAdapter);
			mViewPager.setCurrentItem(mPosition);
		}
	}

	private void initListener() {
		mRlBack.setOnClickListener(this);
		mRlCheck.setOnClickListener(this);
		mTvComplete.setOnClickListener(this);
		mViewPager.addOnPageChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_back:
				finish();
				break;
			case R.id.rl_check:
				boolean isCheck = mPreviewList.get(mPosition).isCheck();
				if (isCheck) {
					mPreviewList.get(mPosition).setCheck(false);
					mIvCheck.setSelected(false);
					mCheckNumber--;
				} else {
					if (mCheckNumber >= mMaxCount && !isCheck) {
						toast(getResources().getString(R.string.photo_limit, mMaxCount + ""));
						return;
					}
					mPreviewList.get(mPosition).setCheck(true);
					mIvCheck.setSelected(true);
					mCheckNumber++;
				}
				setTvCheckNumber();
				break;
			case R.id.tv_complete:
				if (mCheckNumber < mMaxCount){
					toast(getResources().getString(R.string.pick_photos, mMaxCount + "" ));
					return;
				}else if(mCheckNumber > mMaxCount){
					toast(getResources().getString(R.string.had_pick_photos, mMaxCount + "" ));
					return;
				}
				ArrayList<MediaPhoto> checkList = new ArrayList();
				for (int i = 0; i < mPreviewList.size(); i++) {
					if (mPreviewList.get(i).isCheck()) {
						checkList.add(mPreviewList.get(i));
					}
				}
				Intent intent = new Intent();
				intent.putExtra(Constant.PHOTO_CHECK_LIST, checkList);
				setResult(Constant.PREVIEW_PHOTO, intent);
				finish();
				break;
		}
	}

	private void setTvCheckNumber() {
		if (mCheckNumber == 0) {
			mTvComplete.setText(getResources().getString(R.string.finish));
		} else {
			mTvComplete.setText(getResources().getString(R.string.finish) + "(" + mCheckNumber + ")");
		}
	}

	boolean isFirst = true;

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		if (isFirst) {
			mPosition = position;
			if (mPreviewList.get(mPosition).isCheck()) {
				mIvCheck.setSelected(true);
			}
			setTvCheckNumber();
			mTvTitle.setText(mPosition + 1 + "/" + mPreviewList.size());
			isFirst = false;
		}
	}

	@Override
	public void onPageSelected(int position) {
		Log.e(TAG, "onPageSelected: " + position);
		mPosition = position;
		Log.e(TAG, "onPageSelected: " + mPreviewList.get(position).isCheck());
		if (mPreviewList.get(position).isCheck()) {
			mIvCheck.setSelected(true);
		} else {
			mIvCheck.setSelected(false);
		}
		mTvTitle.setText(mPosition + 1 + "/" + mPreviewList.size());
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	public class PhotoPreviewViewPagerAdpater extends PagerAdapter {
		private static final String TAG = "PhotoPreviewViewPagerAdpater";
		Context mContext;
		List<MediaPhoto> mList;
		List<ImageView> mImageList;

		public PhotoPreviewViewPagerAdpater(Context context, List<MediaPhoto> list, List<ImageView> imageList) {
			mContext = context;
			mList = list;
			mImageList = imageList;
		}

		@Override
		public int getCount() {
			if (mList != null) {
				return mList.size();
			}
			return 0;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			ImageView imageView = mImageList.get(position);
			imageView.setBackgroundColor(Color.parseColor("#000000"));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			container.addView(imageView);
			String url = mList.get(position).getUrl();
			Glide.with(mContext).load(url).into(imageView);
			/*try {
				FileInputStream fis = new FileInputStream(url);
				Bitmap bitmap = BitmapFactory.decodeStream(fis);///把流转化为Bitmap

				imageView.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}*/
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}

	public void toast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}
