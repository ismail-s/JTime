package com.ismail_s.jtime.android

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ismail_s.jtime.android.pojo.MasjidPojo
import org.jetbrains.anko.find

/**
 * [RecyclerView.Adapter] that can display a [MasjidPojo].
 */
class MasjidRecyclerViewAdapter(private val mValues: List<MasjidPojo>, private val mainActivity: MainActivity) : RecyclerView.Adapter<MasjidRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val masjidPojo = mValues[position]
        holder.mItem = masjidPojo
        holder.mNameView.text = masjidPojo.name
        holder.mAddressView.text = masjidPojo.address
        holder.mView.setOnClickListener {
            mainActivity.switchToMasjidsFragment(masjidPojo.id!!, masjidPojo.name!!)
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mNameView: TextView = mView.find<TextView>(R.id.content)
        val mAddressView: TextView = mView.find<TextView>(R.id.address)
        var mItem: MasjidPojo = MasjidPojo()

        override fun toString(): String {
            return "${super.toString()} '${mNameView.text}' ${mAddressView.text}'"
        }
    }
}
