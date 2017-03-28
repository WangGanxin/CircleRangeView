# CircleRangeView
自定义圆形仪表盘View，适合根据数值显示不同等级范围的场景 

# Demo

运行效果图：

![CircleRangeView](/images/circle-range-view.gif)

# Usage

- 1.布局文件引入：

```XML

    <com.ganxin.circlerangeview.CircleRangeView
        android:id="@+id/circleRangeView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:rangeColorArray="@array/circlerangeview_colors"
        app:rangeTextArray="@array/circlerangeview_txts"
        app:rangeValueArray="@array/circlerangeview_values"/>

```

自定义属性：

> rangeColorArray：等级颜色数组，必填
> 
> rangeValueArray：等级数值数组，数组长度同rangeColorArray保持一致，必填
> 
> rangeTextArray：等级文本数组，数组长度同rangeColorArray保持一致，必填
> 
> borderColor：外圆弧颜色，可选
> 
> cursorColor：指示标颜色，可选
> 
> extraTextColor：附加文本颜色，可选
> 
> rangeTextSize：等级文本字体大小，可选
> 
> extraTextSize：附加文本字体大小，可选
> 


- 2.在你的onCreate方法或者fragment的onCreateView方法中，根据id绑定该控件

```Java

    CircleRangeView circleRangeView= (CircleRangeView) findViewById(R.id.circleRangeView);

```

- 3.在合适的时机，调用方法给控件设值

```Java

    List<String> extras =new ArrayList<>();
    extras.add("收缩压：116");
    extras.add("舒张压：85  ");

    //circleRangeView.setValueWithAnim(value);
    circleRangeView.setValueWithAnim(value,extras);

```

# Contact Me

- QQ：445253393（注明来自GitHub）
- Email：mail@wangganxin.me

# License
   		Copyright 2017 CircleRangeView of copyright 守望君

   		Licensed under the Apache License, Version 2.0 (the "License");
   		you may not use this file except in compliance with the License.
   		You may obtain a copy of the License at

       		http://www.apache.org/licenses/LICENSE-2.0

   		Unless required by applicable law or agreed to in writing, software
   		distributed under the License is distributed on an "AS IS" BASIS,
   		WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   		See the License for the specific language governing permissions and
   		limitations under the License.