/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.common.client.dom;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/GlobalEventHandlers">Web API</a>
 */
@JsType(isNative = true)
public interface GlobalEventHandlers {
  @JsProperty EventListener getOnabort();
  @JsProperty void setOnabort(EventListener onabort);

  @JsProperty EventListener getOnblur();
  @JsProperty void setOnblur(EventListener onblur);

  @JsProperty EventListener getOncancel();
  @JsProperty void setOncancel(EventListener oncancel);

  @JsProperty EventListener getOncanplay();
  @JsProperty void setOncanplay(EventListener oncanplay);

  @JsProperty EventListener getOncanplaythrough();
  @JsProperty void setOncanplaythrough(EventListener oncanplaythrough);

  @JsProperty EventListener getOnchange();
  @JsProperty void setOnchange(EventListener onchange);

  @JsProperty EventListener getOnclick();
  @JsProperty void setOnclick(EventListener onclick);

  @JsProperty EventListener getOnclose();
  @JsProperty void setOnclose(EventListener onclose);

  @JsProperty EventListener getOncontextmenu();
  @JsProperty void setOncontextmenu(EventListener oncontextmenu);

  @JsProperty EventListener getOncuechange();
  @JsProperty void setOncuechange(EventListener oncuechange);

  @JsProperty EventListener getOndblclick();
  @JsProperty void setOndblclick(EventListener ondblclick);

  @JsProperty EventListener getOndrag();
  @JsProperty void setOndrag(EventListener ondrag);

  @JsProperty EventListener getOndragend();
  @JsProperty void setOndragend(EventListener ondragend);

  @JsProperty EventListener getOndragenter();
  @JsProperty void setOndragenter(EventListener ondragenter);

  @JsProperty EventListener getOndragexit();
  @JsProperty void setOndragexit(EventListener ondragexit);

  @JsProperty EventListener getOndragleave();
  @JsProperty void setOndragleave(EventListener ondragleave);

  @JsProperty EventListener getOndragover();
  @JsProperty void setOndragover(EventListener ondragover);

  @JsProperty EventListener getOndragstart();
  @JsProperty void setOndragstart(EventListener ondragstart);

  @JsProperty EventListener getOndrop();
  @JsProperty void setOndrop(EventListener ondrop);

  @JsProperty EventListener getOndurationchange();
  @JsProperty void setOndurationchange(EventListener ondurationchange);

  @JsProperty EventListener getOnemptied();
  @JsProperty void setOnemptied(EventListener onemptied);

  @JsProperty EventListener getOnended();
  @JsProperty void setOnended(EventListener onended);

  @JsProperty EventListener getOnerror();
  @JsProperty void setOnerror(EventListener onerror);

  @JsProperty EventListener getOnfocus();
  @JsProperty void setOnfocus(EventListener onfocus);

  @JsProperty EventListener getOninput();
  @JsProperty void setOninput(EventListener oninput);

  @JsProperty EventListener getOninvalid();
  @JsProperty void setOninvalid(EventListener oninvalid);

  @JsProperty EventListener getOnkeydown();
  @JsProperty void setOnkeydown(EventListener onkeydown);

  @JsProperty EventListener getOnkeypress();
  @JsProperty void setOnkeypress(EventListener onkeypress);

  @JsProperty EventListener getOnkeyup();
  @JsProperty void setOnkeyup(EventListener onkeyup);

  @JsProperty EventListener getOnload();
  @JsProperty void setOnload(EventListener onload);

  @JsProperty EventListener getOnloadeddata();
  @JsProperty void setOnloadeddata(EventListener onloadeddata);

  @JsProperty EventListener getOnloadedmetadata();
  @JsProperty void setOnloadedmetadata(EventListener onloadedmetadata);

  @JsProperty EventListener getOnloadstart();
  @JsProperty void setOnloadstart(EventListener onloadstart);

  @JsProperty EventListener getOnmousedown();
  @JsProperty void setOnmousedown(EventListener onmousedown);

  @JsProperty EventListener getOnmousemove();
  @JsProperty void setOnmousemove(EventListener onmousemove);

  @JsProperty EventListener getOnmouseout();
  @JsProperty void setOnmouseout(EventListener onmouseout);

  @JsProperty EventListener getOnmouseover();
  @JsProperty void setOnmouseover(EventListener onmouseover);

  @JsProperty EventListener getOnmouseup();
  @JsProperty void setOnmouseup(EventListener onmouseup);

  @JsProperty EventListener getOnmousewheel();
  @JsProperty void setOnmousewheel(EventListener onmousewheel);

  @JsProperty EventListener getOnpause();
  @JsProperty void setOnpause(EventListener onpause);

  @JsProperty EventListener getOnplay();
  @JsProperty void setOnplay(EventListener onplay);

  @JsProperty EventListener getOnplaying();
  @JsProperty void setOnplaying(EventListener onplaying);

  @JsProperty EventListener getOnpointerdown();
  @JsProperty void setOnpointerdown(EventListener onpointerdown);

  @JsProperty EventListener getOnpointermove();
  @JsProperty void setOnpointermove(EventListener onpointermove);

  @JsProperty EventListener getOnpointerup();
  @JsProperty void setOnpointerup(EventListener onpointerup);

  @JsProperty EventListener getOnpointercancel();
  @JsProperty void setOnpointercancel(EventListener onpointercancel);

  @JsProperty EventListener getOnpointerover();
  @JsProperty void setOnpointerover(EventListener onpointerover);

  @JsProperty EventListener getOnpointerout();
  @JsProperty void setOnpointerout(EventListener onpointerout);

  @JsProperty EventListener getOnpointerenter();
  @JsProperty void setOnpointerenter(EventListener onpointerenter);

  @JsProperty EventListener getOnpointerleave();
  @JsProperty void setOnpointerleave(EventListener onpointerleave);

  @JsProperty EventListener getOnpointerlockchange();
  @JsProperty void setOnpointerlockchange(EventListener onpointerlockchange);

  @JsProperty EventListener getOnpointerlockerror();
  @JsProperty void setOnpointerlockerror(EventListener onpointerlockerror);

  @JsProperty EventListener getOnprogress();
  @JsProperty void setOnprogress(EventListener onprogress);

  @JsProperty EventListener getOnratechange();
  @JsProperty void setOnratechange(EventListener onratechange);

  @JsProperty EventListener getOnreadystatechange();
  @JsProperty void setOnreadystatechange(EventListener onreadystatechange);

  @JsProperty EventListener getOnreset();
  @JsProperty void setOnreset(EventListener onreset);

  @JsProperty EventListener getOnscroll();
  @JsProperty void setOnscroll(EventListener onscroll);

  @JsProperty EventListener getOnseeked();
  @JsProperty void setOnseeked(EventListener onseeked);

  @JsProperty EventListener getOnseeking();
  @JsProperty void setOnseeking(EventListener onseeking);

  @JsProperty EventListener getOnselect();
  @JsProperty void setOnselect(EventListener onselect);

  @JsProperty EventListener getOnselectionchange();
  @JsProperty void setOnselectionchange(EventListener onselectionchange);

  @JsProperty EventListener getOnshow();
  @JsProperty void setOnshow(EventListener onshow);

  @JsProperty EventListener getOnstalled();
  @JsProperty void setOnstalled(EventListener onstalled);

  @JsProperty EventListener getOnsubmit();
  @JsProperty void setOnsubmit(EventListener onsubmit);

  @JsProperty EventListener getOnsuspend();
  @JsProperty void setOnsuspend(EventListener onsuspend);

  @JsProperty EventListener getOntimeupdate();
  @JsProperty void setOntimeupdate(EventListener ontimeupdate);

  @JsProperty EventListener getOnvolumechange();
  @JsProperty void setOnvolumechange(EventListener onvolumechange);

  @JsProperty EventListener getOntouchcancel();
  @JsProperty void setOntouchcancel(EventListener ontouchcancel);

  @JsProperty EventListener getOntouchend();
  @JsProperty void setOntouchend(EventListener ontouchend);

  @JsProperty EventListener getOntouchmove();
  @JsProperty void setOntouchmove(EventListener ontouchmove);

  @JsProperty EventListener getOntouchenter();
  @JsProperty void setOntouchenter(EventListener ontouchenter);

  @JsProperty EventListener getOntouchstart();
  @JsProperty void setOntouchstart(EventListener ontouchstart);

  @JsProperty EventListener getOntouchleave();
  @JsProperty void setOntouchleave(EventListener ontouchleave);

  @JsProperty EventListener getOnwaiting();
  @JsProperty void setOnwaiting(EventListener onwaiting);
}
