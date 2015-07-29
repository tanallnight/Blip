/*
 * Copyright 2015, Tanmay Parikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tanmay.blip.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.tanmay.blip.Keys;
import com.tanmay.blip.R;

import java.util.List;

public class DonateActivity extends BaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener, BillingProcessor.IBillingHandler {

    private ViewPager mPager;
    private TextView mDonateButton;
    private BillingProcessor mBillingProcessor;
    private int mCurrentPosition = 0;

    @Override
    protected int getToolbarColor() {
        return getResources().getColor(R.color.primary);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_donate;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPager = (ViewPager) findViewById(R.id.pager);
        mDonateButton = (TextView) findViewById(R.id.donate);

        mPager.addOnPageChangeListener(this);

        mDonateButton.setClickable(false);
        mDonateButton.setOnClickListener(this);

        mBillingProcessor = new BillingProcessor(this, Keys.GOOGLE_PLAY_RSA_PUBLIC, this);
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPosition = position;
    }

    @Override
    public void onClick(View v) {
        mBillingProcessor.purchase(this, Keys.SKU_NAMES.get(mCurrentPosition));
    }

    @Override
    public void onBillingInitialized() {
        mDonateButton.setClickable(true);
        List<SkuDetails> detailsList = mBillingProcessor.getPurchaseListingDetails(Keys.SKU_NAMES);
        mPager.setAdapter(new DonatePagerAdapter(detailsList));
    }

    @Override
    public void onBillingError(int i, Throwable throwable) {

    }

    @Override
    public void onProductPurchased(String s, TransactionDetails transactionDetails) {
        mBillingProcessor.consumePurchase(s);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (mBillingProcessor != null)
            mBillingProcessor.release();
        super.onDestroy();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public class DonatePagerAdapter extends PagerAdapter {

        private List<SkuDetails> mDetailsList;

        public DonatePagerAdapter(List<SkuDetails> detailsList) {
            mDetailsList = detailsList;
        }

        @Override
        public int getCount() {
            return mDetailsList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getLayoutInflater().inflate(R.layout.item_donate_text, container, false);
            ((TextView) view.findViewById(R.id.text)).setText(mDetailsList.get(position).priceText);
            container.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
