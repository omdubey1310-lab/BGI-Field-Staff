package com.pd.lokawaaz

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI ELEMENTS
    private lateinit var etName: EditText
    private lateinit var spinnerWorkerType: Spinner
    private lateinit var spinnerDepartment: Spinner

    private lateinit var btnUploadAadhaar: Button
    private lateinit var btnUploadSelfie: Button

    private lateinit var btnCaptureAadhaar: Button
    private lateinit var btnCaptureSelfie: Button

    private lateinit var txtToggleMode: TextView

    private var isRegisterMode = false

    private var aadhaarUri: Uri? = null
    private var selfieUri: Uri? = null

    private var aadhaarCameraUri: Uri? = null
    private var selfieCameraUri: Uri? = null

    // GALLERY PICKER - AADHAAR
    private val aadhaarLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            aadhaarUri = uri

            Toast.makeText(
                this,
                "Aadhaar Uploaded ✅",
                Toast.LENGTH_SHORT
            ).show()
        }

    // GALLERY PICKER - SELFIE
    private val selfieLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            selfieUri = uri

            Toast.makeText(
                this,
                "Selfie Uploaded ✅",
                Toast.LENGTH_SHORT
            ).show()
        }

    // CAMERA - AADHAAR
    private val captureAadhaarLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->

            if (success) {

                aadhaarUri = aadhaarCameraUri

                Toast.makeText(
                    this,
                    "Aadhaar Captured ✅",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // CAMERA - SELFIE
    private val captureSelfieLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->

            if (success) {

                selfieUri = selfieCameraUri

                Toast.makeText(
                    this,
                    "Selfie Captured ✅",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val email =
            findViewById<EditText>(R.id.etEmail)

        val password =
            findViewById<EditText>(R.id.etPassword)

        val btnLogin =
            findViewById<Button>(R.id.btnLogin)

        // NEW VIEWS
        etName =
            findViewById(R.id.etName)

        spinnerWorkerType =
            findViewById(R.id.spinnerWorkerType)

        spinnerDepartment =
            findViewById(R.id.spinnerDepartment)

        btnUploadAadhaar =
            findViewById(R.id.btnUploadAadhaar)

        btnUploadSelfie =
            findViewById(R.id.btnUploadSelfie)

        btnCaptureAadhaar =
            findViewById(R.id.btnCaptureAadhaar)

        btnCaptureSelfie =
            findViewById(R.id.btnCaptureSelfie)

        txtToggleMode =
            findViewById(R.id.txtToggleMode)

        // Worker Type Spinner
        val workerTypes = arrayOf(
            "Municipal Worker",
            "Freelancer"
        )

        spinnerWorkerType.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                workerTypes
            )

        // Department Spinner
        val departments = arrayOf(
            "Roads",
            "Electricity",
            "Sanitation",
            "Water",
            "Housing"
        )

        spinnerDepartment.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                departments
            )

        // Toggle Login/Register
        txtToggleMode.setOnClickListener {

            isRegisterMode = !isRegisterMode

            if (isRegisterMode) {

                etName.visibility = View.VISIBLE

                spinnerWorkerType.visibility =
                    View.VISIBLE

                spinnerDepartment.visibility =
                    View.VISIBLE

                btnUploadAadhaar.visibility =
                    View.VISIBLE

                btnUploadSelfie.visibility =
                    View.VISIBLE

                btnCaptureAadhaar.visibility =
                    View.VISIBLE

                btnCaptureSelfie.visibility =
                    View.VISIBLE

                btnLogin.text = "Register"

                txtToggleMode.text =
                    "Already Registered? Login"

            } else {

                etName.visibility = View.GONE

                spinnerWorkerType.visibility =
                    View.GONE

                spinnerDepartment.visibility =
                    View.GONE

                btnUploadAadhaar.visibility =
                    View.GONE

                btnUploadSelfie.visibility =
                    View.GONE

                btnCaptureAadhaar.visibility =
                    View.GONE

                btnCaptureSelfie.visibility =
                    View.GONE

                btnLogin.text = "Login"

                txtToggleMode.text =
                    "New Worker? Register Here"
            }
        }

        // Upload Aadhaar
        btnUploadAadhaar.setOnClickListener {

            aadhaarLauncher.launch("image/*")
        }

        // Upload Selfie
        btnUploadSelfie.setOnClickListener {

            selfieLauncher.launch("image/*")
        }

        // Capture Aadhaar
        btnCaptureAadhaar.setOnClickListener {

            aadhaarCameraUri =
                createImageUri("aadhaar.jpg")

            captureAadhaarLauncher.launch(
                aadhaarCameraUri
            )
        }

        // Capture Selfie
        btnCaptureSelfie.setOnClickListener {

            selfieCameraUri =
                createImageUri("selfie.jpg")

            captureSelfieLauncher.launch(
                selfieCameraUri
            )
        }

        // LOGIN / REGISTER BUTTON
        btnLogin.setOnClickListener {

            val userEmail =
                email.text.toString().trim()

            val userPass =
                password.text.toString().trim()

            if (
                userEmail.isEmpty() ||
                userPass.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Enter email and password",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // REGISTER MODE
            if (isRegisterMode) {

                val workerName =
                    etName.text.toString().trim()

                if (workerName.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Enter full name",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                if (
                    aadhaarUri == null ||
                    selfieUri == null
                ) {

                    Toast.makeText(
                        this,
                        "Upload Aadhaar and Selfie",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                registerWorker(
                    workerName,
                    userEmail,
                    userPass
                )

            } else {

                // LOGIN
                auth.signInWithEmailAndPassword(
                    userEmail,
                    userPass
                )

                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            val uid =
                                auth.currentUser?.uid

                            if (uid == null) {

                                Toast.makeText(
                                    this,
                                    "Login error",
                                    Toast.LENGTH_SHORT
                                ).show()

                                return@addOnCompleteListener
                            }

                            db.collection("field_staff")
                                .document(uid)
                                .get()

                                .addOnSuccessListener { document ->

                                    val status =
                                        document.getString(
                                            "verificationStatus"
                                        )

                                    if (
                                        status != "Approved"
                                    ) {

                                        Toast.makeText(
                                            this,
                                            "Account under verification",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        auth.signOut()

                                        return@addOnSuccessListener
                                    }

                                    Toast.makeText(
                                        this,
                                        "Login Successful ✅",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    startActivity(
                                        Intent(
                                            this,
                                            DashboardActivity::class.java
                                        )
                                    )

                                    finish()
                                }

                                .addOnFailureListener {

                                    Toast.makeText(
                                        this,
                                        "Firestore Error ❌",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                        } else {

                            Toast.makeText(
                                this,
                                "Invalid credentials ❌",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    // CREATE CAMERA URI
    private fun createImageUri(
        fileName: String
    ): Uri {

        val file =
            File(
                externalCacheDir,
                fileName
            )

        return FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )
    }

    // REGISTER WORKER
    private fun registerWorker(
        workerName: String,
        email: String,
        password: String
    ) {

        Toast.makeText(
            this,
            "Starting Registration...",
            Toast.LENGTH_SHORT
        ).show()

        auth.createUserWithEmailAndPassword(
            email,
            password
        )

            .addOnSuccessListener {

                val uid =
                    auth.currentUser?.uid
                        ?: return@addOnSuccessListener

                Toast.makeText(
                    this,
                    "Auth Created ✅",
                    Toast.LENGTH_SHORT
                ).show()

                uploadImage(
                    aadhaarUri!!,
                    "aadhaar/$uid.jpg"
                ) { aadhaarUrl ->

                    Toast.makeText(
                        this,
                        "Aadhaar Uploaded ✅",
                        Toast.LENGTH_SHORT
                    ).show()

                    uploadImage(
                        selfieUri!!,
                        "selfies/$uid.jpg"
                    ) { selfieUrl ->

                        Toast.makeText(
                            this,
                            "Selfie Uploaded ✅",
                            Toast.LENGTH_SHORT
                        ).show()

                        saveWorkerData(
                            uid,
                            workerName,
                            email,
                            aadhaarUrl,
                            selfieUrl
                        )
                    }
                }
            }

            .addOnFailureListener { e ->

                Toast.makeText(
                    this,
                    "Registration Failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // IMAGE UPLOAD
    private fun uploadImage(
        uri: Uri,
        path: String,
        callback: (String) -> Unit
    ) {

        val ref =
            FirebaseStorage.getInstance()
                .reference
                .child(path)

        ref.putFile(uri)

            .continueWithTask { task ->

                if (!task.isSuccessful) {

                    throw task.exception!!
                }

                ref.downloadUrl
            }

            .addOnSuccessListener {

                callback(it.toString())
            }

            .addOnFailureListener { e ->

                Toast.makeText(
                    this,
                    "Image Upload Failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // SAVE DATA
    private fun saveWorkerData(
        uid: String,
        workerName: String,
        email: String,
        aadhaarUrl: String,
        selfieUrl: String
    ) {

        val data = hashMapOf(

            "fsid" to uid,

            "email" to email,

            "name" to workerName,

            "workerType" to
                    spinnerWorkerType.selectedItem.toString(),

            "department" to
                    spinnerDepartment.selectedItem.toString(),

            "designation" to "Field Worker",

            "aadhaarUrl" to aadhaarUrl,

            "selfieUrl" to selfieUrl,

            "verificationStatus" to "Pending",

            "resolvedCount" to 0,

            "duty_status" to false,

            "assignedTask" to "",

            "location" to GeoPoint(0.0, 0.0)
        )

        db.collection("field_staff")
            .document(uid)
            .set(data)

            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Registration Submitted For Approval ✅",
                    Toast.LENGTH_LONG
                ).show()

                auth.signOut()

                isRegisterMode = false

                recreate()
            }

            .addOnFailureListener { e ->

                Toast.makeText(
                    this,
                    "Firestore Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onStart() {
        super.onStart()

        val currentUser =
            FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {

            db.collection("field_staff")
                .document(currentUser.uid)
                .get()

                .addOnSuccessListener {

                    val status =
                        it.getString(
                            "verificationStatus"
                        )

                    if (status == "Approved") {

                        startActivity(
                            Intent(
                                this,
                                DashboardActivity::class.java
                            )
                        )

                        finish()
                    }
                }
        }
    }
}