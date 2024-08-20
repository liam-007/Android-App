package com.example.poe1

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

class Camera : AppCompatActivity() {
    // variables
    lateinit var imgViewCam: ImageView
    lateinit var btnChoosePic: Button
    lateinit var btnTakePic: Button
    lateinit var btnUploadPic: Button

    // globals
    var filePath: Uri? = null
    val PICK_IMAGE_REQUEST = 22
    val storage = FirebaseStorage.getInstance()
    val storageReference = storage.reference
    val firestore = FirebaseFirestore.getInstance()
    val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        imgViewCam = findViewById(R.id.imgViewCamera2)
        btnChoosePic = findViewById(R.id.btnCam2Choose)
        btnUploadPic = findViewById(R.id.btnCam2UploadImage)
        btnTakePic = findViewById(R.id.btnCam2TakePic)

        // Choose Btn
        btnChoosePic.setOnClickListener {
            selectImage()
        }
        // take pic btn
        btnTakePic.setOnClickListener {
            dispatchTakePicture()
        }
        // upload btn
        btnUploadPic.setOnClickListener {
            uploadImage()
        }

    }// on create ends

    // method to select an image
    fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Image"),
            PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (
            requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null
        ) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imgViewCam.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgViewCam.setImageBitmap(imageBitmap)
            // Convert bitmap to URI and set filePath
            filePath = getImageUri(applicationContext, imageBitmap)
        }
    }


    private fun saveImageToFirebase(imageBitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val imageName = UUID.randomUUID().toString() + ".jpg"
        val imageRef = storageReference.child("images/$imageName")
        imageRef.putBytes(data)
            .addOnSuccessListener { uri ->
                val imageURL = uri.toString()
                saveImageToFirestore(imageURL)
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to upload image to Firebase Storage",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveImageToFirestore(imageURL: String) {
        // Save image URL to Firestore
        val docRef = firestore.collection("images").document()
        val imageId = docRef.id
        docRef.set(mapOf("imageURL" to imageURL))
            .addOnSuccessListener {
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to save image URL to Firestore", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun dispatchTakePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)
                ?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val imageTitle = "UserPhoto"
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            imageTitle,
            null
        )
        return Uri.parse(path)
    }


    // upload to firestore
    fun uploadImage() {
        filePath?.let { filePath ->
            if (contentResolver.openInputStream(filePath) != null) {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading...")
                progressDialog.show()

                val ref = storageReference.child("images/${UUID.randomUUID()}.jpg")
                ref.putFile(filePath)
                    .addOnSuccessListener { taskSnapshot ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Image Uploaded Successfully", Toast.LENGTH_SHORT)
                            .show()

                        // Get the download URL from Firebase Storage
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            // Save image URL to Firestore
                            saveImageToFirestore(uri.toString())
                            // Save image URL to Realtime Database
                            saveImageToRealtimeDatabase(uri.toString())
                        }.addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                "Failed to get image download URL",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToRealtimeDatabase(imageURL: String) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("images")

        // Creating a unique key for the image
        val imageId = ref.push().key ?: ""

        // Create a HashMap to hold the image data
        val imageData = HashMap<String, Any>()
        imageData["imageURL"] = imageURL

        // Set the image data in the Realtime Database under the unique key
        ref.child(imageId).setValue(imageData)
    }

}