package com.pandaapps.abmsstudies

import com.pandaapps.abmsstudies.sell.model.ModelCategory

interface RvListenerCategory {

    fun onCategoryClick(modelCategory: ModelCategory)
}