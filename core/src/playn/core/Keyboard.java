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
package playn.core;

import playn.core.util.Callback;

import java.util.List;

/**
 * Input-device interface for keyboard events. Three events are generated by keyboard input:
 * <ul>
 * <li> When any key is depressed, {@link Keyboard.Listener#onKeyDown} is called indicating the
 * logical key that was depressed. </li>
 * <li> If the depressed key also corresponds to a printable character ('c' for example, but not
 * shift or alt), {@link Keyboard.Listener#onKeyTyped} will be called to inform the app of the
 * typed character. The typed character will account for whether the shift key is depressed and
 * will be appropriately mapped to the uppercase equivalent or the appropriate alternate character
 * (for example, # for 3, in the US keyboard layout). The typed event is delivered immediately
 * after the pressed event. </li>
 * <li> When a key is released, {@link Keyboard.Listener#onKeyUp} is called, indicating the logical
 * key that was released. </li>
 * </ul>
 */
public interface Keyboard {

  /** An event dispatched when a key is pressed or released. */
  interface Event extends Events.Input {
    /**
     * The key that triggered this event, e.g. {@link Key#A}, etc.
     */
    Key key();

    /**
     * List of key modifiers during this event, e.g. {@link Key#ALT}, etc.
     */
    Modifiers modifiers();

    class Impl extends Events.Input.Impl implements Event {
      private Key key;
      private Modifiers modifiers;

      @Override
      public Key key() {
        return key;
      }

      @Override
      public Modifiers modifiers() {
        return modifiers;
      }

      public Impl(Events.Flags flags, double time, Key key, Modifiers modifiers) {
        super(flags, time);
        this.key = key;
        this.modifiers = modifiers;
      }

      @Override
      protected String name() {
        return "Keyboard.Event";
      }

      @Override
      protected void addFields(StringBuilder builder) {
        super.addFields(builder);
        builder.append(", key=").append(key);
      }
    }
  }

  /** An event dispatched when a printable character is typed. */
  interface TypedEvent extends Events.Input {
    /**
     * The character typed to trigger this event, e.g. 'c'.
     */
    char typedChar();

    class Impl extends Events.Input.Impl implements TypedEvent {
      private char typedChar;

      @Override
      public char typedChar() {
        return typedChar;
      }

      public Impl(Events.Flags flags, double time, char typedChar) {
        super(flags, time);
        this.typedChar = typedChar;
      }

      @Override
      protected String name() {
        return "Keyboard.TypedEvent";
      }

      @Override
      protected void addFields(StringBuilder builder) {
        super.addFields(builder);
        builder.append(", typedChar=").append(typedChar);
      }
    }
  }

  interface Listener {
    /**
     * Called when a key is depressed.
     */
    void onKeyDown(Event event);

    /**
     * Called when a printable character is typed.
     */
    void onKeyTyped(TypedEvent event);

    /**
     * Called when a key is released.
     */
    void onKeyUp(Event event);
  }

  /** A {@link Listener} implementation with NOOP stubs provided for each method. */
  class Adapter implements Listener {
    @Override
    public void onKeyDown(Event event) { /* NOOP! */ }
    @Override
    public void onKeyTyped(TypedEvent event) { /* NOOP! */ }
    @Override
    public void onKeyUp(Event event) { /* NOOP! */ }
  }

  /** Enumerates the different mobile keyboard types that can be requested.
   * See {@link #getText}. */
  public static enum TextType {
    DEFAULT, NUMBER, EMAIL, URL;
  }

  /**
   * Returns the currently configured global keyboard listener, or null.
   */
  Listener listener ();

  /**
   * Sets the listener that will receive keyboard events. Setting the listener to
   * {@code null} will cause keyboard events to stop being fired.
   */
  void setListener(Listener listener);

  /**
   * Returns true if this device has a hardware keyboard, false if not. Devices that lack a
   * hardware keyboard will generally not generate keyboard events. Older android devices that
   * support four hardware buttons are an exception. Use {@link #getText} for text entry on a
   * non-hardware-keyboard having device.
   */
  boolean hasHardwareKeyboard();

  /**
   * Requests a line of text from the user. On platforms that have only a virtual keyboard, this
   * will display a text entry interface, obtain the line of text, and dismiss the text entry
   * interface when finished.
   *
   * @param textType the expected type of text. On mobile devices this hint may be used to display
   * a keyboard customized to the particular type of text.
   * @param label a label to display over the text entry interface, may be null.
   * @param initialValue the initial value to display in the text input field, may be null.
   * @param callback a callback which will be notified when the text entry is complete. If the user
   * cancels the text entry process, null will be supplied. Otherwise the entered text will be
   * supplied.
   */
  void getText(TextType textType, String label, String initialValue, Callback<String> callback);
}
