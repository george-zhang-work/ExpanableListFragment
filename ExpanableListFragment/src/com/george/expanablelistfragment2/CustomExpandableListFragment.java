
package com.george.expanablelistfragment2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ToggleButton;

public class CustomExpandableListFragment extends Fragment implements
        ExpandableListView.OnGroupExpandListener, ExpandableListView.OnGroupCollapseListener,
        ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener,
        AbsListView.OnScrollListener {
    public final static String TAG = CustomExpandableListFragment.class.getSimpleName();

    ExpandableListAdapter mAdapter;
    ExpandableListView mList;
    protected View mFlotageView;
    private int indicatorGroupHeight;

    private View.OnClickListener mFlotageViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int groupPos = (Integer) v.getTag();
            mList.collapseGroup(groupPos);
            mList.setSelectedGroup(groupPos);
        }
    };

    /**
     * The subclass should overrider this method to display the flotage view
     * content.
     */
    public void onFlotageViewContentChanged(int groupPosition, int childPosition) {
        mFlotageView.setTag(groupPosition);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
        return false;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        ((ToggleButton) v.findViewById(R.id.expandable_toggle_btn)).toggle();
        return false;
    }

    @Override
    public void onGroupCollapse(int groupPosition) {
    }

    @Override
    public void onGroupExpand(int groupPosition) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.expandable_list, null, false);
        View emptyView = v.findViewById(android.R.id.empty);
        mList = (ExpandableListView) v.findViewById(android.R.id.list);
        if (mList == null) {
            throw new RuntimeException(
                    "Your content must have a ExpandableListView whose id attribute is "
                            + "'android.R.id.list'");
        }
        if (emptyView != null) {
            mList.setEmptyView(emptyView);
        }
        mList.setOnChildClickListener(this);
        mList.setOnGroupClickListener(this);
        mList.setOnGroupCollapseListener(this);
        mList.setOnGroupExpandListener(this);

        mFlotageView = v.findViewById(R.id.flotage_view);
        if (mFlotageView == null) {
            throw new RuntimeException(
                    "Your content must have a Flotage View whose id attribute is "
                            + "'R.id.flotage_view'");
        }
        mFlotageView.setTag(0);
        mFlotageView.setOnClickListener(mFlotageViewOnClickListener);
        mList.setOnScrollListener(this);
        return v;
    }

    /**
     * Provide the adapter for the expandable list.
     */
    public void setListAdapter(ExpandableListAdapter adapter) {
        synchronized (this) {
            ensureList();
            mAdapter = adapter;
            mList.setAdapter(adapter);
        }
    }

    /**
     * Get the fragment's expandable list view widget. This can be used to get
     * the selection, set the selection, and many other useful functions.
     * 
     * @see ExpandableListView
     */
    public ExpandableListView getExpandableListView() {
        ensureList();
        return mList;
    }

    /**
     * Get the ExpandableListAdapter associated with this fragment's
     * ExpandableListView.
     */
    public ExpandableListAdapter getExpandableListAdapter() {
        return mAdapter;
    }

    /**
     * Gets the ID of the currently selected group or child.
     * 
     * @return The ID of the currently selected group or child.
     */
    public long getSelectedId() {
        return mList.getSelectedId();
    }

    /**
     * Gets the position (in packed position representation) of the currently
     * selected group or child. Use
     * {@link ExpandableListView#getPackedPositionType},
     * {@link ExpandableListView#getPackedPositionGroup}, and
     * {@link ExpandableListView#getPackedPositionChild} to unpack the returned
     * packed position.
     * 
     * @return A packed position representation containing the currently
     *         selected group or child's position and type.
     */
    public long getSelectedPosition() {
        return mList.getSelectedPosition();
    }

    /**
     * Sets the selection to the specified child. If the child is in a collapsed
     * group, the group will only be expanded and child subsequently selected if
     * shouldExpandGroup is set to true, otherwise the method will return false.
     * 
     * @param groupPosition The position of the group that contains the child.
     * @param childPosition The position of the child within the group.
     * @param shouldExpandGroup Whether the child's group should be expanded if
     *            it is collapsed.
     * @return Whether the selection was successfully set on the child.
     */
    public boolean setSelectedChild(int groupPosition, int childPosition, boolean shouldExpandGroup) {
        return mList.setSelectedChild(groupPosition, childPosition, shouldExpandGroup);
    }

    /**
     * Sets the selection to the specified group.
     * 
     * @param groupPosition The position of the group that should be selected.
     */
    public void setSelectedGroup(int groupPosition) {
        mList.setSelectedGroup(groupPosition);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        if (firstVisibleItem == 0) {
            mFlotageView.setVisibility(View.GONE);
        }
        // The first visible flat item position
        final int fvPos = view.getFirstVisiblePosition();
        final int fPos = view.pointToPosition(0, 0);
        if (fPos != AdapterView.INVALID_POSITION) {
            // To get the corresponds packed position.
            final long pPos = mList.getExpandableListPosition(fPos);
            // To check whether the packed position is header or footer.
            if (pPos != ExpandableListView.PACKED_POSITION_VALUE_NULL) {
                // Extract the group and child position.
                final int groupPos = ExpandableListView.getPackedPositionGroup(pPos);
                final int childPos = ExpandableListView.getPackedPositionChild(pPos);
                if (childPos == AdapterView.INVALID_POSITION) {
                    // The flat position is a group item, no child item.
                    // Then try to get the group item height.
                    indicatorGroupHeight = mList.getChildAt(fPos - fvPos).getHeight();
                }
                if (indicatorGroupHeight == 0) {
                    return;
                }
                // Set the flotView's visibility and data.
                if (mList.isGroupExpanded(groupPos)) {
                    mFlotageView.setVisibility(View.VISIBLE);
                    onFlotageViewContentChanged(groupPos, childPos);
                    // Try to adjust the flotageView's place.
                    final int nfPos = mList.pointToPosition(0, indicatorGroupHeight);
                    if (nfPos != AdapterView.INVALID_POSITION && fPos != nfPos) {
                        final long npPos = mList.getExpandableListPosition(nfPos);
                        final int ngroupPos = ExpandableListView.getPackedPositionGroup(npPos);
                        final int newIndicatorGroupHeight;
                        if (groupPos != ngroupPos) {
                            newIndicatorGroupHeight = mList.getChildAt(nfPos - fvPos).getTop();
                        } else {
                            newIndicatorGroupHeight = indicatorGroupHeight;
                        }
                        // Try to adjust the list.
                        MarginLayoutParams params = (MarginLayoutParams) mFlotageView
                                .getLayoutParams();
                        params.topMargin = -(indicatorGroupHeight - newIndicatorGroupHeight);
                        mFlotageView.setLayoutParams(params);
                    }
                } else {
                    mFlotageView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void ensureList() {
        // Remind do nothing.
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

}
