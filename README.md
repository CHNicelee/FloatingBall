# Android悬浮球及全局返回功能的实现
## 先来一发效果图：
前面是返回效果，最后一下是实现home键的效果
![效果图](http://upload-images.jianshu.io/upload_images/4774781-5da3a469f8adb4dc.gif?imageMogr2/auto-orient/strip)

## 前言
很久之前，就想做一个悬浮球了，毕竟是程序猿嘛，有想要的功能的时候总是想自己尝试一下，于是兴致勃勃的找了好久，都没有找到全局返回功能该如何实现！最后也无疾而终，就在前两天，又想到了这个功能，今天硬是花了好久，从一个同类软件获得了一点灵感，有一个关键的地方被我察觉到了，顺着这个思路找了很多资料，便实现了全局返回功能。
## 思路
废话不多说了，说说主要的思路吧，关键的一个类就是：`AccessibilityService`，[官方文档地址](https://developer.android.google.cn/reference/android/accessibilityservice/AccessibilityService.html "官方文档地址")，这个类与手机里面的一个功能密切相关：辅助功能-服务。官方文档来看，这个功能是为了方便有障碍的人士更好的使用手机。我们这里就不展开介绍里面的API了，为了实现我们的全局返回功能，我们只需要使用一个函数即可：`boolean performGlobalAction (int action)`,官方解释如下：
> Performs a global action. Such an action can be performed at any moment regardless of the current application or user location in that application. For example going back, going home, opening recents, etc.

翻译过来就是：
> 执行全局动作。无论该应用程序中的当前应用程序或用户位置如何，都可以随时执行此类操作。例如执行HOME键，BACK键,任务键等

其中可以传入的参数有四个：
`
GLOBAL_ACTION_BACK
GLOBAL_ACTION_HOME
GLOBAL_ACTION_NOTIFICATIONS
GLOBAL_ACTION_RECENTS
`
从字面就可以理解，我们返回功能需要的就是GLOBAL_ACTION_BACK。所以我们只需要开启服务，调用函数就可以实现全局返回功能了。
##编写代码
###最重要的服务类
我们要新建一个类去继承自上面那个类：
```
public class MyAccessibilityService extends AccessibilityService {
    public static final int BACK = 1;
    public static final int HOME = 2;
    private static final String TAG = "ICE";

    @Override
    public void onCreate() {
        super.onCreate();
		//使用EventBus代替广播
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) { }

    @Override
    public void onInterrupt() {}

    @Subscribe
    public void onReceive(Integer action){
        switch (action){
            case BACK:
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                break;
            case HOME:
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                break;
        }
    }

}
```
上面的`onReceive`方法是我们使用EventBus的订阅函数，当其他地方发送消息之后，我们这里就可以收到，然后判断是要执行后退还是回到桌面。
然后我们在AndroiManifest里面要注册我们的服务，但是这个注册的比较特殊：
首先加入权限声明：
`<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE"/>`
然后注册服务：
```
<service
            android:name=".MyAccessibilityService"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService"/>
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibilityservice"/>
</service>
```
其中resource中的内容我们要在xml包中声明，首先新建一个xml包，如下：

![包结构](http://upload-images.jianshu.io/upload_images/4774781-651671110ba6105e.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

然后新建一个accessibilityservice.xml文件，内容如下：
```
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/start_floatingBall"/><!--我这里写的是开启悬浮球功能-->
```
里面还可以设置许多属性，在这里就不介绍了，有兴趣的可以在官方文档里面查看。
到时候description的显示效果如下：

![description显示效果](http://upload-images.jianshu.io/upload_images/4774781-c128d092dcc95594.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

好了，到现在就已经完成了AccessibilityService服务的创建与注册了，接下来在Activity中启动服务就可以了: `startService(new 
Intent(this,MyAccessibilityService.class));`
使用EventBus传递事件即可实现返回:`EventBus.getDefault().post(MyAccessibilityService.BACK);`
但是要打开服务才行，简单办法是直接调用Intent跳到设置界面：
`startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));`
或者手动进入设置->辅助功能->服务->找到自己的app，然后开启服务即可。（不同的系统可能略有差异，小米就是在无障碍里面），界面如下：

![服务界面](http://upload-images.jianshu.io/upload_images/4774781-ff7d110a4857ca10.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 悬浮球的简单实现
1.自定义一个View，画一个悬浮球：
```
public class FloatingView extends View {

    public int height = 150;
    public int width = 150;
    private Paint paint;


    public  FloatingView(Context context){
        super(context);
        paint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(height,width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画大圆
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.state_one));
        canvas.drawCircle(width/2,width/2,width/2,paint);
        //画小圆圈
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(width/2,width/2, (float) (width*1.0/4),paint);

    }
```
代码很简单，是画了一个大圆，然后一个小点的圆圈。
接下来，把这个view展示在桌面：
```

public class ViewManager {
    FloatingView floatBall;
    WindowManager windowManager;
    public static ViewManager manager;
    Context context;
    private WindowManager.LayoutParams floatBallParams;

    private ViewManager(Context context) {
        this.context = context;
    }

    public static ViewManager getInstance(Context context) {
        if (manager == null) {
            manager = new ViewManager(context);
        }
        return manager;
    }
    public void showFloatBall() {
        floatBall = new FloatingView(context);
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        if (floatBallParams == null) {
            floatBallParams = new WindowManager.LayoutParams();
            floatBallParams.width = floatBall.width;
            floatBallParams.height = floatBall.height;
            floatBallParams.gravity = Gravity.TOP | Gravity.LEFT;
            floatBallParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            floatBallParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            floatBallParams.format = PixelFormat.RGBA_8888;
        }

        windowManager.addView(floatBall, floatBallParams);

        floatBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(MyAccessibilityService.BACK);
                Toast.makeText(context, "点击了悬浮球 执行后退操作", Toast.LENGTH_SHORT).show();
            }
        });

        floatBall.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                EventBus.getDefault().post(MyAccessibilityService.HOME);
                Toast.makeText(context, "长按了悬浮球  执行返回桌面", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    public int getScreenWidth() {
        return windowManager.getDefaultDisplay().getWidth();
    }

}
```
上面代码把view加入到window中，并给view设置了点击事件，以及长按事件，向AccessibilityService传递消息，执行相应的事件。
要显示悬浮窗，要声明权限：
`<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>`
然后手动开启权限！不然无法显示悬浮窗。
最后我们在Activity中开启我们自定义的悬浮窗即可：
`ViewManager.getInstance(MainActivity.this).showFloatBall();`
## 结束语
现在看来，实现一个全局返回功能真的非常简单，但是当初就真的找了非常久，怎么找，怎么试都没法实现这个功能，于是尝试着去学学别的悬浮窗的代码，但是没办法，加壳了，反编译后没法看。但是我注意到了一个细节，它要我打开服务才能使用悬浮窗的功能，所以就从这里下手，慢慢找到了实现全局返回的方法。
