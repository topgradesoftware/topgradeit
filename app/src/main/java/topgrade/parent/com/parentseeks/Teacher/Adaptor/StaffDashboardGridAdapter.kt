package topgrade.parent.com.parentseeks.Teacher.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import topgrade.parent.com.parentseeks.R
import topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard

class StaffDashboardGridAdapter(
    private val context: Context,
    private val cards: List<StaffDashboardCard>,
    private val onCardClick: OnCardClickListener
) : RecyclerView.Adapter<StaffDashboardGridAdapter.CardViewHolder>() {

    interface OnCardClickListener {
        fun onCardClick(card: StaffDashboardCard)
    }

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.card_icon)
        val title: TextView = itemView.findViewById(R.id.card_title)
        val subtitle: TextView = itemView.findViewById(R.id.card_subtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_staff_dashboard_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        
        holder.icon.setImageResource(card.iconResId)
        holder.title.text = card.title
        holder.subtitle.text = card.subtitle
        
        holder.itemView.setOnClickListener {
            onCardClick.onCardClick(card)
        }
    }

    override fun getItemCount(): Int = cards.size
}
