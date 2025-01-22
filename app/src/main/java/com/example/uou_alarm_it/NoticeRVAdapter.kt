package com.example.uou_alarm_it

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.uou_alarm_it.databinding.ItemNoticeBinding

class NoticeRVAdapter() : RecyclerView.Adapter<NoticeRVAdapter.ViewHolder>() {

    var noticeList : ArrayList<Notice> = arrayListOf(
        Notice(true, "★재학생(편입, 복전) 필독★ 졸업작품 이수 관련 안내", 9, "2024-09-05"),
        Notice(true, "2024-2학기 빛냄장학 신청 안내", 10, "2024-09-05"),
        Notice(true, "2024-2학기 역량개발장학 신청 안내", 11, "2024-09-05"),
        Notice(false, "개인단위 수준별 영어교육 시행에 따른 기초학력...", 388, "2025-01-06"),
        Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 387, "2025-01-03"),
        Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 386, "2025-01-03"),
        Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 385, "2025-01-03"),
        Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 384, "2025-01-03"),
        Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 383, "2025-01-03"),
        Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 382, "2025-01-03"),
        Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 381, "2025-01-03"),
        Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 380, "2025-01-03"),
        Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 379, "2025-01-03"),
        Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 378, "2025-01-03"),
        Notice(false, "울산·경남지역혁신플랫폼 미래모빌리티 기술 관...", 377, "2025-01-03"),
    )

    var bookmarkList : ArrayList<Notice> = arrayListOf()

    interface MyClickListener{
        fun onItemClick(notice: Notice)
        fun onBookmarkClick(notice: Notice)
    }

    private lateinit var myClickListener: MyClickListener

    fun setMyClickListener(itemClickListener:MyClickListener){
        myClickListener = itemClickListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = noticeList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(noticeList[position])
        holder.itemView.setOnClickListener {
            myClickListener.onItemClick(noticeList[position])
        }
    }

    inner class ViewHolder(private val binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(notice : Notice) {
            binding.itemNoticeTitle.text = notice.title
            binding.itemNoticeDate.text = notice.date
            if(notice.isImportant) {
                binding.itemNoticeNumber.text = "공지"
                binding.itemNotice.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.green_20)
                )
            }
            else {
                binding.itemNoticeNumber.text = notice.number.toString()
                binding.itemNotice.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.transparent)
                )
            }

            if(notice in bookmarkList) {
                binding.itemNoticeBookmark.setImageResource(R.drawable.bookmark_on)
            }
            else{
                binding.itemNoticeBookmark.setImageResource(R.drawable.bookmark_off)
            }

            binding.itemNoticeBookmark.setOnClickListener {
                myClickListener.onBookmarkClick(notice)
            }
        }
    }

}