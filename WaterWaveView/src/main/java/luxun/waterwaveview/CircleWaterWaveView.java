package luxun.waterwaveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Region;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * TODO:动态水波载入控件
 *
 * @author luxun
 */
public class CircleWaterWaveView extends SurfaceView implements SurfaceHolder.Callback {
    private int textColor = Color.WHITE;
    private float mOutStrokeWidth = 10;//外圈宽度
    private Point mCenterPoint; //圆的中心点
    int mCurrentHight = 0;//当前水位
    int mRadius = 0;//内圆半径
    int mOutRadius = 0;//外圆半径
    boolean mStart = false;//是否开始绘图
    float mTextSise = 50;//文字大小
    int mTranX = 0;//水波平移量
    private Paint mCirclePaint;//内圆画笔
    private Paint mOutCirclePaint;//外圆画笔
    private Paint mWaterPaint;//正弦曲线画笔
    private Paint mTextPaint;//文字画笔
    private SurfaceHolder mHolder;
    private RenderThread renderThread;//绘图线程
    private boolean isDrawing = false;// 控制绘制的开关
    private int mCircleColor = Color.parseColor("#D2F557");//背景内圆颜色
    private int mOutStrokeColor = Color.parseColor("#B8D86A");//外圆颜色
    private int mWaterColor = Color.parseColor("#04DD98");//水波颜色
    private int mWaterTaget = 0;// 目标水位
    private int flowNum = 0;//目标水位百分比
    private int mWaterSpeed = 10;//水波起伏速度
    private int mUpSpeed = 2;//水面上升速度
    private int max = 100; //最大进度
    private int progress = 0;//当前进度
    private float amplitude = 1f;//水波振幅
    private float increase = 6f;//水波涨幅

    public CircleWaterWaveView(Context context) {
        super(context);
        init(null, 0);
    }

    public CircleWaterWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CircleWaterWaveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        loadAttrs(attrs, defStyle);
        setZOrderOnTop(true);//使SurfaceView透明化 ，可以看见layout的背景
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);//支持透明度

        mCirclePaint = new Paint();
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setAntiAlias(true);

        mOutCirclePaint = new Paint();
        mOutCirclePaint.setColor(mOutStrokeColor);
        mOutCirclePaint.setStyle(Paint.Style.FILL);
        mOutCirclePaint.setAntiAlias(true);

        mWaterPaint = new Paint();
        mWaterPaint.setStrokeWidth(1.0F);
        mWaterPaint.setColor(mWaterColor);
        mWaterPaint.setStyle(Paint.Style.FILL);
        mWaterPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setStrokeWidth(1.0F);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(mTextSise);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setAntiAlias(true);

        renderThread = new RenderThread();
    }


    /**
     * 载入相关的属性
     *
     * @param attrs
     */
    private void loadAttrs(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CircleWaterWaveView, defStyle, 0);
        textColor = a.getColor(
                R.styleable.CircleWaterWaveView_textColor,
                textColor);
        mTextSise = a.getDimension(R.styleable.CircleWaterWaveView_textSize, mTextSise);
        mOutStrokeWidth = a.getDimension(R.styleable.CircleWaterWaveView_strokeSize, mOutStrokeWidth);
        mCircleColor = a.getColor(R.styleable.CircleWaterWaveView_backgroudColor, mCircleColor);
        mOutStrokeColor = a.getColor(R.styleable.CircleWaterWaveView_strokeColor, mOutStrokeColor);
        mWaterColor = a.getColor(R.styleable.CircleWaterWaveView_waterColor, mWaterColor);
        progress = a.getInt(R.styleable.CircleWaterWaveView_progress, progress);
        if (progress < 0) {
            throw new RuntimeException("progress can not less than 0");
        }
        max = a.getInt(R.styleable.CircleWaterWaveView_max, max);
        mOutStrokeWidth = a.getDimension(R.styleable.CircleWaterWaveView_strokeSize, mOutStrokeWidth);
        amplitude = a.getFloat(R.styleable.CircleWaterWaveView_amplitude, amplitude);
        increase = a.getFloat(R.styleable.CircleWaterWaveView_increase, increase);
        mWaterSpeed = a.getInt(R.styleable.CircleWaterWaveView_waterSpeed, mWaterSpeed);
        mUpSpeed = a.getInt(R.styleable.CircleWaterWaveView_upSpeed, mUpSpeed);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        isDrawing = true;
        new Thread(renderThread).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        int minLength = Math.min(width, height);
        mOutRadius = minLength / 2;
        mRadius = (int) (0.5 * (minLength - mOutStrokeWidth));
        mCenterPoint = new Point(minLength / 2, minLength / 2);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isDrawing = false;
    }

    /**
     * 绘制界面的线程
     *
     * @author luxun
     */
    private class RenderThread implements Runnable {
        @Override
        public void run() {
            // 不停绘制界面，这里是异步绘制，不采用外部通知开启绘制的方式，水波根据数据更新才会开始增长
            while (isDrawing) {
                if (mWaterTaget > mCurrentHight) {
                    mCurrentHight = mCurrentHight + mUpSpeed;
                    if (mWaterTaget <= mCurrentHight) {
                        mCurrentHight = mWaterTaget;
                    }
                }
                if (mStart) {
                    if (mTranX > mRadius) {
                        mTranX = 0;
                    }
                    mTranX -= mWaterSpeed;
                }
                drawUI();
                SystemClock.sleep(25);//控制刷新速率，减少cpu占用
            }
        }
    }

    public void drawUI() {
        Canvas canvas = mHolder.lockCanvas();//锁定画布
        try {
            drawCanvas(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null)
                mHolder.unlockCanvasAndPost(canvas);//释放画布
        }
    }

    /**
     * 绘制图像
     *
     * @author luxun
     */
    private void drawCanvas(Canvas canvas) {
        if (canvas == null)
            return;
        //画背景圆圈
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mOutRadius, mOutCirclePaint);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius, mCirclePaint);
        if (mStart) {
            //计算正弦曲线的路径
            int mH = mCenterPoint.y + mRadius - mCurrentHight;
            int length = 2 * mOutRadius;
            Path path = new Path();
            path.moveTo(0, mH);
            for (int i = 0; i < length; i++) {
                int x = i;
                int y = (int) (Math.sin(Math.toRadians(x + mTranX) / amplitude) * mRadius / increase);
                path.lineTo(x, mH + y);
            }
            path.lineTo(length, mH);
            path.lineTo(length, mCenterPoint.y + mRadius);
            path.lineTo(0, mCenterPoint.y + mRadius);
            path.lineTo(0, mH);
            canvas.save();//保存画布状态
            //这里与圆形取交集，除去正弦曲线多画的部分
            Path pc = new Path();
            pc.addCircle(mCenterPoint.x, mCenterPoint.y, mRadius, Path.Direction.CCW);
            canvas.clipPath(pc, Region.Op.INTERSECT);//切割画布
            canvas.drawPath(path, mWaterPaint);
            //绘制文字
            canvas.drawText(flowNum + "%", mCenterPoint.x, mCenterPoint.y + mTextSise / 2, mTextPaint);
            canvas.restore();//恢复画布状态
        }
    }

    public void setProgress(int progress) {
        if (progress < 0) {
            throw new RuntimeException("progress can not less than 0");
        }
        this.progress = progress;
        if (this.progress > max) {
            this.progress = max;
        }
        flowNum = this.progress * 100 / max;
        mStart = true;
        mWaterTaget = 2 * mRadius * progress / max;//算出目标水位高度
    }


    public void setCircleColor(int mCircleColor) {
        this.mCircleColor = mCircleColor;
    }


    public void setOutStrokeColor(int mOutStrokeColor) {
        this.mOutStrokeColor = mOutStrokeColor;
    }

    public void setWaterColor(int mWaterColor) {
        this.mWaterColor = mWaterColor;
    }

    public int getWaterSpeed() {
        return mWaterSpeed;
    }

    public void setWaterSpeed(int mWaterSpeed) {
        if (mWaterSpeed < 0) {
            throw new RuntimeException("WaterSpeed can not less than 0");
        }
        this.mWaterSpeed = mWaterSpeed;
    }

    public void setmUpSpeed(int mUpSpeed) {
        this.mUpSpeed = mUpSpeed;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        if (max < 0) {
            throw new RuntimeException("max can not less than 0");
        }
        this.max = max;
    }

    public int getProgress() {
        return progress;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public float getIncrease() {
        return increase;
    }

    public void setIncrease(float increase) {
        this.increase = increase;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setOutStrokeWidth(float mOutStrokeWidth) {
        this.mOutStrokeWidth = mOutStrokeWidth;
    }

    public void setTextSise(float s) {
        mTextSise = s;
        mTextPaint.setTextSize(s);
    }

    //设置水波起伏速度
    public void setWaveSpeed(int speed) {
        mWaterSpeed = speed;
    }

    //设置水面上升速度
    public void setUpSpeed(int speed) {
        mUpSpeed = speed;
    }

    public void setColor(int waveColor, int circleColor, int outcircleColor) {
        mWaterColor = waveColor;
        mCircleColor = circleColor;
        mOutStrokeColor = outcircleColor;
        mWaterPaint.setColor(mWaterColor);
        mCirclePaint.setColor(mCircleColor);
        mOutCirclePaint.setColor(mOutStrokeColor);
    }
}
