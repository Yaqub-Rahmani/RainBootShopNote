package com.example.rainbootshopnote;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rainbootshopnote.Dto.Hadith;

import java.util.ArrayList;
import java.util.Random;

public class SplashScreen extends AppCompatActivity {

    ImageView _logoImage;
    LinearLayout _linerLayout;
    TextView _hatith, _translate;
    Animation _topAnimation, _botAnimation;
    int cam = 5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ArrayList<Hadith> hadithList = new ArrayList<>();
        hadithList.add(new Hadith("نیکوکاری، اخلاق نیکو است.","الْبِرُّ حُسْنُ الْخُلُقِ",1));
        hadithList.add(new Hadith("هر کسی که از خدا بترسد، برکت در مالش خواهد بود.","مَنْ خَشِيَ اللَّهَ فَإِنَّ الْبَرَكَةَ فِي مَالِهِ",2));
        hadithList.add(new Hadith("کار های نیک به سوی او می روند.","الْحَسَنَاتُ يُذْهَبُ إِلَيْهَا",3));
        hadithList.add(new Hadith("هر کس که در تجارت به موقع عمل نکند، زیان می برد.","مَنْ لَا يُبَادِرُ فِي الْعَامِ تَجَارَةً يَخْسَرُ",4));
        hadithList.add(new Hadith("بهترین شما کسی است که با دیگران خوب رفتار کند.","إِنَّ أَفْضَلَكُمْ مَنْ أَحْسَنَ صُحْبَتَكُمْ",5));
        hadithList.add(new Hadith("هر کسی که از شما چیزی بخواهد، آن را به او بدهید.","مَنْ جَاءَكُمْ يَطْلُبُ شَيْئًا فَأَعْطُوهُ إِيَّاهُ",6));
        hadithList.add(new Hadith("هر کسی که از خدا شفای بیماری را بخواهد، پاداشی خواهد داشت.","مَنْ سَأَلَ اللَّهَ الشَّفَاءِ فَإِنَّ لَهُ جَزَاءً",7));
        hadithList.add(new Hadith("تجارت باید با نیت صادقانه باشد.","الْبَيْعُ لَا يَكُونُ إِلَّا بِنِيَّةٍ صَادِقَةٍ",8));

        Random mRandom = new Random();
        int code = mRandom.nextInt(7)+1;

        Hadith selectedHadith = null;
        for (Hadith hadith : hadithList) {
            if (hadith.getId() == code) {
                selectedHadith = hadith;
                break;
            }
        }

        _logoImage = findViewById(R.id.vlogoImage);
        _linerLayout = findViewById(R.id.vlinerLayout);

        _hatith = findViewById(R.id.tvhadith);
        _translate = findViewById(R.id.tvtranslate);

        _translate.setText(selectedHadith.getTranslate());
        _hatith.setText(selectedHadith.getHadith());

        _botAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        _topAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        _logoImage.setAnimation(_botAnimation);
        _linerLayout.setAnimation(_botAnimation);
        _translate.setAnimation(_topAnimation);
        _hatith.setAnimation(_botAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent myIntent = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(myIntent);
                finish();
            }
        }, cam);

    }
}