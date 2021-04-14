import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

/**
 * 循环RecyclerView Pager (4 * itemTotalCount)
 * <p>
 * 1 --> 2 --> 3 --> 4|startEdge|1 --> 2 --> 3 --> 4|initPosition|1 --> 2 --> 3 --> 4|endEdge|1 --> 2 --> 3 --> 4
 * <p>
 * When current scroll position = startEdge|| endEdge ,Then current scroll position will reset = initPosition
 */

public class LooperRecyclerViewPager2 extends FrameLayout {

    private ViewPager2 vp2Content;
    private LooperRecyclerAdapterWrapper2 mAdapterWrapper;
    private RecyclerView.Adapter mRealAdapter;
    private OnPageChangeListener onPageChangeListener;

    public LooperRecyclerViewPager2(Context context) {
        super(context);
        init();
    }

    public LooperRecyclerViewPager2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LooperRecyclerViewPager2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.lay_common_looper_view_pager, this);
        vp2Content = new ViewPager2(getContext());
        addView(vp2Content, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        vp2Content.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Log.d("LooperRecyclerViewPager2", "onPageSelected pos = " + position);
                if (onPageChangeListener != null) {
                    int realPosition = mAdapterWrapper.getRealPosition(position);
                    onPageChangeListener.onPagerSelected(realPosition);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    scrollToInitPosition();
                }
            }
        });
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null && mRealAdapter != adapter) {
            mRealAdapter = adapter;
            mAdapterWrapper = new LooperRecyclerAdapterWrapper2(mRealAdapter);
            vp2Content.setAdapter(mAdapterWrapper);
            scrollToInitPosition();
        }
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    /**
     * 滚动到初始化位置
     */
    private void scrollToInitPosition() {
        int currentPos = vp2Content.getCurrentItem();
        int realAdapterCount = mAdapterWrapper.getRealAdapterCount();
        int realPosition = mAdapterWrapper.getRealPosition(currentPos);
        int endLooperEdge = realAdapterCount * 3;
        if (realAdapterCount > 1 && (currentPos <= realAdapterCount || currentPos >= endLooperEdge)) {
            int scrollPos = 0;
            if (currentPos <= realAdapterCount) {
                scrollPos = realAdapterCount + realPosition;
            }
            if (currentPos >= endLooperEdge) {
                scrollPos = realAdapterCount * 2 + realPosition;
            }
            if (scrollPos == 0) {
                scrollPos = realAdapterCount * 2;
            }
            vp2Content.setCurrentItem(scrollPos, false);
        }
        Log.d("LooperRecyclerViewPager2", "scrollToInitPosition pos = " + currentPos + "| realPos = " + realPosition);
    }

    public boolean canLooperPager() {
        return mAdapterWrapper != null && mAdapterWrapper.getRealAdapterCount() > 1;
    }

    /**
     * 滚动到下一页
     */
    public void scrollToNext() {
        if (!canLooperPager()) {
            return;
        }
        int currentPos = vp2Content.getCurrentItem();
        Log.d("LooperRecyclerViewPager2", "onPageScrollStateChanged pos = " + currentPos);
        currentPos++;
        vp2Content.setCurrentItem(currentPos);
    }

    public interface OnPageChangeListener {
        void onPagerSelected(int pageIndex);
    }

}
