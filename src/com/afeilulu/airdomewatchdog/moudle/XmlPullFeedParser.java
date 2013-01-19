package com.afeilulu.airdomewatchdog.moudle;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.afeilulu.airdomewatchdog.io.Analog_C;
import com.afeilulu.airdomewatchdog.io.Digital_C;
import com.afeilulu.airdomewatchdog.io.Message;

import android.util.Xml;
import android.util.Log;

public class XmlPullFeedParser extends BaseFeedParser {
    public XmlPullFeedParser(String feedUrl) {
        super(feedUrl);
    }
    public List<Message> parse() {
        List<Message> messages = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            // auto-detect the encoding from the stream
            parser.setInput(this.getInputStream(), null);
            int eventType = parser.getEventType();
            Message currentMessage = null;
            List<Digital_C> dcs = null;
            List<Analog_C> acs = null;
            boolean done = false;
            while (eventType != XmlPullParser.END_DOCUMENT && !done){
                String name = null;
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        messages = new ArrayList<Message>();
                        dcs = new ArrayList<Digital_C>();
                        acs =  new ArrayList<Analog_C>();
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase(DATA)){
                            currentMessage = new Message();
                            currentMessage.setId(parser.getAttributeValue(null, "ID"));
                            currentMessage.setRet(parser.getAttributeValue(null, "Ret"));
                            currentMessage.setVerify_code(parser.getAttributeValue(null, "Password"));
                            
                             
                        } else if (currentMessage != null){
                            if (name.equalsIgnoreCase(NEXTTIME)){
                                currentMessage.setNexttime(parser.nextText());
                            } else if (name.equalsIgnoreCase(PERIOD)){
                                currentMessage.setPeriod(parser.nextText());
                            } else if (name.equalsIgnoreCase(PASSWORD)){
                                currentMessage.setPassword(parser.nextText());
                            } else if (name.equalsIgnoreCase(DIGITAL)){
                            	Digital_C digital_c =  new Digital_C();
                            	digital_c.setMethod(parser.getAttributeValue(null, "method"));
                            	digital_c.setMethod(parser.getAttributeValue(null, "ID"));
                            	digital_c.setAddress(parser.nextText());
                            	dcs.add(digital_c);
                            } else if (name.equalsIgnoreCase(ANALOG)){
                            	Analog_C analog_c =  new Analog_C();
                            	analog_c.setMethod(parser.getAttributeValue(null, "method"));
                            	analog_c.setMethod(parser.getAttributeValue(null, "ID"));
                            	analog_c.setAddress(parser.nextText());
                            	acs.add(analog_c);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase(DATA) && currentMessage != null){
                            messages.add(currentMessage);
                            done = true;
                        } else if (name.equalsIgnoreCase(DIGITAL)){
                        	currentMessage.setdCs(dcs);
                        } else if (name.equalsIgnoreCase(ANALOG)){
                        	currentMessage.setaCs(acs);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return messages;
    }
}
