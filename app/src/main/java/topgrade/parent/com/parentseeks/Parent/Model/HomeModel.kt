package topgrade.parent.com.parentseeks.Parent.Model

import com.google.gson.annotations.SerializedName

data class HomeModel(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("image")
    val image: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("subtitle")
    val subtitle: String = ""
) 