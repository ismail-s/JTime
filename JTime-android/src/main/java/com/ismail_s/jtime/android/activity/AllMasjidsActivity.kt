package com.ismail_s.jtime.android.activity

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.ismail_s.jtime.android.R

class AllMasjidsActivity : ListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val masjids = arrayOf("one", "two", "three", "four", "five", "six")
        listAdapter = ArrayAdapter(this, R.layout.list_item, masjids)
        listView.isTextFilterEnabled = true
        listView.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            val text = (view as TextView).text
            val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
            toast.show()
            val intent = Intent(baseContext, MasjidActivity::class.java)
            intent.putExtra(Constants.MASJID_NAME, text)
            startActivity(intent)
        }
    }
}
