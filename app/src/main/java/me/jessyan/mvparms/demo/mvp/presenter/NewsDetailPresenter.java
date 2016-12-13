package me.jessyan.mvparms.demo.mvp.presenter;

import android.app.Application;

import com.jess.arms.base.BaseActivity;
import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.mvp.BasePresenter;
import com.jess.arms.utils.PermissionUtil;
import com.tbruyelle.rxpermissions.RxPermissions;

/**
 * 通过Template生成对应页面的MVP和Dagger代码,请注意输入框中输入的名字必须相同
 * 由于每个项目包结构都不一定相同,所以每生成一个文件需要自己导入import包名,可以在设置中设置自动导入包名
 * 请在对应包下按以下顺序生成对应代码,Contract->Model->Presenter->Activity->Module->Component
 * 因为生成Activity时,Module和Component还没生成,但是Activity中有它们的引用,所以会报错,但是不用理会
 * 继续将Module和Component生成完后,编译一下项目再回到Activity,按提示修改一个方法名即可
 * 如果想生成Fragment的相关文件,则将上面构建顺序中的Activity换为Fragment,并将Component中inject方法的参数改为此Fragment
 */

import javax.inject.Inject;

import me.jessyan.mvparms.demo.mvp.contract.NewsDetailContract;
import me.jessyan.mvparms.demo.mvp.model.entity.NewsDetailEntity;
import me.jessyan.mvparms.demo.mvp.model.entity.NewsListEntity;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.ErrorHandleSubscriber;
import me.jessyan.rxerrorhandler.handler.RetryWithDelay;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by xing on 2016/12/8.
 */

@ActivityScope
public class NewsDetailPresenter extends BasePresenter<NewsDetailContract.Model, NewsDetailContract.View> {
    private RxErrorHandler mErrorHandler;
    private Application mApplication;
    private RxPermissions mRxPermissions;

    @Inject
    public NewsDetailPresenter(NewsDetailContract.Model model, NewsDetailContract.View rootView
            , RxErrorHandler handler, Application application, RxPermissions rxPermissions) {
        super(model, rootView);
        this.mErrorHandler = handler;
        this.mApplication = application;
        this.mRxPermissions = rxPermissions;
    }

   public void getNewsDetail(int id){
       PermissionUtil.externalStorage(new PermissionUtil.RequestPermission() {
           @Override
           public void onRequestPermissionSuccess() {

           }
       }, mRxPermissions, mRootView, mErrorHandler);

       mModel.getNewsDetail(id).subscribeOn(Schedulers.io())
               .retryWhen(new RetryWithDelay(3,2))
               .doOnSubscribe(new Action0() {
                   @Override
                   public void call() {
                       mRootView.showLoading();
                   }
               }).subscribeOn(AndroidSchedulers.mainThread())
               .observeOn(AndroidSchedulers.mainThread())
               .doAfterTerminate(new Action0() {
                   @Override
                   public void call() {
                       mRootView.hideLoading();
                   }
               }).compose(((BaseActivity)mRootView).<NewsDetailEntity>bindToLifecycle())
                .subscribe(new ErrorHandleSubscriber<NewsDetailEntity>(mErrorHandler) {
                    @Override
                    public void onNext(NewsDetailEntity entity) {
                        mRootView.loadData(entity);
                    }
                });
   }
}