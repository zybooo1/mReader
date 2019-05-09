package com.zyb.base.utils;


import com.zyb.base.event.BaseEvent;

import org.greenrobot.eventbus.EventBus;

public class EventBusUtil {

    public static void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }

    public static void sendEvent(BaseEvent event) {
        EventBus.getDefault().post(event);
    }

    public static void sendStickyEvent(BaseEvent event) {
        EventBus.getDefault().postSticky(event);
    }
}
