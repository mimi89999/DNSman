package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class CurrentStatusView extends View {
    public CurrentStatusView(Context context) {
        super(context);
        init(null, 0);
    }

    public CurrentStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CurrentStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

    }
}
