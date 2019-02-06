package com.coal.projects.simpleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

public class LoginActivity extends AppCompatActivity {

    private BehaviorSubject<Boolean> isLoggedIn = BehaviorSubject.create();
    private Disposable isLoggedInDisposable;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private ConstraintLayout content;
    private int RC_SIGN_IN = 333;
    private String TAG = "@@@";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        setTitle("Sign in");
        content = findViewById(com.coal.projects.chat.R.id.content);
        findViewById(R.id.sign_in_button).setOnClickListener(v -> signInWithGoogle());

        mAuth = FirebaseAuth.getInstance();
    }

    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onPause() {
        if (isLoggedInDisposable != null && !isLoggedInDisposable.isDisposed())
            isLoggedInDisposable.dispose();
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        isLoggedInDisposable = isLoggedIn
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(isLoggedIn -> {
                    if (isLoggedIn) {
                        goNext();
                    } else {
                        Toast.makeText(this, "Something goes wrong. Closing app...", Toast.LENGTH_LONG).show();
                    }
                });

        updateUI(mAuth.getCurrentUser());
    }

    private void goNext() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.e(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "signInWithCredential: SUCCESS");
                        goNext();
                    } else {
                        Toast.makeText(this, "Sign in with Credential failed", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "signInWithCredential: FAIL");
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user == null) {
            content.setVisibility(View.VISIBLE);
        } else {
            isLoggedIn.onNext(true);
            content.setVisibility(View.GONE);
        }
    }
}
