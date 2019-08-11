package com.eternal_search.fragmenthelper

import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

open class ActivityFragmentHelper(
	private val activity: FragmentActivity,
	private val containerId: Int
): FragmentManager.FragmentLifecycleCallbacks() {
	private val _resumedFragments = mutableSetOf<Fragment>()
	val resumedFragments: Set<Fragment> get() = _resumedFragments
	private var savedActivityTitle: String? = null
	private var savedActionBarContentInsetsStart: Int? = null
	private var savedActionBarContentInsetsEnd: Int? = null
	private var currentActionBarCustomViewProvider: ActionBarCustomViewProvider? = null
	private var currentActionBarCustomTitleProvider: ActionBarTitleProvider? = null
	var customAnimations: CustomAnimations? = null
	
	init {
		activity.supportFragmentManager.registerFragmentLifecycleCallbacks(this, false)
	}

	override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
		_resumedFragments.add(f)
		onResumedFragmentsChanged()
	}

	override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
		_resumedFragments.remove(f)
		onResumedFragmentsChanged()
	}

	open fun onResumedFragmentsChanged() {
		val actionBarCustomViewProvider = resumedFragments.firstOrNull {
			it is ActionBarCustomViewProvider
		} as? ActionBarCustomViewProvider
		val actionBarTitleProvider = resumedFragments.firstOrNull {
			it is ActionBarTitleProvider
		} as? ActionBarTitleProvider
		val supportActionBar = (activity as? AppCompatActivity)?.supportActionBar
		val toolbar = (activity as? ActivityToolbarProvider)?.getToolbar()
		val bottomNavigationView = (activity as? ActivityToolbarProvider)?.getBottomNavigationView()
		if (actionBarCustomViewProvider != currentActionBarCustomViewProvider) {
			currentActionBarCustomViewProvider = actionBarCustomViewProvider
			val actionBarCustomView = actionBarCustomViewProvider?.onCreateActionBarCustomView(
				activity.layoutInflater, LinearLayout(supportActionBar?.themedContext)
			)
			supportActionBar?.setCustomView(
				actionBarCustomView,
				(actionBarCustomView?.layoutParams as? LinearLayout.LayoutParams)?.let {
					ActionBar.LayoutParams(it.width, it.height, it.gravity)
				}
			)
			if (actionBarCustomView != null) {
				if (savedActionBarContentInsetsStart == null) {
					savedActionBarContentInsetsStart = toolbar?.contentInsetStart
				}
				if (savedActionBarContentInsetsEnd == null) {
					savedActionBarContentInsetsEnd = toolbar?.contentInsetEnd
				}
				toolbar?.setContentInsetsAbsolute(0, 0)
			} else {
				if (savedActionBarContentInsetsStart != null &&
					savedActionBarContentInsetsEnd != null) {
					toolbar?.setContentInsetsAbsolute(
						savedActionBarContentInsetsStart!!,
						savedActionBarContentInsetsEnd!!
					)
				}
			}
			supportActionBar?.setDisplayShowTitleEnabled(actionBarCustomView == null)
			supportActionBar?.setDisplayShowCustomEnabled(actionBarCustomView != null)
		}
		if (actionBarTitleProvider != currentActionBarCustomTitleProvider) {
			currentActionBarCustomTitleProvider = actionBarTitleProvider
			if (actionBarTitleProvider != null) {
				if (savedActivityTitle == null) {
					savedActivityTitle = supportActionBar?.title?.toString()
				}
				supportActionBar?.title = actionBarTitleProvider.title
				supportActionBar?.subtitle = actionBarTitleProvider.subtitle
			} else {
				supportActionBar?.title = savedActivityTitle
				supportActionBar?.subtitle = null
			}
		}
		if (bottomNavigationView != null) {
			val navigationId = resumedFragments.mapNotNull {
				it::class.java.getAnnotation(NavigationId::class.java)?.id
			}.firstOrNull()
			if (navigationId != null) {
				bottomNavigationView.setOnNavigationItemSelectedListener(null)
				bottomNavigationView.selectedItemId = navigationId
				bottomNavigationView.setOnNavigationItemSelectedListener(
					(activity as? ActivityToolbarProvider)?.getBottomNavigationViewOnItemSelectedListener()
				)
				bottomNavigationView.visibility = View.VISIBLE
				supportActionBar?.setDisplayHomeAsUpEnabled(false)
			} else {
				bottomNavigationView.visibility = View.GONE
				supportActionBar?.setDisplayHomeAsUpEnabled(true)
			}
		}
	}
	
	fun showFragment(fragment: Fragment) {
		if (fragment is DialogFragment) {
			fragment.show(activity.supportFragmentManager, null)
			return
		}
		val navigationId = fragment::class.java.getAnnotation(NavigationId::class.java)?.id
		val transaction = activity.supportFragmentManager.beginTransaction()
		if (navigationId == null) {
			customAnimations?.let {
				transaction.setCustomAnimations(it.enter, it.exit, it.popEnter, it.popExit)
			}
		}
		transaction.replace(containerId, fragment)
		if (navigationId == null) {
			transaction.addToBackStack(null)
		}
		transaction.commit()
	}
	
	annotation class NavigationId(val id: Int)
	
	data class CustomAnimations(
		val enter: Int,
		val exit: Int,
		val popEnter: Int = enter,
		val popExit: Int = exit
	)
}
