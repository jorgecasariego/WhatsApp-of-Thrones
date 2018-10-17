package mew.jorgecasariego.whatsappofthrones.registerLogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import mew.jorgecasariego.whatsappofthrones.R
import mew.jorgecasariego.whatsappofthrones.messages.LatestMessagesActivity
import mew.jorgecasariego.whatsappofthrones.models.User
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        select_photo_button_register.setOnClickListener {
            // Mostramos las fotos del telefono
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        register_button.setOnClickListener {
            performRegister()
        }

        already_have_account_textview.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // Location where the image is located
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selected_photo_imageview_register.setImageBitmap(bitmap)
            select_photo_button_register.alpha = 0f
        }

    }

    private fun performRegister() {
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter text in email/Password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) return@addOnCompleteListener

                    uploadImageToFirebaseStorage()


                }
                .addOnFailureListener {
                    Log.e("RegisterActivity", "Failed to create user: ${it.message}")
                    Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
                }
    }

    private fun uploadImageToFirebaseStorage() {
        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Successfully upload image ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("RegisterActivity", "Successfully upload image ${it.toString()}")
                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener {

                }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl)
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Finally we saved our user to Firebase Database")
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {

                }
    }
}

