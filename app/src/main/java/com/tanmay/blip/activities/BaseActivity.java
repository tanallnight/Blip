package com.tanmay.blip.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tanmay.blip.R;

public abstract class BaseActivity extends AppCompatActivity{

    private Toolbar mToolbar;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if(mToolbar != null){
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(getDisplayHomeAsUpEnabled());
        } else {
            throw new NullPointerException("Layout must contain a toolbar with id 'toolbar'");
        }
    }

    protected abstract int getLayoutResource();

    protected abstract boolean getDisplayHomeAsUpEnabled();

    protected Toolbar getToolbar(){
        return this.mToolbar;
    }
}
