package com.ismail_s.jtime.android.activity

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.ismail_s.jtime.android.MasjidPojo
import com.ismail_s.jtime.android.R

/**
 * [RecyclerView.Adapter] that can display a [MasjidPojo].
 */
class MyItemRecyclerViewAdapter(private val mValues: List<MasjidPojo>, private val mainActivity: MainActivity) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val masjidPojo = mValues[position]
        holder.mItem = masjidPojo
        holder.mContentView.text = masjidPojo.name
        holder.mView.setOnClickListener {
            val text = masjidPojo.name
            val toast = Toast.makeText(mainActivity.applicationContext, text, Toast.LENGTH_SHORT)
            toast.show()
            mainActivity.switchToMasjidsFragment(masjidPojo.id!!, masjidPojo.name!!)
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mContentView: TextView
        var mItem: MasjidPojo = MasjidPojo()

        init {
            mContentView = mView.findViewById(R.id.content) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
