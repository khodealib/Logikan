/*
 * Copyright (C) Logicfan, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited Proprietary and confidential
 * Written by Mahdi Javaheri Saber <m.javaherisaber@gmail.com>, Mohammad Hosein Abedini <mohammadhoseinabedini@gmail.com>  2020.
 */

package ir.logicfan.core.ui.util.extension

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import ir.logicfan.core.R

/**
 * Initialize toolbar with action bar
 * @param displayTitle should display title
 * @param displayBack should display home icon
 */
fun Fragment.initializeToolbar(toolbar: Toolbar, displayTitle: Boolean = false, displayBack: Boolean = true) =
    (this.activity as AppCompatActivity).apply {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(displayTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(displayBack)
    }

/**
 * @return nullable Int if navigation argument is not passed
 */
fun Fragment.navIntegerArgument(argument: Int): Int? =
    if (argument == resources.getInteger(R.integer.core_navigation_integer_default)) null else argument

fun Fragment.initializeToolbar(toolbar: Toolbar) = (this.activity as AppCompatActivity).apply {
    setSupportActionBar(toolbar)
}

fun Fragment.setToolbarHomeIcon(@DrawableRes drawable: Int) =
    (this.activity as AppCompatActivity).apply {
        supportActionBar?.setHomeAsUpIndicator(drawable)
    }

fun Fragment.hideKeyBoard() {
    (this.activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
        view?.windowToken,
        0
    )
}

fun Fragment.setToolbarTitle(title: String) = (this.activity as AppCompatActivity).supportActionBar?.apply {
    setDisplayShowTitleEnabled(true)
    this.title = title
}

fun Fragment.setToolbarTitle(@StringRes title: Int) = (this.activity as AppCompatActivity).supportActionBar?.apply {
    setDisplayShowTitleEnabled(true)
    this.title = getString(title)
}

fun Fragment.openChildFragment(@IdRes viewId: Int, fragment: Fragment, addToBackStack: Boolean = false) {
    childFragmentManager.beginTransaction().apply {
        replace(viewId, fragment)
        if (addToBackStack) {
            addToBackStack(fragment.javaClass.simpleName)
        }
        commit()
    }
}

fun Fragment.notImplementedToast() {
    Toast.makeText(this.context, "Not implemented yet!", Toast.LENGTH_SHORT).show()
}

fun Fragment.showTryLaterErrorToast() {
    Toast.makeText(this.context, R.string.core_error_try_later, Toast.LENGTH_LONG).show()
}

fun Fragment.showTryAgainErrorToast() {
    Toast.makeText(this.context, R.string.core_error_try_again, Toast.LENGTH_LONG).show()
}