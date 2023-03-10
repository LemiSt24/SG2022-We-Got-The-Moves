package com.sg2022.we_got_the_moves.ui.training.mediapipe;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.os.Looper;
import android.util.Size;
import android.view.View;
import androidx.annotation.NonNull;

public class ViewRecorder extends SurfaceMediaRecorder {
  private View mRecordedView;
  private Size mVideoSize;
  private final VideoFrameDrawer mVideoFrameDrawer =
      new VideoFrameDrawer() {
        private Matrix getMatrix(int bw, int bh, int vw, int vh) {
          Matrix matrix = new Matrix();
          float scale, scaleX = 1, scaleY = 1, transX, transY;

          if (bw > vw) {
            scaleX = ((float) vw) / bw;
          }
          if (bh > vh) {
            scaleY = ((float) vh) / bh;
          }
          scale = (Math.min(scaleX, scaleY));
          transX = (vw - bw * scale) / 2;
          transY = (vh - bh * scale) / 2;

          matrix.postScale(scale, scale);
          matrix.postTranslate(transX, transY);

          return matrix;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onDraw(Canvas canvas) {
          mRecordedView.setDrawingCacheEnabled(true);
          Bitmap bitmap = mRecordedView.getDrawingCache();

          int bitmapWidth = bitmap.getWidth();
          int bitmapHeight = bitmap.getHeight();
          int videoWidth = mVideoSize.getWidth();
          int videoHeight = mVideoSize.getHeight();
          Matrix matrix = getMatrix(bitmapWidth, bitmapHeight, videoWidth, videoHeight);
          canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
          canvas.drawBitmap(bitmap, matrix, null);

          mRecordedView.setDrawingCacheEnabled(false);
        }
      };

  @Override
  public void setVideoSize(int width, int height) throws IllegalStateException {
    super.setVideoSize(width, height);
    mVideoSize = new Size(width, height);
  }

  @Override
  public void start() throws IllegalStateException {
    if (isSurfaceAvailable()) {
      if (mVideoSize == null) {
        throw new IllegalStateException("video size is not initialized yet");
      }
      if (mRecordedView == null) {
        throw new IllegalStateException("recorded view is not initialized yet");
      }
      setWorkerLooper(Looper.getMainLooper());
      setVideoFrameDrawer(mVideoFrameDrawer);
    }

    super.start();
  }

  public void setRecordedView(@NonNull View view) throws IllegalStateException {
    mRecordedView = view;
  }
}
