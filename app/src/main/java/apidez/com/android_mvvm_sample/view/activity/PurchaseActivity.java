package apidez.com.android_mvvm_sample.view.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import apidez.com.android_mvvm_sample.MyApplication;
import apidez.com.android_mvvm_sample.R;
import apidez.com.android_mvvm_sample.utils.RxTextViewEx;
import apidez.com.android_mvvm_sample.utils.UiUtils;
import apidez.com.android_mvvm_sample.viewmodel.IPurchaseViewModel;
import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by nongdenchet on 10/1/15.
 */
public class PurchaseActivity extends BaseActivity {

    @Bind(R.id.creditCard)
    EditText mEdtCreditCard;

    @Bind(R.id.email)
    EditText mEdtEmail;

    @Bind(R.id.layoutCreditCard)
    TextInputLayout mLayoutCreditCard;

    @Bind(R.id.layoutEmail)
    TextInputLayout mLayoutEmail;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.btnSubmit)
    TextView mBtnSubmit;

    @Inject
    IPurchaseViewModel mViewModel;

    private ProgressDialog mProgressDialog;
    private View.OnClickListener onSubmitClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        // Setup dependency
        ((MyApplication) getApplication())
                .builder()
                .purchaseComponent()
                .inject(this);

        // Setup butterknife
        ButterKnife.bind(this);

        // Setup views
        setUpView();
    }

    private void setUpView() {
        // Progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);

        // Toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void bindViewModel() {
        // binding credit card
        RxTextViewEx.textChanges(mEdtCreditCard)
                .takeUntil(preDestroy())
                .subscribe(mViewModel::nextCreditCard);

        // binding email
        RxTextViewEx.textChanges(mEdtEmail)
                .takeUntil(preDestroy())
                .subscribe(mViewModel::nextEmail);

        // create event on click on submit
        onSubmitClickListener = v -> mViewModel.submit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .takeUntil(preDestroy())
                .doOnSubscribe(mProgressDialog::show)
                .doOnTerminate(mProgressDialog::hide)
                .subscribe(done -> {
                    UiUtils.showDialog(getString(R.string.success), this);
                }, throwable -> {
                    UiUtils.showDialog(getString(R.string.error), this);
                });

        // binding credit card change
        mViewModel.creditCardValid()
                .takeUntil(preDestroy())
                .subscribe(valid -> {
                    mLayoutCreditCard.setError(valid ? "" : getString(R.string.error_credit_card));
                });

        // binding password change
        mViewModel.emailValid()
                .takeUntil(preDestroy())
                .subscribe(valid -> {
                    mLayoutEmail.setError(valid ? "" : getString(R.string.error_email));
                });

        // can submit
        mViewModel.canSubmit()
                .takeUntil(preDestroy())
                .subscribe(active -> {
                    mBtnSubmit.setBackgroundResource(active ? R.drawable.bg_submit : R.drawable.bg_inactive_submit);
                    mBtnSubmit.setOnClickListener(active ? onSubmitClickListener : null);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // bind to viewmodel
        bindViewModel();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}