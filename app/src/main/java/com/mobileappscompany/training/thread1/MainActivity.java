package com.mobileappscompany.training.thread1;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.Subscription;

public class MainActivity extends AppCompatActivity {
    TextView tV;
    int tFAT = 7;
    Subscription mySub; //For RxJava

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tV = (TextView) findViewById(R.id.textView);

    }
///////////////////////// Start Thread /////////////////////////
    public void onDo(View view) {

        new Thread(){

            public void run(){


                try {
                    Thread.sleep(5000);

                    tV.post(new Runnable() {
                        @Override
                        public void run() {
                            tV.setText("Done with Thread");
                        }
                    });


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

        }.start();

    }


    ///////////////////////// End Thread /////////////////////////

    ///////////////////////// Start AsyncTask /////////////////////////
    private class MyAT extends AsyncTask<Integer,Integer, String>{

        @Override
        protected String doInBackground(Integer... params) {
            int tTS = params[0] *100;

            for(int i = 0; i<10; i++){
                try {
                    Thread.sleep(tTS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(i);
            }

            return "Done with AT " + tTS/100;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            tV.setText(String.valueOf(values[0]+1) );
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            tV.setText(s);
            Log.d("MAC_Tag", s);

        }



    }

    public void onAT(View view) {

        MyAT mAT = new MyAT();

       mAT.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,tFAT--);

    }
    ///////////////////////// End AsyncTask /////////////////////////



///////////////////////// Start EventBus /////////////////////////

    public class MyEvent{
        private String mMssg;

        public MyEvent(String mssg) {
            mMssg = mssg;
        }

        public String getMssg() {
            return mMssg;
        }
    }


    public void onEB(View view) {
        new Thread(){
            public void run(){

                try {
                    Thread.sleep(1500);
                    EventBus.getDefault().post(new MyEvent("Done with EB"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public  void getEvent(MyEvent e){
        tV.setText(e.getMssg());
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    ///////////////////////// Start RxJava /////////////////////////


    public void onRx(View view) {
        Observable<Integer> myO = Observable.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() {
                int tts = 3000;
                try {
                    Thread.sleep(tts);
                    return tts;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return -1;
                }

            }
        });

        mySub = myO
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        tV.setText("Done RxJava " + integer);
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( mySub != null && !mySub.isUnsubscribed() ){
            mySub.unsubscribe();
        }
    }

    ///////////////////////// End RxJava /////////////////////////

}
