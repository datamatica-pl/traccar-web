/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.widget;

import com.sencha.gxt.widget.core.client.box.MessageBox;

/**
 *
 * @author piotrkrzeszewski
 */
public class InfoMessageBox extends MessageBox {

  /**
   * Creates a message box with an error icon and the specified title and
   * message.
   * 
   * @param title the message box title
   * @param message the message displayed in the message box
   */
  public InfoMessageBox(String title, String message) {
    super(title, message);

    setIcon(ICONS.info());
  }
}