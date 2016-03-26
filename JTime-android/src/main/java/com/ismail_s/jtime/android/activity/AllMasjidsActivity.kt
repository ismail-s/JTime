package com.ismail_s.jtime.android.activity

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.ismail_s.jtime.android.MasjidPojo
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import java.util.*

class AllMasjidsActivity : ListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cb = object: RestClient.MasjidsCallback {
            override fun onSuccess(masjids: List<MasjidPojo>) {
                var masjidMap = HashMap<String, Int>()
                for (masjid in masjids) {
                    masjidMap.put(masjid.name!!, masjid.id!!)
                }
                listAdapter = ArrayAdapter(applicationContext, R.layout.list_item, masjidMap.keys.toList())
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

            override fun onError(t: Throwable) {
                Toast.makeText(applicationContext, "Weren't able to get masjids", Toast.LENGTH_LONG)
                        .show()
            }
        }
        RestClient(applicationContext).getMasjids(cb)
    }
}
