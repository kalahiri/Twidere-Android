/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.view.holder

import android.view.View
import android.widget.CompoundButton
import org.mariotaku.twidere.adapter.SelectableUsersAdapter
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.support.ViewSupport

class SelectableUserViewHolder(
        itemView: View,
        adapter: SelectableUsersAdapter
) : SimpleUserViewHolder(itemView, adapter) {
    private val checkChangedListener = CompoundButton.OnCheckedChangeListener { view, value ->
        adapter.setItemChecked(layoutPosition, value)
    }

    init {
        ViewSupport.setBackground(itemView, ThemeUtils.getSelectableItemBackgroundDrawable(itemView.context))
        checkBox.visibility = View.VISIBLE
        itemView.setOnClickListener {
            checkBox.toggle()
        }
    }

    override fun displayUser(user: ParcelableUser) {
        super.displayUser(user)
        checkBox.setOnCheckedChangeListener(null)
        checkBox.isChecked = (adapter as SelectableUsersAdapter).isItemChecked(layoutPosition)
        checkBox.setOnCheckedChangeListener(checkChangedListener)
        itemView.isEnabled = !user.is_filtered
        checkBox.isEnabled = !user.is_filtered
    }

}