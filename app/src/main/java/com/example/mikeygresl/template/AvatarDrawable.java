package com.example.mikeygresl.template;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;

public class AvatarDrawable extends Drawable {
  private TextPaint namePaint;
  private int color;
  private int textColor;
  private StaticLayout textLayout;
  private float textWidth;
  private float textHeight;
  private float textLeft;
  private StringBuilder stringBuilder = new StringBuilder(5);
  private int maxLayoutTextWidth = 0;

  private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private static final int[] AVATAR_BACKGROUNDS = {
      Color.BLACK,
      Color.RED,
      Color.GREEN,
      Color.MAGENTA,
      Color.YELLOW,
      Color.BLUE
  };

  private static final int[] TEXT_COLORS = {
      Color.WHITE,
      Color.WHITE,
      Color.WHITE,
      Color.WHITE,
      Color.BLACK,
      Color.WHITE
  };

  public AvatarDrawable(Context context) {
    super();

    paint.setStyle(Paint.Style.FILL);

    namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    namePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18,
        context.getResources().getDisplayMetrics()));

    maxLayoutTextWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100,
        context.getResources().getDisplayMetrics());
  }

  public static int getColorIndex(String idString) {
    return Math.abs(idString.hashCode() % AVATAR_BACKGROUNDS.length);
  }

  public void setColor(int value) {
    color = value;
  }

  public void setTextSize(int size) {
    namePaint.setTextSize(size);
  }

  public int getColor() {
    return color;
  }

  public void setInfo(String id, String firstName, String lastName) {
    color = AVATAR_BACKGROUNDS[getColorIndex(id)];
    textColor = TEXT_COLORS[getColorIndex(id)];

    if (firstName == null || firstName.length() == 0) {
      firstName = lastName;
      lastName = null;
    }

    stringBuilder.setLength(0);

    if (firstName != null && firstName.length() > 0) {
      stringBuilder.appendCodePoint(firstName.codePointAt(0));
    }
    if (lastName != null && lastName.length() > 0) {
      Integer lastch = null;
      for (int a = lastName.length() - 1; a >= 0; a--) {
        if (lastch != null && lastName.charAt(a) == ' ') {
          break;
        }
        lastch = lastName.codePointAt(a);
      }
      if (Build.VERSION.SDK_INT > 17) {
        stringBuilder.append("\u200C");
      }
      stringBuilder.appendCodePoint(lastch);
    } else if (firstName != null && firstName.length() > 0) {
      for (int a = firstName.length() - 1; a >= 0; a--) {
        if (firstName.charAt(a) == ' ') {
          if (a != firstName.length() - 1 && firstName.charAt(a + 1) != ' ') {
            if (Build.VERSION.SDK_INT > 17) {
              stringBuilder.append("\u200C");
            }
            stringBuilder.appendCodePoint(firstName.codePointAt(a + 1));
            break;
          }
        }
      }
    }

    if (stringBuilder.length() > 0) {
      String text = stringBuilder.toString().toUpperCase();
      try {
        textLayout = new StaticLayout(text, namePaint, maxLayoutTextWidth,
            Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        if (textLayout.getLineCount() > 0) {
          textLeft = textLayout.getLineLeft(0);
          textWidth = textLayout.getLineWidth(0);
          textHeight = textLayout.getLineBottom(0);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      textLayout = null;
    }

    Log.d("Avatar", stringBuilder.toString());
  }

  @Override
  public void draw(Canvas canvas) {
    Rect bounds = getBounds();
    Log.d("Avatar", String.valueOf(bounds));
    if (bounds == null) {
      return;
    }
    int size = bounds.width();
    namePaint.setColor(textColor);
    paint.setColor(color);
    canvas.save();
    canvas.translate(bounds.left, bounds.top);
    canvas.drawCircle(size / 2.0f, size / 2.0f, size / 2.0f, paint);

    if (textLayout != null) {
      canvas.translate((size - textWidth) / 2 - textLeft, (size - textHeight) / 2);
      textLayout.draw(canvas);
    }
    canvas.restore();
  }

  @Override
  public void setAlpha(int alpha) {

  }

  @Override
  public void setColorFilter(ColorFilter cf) {

  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSPARENT;
  }
}