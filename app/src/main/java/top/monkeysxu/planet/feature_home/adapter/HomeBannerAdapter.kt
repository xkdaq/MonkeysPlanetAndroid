package top.monkeysxu.planet.feature_home.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.youth.banner.adapter.BannerAdapter
import top.monkeysxu.planet.feature_home.model.Banner

class HomeBannerAdapter(bannerList: List<Banner>) : BannerAdapter<Banner, HomeBannerAdapter.BannerViewHolder>(bannerList) {

    var onBannerClick: ((Banner) -> Unit)? = null

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return BannerViewHolder(imageView)
    }

    override fun onBindView(holder: BannerViewHolder, data: Banner, position: Int, size: Int) {
        holder.imageView.load(data.imageUrl) {
            crossfade(true)
        }
        holder.imageView.setOnClickListener {
            onBannerClick?.invoke(data)
        }
    }

    class BannerViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}
