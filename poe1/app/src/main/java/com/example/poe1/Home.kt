package com.example.poe1

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Home : AppCompatActivity() {
    // Variables
    private lateinit var spinner: Spinner
    private lateinit var etName: EditText
    private lateinit var etdesc: EditText
    private lateinit var btnSaveEntry: Button
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var btnStartTime: Button
    private lateinit var btnEndTime: Button
    private lateinit var btnAdvCam: Button
    private lateinit var btnViewRecs: Button
    private lateinit var database: DatabaseReference

    // Globals
    private var startDate: Date? = null
    private var startTime: Date? = null
    private var endDate: Date? = null
    private var endTime: Date? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        etName = findViewById(R.id.edName)
        etdesc = findViewById(R.id.edDesc)
        spinner = findViewById(R.id.spinner)
        btnSaveEntry = findViewById(R.id.btnSaveEntry)
        btnStartDate = findViewById(R.id.btnStartDate)
        btnStartTime = findViewById(R.id.btnStartTime)
        btnEndDate = findViewById(R.id.btnEndDate)
        btnEndTime = findViewById(R.id.btnEndTime)
        btnAdvCam = findViewById(R.id.btnAdvCamera)
        btnViewRecs = findViewById(R.id.btnRead)
        database = FirebaseDatabase.getInstance().reference

        val btnGoal: Button = findViewById(R.id.btnGoal)

        // Set OnClickListener for btnGoal
        btnGoal.setOnClickListener {
            // Start the goals activity
            startActivity(Intent(this, goals::class.java))
        }

        val btnExit: Button = findViewById(R.id.btnExit)

        btnExit.setOnClickListener {
            // Exit the entire application and shut it down
            finishAffinity()
        }

        // Spinner
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_items,
            android.R.layout.simple_spinner_dropdown_item
        )
        spinner.adapter = spinnerAdapter

        // Set listeners for date and time pickers
        btnStartDate.setOnClickListener { showDatePicker(startDateListener) }
        btnStartTime.setOnClickListener { showTimePicker(startTimeListener) }
        btnEndDate.setOnClickListener { showDatePicker(endDateListener) }
        btnEndTime.setOnClickListener { showTimePicker(endTimeListener) }

        // Firebase button
        btnSaveEntry.setOnClickListener {
            val selectedItem = spinner.selectedItem as String
            val taskName = etName.text.toString()
            val taskDesc = etdesc.text.toString()
            if (taskName.isEmpty()) {
                etName.error = "Please enter timesheet name"
                return@setOnClickListener
            }
            if (taskDesc.isEmpty()) {
                etdesc.error = "Enter a timesheet description"
                return@setOnClickListener
            }
            saveToFirebase(selectedItem, taskName, taskDesc)
        }

        btnAdvCam.setOnClickListener {
            val intentCam = Intent(this, Camera::class.java)
            startActivity(intentCam)
        }

        // Call view records method
        btnViewRecs.setOnClickListener {
            fetchAndDisplay()
        }
    }

    // Method to pick the date
    private fun showDatePicker(dateSetListener: DatePickerDialog.OnDateSetListener) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(this, dateSetListener, year, month, day)
        datePickerDialog.show()
    }

    private fun showTimePicker(timeSetListener: TimePickerDialog.OnTimeSetListener) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this, timeSetListener, hour, minute, true)
        timePickerDialog.show()
    }

    private val startDateListener =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            startDate = selectedCalendar.time
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectedDateString = dateFormat.format(startDate!!)
            btnStartDate.text = selectedDateString
        }

    private val startTimeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.time = startDate
        selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
        selectedCalendar.set(Calendar.MINUTE, minute)
        startTime = selectedCalendar.time

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val selectedTimeString = timeFormat.format(startTime!!)
        btnStartTime.text = selectedTimeString
    }

    private val endDateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.set(year, month, dayOfMonth)
        endDate = selectedCalendar.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateString = dateFormat.format(endDate!!)
        btnEndDate.text = selectedDateString
    }

    private val endTimeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.time = endDate
        selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
        selectedCalendar.set(Calendar.MINUTE, minute)
        endTime = selectedCalendar.time

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val selectedTimeString = timeFormat.format(endTime!!)
        btnEndTime.text = selectedTimeString
    }

    private fun saveToFirebase(selectedItem: String, taskName: String, taskDesc: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val startDateString = btnStartDate.text.toString()
        val startTimeString = btnStartTime.text.toString()
        val endDateString = btnEndDate.text.toString()
        val endTimeString = btnEndTime.text.toString()

        val startDate = dateFormat.parse(startDateString)
        val endDate = dateFormat.parse(endDateString)
        val startTime = timeFormat.parse(startTimeString)
        val endTime = timeFormat.parse(endTimeString)

        val totalTimeInMillis = endDate.time - startDate.time + endTime.time - startTime.time
        val totalMinutes = totalTimeInMillis / (1000 * 60)
        val totalHours = totalMinutes / 60
        val minutesRemaining = totalMinutes % 60
        val totalTimeString = String.format(
            Locale.getDefault(),
            "%02d:%02d", totalHours, minutesRemaining
        )

        val key = database.child("items").push().key
        if (key != null) {
            val task = TaskModel(
                taskName, taskDesc, startDateString, startTimeString,
                endDateString, endTimeString, totalTimeString, selectedItem
            )
            database.child("items").child(key).setValue(task)
                .addOnSuccessListener {
                    Toast.makeText(this, "Timesheet entry saved to database", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener { err ->
                    Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchAndDisplay() {
        database.child("items").get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val records = ArrayList<String>()
                    dataSnapshot.children.forEach { snapshot ->
                        val task = snapshot.getValue(TaskModel::class.java)
                        task?.let {
                            val recordString = StringBuilder()
                            recordString.append("Name: ${it.taskName}\n")
                            recordString.append("Description: ${it.taskDesc}\n")
                            recordString.append("Category: ${it.selectedItem}\n")
                            recordString.append("Start Date: ${it.startDateString}\n")
                            recordString.append("Start Time: ${it.startTimeString}\n")
                            recordString.append("End Date: ${it.endDateString}\n")
                            recordString.append("End Time: ${it.endTimeString}\n")
                            recordString.append("Total Time: ${it.totalTimeString}\n")
                            records.add(recordString.toString())
                        }
                    }
                    displayDialog(records)
                } else {
                    Toast.makeText(this, "No records found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayDialog(record: ArrayList<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Database Records")

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, record)

        builder.setAdapter(adapter, null)

        builder.setPositiveButton("Ok", null)
        builder.show()
    }


}

data class TaskModel(
    var taskName: String? = null,
    var taskDesc: String? = null,
    var startDateString: String? = null,
    var startTimeString: String? = null,
    var endDateString: String? = null,
    var endTimeString: String? = null,
    var totalTimeString: String? = null,
    var selectedItem: String? = null
)