# CircleWaterWaveView
A Android Custom View With The Water Wave Effect.

## Import

Step 1. Import it to your project as a module;[将该控件作为module导入你的项目中]


Step 2. Add the dependency in your project build gradle.[在你的build.gradle中，添加以下依赖]

```gradle
dependencies {
    compile project(':WaterWaveView')
}
```

##Effect Picture
![](http://i.imgur.com/jjTrXb9.gif)

![](http://i.imgur.com/lqqtVdB.gif)
## Usage

In the Xml file,like this:[在布局文件中，如下]

``` xml
    <luxun.waterwaveview.CircleWaterWaveView
		android:id="@+id/waveview"
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:layout_centerHorizontal="true"
        android:layout_centerVertical="true" />
```
You can also add the following attribute for it:<br>
> app:textColor="#00ff00"<br>
> app:waterColor="#00ff00"<br>
> app:strokeColor="#00ff00"<br>
> app:backgroudColor="#00ff00"<br>
> app:amplitude="1.0"[水波振幅]<br>
> app:max="1000"<br>
> app:progress="500"<br>
> app:increase="6.0"[水波涨幅]<br>
> app:upSpeed="3"[上涨速度]<br>
> app:waterSpeed="8"[移动速度]<br>
> app:strokeSize="4dp"<br>
> app:textSize="20dp"<br>

##About Author

安卓菜鸟一枚，欢迎访问我的博客：[我的博客](http://blog.csdn.net/lx578111527 "我的博客")

If you like this,star and fork,thank you!
## License
[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)