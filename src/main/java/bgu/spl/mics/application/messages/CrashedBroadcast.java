package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {

    private final String senderId;
    private final String senderType;

    public CrashedBroadcast(String senderId, String senderType) {
        this.senderId = senderId;
        this.senderType = senderType;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderType() {
        return senderType;
    }
}
