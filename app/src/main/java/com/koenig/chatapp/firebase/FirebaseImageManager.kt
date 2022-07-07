package com.koenig.chatapp.firebase

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.ByteArrayOutputStream
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Transformation

object FirebaseImageManager {

    private var storage = FirebaseStorage.getInstance().reference
    var imageUri = MutableLiveData<Uri>()
    private var groupImageUri = MutableLiveData<Uri>()

    fun checkStorageForExistingProfilePic(userid: String) {
        val imageRef = storage.child("photos").child("${userid}.jpg")

        imageRef.metadata.addOnSuccessListener { //File Exists
            imageRef.downloadUrl.addOnCompleteListener { task ->
                imageUri.value = task.result!!
            }
            //File Doesn't Exist
        }.addOnFailureListener {
            imageUri.value = Uri.EMPTY
        }
    }

    fun getUserImage(userid: String,  userImageUri: MutableLiveData<Uri>)
    {
        val imageRef = storage.child("photos").child("${userid}.jpg")

        imageRef.metadata.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener {
                userImageUri.value = it
            }
        }
    }

    fun getUserGroupImages(userIds: ArrayList<String>, userGroupImages: MutableLiveData<List<Uri>>)
    {
        Log.d("Debug", "getUserGroupImages")
        val imageRef = storage.child("photos")

        imageRef.listAll()
            .addOnSuccessListener {

                val localList = ArrayList<Uri>()
                it.items.forEach{ sr ->

                    // All none group images
                    if(sr.toString().length > 64)
                    {
                        val subString = sr.toString().substring(38, 66)
                        if(subString in userIds)
                        {
                            sr.metadata.addOnSuccessListener {
                                sr.downloadUrl.addOnSuccessListener { uri ->
                                    Log.d("DebugItem", uri.toString())
                                    localList.add(uri)
                                }
                            }
                        }
                    }
                }

                userGroupImages.value = localList
        }
    }


    fun uploadImageToFirebase(userid: String, bitmap: Bitmap, updating : Boolean) {

        val imageRef = storage.child("photos").child("${userid}.jpg")
        val baos = ByteArrayOutputStream()
        lateinit var uploadTask: UploadTask

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.metadata.addOnSuccessListener { //File Exists
            if(updating)
            {
                uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        imageUri.value = task.result!!
                        // Can update the userImageUri => task.result
                    }
                }
            }
        }.addOnFailureListener { //File Doesn't Exist
            uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { ut ->
                ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    imageUri.value = task.result!!
                    FirebaseDBManager.updateUserImage(userid, task.result)
                }
            }
        }
    }

    fun uploadGroupImage(groupId: String, bitmap: Bitmap, updating : Boolean)
    {
        val imageRef = storage.child("photos").child("${groupId}.jpg")
        val baos = ByteArrayOutputStream()
        lateinit var uploadTask: UploadTask

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.metadata.addOnSuccessListener { //File Exists

        uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener { ut ->
            ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                groupImageUri.value = task.result!!
                FirebaseGroupChatManager.updateGroupImage(groupId, task.result.toString())
                }
            }
        }.addOnFailureListener{
            uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { ut ->
                ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    groupImageUri.value = task.result!!
                    FirebaseGroupChatManager.updateGroupImage(groupId, task.result.toString())
                }
            }
        }
    }

    fun updateGroupImage(groupId: String, imageUri: Uri?, imageView: ImageView, updating: Boolean)
    {
        Picasso.get().load(imageUri)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {

                    uploadGroupImage(groupId, bitmap!!,updating)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    fun updateUserImage(userid: String, imageUri : Uri?, imageView: ImageView, updating : Boolean) {
        Picasso.get().load(imageUri)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {

                    uploadImageToFirebase(userid, bitmap!!,updating)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {

                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    fun updateDefaultImage(userid: String, resource: Int, imageView: ImageView) {
        Picasso.get().load(resource)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    uploadImageToFirebase(userid, bitmap!!,false)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                }
            })
    }

    private fun customTransformation() : Transformation =
        RoundedTransformationBuilder()
            .borderColor(Color.WHITE)
            .borderWidth(2f)
            .cornerRadius(35f)
            .oval(false)
            .build()

}