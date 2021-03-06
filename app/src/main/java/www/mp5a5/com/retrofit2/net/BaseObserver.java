package www.mp5a5.com.retrofit2.net;

import com.google.gson.JsonParseException;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.ParseException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import www.mp5a5.com.retrofit2.R;
import www.mp5a5.com.retrofit2.customview.dialog.CustomProgressDialogUtils;
import www.mp5a5.com.retrofit2.utils.ToastUtils;

/**
 * @author ：王文彬 on 2018/5/22 13：52
 * @describe： 网络状态的封装类，子类初始化该类后，必须重写onSuccess(response)方法，可以选择性的重写onFailing(response)
 * 方法，如果子类使用super方法，那么会按照统一的方法进行{@link ToastUtils}，如果不使用则根据返回的response自行进行操作，如果选择重写onError(Throwable e)
 * 方法，如果子类使用super方法，那么会按照统一的方法进行{@link ToastUtils}，如果不使用则不会{@link ToastUtils}，但是您可以做自己的操作
 * @email：wwb199055@126.com
 */
public abstract class BaseObserver<T extends BaseResponseEntity> implements Observer<T> {


  private Activity mContext;
  private boolean mShowLoading = false;
  private CustomProgressDialogUtils progressDialogUtils;
  private static final String TOKEN_INVALID_TAG = "token_invalid";

  public BaseObserver() {
  }

  /**
   * 如果传入上下文，那么表示您将开启自定义的进度条
   *
   * @param context 上下文
   */
  public BaseObserver(Activity context) {
    this.mContext = context;
    this.mShowLoading = true;
  }

  @Override
  public void onSubscribe(Disposable d) {
    onRequestStart();
  }


  @Override
  public void onNext(T response) {

    onRequestEnd();
    if (response.isSuccess()) {
      try {
        onSuccess(response);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (response.getTokenInvalid() == response.getCode()) {
      EventBus.getDefault().post(new EventBusMessage<>(TOKEN_INVALID_TAG));
    } else {
      try {
        onFailing(response);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  @Override
  public void onError(Throwable e) {
    onRequestEnd();
    if (e instanceof retrofit2.HttpException) {
      //HTTP错误
      onException(ExceptionReason.BAD_NETWORK);
    } else if (e instanceof ConnectException || e instanceof UnknownHostException) {
      //连接错误
      onException(ExceptionReason.CONNECT_ERROR);
    } else if (e instanceof InterruptedIOException) {
      //连接超时
      onException(ExceptionReason.CONNECT_TIMEOUT);
    } else if (e instanceof JsonParseException || e instanceof JSONException || e instanceof ParseException) {
      //解析错误
      onException(ExceptionReason.PARSE_ERROR);
    } else {
      //其他错误
      onException(ExceptionReason.UNKNOWN_ERROR);
    }
  }

  private void onException(ExceptionReason reason) {
    switch (reason) {
      case CONNECT_ERROR:
        ToastUtils.show(R.string.connect_error, Toast.LENGTH_SHORT);
        break;

      case CONNECT_TIMEOUT:
        ToastUtils.show(R.string.connect_timeout, Toast.LENGTH_SHORT);
        break;

      case BAD_NETWORK:
        ToastUtils.show(R.string.bad_network, Toast.LENGTH_SHORT);
        break;

      case PARSE_ERROR:
        ToastUtils.show(R.string.parse_error, Toast.LENGTH_SHORT);
        break;

      case UNKNOWN_ERROR:
      default:
        ToastUtils.show(R.string.unknown_error, Toast.LENGTH_SHORT);
        break;
    }
  }

  @Override
  public void onComplete() {
    onRequestEnd();
  }

  /**
   * 网络请求成功并返回正确值
   *
   * @param response 返回值
   */
  public abstract void onSuccess(T response);

  /**
   * 网络请求成功但是返回值是错误的
   *
   * @param response 返回值
   */
  public void onFailing(T response) {
    String message = response.getMsg();
    if (TextUtils.isEmpty(message)) {
      ToastUtils.show(R.string.response_return_error);
    } else {
      ToastUtils.show(message);
    }
  }


  /**
   * 网络请求失败原因
   */
  public enum ExceptionReason {
    /**
     * 解析数据失败
     */
    PARSE_ERROR,
    /**
     * 网络问题
     */
    BAD_NETWORK,
    /**
     * 连接错误
     */
    CONNECT_ERROR,
    /**
     * 连接超时
     */
    CONNECT_TIMEOUT,
    /**
     * 未知错误
     */
    UNKNOWN_ERROR
  }

  /**
   * 网络请求开始
   */
  private void onRequestStart() {
    if (mShowLoading) {
      showProgressDialog();
    }
  }

  /**
   * 网络请求结束
   */
  private void onRequestEnd() {
    closeProgressDialog();
  }

  /**
   * 开启Dialog
   */
  private void showProgressDialog() {
    progressDialogUtils = new CustomProgressDialogUtils();
    progressDialogUtils.showProgress(mContext, "Loading...");
  }

  /**
   * 关闭Dialog
   */
  private void closeProgressDialog() {
    if (progressDialogUtils != null) {
      progressDialogUtils.dismissProgress();
    }
  }
}
