package com.mapbox.mapboxsdk.maps.renderer.glsurfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

/**
 * {@link GLSurfaceView} extension that notifies a listener when the view is detached from window,
 * which is the point of destruction of the GL thread.
 */
public class MapboxGLSurfaceView extends GLSurfaceView implements SurfaceHolder.Callback {

  private OnGLSurfaceViewDetachedListener detachedListener;

  public MapboxGLSurfaceView(Context context) {
    super(context);
    getHolder().addCallback(this);
  }

  public MapboxGLSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    getHolder().addCallback(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    if (detachedListener != null) {
      detachedListener.onGLSurfaceViewDetached();
    }
    super.onDetachedFromWindow();
  }

  /**
   * Set a listener that gets notified when the view is detached from window.
   *
   * @param detachedListener listener
   */
  public void setDetachedListener(@NonNull OnGLSurfaceViewDetachedListener detachedListener) {
    if (this.detachedListener != null) {
      throw new IllegalArgumentException("Detached from window listener has been already set.");
    }
    this.detachedListener = detachedListener;
  }

  /**
   * Listener interface that notifies when a {@link MapboxGLSurfaceView} is detached from window.
   */
  public interface OnGLSurfaceViewDetachedListener {

    /**
     * Called when a {@link MapboxGLSurfaceView} is detached from window.
     */
    void onGLSurfaceViewDetached();
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    Canvas canvas = holder.lockCanvas();
    holder.unlockCanvasAndPost(canvas);
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {}
}
