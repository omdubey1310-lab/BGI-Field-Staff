package com.pd.lokawaaz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvName: TextView
    private lateinit var tvDept: TextView
    private lateinit var tvDesignation: TextView
    private lateinit var tvResolved: TextView
    private lateinit var tvVerification: TextView
    private lateinit var tvWorkerType: TextView
    private lateinit var tvDutyStatus: TextView

    private lateinit var btnLogout: Button

    private lateinit var imgProfile: ShapeableImageView

    private var profileListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Firebase init
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // UI binding
        tvName =
            findViewById(R.id.tvName)

        tvDept =
            findViewById(R.id.tvDepartment)

        tvDesignation =
            findViewById(R.id.tvDesignation)

        tvResolved =
            findViewById(R.id.tvResolved)

        tvVerification =
            findViewById(R.id.tvVerification)

        tvWorkerType =
            findViewById(R.id.tvWorkerType)

        tvDutyStatus =
            findViewById(R.id.tvDutyStatus)

        btnLogout =
            findViewById(R.id.btnLogout)

        imgProfile =
            findViewById(R.id.imgProfile)

        loadProfileData()

        setupLogout()
    }

    private fun loadProfileData() {

        val uid = auth.currentUser?.uid

        if (uid == null) {

            Toast.makeText(
                this,
                "User not logged in",
                Toast.LENGTH_SHORT
            ).show()

            finish()

            return
        }

        profileListener =
            db.collection("field_staff")
                .document(uid)

                .addSnapshotListener { doc, error ->

                    if (error != null) {

                        Toast.makeText(
                            this,
                            "Error loading profile",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@addSnapshotListener
                    }

                    if (doc != null && doc.exists()) {

                        val name =
                            doc.getString("name") ?: "N/A"

                        val dept =
                            doc.getString("department") ?: "N/A"

                        val designation =
                            doc.getString("designation") ?: "N/A"

                        val resolved =
                            doc.getLong("resolvedCount") ?: 0

                        val workerType =
                            doc.getString("workerType")
                                ?: "Municipal Worker"

                        val verification =
                            doc.getString("verificationStatus")
                                ?: "Pending"

                        val duty =
                            doc.getBoolean("duty_status")
                                ?: false

                        val selfieUrl =
                            doc.getString("selfieUrl")

                        // Set Profile Data
                        tvName.text = name

                        tvDept.text =
                            "Department: $dept"

                        tvDesignation.text =
                            "Designation: $designation"

                        tvResolved.text =
                            "Resolved Tasks: $resolved"

                        tvWorkerType.text =
                            "Type: $workerType"

                        tvVerification.text =
                            if (verification == "Approved")
                                "Verified Worker ✅"
                            else
                                "Verification Pending ⏳"

                        tvDutyStatus.text =
                            if (duty)
                                "Duty Status: ON"
                            else
                                "Duty Status: OFF"

                        // Load Profile Image
                        Glide.with(this)
                            .load(selfieUrl)
                            .placeholder(
                                R.drawable.ic_launcher_foreground
                            )
                            .into(imgProfile)

                    } else {

                        Toast.makeText(
                            this,
                            "Profile not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }

    private fun setupLogout() {

        btnLogout.setOnClickListener {

            auth.signOut()

            val intent =
                Intent(
                    this,
                    LoginActivity::class.java
                )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        profileListener?.remove()
    }
}