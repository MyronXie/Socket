package com.example.quanzhi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class HelpActivity extends Activity implements OnClickListener {

	public Button btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        
//        btn_back.setOnClickListener(this);

    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
//		case R.id.hp_back:
//			Intent intent = new Intent();
//			intent.setClass(HelpActivity.this, MainActivity.class);
//			startActivity(intent);
//			break;
		}
	}
}
