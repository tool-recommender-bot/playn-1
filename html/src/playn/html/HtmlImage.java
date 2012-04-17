/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.html;

import com.google.gwt.canvas.dom.client.CanvasPattern;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.webgl.client.WebGLTexture;

import pythagoras.f.MathUtil;

import playn.core.Image;
import playn.core.Pattern;
import playn.core.ResourceCallback;
import playn.core.gl.GLContext;
import playn.core.gl.ImageGL;

class HtmlImage extends ImageGL implements HtmlCanvas.Drawable {

  private static native boolean isComplete(ImageElement img) /*-{
    return img.complete;
  }-*/;

  private static native void fakeComplete(CanvasElement img) /*-{
   img.complete = true; // CanvasElement doesn't provide a 'complete' property
  }-*/;

  ImageElement img;

  HtmlImage(CanvasElement img) {
    fakeComplete(img);
    this.img = img.cast();
  }

  HtmlImage(ImageElement img) {
    this.img = img;
  }

  @Override
  public int height() {
    return img == null ? 0 : img.getHeight();
  }

  @Override
  public int width() {
    return img == null ? 0 : img.getWidth();
  }

  @Override
  public void addCallback(final ResourceCallback<? super Image> callback) {
    if (isReady()) {
      callback.done(this);
    } else {
      HtmlPlatform.addEventListener(img, "load", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent evt) {
          callback.done(HtmlImage.this);
        }
      }, false);
      HtmlPlatform.addEventListener(img, "error", new EventHandler() {
        @Override
        public void handleEvent(NativeEvent evt) {
          callback.error(new RuntimeException("Error loading image " + img.getSrc()));
        }
      }, false);
    }
  }

  @Override
  public boolean isReady() {
    return isComplete(this.img);
  }

  @Override
  public Pattern toPattern() {
    // TODO: if we're not ready, this will go haywire, should we except? log a warning?
    return new HtmlPattern(this);
  }

  @Override
  public Region subImage(float x, float y, float width, float height) {
    return new HtmlImageRegion(this, x, y, width, height);
  }

  @Override
  public void draw(Context2d ctx, float x, float y, float width, float height) {
    draw(ctx, 0, 0, width(), height(), x, y, width, height);
  }

  @Override
  public void draw(Context2d ctx, float sx, float sy, float sw, float sh,
            float dx, float dy, float dw, float dh) {
    ctx.drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh);
  }

  @Override
  protected void updateTexture(GLContext ctx, Object tex) {
    ((HtmlGLContext)ctx).updateTexture((WebGLTexture)tex, img);
  }

  // TODO: override this in HtmlImageRegionCanvas and create tileable copy of our image
  CanvasPattern createPattern(Context2d ctx, boolean repeatX, boolean repeatY) {
    Context2d.Repetition repeat;
    if (repeatX) {
      if (repeatY) {
        repeat = Context2d.Repetition.REPEAT;
      } else {
        repeat = Context2d.Repetition.REPEAT_X;
      }
    } else if (repeatY) {
      repeat = Context2d.Repetition.REPEAT_Y;
    } else {
      return null;
    }
    return ctx.createPattern(img, repeat);
  }

  ImageElement subImageElement(float x, float y, float width, float height) {
    CanvasElement canvas = Document.get().createElement("canvas").<CanvasElement>cast();
    canvas.setWidth(MathUtil.iceil(width));
    canvas.setHeight(MathUtil.iceil(height));
    canvas.getContext2d().drawImage(img, x, y, width, height, 0, 0, width, height);
    return canvas.cast();
  }
}
