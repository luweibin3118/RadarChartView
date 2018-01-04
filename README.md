# RadarChartView
Android实现的雷达图（Radar Chart），可用于实现对比展示，可旋转

项目添加依赖：
project/build.gradle中添加：

	allprojects {
	    repositories {
	        ...
	        maven { url 'https://jitpack.io' }
	    }
	}

project/app/build.gradle中添加：
	
	dependencies {
        compile 'com.github.luweibin3118:RadarChartView:v1.0.2'
    }


 1. 在布局文件中引入RadarChartView：

       <com.example.radarchart.RadarChartView
           android:id="@+id/radar_chart_view"
           android:layout_width="match_parent"
           android:layout_height="match_parent" />


 2. Java代码中，通过以下方法添加一条属性：

        radar_chart_view.insertType("法术", 800, 1000);
        radar_chart_view.insertType("攻击", 700, 1000);
        radar_chart_view.insertType("物抗", 550, 1000);
        radar_chart_view.insertType("魔抗", 500, 1000);
        radar_chart_view.insertType("移速", 800, 1000);
        radar_chart_view.insertType("天赋", 400, 1000);
        radar_chart_view.insertType("暴击", 80, 100);
        radar_chart_view.insertType("金钱", 9000, 10000);

 3. 添加过后必须设置RadarChartView.Config，该对象用来配置雷达图表的属性:

         RadarChartView.Config config = new RadarChartView.Config()
                 .setMaxLevel(5)
                 .setCenterPointRadius(5)
                 .setChartWidget(0.8f)
                 .setFillColor(0xff268415)
                 .setSecondFillColor(0xffcd2626)
                 .setValueLineSize(1)
                 .setValuePointRadius(5)
                 .setBackgroundColor(0x88985615)
                 .setTextColor(0x88985615)
                 .setTextSize(40)
                 .setCanScroll(true)
                 .setCanFling(true)
                 .setValueBackgroundAlpha(0.2f)
                 .setTextPosition(1.1f)
                 .setPointRadius(2);
         radar_chart_view.setConfig(config);

  4. 效果图如下：

  ![image](https://github.com/luweibin3118/RadarChartView/blob/master/app/Screenshot_20171226-185310.png)

  5. 如果要实现多个对象的属性对比，需要通过compareType方法添加对比属性：

          radar_chart_view.compareType("法术", 700, 820, 1000);
          radar_chart_view.compareType("攻击", 900, 750, 1000);
          radar_chart_view.compareType("物抗", 550, 600, 1000);
          radar_chart_view.compareType("魔抗", 750, 800, 1000);
          radar_chart_view.compareType("移速", 800, 750, 1000);
          radar_chart_view.compareType("天赋", 800, 900, 1000);
          radar_chart_view.compareType("暴击", 80, 70, 100);
          radar_chart_view.compareType("金钱", 9000, 9500, 10000);

  6. 在Config对象中设置 setCompareName("后裔","狄仁杰")，可以在左上角展示对比图标

  7. 对比图的效果图如下：

  ![image](https://github.com/luweibin3118/RadarChartView/blob/master/app/Screenshot_20171226-185135.png)

  8. 如果给Config设置如下参数，可实现雷达图随手势滑动：

                   setCanScroll(true) // true表示可以手势滑动

                   setCanFling(true)  // true表示可以Fling
