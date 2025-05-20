package com.example.mikeygresl.template;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class MessageContainer extends LinearLayout {

    public MessageContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MessageContainer(Context context) {
        this(context, null);
    }

    public MessageContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int maxWidth = (int) (getResources().getDisplayMetrics().density * 220);
        final int atMostWidthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);

        super.onMeasure(atMostWidthSpec, heightMeasureSpec);
    }
}
