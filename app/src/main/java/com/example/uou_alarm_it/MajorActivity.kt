package com.example.uou_alarm_it

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uou_alarm_it.databinding.ActivityMajorBinding

data class MajorItem(val title: String, val imageResId: Int)

class MajorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMajorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMajorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // GridLayout에 아이템들을 동적으로 추가
        val gridLayout = binding.majorFeGl
        val items = getMajorItems() // 예시 데이터 리스트
        // 열 개수가 2라고 가정
        val columnCount = 2
        val totalRows = (items.size + columnCount - 1) / columnCount

        // 32dp를 픽셀로 변환
        val marginPx = (32 * resources.displayMetrics.density + 0.5f).toInt()

        for ((index, item) in items.withIndex()) {
            // grid_item.xml 레이아웃 inflate
            val itemView = layoutInflater.inflate(R.layout.item_major, gridLayout, false)
            val imageView = itemView.findViewById<ImageView>(R.id.item_major_img)
            val textView = itemView.findViewById<TextView>(R.id.item_major_text)

            // 데이터 적용
            textView.text = item.title
            imageView.setImageResource(item.imageResId)

            // 각 행의 마지막 행이 아니라면 하단 margin을 marginPx로 설정
            val rowIndex = index / columnCount
            val params = itemView.layoutParams as GridLayout.LayoutParams
            params.bottomMargin = if (rowIndex < totalRows - 1) marginPx else 0
            itemView.layoutParams = params

            // 아이템 클릭 시 해당 아이템의 텍스트 데이터를 NoticeActivity로 전달
            itemView.setOnClickListener {
                val resultIntent = Intent().apply {
                    putExtra("selectedItem", item.title)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            gridLayout.addView(itemView)
        }

        // Back 버튼 클릭 시 NoticeActivity로 복귀 (결과 없이)
        binding.majorBackBtnIv.setOnClickListener {
            finish()
        }
    }

    private fun getMajorItems(): List<MajorItem> {
        return listOf(
            MajorItem("ICT융합학부", R.drawable.logo_splash),
            MajorItem("IT융합전공", R.drawable.logo_splash),
            MajorItem("AI융합전공", R.drawable.logo_splash)
            // 필요에 따라 추가
        )
    }
}