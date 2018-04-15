# QScaleView
自定义滚动刻度尺控件

![自定义滑动刻度尺](https://github.com/272664150/QScaleView/blob//master/images/QScaleView.gif)


目前支持的自定义属性
-----
    <declare-styleable name="ScaleView">
        <!-- 基线是否显示 -->
        <attr name="baseLineEnable" format="boolean" />
        <!-- 基线的颜色 -->
        <attr name="baseLineColor" format="reference|color" />
        <!-- 基线的高度 -->
        <attr name="baseLineHeight" format="dimension" />
        <!-- 基线与底部的间距 -->
        <attr name="baseLineMarginBottom" format="dimension" />

        <!-- 刻度尺两侧空白区域的刻度线是否显示 -->
        <attr name="scaleLineExtendEnable" format="boolean" />
        <!-- 刻度线的颜色 -->
        <attr name="scaleLineColor" format="reference|color" />
        <!-- 刻度线的宽度 -->
        <attr name="scaleLineWidth" format="dimension" />
        <!-- 刻度值的颜色 -->
        <attr name="scaleLineTextColor" format="reference|color" />
        <!-- 刻度值字体大小 -->
        <attr name="scaleLineTextSize" format="dimension" />
        <!-- 刻度线间的宽度 -->
        <attr name="scaleLineSpaceWidth" format="dimension" />
        <!-- 刻度线间的子区间数 -->
        <attr name="scaleLineSubSpaceCount" format="integer" />

        <!-- 标线是否显示 -->
        <attr name="markLineEnable" format="boolean" />
        <!-- 标线的颜色 -->
        <attr name="markLineColor" format="reference|color" />
        <!-- 标线的宽度 -->
        <attr name="markLineWidth" format="dimension" />
        <!-- 标线顶部托盘的颜色 -->
        <attr name="markLineTrayColor" format="reference|color" />
        <!-- 标线顶部托盘的样式 -->
        <attr name="markLineTrayStyle">
            <enum name="none" value="0" />
            <enum name="solid_line" value="1" />
            <enum name="dotted_line" value="2" />
            <enum name="inverted_triangle" value="3" />
        </attr>
        <!-- 标线值的颜色 -->
        <attr name="markLineTextColor" format="reference|color" />
        <!-- 标线值字体大小 -->
        <attr name="markLineTextSize" format="dimension" />
        <!-- 显示标线值时，对应的刻度值是否隐藏 -->
        <attr name="markLineRetainScaleLineValueEnable" format="boolean" />
    </declare-styleable>


初始化刻度值及默认下标
-----
    QScaleView scaleView = findViewById(R.id.select_scale_widget);
    scaleView.setScaleInfo(list, 0);


获取当前的刻度值及下标
-----
    scaleView.setOnScaleChangeListener(new QScaleView.OnScaleChangeListener() {
        @Override
        public void onScaleChange(String scale, int position) {
            Log.e("QTest", "view  ->  scale:  " + scale + "  position:  " + position);
        }
    });