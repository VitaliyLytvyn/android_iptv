package com.app.tv.mediacast.info;

import android.app.DialogFragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.app.tv.mediacast.util.Constant;
import com.app.tv.mediacast.util.Global;
import com.app.tv.mediacast.util.GlobalProgressDialog;
import com.app.tv.mediacast.util.InternetConnection;
import com.app.tv.mediacast.MyApplication;
import com.app.tv.mediacast.R;
import com.app.tv.mediacast.retrofit.Restapi;
import com.app.tv.mediacast.retrofit.data.DataAuthToken;
import com.app.tv.mediacast.retrofit.data.DataError;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.app.tv.mediacast.util.Constant.PACKAGE_ID;
import static com.app.tv.mediacast.util.Constant.STRIPE;
import static com.app.tv.mediacast.util.Constant.SUBSCRIPTION_ID;
import static com.app.tv.mediacast.util.Constant.TOKEN;

public class InfoSubscribeStripeActivity extends AppCompatActivity {

    /* Change this to your publishable key. You can get your key here:
     * https://manage.stripe.com/account/apikeys*/
    public static final String PUBLISHABLE_KEY = "pk_test_9m005tX3Q33qMDgOCpmPlxsh";///  TODO CHANGE


    /** Card number entering - this provide format 'xxxx xxxx xxxx xxxx'
     * Breakdown of this regexp:
     * ^             - Start of the string
     * (\\d{4}\\s)*  - A group of four digits, followed by a whitespace, e.g. "1234 ". Zero or more times.
     * \\d{0,4}      - Up to four (optional) digits.
     * (?<!\\s)$     - End of the string, but NOT with a whitespace just before it.
     *
     * Example of matching strings:
     *  - "2304 52"
     *  - "2304"
     *  - ""
     */
    private final String regexp = "^(\\d{4}\\s)*\\d{0,4}(?<!\\s)$";
    private static final char SPACE_CHAR = ' ';
    private static final String SPACE_STRING = String.valueOf(SPACE_CHAR);
    private static final int GROUPSIZE = 4;
    private boolean isUpdating = false;
    private boolean isRenew;

    Button saveButton;
    EditText cardNumberEditText;
    EditText cvcEditText;
    EditText expMonthEditText;
    EditText expYearEditText;

    static final String PACKAGE = "package";

    String mPackageId;

    Call<Void> call1;
    Call<DataAuthToken> call2;

    @Inject
    Retrofit retrofit;

    @Override
    public void onStop() {
        if(call1 != null && !call1.isCanceled()){
            call1.cancel();
            call1 = null;
        }
        if(call2 != null && !call2.isCanceled()){
            call2.cancel();
            call2 = null;
        }
        finishProgress();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.info_subscribe_stripe_layout);

        ((MyApplication) getApplication()).getNetComponent().inject(this);

        if(savedInstanceState != null){
            isRenew = savedInstanceState.getBoolean(InfoMainActivity.RENEW);
            mPackageId = savedInstanceState.getString(PACKAGE, null);

        } else {
            isRenew = getIntent().getBooleanExtra(InfoMainActivity.RENEW, false);
            mPackageId = getIntent().getStringExtra(PACKAGE);
        }

        //mPackageId can be null if workflow is from InfoConfirmActivity
        // and NOT null if from InfoUpgradeChooseCardActivity

        TextView headerText = (TextView) findViewById(R.id.textViewHead);
        headerText.setText(getString(R.string.credit_card_header));

        TextView textViewStripeLable = (TextView) findViewById(R.id.textView18);
        CharSequence text1 = Global.bold(Global.textSize(getString(R.string.upgrade_choose_card_powered_by), 25));
        CharSequence text2 = Global.bold(Global.textSize(STRIPE, 35));
        CharSequence text3 = TextUtils.concat(text1, "  ", text2);
        textViewStripeLable.setText(text3);

        cvcEditText = (EditText)findViewById(R.id.cvcEditText);
        expMonthEditText = (EditText)findViewById(R.id.expMonthEditText);
        expYearEditText = (EditText)findViewById(R.id.expYearEditText);

        saveButton = (Button)findViewById(R.id.button_create_info);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //disable resending requests while in progress
                setControlsEnabled(false);
                //hide keyBoard
                Global.hideKeyBoard(InfoSubscribeStripeActivity.this, view);
                saveCreditCard();
            }
        });

        cardNumberEditText = (EditText)findViewById(R.id.editTextCardNumber);
        setCardNumberChangeListener();

        View logo = findViewById(R.id.imageViewLogo);
        if(logo != null){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToMainActivity();
                }
            });
        }

    }

    private void setCardNumberChangeListener() {
        cardNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String originalString = s.toString();
                // Check if we are already updating, to avoid infinite loop.
                // Also check if the string is already in a valid format.
                if (isUpdating || originalString.matches(regexp)) {
                    return;
                }
                // Set flag to indicate that we are updating the Editable.
                isUpdating = true;

                // First all whitespaces must be removed. Find the index of all whitespace.
                LinkedList<Integer> spaceIndices = new LinkedList <Integer>();
                for (int index = originalString.indexOf(SPACE_CHAR); index >= 0; index = originalString.indexOf(SPACE_CHAR, index + 1)) {
                    spaceIndices.offerLast(index);
                }

                // Delete the whitespace, starting from the end of the string and working towards the beginning.
                Integer spaceIndex = null;
                while (!spaceIndices.isEmpty()) {
                    spaceIndex = spaceIndices.removeLast();
                    s.delete(spaceIndex, spaceIndex + 1);
                }

                // Loop through the string again and add whitespaces in the correct positions
                for(int i = 0; ((i + 1) * GROUPSIZE + i) < s.length(); i++) {
                    s.insert((i + 1) * GROUPSIZE + i, SPACE_STRING);
                }

                // Finally check that the cursor is not placed before a whitespace.
                // This will happen if, for example, the user deleted the digit '5' in
                // the string: "1234 567".
                // If it is, move it back one step; otherwise it will be impossible to delete
                // further numbers.
                int cursorPos = cardNumberEditText.getSelectionStart();
                if (cursorPos > 0 && s.charAt(cursorPos - 1) == SPACE_CHAR) {
                    cardNumberEditText.setSelection(cursorPos - 1);
                }
                isUpdating = false;
            }
        });
    }

    private void goToMainActivity(){
        Intent intent = new Intent(InfoSubscribeStripeActivity.this, InfoMainActivity.class);
        intent.putExtras(getIntent().getExtras());
        intent.putExtra(InfoMainActivity.RENEW, isRenew);
        startActivity(intent);
        InfoSubscribeStripeActivity.this.finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(InfoMainActivity.RENEW, isRenew);
        super.onSaveInstanceState(outState);
    }

    public void saveCreditCard() {

        Card card = new Card(
        this.cardNumberEditText.getText().toString().replaceAll("\\s+",""),
                getInteger(this.expMonthEditText.getText().toString()),
                getInteger("20"+this.expYearEditText.getText().toString()),
                this.cvcEditText.getText().toString());
        card.setCurrency(null);

        boolean validation = card.validateCard();
        if (validation) {
            startProgress();
            new Stripe().createToken(
                    card,
                    PUBLISHABLE_KEY,
                    new TokenCallback() {
                        public void onSuccess(Token token) {

                            sendTokenToServer(token);

                        }
                        public void onError(Exception error) {
                            setControlsEnabled(true);

                            handleError(error.getLocalizedMessage());
                            finishProgress();
                        }
                    });
        } else if (!card.validateNumber()) {
            handleError(getString(R.string.validationErrors_card_number_invalid));
        } else if (!card.validateExpiryDate()) {
            handleError(getString(R.string.validationErrors_expiration_date_invalid));
        } else if (!card.validateCVC()) {
            handleError(getString(R.string.validationErrors_CVC_code_invalid));
        } else {
            handleError(getString(R.string.validationErrors_card_details_invalid));
        }
        setControlsEnabled(true);
    }
    private void setControlsEnabled(boolean enableType){
        saveButton.setEnabled(enableType);
        cardNumberEditText.setEnabled(enableType);
        cvcEditText.setEnabled(enableType);
        expMonthEditText.setEnabled(enableType);
        expYearEditText.setEnabled(enableType);
    }

    private Integer getInteger(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void startProgress() {
        GlobalProgressDialog.show(this, getString(R.string.spinner_processing_your_payment));
    }

    private void finishProgress() {
        GlobalProgressDialog.dismiss();
    }

    private void handleError(String error) {
        DialogFragment fragment = ErrorDialogFragment.newInstance(R.string.validationErrors, error);
        fragment.show(getFragmentManager(), "error");
    }

    private void sendTokenToServer(Token token){

        //check for Internet connection
        if (InternetConnection.isConnected(InfoSubscribeStripeActivity.this)) {

            Map<String, String> mapOfData = new HashMap<>();
            if(isRenew){

                /**
                 *
                 * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                 *
                 */

            } else {

                /**
                 *
                 * DELETED TO HIDE SENSITIVE DETAILS AND/OR SIMPLIFY READING
                 *
                 */

            }

        } else {
            finishProgress();
            setControlsEnabled(true);
            //no internet connection
            Constant.toastIt(InfoSubscribeStripeActivity.this, getString(R.string.no_internet_connection));
        }
    }

}
