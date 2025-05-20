package com.example.mikeygresl.template;

import java.util.ArrayList;
import java.util.List;

public class Conversation {

    private String CID;
    private List<Message> messages;

    public Conversation() {}

    public Conversation(String CID) {
        this.CID = CID;
        messages = new ArrayList<Message>();
    }

    public String getCID() {
        return CID;
    }

    public void setCID(String CID) {
        this.CID = CID;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public List<Message> getMessages() {
        return messages;

    }
}
