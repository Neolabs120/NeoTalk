package neotalk.neolabs.com.neotalk;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import neotalk.neolabs.com.neotalk.model.UserModel;

public class SignupActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;
    private EditText email;
    private EditText name;
    private EditText password;
    private Button signup;
    private FirebaseAuth mAuth;
    private ImageView profile;
    private Uri imageUri;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        profile = (ImageView)findViewById(R.id.signupActivity_imageView_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        email = (EditText)findViewById(R.id.signupActivity_edittext_email);
        name = (EditText)findViewById(R.id.signupActivity_edittext_name);
        password = (EditText) findViewById(R.id.signupActivity_edittext_password);
        signup = (Button) findViewById(R.id.signupActivity_Button_signup);

        mAuth = FirebaseAuth.getInstance();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //빈칸이 있으면 가입이 되지 않는다.
                if(email.getText().toString() == null || name.getText().toString() == null || password.getText().toString() == null || imageUri == null) {
                    return;
                }

                //성공할경우
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                  uid = task.getResult().getUser().getUid();
                                  FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                      @Override
                                      public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                          @SuppressWarnings("VisibleForTests")
                                          String imageUrl = task.getResult().getDownloadUrl().toString();

                                          //유저이름, 고유 UID, 프로파일 링크를 넣어준다.
                                          UserModel userModel = new UserModel();
                                          userModel.userName = name.getText().toString();
                                          userModel.profileImageUrl = imageUrl;
                                          userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                          FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                              @Override
                                              public void onSuccess(Void aVoid) {
                                                  //끝내준다.
                                                  SignupActivity.this.finish();
                                              }
                                          });
                                      }
                                  });
                            }
                        });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {
            profile.setImageURI(data.getData()); //가운데 이미지뷰 변경.
            imageUri = data.getData(); //이미지 경로 원본
        }
    }

}
