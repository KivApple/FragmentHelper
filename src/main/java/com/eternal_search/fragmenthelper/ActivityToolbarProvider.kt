package com.eternal_search.fragmenthelper

import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

interface ActivityToolbarProvider {
	fun getToolbar(): Toolbar?
	
	fun getBottomNavigationView(): BottomNavigationView? = null
	
	fun getBottomNavigationViewOnItemSelectedListener(): BottomNavigationView.OnNavigationItemSelectedListener? = null
}
