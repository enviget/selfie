package com.example.selifeproject.adapters

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.selifeproject.R
import kotlinx.android.synthetic.main.adapter_photos_rows.view.*

class AdapterPhotos(var mContext: Context) : RecyclerView.Adapter<AdapterPhotos.MyViewHolder>(){

    var mPhotos = ArrayList<Uri>()
    inner class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bind(uri:Uri){
            Glide.with(mContext.applicationContext).load(uri).into(itemView.image_view_adapter_photo)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(mContext).inflate(R.layout.adapter_photos_rows, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPhotos.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(mPhotos[position])
    }

    fun setData(photos : ArrayList<Uri>){
        mPhotos = photos
        notifyDataSetChanged()
    }
}