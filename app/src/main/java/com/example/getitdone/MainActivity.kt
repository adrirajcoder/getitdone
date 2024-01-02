package com.example.getitdone

import android.app.DatePickerDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.getitdone.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var arrayAdapter : ArrayAdapter<String>
    var itemList = ArrayList<String>()
    var fileHelper = FileHelper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        itemList = fileHelper.readData(this)

        arrayAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, android.R.id.text1, itemList)
        binding.listview.adapter = arrayAdapter

        binding.add.setOnClickListener {
            val item: String = binding.input.text.toString()
            if(item!=""){
                itemList.add(item)
                binding.input.setText("")
                fileHelper.writeData(itemList, applicationContext)
                arrayAdapter.notifyDataSetChanged()
            }
        }

        binding.listview.setOnItemClickListener { adapterView, view, position, l ->
//            val currentItem = itemList[position]
//
//            // Create a dialog for editing the item
//            val editDialog = AlertDialog.Builder(this)
//            editDialog.setTitle("Edit Item")
//
//            val input = EditText(this)
//            input.setText(currentItem)
//
//            editDialog.setView(input)
//
//            editDialog.setPositiveButton("Save") { _, _ ->
//                val editedItem = input.text.toString()
//                itemList[position] = editedItem
//                fileHelper.writeData(itemList, applicationContext)
//                arrayAdapter.notifyDataSetChanged()
//                Toast.makeText(this, "Item edited successfully", Toast.LENGTH_SHORT).show()
//            }
//            editDialog.setCancelable(false)
//
//            editDialog.setNegativeButton("Cancel") { dialog, _ ->
//                dialog.cancel()
//            }
//
//            editDialog.create().show()

//            showDatePickerDialog(position)



            val currentItem = itemList[position]

            val dueDatePattern = """\(Due: (\d{4}-\d{2}-\d{2})\)""".toRegex()
            val existingDueDateMatch = dueDatePattern.find(currentItem)

            val editDialog = AlertDialog.Builder(this)
            editDialog.setTitle("Choose Action")

            val options = arrayOf("Edit Item", "Set Due Date")

            editDialog.setItems(options) { dialog, which ->
                when (which) {
                    0 -> showEditItemDialog(position, currentItem, existingDueDateMatch)
                    1 -> showDatePickerDialog(position, existingDueDateMatch?.value.orEmpty())
                }
                dialog.dismiss()
            }

            editDialog.show()
        }

        binding.listview.setOnItemLongClickListener { _, _, position, _ ->
            var alert = AlertDialog.Builder(this)
            alert.setTitle("Delete")
                .setMessage("Do you want to delete this item from the list?")
                .setCancelable(false)
                .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.cancel()
                })
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialogInterface, i ->
                    itemList.removeAt(position)
                    arrayAdapter.notifyDataSetChanged()
                    fileHelper.writeData(itemList, applicationContext)
                })

            alert.create().show()

            true
        }

        binding.listview.isClickable = false



//        binding.button.setOnClickListener {
//            Toast.makeText(this, "This button is clicked", Toast.LENGTH_LONG).show()
//        }
    }

    private fun showDatePickerDialog(position: Int, existingDueDate: String) {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val currentItem = itemList[position]
                val dueDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                val updatedItem =
                    if (existingDueDate.isNotEmpty()) currentItem.replace(existingDueDate, "(Due: $dueDate)")
                    else "$currentItem (Due: $dueDate)"
                itemList[position] = updatedItem
                fileHelper.writeData(itemList, applicationContext)
                arrayAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Due date updated successfully", Toast.LENGTH_SHORT).show()
            },
            year,
            month,
            day
        )

        datePicker.show()
    }

    private fun showEditItemDialog(position: Int, currentItem: String, existingDueDateMatch: MatchResult?) {
        val editDialog = AlertDialog.Builder(this)
        editDialog.setTitle("Edit Item")

        val input = EditText(this)
        input.setText(currentItem.replace(existingDueDateMatch?.value.orEmpty(), "").trim())

        editDialog.setView(input)

        editDialog.setPositiveButton("Save") { _, _ ->
            val editedItem = input.text.toString()
            val updatedItem = if (existingDueDateMatch != null) {
                "$editedItem ${existingDueDateMatch.value}" // Reattach existing due date
            } else {
                editedItem
            }
            itemList[position] = updatedItem
            fileHelper.writeData(itemList, applicationContext)
            arrayAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Item edited successfully", Toast.LENGTH_SHORT).show()
        }

        editDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        editDialog.show()
    }


}
