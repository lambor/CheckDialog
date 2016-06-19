package dcnh35.com.checkdialoglibrary;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


/**
 * Created by lambor on 2016/1/9.
 */
public class CheckDialog extends Dialog {
    private CheckmarkView checkView;
    private ProgressWheel progressWheel;
    private boolean resulted = false;

    private OnFinishListener finishListener;
    private OnResultListener resultListener;

    public CheckDialog(Context context) {
        super(context);

        this.setCanceledOnTouchOutside(false);
        this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        this.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        setContentView(R.layout.loading_check_dialog_layout);
        checkView = (CheckmarkView) findViewById(R.id.check);
        checkView.setCompleteListener(new CheckmarkView.OnAnimationCompleteListener() {
            @Override
            public void onComplete() {
                if(resultListener!=null) resultListener.onResult();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(CheckDialog.this.isShowing()){
                            CheckDialog.this.dismiss();
                            if(finishListener!=null) finishListener.onFinish();
                        }
                    }
                },500);
            }
        });
        progressWheel = (ProgressWheel) findViewById(R.id.progress);
    }

    public CheckDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    private void OnResult( @CheckmarkView.WaitResult final int result){
        if(!resulted) resulted = true;
        else return;
        progressWheel.setCallback(new ProgressWheel.ProgressCallback(){
            @Override
            public void onProgressUpdate(float progress) {
                if(progress == -1.0f){
                    progressWheel.setProgress(1.0f);
                }else if(progress == 1.0f){
                    checkView.start(result);
                }
            }
        });
    }

    public void OnOk(){
        OnResult(CheckmarkView.OK);
    }

    public void OnError(){
        OnResult(CheckmarkView.ERROR);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //do cleanup
    }

    //do something after dialog dismiss self
    public interface OnFinishListener{
        void onFinish();
    }

    public void setFinishListener(OnFinishListener listener){
        this.finishListener = listener;
    }

    //do something when finish showing result
    public interface OnResultListener{
        void onResult();
    }

    public void setResultListener(OnResultListener listener){
        this.resultListener = listener;
    }

    private void setMessageListener(final String msg, final Activity activity){
        setResultListener(new OnResultListener() {
            @Override
            public void onResult() {
//                new MessageBar(activity).show(msg);
                Toast.makeText(activity,msg,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void OnOk(String msg,Activity activity){
        setMessageListener(msg,activity);
        OnOk();
    }

    public void OnError(String msg,Activity activity){
        setMessageListener(msg,activity);
        OnError();
    }
}
