package com.inmoglass.launcher.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inmoglass.launcher.R
import com.inmoglass.launcher.adapter.Adapter
import kotlinx.android.synthetic.main.activity_horizon_recyclerview.*


/**
 * 封装横向滑动卡片式列表
 * @author Administrator
 * @date 2021-12-08
 */
class HorizonRecyclerviewActivity : AppCompatActivity() {

    var recyclerView: RecyclerView? = null

    // Array list for recycler view data source
    var source: ArrayList<String>? = null

    // Layout Manager
    var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null

    // adapter class object
    var adapter: Adapter? = null

    // Linear Layout Manager
    var horizontalLayout: LinearLayoutManager? = null

    var childView: View? = null
    var recyclerViewItemPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horizon_recyclerview)

//        recyclerViewLayoutManager = LinearLayoutManager(applicationContext)
//        recyclerview.layoutManager = recyclerViewLayoutManager
        addItemsToRecyclerViewArrayList()
        adapter = Adapter(source)
        horizontalLayout = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerview.layoutManager = horizontalLayout
        recyclerview.adapter = adapter
    }

    private fun addItemsToRecyclerViewArrayList() {
        source = ArrayList()
        source!!.add("sdadsad")
        source!!.add("sdadsad1111")
        source!!.add("sdadsad2222")
        source!!.add("sdadsad3333")
        source!!.add("sdadsad4444")
        source!!.add("sdadsad5555")
        source!!.add("sdadsad5555")
        source!!.add("sdadsad5555")
        source!!.add("sdadsad5555")
        source!!.add("sdadsad5555")
        source!!.add("sdadsad5555")
        source!!.add("sdadsad5555")
        source!!.add("sdadsad5555")
        source!!.add("sdadsad5555")
        source!!.add("sdadsad5555")
        source!!.add("sdadsad5555")
        source!!.add("sdadsad5555")
    }
}